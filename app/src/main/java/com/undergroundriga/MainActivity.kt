package com.undergroundriga

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.firebase.auth.GoogleAuthProvider

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.Timestamp
import com.undergroundriga.ActivityReg

private const val TAG = "Main"





class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPreferences: SharedPreferences

    var PREFS_KEY = "prefs"
    var USER_ID_KEY = "user_id"
    var USER_NAME_KEY = "user_name"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already signed in, navigate to MapsActivity
            goToMapsActivity()
        }

        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId("716429145792-nt4qem3pck8hobjijbmcic1tin6qikr1.apps.googleusercontent.com")
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build())
            .build()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1086149395268-vs2atf4tg5vaafi17ftk079stsnj5k2u.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set click listener for Google Sign In button
        findViewById<Button>(R.id.googleSignInButton).setOnClickListener {
            signIn()
        }

        // Set click listener for Email Sign Up button
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
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
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
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                    Toast.makeText(this, "Google sign in successful", Toast.LENGTH_SHORT).show()

                    // Add user data to Firestore
                    addUserToFirestore(user)

                    // Save user ID and name in SharedPreferences
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putString(USER_ID_KEY, user?.uid)
                    editor.putString(USER_NAME_KEY, user?.displayName)
                    editor.apply()

                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
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

    private fun addUserToFirestore(user: FirebaseUser?) {
        // Access a Cloud Firestore instance
        val db = FirebaseFirestore.getInstance()

        // Get the current timestamp
        val timestamp = (Timestamp.now()).toString()

        // Create a new user with a first and last name
        val newUser = hashMapOf(
            "name" to user?.displayName,
            "email" to user?.email,
            "dateOfCreation" to timestamp
            // Add more user data as needed
        )

        // Add UserStats data
        val userStats = hashMapOf(
            "UserID" to user!!.uid,
            "VisitedPlaces" to 0,
            "SubmitedPlaces" to 0,
            "AcceptedSubm" to 0,
            "ProfPickPurchased" to 0,
            "LeaderboardMaxPos" to ""
        )

        // Add UserBalance data with initial balance of 100
        val userBalance = hashMapOf(
            "UserID" to user!!.uid,
            "Balance" to 100
        )

        db.collection("Users")
            .document(user!!.uid)
            .set(newUser)
            .addOnSuccessListener {
                db.collection("UserStats")
                    .document(user!!.uid)
                    .set(userStats)
                    .addOnSuccessListener {
                        db.collection("UserBalance")
                            .document(user!!.uid)
                            .set(userBalance)
                            .addOnSuccessListener {
                                Toast.makeText(this, "User added to Firestore", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, ActivityLogin::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding user to Firestore", Toast.LENGTH_SHORT).show()
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

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

    private fun goToMapsActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish() // Finish MainActivity to prevent going back to it when pressing back button from MapsActivity
    }
}
