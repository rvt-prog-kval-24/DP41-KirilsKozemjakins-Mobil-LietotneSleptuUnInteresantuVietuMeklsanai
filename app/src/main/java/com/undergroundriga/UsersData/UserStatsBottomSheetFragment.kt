package com.undergroundriga

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore

class UserStatsBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var userStatsTextView: TextView
    private lateinit var firestore: FirebaseFirestore
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        userId = arguments?.getString("USER_ID")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_stats_bottom_sheet, container, false)
        userStatsTextView = view.findViewById(R.id.userStatsTextView)

        userId?.let {
            fetchUserStats(it)
        } ?: run {
            userStatsTextView.text = "User ID not found"
        }

        return view
    }

    private fun fetchUserStats(userId: String) {
        firestore.collection("UserStats").whereEqualTo("UserID", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val acceptedSubm = document.getLong("AcceptedSubm") ?: 0
                    val leaderboardMaxPos = document.getString("LeaderboardMaxPos") ?: "N/A"
                    val profPickPurchased = document.getLong("ProfPickPurchased") ?: 0
                    val submitedPlaces = document.getLong("SubmitedPlaces") ?: 0
                    val visitedPlaces = document.getLong("VisitedPlaces") ?: 0

                    userStatsTextView.text = """
                        Accepted Submissions: $acceptedSubm
                        Leaderboard Max Position: $leaderboardMaxPos
                        Profile Pics Purchased: $profPickPurchased
                        Submitted Places: $submitedPlaces
                        Visited Places: $visitedPlaces
                    """.trimIndent()
                } else {
                    userStatsTextView.text = "User Stats: N/A"
                }
            }
            .addOnFailureListener {
                userStatsTextView.text = "User Stats: N/A"
                Toast.makeText(context, "Failed to fetch user stats", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        fun newInstance(userId: String): UserStatsBottomSheetFragment {
            val fragment = UserStatsBottomSheetFragment()
            val args = Bundle()
            args.putString("USER_ID", userId)
            fragment.arguments = args
            return fragment
        }
    }
}
