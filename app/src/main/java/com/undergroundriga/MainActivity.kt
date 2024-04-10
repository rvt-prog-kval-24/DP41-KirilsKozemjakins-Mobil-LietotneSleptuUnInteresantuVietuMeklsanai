package com.undergroundriga

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


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
