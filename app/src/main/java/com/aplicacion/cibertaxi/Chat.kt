package com.aplicacion.cibertaxi

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.cibertaxi.R
import com.aplicacion.cibertaxi.chatAdapter.Adaptador
import com.aplicacion.cibertaxi.chatAdapter.Mensaje
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_chat.*

class Chat : AppCompatActivity() {

    val uri = "http://eleccionesargentina.online/WebServices/"

    // WebService | Iniciamos el objeto
    lateinit var queue: RequestQueue

    // SharedPreferences | Variable global en la app para guardar información ( Se guarda en `Datos de la aplicación` )
    val PREFS_FILENAME = "com.aplicacion.cibertaxi.prefs"
    var prefs: SharedPreferences? = null
    // Datos del cliente logueado
    var idusuario = 0
    var puesto = ""

    // RecyclerView Mensajes
    var rvMensajes: RecyclerView? = null

    //Adaptador
    lateinit var adapter: Adaptador


    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Hacemos Fullscreen el layout*/
        setContentView(R.layout.activity_chat)
        supportActionBar?.hide()

        // SharedPreferences | Inicialización
        prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
        idusuario = prefs!!.getInt("idusuario", 0) // ID usuario
        puesto = prefs!!.getString("puesto", "usuario") // ID usuario

        // WebService | Inicialización
        queue = Volley.newRequestQueue(this)


        // Inicializamos el RecyclerView para usarlo mas tarde
        rvMensajes = findViewById<RecyclerView>(R.id.rv_mensajes_chat) // Llamamos al RecyclerView del layout


        // Acciones de botones
        var ib_volver_chat = findViewById<ImageButton>(R.id.ib_volver_chat)
        ib_volver_chat.setOnClickListener {
            // Boton para volver al mapa
            onBackPressed() // Volvemos a la actividad anterior
        }

        et_enviar_mensaje_chat.requestFocus() // Colocamos el cursor en EditText
        ChatBox(rvMensajes) // Cargamos los mensajes en el ChatBox
        timerFunc() // Este es un timmer para hacer 'visto=si' los mensajes y actualizarlos
    }


    @SuppressLint("WrongConstant")
    private fun ChatBox(recyclerview: RecyclerView?) {
        // Este método enlaza el adaptador con el objeto `Mensaje`



        var resultados_array = ArrayList<Mensaje>()

        var url = uri+"acciones/peticionesChat.php?" +
                "idusuario=" + idusuario
        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener { response ->
                if(response.getBoolean("status")){// Verificamos que existen viajes disponibles

                    val json = response.getJSONArray("mensajes_array") // Tomamos la cadena json con los datos
                    var i=0 // inicializamos i en 0
                    while (i < json.length()){ // Este algoritmo crea un while con el tamaño del json que traemos de la base de datos con los datos de los usuarios que están pidiendo un viaje
                        var separador = json[i].toString().split("|") // Separamos el json por `|` y creamos un array para manejarlos
                        var dataMensajes = Mensaje(
                            separador[1],
                            separador[0],
                            separador[3],
                            idusuario.toString()
                        ) // Creamos el objeto
                        resultados_array.add(dataMensajes) // Lo insertamos en el array

                        i++ // aumentamos i para tomar la proxima cadena
                    }
                    // AGREGAMOS EL ARRAY DEL OBJETO `MENSAJE` AL ADAPTADOR
                    adapter = Adaptador(resultados_array, applicationContext)
                    recyclerview?.layoutManager = LinearLayoutManager(this, OrientationHelper.VERTICAL, false)
                    recyclerview?.itemAnimator = DefaultItemAnimator()
                    recyclerview?.adapter = adapter
                    adapter.notifyDataSetChanged()

                    rv_mensajes_chat.scrollToPosition(adapter.itemCount - 1)
                }
            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)



    }


    fun timerFunc(){
        var handler = Handler()
        handler.postDelayed( {
            ChatBox(rvMensajes) // Actualizamos los mensajes
            mensajeVisto() // Actualizamos los mensajes
            timerFunc()
        }, 10000) // Cada 10 segundos
    }


    fun mensajeVisto(){ // Marca todos los mensajes del usuario como visto

        var url = uri+"acciones/marcarVisto.php?" +
                "idusuario=" + idusuario
        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener { response -> },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)

    }



    fun enviarMensaje(view: View){
        var textoMensaje = et_enviar_mensaje_chat.text
        if(textoMensaje.toString() != ""){ // Si el mensaje no es un vacio =>

            var pb_chat = findViewById<ProgressBar>(R.id.pb_chat)
            var url = uri+"acciones/enviarMensaje.php?" +
                    "idusuario=" + idusuario +
                    "&tipo=" + puesto +
                    "&mensaje=" + et_enviar_mensaje_chat.text // Mensaje
            val jsonObjectRequest = JsonObjectRequest(url,null,
                Response.Listener { response ->
                    // Mensaje enviado

                    var imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager // Esconde le teclado
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0) // Esconde le teclado

                    et_enviar_mensaje_chat.setText("") // Borramos todo lo que contiene el editText
                    pb_chat.visibility = View.GONE
                    ChatBox(rvMensajes) // Actualizamos los mensajes
                },
                Response.ErrorListener { error -> error.printStackTrace() })
            queue.add(jsonObjectRequest)
        }
    }

}
