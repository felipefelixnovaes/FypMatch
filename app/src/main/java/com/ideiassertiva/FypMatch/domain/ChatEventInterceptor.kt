package com.ideiassertiva.FypMatch.domain

import com.ideiassertiva.FypMatch.data.repository.LiveConnectionRepository
import com.ideiassertiva.FypMatch.model.ConnectionEvent
import com.ideiassertiva.FypMatch.model.ConnectionEventType

// Interceptador para plugar no ChatRepository sem quebrar a lógica atual
class ChatEventInterceptor(
    private val liveConnectionRepository: LiveConnectionRepository,
    private val engine: LiveConnectionEngine
) {
    suspend fun onMessageSent(matchId: String, senderId: String, liveConnectionId: String) {
        val event = ConnectionEvent(
            liveConnectionId = liveConnectionId,
            initiatorUserId = senderId,
            type = ConnectionEventType.MESSAGE_SENT
        )
        engine.processNewEvent(liveConnectionId, event)
    }
    
    suspend fun onMediaShared(matchId: String, senderId: String, liveConnectionId: String) {
        val event = ConnectionEvent(
            liveConnectionId = liveConnectionId,
            initiatorUserId = senderId,
            type = ConnectionEventType.MEDIA_SHARED
        )
        engine.processNewEvent(liveConnectionId, event)
    }
}
