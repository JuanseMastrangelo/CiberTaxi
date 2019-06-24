package com.android.cibertaxi

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_iniciar_sesion.*

class registro_sesion : AppCompatActivity() {

    // SharedPreferences
    val PREFS_FILENAME = "com.android.cibertaxi.prefs"
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


        btn_accion.setOnClickListener {
            verificarSesion()
        }

    }

    fun abrirSesion(idusuario: Int, nombre:String, email:String, reputacion:String){
        val editor = prefs!!.edit()
        editor.putInt("iniciado", 1) // (tag, valorDefault)
        editor.putInt("idusuario", idusuario)
        editor.putString("nombre", nombre)
        editor.putString("email", email)
        editor.putString("reputacion", reputacion)
        editor.apply() // Aplicamos
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    fun verificarSesion(){
        var dialog: ProgressDialog
        dialog = ProgressDialog.show(this, "",
            "Cargando...", true);

        var url =
            "http://eleccionesargentina.online/WebServices/sesion/registrarSesion.php?" +
                    "email="+et_email.text.trim()+
                    "&pass="+et_pass.text+
                    "&nombre="+et_pass.text

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener { response ->
                Log.i("WebserviceSesionLogin","Respuesta:"+response)

                // Validamos si el usuario existe
                if(response.getBoolean("status")){
                    val idusuario = response.getInt("idusuario")
                    val nombre = response.getString("nombre")
                    val email = response.getString("email")
                    val reputacion = response.getString("reputacion")
                    abrirSesion(idusuario, nombre, email, reputacion)
                    dialog.setMessage("Registrado con éxito")
                }else{
                    Toast.makeText(this, "El usuario ya existe", Toast.LENGTH_SHORT).show()
                    et_email.isFocusable
                    dialog.dismiss()
                }

            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)

    }
}
