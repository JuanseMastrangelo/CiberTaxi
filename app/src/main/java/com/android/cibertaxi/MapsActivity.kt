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
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_maps.*
import ru.whalemare.sheetmenu.SheetMenu
import java.io.IOException
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // Google Maps
    private lateinit var mMap: GoogleMap
    private lateinit var marcadorUsuario : Marker
    private var latitudUsuario: Double = 0.0
    private var longitudUsuario: Double = 0.0

    private lateinit var btn_geolocalizacion: ImageButton
    private lateinit var et_ubicacion: EditText
    private lateinit var btn_pedirRemisse: Button
    private lateinit var btn_cancelar: Button

    // Webservice
    lateinit var queue: RequestQueue

    // GeoLocalizacion
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val INTERVAL: Long = 2000
    private val FASTEST_INTERVAL: Long = 1000
    lateinit var mLastLocation: Location
    internal lateinit var mLocationRequest: LocationRequest
    private val REQUEST_PERMISSION_LOCATION = 10
    var geocodeMatches: List<Address>? = null

    // SharedPreferences
    val PREFS_FILENAME = "com.android.cibertaxi.prefs"
    var prefs: SharedPreferences? = null

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*Fullscreen*/
        //getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_maps)
        supportActionBar?.hide()


        // SharedPreferences <Nos fijamos si la sesión esta iniciada, sino la reenviamos a iniciar_sesion.kt>
        prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
        if(prefs!!.getInt("iniciado", 0) == 0){ // Si la sesión no esta iniciada
            val intent = Intent(this, iniciar_sesion::class.java)
            startActivity(intent)
        }
        // Google Maps
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // GeoLocalizacion
        mLocationRequest = LocationRequest()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }
        if (checkPermissionForLocation(this)) {
            startLocationUpdates()
        }

        // Webservice
        queue = Volley.newRequestQueue(this)



        // OnClick Buttons
        btn_geolocalizacion = findViewById<ImageButton>(R.id.btn_geolocalizacion)
        et_ubicacion = findViewById(R.id.et_ubicacion)
        btn_pedirRemisse = findViewById<Button>(R.id.btn_pedirRemisse)
        btn_cancelar = findViewById<Button>(R.id.btn_cancelar)

        btn_geolocalizacion.setOnClickListener {
            startLocationUpdates()
        }
        btn_cancelar.setOnClickListener {
            btn_cancelar.visibility = View.GONE
            cancelarVehiculo()
        }

        btn_pedirRemisse.setOnClickListener {

            // Peticiones Webservices
            var url =
                "http://eleccionesargentina.online/WebServices/acciones/crearViaje.php?" +
                        "idusuario=1" +
                        "&lat="+latitudUsuario+
                        "&lon="+longitudUsuario

            val jsonObjectRequest = JsonObjectRequest(url,null,
                Response.Listener {response ->
                    Log.i("Webservice","Respuesta:"+response)

                    // Validamos si el usuario ya pidió un remisse
                    Snackbar.make(
                        mainActivity,
                        response.getString("mensaje")+"",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    validarPeticionVehiculo()
                },
                Response.ErrorListener { error -> error.printStackTrace() })
            queue.add(jsonObjectRequest)
        }



        // Validamos si el usuario ya pidió un vehiculo
        validarPeticionVehiculo()

    }
    fun validarPeticionVehiculo(){
        var url =
            "http://eleccionesargentina.online/WebServices/acciones/validarPedidos.php?" +
                    "idusuario=1"

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener {response ->
                Log.i("Webservice","Respuesta:"+response)
                // Validamos si el usuario ya pidió un remisse, hacemos visible el boton de cancelar
                if(response.getString("mensaje") == "true")
                    btn_cancelar.visibility = View.VISIBLE
                else
                    btn_cancelar.visibility = View.GONE
            },
            Response.ErrorListener { error -> error.printStackTrace() })
            queue.add(jsonObjectRequest)
    }
    fun cancelarVehiculo(){
        var url =
            "http://eleccionesargentina.online/WebServices/acciones/cancelarVehiculo.php?" +
                    "idusuario=1"

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener {response ->
                Log.i("Webservice","Respuesta:"+response)
                // Validamos si el usuario ya pidió un remisse, hacemos visible el boton de cancelar
                if(response.getString("mensaje") == "true")
                    Snackbar.make(
                        mainActivity,
                        "Vehiculo cancelado",
                        Snackbar.LENGTH_SHORT
                    ).show()

                validarPeticionVehiculo()
            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    fun agregarMarcadorUsuario(lat: Double,long: Double){
        val tag = LatLng(lat, long)
        mMap.clear()
        marcadorUsuario = mMap.addMarker(MarkerOptions().position(tag).title("Tu ubicación"))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat,long),17.0f))
        address(lat,long)
        // Frenamos la auto-localizacion (por ahora)
        stoplocationUpdates()
    }






    /* ****************************************************************************************
                        GEOLOCALIZACION (NO TOCAR!!!!)
    **************************************************************************************** */

    fun address(lat: Double, long: Double){
        try {
            geocodeMatches = Geocoder(this).getFromLocation(lat, long, 1)

        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (geocodeMatches != null)
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
