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
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import ru.whalemare.sheetmenu.extension.marginTop
import java.io.IOException

class ConductorActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerDragListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    // Google Maps
    private lateinit var mMap: GoogleMap
    private lateinit var marcadorConductor : Marker
    private var camara : Int = 0
    private var conductor_Lat : Double = 0.0
    private var conductor_Lon : Double = 0.0


    // Webservice
    lateinit var queue: RequestQueue

    // GeoLocalizacion
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val INTERVAL: Long = 1000
    private val FASTEST_INTERVAL: Long = 2000
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


    fun agregarMarcador(lat: Double,long: Double, title: String){
        val tag = LatLng(lat, long)
        mMap.addMarker(MarkerOptions().position(tag).title(title).icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.person)))
        mMap.setOnMarkerDragListener(this)
        mMap.setInfoWindowAdapter(this)
        mMap.setOnInfoWindowClickListener(this);

    }

    fun crearMarcadoresPasajeros(){
        var url =
            "http://eleccionesargentina.online/WebServices/acciones/peticionesViajes.php?idusuario=1"

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener { response ->
                val json = response.getJSONArray("viajes_array")
                Log.i("WebserviceConductor","Respuesta:"+json[0])

                var i=0
                while (i < json.length()){
                    var separador = json[i].toString().split("|")
                    agregarMarcador(separador[0].toDouble(), separador[1].toDouble(), response.getString("usuario"))
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
        Toast.makeText(this, "asas", Toast.LENGTH_LONG).show()

        var valoresMarcador = marker.title.toString().split("|")

        // WEBSERVICE
        var url =
            "http://eleccionesargentina.online/WebServices/acciones/tomarViaje.php?" +
                    "idusuario=" + valoresMarcador[0] +
                    "&idconductor=3"

        val jsonObjectRequest = JsonObjectRequest(url,null,
            Response.Listener { response ->
                Log.i("WebserviceViajeTomado","Respuesta:"+response)

                if(response.getBoolean("status") == true){

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(conductor_Lat,conductor_Lon),14.0f))
                    Toast.makeText(this, "Viaje tomado", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this, "Ups! Parece que el viaje ya fué tomado", Toast.LENGTH_LONG).show()
                }

            },
            Response.ErrorListener { error -> error.printStackTrace() })
        queue.add(jsonObjectRequest)

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
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        infoView.setOrientation(LinearLayout.VERTICAL);
        infoView.setLayoutParams(infoViewParams)
        var nombreUsuario = TextView(this)
        var reputacion = TextView(this)
        var btn_tomarViaje = Button(this)

        // estilos
        btn_tomarViaje.setText("TOMAR VIAJE")
        btn_tomarViaje.setTextColor(this.getResources().getColor(R.color.white))
        btn_tomarViaje.setBackgroundColor(this.getResources().getColor(R.color.LightBlue))

        var valoresMarcador = marker.title.toString().split("|")
        nombreUsuario.setText("Nombre: " + valoresMarcador[1] + " #" + valoresMarcador[0])
        reputacion.setText("Reputacion: "+valoresMarcador[2] + " estrellas")




        // Agregamos los textview a la vista <LineaLayout> ~~infoView
        infoView.addView(nombreUsuario)
        infoView.addView(reputacion)
        infoView.addView(btn_tomarViaje)
        // retornamos la vista en forma de <LinearLayout> ~~infoView
        return infoView
    }



    var MyOnInfoWindowClickListener = GoogleMap.OnInfoWindowClickListener{
        //Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()

    }


}
