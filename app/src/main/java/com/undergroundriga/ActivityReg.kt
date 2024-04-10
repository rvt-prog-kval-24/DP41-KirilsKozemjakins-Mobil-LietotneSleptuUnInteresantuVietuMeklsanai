package com.undergroundriga

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast


class ActivityReg : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordRepeatEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var regButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg)

        // Initialize your views
        usernameEditText = findViewById(R.id.etUsername)
        passwordEditText = findViewById(R.id.etPassword)
        passwordRepeatEditText = findViewById(R.id.etRepeatPassword)
        emailEditText = findViewById(R.id.etEmail)
        regButton = findViewById(R.id.bSubmitReg)

        // Set the click listener for the registration button
        regButton.setOnClickListener {
            handleRegistration()
        }
    }

    private fun handleRegistration() {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val email = emailEditText.text.toString()
        val repeatPassword = passwordRepeatEditText.text.toString()
        val role = "0"

        if (username.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty() && repeatPassword.isNotEmpty()) {
            // Check if username or email already exists
            val db = DataBaseHandler(this)
            if (db.isUsernameExists(username)) {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
            } else if (db.isEmailExists(email)) {
                Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show()
            } else {
                // Insert the new user if username and email are unique
                val user = User(username, password, email, role)
                db.insertData(user)

                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }
}
