package com.example.myphonec

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository for Firestore operations.
 * Optimized to ensure Firebase initialization via Application class is respected.
 */
class FirebaseRepository {
    
    // Using lazy to ensure Firestore is only accessed after MyApplication's onCreate
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    suspend fun saveOrUpdateUser(uid: String, name: String, email: String, photoUrl: String?) {
        val userMap = hashMapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "photoUrl" to photoUrl,
            "createdAt" to FieldValue.serverTimestamp()
        )
        
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

    // --- PC Hardware Methods ---

    suspend fun getCPUs(): List<CPU> {
        return try {
            firestore.collection("cpus")
                .orderBy("name")
                .get()
                .await()
                .toObjects(CPU::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getGPUs(): List<GPU> {
        return try {
            firestore.collection("gpus")
                .orderBy("name")
                .get()
                .await()
                .toObjects(GPU::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMotherboards(): List<Motherboard> {
        return try {
            firestore.collection("motherboards")
                .orderBy("name")
                .get()
                .await()
                .toObjects(Motherboard::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRAM(): List<RAM> {
        return try {
            firestore.collection("ram")
                .orderBy("name")
                .get()
                .await()
                .toObjects(RAM::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPSUs(): List<PSU> {
        return try {
            firestore.collection("psu")
                .orderBy("name")
                .get()
                .await()
                .toObjects(PSU::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun bulkImportCPUs(cpus: List<CPU>) {
        val batch = firestore.batch()
        cpus.forEach { cpu ->
            val ref = firestore.collection("cpus").document(cpu.id.ifBlank { cpu.name.replace(" ", "_").lowercase() })
            batch.set(ref, cpu)
        }
        batch.commit().await()
    }

    suspend fun bulkImportGPUs(gpus: List<GPU>) {
        val batch = firestore.batch()
        gpus.forEach { gpu ->
            val ref = firestore.collection("gpus").document(gpu.id.ifBlank { gpu.name.replace(" ", "_").lowercase() })
            batch.set(ref, gpu)
        }
        batch.commit().await()
    }

    suspend fun clearCollection(collectionName: String) {
        val snapshot = firestore.collection(collectionName).get().await()
        val batch = firestore.batch()
        snapshot.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    // --- Benchmark Methods ---

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

        firestore.collection("user_benchmarks").add(benchmarkData).await()

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
