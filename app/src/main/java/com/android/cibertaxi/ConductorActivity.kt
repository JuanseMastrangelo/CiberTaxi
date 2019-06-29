package com.android.cibertaxi

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.tapadoo.alerter.Alerter
import kotlinx.android.synthetic.main.activity_conductor.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import com.google.gson.Gson

class ConductorActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerDragListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    // Google Maps | variables globales
    private lateinit var mMapC: GoogleMap
    private lateinit var marcadorConductor : Marker
    private var camara : Int = 0
    private var conductor_Lat : Double = 0.0
    private var conductor_Lon : Double = 0.0

    lateinit var btn_tomarViaje: Button
    private var cliente_Lat : Double = 0.0
    private var cliente_Lon : Double = 0.0
    private lateinit var marcadorCliente : Marker


    // Volumen de las notificaciones
    var volumenNotificacion = 1



    // WebService | Iniciamos el objeto
    lateinit var queue: RequestQueue

    // Datos del cliente logueado
    var idusuario = 0

    //variable para saber si esta en viaje
    var enViaje = 0

    // GeoLocalizacion | Variables globales
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val INTERVAL: Long = 1000 // Cada cuanto se vuelve a buscar la geolocalizacion del usuario
    private val FASTEST_INTERVAL: Long = 20000 // 1000 1 segundo
    lateinit var mLastLocation: Location
    internal lateinit var mLocationRequest: LocationRequest
    private val REQUEST_PERMISSION_LOCATION = 10
    var geocodeMatches: List<Address>? = null

    // SharedPreferences | Variable global en la app para guardar información ( Se guarda en `Datos de la aplicación` )
    val PREFS_FILENAME = "com.android.cibertaxi.prefs"
    var prefs: SharedPreferences? = null





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Hacemos Fullscreen el layout*/
        setContentView(R.layout.activity_conductor)
        supportActionBar?.hide()

        // SharedPreferences | Inicialización
        prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
        // El usuario logueado es un conductor, guardamos los datos en variables globales
        idusuario = prefs!!.getInt("idusuario", 0) // ID usuario

        // Google Maps | Inicialización
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // SharedPreferences
        prefs = this.getSharedPreferences(PREFS_FILENAME, 0)

        // GeoLocalizacion | Inicialización
        mLocationRequest = LocationRequest()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Verificamos si el usuario dió permisos necesarios `GPS_PROVIDER`
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps() // Pedimos permisos
        }
        if (checkPermissionForLocation(this)) {
            startLocationUpdates() // Localizamos al usuario
        }

        // WebService | Inicialización
        queue = Volley.newRequestQueue(this)


        VerificarConductor() // Verifica si el conductor ya aceptó un viaje


        btn_finViaje.setOnClickListener {  // Boton de finalizar viaje
            finalizarViaje()
        }

        btn_chat.setOnClickListener { // Boton para abrir Chat
            val intent = Intent(this, Chat::class.java)
            startActivity(intent)
        }


        // Verificamos mensajes cada x tiempo
        verificarMensajes()





    }


    override fun onMapReady(googleMap: GoogleMap) {
        // Este algoritmo se inicia una vez que el mapa está listo
        mMapC = googleMap
    }


    fun agregarMarcadorConductor(lat: Double,long: Double){
        // Algoritmo para agregar marcador del conductor

        val tag = LatLng(lat, long)
        mMapC.clear() // Limpiamos el mapa para que no cree infinitos marcadoresConductor
        marcadorConductor = mMapC.addMarker( // Agregamos el marcador
            MarkerOptions()
                .position(tag)
                .title("Tu")
                .icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.taxi))
        )

        if(camara == 0){
            mMapC.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat,long),14.0f))// Enviamos a la camara hacia esa ubicación con un zoom de 14.0
            camara = 1 // Hacemos camara = 1 para que no se mueva y el conductor pueda mover el mapa
        }

        if(enViaje == 0)crearMarcadoresPasajeros() // Creamos los marcadores de los usuarios que estan pidiendo un auto

        marcadorCliente = mMapC.addMarker( // Este marcador es una BANDERA, se coloca cuando tomamos un viaje para saber a donde dirigirnos
            MarkerOptions()
                .position(LatLng(cliente_Lat, cliente_Lon))
                .title("Destino")
                .icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.destination))
        )
    }








    fun agregarMarcador(lat: Double,long: Double, title: String){
        // Algoritmo que crea los marcadores
        val tag = LatLng(lat, long)
        mMapC.addMarker(MarkerOptions().position(tag).title(title).icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.person)))
            // LISTENER DE MAPA (click en infowindows para tomar viaje del usuario)
            mMapC.setOnMarkerDragListener(this)
            mMapC.setInfoWindowAdapter(this)
            mMapC.setOnInfoWindowClickListener(this);

    }

    fun crearMarcadoresPasajeros(){
        // Este algoritmo toma todos los usuarios que están pidiendo viajes y los envia a `agregarMarcador` para ser creados

        var url = "http://eleccionesargentina.online/WebServices/acciones/peticionesViajes.php"
        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener { response ->
                if(response.getBoolean("status")){// Verificamos que existen viajes disponibles
                    val json = response.getJSONArray("viajes_array") // Tomamos la cadena json con los datos
                    var i=0 // inicializamos i en 0
                    tv_viajesDisponibles.setText(json.length().toString() + " disponibles") // Mostramos total los viajes disponibles
                    while (i < json.length()){ // Este algoritmo crea un while con el tamaño del json que traemos de la base de datos con los datos de los usuarios que están pidiendo un viaje
                        var separador = json[i].toString().split("|") // Separamos el json por `|` y creamos un array para manejarlos
                        agregarMarcador(separador[0].toDouble(), separador[1].toDouble(), separador[2]+"|"+separador[3]+"|"+separador[5]) // Pasamos los parametros lat,lon y title; En title pasamos parametros de id,nombre y reputación para tomarlo más facil después con `market.title`
                        i++ // aumentamos i para tomar la proxima cadena
                    }
                }
            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)


    }


    fun VerificarConductor(){
        // Este algoritmo verifica si el conductor aceptó un viaje

        var url =
            "http://eleccionesargentina.online/WebServices/acciones/validarConductor.php?" +
                    "idusuario="+ idusuario

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener {response ->
                // Validamos si el usuario ya pidió un vehiculo
                if(response.getString("mensaje") == "false"){ // El conductor no aceptó ningún viaje
                    ll_top_conductor.visibility = View.GONE // Si no hay viajes, hacemos invisible el LinearLayout
                    btn_chat.visibility = View.GONE
                }
                else{

                    var arrayRespuesta = response.getJSONArray("viajes_array") // Llamamos el JSONArray que respondio la url
                    var valoresMarcador = arrayRespuesta[0].toString().split("|") // Lo separamos para obtener los datos
                    tv_nombreConductor.setText(""+ valoresMarcador[3]) // seteamos el TextView `tv_nombreConductor`
                    tv_reputacionConductor.setText(""+ valoresMarcador[5]) // seteamos el TextView `tv_reputacionConductor`
                    address(valoresMarcador[0].toDouble(), valoresMarcador[1].toDouble()) // Enviamos los datos de Latitud y Longitud a address() para saber la dirección fisica. Este método lo setea en el TextView origen
                    ll_top_conductor.visibility = View.VISIBLE // Hacemos visible el LinearLayout
                    btn_chat.visibility = View.VISIBLE
                    // Creamos la bandera
                    cliente_Lat = valoresMarcador[0].toDouble() // Guardamos los datos de Latitud de forma global
                    cliente_Lon = valoresMarcador[1].toDouble() // Guardamos los datos de Longitud de forma global
                    agregarMarcadorConductor(conductor_Lat, conductor_Lon) // Agregamos el marcador (bandera)

                    Alerter.create(this@ConductorActivity) // Enviamos una Notificación
                        .setTitle("Conductor")
                        .setText("Alerta! Ya tienes un viaje")
                        .enableSwipeToDismiss()
                        .setBackgroundColorRes(R.color.red)
                        .show()


                    mMapC.clear() // Limpiamos el mapa
                    enViaje =1 // Marcamos como un auto en viaje
                    tv_viajesDisponibles.setText("En viaje")

                }
            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)
    }





    fun finalizarViaje(){
        // Este algoritmo finaliza el viaje

        var url =
            "http://eleccionesargentina.online/WebServices/acciones/cancelarVehiculo.php?" +
                    "idconductor="+ idusuario
        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener {response ->
                // Finalizamos viaje
                if(response.getString("mensaje") == "true") {
                    Alerter.create(this@ConductorActivity)
                        .setTitle("Viaje Finalizado")
                        .setText("El viaje fue finalizado con éxito")
                        .enableSwipeToDismiss()
                        .setBackgroundColorRes(R.color.green)
                        .show()

                }
                VerificarConductor() // Escondemos el LinearLayout con datos del viaje
            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)


        mMapC.clear() // Limpiamos el mapa
        enViaje =0 // Marcamos como auto disponible (muestra pasajeros para aceptar viajes)
        tv_viajesDisponibles.setText("Espere..") // seteamos el TextView `tv_viajesDisponibles`

        cliente_Lat = 0.0 // Hacemos 0 el marcador cliente (bandera)
        cliente_Lon = 0.0 // Hacemos 0 el marcador cliente (bandera)

        enviarViajeFinalizado() // Enviamos los datos del viaje finalizado a una base de datos para controlar los viajes

    }

    fun enviarViajeFinalizado(){
        // Este método envia a la base de datos la finalización del viaje para tener un conteo

        var url =
            "http://eleccionesargentina.online/WebServices/acciones/viajeFinalizado.php?" +
                    "idconductor="+ idusuario
        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener {response ->

            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)
    }


















    // *************************************************
    // INFO WINDOWS GOOGLE MAPS MARKER
    // *************************************************

    override fun onMarkerDragStart(marker: Marker) {
        marker.showInfoWindow()
    }
    override fun onMarkerDragEnd(marker: Marker) {
        marker.showInfoWindow()
    }
    override fun onMarkerDrag(marker: Marker) {
        marker.setTitle(marker.getPosition().toString())
        marker.showInfoWindow()
    }
    override fun onInfoWindowClick(marker: Marker){
        if(marker.title.toString() != "Tu"){
            // Si el conductor no clickea su propio icono ( SINO NOS DARÁ ERROR)


            var valoresMarcador = marker.title.toString().split("|")  // Creamos un array con los datos almacenados en el title separados con `|`

            var url = "http://eleccionesargentina.online/WebServices/acciones/tomarViaje.php?" + "idusuario=" + valoresMarcador[0] + "&idconductor="+ idusuario
            val jsonObjectRequest = JsonObjectRequest(url,null,
                Response.Listener { response ->
                    if(response.getBoolean("status") == true){
                        // Si el viaje es tomado con éxito =>
                        mMapC.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(conductor_Lat,conductor_Lon),14.0f))

                        Alerter.create(this@ConductorActivity)
                            .setTitle("Conductor")
                            .setText("El viaje fué tomado con éxito, dirigete al destino marcado con una bandera")
                            .enableSwipeToDismiss()
                            .setBackgroundColorRes(R.color.colorPrimaryDark)
                            .show()


                        mMapC.clear() // Limpiamos el mapa
                        enViaje =1 // Marcamos como un auto en viaje
                        tv_viajesDisponibles.setText("En viaje")


                        // Creamos la bandera
                        cliente_Lat = marker.position.latitude
                        cliente_Lon = marker.position.longitude
                        agregarMarcadorConductor(conductor_Lat, conductor_Lon)




                        // Mostramos los detalles en el navegador de abajo
                        btn_chat.visibility = View.VISIBLE
                        ll_top_conductor.visibility = View.VISIBLE
                        tv_nombreConductor.setText(""+ valoresMarcador[1])
                        tv_reputacionConductor.setText(""+ valoresMarcador[2])
                        address(marker.position.latitude, marker.position.longitude)
                        //****


                        // Creamos la ruta a partir de la api de google
                        var origen = LatLng(conductor_Lat, conductor_Lon)
                        var destino = LatLng(cliente_Lat, cliente_Lon)
                        val URL = getDirectionURL(origen,destino)
                        GetDirection(URL).execute() // Mostramos la ruta en el mapa con un AsyncTask







                    }else{

                        Alerter.create(this@ConductorActivity)
                            .setTitle("Conductor")
                            .setText("Ups! Parece que el usuario canceló el viaje o ya fue tomado por otro conductor")
                            .enableSwipeToDismiss()
                            .setBackgroundColorRes(R.color.red)
                            .show()
                    }
                },
                Response.ErrorListener { error -> error.printStackTrace() })
            queue.add(jsonObjectRequest)
        }
    }


    // funciones que tienen que ser inicializadas si o si
    override fun getInfoWindow(marker: Marker): View? {
        return null
    }
    override fun getInfoContents(marker: Marker): View? {
        return prepareInfoView(marker)
    }
    // Fin funciones




    fun prepareInfoView(marker: Marker): LinearLayout {
        // infoWindows | Este es el cartel que aparece arriba del usuario que pide el viaje, creamos un view y lo mandamos

        var infoView = LinearLayout(this) // Creamos el objeto vista y lo inicializamos

        if(marker.title.toString() == "Tu") {
            var infoViewParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            infoView.setOrientation(LinearLayout.VERTICAL);
            infoView.setLayoutParams(infoViewParams)
            var nombreUsuario = TextView(this)
            nombreUsuario.setText("Tú")
            infoView.addView(nombreUsuario)

        }else{
            // Si el conductor no clickea su propio icono
            var infoViewParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            infoView.setOrientation(LinearLayout.VERTICAL);
            infoView.setLayoutParams(infoViewParams)
            var nombreUsuario = TextView(this)
            var reputacion = TextView(this)
            btn_tomarViaje = Button(this)

            // estilos
            btn_tomarViaje.setText("TOMAR VIAJE")
            btn_tomarViaje.setTextColor(this.getResources().getColor(R.color.white))
            btn_tomarViaje.setBackgroundColor(this.getResources().getColor(R.color.LightBlue))

            var valoresMarcador = marker.title.toString().split("|") // Creamos un array con los datos almacenados en el title separados con `|`
            nombreUsuario.setText("Nombre: " + valoresMarcador[1] + " #" + valoresMarcador[0])
            reputacion.setText("Reputacion: "+valoresMarcador[2] + " estrellas")




            // Agregamos los textview y button a la vista <LineaLayout> ~~infoView
            infoView.addView(nombreUsuario)
            infoView.addView(reputacion)
            infoView.addView(btn_tomarViaje)
            // retornamos la vista en forma de <LinearLayout> ~~infoView

        }
        return infoView // retornamos la vista
    }

    fun verificarMensajes(){
        // Este algoritmo verifica si el conductor tiene mensajes sin leer
        var url =
            "http://eleccionesargentina.online/WebServices/acciones/mensajesSinLeer.php?" +
                    "idconductor="+ idusuario

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener {response ->
                if(response.getBoolean("status") == true) {
                    // Creamos una alerta indicando que tiene mensajes sin leer
                    btn_chat.setText("Chat ("+response.getString("cantidad")+")")
                    Alerter.create(this@ConductorActivity)
                        .setTitle("Mensajes")
                        .setText("Tienes mensajes sin leer: "+response.getString("cantidad"))
                        .enableSwipeToDismiss()
                        .setBackgroundColorRes(R.color.green)
                        .show()

                    // Reproducimos sonido
                    var mp = MediaPlayer.create(this, R.raw.notificacion)
                    mp.setVolume(volumenNotificacion.toFloat(),volumenNotificacion.toFloat())
                    mp.start()

                }else{
                    btn_chat.setText("Chat") // Seteamos el textview en su forma Default
                }
            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)

        var handler = Handler()
        handler.postDelayed( {
            if(enViaje==1){ // Si el usuario aceptó un viaje
                verificarMensajes()
                enviarCoordenadas()
            }
        }, 10000) // Cada 10 segundos

    }

    fun enviarCoordenadas(){
        var url =
            "http://eleccionesargentina.online/WebServices/acciones/enviarCoordenadas.php?" +
                    "idconductor="+ idusuario+
                    "&lat="+ conductor_Lat+
                    "&lon="+ conductor_Lon

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener {response -> },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)
    }





























    fun bitmapDescriptorFromVector(context: Context, vectorRestId:Int): BitmapDescriptor? {
        // Este algoritmo sirve para convertir un drawable a bitmap | Se usa para los iconos del mapa

        var vectorDrawable = ContextCompat.getDrawable(context, vectorRestId)
        vectorDrawable?.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight())
        var bitmap = Bitmap.createBitmap(vectorDrawable!!.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

















































    /* ****************************************************************************************
                        GEOLOCALIZACION (NO TOCAR!!!!)
    **************************************************************************************** */

    fun address(lat: Double, long: Double){
        // Convierte latitud y longitud en una dirección física

        try {
            geocodeMatches = Geocoder(this).getFromLocation(lat, long, 1)

        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (geocodeMatches != null) // Si la direccion existe =>
            tv_origenConductor.setText(geocodeMatches!![0].getAddressLine(0))

    }


    private fun buildAlertMessageNoGps() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Tu GPS esta deshabilitado, quieres habilitarlo?")
            .setCancelable(false)
            .setPositiveButton("Si") { dialog, id ->
                startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    , 11)
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.cancel()
                finish()
            }
        val alert: AlertDialog = builder.create()
        alert.show()

    }


    protected fun startLocationUpdates() {
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest!!.setInterval(INTERVAL)
        mLocationRequest!!.setFastestInterval(FASTEST_INTERVAL)
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        val locationSettingsRequest = builder.build()
        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback,
            Looper.myLooper())
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    fun onLocationChanged(location: Location) {
        mLastLocation = location
        agregarMarcadorConductor(mLastLocation.latitude, mLastLocation.longitude)
        conductor_Lat = mLastLocation.latitude
        conductor_Lon = mLastLocation.longitude
    }

    private fun stoplocationUpdates() {
        mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Vemos si el usuario dió los permisos necesarios (GPS LOCATION)
    fun checkPermissionForLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION)
                false
            }
        } else {
            true
        }
    }













































    //****************************************************************************
    //                          CREAR RUTA ENTRE 2 PUNTOS
    //****************************************************************************


    fun getDirectionURL(origin:LatLng,dest:LatLng) : String{
        // Esta función llama a la url de google para saber que ruta trazar en el mapa
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=AIzaSyATswYLlviN5LWh1a7dC68BfOVD_BAXHZU"
    }
    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    private inner class GetDirection(val url : String) : AsyncTask<Void,Void,List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()!!.string()
            Log.d("GoogleMap" , " data : $data")
            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data,GoogleMapDTO::class.java)

                val path =  ArrayList<LatLng>()

                for (i in 0..(respObj.routes[0].legs[0].steps.size-1)){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            mMapC.addPolyline(lineoption)
        }
    }

    fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }


}
