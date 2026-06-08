package com.ideiassertiva.FypMatch.domain

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MigrationWorker {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun migrateOldMatches() {
        val matches = firestore.collection("Matches").get().await()
        for (match in matches) {
            val messageCount = firestore.collection("Messages")
                                        .whereEqualTo("matchId", match.id)
                                        .get().await().size()
            
            val status = when {
                messageCount > 100 -> "ON_FIRE"
                messageCount > 50 -> "ACTIVE"
                else -> "WARMING_UP"
            }
            
            firestore.collection("Matches").document(match.id)
                .update("connectionStatus", status, "connectionScore", if (messageCount > 100) 85f else 55f)
        }
    }
}
