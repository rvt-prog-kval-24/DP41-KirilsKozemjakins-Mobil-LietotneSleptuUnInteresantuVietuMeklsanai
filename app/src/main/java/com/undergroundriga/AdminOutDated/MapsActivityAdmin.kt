package com.undergroundriga

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import com.google.android.gms.maps.model.Marker


class MapsActivityAdmin : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var btn_insert: Button
    private lateinit var etPlaceName: EditText
    private lateinit var etDescription: EditText
    private lateinit var spTag: Spinner
    private lateinit var etPosX: EditText
    private lateinit var etPosY: EditText
    private lateinit var etPlaceAddress: EditText

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap

    // Declare these variables in your activity or fragment
    private val ZOOM_LEVEL_INCREMENT = 1f
    private val DEFAULT_ZOOM_LEVEL = 10f  // Set to your preferred default

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val YOUR_PERMISSION_REQUEST_CODE = 123 // Use any unique integer value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_admin)

        val context = this
        var db = DataBaseHandler(context)

        // Initialize views
        btn_insert = findViewById(R.id.btn_insert)
        etPlaceName = findViewById(R.id.etPlaceName)
        etDescription = findViewById(R.id.etDescription)
        spTag = findViewById(R.id.spTag)
        etPosX = findViewById(R.id.etPosX)
        etPosY = findViewById(R.id.etPosY)
        etPlaceAddress = findViewById(R.id.etPlaceAddress)

        mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set up spinner
        val spinner: Spinner = findViewById(R.id.spTag)
        val items = arrayOf(
            "#Teashops",
            "#Animeshops",
            "#Food",
            "#Graffiti",
            "#Exotic",
            "#Second hand",
            "#Toilet",
            "#Vinyl store",
            "#Monument"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        btn_insert.setOnClickListener {
            // Get user input
            val placeName = etPlaceName.text.toString()
            val description = etDescription.text.toString()
            val posX = etPosX.text.toString()
            val posY = etPosY.text.toString()
            val address = etPlaceAddress.text.toString()

            // Convert address to LatLng
            val addressLat = convertAddressToLat(address)
            val addressLng = convertAddressToLng(address)

            if (placeName.isNotEmpty() && description.isNotEmpty()) {
                val selectedTag = spTag.selectedItem.toString()

                val finalPosX = if (posX.isNotEmpty()) posX else addressLat?.toString() ?: ""
                val finalPosY = if (posY.isNotEmpty()) posY else addressLng?.toString() ?: ""

                // Add additional statement: if posX and posY are not empty, use addressLat and addressLng
                val useAddress = posX.isNotEmpty() && posY.isNotEmpty() && addressLat != null && addressLng != null
                val finalPosXToUse = if (useAddress) addressLat.toString() else finalPosX
                val finalPosYToUse = if (useAddress) addressLng.toString() else finalPosY

                if (finalPosXToUse.isNotEmpty() && finalPosYToUse.isNotEmpty()) {
                    val place = Places(placeName, description, selectedTag, finalPosXToUse, finalPosYToUse)
                    db.insertDataPlaces(place)

                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Please Fill All Data's", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please Fill All Data's", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun convertAddressToLat(address: String): String? {
        val geocoder = Geocoder(this)
        try {
            val addresses: List<Address>? = geocoder.getFromLocationName(address, 1)
            if (addresses != null && addresses.isNotEmpty()) {


                val latitude = addresses[0].latitude.toString()
                return latitude
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun convertAddressToLng(address: String): String? {
        val geocoder = Geocoder(this)
        try {
            val addresses: List<Address>? = geocoder.getFromLocationName(address, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val longitude = addresses[0].longitude.toString()
                return longitude
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap

        // Initialize location features
        initLocationFeatures()

        val db = DataBaseHandler(this)
        val data = db.readDataMapsPlaces()

        // Keep track of the current marker
        var currentMarker: Marker? = null

        data.forEach { places ->
            val mapPoint = LatLng(places.PosX.toDouble(), places.PosY.toDouble())

            val tag = places.Tag
            val iconResource = CheckTagIc(tag)

            googleMap.addMarker(
                MarkerOptions()
                    .position(mapPoint)
                    .title(places.PlaceName)
                    .snippet("${places.Description}_${places.Tag}")
                    .icon(BitmapDescriptorFactory.fromResource(iconResource))
            )
        }

        // Zoom to Latvia (for example, Riga)
        val latviaLatLng = LatLng(56.9496, 24.1052) // Replace with the actual coordinates of Latvia or a specific location in Latvia

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latviaLatLng, 10f))

//        // Zoom In Button
//        val zoomInButton = findViewById<Button>(R.id.zoomInButton)
//        zoomInButton.setOnClickListener {
//            val currentZoomLevel = googleMap.cameraPosition.zoom
//            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latviaLatLng, currentZoomLevel + ZOOM_LEVEL_INCREMENT))
//        }

        // Zoom Out Button



        googleMap.setOnMapClickListener { latLng ->
            // Remove the previous marker if it exists
            currentMarker?.remove()

            // Add a new marker at the clicked position
            currentMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("New Place Marker")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_choose_location))
            )

            // Update the EditText fields with the new coordinates
            etPosX.setText(latLng.latitude.toString())
            etPosY.setText(latLng.longitude.toString())
        }
    }


    private fun initLocationFeatures() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted, proceed with location features
            getCurrentLocation()
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                YOUR_PERMISSION_REQUEST_CODE
            )
        }
    }


    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))
                }
            }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(
                    this,
                    "Failed to get current location: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun goTogoToMainAdmin(view: View) {
        val intent = Intent(this, MapsActivityAdmin::class.java)
        startActivity(intent)
        finish()
    }

}


