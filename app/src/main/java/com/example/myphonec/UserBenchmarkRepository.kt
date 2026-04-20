package com.example.myphonec

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class UserBenchmarkRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun getUserBenchmarks(uid: String): Flow<List<BenchmarkedDevice>> = callbackFlow {
        if (uid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val query = firestore.collection("user_benchmarks")
            .whereEqualTo("uid", uid)
            .orderBy("testedAt", Query.Direction.DESCENDING)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("UserBenchmarkRepo", "Firestore error: ${error.message}. Check if index is created.")
                // Instead of close(error) which might crash if unhandled, 
                // we send an empty list and keep the flow alive or close gracefully.
                trySend(emptyList())
                return@addSnapshotListener
            }

            val devices = snapshot?.documents?.mapNotNull { doc ->
                try {
                    BenchmarkedDevice(
                        id = doc.id,
                        uid = doc.getString("uid") ?: "",
                        deviceModel = doc.getString("deviceModel") ?: "",
                        chipset = doc.getString("chipset") ?: "",
                        score = doc.getLong("score")?.toInt() ?: 0,
                        fps = doc.getLong("fps")?.toInt() ?: 0,
                        testedAt = doc.getTimestamp("testedAt")?.toDate()
                    )
                } catch (e: Exception) {
                    Log.e("UserBenchmarkRepo", "Error parsing device doc", e)
                    null
                }
            } ?: emptyList()

            trySend(devices).isSuccess
        }

        awaitClose { subscription.remove() }
    }
}
