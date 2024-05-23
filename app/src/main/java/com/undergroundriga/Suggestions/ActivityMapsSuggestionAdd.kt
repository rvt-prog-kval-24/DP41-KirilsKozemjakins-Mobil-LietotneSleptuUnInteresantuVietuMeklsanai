package com.undergroundriga

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.undergroundriga.ActivityCheckMySuggestions
import com.undergroundriga.MapsActivity
import java.io.IOException
import java.util.*

import com.undergroundriga.R
import android.location.Geocoder


class ActivityMapsSuggestionAdd : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var btn_insert: Button
    private lateinit var etPlaceName: EditText
    private lateinit var etDescription: EditText
    private lateinit var spTag: Spinner
    private lateinit var etPlaceAddress: EditText

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_suggestion_add)

        // Initialize views
        btn_insert = findViewById(R.id.btn_insert)
        etPlaceName = findViewById(R.id.etPlaceName)
        etDescription = findViewById(R.id.etDescription)
        spTag = findViewById(R.id.spTag)
        etPlaceAddress = findViewById(R.id.etPlaceAddress)

        // Initialize map
        mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

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

            // Convert address to LatLng
            val addressLat = convertAddressToLat(address)
            val addressLng = convertAddressToLng(address)

            if (placeName.isNotEmpty() && description.isNotEmpty() && addressLat != null && addressLng != null) {
                val posX = addressLat.toString()
                val posY = addressLng.toString()

                // Get user ID from SharedPreferences
                val userId = getSharedPreferences("prefs", MODE_PRIVATE).getInt("user_id", -1)

                // Create a map to hold the suggestion data
                val suggestData = hashMapOf(
                    "placeName" to placeName,
                    "description" to description,
                    "userId" to userId,
                    "tag" to tag,
                    "posX" to posX,
                    "posY" to posY,
                    "timestamp" to FieldValue.serverTimestamp() // Add a timestamp for sorting
                )

                // Add the suggestion data to Firestore
                firestore.collection("suggestions")
                    .add(suggestData)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to add suggestion: $e", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please Fill All Data's", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun convertAddressToLat(address: String): Double? {
        val geocoder = Geocoder(this)
        try {
            val addresses = geocoder.getFromLocationName(address, 1)
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
            val addresses = geocoder.getFromLocationName(address, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                return addresses[0].longitude
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        // Zoom to Latvia (for example, Riga)
        val latviaLatLng = LatLng(56.9496, 24.1052)
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
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                return addresses[0].getAddressLine(0)
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
