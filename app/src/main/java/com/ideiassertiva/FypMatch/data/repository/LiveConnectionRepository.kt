package com.ideiassertiva.FypMatch.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ideiassertiva.FypMatch.model.ConnectionEvent
import com.ideiassertiva.FypMatch.model.LiveConnection
import kotlinx.coroutines.tasks.await
import java.util.UUID

class LiveConnectionRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val connectionsCollection = firestore.collection("LiveConnections")

    suspend fun getOrCreateLiveConnection(matchId: String, user1Id: String, user2Id: String): LiveConnection {
        // Tenta buscar conexão ativa para esse match
        val snapshot = connectionsCollection
            .whereEqualTo("matchId", matchId)
            .get()
            .await()

        if (!snapshot.isEmpty) {
            return snapshot.documents.first().toObject(LiveConnection::class.java)!!
        }

        // Se não existir, cria uma nova
        val newConnection = LiveConnection(
            id = UUID.randomUUID().toString(),
            matchId = matchId,
            user1Id = user1Id,
            user2Id = user2Id
        )

        connectionsCollection.document(newConnection.id).set(newConnection).await()
        return newConnection
    }

    suspend fun logConnectionEvent(liveConnectionId: String, event: ConnectionEvent) {
        val eventWithId = if (event.id.isBlank()) {
            event.copy(id = UUID.randomUUID().toString(), liveConnectionId = liveConnectionId)
        } else {
            event.copy(liveConnectionId = liveConnectionId)
        }

        connectionsCollection
            .document(liveConnectionId)
            .collection("Events")
            .document(eventWithId.id)
            .set(eventWithId)
            .await()
            
        // O LiveConnectionEngine escutará essa coleção de eventos ou será chamado em seguida
    }

    suspend fun getLiveConnection(liveConnectionId: String): LiveConnection? {
        val doc = connectionsCollection.document(liveConnectionId).get().await()
        return doc.toObject(LiveConnection::class.java)
    }
    
    suspend fun updateConnectionScore(liveConnectionId: String, updatedConnection: LiveConnection) {
        connectionsCollection.document(liveConnectionId)
            .set(updatedConnection, SetOptions.merge())
            .await()
    }
}
