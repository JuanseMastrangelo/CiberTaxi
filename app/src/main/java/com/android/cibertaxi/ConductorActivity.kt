package com.android.cibertaxi

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.cibertaxi.Adapters.infoWindowsAdapter
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException

class ConductorActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerDragListener, GoogleMap.InfoWindowAdapter {

    // Google Maps
    private lateinit var mMap: GoogleMap
    private lateinit var marcadorConductor : Marker
    private var camara : Int = 0


    // Webservice
    lateinit var queue: RequestQueue

    // GeoLocalizacion
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val INTERVAL: Long = 1000
    private val FASTEST_INTERVAL: Long = 60000
    lateinit var mLastLocation: Location
    internal lateinit var mLocationRequest: LocationRequest
    private val REQUEST_PERMISSION_LOCATION = 10
    var geocodeMatches: List<Address>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*Fullscreen*/
        //getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_conductor)
        supportActionBar?.hide()

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



    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }


    fun agregarMarcadorConductor(lat: Double,long: Double){
        val tag = LatLng(lat, long)
        mMap.clear()
        marcadorConductor = mMap.addMarker(
            MarkerOptions()
                .position(tag)
                .title("Tu ubicación")
                .icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.taxi))
        )

        if(camara == 0){
            // Movemos la camara hacia la posicion del conductor, solo una vez
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat,long),16.0f))
            camara = 1
        }
        crearMarcadoresPasajeros()
    }
    fun bitmapDescriptorFromVector(context: Context, vectorRestId:Int): BitmapDescriptor? {
        var vectorDrawable = ContextCompat.getDrawable(context, vectorRestId)
        vectorDrawable?.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight())
        var bitmap = Bitmap.createBitmap(vectorDrawable!!.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    fun agregarMarcador(lat: Double,long: Double){
        val tag = LatLng(lat, long)
        mMap.addMarker(MarkerOptions().position(tag).title("Tu ubicación").icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.person)))
        mMap.setOnMarkerDragListener(this);
        mMap.setInfoWindowAdapter(this);
        mMap.setOnInfoWindowClickListener(MyOnInfoWindowClickListener);

    }

    fun crearMarcadoresPasajeros(){
        var url =
            "http://eleccionesargentina.online/WebServices/acciones/peticionesViajes.php"

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener { response ->
                val json = response.getJSONArray("viajes_array")
                Log.i("WebserviceConductor","Respuesta:"+json[0])

                var i=0
                while (i < json.length()){
                    var separador = json[i].toString().split("|")
                    agregarMarcador(separador[0].toDouble(), separador[1].toDouble())
                    i++
                }

            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)


    }



    /* ****************************************************************************************
                        GEOLOCALIZACION (NO TOCAR!!!!)
    **************************************************************************************** */

    fun address(lat: Double, long: Double): List<Address>? {
        try {
            geocodeMatches = Geocoder(this).getFromLocation(lat, long, 1)

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return geocodeMatches
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




    // *************************************************
    // INFO WINDOWS GOOGLE MAPS MARKER
    // *************************************************

    override fun onMarkerDragStart(marker: Marker) {
        marker.setTitle(marker.getPosition().toString())
        marker.showInfoWindow()
        marker.setAlpha(0.5f)
    }
    override fun onMarkerDragEnd(marker: Marker) {
        marker.setTitle(marker.getPosition().toString())
        marker.showInfoWindow()
        marker.setAlpha(0.5f)
    }
    override fun onMarkerDrag(marker: Marker) {
        marker.setTitle(marker.getPosition().toString())
        marker.showInfoWindow()
        marker.setAlpha(0.5f)
    }
    override fun getInfoWindow(marker: Marker): View? {
        return null
    }
    override fun getInfoContents(marker: Marker): View? {
        return prepareInfoView(marker)
    }





    fun prepareInfoView(marker: Marker): LinearLayout {
        var infoView = LinearLayout(this)
        var infoViewParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoView.setOrientation(LinearLayout.HORIZONTAL);
        infoView.setLayoutParams(infoViewParams)

        var subInfoLat = TextView(this)
        subInfoLat.setText("Lat: " + marker.getPosition().latitude)

        infoView.addView(subInfoLat)
        return infoView
    }
    fun tomarViaje(){
        var url =
            "http://eleccionesargentina.online/WebServices/acciones/tomarViaje.php"

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener { response ->
                val json = response.getJSONArray("viajes_array")
                Log.i("WebserviceConductor","Respuesta:"+json[0])

                var i=0
                while (i < json.length()){
                    var separador = json[i].toString().split("|")
                    agregarMarcador(separador[0].toDouble(), separador[1].toDouble())
                    i++
                }

            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)


    }


    var MyOnInfoWindowClickListener = GoogleMap.OnInfoWindowClickListener{
        //Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()

    }


}
