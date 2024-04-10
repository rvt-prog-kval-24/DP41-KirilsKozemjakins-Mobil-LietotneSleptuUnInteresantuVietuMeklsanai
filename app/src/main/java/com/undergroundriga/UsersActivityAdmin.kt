package com.undergroundriga

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast


class UsersActivityAdmin: AppCompatActivity() {

    private lateinit var btn_insert: Button
    private lateinit var btn_delete: Button
    private lateinit var btn_update: Button
    private lateinit var btn_read: Button
    private lateinit var etUserId: EditText

    private lateinit var etPassword: EditText
    private lateinit var etUsername: EditText
    private lateinit var etRole: EditText
    private lateinit var etEmail: EditText

    private lateinit var tvResult: TextView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_admin)

        val context = this
        var db = DataBaseHandler(context)

        // Initialize your views by finding them with their IDs
        btn_insert = findViewById(R.id.btn_insert)
        btn_delete = findViewById(R.id.btn_delete)
        btn_update = findViewById(R.id.btn_update)
        btn_read = findViewById(R.id.btn_read)
        etUserId = findViewById(R.id.etUserId)

        etPassword = findViewById(R.id.etPassword)
        etUsername = findViewById(R.id.etUsername)
        etRole = findViewById(R.id.etRole)
        etEmail = findViewById(R.id.etEmail)

        tvResult = findViewById(R.id.tvResult)

        btn_insert.setOnClickListener({
            if (etUsername.text.toString().length > 0 &&
                etPassword.text.toString().length > 0 &&
                etRole.text.toString().length > 0) {
                var user = User(etUsername.text.toString(),etPassword.text.toString(),etEmail.text.toString(),etRole.text.toString())
                db.insertData(user)
            } else {
                Toast.makeText(context,"Please Fill All Data's",Toast.LENGTH_SHORT).show()
            }
        })

        btn_read.setOnClickListener({
            var data = db.readDataUsers()
            tvResult.text = ""
            for (i in 0..(data.size - 1)) {
                tvResult.append(data.get(i).id.toString() + " " + data.get(i).username + " " + data.get(i).password + " " + data.get(i).email + " " + data.get(i).role + "\n")
            }
        })

        btn_update.setOnClickListener({
            val UserIdText = etUserId.text.toString()
            if (UserIdText.isNotEmpty() && etUsername.text.toString().length > 0 &&
                etPassword.text.toString().length > 0 &&
                etRole.text.toString().length > 0 &&
                etEmail.text.toString().length > 0) {
                val UserId = UserIdText.toInt()
                db.updateUserData(UserId,etUsername.text.toString(),etPassword.text.toString(),etEmail.text.toString(),etRole.text.toString())
                btn_read.performClick()
            } else {
                Toast.makeText(context, "Please enter a valid User ID to delete", Toast.LENGTH_SHORT).show()
            }
        })

        btn_delete.setOnClickListener({
            val UserIdText = etUserId.text.toString()
            if (UserIdText.isNotEmpty()) {
                val UserId = UserIdText.toInt()
                db.deleteUserData(UserId)
                btn_read.performClick()
            } else {
                Toast.makeText(context, "Please enter a valid User ID to delete", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun goToMain(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}





