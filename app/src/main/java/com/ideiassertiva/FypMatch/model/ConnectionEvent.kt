package com.ideiassertiva.FypMatch.model

import java.util.Date

data class ConnectionEvent(
    val id: String = "",
    val liveConnectionId: String = "",
    val initiatorUserId: String = "",
    val type: ConnectionEventType = ConnectionEventType.MESSAGE_SENT,
    val value: Float = 1.0f, 
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: Date = Date()
)

enum class ConnectionEventType {
    MESSAGE_SENT,
    REACTION_ADDED,
    MEDIA_SHARED,
    GAME_PLAYED,
    VOICE_NOTE_SENT,
    CONVERSATION_INITIATED 
}
