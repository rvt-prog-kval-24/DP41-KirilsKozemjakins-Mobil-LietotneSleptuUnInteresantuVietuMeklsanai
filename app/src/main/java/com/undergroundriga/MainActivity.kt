package com.undergroundriga

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast


import android.util.Log
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase


private val TAG = "Main"
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = Firebase.firestore

        // Create a new user with a first and last name
        val user = hashMapOf(
            "username" to "Ada",
            "password" to "Lovelace",
            "email" to "test",
            "dateOfCreation" to "2024-04-29"
        )



// Add a new document with a generated ID
        db.collection("Users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Main test added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }


    }



    fun goToSignIn(view: View) {
        val intent = Intent(this, ActivityLogin::class.java)
        startActivity(intent)
    }

    fun goToSignUp(view: View) {
        val intent = Intent(this, ActivityReg::class.java)
        startActivity(intent)
    }
}
