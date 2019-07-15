package com.aplicacion.cibertaxi.historialAdapter



import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.cibertaxi.R
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.aplicacion.cibertaxi.Chat
import com.aplicacion.cibertaxi.MapsActivity
import com.tapadoo.alerter.Alerter
import java.io.IOException


class Adaptador(private var items: ArrayList<Historial>, private var context: Context): RecyclerView.Adapter<Adaptador.ViewHolder>() {

    var geocodeMatches: List<Address>? = null
    lateinit var origen: String
    lateinit var destino: String


    val uri = "http://eleccionesargentina.online/WebServices/"

    // WebService | Iniciamos el objeto
    lateinit var queue: RequestQueue

    // SharedPreferences | Variable global en la app para guardar información ( Se guarda en `Datos de la aplicación` )
    val PREFS_FILENAME = "com.aplicacion.cibertaxi.prefs"
    var prefs: SharedPreferences? = null
    var idusuario = 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var userDto = items[position]


        // Insertamos los datos en el LinearLayout
        holder?.tv_historial_fecha?.text = userDto.fecha

        var separador_origen = userDto.origen.split(" ")
        address(separador_origen[0].toDouble(), separador_origen[1].toDouble(), 0)

        var separador_destino = userDto.destino.split(" ")
        address(separador_destino[0].toDouble(), separador_destino[1].toDouble(), 1)


        holder?.tv_historial_estado?.text = userDto.estado
        holder?.tv_historial_direccion?.text = origen +" hasta "+ destino


        if(userDto.tipo == "usuario")
        {
            holder?.tv_historial_usuario?.text = "id Conductor: "+ userDto.conductor_usuario
            holder?.btn_nuevoViajeOrigen?.setText("Llamar auto a "+ origen)
            holder?.btn_nuevoViajeDestino?.setText("Llamar auto a "+ destino)


            holder?.btn_nuevoViajeOrigen?.setOnClickListener {
                pedirVehiculoHistorial(separador_origen[0].toDouble(), separador_origen[1].toDouble())
            }
            holder?.btn_nuevoViajeDestino?.setOnClickListener {
                pedirVehiculoHistorial(separador_destino[0].toDouble(), separador_destino[1].toDouble())
            }


        }else
        {
            holder?.tv_historial_usuario?.text = "id Usuario: "+ userDto.conductor_usuario

            holder?.btn_nuevoViajeOrigen?.visibility = View.GONE
            holder?.btn_nuevoViajeDestino?.visibility = View.GONE
        }


        // SharedPreferences | Inicialización
        prefs = context.getSharedPreferences(PREFS_FILENAME, 0)
        idusuario = prefs!!.getInt("idusuario", 0)

        // WebService | Inicialización
        queue = Volley.newRequestQueue(context)
    }


    fun pedirVehiculoHistorial(lat: Double, lon:Double)
    {// Funcion para pedir vehiculo desde Historial

        var url = uri+"acciones/crearViaje.php?" +
                "idusuario=" + idusuario +
                "&lat="+lat+
                "&lon="+lon

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener {response ->

                Toast.makeText(context, "Se pidió un vehículo", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)
    }


    fun address(lat: Double, long: Double, tipo: Int){
        // Convierte latitud y longitud en una dirección física

        try {
            geocodeMatches = Geocoder(context).getFromLocation(lat, long, 1)
        } catch (e: IOException) {e.printStackTrace()}
        if (geocodeMatches != null) // Si la direccion existe =>
            if(tipo == 0)
                origen = geocodeMatches!![0].getAddressLine(0)
            else
                destino = geocodeMatches!![0].getAddressLine(0)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent?.context)
            .inflate(R.layout.vista_historial, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int { return items.size}


    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        var tv_historial_fecha: TextView? = null
        var tv_historial_direccion: TextView? = null
        var tv_historial_usuario: TextView? = null
        var tv_historial_estado: TextView? = null


        var btn_nuevoViajeOrigen: Button? = null
        var btn_nuevoViajeDestino: Button? = null

        init {
            this.tv_historial_fecha = row?.findViewById(R.id.tv_historial_fecha)
            this.tv_historial_direccion = row?.findViewById(R.id.tv_historial_direccion)
            this.tv_historial_usuario = row?.findViewById(R.id.tv_historial_usuario)
            this.tv_historial_estado = row?.findViewById(R.id.tv_historial_estado)

            this.btn_nuevoViajeOrigen = row?.findViewById(R.id.btn_nuevoViajeOrigen)
            this.btn_nuevoViajeDestino = row?.findViewById(R.id.btn_nuevoViajeDestino)


        }
    }
}