package com.example.firebaseloginapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.firebaseloginapp.databinding.ActivityHomeBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*


class HomeActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityHomeBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var map: GoogleMap
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var destino: Location
    private lateinit var fusedLocation: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)

        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        val bundle: Bundle? = intent.extras
        val email: String? = bundle?.getString("email")

        notification()
        setup(email ?:"")
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        destino = Location("")
        destino.latitude = 39.422781839708335
        destino.longitude = -0.41536888910767716

        createPolylines() // Poligonos
        currentLocation() // Ubicacion actual
        createMarker() // Colocar chincheta
        addMarkers() // A??adir marcadores
        addRoutes() // Unir marcadores con poligonos
    }

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }
    }

    private fun createMarker() {
        map.addMarker(
            MarkerOptions().position(LatLng(destino.latitude,destino.longitude)).title("Mi destino")
        )
    }

    internal var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                //The last location in the list is the newest
                val location = locationList.last()
                if (location.distanceTo(destino) <= 5) {
                    Toast.makeText(this@HomeActivity,"Estas en el destino", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@HomeActivity,"Estas a ${location.distanceTo(destino).toInt()} metros del destino", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun currentLocation() {
        checkPermissions() // Comprobar permisos
        map.isMyLocationEnabled = true
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true

        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 5000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        fusedLocation.requestLocationUpdates(mLocationRequest,mLocationCallback,Looper.myLooper()!!)

        /*fusedLocation.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val ubicacion = LatLng(location.latitude,location.longitude)
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(ubicacion,12f)
                )
            }
        }*/
    }

    private fun addMarkers() {
        map.setOnMapLongClickListener {
            val markerOptions  = MarkerOptions().position(it)
            markerOptions.icon(
                BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_GREEN
                )
            )
            map.addMarker(markerOptions)
            map.animateCamera(CameraUpdateFactory.newLatLng(it))
        }
    }

    private fun addRoutes() {
        val routeOptions = PolylineOptions()
        map.setOnMapLongClickListener {
            val markerOptions  = MarkerOptions().position(it)
            markerOptions.icon(
                BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_CYAN
                )
            )
            map.addMarker(markerOptions)
            map.animateCamera(CameraUpdateFactory.newLatLng(it))
            routeOptions.add(it)
            map.addPolyline(routeOptions)
        }
    }

    private fun createPolylines() {
        val polylineOptions = PolylineOptions()
            .add(LatLng(40.419173113350965,-3.705976009368897))
            .add(LatLng( 40.4150807746539, -3.706072568893432))
            .add(LatLng( 40.41517062907432, -3.7012016773223873))
            .add(LatLng( 40.41713105928677, -3.7037122249603267))
            .add(LatLng( 40.41926296230622,  -3.701287508010864))
            .add(LatLng( 40.419173113350965, -3.7048280239105225))
            .add(LatLng(40.419173113350965,-3.705976009368897))

        val polyline = map.addPolyline(polylineOptions)
    }

    private fun notification() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            // Get new FCM registration token
            val token = task.result

            // Log and toast
            println("El token del dispositivo es: ${token}")
        })

        FirebaseMessaging.getInstance().subscribeToTopic("tutorial")
    }

    private fun setup(email: String) {
        title = "Inicio"
        binding.emailTextView.text = "Bienvenid@, $email"

        binding.btnCargarDatos.setOnClickListener {
            cargarDatos()
        }

        binding.btnConsultarInfo.setOnClickListener {
            val intent = Intent(this, ConsultaActivity::class.java)
            startActivity(intent)
        }

        binding.btnComposeActivity.setOnClickListener {
            val intent = Intent(this, ComposeActivity::class.java)
            startActivity(intent)
        }

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }

    }

    private fun cargarDatos() {

        // ------------------=PAISES=------------------

        db.collection("paises").document("ES").set(
            hashMapOf(
                "nombre" to "ESPA??A",
                "poblacion" to 47350000,
            )
        )
        db.collection("paises").document("FR").set(
            hashMapOf(
                "nombre" to "FRANCIA",
                "poblacion" to 67390000,
            )
        )
        db.collection("paises").document("IT").set(
            hashMapOf(
                "nombre" to "ITALIA",
                "poblacion" to 59550000,
            )
        )
        db.collection("paises").document("DE").set(
            hashMapOf(
                "nombre" to "ALEMANIA",
                "poblacion" to 83240000,
            )
        )
        db.collection("paises").document("UK").set(
            hashMapOf(
                "nombre" to "INGLATERRA",
                "poblacion" to 55980000,
            )
        )

        // ------------------=CIUDADES=------------------

        db.collection("ciudades").document("mad").set(
            hashMapOf(
                "nombre" to "Madrid",
                "poblacion" to 3223000,
                "cod_pais" to "ES",
            )
        )
        db.collection("ciudades").document("val").set(
            hashMapOf(
                "nombre" to "Valencia",
                "poblacion" to 791413,
                "cod_pais" to "ES",
            )
        )
        db.collection("ciudades").document("bar").set(
            hashMapOf(
                "nombre" to "Barcelona",
                "poblacion" to 1620000,
                "cod_pais" to "ES",
            )
        )
        db.collection("ciudades").document("par").set(
            hashMapOf(
                "nombre" to "Paris",
                "poblacion" to 2161000,
                "cod_pais" to "FR",
            )
        )
        db.collection("ciudades").document("tou").set(
            hashMapOf(
                "nombre" to "Toulouse",
                "poblacion" to 471941,
                "cod_pais" to "FR",
            )
        )
        db.collection("ciudades").document("ly").set(
            hashMapOf(
                "nombre" to "Lyon",
                "poblacion" to 513275,
                "cod_pais" to "FR",
            )
        )
        db.collection("ciudades").document("rom").set(
            hashMapOf(
                "nombre" to "Roma",
                "poblacion" to 2873000,
                "cod_pais" to "IT",
            )
        )
        db.collection("ciudades").document("mil").set(
            hashMapOf(
                "nombre" to "Mil??n",
                "poblacion" to 1352000,
                "cod_pais" to "IT",
            )
        )
        db.collection("ciudades").document("nap").set(
            hashMapOf(
                "nombre" to "N??poles",
                "poblacion" to 3085000,
                "cod_pais" to "IT",
            )
        )
        db.collection("ciudades").document("mun").set(
            hashMapOf(
                "nombre" to "Munich",
                "poblacion" to 1472000,
                "cod_pais" to "DE",
            )
        )
        db.collection("ciudades").document("ber").set(
            hashMapOf(
                "nombre" to "Berlin",
                "poblacion" to 3645000,
                "cod_pais" to "DE",
            )
        )
        db.collection("ciudades").document("stu").set(
            hashMapOf(
                "nombre" to "Stuttgart",
                "poblacion" to 634830,
                "cod_pais" to "DE",
            )
        )
        db.collection("ciudades").document("lon").set(
            hashMapOf(
                "nombre" to "Londres",
                "poblacion" to 8982000,
                "cod_pais" to "UK",
            )
        )
        db.collection("ciudades").document("man").set(
            hashMapOf(
                "nombre" to "Manchester",
                "poblacion" to 553230,
                "cod_pais" to "UK",
            )
        )
        db.collection("ciudades").document("liv").set(
            hashMapOf(
                "nombre" to "Liverpool",
                "poblacion" to 496784,
                "cod_pais" to "UK",
            )
        )

        // ------------------=MONUMENTOS=------------------

        db.collection("monumentos").document("pr").set(
            hashMapOf(
                "nombre" to "Palacio Real",
                "cod_poblacion" to "mad",
            )
        )
        db.collection("monumentos").document("cac").set(
            hashMapOf(
                "nombre" to "Ciudad de las Artes y las Ciencias",
                "cod_poblacion" to "val",
            )
        )
        db.collection("monumentos").document("pg").set(
            hashMapOf(
                "nombre" to "Park G??ell",
                "cod_poblacion" to "bar",
            )
        )
        db.collection("monumentos").document("tei").set(
            hashMapOf(
                "nombre" to "Torre Eiffel",
                "cod_poblacion" to "par",
            )
        )
        db.collection("monumentos").document("jarjap").set(
            hashMapOf(
                "nombre" to "Jardin Japonais Pierre Baudis",
                "cod_poblacion" to "tou",
            )
        )
        db.collection("monumentos").document("galr").set(
            hashMapOf(
                "nombre" to "Gallo-Roman Museum of Lyon-Fourvi??re",
                "cod_poblacion" to "ly",
            )
        )
        db.collection("monumentos").document("col").set(
            hashMapOf(
                "nombre" to "Coliseo Romano",
                "cod_poblacion" to "rom",
            )
        )
        db.collection("monumentos").document("mil").set(
            hashMapOf(
                "nombre" to "Duomo de Mil??n",
                "cod_poblacion" to "mil",
            )
        )
        db.collection("monumentos").document("ves").set(
            hashMapOf(
                "nombre" to "Monte Vesubio",
                "cod_poblacion" to "nap",
            )
        )
        db.collection("monumentos").document("mar").set(
            hashMapOf(
                "nombre" to "Marienplatz",
                "cod_poblacion" to "mun",
            )
        )
        db.collection("monumentos").document("muc").set(
            hashMapOf(
                "nombre" to "Museo de la cerveza",
                "cod_poblacion" to "mun",
            )
        )
        db.collection("monumentos").document("pbr").set(
            hashMapOf(
                "nombre" to "Puerta de Brandeburgo",
                "cod_poblacion" to "ber",
            )
        )
        db.collection("monumentos").document("lps").set(
            hashMapOf(
                "nombre" to "Librer??a p??blica de Stuttgart",
                "cod_poblacion" to "stu",
            )
        )
        db.collection("monumentos").document("lbr").set(
            hashMapOf(
                "nombre" to "London Bridge",
                "cod_poblacion" to "lon",
            )
        )
    }
}