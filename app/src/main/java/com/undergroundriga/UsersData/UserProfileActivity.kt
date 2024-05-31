package com.undergroundriga

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
private lateinit var sharedPreferences: SharedPreferences


private val PREFS_KEY = "prefs"
private val USER_NAME_KEY = "user_name"

class UserProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userEmailTextView: TextView
    private lateinit var userIdTextView: TextView
    private lateinit var userBalanceTextView: TextView
    private lateinit var userStatsTextView: TextView
    private lateinit var userNameTextView: TextView

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        userEmailTextView = findViewById(R.id.userEmailTextView)
        userIdTextView = findViewById(R.id.userIdTextView)
        userBalanceTextView = findViewById(R.id.userBalanceTextView)
        userNameTextView = findViewById(R.id.userName)


        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        val username = sharedPreferences.getString(USER_NAME_KEY, null)

        val user = auth.currentUser
        if (user != null) {
            userEmailTextView.text = "Email: ${user.email}"
            userIdTextView.text = "User ID: ${user.uid}"

            firestore.collection("Users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("name").orEmpty()

                        userNameTextView.text = "$username"
                    } else {
                        userNameTextView.text  = "Who are you???"
                    }
                }


            fetchUserBalance(user.uid)
        }
    }

    private fun showUserStatsBottomSheet(userId: String) {
        val bottomSheetFragment = UserStatsBottomSheetFragment.newInstance(userId)
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    fun ShowStats(view: View) {
        val user = auth.currentUser
        if (user != null) {
            showUserStatsBottomSheet(user.uid)
        }
    }

    private fun fetchUserBalance(userId: String) {
        firestore.collection("UserBalance").whereEqualTo("UserID", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val balance = document.getDouble("Balance") ?: 0.0
                    userBalanceTextView.text = "$balance"
                } else {
                    userBalanceTextView.text = "N/A"
                }
            }
            .addOnFailureListener {
                userBalanceTextView.text = "Balance: N/A"
                Toast.makeText(this, "Failed to fetch balance", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserStats(userId: String) {
        firestore.collection("UserStats").whereEqualTo("UserID", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val acceptedSubm = document.getLong("AcceptedSubm") ?: 0
                    val leaderboardMaxPos = document.getString("LeaderboardMaxPos") ?: "N/A"
                    val profPickPurchased = document.getLong("ProfPickPurchased") ?: 0
                    val submitedPlaces = document.getLong("SubmitedPlaces") ?: 0
                    val visitedPlaces = document.getLong("VisitedPlaces") ?: 0

                    userStatsTextView.text = """
                        Accepted Submissions: $acceptedSubm
                        Leaderboard Max Position: $leaderboardMaxPos
                        Profile Pics Purchased: $profPickPurchased
                        Submitted Places: $submitedPlaces
                        Visited Places: $visitedPlaces
                    """.trimIndent()
                } else {
                    userStatsTextView.text = "User Stats: N/A"
                }
            }
            .addOnFailureListener {
                userStatsTextView.text = "User Stats: N/A"
                Toast.makeText(this, "Failed to fetch user stats", Toast.LENGTH_SHORT).show()
            }
    }

    fun goBackToMaps(view: View) {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish() // Finish MainActivity to prevent going back to it when pressing back button from MapsActivity
    }

    fun goToProfPickStore(view: View) {
        val intent = Intent(this, ProfilePickStoreActivity::class.java)
        startActivity(intent)
        finish() // Finish MainActivity to prevent going back to it when pressing back button from MapsActivity
    }

    fun resetPassword(view: View) {
        val user = auth.currentUser
        user?.let {
            auth.sendPasswordResetEmail(it.email!!)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password reset link sent to your email", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to send password reset link", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
