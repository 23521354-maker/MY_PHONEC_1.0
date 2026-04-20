package com.example.myphonec

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun saveOrUpdateUser(uid: String, name: String, email: String, photoUrl: String?) {
        val userMap = hashMapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "photoUrl" to photoUrl,
            "createdAt" to FieldValue.serverTimestamp()
        )
        // Use merge to not overwrite createdAt if it exists, or handle it specifically
        // But the requirement says createdAt, so we can use set with merge for other fields
        // and only set createdAt if the document doesn't exist.
        
        val userRef = firestore.collection("users").document(uid)
        val snapshot = userRef.get().await()
        
        if (!snapshot.exists()) {
            userRef.set(userMap).await()
        } else {
            val updateMap = hashMapOf(
                "name" to name,
                "email" to email,
                "photoUrl" to photoUrl
            )
            userRef.update(updateMap as Map<String, Any>).await()
        }
    }

    suspend fun saveBenchmarkResult(
        uid: String, 
        userName: String,
        deviceModel: String, 
        chipset: String, 
        score: Int,
        fps: Int
    ) {
        val testedAt = FieldValue.serverTimestamp()
        val benchmarkData = hashMapOf(
            "uid" to uid,
            "userName" to userName,
            "deviceModel" to deviceModel,
            "chipset" to chipset,
            "score" to score,
            "fps" to fps,
            "testedAt" to testedAt
        )

        // 1. Save into user_benchmarks (History)
        firestore.collection("user_benchmarks").add(benchmarkData).await()

        // 2. Upload to leaderboard_scores (Best score per user+device)
        val deviceDocId = "${uid}_${deviceModel.replace(" ", "_")}"
        val leaderboardRef = firestore.collection("leaderboard_scores").document(deviceDocId)
        
        val existingDoc = leaderboardRef.get().await()
        if (!existingDoc.exists() || (existingDoc.getLong("score") ?: 0) < score.toLong()) {
            leaderboardRef.set(benchmarkData, SetOptions.merge()).await()
        }
    }

    fun getTopScores(): Flow<List<PhoneRankItem>> = callbackFlow {
        val query = firestore.collection("leaderboard_scores")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(20)
        
        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val items = snapshot?.documents?.mapIndexed { index, doc ->
                PhoneRankItem(
                    rank = index + 1,
                    name = doc.getString("deviceModel") ?: "Unknown",
                    chipset = doc.getString("chipset") ?: "Unknown",
                    score = doc.getLong("score")?.toInt() ?: 0
                )
            } ?: emptyList()
            
            trySend(items).isSuccess
        }
        
        awaitClose { subscription.remove() }
    }
}
