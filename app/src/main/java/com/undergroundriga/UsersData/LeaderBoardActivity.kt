package com.undergroundriga

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.firebase.firestore.Query
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

data class LeaderboardItem(
    val userId: String,
    val username: String,
    val acceptedSubmissions: Int,
    val profilePictureUrl: String
)

class LeaderBoardActivity : AppCompatActivity() {

    private lateinit var leaderboardList: ListView
    private lateinit var firestore: FirebaseFirestore
    private val defaultProfilePictureUrl = "URL_OF_YOUR_DEFAULT_PICTURE" // Replace with actual URL or resource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leader_board)

        leaderboardList = findViewById(R.id.Leaderboard_list)
        firestore = FirebaseFirestore.getInstance()

        // Fetch leaderboard data
        fetchLeaderboardData()
    }

    class LeaderboardAdapter(private val context: Context, private val leaderboardItems: List<LeaderboardItem>) : BaseAdapter() {

        override fun getCount(): Int = leaderboardItems.size

        override fun getItem(position: Int): LeaderboardItem = leaderboardItems[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.leaderboard_list_item, parent, false)

            val leaderboardItem = getItem(position)
            val userImage = view.findViewById<ImageView>(R.id.user_image)
            val usernameTextView = view.findViewById<TextView>(R.id.username)
            val userAcceptedSubmTextView = view.findViewById<TextView>(R.id.user_AcceptedSubm)

            // Load the profile picture into ImageView using Glide
            Glide.with(context)
                .load(leaderboardItem.profilePictureUrl)
                .placeholder(R.drawable.default_prof_pick) // Placeholder image while loading
                .error(R.drawable.default_prof_pick) // Error image if loading fails
                .into(userImage)

            usernameTextView.text = leaderboardItem.username
            userAcceptedSubmTextView.text = "Accepted submissions: " + leaderboardItem.acceptedSubmissions.toString()

            return view
        }
    }

    private fun fetchLeaderboardData() {
        val leaderboardItems = mutableListOf<LeaderboardItem>()
        firestore.collection("UserStats")
            .orderBy("AcceptedSubm", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documentsSnapshot ->
                GlobalScope.launch(Dispatchers.Main) {
                    documentsSnapshot.forEach { document ->
                        val userId = document.getString("UserID") ?: ""
                        val acceptedSubmissions = document.getLong("AcceptedSubm")?.toInt() ?: 0

                        val username = fetchUsername(userId)
                        val profilePictureUrl = fetchProfilePictureUrl(userId) ?: defaultProfilePictureUrl

                        val leaderboardItem = LeaderboardItem(userId, username, acceptedSubmissions, profilePictureUrl)
                        leaderboardItems.add(leaderboardItem)

                        if (leaderboardItems.size == documentsSnapshot.size()) {
                            val adapter = LeaderboardAdapter(this@LeaderBoardActivity, leaderboardItems)
                            leaderboardList.adapter = adapter
                        }
                    }
                }
            }
    }

    private suspend fun fetchUsername(userId: String): String {
        return try {
            val userDoc = firestore.collection("Users").document(userId).get().await()
            userDoc.getString("name") ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private suspend fun fetchProfilePictureUrl(userId: String): String? {
        return try {
            val userDoc = firestore.collection("Users").document(userId).get().await()
            val picId = userDoc.getString("CurrentPickID")
            if (picId != null) {
                val picDoc = firestore.collection("ProfPictures").document(picId).get().await()
                picDoc.getString("ProfPickURL")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun goBackToMaps(view: View) {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }
}
