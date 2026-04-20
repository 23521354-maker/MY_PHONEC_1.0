package com.example.myphonec

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LeaderboardRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun getTopScores(): Flow<List<PhoneRankItem>> = callbackFlow {
        val query = firestore.collection("leaderboard_scores")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(10)
        
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
                    score = doc.getLong("score")?.toInt() ?: 0,
                    fps = doc.getLong("fps")?.toInt() ?: 0,
                    userName = doc.getString("userName")
                )
            } ?: emptyList()
            
            trySend(items).isSuccess
        }
        
        awaitClose { subscription.remove() }
    }
}
