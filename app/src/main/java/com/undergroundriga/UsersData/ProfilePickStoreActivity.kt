package com.undergroundriga

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.view.View
import com.google.firebase.firestore.FirebaseFirestore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.undergroundriga.MapsActivity
import com.undergroundriga.R

class ProfilePickStoreActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var profileAdapter: ProfilePictureAdapter

    private lateinit var profileRecyclerView: RecyclerView
    private lateinit var backButton: ImageView  // Change type to ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_pick_store)

        firestore = FirebaseFirestore.getInstance()
        profileAdapter = ProfilePictureAdapter(this)
        profileRecyclerView = findViewById(R.id.profile_recycler_view)
        profileRecyclerView.layoutManager = GridLayoutManager(this, 3)
        profileRecyclerView.adapter = profileAdapter

        backButton = findViewById(R.id.button)
        backButton.setOnClickListener { goBackToProf() }  // Set onClickListener correctly

        fetchProfilePictures()
    }

    private fun fetchProfilePictures() {
        firestore.collection("ProfPictures")
            .get()
            .addOnSuccessListener { documents ->
                val profilePictures = mutableListOf<ProfilePicture>()
                for (document in documents) {
                    val price = document.getLong("Price")?.toInt() ?: 0
                    val profPickURL = document.getString("ProfPickURL") ?: ""
                    val isSelected = document.getBoolean("isSelected") ?: false
                    profilePictures.add(ProfilePicture(price, profPickURL, isSelected))
                }
                profileAdapter.setData(profilePictures)
            }
    }

    private fun goBackToProf() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }
}

data class ProfilePicture(val price: Int, val profPickURL: String, val isSelected: Boolean)

class ProfilePictureAdapter(private val context: ProfilePickStoreActivity) :
    RecyclerView.Adapter<ProfilePictureAdapter.ViewHolder>() {

    private var profilePictures = listOf<ProfilePicture>()

    fun setData(data: List<ProfilePicture>) {
        profilePictures = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.profile_picture_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profilePicture = profilePictures[position]
        Glide.with(context).load(profilePicture.profPickURL).into(holder.imageView)
        holder.borderView.isSelected = profilePicture.isSelected
        holder.itemView.setOnClickListener {
            setSelected(position)
        }
    }

    override fun getItemCount(): Int = profilePictures.size

    private fun setSelected(position: Int) {
        val newPictures = profilePictures.toMutableList()
        for (i in profilePictures.indices) {
            newPictures[i] = newPictures[i].copy(isSelected = i == position)
        }
        profilePictures = newPictures
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.profile_picture_image)
        val borderView: View = itemView.findViewById(R.id.profile_picture_border)
    }
}
