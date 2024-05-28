package com.undergroundriga

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import android.util.Log

import android.content.Intent

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

        if (email.isEmpty() || password.isEmpty() || passwordRep.isEmpty() || username.isEmpty() || passwordRep != password) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Email sign up successful", Toast.LENGTH_SHORT).show()

                    addUserToFirestore(user, email, username, password)
                    sendVerificationEmail(user)
                } else {
                    Toast.makeText(this, "Email sign up failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addUserToFirestore(user: FirebaseUser?, email: String, username: String, password: String) {
        val db = FirebaseFirestore.getInstance()
        val dateOfCreation = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val hashedPassword = hashPassword(password)

        val newUser = hashMapOf(
            "email" to email,
            "name" to username,
            "dateOfFirstLogin" to dateOfCreation
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

    private fun hashPassword(password: String): String {
        return try {
            val md: MessageDigest = MessageDigest.getInstance("SHA-256")
            val hash: ByteArray = md.digest(password.toByteArray())
            val hexString = StringBuilder(2 * hash.size)
            for (b in hash) {
                val hex = Integer.toHexString(0xff and b.toInt())
                if (hex.length == 1) {
                    hexString.append('0')
                }
                hexString.append(hex)
            }
            hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            Log.e("Error", "Exception: ${e.message}")
            ""
        }
    }
}
