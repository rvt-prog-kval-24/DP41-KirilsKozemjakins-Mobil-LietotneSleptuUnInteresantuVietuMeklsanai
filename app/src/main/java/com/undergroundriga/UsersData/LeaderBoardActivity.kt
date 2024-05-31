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
import com.google.firebase.firestore.Query
import com.undergroundriga.R

data class LeaderboardItem(
    val userId: String,
    val username: String,
    val acceptedSubmissions: Int
)

class LeaderBoardActivity : AppCompatActivity() {

    private lateinit var leaderboardList: ListView
    private lateinit var firestore: FirebaseFirestore

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

            userImage.setImageResource(R.drawable.default_prof_pick) // Placeholder image

            usernameTextView.text = leaderboardItem.username
            userAcceptedSubmTextView.text = leaderboardItem.acceptedSubmissions.toString()

            return view
        }
    }

    private fun fetchLeaderboardData() {
        val leaderboardItems = mutableListOf<LeaderboardItem>()
        firestore.collection("UserStats")
            .orderBy("AcceptedSubm", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documentsSnapshot ->
                val tasks = documentsSnapshot.map { document ->
                    val userId = document.getString("UserID") ?: ""
                    val acceptedSubmissions = document.getLong("AcceptedSubm")?.toInt() ?: 0

                    // Fetch the username from the Users table
                    firestore.collection("Users").document(userId)
                        .get()
                        .continueWith { task ->
                            val username = if (task.isSuccessful) {
                                task.result?.getString("name") ?: "Unknown"
                            } else {
                                "Unknown"
                            }
                            LeaderboardItem(userId, username, acceptedSubmissions)
                        }
                }

                // Wait for all username fetch tasks to complete
                tasks.forEach { task ->
                    task.addOnCompleteListener { completedTask ->
                        if (completedTask.isSuccessful) {
                            leaderboardItems.add(completedTask.result!!)
                            // Update the adapter only once all tasks are complete
                            if (leaderboardItems.size == tasks.size) {
                                val adapter = LeaderboardAdapter(this@LeaderBoardActivity, leaderboardItems)
                                leaderboardList.adapter = adapter
                            }
                        }
                    }
                }
            }
    }

    fun goBackToMaps(view: View) {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }
}
