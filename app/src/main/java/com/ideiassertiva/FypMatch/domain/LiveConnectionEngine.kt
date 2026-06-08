package com.ideiassertiva.FypMatch.domain

import com.ideiassertiva.FypMatch.data.repository.LiveConnectionDataSource
import com.ideiassertiva.FypMatch.model.ConnectionEvent
import com.ideiassertiva.FypMatch.model.ConnectionEventType
import com.ideiassertiva.FypMatch.model.ConnectionStatus
import java.util.Date
import javax.inject.Inject
import kotlin.math.min

class LiveConnectionEngine @Inject constructor(
    private val repository: LiveConnectionDataSource
) {
    suspend fun processNewEvent(liveConnectionId: String, newEvent: ConnectionEvent) {
        val currentConnection = repository.getLiveConnection(liveConnectionId) ?: return
        
        repository.logConnectionEvent(liveConnectionId, newEvent)

        var newReciprocity = currentConnection.dimensions.reciprocity
        var newContinuity = currentConnection.dimensions.continuity
        var newAffinity = currentConnection.dimensions.affinity
        var newLightness = currentConnection.dimensions.lightness
        var newDepth = currentConnection.dimensions.depth
        var newInitiative = currentConnection.dimensions.initiative
        
        when (newEvent.type) {
            ConnectionEventType.MESSAGE_SENT -> {
                newContinuity = min(100f, newContinuity + 1.5f)
                newInitiative = min(100f, newInitiative + 1f)
            }
            ConnectionEventType.GAME_PLAYED -> {
                newAffinity = min(100f, newAffinity + 5f)
                newLightness = min(100f, newLightness + 2f)
                newDepth = min(100f, newDepth + 1f)
            }
            ConnectionEventType.MEDIA_SHARED -> newLightness = min(100f, newLightness + 2f)
            ConnectionEventType.REACTION_ADDED -> {
                newLightness = min(100f, newLightness + 1f)
                newReciprocity = min(100f, newReciprocity + 1f)
            }
            ConnectionEventType.VOICE_NOTE_SENT -> {
                newDepth = min(100f, newDepth + 2f)
                newContinuity = min(100f, newContinuity + 1f)
            }
            ConnectionEventType.CONVERSATION_INITIATED -> newContinuity = min(100f, newContinuity + 5f)
        }

        val newOverallScore = listOf(
            newReciprocity,
            newContinuity,
            newAffinity,
            newLightness,
            newDepth,
            newInitiative
        ).average().toFloat()

        val newStatus = when {
            newOverallScore > 80f -> ConnectionStatus.ON_FIRE
            newOverallScore > 50f -> ConnectionStatus.ACTIVE
            newOverallScore > 15f -> ConnectionStatus.WARMING_UP
            else -> ConnectionStatus.ICE_COLD
        }

        val updatedDimensions = currentConnection.dimensions.copy(
            reciprocity = newReciprocity,
            continuity = newContinuity,
            affinity = newAffinity,
            lightness = newLightness,
            depth = newDepth,
            initiative = newInitiative
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
