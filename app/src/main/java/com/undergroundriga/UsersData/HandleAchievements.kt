// HandleAchievements.kt

package com.undergroundriga

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.*


object HandleAchievements {

    private val db = FirebaseFirestore.getInstance()

    // Function to update a specific field in UserStats for a user
// Function to update a specific field in UserStats for a user
    fun updateUserStatsField(userID: String, fieldName: String, increment: Int) {
        val userStatsRef = db.collection("UserStats").whereEqualTo("UserID", userID)

        userStatsRef.get()
            .addOnSuccessListener { documentSnapshot ->

                val documentSnapshot = documentSnapshot.documents[0]
                val currentValue = documentSnapshot.getLong(fieldName) ?: 0
                val updatedValue = currentValue + increment

                documentSnapshot.reference.update(fieldName, updatedValue)
                    .addOnSuccessListener {
                        Log.d("FirestoreHelper", "$fieldName updated successfully!")
                        // Assuming we always want to check and update achievements after any field update

                        checkAndUpdateAchievements(userID, updatedValue, fieldName)
                    }
                    .addOnFailureListener { e ->
                        Log.w("FirestoreHelper", "Error updating $fieldName", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreHelper", "Error fetching user stats${userStatsRef}", e)
            }
    }


// Function to check for achievements and update user balance
    fun checkAndUpdateAchievements(userID: String, updatedStatsField: Long, statsFieldTitle: String) {
        val achievementsRef = FirebaseFirestore.getInstance().collection("Achievements")

        GlobalScope.launch {
            try {
                val completedAchievementIds = getCompletedAchievementIdsForUser(userID)

                val achievementsSnapshot = achievementsRef.get().await()

                for (achievementDoc in achievementsSnapshot) {
                    val conditionVariable = achievementDoc.getString("ConditionVariable")
                    val requiredValue = achievementDoc.getLong("RequiredValue") ?: 0L
                    val reward = achievementDoc.getLong("Reward") ?: 0L
                    val achievementId = achievementDoc.id

                    if (conditionVariable == statsFieldTitle && updatedStatsField >= requiredValue &&
                        !completedAchievementIds.contains(achievementId)) {
                        updateUserBalance(userID, reward)
                        addUserCompletedAchievement(userID, achievementId)
                        break
                    }
                }
            } catch (e: Exception) {
                Log.w("FirestoreHelper", "Error fetching completed achievements for user $userID", e)
            }
        }

    }


    // Function to retrieve completed achievement IDs for a user
    suspend fun getCompletedAchievementIdsForUser(userID: String): List<String> {
        val completedAchievementIds = mutableListOf<String>()
        val completedAchievementsRef = FirebaseFirestore.getInstance()
            .collection("Users").document(userID).collection("CompletedAchievements")

        try {
            val documentsSnapshot = completedAchievementsRef.get().await()
            for (documentSnapshot in documentsSnapshot) {
                val achievementId = documentSnapshot.getString("AchievementID")
                if (achievementId != null) {
                    completedAchievementIds.add(achievementId)
                }
            }
        } catch (e: Exception) {
            Log.w("FirestoreHelper", "Error fetching completed achievements for user $userID", e)
        }

        return completedAchievementIds
    }

    // Function to update user balance
    fun updateUserBalance(userID: String, reward: Long) {
        val userBalanceRef = db.collection("UserBalance").whereEqualTo("UserID", userID)

        userBalanceRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val documentSnapshot = documentSnapshot.documents[0]
                val currentBalance = documentSnapshot.getLong("Balance") ?: 0L
                val updatedBalance = currentBalance + reward

                documentSnapshot.reference.update("Balance", updatedBalance)
                    .addOnSuccessListener {
                        Log.d("FirestoreHelper", "User balance updated successfully! New balance: $updatedBalance")
                    }
                    .addOnFailureListener { e ->
                        Log.w("FirestoreHelper", "Error updating user balance", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreHelper", "Error fetching user balance", e)
            }
    }

    // Function to add completed achievement for a user
    fun addUserCompletedAchievement(userID: String, achievementId: String) {
        val userRef = db.collection("Users").document(userID)
        val completedAchievements = userRef.collection("CompletedAchievements")

        completedAchievements.add(hashMapOf("AchievementID" to achievementId))
            .addOnSuccessListener { documentReference ->
                Log.d("FirestoreHelper", "Achievement $achievementId added to completed achievements for user $userID")
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreHelper", "Error adding achievement to completed list for user $userID", e)
            }
    }
}
