package com.aplicacion.cibertaxi

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.cibertaxi.R
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.tapadoo.alerter.Alerter

class editarUsuario : AppCompatActivity() {

    val uri = "http://eleccionesargentina.online/WebServices/"

    // WebService | Iniciamos el objeto
    lateinit var queue: RequestQueue

    // Datos del cliente logueado
    var idusuario = 0

    // Componentes
    lateinit var et_nombre_eu: EditText
    lateinit var et_apellido_eu: EditText
    lateinit var et_telefono_eu: EditText

    // SharedPreferences | Variable global en la app para guardar información ( Se guarda en `Datos de la aplicación` )
    val PREFS_FILENAME = "com.aplicacion.cibertaxi.prefs"
    var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_usuario)
        supportActionBar?.hide()

        // SharedPreferences | Inicialización
        prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
        // El usuario logueado es un conductor, guardamos los datos en variables globales
        idusuario = prefs!!.getInt("idusuario", 0) // ID usuario
        prefs = this.getSharedPreferences(PREFS_FILENAME, 0)

        // WebService | Inicialización
        queue = Volley.newRequestQueue(this)

        et_nombre_eu = findViewById(R.id.et_nombre_eu)
        et_apellido_eu = findViewById(R.id.et_apellido_eu)
        et_telefono_eu = findViewById(R.id.et_telefono_eu)


        // Botones
        var btn_cancelar_eu = findViewById<Button>(R.id.btn_cancelar_eu)
        btn_cancelar_eu.setOnClickListener {
            onBackPressed() // Volvemos a la actividad anterior
        }

        var btn_guardar_eu = findViewById<Button>(R.id.btn_guardar_eu)
        btn_guardar_eu.setOnClickListener {
            guardarCambios()
        }


        cargarDatos()
    }

    fun cargarDatos() {
        // Este método carga los datos del usuario

        var url = uri+"sesion/cargarDatos.php?" +
                    "id="+ idusuario
            val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener { response ->
                if(response.getBoolean("status") == true){
                    et_nombre_eu.setText(prefs!!.getString("nombre", ""))
                    et_apellido_eu.setText(response.getString("apellido"))
                    et_telefono_eu.setText(response.getString("telefono"))
                }else {
                    Toast.makeText(this, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)
    }

    fun guardarCambios() {
        // Este método guarda los cambios

        var url = uri+"sesion/actualizarDatos.php?" +
                    "id="+ idusuario +
                    "&apellido="+et_apellido_eu.text+
                    "&telefono="+et_telefono_eu.text

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener { response ->

                Alerter.create(this@editarUsuario)
                    .setTitle("Datos")
                    .setText("Tu información se actualizó con éxito")
                    .enableSwipeToDismiss()
                    .setBackgroundColorRes(R.color.green)
                    .show()

                onBackPressed() // Volvemos atrás
            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)
    }
}
