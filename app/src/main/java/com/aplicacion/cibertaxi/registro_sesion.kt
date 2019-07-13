package com.aplicacion.cibertaxi

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.cibertaxi.R
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_iniciar_sesion.*
import kotlinx.android.synthetic.main.activity_iniciar_sesion.btn_accion
import kotlinx.android.synthetic.main.activity_iniciar_sesion.et_email
import kotlinx.android.synthetic.main.activity_iniciar_sesion.et_pass
import kotlinx.android.synthetic.main.activity_registro_sesion.*

class registro_sesion : AppCompatActivity() {

    val uri = R.string.uri

    // SharedPreferences
    val PREFS_FILENAME = "com.aplicacion.cibertaxi.prefs"
    var prefs: SharedPreferences? = null


    // Webservice
    lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*Fullscreen*/
        setContentView(R.layout.activity_registro_sesion)
        supportActionBar?.hide()

        // SharedPreferences
        prefs = this.getSharedPreferences(PREFS_FILENAME, 0)


        // Webservice
        queue = Volley.newRequestQueue(this)


        btn_accionReg.setOnClickListener {
            verificarSesion()
        }
        btn_segundaAccionReg.setOnClickListener {
            val intent = Intent(this, iniciar_sesion::class.java)
            startActivity(intent)
            finish()
        }

    }

    fun abrirSesion(idusuario: Int, nombre:String, email:String, reputacion:String, puesto: String){
        // Este algoritmo abre la sesión guardando los datos del usuario en variables dentro de la aplicación con SharedPreferences

        val editor = prefs!!.edit()
        editor.putInt("iniciado", 1) // (tag, valorDefault)
        editor.putInt("idusuario", idusuario)
        editor.putString("nombre", nombre)
        editor.putString("email", email)
        editor.putString("reputacion", reputacion)
        editor.putString("puesto", puesto)
        editor.apply() // Aplicamos
        if(puesto == "usuario"){ // Si el puesto del usuario que inició es 'USUARIO' =>
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }else { // Si el puesto del usuario que inició es 'CONDUCTOR' =>
            val intent = Intent(this, vistaConductor::class.java)
            startActivity(intent)
        }
    }

    fun verificarSesion(){
        // Esta función llama a un webservice y verifica si los datos `email` y `contraseña` son correctos

        var dialog: ProgressDialog
        dialog = ProgressDialog.show(this, "", "Cargando...", true)   // Cartel de cargando

        var url = uri.toString()+"sesion/registrarSesion.php?" +
                    "email="+et_emailReg.text.trim()+
                    "&pass="+et_passReg.text+
                    "&nombre="+et_nombreReg.text.toString().trim().replace(" ","-")

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener { response ->
                Log.i("WebserviceSesionLogin","Respuesta:"+response)

                // Validamos si el usuario existe con los datos traidos del método `conexionWebservice` guardado en variable `respuesta`
                if(response.getBoolean("status")){
                    // No se encontró un usuario con ese email y contraseña, entonces se crea
                    val idusuario = response.getInt("idusuario")
                    val nombre = response.getString("nombre")
                    val email = response.getString("email")
                    val reputacion = response.getString("reputacion")
                    val puesto = response.getString("puesto")
                    // Abrimos la sesión con los datos adquiridos
                    abrirSesion(idusuario, nombre, email, reputacion, puesto)
                    dialog.setMessage("Registrado con éxito") // Cambiamos texto 'cargando' a 'registrado con éxito'
                }else{
                    // Ha encontrado un usuario con ese email y contraseña, entonces envia un error
                    Toast.makeText(this, "El usuario ya existe", Toast.LENGTH_SHORT).show()
                    et_nombreReg.isFocusable
                    dialog.dismiss() // Quitamos el cartel de cargando
                }

            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)

    }
}
