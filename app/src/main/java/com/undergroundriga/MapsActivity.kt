package com.undergroundriga


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var placeNameTextView: TextView
    private lateinit var placeDescriptionTextView: TextView
    private lateinit var placeAuthorTextView: TextView
    private lateinit var placeImageView: ImageView
    private lateinit var vMenu: ConstraintLayout
    private lateinit var bHideMenu: Button
    private lateinit var userTV: TextView
    private lateinit var logoutBtn: Button
    private lateinit var ivFocusOnLocation: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private var CurrentLocationMarker: Marker? = null

    private lateinit var commentList: ListView
    private lateinit var etCommText: EditText
    private lateinit var DislikeImage: ImageView
    private lateinit var LikeImage: ImageView
    private lateinit var bSendComment: Button

    private lateinit var currentUserID: String

    private val ZOOM_LEVEL_INCREMENT = 1f
    private val YOUR_PERMISSION_REQUEST_CODE = 123 // Use any unique integer value
    private val PREFS_KEY = "prefs"
    private val USER_NAME_KEY = "user_name"

    private lateinit var tagFilterLayout: LinearLayout
    private lateinit var ivTagFilter: ImageView
    private lateinit var buttonCloseTags: ImageView
    private val markers = mutableListOf<Marker>()

    private lateinit var db: FirebaseFirestore

    private lateinit var loadingLayout: LinearLayout




    override fun onCreate(savedInstanceState: Bundle?) {

        db = FirebaseFirestore.getInstance();

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        auth = FirebaseAuth.getInstance()

        userTV = findViewById(R.id.idTVUserName)
        logoutBtn = findViewById(R.id.idBtnLogOut)

        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

        loadingLayout = findViewById(R.id.loadingLayout)

        val user = auth.currentUser
        val userID = user?.uid ?: ""

        if (!userID.isNullOrEmpty()) {
            db.collection("Users").document(userID)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("name").orEmpty()
                        userTV.text = "Welcome, $username!"
                    } else {
                        userTV.text = "Welcome, who are you???"
                    }
                }
                .addOnFailureListener { e ->
                    userTV.text = "Error retrieving user: ${e.message}"
                }
        } else {
            userTV.text = "User ID is invalid."
        }

        logoutBtn.setOnClickListener {
            // Sign out from Firebase Authentication


            // Clear SharedPreferences data
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            // Start MainActivity
            val intent = Intent(this@MapsActivity, MainActivity::class.java)
            startActivity(intent)
            auth.signOut()

            // Finish the current activity
            finish()
        }

        initializeViews()
        setupButtonListeners()
        initializeBottomSheet()

        tagFilterLayout = findViewById(R.id.tagFilterLayout)
        ivTagFilter = findViewById(R.id.ivTagFilter)
        buttonCloseTags = findViewById(R.id.buttonCloseTags)

        ivTagFilter.setOnClickListener {
            toggleTagFilterLayout()
        }

        findViewById<Button>(R.id.applyTagFilterButton).setOnClickListener {
            applyTagFilter()
        }

        if (!isInternetAvailable(this) || !isLocationEnabled(this)) {
            showAlertDialog(this,layoutInflater)
        } else {
            try {
                fetchInitialData()
                fetchTagsFromFirestore { tags ->
                    displayTags(tags)
                }

                ivTagFilter.setOnClickListener {
                    toggleTagFilterLayout()
                    ivTagFilter.visibility = View.GONE
                }
                buttonCloseTags.setOnClickListener {
                    toggleTagFilterLayout()
                    ivTagFilter.visibility = View.VISIBLE
                }
            }catch (e: IllegalArgumentException){
                showAlertDialog(this,layoutInflater)
            }

        }

    }



    private fun fetchInitialData() {
        // Fetch user data
        fetchUserData { success ->
            if (success) {

                    // Hide the loading layout once all data is loaded
                    loadingLayout.visibility = View.GONE

            } else {
                // Handle error
                Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show()
                // Optionally, navigate back to the login screen
            }
        }
    }

    private fun fetchUserData(callback: (Boolean) -> Unit) {
        val user = auth.currentUser
        val userID = user?.uid ?: ""

        db.collection("Users").document(userID)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("name").orEmpty()
                    userTV.text = "Welcome, $username!"
                    callback(true)
                } else {
                    userTV.text = "Welcome, who are you???"
                    callback(false)
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }


    private fun toggleTagFilterLayout() {
        if (tagFilterLayout.visibility == View.GONE) {
            tagFilterLayout.visibility = View.VISIBLE
        } else {
            tagFilterLayout.visibility = View.GONE
        }
    }

    private fun fetchTagsFromFirestore(callback: (List<String>) -> Unit) {
        db.collection("Tags").get()
            .addOnSuccessListener { result ->
                val tags = result.documents.mapNotNull { it.getString("name") }
                callback(tags)
                Log.d("MapsActivity", "Tags: ${tags}")
            }
            .addOnFailureListener { exception ->
                Log.w("MapsActivity", "Error getting tags: ", exception)
                callback(emptyList())
            }
    }

    private fun displayTags(tags: List<String>) {

        val typefaceN = ResourcesCompat.getFont(this, R.font.font_family_nunito)

        tags.forEach { tag ->
            val checkBox = CheckBox(this).apply {
                text = tag
                id = View.generateViewId()
                // Customizing the appearance of the CheckBox
                setTextColor(ContextCompat.getColor(context, R.color.black))
                textSize = 16f
                setPadding(10, 10, 10, 10)
                typeface = typefaceN
            }
            tagFilterLayout.addView(checkBox, tagFilterLayout.childCount - 2)
        }

    }

    fun resetTagFilter(view: View) {
        // Iterate through all the views in the tagFilterLayout
        val selectedTags = mutableListOf<String>()
        for (i in 0 until tagFilterLayout.childCount) {
            val view = tagFilterLayout.getChildAt(i)
            // If the view is a CheckBox, uncheck it
            if (view is CheckBox) {
                view.isChecked = false
                selectedTags.add(view.text.toString())
            }
        }

        showMarkersForSelectedTags(selectedTags)
    }



    private fun applyTagFilter() {
        val selectedTags = mutableListOf<String>()

        for (i in 0 until tagFilterLayout.childCount) {
            val view = tagFilterLayout.getChildAt(i)
            if (view is CheckBox && view.isChecked) {
                selectedTags.add(view.text.toString())
            }
        }

        showMarkersForSelectedTags(selectedTags)
    }


    private fun showMarkersForSelectedTags(selectedTags: List<String>) {


        for (marker in markers) {
            val markerTag = marker.tag as? MarkerTag
            val placeTag = markerTag?.placeTag

            val tag = "#"+marker.snippet?.split("#")?.lastOrNull()


            if (placeTag != null && selectedTags.contains("$placeTag")) {
                marker.isVisible = true
            } else {
                marker.isVisible = false
            }
        }
    }

    private fun initializeBottomSheet() {
        val bottomSheet: ConstraintLayout = findViewById(R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        placeNameTextView = findViewById(R.id.placeName)
        placeDescriptionTextView = findViewById(R.id.placeDescription)
        placeAuthorTextView = findViewById(R.id.placeAuthor)
        placeImageView = findViewById(R.id.placeImage)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        commentList.adapter =
            ArrayAdapter(this, R.layout.comments_list_item, ArrayList<String>())


    }



    data class Comment(
        val commentID: String, // Unique identifier for the comment
        val userID: String, // User who posted the comment
        val userName: String, // Username of the commenter (optional)
        val placeID: String, // ID of the place the comment belongs to
        val commentText: String, // Content of the comment
        val commentMark: Long, // Content of the comment
        val timestamp: String // Time the comment was posted (optional)
    )



    class CommentAdapter(private val context: Context, private val comments: List<Comment>) : BaseAdapter() {

        override fun getCount(): Int = comments.size

        override fun getItem(position: Int): Comment = comments[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View = convertView ?: inflateItemView(parent!!)

            val comment = getItem(position)

            val profileImage = view.findViewById<ImageView>(R.id.profile_image)
            val userNameTextView = view.findViewById<TextView>(R.id.user_name)
            val commentTextView = view.findViewById<TextView>(R.id.comment_text)
            val commentMark = view.findViewById<ImageView>(R.id.comment_mark) // Assuming comment mark for indication

            // Load profile image based on user ID (implementation depends on your data source)
            // You can use Glide or another image loading library here.
            // profileImage.setImageResource(R.drawable.default_profile_image) // Set default image if needed

            userNameTextView.text = comment.userName
            commentTextView.text = comment.commentText

            // Handle comment mark visibility or icon based on logic (optional)
            commentMark.visibility = View.GONE // Hide by default

            // You can add functionality for the Change and Delete buttons (optional)
            val changeButton = view.findViewById<Button>(R.id.bChangeComment)
            val deleteButton = view.findViewById<Button>(R.id.bDeleteComment)

            val db = FirebaseFirestore.getInstance()
            val commentList = view.findViewById<ListView>(R.id.commentList)

            // Set click listeners and handle change/delete logic here
            deleteButton.setOnClickListener {
                val commentId = comment.commentID
                deleteComment(commentId) { success ->
                    if (success) {
                        val updatedComments = comments.toMutableList().apply { removeAt(position) }
                        val adapter = CommentAdapter(context, updatedComments)
                        commentList.adapter = adapter
                    } else {
                        // Handle delete failure (e.g., show a toast message)
                        Toast.makeText(context, "Error deleting comment!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            return view
        }

        fun deleteComment(commentId: String, callback: (Boolean) -> Unit) {
            val db = FirebaseFirestore.getInstance()
            db.collection("Comments").document(commentId)
                .delete()
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener { e ->
                    Log.w("CommentAdapter", "Error deleting comment: ", e)
                    callback(false)
                }
        }

        private fun inflateItemView(parent: ViewGroup): View {
            val inflater = LayoutInflater.from(parent.context)
            return inflater.inflate(R.layout.comments_list_item, parent, false)
        }
    }




    private fun showBottomSheet(marker: Marker) {
        val placeName = marker.title
        val placeDescription = marker.snippet
        val markerTag = marker.tag as? MarkerTag
        val photoURL = markerTag?.photoURL
        val AuthorID = markerTag?.authorID




        placeNameTextView.text = placeName
        placeDescriptionTextView.text = placeDescription

        // Load the image using Glide
        if (!photoURL.isNullOrEmpty()) {
            Glide.with(this)
                .load(photoURL)
                .into(placeImageView)
        } else {
            placeImageView.setImageResource(R.drawable.ic_bright_lampa_menu) // Set a default image if photoURL is empty
        }

        // Fetch and display author's name
        if (!AuthorID.isNullOrEmpty()) {
            val db = FirebaseFirestore.getInstance()

            db.collection("Users").document(AuthorID)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val authorName = document.getString("name").orEmpty()
                        placeAuthorTextView.text ="added by "+ authorName
                    } else {
                        placeAuthorTextView.text = "added by team of Underground Riga"
                    }
                }
                .addOnFailureListener { e ->
                    placeAuthorTextView.text = "Error fetching author"
                    Log.e("MapsActivity", "Error fetching author: ${e.message}")
                }
        } else {
            placeAuthorTextView.text = "added by team of Underground Riga"
        }

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        val placeID = markerTag?.placeID

        if (placeID != null) {
            fetchComments(placeID)
        }

        // Set like/dislike button click listeners

        // Set like/dislike button click listeners
        DislikeImage.setOnClickListener { v: View? ->
            handleLikeDislike(
                marker,
                0
            )
        }
        LikeImage.setOnClickListener { v: View? ->
            handleLikeDislike(
                marker,
                1
            )
        }

        // Set send comment button click listener

        // Set send comment button click listener
        bSendComment.setOnClickListener { v: View? ->
            sendComment(
                marker
            )
        }


    }

    private fun fetchComments(placeID: String) {
        val comments: MutableList<Comment> = ArrayList()

        db.collection("Comments")
            .whereEqualTo("placeID", placeID)
            .get()
            .addOnSuccessListener { commentsSnapshot ->
                for (commentDoc in commentsSnapshot.documents) {
                    val commentID = commentDoc.id
                    val userID = commentDoc.getString("userID") ?: ""
                    val commentText = commentDoc.getString("commentText") ?: ""
                    val commentMark = commentDoc.getLong("commentMark") ?: 0L
                    val timestamp = commentDoc.getString("dateTime") ?: ""

                    // Extract username later using a separate call (optional)
                    val userName = "" // Placeholder for username (not retrieved here)

                    comments.add(Comment(commentID, userID, userName, placeID, commentText, commentMark,timestamp))
                }

                // Update adapter with fetched comments (usernames can be fetched later)
                val adapter = CommentAdapter(this, comments)
                commentList.adapter = adapter

            }
            .addOnFailureListener { e ->
                Log.w("MapsActivity", "Error fetching comments: ", e)
            }
    }


    private fun handleLikeDislike(marker: Marker, likeDislike: Int) {
        // Update drawable based on selection (like_selected_color or dislike_selected_color)
        val dislikeSelectedDrawable = resources.getDrawable(R.drawable.dislike_selected_color)
        val likeSelectedDrawable = resources.getDrawable(R.drawable.like_selected_color)
        DislikeImage.setImageResource(if (likeDislike == 0) R.drawable.dislike_selected_color else R.drawable.dislike)
        LikeImage.setImageResource(if (likeDislike == 1) R.drawable.like_selected_color else R.drawable.like)

    }

    private fun sendComment(marker: Marker) {
        val commentText = etCommText.text.toString()
        val markerTag = marker.tag as? MarkerTag
        val placeID = markerTag?.placeID
        val commentMark = if (DislikeImage.drawable === resources.getDrawable(R.drawable.dislike_selected_color)) 0 else 1
        val dateTime = SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Date())

        updateUserBalance(currentUserID,-10)
        // Option 1: Store like/dislike choice in a separate field within the "Comments" collection
        val commentData = hashMapOf(
            "userID" to currentUserID,
            "placeID" to placeID,
            "commentText" to commentText,
            "commentMark" to commentMark,
            "dateTime" to dateTime
        )


        db.collection("Comments")  // Update collection name based on your choice (Comments or UserLikes)
            .add(commentData)  // Update data based on your choice (commentData or userLikeData)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Comment sent!", Toast.LENGTH_SHORT).show()
                etCommText.text.clear()  // Clear comment text after successful submission
            }
            .addOnFailureListener { e ->
                Log.w("MapsActivity", "Error sending comment: ", e)
            }
    }




    private fun initializeViews() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        vMenu = findViewById(R.id.vMenu)
        bHideMenu = findViewById(R.id.bHideMenu)

        ivFocusOnLocation = findViewById<ImageView>(R.id.ivFocusOnLocation)

        ivFocusOnLocation.setOnClickListener {
            focusOnUserLocation()
        }

        commentList = findViewById(R.id.commentList);
        etCommText = findViewById(R.id.etCommText);
        DislikeImage = findViewById(R.id.DislikeImage);
        LikeImage = findViewById(R.id.LikeImage);
        bSendComment = findViewById(R.id.bSendComment);

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            currentUserID = currentUser.uid
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
                    }
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

    private fun updateLocationOnMap(location: Location) {
        val currentLatLng = LatLng(location.latitude, location.longitude)

        // Remove the last location marker if it exists
        CurrentLocationMarker?.remove()

        // Add the new location marker to the map
        CurrentLocationMarker = mMap.addMarker(
            MarkerOptions()
                .position(currentLatLng)
                .title("Your Location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_current_location))
        )
    }




    private fun addMarker(document: QueryDocumentSnapshot) {
        val posX = document.getString("PosX")?.toDoubleOrNull()
        val posY = document.getString("PosY")?.toDoubleOrNull()
        val placeName = document.getString("PlaceName").orEmpty()
        val description = document.getString("Description").orEmpty()
        val tag = document.getString("tags").orEmpty()
        val AuthorID = document.getString("UserId").orEmpty()
        val placeID = document.id
        val photoURL = document.getString("photoURL").orEmpty()  // Get photoURL from the document

        if (posX != null && posY != null) {
            val iconResource = CheckTagIc(tag)
            val markerOptions = MarkerOptions()
                .position(LatLng(posX, posY))
                .title(placeName)
                .snippet("$description$tag")
                .icon(BitmapDescriptorFactory.fromResource(iconResource))

            // Add a custom info window with the image from photoURL

            val marker = mMap.addMarker(markerOptions)
            marker?.tag = MarkerTag(photoURL, AuthorID, placeID, tag)
            marker?.let { markers.add(it) }

            // Add place ID to the marker snippet (assuming you have a way to store it)



        }
    }

    data class MarkerTag(val photoURL: String, val authorID: String, val placeID: String, val placeTag: String)



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val latviaLatLng = LatLng(56.9496, 24.1052)

        val zoomOutButton = findViewById<ImageView>(R.id.zoomOutButton)
        zoomOutButton.setOnClickListener {
            val currentZoomLevel = googleMap.cameraPosition.zoom
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latviaLatLng, currentZoomLevel - ZOOM_LEVEL_INCREMENT))
        }

        fetchPlacesAndAddMarkers()

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)


                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                    }
                }
                .addOnFailureListener { e: Exception ->
                    Toast.makeText(this, "Failed to get current location: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                YOUR_PERMISSION_REQUEST_CODE
            )
        }

        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(5000)

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult?.lastLocation?.let { location ->
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        checkMarkersForProximity(currentLatLng)
                    }
                }
            },
            Looper.getMainLooper()
        )

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                }
            }

        mMap.setOnMarkerClickListener { marker ->
            showBottomSheet(marker)
            true
        }
    }







    private fun fetchPlacesAndAddMarkers() {
        val db = FirebaseFirestore.getInstance()

        db.collection("Places")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    addMarker(document)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun checkMarkersForProximity(currentLatLng: LatLng) {

        for (marker in markers) {
            val markerLatLng = marker.position
            val distanceInMeters = distanceBetween(currentLatLng, markerLatLng)
            if (distanceInMeters <= 10) { // Update to 10 meters for proximity check
                registerVisit(marker);
                break; // Register only the closest visited place within 10 meters
            }
        }
    }

    private fun distanceBetween(latLng1: LatLng, latLng2: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            latLng1.latitude,
            latLng1.longitude,
            latLng2.latitude,
            latLng2.longitude,
            results
        )
        return results[0]
    }

    private fun registerVisit(marker: Marker) {

        if (marker != null) {

            val markerTag = marker.tag as? MarkerTag

            if (markerTag != null) {
                val placeID = markerTag.placeID


                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val userID = currentUser.uid


                    // Prepare reference to user's Visited subcollection
                    val visitedRef = db.collection("Users").document(userID).collection("Visited");

                    if (placeID != null) {
                        // Check if place has already been visited
                        visitedRef.document(placeID).get()
                            .addOnSuccessListener { documentSnapshot ->
                                if (!documentSnapshot.exists()) { // Place not visited yet
                                    // Create a new document for the visited place
                                    val visitDoc = visitedRef.document(placeID);

                                    updateUserBalance(userID,100)


                                    // Set data for the visited place document (e.g., placeID and visit timestamp)
                                    visitDoc.set(hashMapOf(
                                        "placeID" to placeID,
                                        "visitTime" to FieldValue.serverTimestamp()
                                    ))
                                        .addOnSuccessListener { aVoid: Void? ->
                                            // Update VisitedPlaces in UserStats (assuming a separate collection)
                                            updateVisitedPlacesCount(userID, 1)
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w(
                                    "MapsActivity",
                                    "Error checking for visited place",
                                    e
                                )
                            }
                    }
                } else {
                    // Handle the case when there is no current user signed in
                }



            }
        }

    }

    private fun updateVisitedPlacesCount(userID: String, increment: Int) {
        val userStatsRef = db.collection("UserStats").whereEqualTo("UserID", userID) // Filter by UserID

        userStatsRef.get()
            .addOnSuccessListener { documentsSnapshot ->
                if (documentsSnapshot.isEmpty) {
                    // UserStats document not found, create a new one with initial VisitedPlaces value (0)
                    db.collection("UserStats").document(userID).set(
                        hashMapOf("UserID" to userID, "VisitedPlaces" to 0)
                    )
                        .addOnSuccessListener {
                            Log.d(
                                "MapsActivity",
                                "New UserStats document created for user: $userID"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.w(
                                "MapsActivity",
                                "Error creating UserStats document",
                                e
                            )
                        }
                    return@addOnSuccessListener // Exit the function if no documents found
                }

                // Get the first document (assuming there should only be one per user)
                val documentSnapshot = documentsSnapshot.documents[0]
                val currentVisitedPlaces = documentSnapshot.getLong("VisitedPlaces") ?: 0
                val updatedVisitedPlaces = currentVisitedPlaces + increment

                documentSnapshot.reference.update("VisitedPlaces", updatedVisitedPlaces)
                    .addOnSuccessListener {
                        Log.d(
                            "MapsActivity",
                            "VisitedPlaces count updated successfully!"
                        )
                        checkAndUpdateAchievements(userID, updatedVisitedPlaces) // Check for achievements after update
                    }
                    .addOnFailureListener { e ->
                        Log.w(
                            "MapsActivity",
                            "Error updating VisitedPlaces count",
                            e
                        )
                    }
            }
            .addOnFailureListener { e ->
                Log.w(
                    "MapsActivity",
                    "Error fetching user stats",
                    e
                )
            }
    }

    private fun checkAndUpdateAchievements(userID: String, updatedVisitedPlaces: Long) = runBlocking {
        val achievementsRef = FirebaseFirestore.getInstance().collection("Achievements")

        try {
            val achievementsSnapshot = achievementsRef.get().await()
            val completedAchievementIds = getCompletedAchievementIds(userID)


            for (achievementDoc in achievementsSnapshot) {
                val conditionVariable = achievementDoc.getString("ConditionVariable")
                val requiredValue = achievementDoc.getLong("RequiredValue") ?: 0L
                val reward = achievementDoc.getLong("Reward") ?: 0L
                val achievementId = achievementDoc.id


                if (conditionVariable == "VisitedPlaces" && updatedVisitedPlaces >= requiredValue &&
                    !completedAchievementIds.contains(achievementId)) {
                    // Achievement unlocked! Update user balance and completed achievements
                    updateUserBalance(userID, reward)
                    addUserCompletedAchievement(userID, achievementId)
                    break // Only process the first achievement that meets the condition
                }
            }
        } catch (e: Exception) {
            Log.w(
                "MapsActivity",
                "Error fetching achievements",
                e
            )
        }
    }

    private suspend fun getCompletedAchievementIds(userID: String): List<String> {
        val completedAchievementIds = mutableListOf<String>()
        val completedAchievementsRef = FirebaseFirestore.getInstance()
            .collection("Users").document(userID).collection("CompletedAchievements")

        try {
            val documentsSnapshot = completedAchievementsRef.get().await()
            for (documentSnapshot in documentsSnapshot) {
                val achievementId = documentSnapshot.getString("AchievementID")


                if (achievementId != null) {
                    completedAchievementIds.add(achievementId)

                }
            }
        } catch (e: Exception) {
            Log.w(
                "MapsActivity",
                "Error fetching completed achievements for user $userID",
                e
            )
        }



        return completedAchievementIds
    }

    private fun updateUserBalance(userID: String, reward: Long) {
        val userBalanceRef = db.collection("UserBalance").whereEqualTo("UserID", userID)

        userBalanceRef.get()
            .addOnSuccessListener { documentsSnapshot ->
                if (documentsSnapshot.isEmpty) {
                    // UserBalance document not found, create a new one with initial balance (0)
                    db.collection("UserBalance").document(userID).set(
                        hashMapOf("UserID" to userID, "Balance" to 0L)
                    )
                        .addOnSuccessListener {
                            Log.d(
                                "MapsActivity",
                                "New UserBalance document created for user: $userID"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.w(
                                "MapsActivity",
                                "Error creating UserBalance document",
                                e
                            )
                        }
                    return@addOnSuccessListener // Exit the function if no documents found
                }

                // Get the first document (assuming there should be only one per user)
                val documentSnapshot = documentsSnapshot.documents[0]
                val currentBalance = documentSnapshot.getLong("Balance") ?: 0L
                val updatedBalance = currentBalance + reward

                documentSnapshot.reference.update("Balance", updatedBalance)
                    .addOnSuccessListener {
                        Log.d(
                            "MapsActivity",
                            "User balance updated successfully! New balance: $updatedBalance"
                        )
                    }
                    .addOnFailureListener { e ->
                    Log.w(
                        "MapsActivity",
                        "Error updating user balance",
                        e
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.w(
                    "MapsActivity",
                    "Error fetching user balance",
                    e
                )
            }
    }

    private fun addUserCompletedAchievement(userID: String, achievementId: String) {
        val userRef = db.collection("Users").document(userID)

        val completedAchievements = userRef.collection("CompletedAchievements")

        completedAchievements.add(hashMapOf("AchievementID" to achievementId))
            .addOnSuccessListener { documentReference ->
                Log.d(
                    "MapsActivity",
                    "Achievement $achievementId added to completed achievements for user $userID"
                )
            }
            .addOnFailureListener { e ->
                Log.w(
                    "MapsActivity",
                    "Error adding achievement to completed list for user $userID",
                    e
                )
            }
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

    fun goToLeaderboard(view: View) {
        val intent = Intent(this, LeaderBoardActivity::class.java)
        startActivity(intent)
    }

    fun goToAchievements(view: View) {
        val intent = Intent(this, AchievementsActivity::class.java)
        startActivity(intent)
    }

    fun goToProf(view: View) {
        val intent = Intent(this, UserProfileActivity::class.java)
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
