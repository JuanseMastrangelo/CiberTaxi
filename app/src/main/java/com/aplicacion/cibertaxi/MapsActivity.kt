package com.aplicacion.cibertaxi

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
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
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.cibertaxi.R

import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*


import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import com.squareup.picasso.Picasso
import com.tapadoo.alerter.Alerter
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.activity_maps.btn_chat
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    val uri = "http://eleccionesargentina.online/WebServices/"

    // Google Maps | variables globales
    private lateinit var mMap: GoogleMap
    private lateinit var marcadorUsuario : Marker
    private lateinit var conductorMarker : Marker
    private var latitudUsuario: Double = 0.0
    private var longitudUsuario: Double = 0.0

    // WebService | Iniciamos el objeto
    lateinit var queue: RequestQueue

    // SharedPreferences | Variable global en la app para guardar información ( Se guarda en `Datos de la aplicación` )
    val PREFS_FILENAME = "com.aplicacion.cibertaxi.prefs"
    var prefs: SharedPreferences? = null


    // Datos del cliente logueado
    var idusuario = 0
    var usuarioPidioAuto = 0


    // Volumen de las notificaciones
    var volumenNotificacion = 1


    // GeoLocalizacion | Variables globales
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val INTERVAL: Long = 2000 // Cada cuanto se vuelve a buscar la geolocalizacion del usuario
    private val FASTEST_INTERVAL: Long = 1000 // 1 segundo
    lateinit var mLastLocation: Location
    internal lateinit var mLocationRequest: LocationRequest
    private val REQUEST_PERMISSION_LOCATION = 10
    var geocodeMatches: List<Address>? = null


    // Extras
    var marcadorConductorExistencia = 0

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Hacemos Fullscreen el layout*/
        setContentView(R.layout.activity_maps)
        supportActionBar?.hide()

        // SharedPreferences | Inicialización
        prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
        // Verificamos si existe una sesión iniciada, si no existe nos envia a iniciar_sesion.class
        idusuario = prefs!!.getInt("idusuario", 0)


        // Google Maps | Inicialización
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


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


        // Acciones de botones
        btn_cancelar.setOnClickListener {
            // Boton cancelar viaje
            cancelarVehiculo()
        }

        btn_relocalizacion.setOnClickListener {
            // Boton re-localizar al usuario (actualiza su posición)
            startLocationUpdates()
            Alerter.create(this@MapsActivity)
                .setTitle("Localización")
                .setText("Localización actualizada")
                .enableSwipeToDismiss()
                .setBackgroundColorRes(R.color.colorPrimary)
                .show()
        }

        btn_pedirRemisse.setOnClickListener {
            pedirVehiculo(latitudUsuario, longitudUsuario) // Boton pedir viaje
        }
        btn_chat.setOnClickListener { // Boton para abrir Chat
            val intent = Intent(this, Chat::class.java)
            startActivity(intent)
        }

        btn_usuario_u.setOnClickListener {
            configuracion()
        }



        validarPeticionVehiculo()  // Validamos si el usuario ya pidió un vehiculo para mostrar cartel de cancelar


        abrirPublicidad()

    }



    fun pedirVehiculo(lat: Double, lon:Double)
    {// Funcion para pedir vehiculo

        var url = uri+"acciones/crearViaje.php?" +
                "idusuario=" + idusuario +
                "&lat="+lat+
                "&lon="+lon

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener {response ->

                // Creamos un viaje
                Alerter.create(this@MapsActivity)
                    .setTitle("Auto")
                    .setText(response.getString("mensaje"))
                    .enableSwipeToDismiss()
                    .setBackgroundColorRes(R.color.colorPrimary)
                    .show()
                validarPeticionVehiculo()
            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)
    }



    override fun onMapReady(googleMap: GoogleMap) {
        // Este algoritmo se inicia una vez que el mapa está listo
        mMap = googleMap
    }


    fun configuracion(){
        val alert = AlertView("Configuración de usuario", "Seleccione una opción:", AlertStyle.DIALOG)
        alert.addAction(AlertAction("Cuenta", AlertActionStyle.DEFAULT, { action ->
            val intent = Intent(this, editarUsuario::class.java)
            startActivity(intent)
        }))
        alert.addAction(AlertAction("Historial", AlertActionStyle.DEFAULT, { action ->
            val intent = Intent(this, Historial::class.java)
            startActivity(intent)
        }))
        alert.addAction(AlertAction("Crear rutina", AlertActionStyle.DEFAULT, { action ->
            Toast.makeText(this, "Rutinas", Toast.LENGTH_SHORT).show()
        }))
        alert.addAction(AlertAction("Cerrar Sesión", AlertActionStyle.NEGATIVE, { action ->
            val editor = prefs!!.edit()
            editor.remove("iniciado")
            editor.apply() // Aplicamos
            val intent = Intent(this, iniciar_sesion::class.java)
            startActivity(intent)
            finish()
        }))
        alert.addAction(AlertAction("Salir", AlertActionStyle.DEFAULT, { action -> }))
        alert.show(this)
    }




    fun agregarMarcadorUsuario(lat: Double,long: Double){
        // Algoritmo para agregar marcador del usuario

        val tag = LatLng(lat, long)
        mMap.clear()
        marcadorUsuario = mMap.addMarker(MarkerOptions().position(tag).title("Tu ubicación")) // Agregamos el marcador
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat,long),15.0f))  // Enviamos a la camara hacia esa ubicación con un zoom de 15.0
        et_ubicacion.setText(address(lat,long))
        stoplocationUpdates()// Frenamos la auto-localizacion
    }



    fun validarPeticionVehiculo(){
        // Este algoritmo verifica si el usuario pidió un vehículo

        var url = uri+"acciones/validarPedidos.php?" + "idusuario="+ idusuario
        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener {response ->
                // Validamos si el usuario ya pidió un vehiculo
                if(response.getBoolean("status") == false){
                    usuarioPidioAuto = 0
                    // Si el usuario no pidió un auto todavía
                    btn_cancelar.visibility = View.GONE
                    btn_pedirRemisse.visibility = View.VISIBLE
                    btn_relocalizacion.visibility = View.VISIBLE
                    btn_chat.visibility = View.GONE
                    tv_label_activityMaps.setText("El vehículo se enviará a:")
                    tv_label_activityMaps.setTextColor(Color.parseColor("#313131"))
                }
                else{
                    // El usuario pidio un auto ¿Ya tiene un conductor?
                    usuarioPidioAuto = 1
                    btn_pedirRemisse.visibility = View.GONE
                    btn_relocalizacion.visibility = View.GONE
                    tv_label_activityMaps.setText("El vehículo se enviará a:")
                    tv_label_activityMaps.setTextColor(Color.parseColor("#313131"))

                    et_ubicacion.setText(address(response.getString("latitud").toDouble(), response.getString("longitud").toDouble()))


                    if(response.getString("mensaje") == "0"){
                        // Si no tiene conductor
                        btn_cancelar.visibility = View.VISIBLE
                        btn_chat.visibility = View.GONE
                        et_ubicacion.setText(address(latitudUsuario, longitudUsuario))

                    }else{
                        // Si tiene conductor
                        tv_label_activityMaps.setText("Confirmado! Un automovil está yendo a esta ubicación, siguelo en el mapa: ")
                        tv_label_activityMaps.setTextColor(Color.parseColor("#8BC34A"))
                        btn_cancelar.visibility = View.GONE
                        btn_chat.visibility = View.VISIBLE
                        verificarMensajes() // verificamos si tenemos mensajes del conductor
                        localizacionDeConductor(response.getString("mensaje")) // mostramos por donde anda el conductor (Pasamos el id del conductor como parametro)
                    }
                }

                if(usuarioPidioAuto == 1){ // El usuario pidió un vehículo
                    // Verificamos que si hay el conductor aceptó su viaje
                    var handler = Handler()
                    handler.postDelayed( {
                        validarPeticionVehiculo()
                    }, 5000) // Cada 5 segundos actualizamos
                }
            },
            Response.ErrorListener { error -> error.printStackTrace() })
            queue.add(jsonObjectRequest)
    }
    fun cancelarVehiculo(){
        // Algoritmo para cancelar vehículo

        var url = uri+"acciones/cancelarVehiculo.php?" +
                    "idusuario="+ idusuario

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener {response ->
                // Validamos si el usuario ya pidió un remisse, hacemos visible el boton de cancelar
                if(response.getString("mensaje") == "true") {
                    Alerter.create(this@MapsActivity)
                        .setTitle("Auto cancelado")
                        .setText("El vehiculo pedido fué cancelado")
                        .enableSwipeToDismiss()
                        .setBackgroundColorRes(R.color.red)
                        .show()

                }
                validarPeticionVehiculo()
            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)

    }









    fun verificarMensajes() {
        // Este algoritmo verifica si el conductor tiene mensajes sin leer
        var url = uri+"acciones/mensajesSinLeer.php?" +
                    "idconductor=" + idusuario

        val jsonObjectRequest = JsonObjectRequest(url, null,
            Response.Listener { response ->
                if (response.getBoolean("status") == true) {
                    // Creamos una alerta indicando que tiene mensajes sin leer
                    btn_chat.setText("Chat (" + response.getString("cantidad") + ")")
                    Alerter.create(this@MapsActivity)
                        .setTitle("Mensajes")
                        .setText("Tienes mensajes sin leer: " + response.getString("cantidad"))
                        .enableSwipeToDismiss()
                        .setBackgroundColorRes(R.color.green)
                        .show()

                    // Reproducimos sonido
                    var mp = MediaPlayer.create(this, R.raw.notificacion)
                    mp.setVolume(volumenNotificacion.toFloat(), volumenNotificacion.toFloat())
                    mp.start()

                } else {
                    btn_chat.setText("Chat") // Seteamos el textview en su forma Default
                }
            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)
    }




    fun localizacionDeConductor(idConductor: String){
        // Este algoritmo muestra al usuario por donde anda el conductor (Se actualiza cada 10 segundos)


        var url = uri+"acciones/recibirCoordenadas.php?" +
                    "idconductor=" + idConductor

        val jsonObjectRequest = JsonObjectRequest(url, null,
            Response.Listener { response ->
                if (response.getBoolean("status") == true) {
                    // Se reciben coordenas del conductor
                    var latitudConductor = response.getString("lat").toDouble()
                    var longitudConductor = response.getString("lon").toDouble()


                    // Creamos el marcador y lo actualizamos
                    if(marcadorConductorExistencia != 0){ // Si no existe un marcador nos dará un error
                        conductorMarker.remove() // Limpiamos el viejo y creamos uno nuevo
                    }else{
                        marcadorConductorExistencia = 1 // Hacemos 1 a la variable para que se ejecute 1 vez
                    }
                    conductorMarker = mMap.addMarker(MarkerOptions().position(LatLng(latitudConductor, longitudConductor)).title("Conductor").icon(bitmapDescriptorFromVector(getApplicationContext(),
                        R.drawable.taxi
                    ))) // Agregamos el marcador

                } else {
                    // Error, el conductor no esta enviando coordenadas
                    Toast.makeText(this, "Error al recibir coordenadas del conductor", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)
    }


    fun abrirPublicidad(){
        // Crea un popup con publicidad
        var dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog_publicidad)

        var btn_dialog_cerrar = dialog.findViewById<Button>(R.id.btn_dialog_cerrar)
        var iv_dialog = dialog.findViewById<ImageView>(R.id.iv_dialog)
        btn_dialog_cerrar.setOnClickListener {
            dialog.dismiss()
        }



        var url = uri+"acciones/recibirPublicidad.php"

        val jsonObjectRequest = JsonObjectRequest(url, null,
            Response.Listener { response ->
                Picasso
                    .with(this)
                    .load(response.getString("url"))
                    .into(iv_dialog)
                btn_dialog_cerrar.visibility = View.VISIBLE
            },
            Response.ErrorListener {
                    error -> error.printStackTrace()
                    dialog.dismiss()
            })
        queue.add(jsonObjectRequest)



        dialog.show()


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

    fun address(lat: Double, long: Double): String{
        // Convierte latitud y longitud en una dirección física
        var respuesta = ""

        try {
            geocodeMatches = Geocoder(this).getFromLocation(lat, long, 1)

        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (geocodeMatches != null) // Si la direccion existe =>
            respuesta = geocodeMatches!![0].getAddressLine(0)


        return respuesta
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
        agregarMarcadorUsuario(mLastLocation.latitude, mLastLocation.longitude)
        // Hacemos global la lat y long del usuario
        latitudUsuario = mLastLocation.latitude
        longitudUsuario = mLastLocation.longitude
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


}
