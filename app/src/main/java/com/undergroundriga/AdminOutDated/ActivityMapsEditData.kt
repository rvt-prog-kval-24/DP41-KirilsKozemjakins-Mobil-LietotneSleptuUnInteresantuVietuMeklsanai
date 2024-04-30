package com.undergroundriga

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
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

private lateinit var mapFragment: SupportMapFragment
private lateinit var googleMap: GoogleMap

private lateinit var spTag: Spinner
private lateinit var spPlaces: Spinner

private lateinit var fusedLocationClient: FusedLocationProviderClient
private val YOUR_PERMISSION_REQUEST_CODE = 123 // Use any unique integer value

class ActivityMapsEditData : AppCompatActivity(), OnMapReadyCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_edit_data)

        val context = this
        var db = DataBaseHandler(context)

        spTag = findViewById(R.id.spTag)
        spPlaces = findViewById(R.id.spChoosePlace)


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
            "#Graffity",
            "#Exotic",
            "#Second hand",
            "#Toilet",
            "#Vinyl store",
            "#Monument"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Set up spinner
        val spChoosePlace: Spinner = findViewById(R.id.spChoosePlace)
// Inside your ActivityMapsEditData class

        val placesList = db.getAllPlaceNamesAndIds()
        val placeNamesAndIds = placesList.map { "${it.first}: ${it.second}" }.toTypedArray()

        val adapterPlaces = ArrayAdapter(this, android.R.layout.simple_spinner_item, placeNamesAndIds)
        adapterPlaces.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spChoosePlace.adapter = adapterPlaces


            /* btn_read.setOnClickListener({
         var data = db.readDataMapsPlaces()
         tvResult.text = ""
         for (i in 0..(data.size - 1)) {
             tvResult.append(data.get(i).PlacesId.toString() + " "
                     + data.get(i).PlaceName + " "
                     + data.get(i).Description + " "
                     + data.get(i).Tag + " "
                     + data.get(i).PosX + " "
                     + data.get(i).PosY + "\n")
         }
     })

     btn_update.setOnClickListener {
         val placeIdText = etPlaceId.text.toString()

         if (placeIdText.isNotEmpty() && etPlaceName.text.isNotEmpty() && etDescription.text.isNotEmpty() &&
             spTag.selectedItem.toString().isNotEmpty() && etPosX.text.isNotEmpty() && etPosY.text.isNotEmpty()
         ) {
             val placeId = placeIdText.toInt()
             db.updateDataPlaces(
                 placeId,
                 etPlaceName.text.toString(),
                 etDescription.text.toString(),
                 spTag.selectedItem.toString(),
                 etPosX.text.toString(),
                 etPosY.text.toString()
             )
             btn_read.performClick()
         } else {
             Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
         }
     }



     btn_delete.setOnClickListener({
         val placeIdText = etPlaceId.text.toString()
         if (placeIdText.isNotEmpty()) {
             val placeId = placeIdText.toInt()
             db.deleteMapsData(placeId)
             btn_read.performClick()
         } else {
             Toast.makeText(context, "Please enter a valid Place ID to delete", Toast.LENGTH_SHORT).show()
         }
     })

    */

    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap
    }



}
