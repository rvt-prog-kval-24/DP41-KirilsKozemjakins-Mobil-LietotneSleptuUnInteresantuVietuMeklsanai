package com.undergroundriga.SignUp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.undergroundriga.MainActivity
import com.undergroundriga.R
import com.undergroundriga.User
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.undergroundriga.ActivityLogin
import com.undergroundriga.EmailSignUpActivity


private const val RC_SIGN_IN = 9001

class ActivityReg : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg)

        val db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId("716429145792-ame2svf431sge7du4nqa3pb612gs67qb.apps.googleusercontent.com")
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build())
            .build()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("bob")
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
        val timestamp = Timestamp.now()

        // Create a new user with a first and last name
        val newUser = hashMapOf(
            "name" to user?.displayName,
            "email" to user?.email,
            "dateOfCreation" to timestamp
            // Add more user data as needed
        )

        // Add a new document with a generated ID
        db.collection("Users")
            .document(user!!.uid)
            .set(newUser)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
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

    fun goToMain(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}
