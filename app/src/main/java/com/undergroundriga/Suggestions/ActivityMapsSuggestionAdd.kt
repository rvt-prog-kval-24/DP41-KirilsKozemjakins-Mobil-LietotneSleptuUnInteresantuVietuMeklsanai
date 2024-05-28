package com.undergroundriga

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class ActivityMapsSuggestionAdd : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var btn_insert: Button
    private lateinit var etPlaceName: EditText
    private lateinit var etDescription: EditText
    private lateinit var spTag: Spinner

    private var uploadedImageUrl: String = ""
    private val REQUEST_IMAGE_CODE = 100
    private val REQUEST_CAMERA_CODE = 101

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private var currentMarker: Marker? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var firestore: FirebaseFirestore

    private lateinit var currentUser: FirebaseUser
    private lateinit var sharedPreferences: SharedPreferences

    var PREFS_KEY = "prefs"
    var USER_KEY = "user"
    var USER_NAME_KEY = "user_name"
    var USER_ID_KEY = "user_id"

    private var currentLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_suggestion_add)

        // Initialize views
        btn_insert = findViewById(R.id.btn_insert)
        etPlaceName = findViewById(R.id.etPlaceName)
        etDescription = findViewById(R.id.etDescription)
        spTag = findViewById(R.id.spTag)

        // Initialize map
        mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
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
            val tag = spTag.selectedItem.toString()

            val posX = currentMarker?.position?.latitude
            val posY = currentMarker?.position?.longitude

            if (placeName.isNotEmpty() && description.isNotEmpty() && posX != null && posY != null) {
                // Get user ID from SharedPreferences
                val sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
                val userId = sharedPreferences.getString(USER_ID_KEY, null)

                if (userId == null) {
                    Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Make an immutable copy of currentLatLng
                val currentLatLngCopy = currentLatLng
                if (currentLatLngCopy != null) {
                    // Check distance from current location
                    val distance = FloatArray(1)
                    android.location.Location.distanceBetween(
                        currentLatLngCopy.latitude, currentLatLngCopy.longitude,
                        posX, posY, distance
                    )

                    if (distance[0] > 25) {
                        Toast.makeText(this, "Place must be within 25 meters of your current location", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                } else {
                    Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (uploadedImageUrl.isEmpty()) {
                    Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Create a map to hold the suggestion data
                val suggestData = hashMapOf(
                    "PlaceName" to placeName,
                    "Description" to description,
                    "UserId" to userId,
                    "Tag" to tag,
                    "PosX" to posX.toString(),
                    "PosY" to posY.toString(),
                    "suggestionDate" to FieldValue.serverTimestamp(),
                    "imageUrl" to uploadedImageUrl  // Add the uploaded image URL
                )

                // Add the suggestion data to Firestore
                firestore.collection("PlacesSuggestions")
                    .add(suggestData)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, ActivityCheckMySuggestions::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to add suggestion: $e", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please Fill All Data's", Toast.LENGTH_SHORT).show()
            }
        }

        val uploadImageView = findViewById<ImageView>(R.id.imageView_upload_photo)
        uploadImageView.setOnClickListener {
            // Show options dialog
            val options = arrayOf("Select from Gallery", "Take a Photo")
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Choose an option")
            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> pickImageFromGallery()
                    1 -> takePhotoWithCamera()
                }
            }
            builder.show()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_CODE)
    }

    private fun takePhotoWithCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CODE && resultCode == RESULT_OK) {
            val imageUri = data?.data ?: return
            uploadImageToFirebase(imageUri)
        } else if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap ?: return
            val imageUri = getImageUriFromBitmap(this, imageBitmap)
            uploadImageToFirebase(imageUri)
        }
    }

    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("place_suggestions_images/${UUID.randomUUID()}")

        storageRef.putFile(imageUri)
            .addOnSuccessListener { snapshot ->
                snapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    uploadedImageUrl = uri.toString()
                }.addOnFailureListener { e ->
                    e.printStackTrace()
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        // Request location permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    currentLatLng = currentLocation
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                } else {
                    // Handle null location
                    Toast.makeText(this, "Last known location is null", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                // Handle failure to get last known location
                Toast.makeText(this, "Failed to get last known location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        googleMap.setOnMapClickListener { latLng ->
            currentMarker?.remove()
            currentMarker = googleMap.addMarker(MarkerOptions().position(latLng))
        }
    }

    private fun goBackToMaps() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish() // Finish MainActivity to prevent going back to it when pressing back button from MapsActivity
    }
}


