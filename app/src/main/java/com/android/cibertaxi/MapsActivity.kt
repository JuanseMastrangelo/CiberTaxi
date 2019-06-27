package com.android.cibertaxi

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*


import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.tapadoo.alerter.Alerter
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // Google Maps | variables globales
    private lateinit var mMap: GoogleMap
    private lateinit var marcadorUsuario : Marker
    private var latitudUsuario: Double = 0.0
    private var longitudUsuario: Double = 0.0

    // WebService | Iniciamos el objeto
    lateinit var queue: RequestQueue

    // SharedPreferences | Variable global en la app para guardar información ( Se guarda en `Datos de la aplicación` )
    val PREFS_FILENAME = "com.android.cibertaxi.prefs"
    var prefs: SharedPreferences? = null


    // Datos del cliente logueado
    var idusuario = 0

    // GeoLocalizacion | Variables globales
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val INTERVAL: Long = 2000 // Cada cuanto se vuelve a buscar la geolocalizacion del usuario
    private val FASTEST_INTERVAL: Long = 1000 // 1 segundo
    lateinit var mLastLocation: Location
    internal lateinit var mLocationRequest: LocationRequest
    private val REQUEST_PERMISSION_LOCATION = 10
    var geocodeMatches: List<Address>? = null





    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Hacemos Fullscreen el layout*/
        setContentView(R.layout.activity_maps)
        supportActionBar?.hide()

        // SharedPreferences | Inicialización
        prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
        // Verificamos si existe una sesión iniciada, si no existe nos envia a iniciar_sesion.class
        if(prefs!!.getInt("iniciado", 0) == 0){
            // Si la sesión no esta iniciada
            val intent = Intent(this, iniciar_sesion::class.java)
            startActivity(intent)
        }else if(prefs!!.getString("puesto", "usuario") != "usuario"){
            // La sesion esta iniciada pero es de un conductor
            val intent = Intent(this, ConductorActivity::class.java)
            startActivity(intent)
        }else{
            // El usuario logueado es un cliente, guardamos los datos en variables globales
            idusuario = prefs!!.getInt("idusuario", 0) // ID usuario
        }


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
            // Boton pedir viaje
            var url =
                "http://eleccionesargentina.online/WebServices/acciones/crearViaje.php?" +
                        "idusuario=" + idusuario +
                        "&lat="+latitudUsuario+
                        "&lon="+longitudUsuario

            val jsonObjectRequest = JsonObjectRequest(url,null,
                Response.Listener {response ->
                    Log.i("Webservice","Respuesta:"+response)

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



        validarPeticionVehiculo()  // Validamos si el usuario ya pidió un vehiculo para mostrar cartel de cancelar

    }



    override fun onMapReady(googleMap: GoogleMap) {
        // Este algoritmo se inicia una vez que el mapa está listo
        mMap = googleMap
    }




    fun agregarMarcadorUsuario(lat: Double,long: Double){
        // Algoritmo para agregar marcador del usuario

        val tag = LatLng(lat, long)
        mMap.clear()
        marcadorUsuario = mMap.addMarker(MarkerOptions().position(tag).title("Tu ubicación")) // Agregamos el marcador
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat,long),15.0f))  // Enviamos a la camara hacia esa ubicación con un zoom de 15.0
        address(lat,long) // Sabemos la dirección física del usuario usando la latitud y longitud
        stoplocationUpdates()// Frenamos la auto-localizacion
    }



    fun validarPeticionVehiculo(){
        // Este algoritmo verifica si el usuario pidió un vehículo

        var url =
            "http://eleccionesargentina.online/WebServices/acciones/validarPedidos.php?" +
                    "idusuario="+ idusuario

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener {response ->
                Log.i("validarPeticionVehiculo","Respuesta:"+response)
                // Validamos si el usuario ya pidió un vehiculo
                if(response.getString("mensaje") == "false"){
                    btn_cancelar.visibility = View.GONE
                    btn_pedirRemisse.visibility = View.VISIBLE
                    btn_relocalizacion.visibility = View.VISIBLE
                }
                else{
                    btn_cancelar.visibility = View.VISIBLE
                    btn_pedirRemisse.visibility = View.GONE
                    btn_relocalizacion.visibility = View.GONE
                }
            },
            Response.ErrorListener { error -> error.printStackTrace() })
            queue.add(jsonObjectRequest)
    }
    fun cancelarVehiculo(){
        // Algoritmo para cancelar vehículo

        var url =
            "http://eleccionesargentina.online/WebServices/acciones/cancelarVehiculo.php?" +
                    "idusuario="+ idusuario

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener {response ->
                Log.i("cancelarVehiculo","Respuesta:"+response)
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
            et_ubicacion.setText(geocodeMatches!![0].getAddressLine(0))

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
