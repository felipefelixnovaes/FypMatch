package com.ideiassertiva.FypMatch.domain

import com.google.firebase.firestore.FirebaseFirestore
import com.ideiassertiva.FypMatch.data.repository.LiveConnectionRepository
import com.ideiassertiva.FypMatch.model.ConnectionStatus
import kotlinx.coroutines.tasks.await

class ConnectionMigrationWorker(
    private val firestore: FirebaseFirestore,
    private val liveConnectionRepository: LiveConnectionRepository
) {
    /**
     * Inicializa a funcionalidade Conexão Viva para matches antigos com base na volumetria de mensagens.
     */
    suspend fun migrateExistingMatches() {
        // Busca todos os matches que não possuem connectionScore definido ou são WARMING_UP defaults antigos.
        // Em um app de produção com grande volume, seria feito via query com limit e pagination.
        val matchesSnapshot = firestore.collection("matches").get().await()
        
        for (doc in matchesSnapshot.documents) {
            val matchId = doc.id
            val user1Id = doc.getString("user1Id") ?: continue
            val user2Id = doc.getString("user2Id") ?: continue
            
            // Busca a conversationId deste match
            val conversationsSnapshot = firestore.collection("conversations")
                .whereEqualTo("matchId", matchId)
                .limit(1)
                .get()
                .await()
                
            if (conversationsSnapshot.isEmpty) continue
            val conversationId = conversationsSnapshot.documents.first().id
            
            // Conta mensagens
            val messagesSnapshot = firestore.collection("messages")
                .whereEqualTo("conversationId", conversationId)
                .get()
                .await()
                
            val messageCount = messagesSnapshot.size()
            
            // Obtém ou cria a conexão
            val connection = liveConnectionRepository.getOrCreateLiveConnection(matchId, user1Id, user2Id)
            
            // Define o novo status baseado na volumetria inicial
            var newScore = connection.overallScore
            var newStatus = connection.status
            
            if (messageCount > 100) {
                newScore = 85f
                newStatus = ConnectionStatus.ON_FIRE
            } else if (messageCount > 50) {
                newScore = 60f
                newStatus = ConnectionStatus.ACTIVE
            }
            
            if (newStatus != connection.status || newScore != connection.overallScore) {
                val updatedConnection = connection.copy(
                    overallScore = newScore,
                    status = newStatus
                )
                liveConnectionRepository.updateConnectionScore(connection.id, updatedConnection)
                
                // Opcional: Atualizar a modelagem do Match também para query de listagem.
                firestore.collection("matches").document(matchId)
                    .update(
                        mapOf(
                            "connectionScore" to newScore,
                            "connectionStatus" to newStatus.name
                        )
                    ).await()
            }
        }
    }
}
