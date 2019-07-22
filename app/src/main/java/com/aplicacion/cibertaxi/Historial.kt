package com.aplicacion.cibertaxi

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.cibertaxi.R
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.aplicacion.cibertaxi.historialAdapter.Adaptador
import com.aplicacion.cibertaxi.historialAdapter.Historial
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class Historial : AppCompatActivity() {
    val uri = "https://ferrule.space/WebServices/"

    // WebService | Iniciamos el objeto
    lateinit var queue: RequestQueue

    // SharedPreferences | Variable global en la app para guardar información ( Se guarda en `Datos de la aplicación` )
    val PREFS_FILENAME = "com.aplicacion.cibertaxi.prefs"
    var prefs: SharedPreferences? = null

    // Datos del cliente logueado
    var idusuario = 0

    // RecyclerView
    var rv_historial: RecyclerView? = null
    lateinit var pb_historial: ProgressBar

    //Adaptador
    lateinit var adapter: Adaptador


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)
        supportActionBar?.hide()

        // SharedPreferences | Inicialización
        prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
        // Verificamos si existe una sesión iniciada, si no existe nos envia a iniciar_sesion.class
        idusuario = prefs!!.getInt("idusuario", 0)

        // WebService | Inicialización
        queue = Volley.newRequestQueue(this)

        // Inicializamos el RecyclerView
        rv_historial = findViewById<RecyclerView>(R.id.rv_historial)

        pb_historial = findViewById(R.id.pb_historial)


        // Acciones de botones
        var iv_volver_historial = findViewById<ImageView>(R.id.iv_volver_historial)
        iv_volver_historial.setOnClickListener {
            onBackPressed() // Volvemos a la actividad anterior
        }


        rellenarHistorial()

    }

    @SuppressLint("WrongConstant")
    fun rellenarHistorial(){
        var resultados_array = ArrayList<Historial>()
        var url = uri+"acciones/retornarHistorial.php?" +
                "idusuario=" + idusuario+
                "&puesto=" + prefs!!.getString("puesto", "usuario")
        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener { response ->
                if(response.getBoolean("status")){// Verificamos que existe historial
                    val json = response.getJSONArray("historial_array") // Tomamos la cadena json con los datos
                    var i=0 // inicializamos i en 0
                    while (i < json.length()){ // Este algoritmo crea un while con el tamaño del json que traemos de la base de datos
                        var separador = json[i].toString().split("|") // Separamos el json por `|` y creamos un array para manejarlos
                        var dataHistorial = Historial(
                            separador[0], // Puesto
                            separador[1], // Id usuario
                            separador[2], // pos inicial (lat|lon)
                            separador[3], // pos final (lat|lon)
                            separador[4], // fecha de comienzo
                            separador[5], // estado del viaje
                            separador[6] // rating
                        ) // Creamos el objeto
                        resultados_array.add(dataHistorial) // Lo insertamos en el array

                        i++ // aumentamos i para tomar la proxima cadena
                    }
                    // AGREGAMOS EL ARRAY DEL OBJETO `HISTORIAL` AL ADAPTADOR
                    adapter = Adaptador(resultados_array, applicationContext)
                    rv_historial?.layoutManager = LinearLayoutManager(this, OrientationHelper.VERTICAL, false)
                    rv_historial?.itemAnimator = DefaultItemAnimator()
                    rv_historial?.adapter = adapter
                    adapter.notifyDataSetChanged()

                    rv_historial?.scrollToPosition(0)
                    pb_historial.visibility = View.GONE

                }else{
                    pb_historial.visibility = View.GONE
                    Toast.makeText(this, "El historial está vacio", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)
    }



}
