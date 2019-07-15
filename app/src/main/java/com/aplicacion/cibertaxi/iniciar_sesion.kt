package com.aplicacion.cibertaxi

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import com.android.cibertaxi.R
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_iniciar_sesion.*
import kotlinx.android.synthetic.main.activity_maps.*

class iniciar_sesion : AppCompatActivity() {

    val uri = "http://eleccionesargentina.online/WebServices/"

    // SharedPreferences
    val PREFS_FILENAME = "com.aplicacion.cibertaxi.prefs"
    var prefs: SharedPreferences? = null


    // Webservice
    lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*Fullscreen*/
        setContentView(R.layout.activity_iniciar_sesion)
        supportActionBar?.hide()

        // SharedPreferences
        prefs = this.getSharedPreferences(PREFS_FILENAME, 0)


        // Verificamos si existe una sesión iniciada
        if(prefs!!.getInt("iniciado", 0) == 1){
            // Si la sesión está iniciada
            if(prefs!!.getString("puesto", "usuario") == "usuario"){
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                finish() // Rompemos la actividad para que el usuario no pueda volver atras y saltar el logueo
            }else{
                val intent = Intent(this, vistaConductor::class.java)
                startActivity(intent)
                finish() // Rompemos la actividad para que el usuario no pueda volver atras y saltar el logueo
            }
        }


        // Webservice
        queue = Volley.newRequestQueue(this)


        btn_accion.setOnClickListener {
            verificarSesion()
        }
        btn_segundaAccion.setOnClickListener {
            val intent = Intent(this, registro_sesion::class.java)
            startActivity(intent)
        }

    }

    fun abrirSesion(idusuario: Int, nombre:String, email:String, reputacion:String, puesto:String){
        // Este algoritmo abre la sesión guardando los datos del usuario en variables dentro de la aplicación con SharedPreferences

        val editor = prefs!!.edit()
        editor.putInt("iniciado", 1) // (tag, valorDefault)
        editor.putInt("idusuario", idusuario)
        editor.putString("nombre", nombre)
        editor.putString("email", email)
        editor.putString("reputacion", reputacion)
        editor.putString("puesto", puesto)
        editor.apply() // Aplicamos
        Toast.makeText(this, "Sesion iniciada", Toast.LENGTH_SHORT).show()
        if(puesto == "usuario"){ // Si el puesto del usuario que inició es 'USUARIO' =>
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)

            finish()
        }else { // Si el puesto del usuario que inició es 'CONDUCTOR' =>
            val intent = Intent(this, vistaConductor::class.java)
            startActivity(intent)

            finish()
        }
    }

    fun verificarSesion(){
        // Esta función llama a un webservice y verifica si los datos `email` y `contraseña` son correctos

         var dialog: ProgressDialog
         dialog = ProgressDialog.show(this, "", "Cargando...", true)  // Cartel de cargando

        var url = uri+"sesion/abrirSesion.php?" +
                    "email="+et_email.text.trim()+
                    "&pass="+et_pass.text

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener { response ->
                Log.i("WebserviceSesionLogin","Respuesta:"+response)

                // Validamos si el usuario existe con los datos traidos del método `conexionWebservice` guardado en variable `respuesta`
                if(response.getBoolean("status")){
                    // Ha encontrado un usuario con ese email y contraseña, guardamos los datos adquiridos en variables
                    val idusuario = response.getInt("idusuario")
                    val nombre = response.getString("nombre")
                    val email = response.getString("email")
                    val reputacion = response.getString("reputacion")
                    val puesto = response.getString("puesto")
                    // Abrimos la sesión con los datos adquiridos
                    abrirSesion(idusuario, nombre, email, reputacion, puesto)
                }else{
                    // No se encontró un usuario con ese email y contraseña, enviamos mensaje de error
                    Toast.makeText(this, "Contraseña o email incorrecto", Toast.LENGTH_SHORT).show()
                    et_email.isFocusable
                    dialog.dismiss() // Quitamos el cartel de cargando
                }

            },
            Response.ErrorListener {
                    error -> error.printStackTrace()
                    Toast.makeText(this, "Reintentando", Toast.LENGTH_SHORT).show()
                    verificarSesion() // Si hay error intentamos nuevamente
            })
        queue.add(jsonObjectRequest)

    }
}
