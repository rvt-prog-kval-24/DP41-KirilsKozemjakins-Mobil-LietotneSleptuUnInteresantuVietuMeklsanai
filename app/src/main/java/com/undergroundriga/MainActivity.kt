package com.undergroundriga

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 9001
        private const val PREFS_KEY = "prefs"
        private const val USER_ID_KEY = "user_id"
        private const val USER_NAME_KEY = "user_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1086149395268-oji3l6dtoguqc6tj0d232j8firkg8sfg.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already signed in, navigate to MapsActivity
            goToMapsActivity()
        }

        findViewById<Button>(R.id.googleSignInButton).setOnClickListener {
            signIn()
        }

        findViewById<Button>(R.id.emailSignUpButton).setOnClickListener {
            signUpWithEmail()
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                    Toast.makeText(this, "Google sign in successful", Toast.LENGTH_SHORT).show()

                    // Check if user exists before adding to Firestore
                    checkIfUserExists(user)

                    // Save user ID and name in SharedPreferences
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putString(USER_ID_KEY, user?.uid)
                    editor.putString(USER_NAME_KEY, user?.displayName)
                    editor.apply()

                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun signUpWithEmail() {
        val intent = Intent(this, EmailSignUpActivity::class.java)
        startActivity(intent)
    }

    private fun checkIfUserExists(user: FirebaseUser?) {
        val db = FirebaseFirestore.getInstance()
        val userId = user?.uid

        userId?.let {
            db.collection("Users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // User already exists
                        Toast.makeText(this, "User already exists in Firestore", Toast.LENGTH_SHORT).show()
                    } else {
                        // User does not exist, proceed to add user to Firestore
                        addUserToFirestore(user)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error checking user in Firestore", Toast.LENGTH_SHORT).show()
                    Log.w(TAG, "Error checking user in Firestore", e)
                }
        }
    }

    private fun addUserToFirestore(user: FirebaseUser?) {
        val db = FirebaseFirestore.getInstance()
        val timestamp = Timestamp.now().toString()

        val newUser = hashMapOf(
            "name" to user?.displayName,
            "email" to user?.email,
            "dateOfCreation" to timestamp,
            "CurrentPickID" to ""
        )

        val userStats = hashMapOf(
            "UserID" to user!!.uid,
            "VisitedPlaces" to 0,
            "SubmitedPlaces" to 0,
            "AcceptedSubm" to 0,
            "ProfPickPurchased" to 0,
            "LeaderboardMaxPos" to ""
        )

        val userBalance = hashMapOf(
            "UserID" to user.uid,
            "Balance" to 500
        )

        db.collection("Users").document(user.uid).set(newUser)
            .addOnSuccessListener {
                db.collection("UserStats").document(user.uid).set(userStats)
                    .addOnSuccessListener {
                        db.collection("UserBalance").document(user.uid).set(userBalance)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                            }
                    }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding user to Firestore", e)
            }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun updateUI(user: FirebaseUser?) {
        // Update UI code here
    }

    fun goToSignIn(view: View) {
        val intent = Intent(this, ActivityLogin::class.java)
        startActivity(intent)
    }

    fun goToSignUp(view: View) {
        val intent = Intent(this, ActivityReg::class.java)
        startActivity(intent)
    }

    private fun goToMapsActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish() // Finish MainActivity to prevent going back to it when pressing back button from MapsActivity
    }
}
