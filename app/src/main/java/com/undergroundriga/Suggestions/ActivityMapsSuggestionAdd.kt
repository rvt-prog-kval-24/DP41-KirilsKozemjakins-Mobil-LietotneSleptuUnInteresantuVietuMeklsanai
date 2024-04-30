package com.undergroundriga

import android.content.Intent
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import android.os.Looper
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


class ActivityMapsSuggestionAdd : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var btn_insert: Button
    private lateinit var etPlaceName: EditText
    private lateinit var etDescription: EditText
    private lateinit var spTag: Spinner
    private lateinit var etPlaceAddress: EditText

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap

    // Declare these variables in your activity or fragment
    private val ZOOM_LEVEL_INCREMENT = 1f
    private val DEFAULT_ZOOM_LEVEL = 10f  // Set to your preferred default

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_suggestion_add)

        val context = this
        var db = DataBaseHandler(context)

        // Initialize views
        btn_insert = findViewById(R.id.btn_insert)
        etPlaceName = findViewById(R.id.etPlaceName)
        etDescription = findViewById(R.id.etDescription)
        spTag = findViewById(R.id.spTag)
        etPlaceAddress = findViewById(R.id.etPlaceAddress)

        mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE)

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
            val address = etPlaceAddress.text.toString()
            val tag = spTag.selectedItem.toString()

            // Get user ID from SharedPreferences
            val userId = sharedPreferences.getInt("user_id", -1)

            // Convert address to LatLng
            val addressLat = convertAddressToLat(address)
            val addressLng = convertAddressToLng(address)

            if (placeName.isNotEmpty() && description.isNotEmpty() && addressLat != null && addressLng != null) {
                val posX = addressLat.toString()
                val posY = addressLng.toString()

                val suggestPlace = SuggestPlace(placeName, description, userId, tag, posX, posY)

                db.insertDataPlacesSuggestions(suggestPlace)

                Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please Fill All Data's", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun convertAddressToLat(address: String): Double? {
        val geocoder = Geocoder(this)
        try {
            val addresses: List

            <Address>? = geocoder.getFromLocationName(address, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                return addresses[0].latitude
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun convertAddressToLng(address: String): Double? {
        val geocoder = Geocoder(this)
        try {
            val addresses: List<Address>? = geocoder.getFromLocationName(address, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                return addresses[0].longitude
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap

        val db = DataBaseHandler(this)
        val data = db.readDataMapsPlaces()

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
        val latviaLatLng = LatLng(
            56.9496,
            24.1052
        ) // Replace with the actual coordinates of Latvia or a specific location in Latvia
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latviaLatLng, 10f))

        googleMap.setOnMapClickListener { latLng ->
            etPlaceAddress.setText("")
            etPlaceAddress.isEnabled = false
            etPlaceAddress.isFocusable = false
            etPlaceAddress.isFocusableInTouchMode = false
            etPlaceAddress.postDelayed({
                etPlaceAddress.setText(getAddressFromLatLng(latLng))
                etPlaceAddress.isEnabled = true
                etPlaceAddress.isFocusable = true
                etPlaceAddress.isFocusableInTouchMode = true
            }, 2000)

            etPlaceName.setText("")
            etDescription.setText("")
        }
    }

    private fun getAddressFromLatLng(latLng: LatLng): String {
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: String
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                address = addresses[0].getAddressLine(0)
                return address
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    fun goToMapsMain(view: View) {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun goToMySuggestions(view: View) {
        val intent = Intent(this, ActivityCheckMySuggestions::class.java)
        startActivity(intent)
        finish()
    }



}





