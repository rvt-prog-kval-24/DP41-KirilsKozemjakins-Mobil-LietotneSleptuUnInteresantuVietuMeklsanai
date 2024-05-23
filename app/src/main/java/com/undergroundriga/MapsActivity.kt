package com.undergroundriga


import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.widget.TextView
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.content.SharedPreferences
import android.location.Location
import com.google.android.gms.maps.model.BitmapDescriptorFactory

import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import android.os.Looper
import com.google.android.gms.maps.model.Marker


import android.content.pm.PackageManager
import android.Manifest
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.location.LocationListener
import android.view.View
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {


    private lateinit var mMap: GoogleMap
    private lateinit var vMenu: LinearLayout
    private lateinit var bHideMenu: Button
    private lateinit var userTV: TextView
    private lateinit var logoutBtn: Button
    private lateinit var ivFocusOnLocation: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var sharedPreferences: SharedPreferences

    private val ZOOM_LEVEL_INCREMENT = 1f
    private val DEFAULT_ZOOM_LEVEL = 10f


    private val YOUR_PERMISSION_REQUEST_CODE = 123 // Use any unique integer value


    var PREFS_KEY = "prefs"
    var USER_KEY = "user"
    var USER_NAME_KEY = "user_name"
    var usr = ""





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        auth = FirebaseAuth.getInstance()


        userTV = findViewById(R.id.idTVUserName)
        logoutBtn = findViewById(R.id.idBtnLogOut)

        val sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        val username = sharedPreferences.getString(USER_NAME_KEY, null)

        // Set the username to TextView
        userTV.text = "Welcome, $username!"


        ////

        logoutBtn.setOnClickListener {
            // Sign out from Firebase Authentication
            auth.signOut()

            // Clear SharedPreferences data
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            // Start MainActivity
            val intent = Intent(this@MapsActivity, MainActivity::class.java)
            startActivity(intent)

            // Finish the current activity
            finish()
        }



        initializeViews()
        setupButtonListeners()
    }



    private fun initializeViews() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        vMenu = findViewById(R.id.vMenu)
        bHideMenu = findViewById(R.id.bHideMenu)

        val ivFocusOnLocation = findViewById<ImageView>(R.id.ivFocusOnLocation)

        ivFocusOnLocation.setOnClickListener {
            focusOnUserLocation()
        }
    }



    private fun setupButtonListeners() {
        val myButton = findViewById<ImageView>(R.id.ivMenu)
        val buttonHideMenu = findViewById<Button>(R.id.bHideMenu)

        myButton.setOnClickListener {
            toggleMenuWidth()
        }

        buttonHideMenu.setOnClickListener {
            toggleHideMenuWidth()
        }
    }


    override fun onLocationChanged(location: Location) {
        updateLocationOnMap(location)
    }


    private fun focusOnUserLocation() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                    } /*else {
                        Toast.makeText(this, "" , Toast.LENGTH_SHORT).show()
                    }*/
                }
                .addOnFailureListener { e: Exception ->
                    Toast.makeText(
                        this,
                        "Failed to get current location: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            // Permission is not granted, request the permission from the user
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                YOUR_PERMISSION_REQUEST_CODE
            )
        }
    }



    private var CurrentLocationMarker: Marker? = null
    private fun updateLocationOnMap(location: Location) {
        val currentLatLng = LatLng(location.latitude, location.longitude)

        // Remove the last location marker if it exists
        CurrentLocationMarker?.remove()

        // Add the new location marker to the map
        CurrentLocationMarker = mMap.addMarker(
            MarkerOptions()
                .position(currentLatLng)
                .title("Your Location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_current_location_cat))
        )

        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))
    }


    /*
    Tags:
    "#Teashops"
    "#Animeshops"
    "#Food"
    "#Graffity"
    "#Exotic"
    "#Second hand"
    "#Toilet"
    "#Vinyl store"

    * */

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val db = DataBaseHandler(this)
        val data = db.readDataMapsPlaces()

        val latviaLatLng = LatLng(56.9496, 24.1052)

        val zoomOutButton = findViewById<ImageView>(R.id.zoomOutButton)
        zoomOutButton.setOnClickListener {
            val currentZoomLevel = googleMap.cameraPosition.zoom
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latviaLatLng, currentZoomLevel - ZOOM_LEVEL_INCREMENT))
        }

        data.forEach { places ->
            val mapPoint = LatLng(places.PosX.toDouble(), places.PosY.toDouble())

            val tag = places.Tag // Replace this with the actual tag you want to check
            val iconResource = CheckTagIc(tag)

            mMap.addMarker(
                MarkerOptions()
                    .position(mapPoint)
                    .title(places.PlaceName)
                    .snippet("${places.Description}_${places.Tag}")
                    .icon(BitmapDescriptorFactory.fromResource(iconResource))
            )

            /*
                        if (places == data.last()) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapPoint, 14f))
                        }*/
        }



        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Check if the app has the necessary location permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted, proceed with getting the last location
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

            fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)

                        CurrentLocationMarker = mMap.addMarker(
                            MarkerOptions()
                                .position(currentLatLng)
                                .title("Your Location")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_current_location_cat))
                        )
                        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f));

                       // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))
                    } /*else {
                        Toast.makeText(this, "Current location is null", Toast.LENGTH_SHORT).show()

                    }*/
                }
                .addOnFailureListener { e: Exception ->
                    Toast.makeText(this, "Failed to get current location: ${e.message}", Toast.LENGTH_SHORT).show()
                }

        } else {
            // Permission is not granted, request the permission from the user
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                YOUR_PERMISSION_REQUEST_CODE
            )
        }

        // Request location updates
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(5000) // Update location every 5 seconds

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult?.lastLocation?.let { location ->
                        updateLocationOnMap(location)
                    }
                }
            },
            Looper.getMainLooper()
        )


    }

    private fun toggleMenuWidth() {
        val layoutParams = vMenu.layoutParams
        val layoutParamsB = bHideMenu.layoutParams

        if (layoutParams.width == 0 && layoutParamsB.width == 0) {
            layoutParams.width = resources.getDimensionPixelSize(R.dimen.menu_width)
            layoutParamsB.width = resources.getDimensionPixelSize(R.dimen.button_menu_width_hide)
        } else {
            layoutParams.width = 0
            layoutParamsB.width = 0
        }

        vMenu.layoutParams = layoutParams
        bHideMenu.layoutParams = layoutParamsB
    }

    private fun toggleHideMenuWidth() {
        val layoutParams = vMenu.layoutParams
        val layoutParamsB = bHideMenu.layoutParams

        if (layoutParams.width == resources.getDimensionPixelSize(R.dimen.menu_width) &&
            layoutParamsB.width == resources.getDimensionPixelSize(R.dimen.button_menu_width_hide)) {
            layoutParams.width = resources.getDimensionPixelSize(R.dimen.menu_width_hide)
            layoutParamsB.width = resources.getDimensionPixelSize(R.dimen.hide_button_menu_width_hide)
        } else {
            layoutParams.width = resources.getDimensionPixelSize(R.dimen.menu_width)
            layoutParamsB.width = resources.getDimensionPixelSize(R.dimen.button_menu_width_hide)
        }

        vMenu.layoutParams = layoutParams
        bHideMenu.layoutParams = layoutParamsB
    }

    fun goToSuggest(view: View) {
        val intent = Intent(this, ActivityMapsSuggestionAdd::class.java)
        startActivity(intent)
    }


}

fun CheckTagIc(tag: String): Int {
    return when (tag) {
        "#Teashops" -> R.drawable.ic_marker_teashop
        "#Animeshops" -> R.drawable.ic_marker_anime_shop
        "#Food" -> R.drawable.ic_marker_food
        "#Graffiti"-> R.drawable.ic_marker_graffiti
        "#Exotic"-> R.drawable.ic_marker_lavka
        "#Second hand"-> R.drawable.ic_marker_second_hand
        "#Toilet"-> R.drawable.ic_marker_toilet
        "#Vinyl store"-> R.drawable.ic_marker_vinyl
        "#Monument"-> R.drawable.ic_marker_monuments

        else -> R.drawable.ic_marker_404
    }
}



