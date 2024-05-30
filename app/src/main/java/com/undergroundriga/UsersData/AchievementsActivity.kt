package com.undergroundriga

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AchievementsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var achievementsListView: ListView

    data class Achievement(
        val photoURL: String,
        val title: String,
        val description: String,
        val reward: Int,
        val completed: Boolean // Add this field
    )



    class AchievementAdapter(private val context: Context, private val achievements: List<Achievement>) : BaseAdapter() {

        override fun getCount(): Int = achievements.size

        override fun getItem(position: Int): Achievement = achievements[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View = convertView ?: inflateItemView(parent!!)

            val achievement = getItem(position)
            val imageView = view.findViewById<ImageView>(R.id.achievement_image)
            val titleTextView = view.findViewById<TextView>(R.id.achievement_title)
            val descriptionTextView = view.findViewById<TextView>(R.id.achievement_description)
            val rewardTextView = view.findViewById<TextView>(R.id.achievement_reward)

            Glide.with(context)
                .load(achievement.photoURL)
                .placeholder(R.drawable.ic_vynil) // Add a placeholder image for loading state
                .error(R.drawable.ic_marker_404) // Add an error image for failed loading
                .into(imageView)

            titleTextView.text = achievement.title
            descriptionTextView.text = achievement.description
            rewardTextView.text = "${achievement.reward} pts"

            // Change background color if the achievement is completed
            if (achievement.completed) {
                view.setBackgroundColor(Color.parseColor("#7AD180"))
            } else {
                view.setBackgroundColor(Color.TRANSPARENT)
            }

            return view
        }

        private fun inflateItemView(parent: ViewGroup): View {
            val inflater = LayoutInflater.from(parent.context)
            return inflater.inflate(R.layout.achievement_list_item, parent, false)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        achievementsListView = findViewById(R.id.achievements_list)


        fetchAchievements()
    }

    private fun fetchAchievements(userId: String = auth.currentUser?.uid ?: "") {
        val achievements = mutableListOf<Achievement>()
        val completedAchievementsIds = mutableSetOf<String>()

        // Fetch completed achievements
        firestore.collection("Users").document(userId).collection("CompletedAchievements")
            .get()
            .addOnSuccessListener { completedSnapshot ->
                for (document in completedSnapshot.documents) {
                    val achievementId = document.getString("AchievementID")
                    if (achievementId != null) {
                        completedAchievementsIds.add(achievementId)
                    }
                }

                // Fetch all achievements
                firestore.collection("Achievements")
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            val photoURL = document.getString("photoURL") ?: ""
                            val title = document.getString("Title") ?: ""
                            val description = document.getString("Description") ?: ""
                            val reward = document.getLong("Reward")?.toInt()
                            val achievementId = document.id

                            if (reward != null) {
                                val completed = completedAchievementsIds.contains(achievementId)
                                achievements.add(Achievement(photoURL, title, description, reward, completed))
                            }
                        }

                        val adapter = AchievementAdapter(this, achievements)
                        achievementsListView.adapter = adapter
                    }
            }
    }


    fun goBackToMaps(view: View) {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish() // Finish MainActivity to prevent going back to it when pressing back button from MapsActivity
    }


}


