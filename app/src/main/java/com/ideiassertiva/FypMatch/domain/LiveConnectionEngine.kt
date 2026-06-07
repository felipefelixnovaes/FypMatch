package com.ideiassertiva.FypMatch.domain

import com.ideiassertiva.FypMatch.data.repository.LiveConnectionRepository
import com.ideiassertiva.FypMatch.model.ConnectionEvent
import com.ideiassertiva.FypMatch.model.ConnectionEventType
import com.ideiassertiva.FypMatch.model.LiveConnection
import com.ideiassertiva.FypMatch.model.ConnectionStatus
import java.util.Date
import kotlin.math.min

class LiveConnectionEngine(
    private val repository: LiveConnectionRepository
) {
    suspend fun processNewEvent(liveConnectionId: String, newEvent: ConnectionEvent) {
        val currentConnection = repository.getLiveConnection(liveConnectionId) ?: return
        
        // Registrar o evento base
        repository.logConnectionEvent(liveConnectionId, newEvent)

        // Calcular novos pesos baseados no evento e regras de negócio
        var newAffinity = currentConnection.dimensions.affinity
        var newLightness = currentConnection.dimensions.lightness
        var newContinuity = currentConnection.dimensions.continuity
        
        when (newEvent.type) {
            ConnectionEventType.GAME_PLAYED -> newAffinity = min(100f, newAffinity + 5f)
            ConnectionEventType.MEDIA_SHARED -> newLightness = min(100f, newLightness + 2f)
            ConnectionEventType.REACTION_ADDED -> newLightness = min(100f, newLightness + 1f)
            ConnectionEventType.CONVERSATION_INITIATED -> newContinuity = min(100f, newContinuity + 5f)
            else -> { /* Outros eventos não afetam dimensões diretamente agora */ }
        }

        // Calcula a pontuação geral baseada nas dimensões (simplificado para MVP)
        val newOverallScore = ((newAffinity * 2) + newLightness + newContinuity) / 4f

        val newStatus = when {
            newOverallScore > 80f -> ConnectionStatus.ON_FIRE
            newOverallScore > 50f -> ConnectionStatus.ACTIVE
            newOverallScore > 20f -> ConnectionStatus.WARMING_UP
            else -> ConnectionStatus.ICE_COLD
        }

        // Atualizar o LiveConnection com os novos cálculos
        val updatedDimensions = currentConnection.dimensions.copy(
            affinity = newAffinity,
            lightness = newLightness,
            continuity = newContinuity
        )

        val updatedConnection = currentConnection.copy(
            overallScore = min(100f, newOverallScore),
            status = newStatus,
            dimensions = updatedDimensions,
            lastInteractionAt = Date(),
            updatedAt = Date()
        )

        repository.updateConnectionScore(liveConnectionId, updatedConnection)
    }
}
