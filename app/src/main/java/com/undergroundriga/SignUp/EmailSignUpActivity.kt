package com.undergroundriga

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class EmailSignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_sign_up)

        auth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.emailSignUp).setOnClickListener {
            signUp()
        }
    }

    private fun signUp() {
        val email = findViewById<EditText>(R.id.email).text.toString()
        val password = findViewById<EditText>(R.id.password).text.toString()
        val passwordRep = findViewById<EditText>(R.id.repeatPassword).text.toString()
        val username = findViewById<EditText>(R.id.username).text.toString()

        if (email.isEmpty() || password.isEmpty() || passwordRep.isEmpty()  || username.isEmpty() || passwordRep!=password) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up success, update UI with the signed-up user's information
                    val user = auth.currentUser
                    Toast.makeText(this, "Email sign up successful", Toast.LENGTH_SHORT).show()

                    // Add user data to Firestore
                    addUserToFirestore(user, email, username)

                    // Send verification email
                    sendVerificationEmail(user)
                } else {
                    // If sign up fails, display a message to the user.
                    Toast.makeText(this, "Email sign up failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addUserToFirestore(user: FirebaseUser?, email: String, username: String) {
        // Access a Cloud Firestore instance
        val db = FirebaseFirestore.getInstance()

        // Get the current timestamp
        val timestamp = Timestamp.now()

        // Create a new user with email, hashed password, username, and dateOfFirstLogin
        val newUser = hashMapOf(
            "email" to email,
            "password" to user?.uid, // Storing UID as password, this should be hashed for production
            "username" to username,
            "dateOfFirstLogin" to timestamp
            // Add more user data as needed
        )

        // Add a new document with a generated ID
        db.collection("Users")
            .document(user!!.uid)
            .set(newUser)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "User added to Firestore", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding user to Firestore", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendVerificationEmail(user: FirebaseUser?) {
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Verification email sent", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send verification email", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
