package com.undergroundriga

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ActivityCheckMySuggestions : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_my_suggestions)

        listView = findViewById(R.id.listViewSuggestions)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Get current user ID
        val currentUserId = auth.currentUser?.uid ?: return

        // Fetch user suggestions
        fetchUserSuggestions(currentUserId)
    }

    private fun fetchUserSuggestions(userId: String) {
        val suggestions = mutableListOf<Suggestion>()
        firestore.collection("PlacesSuggestions")
            .whereEqualTo("UserId", userId)
            .get()
            .addOnSuccessListener { documentsSnapshot ->
                for (document in documentsSnapshot) {
                    val placeName = document.getString("PlaceName") ?: ""
                    val description = document.getString("Description") ?: ""
                    val tag = document.getString("Tag") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val respond = document.getString("respond") ?: ""
                    val posX = document.getString("PosX") ?: ""
                    val posY = document.getString("PosY") ?: ""

                    suggestions.add(Suggestion(placeName, description, tag, imageUrl, respond, posX, posY))
                }

                val adapter = SuggestionAdapter(this, suggestions)
                listView.adapter = adapter
            }
    }

    fun goToMapsMain(view: View) {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Data class to hold suggestion data
    data class Suggestion(
        val placeName: String,
        val description: String,
        val tag: String,
        val imageUrl: String,
        val respond: String,
        val posX: String,
        val posY: String
    )

    // Custom adapter class to display suggestions in a list
    class SuggestionAdapter(private val context: Context, private val suggestions: List<Suggestion>) : BaseAdapter() {

        private val pendingIcon: Drawable by lazy { context.getDrawable(R.drawable.ic_clock_grey)!! }
        private val positiveIcon: Drawable by lazy { context.getDrawable(R.drawable.ic_check_green)!! }
        private val negativeIcon: Drawable by lazy { context.getDrawable(R.drawable.ic_close_red)!! }

        override fun getCount(): Int = suggestions.size

        override fun getItem(position: Int): Suggestion = suggestions[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View = convertView ?: inflateItemView(parent!!)

            val suggestion = getItem(position)
            val imageView = view.findViewById<ImageView>(R.id.suggestion_image)
            val placeNameTextView = view.findViewById<TextView>(R.id.suggestion_place_name)
            val descriptionTextView = view.findViewById<TextView>(R.id.suggestion_description)
            val tagTextView = view.findViewById<TextView>(R.id.suggestion_tag)
            val respondImageView = view.findViewById<ImageView>(R.id.suggestion_respond_icon)

            // Set respond icon based on respond status
            val respondIcon = when (suggestion.respond) {
                "pending" -> pendingIcon
                "positive" -> positiveIcon
                "negative" -> negativeIcon
                else -> null
            }

            Glide.with(context)                // Load suggestion image with placeholder and error handling
                .load(suggestion.imageUrl)
                .into(imageView)

            placeNameTextView.text = suggestion.placeName
            descriptionTextView.text = suggestion.description
            tagTextView.text = suggestion.tag
            respondImageView.setImageDrawable(respondIcon)

            // Set background color based on respond status (optional)
            if (suggestion.respond == "positive") {
                view.setBackgroundColor(Color.parseColor("#BFD5C8")) // Light green for positive response
            } else if (suggestion.respond == "negative") {
                view.setBackgroundColor(Color.parseColor("#F9BDBE")) // Light red for negative response
            }

            return view
        }

        private fun inflateItemView(parent: ViewGroup): View {
            val inflater = LayoutInflater.from(parent.context)
            return inflater.inflate(R.layout.suggestion_list_item, parent, false)
        }
    }
}
