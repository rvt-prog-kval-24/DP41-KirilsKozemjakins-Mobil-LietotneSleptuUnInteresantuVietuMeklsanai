package com.undergroundriga

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

private lateinit var firestore: FirebaseFirestore
private lateinit var profileAdapter: ProfilePictureAdapter
private lateinit var profileRecyclerView: RecyclerView
private lateinit var auth: FirebaseAuth

data class ProfilePicture(
    val id: String, // Document ID of the ProfPictures document
    val price: Int,
    val profPickURL: String,
    val title: String,
    val isSelected: Boolean,
    val isPurchased: Boolean
)

class ProfilePictureAdapter(private val context: ProfilePickStoreActivity) : RecyclerView.Adapter<ProfilePictureAdapter.ViewHolder>() {
    var profilePictures = listOf<ProfilePicture>()
    var currentPickID: String? = null // Current selected profile picture ID

    fun setData(data: List<ProfilePicture>, currentPickID: String?) {
        profilePictures = data
        this.currentPickID = currentPickID
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.profile_picture_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profilePicture = profilePictures[position]
        Glide.with(context).load(profilePicture.profPickURL).into(holder.imageView)

        val isSelected = profilePicture.id == currentPickID

        if (isSelected) {
            holder.borderView.setBackgroundResource(R.drawable.profile_current_picture_border)
        } else {
            holder.borderView.setBackgroundResource(R.drawable.profile_picture_border)
        }



        if (profilePicture.isPurchased) {

            holder.imageView.setOnClickListener {
                // Call a function to handle the click
                handlePurchasedPictureClick(position)
            }
            // Profile picture is purchased
            holder.overlayView.visibility = View.GONE
        } else {

            holder.imageView.setOnClickListener {
                // Call a function to handle the click
                context.handlePictureClick(position)
            }
            // Profile picture is not purchased
            holder.overlayView.visibility = View.VISIBLE
        }
    }



    private fun showSelectDialog(profilePicture: ProfilePicture) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_select_dialog, null)
        val dialogImage = dialogView.findViewById<ImageView>(R.id.dialog_image)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
        val dialogPrice = dialogView.findViewById<TextView>(R.id.dialog_price)

        // Set profile picture details
        Glide.with(context).load(profilePicture.profPickURL).into(dialogImage)
        dialogTitle.text = profilePicture.title
        dialogPrice.text = "Price: ${profilePicture.price}"

        val positiveButton = dialogView.findViewById<Button>(R.id.select_button)
        val negativeButton = dialogView.findViewById<Button>(R.id.negative_button)

        val builder = AlertDialog.Builder(context)
        val dialog = builder.setView(dialogView).create()

        positiveButton.setOnClickListener {
            // Set CurrentPickID in Users collection for current user



            updateCurrentPickID(profilePicture.id)
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun handlePurchasedPictureClick(position: Int) {
        val selectedPicture = profileAdapter.profilePictures[position]
        showSelectDialog(selectedPicture)
    }

    private fun updateCurrentPickID(pictureId: String) {

        // Update CurrentPickID in Users collection for current user
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = firestore.collection("Users").document(userId)
        userRef.update("CurrentPickID", pictureId)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile picture selected successfully!", Toast.LENGTH_SHORT).show()
                context.refreshData() // Refresh data after updating profile picture
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to select profile picture!", Toast.LENGTH_SHORT).show()
            }
    }



    override fun getItemCount(): Int = profilePictures.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.profile_picture_image)
        val overlayView: View = itemView.findViewById(R.id.overlay_view)
        val borderView: View = itemView.findViewById(R.id.profile_picture_border)
    }
}


class ProfilePickStoreActivity : AppCompatActivity() {



    fun refreshData() {
        val intent = Intent(this, ProfilePickStoreActivity::class.java)
        startActivity(intent)
        finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_pick_store)

        firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        profileAdapter = ProfilePictureAdapter(this)
        profileRecyclerView = findViewById(R.id.profile_recycler_view)
        profileRecyclerView.layoutManager = GridLayoutManager(this, 3)
        profileRecyclerView.adapter = profileAdapter

        // button should be defined in your XML layout file
        // button.setOnClickListener { goBackToProf() }

        fetchProfilePictures()
    }

    fun fetchProfilePictures() {
        val auth = FirebaseAuth.getInstance()
        // Get the IDs of purchased profile pictures
        val userId = auth.currentUser?.uid
        val purchasedPicturesRef = userId?.let {
            firestore.collection("Users").document(it).collection("PurchasedPicks")
        }

        purchasedPicturesRef?.get()
            ?.addOnSuccessListener { purchasedDocuments ->
                // Extract IDs of purchased profile pictures
                val purchasedPictureIds = purchasedDocuments.map { it.getString("ProfPickID") }

                // Fetch all profile pictures
                firestore.collection("ProfPictures")
                    .get()
                    .addOnSuccessListener { documents ->
                        val profilePictures = mutableListOf<ProfilePicture>()
                        for (document in documents) {
                            val id = document.id
                            val price = document.getLong("Price")?.toInt() ?: 0
                            val profPickURL = document.getString("ProfPickURL") ?: ""
                            val title = document.getString("Title") ?: ""
                            val isSelected = document.getBoolean("isSelected") ?: false
                            // Check if the profile picture ID is in the PurchasedPicks
                            val isPurchased = purchasedPictureIds.contains(id)
                            profilePictures.add(ProfilePicture(id, price, profPickURL, title, isSelected, isPurchased))
                        }
                        // Get the current selected profile picture ID from the user's data
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser != null) {
                            firestore.collection("Users").document(currentUser.uid)
                                .get()
                                .addOnSuccessListener { userDocument ->
                                    val currentPickID = userDocument.getString("CurrentPickID")
                                    profileAdapter.setData(profilePictures, currentPickID)
                                }
                        } else {
                            // If no user is logged in, just set data without currentPickID
                            profileAdapter.setData(profilePictures, null)
                        }
                    }
            }
    }



    fun handlePictureClick(position: Int) {
        val selectedPicture = profileAdapter.profilePictures[position]
        showPurchaseConfirmationDialog(selectedPicture)
    }






    private fun showPurchaseConfirmationDialog(picture: ProfilePicture) {

        auth = FirebaseAuth.getInstance()

        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_purchase_dialog, null)
        val dialogImage = dialogView.findViewById<ImageView>(R.id.dialog_image)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
        val dialogPrice = dialogView.findViewById<TextView>(R.id.dialog_price)

        // Set values
        Glide.with(this).load(picture.profPickURL).into(dialogImage)
        dialogTitle.text = picture.title
        dialogPrice.text = "Price: ${picture.price}"

        val positiveButton = dialogView.findViewById<Button>(R.id.positive_button)
        val negativeButton = dialogView.findViewById<Button>(R.id.negative_button)

        val builder = AlertDialog.Builder(this)
        val dialog = builder.setView(dialogView).create()

        positiveButton.setOnClickListener {
            // Implementation of purchase functionality
            purchaseProfilePicture(auth.currentUser!!.uid, picture)
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }




    private fun purchaseProfilePicture(userId: String, picture: ProfilePicture) {
        val price = picture.price

        checkUserBalance(userId, price,
            onSuccess = { balance ->
                // Sufficient balance, proceed with the purchase
                val newBalance = balance - price

                // Update user's balance
                firestore.collection("UserBalance").whereEqualTo("UserID", userId).get()
                    .addOnSuccessListener { documentSnapshot ->

                        val documentSnapshot = documentSnapshot.documents[0]
                        documentSnapshot.reference.update("Balance", newBalance)
                        // Add the purchased profile picture ID to the user's purchased picks
                        val purchasedPick = hashMapOf(
                            "ProfPickID" to picture.id // Storing only the ProfPictures document ID
                        )
                        firestore.collection("Users").document(userId).collection("PurchasedPicks")
                            .add(purchasedPick)
                            .addOnSuccessListener {

                                Toast.makeText(this, "Profile picture purchased successfully!", Toast.LENGTH_SHORT).show()

                                HandleAchievements.updateUserStatsField(userId, "ProfPickPurchased", 1)

                                val intent = Intent(this, ProfilePickStoreActivity::class.java)
                                startActivity(intent)
                                finish()


                            }
                            .addOnFailureListener {
                                // Failed to add purchased profile picture
                                Toast.makeText(this, "Failed to add purchased profile picture!", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        // Failed to update balance
                        Toast.makeText(this, "Failed to update balance!", Toast.LENGTH_SHORT).show()
                    }
            },
            onFailure = {
                // Insufficient balance
                Toast.makeText(this, "Insufficient balance to purchase this profile picture!", Toast.LENGTH_SHORT).show()
            }
        )
    }



    private fun checkUserBalance(userId: String, price: Int, onSuccess: (Long) -> Unit, onFailure: () -> Unit) {
        val userBalanceRef = firestore.collection("UserBalance").whereEqualTo("UserID", userId)
        userBalanceRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val documentSnapshot = documentSnapshot.documents[0]
                val balance = documentSnapshot.getLong("Balance") ?: 0
                if (balance >= price) {
                    onSuccess(balance)
                } else {
                    onFailure()
                }
            }
            .addOnFailureListener { onFailure() }
    }



    fun goBackToProf(view: View){
        val intent = Intent(this, UserProfileActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun goUpdate(view: View){
        val intent = Intent(this, ProfilePickStoreActivity::class.java)
        startActivity(intent)
        finish()
    }
}