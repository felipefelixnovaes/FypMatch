package com.ideiassertiva.FypMatch.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ideiassertiva.FypMatch.model.ConnectionEvent
import com.ideiassertiva.FypMatch.model.LiveConnection
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface LiveConnectionDataSource {
    suspend fun getOrCreateLiveConnection(matchId: String, user1Id: String, user2Id: String): LiveConnection
    suspend fun logConnectionEvent(liveConnectionId: String, event: ConnectionEvent)
    suspend fun getLiveConnection(liveConnectionId: String): LiveConnection?
    suspend fun updateConnectionScore(liveConnectionId: String, updatedConnection: LiveConnection)
}

@Singleton
class LiveConnectionRepository @Inject constructor(
    firestore: FirebaseFirestore
) : LiveConnectionDataSource {
    private val connectionsCollection = firestore.collection("liveConnections")

    override suspend fun getOrCreateLiveConnection(matchId: String, user1Id: String, user2Id: String): LiveConnection {
        val snapshot = connectionsCollection
            .whereEqualTo("matchId", matchId)
            .limit(1)
            .get()
            .await()

        if (!snapshot.isEmpty) {
            val document = snapshot.documents.first()
            return document.toObject(LiveConnection::class.java)?.copy(id = document.id)
                ?: error("LiveConnection document ${document.id} could not be parsed")
        }

        val newConnection = LiveConnection(
            id = UUID.randomUUID().toString(),
            matchId = matchId,
            user1Id = user1Id,
            user2Id = user2Id
        )

        connectionsCollection.document(newConnection.id).set(newConnection).await()
        return newConnection
    }

    override suspend fun logConnectionEvent(liveConnectionId: String, event: ConnectionEvent) {
        val eventWithId = if (event.id.isBlank()) {
            event.copy(id = UUID.randomUUID().toString(), liveConnectionId = liveConnectionId)
        } else {
            event.copy(liveConnectionId = liveConnectionId)
        }

        connectionsCollection
            .document(liveConnectionId)
            .collection("events")
            .document(eventWithId.id)
            .set(eventWithId)
            .await()
    }

    override suspend fun getLiveConnection(liveConnectionId: String): LiveConnection? {
        val doc = connectionsCollection.document(liveConnectionId).get().await()
        return doc.toObject(LiveConnection::class.java)?.copy(id = doc.id)
    }
    
    override suspend fun updateConnectionScore(liveConnectionId: String, updatedConnection: LiveConnection) {
        connectionsCollection.document(liveConnectionId)
            .set(updatedConnection, SetOptions.merge())
            .await()
    }
}
