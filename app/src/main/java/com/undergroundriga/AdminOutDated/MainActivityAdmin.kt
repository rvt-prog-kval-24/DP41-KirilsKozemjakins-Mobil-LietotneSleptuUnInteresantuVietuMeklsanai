package com.undergroundriga

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivityAdmin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_admin)
    }

    fun goToUsers(view: View) {
        val intent = Intent(this, UsersActivityAdmin::class.java)
        startActivity(intent)
        finish()
    }

    fun goToMaps(view: View) {
        val intent = Intent(this, MapsActivityAdmin::class.java)
        startActivity(intent)
        finish()
    }

    fun goToMain(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun goToMapsEdit(view: View) {
        val intent = Intent(this, ActivityMapsEditData::class.java)
        startActivity(intent)
        finish()
    }

    fun goToMainAdmin(view: View) {
        val intent = Intent(this, MainActivityAdmin::class.java)
        startActivity(intent)
        finish()
    }
}
