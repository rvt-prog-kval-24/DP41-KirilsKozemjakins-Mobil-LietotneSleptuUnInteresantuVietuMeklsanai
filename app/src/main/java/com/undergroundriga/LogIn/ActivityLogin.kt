package com.undergroundriga

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ActivityLogin : AppCompatActivity() {

    lateinit var EmailEditText: EditText
    lateinit var passwordEditText: EditText
    lateinit var loginButton: Button
    lateinit var sharedPreferences: SharedPreferences
    lateinit var auth: FirebaseAuth

    var PREFS_KEY = "prefs"
    var USER_ID_KEY = "user_id"
    var USER_NAME_KEY = "user_name"
    var USER_KEY = "user"
    var PWD_KEY = "pwd"

    var usr = ""
    var pwd = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        EmailEditText = findViewById(R.id.etEmail)
        passwordEditText = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.bSubmitLogin)
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        auth = FirebaseAuth.getInstance()

        usr = sharedPreferences.getString(USER_KEY, "").toString()
        pwd = sharedPreferences.getString(PWD_KEY, "").toString()

        val forgotPasswordTextView = findViewById<TextView>(R.id.tvForgotPassword)
        forgotPasswordTextView.setOnClickListener {
            val email = EmailEditText.text.toString().trim()

            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password reset link sent to your email", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to send password reset link", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
            }
        }

        loginButton.setOnClickListener {
            val email = EmailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (isValidCredentials(email, password)) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidCredentials(username: String, password: String): Boolean {
        return username.isNotEmpty() && password.isNotEmpty()
    }

    private fun loginUser(email: String, password: String) {

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    // Save user ID and name in SharedPreferences
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putString(USER_ID_KEY, user?.uid)
                    editor.putString(USER_NAME_KEY, user?.displayName)
                    editor.putString(USER_KEY, email)
                    editor.putString(PWD_KEY, password)
                    editor.apply()

                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun goToMain(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
