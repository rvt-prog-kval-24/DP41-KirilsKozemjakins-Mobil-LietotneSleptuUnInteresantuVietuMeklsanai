package com.undergroundriga

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView

class ActivityCheckMySuggestions : AppCompatActivity() {

    lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_my_suggestions)

        listView = findViewById(R.id.listViewSuggestions)

        // Retrieve current user's ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId != -1) {
            val db = DataBaseHandler(this)
            val suggestions = db.getMySuggestions(userId)
            val suggestionNames = suggestions.map { it.PlaceName }
            val suggestionId = suggestions.map { it.PlacesId }
            val suggestionDesc = suggestions.map { it.Description }
            val suggestionTag = suggestions.map { it.Tag }
            val suggestionPosX = suggestions.map { it.PosX }
            val suggestionPosY = suggestions.map { it.PosY }

            val data = arrayOf(suggestionNames,suggestionId,suggestionDesc,suggestionTag,suggestionPosX,suggestionPosY)


            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data)
            listView.adapter = adapter
        }
    }

    fun goToMapsMain(view: View) {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }
}
