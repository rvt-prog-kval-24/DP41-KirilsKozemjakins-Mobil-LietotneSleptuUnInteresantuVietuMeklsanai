package com.undergroundriga

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck


class UserProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userEmailTextView: TextView
    private lateinit var userIdTextView: TextView
    private lateinit var userBalanceTextView: TextView
    private lateinit var userStatsTextView: TextView
    private lateinit var userNameTextView: TextView
    private lateinit var userProfPickImageView: ImageView

    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val DEFAULT_PROFILE_PICTURE_URL = "https://example.com/default_profile_picture.png" // Replace with your default profile picture URL
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // Initialize views
        userEmailTextView = findViewById(R.id.userEmailTextView)
        userIdTextView = findViewById(R.id.userIdTextView)
        userBalanceTextView = findViewById(R.id.userBalanceTextView)
        userNameTextView = findViewById(R.id.userName)
        userProfPickImageView = findViewById(R.id.UserProfPick)

        // Retrieve user information
        val user = auth.currentUser
        if (user != null) {
            userEmailTextView.text = "Email: ${user.email}"
            userIdTextView.text = "User ID: ${user.uid}"

            // Retrieve user name
            firestore.collection("Users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("name").orEmpty()
                        userNameTextView.text = username
                    } else {
                        userNameTextView.text = "Who are you???"
                    }
                }

            // Retrieve selected profile picture URL
            firestore.collection("Users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    val selectedPicId = document.getString("CurrentPickID").orEmpty()

                    if (selectedPicId.isEmpty()) {
                        // Load default profile picture if CurrentPickID is empty
                        loadProfilePicture(DEFAULT_PROFILE_PICTURE_URL)
                    } else {
                        firestore.collection("ProfPictures").document(selectedPicId)
                            .get()
                            .addOnSuccessListener { document ->
                                val profilePictureUrl = document.getString("ProfPickURL")
                                if (!profilePictureUrl.isNullOrEmpty()) {
                                    loadProfilePicture(profilePictureUrl)
                                } else {
                                    loadProfilePicture(DEFAULT_PROFILE_PICTURE_URL)
                                }
                            }
                            .addOnFailureListener {
                                loadProfilePicture(DEFAULT_PROFILE_PICTURE_URL)
                            }
                    }
                }
                .addOnFailureListener {
                    loadProfilePicture(DEFAULT_PROFILE_PICTURE_URL)
                }

            // Fetch user balance
            fetchUserBalance(user.uid)
        }
    }

    private fun loadProfilePicture(url: String) {
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.default_prof_pick) // Placeholder image while loading
            .error(R.drawable.default_prof_pick) // Error image if loading fails
            .into(userProfPickImageView)
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

    fun deleteAccount(view: View) {
        val user = auth.currentUser
        user?.let {
            val userId = it.uid

            // Show confirmation dialog
            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Yes") { dialog, _ ->
                    // Delete user data from Firestore
                    firestore.collection("Users").document(userId).delete()
                        .addOnSuccessListener {
                            // Delete user data from other collections if needed
                            firestore.collection("UserBalance").whereEqualTo("UserID", userId).get()
                                .addOnSuccessListener { querySnapshot ->
                                    for (document in querySnapshot) {
                                        firestore.collection("UserBalance").document(document.id).delete()
                                    }
                                }

                            firestore.collection("UserStats").whereEqualTo("UserID", userId).get()
                                .addOnSuccessListener { querySnapshot ->
                                    for (document in querySnapshot) {
                                        firestore.collection("UserStats").document(document.id).delete()
                                    }
                                }

                            // Delete user account from Firebase Auth
                            user.delete()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
                                        Log.e("DeleteAccount", "Error deleting account", task.exception)
                                    }
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to delete user data", Toast.LENGTH_SHORT).show()
                            Log.e("DeleteAccount", "Error deleting user data", e)
                        }

                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

}
