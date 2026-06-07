package com.ideiassertiva.FypMatch.model

import java.util.Date

data class LiveConnection(
    val id: String = "",
    val matchId: String = "",
    val conversationId: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val overallScore: Float = 0f, 
    val status: ConnectionStatus = ConnectionStatus.WARMING_UP,
    val dimensions: ConnectionDimensions = ConnectionDimensions(),
    val streakDays: Int = 0,
    val lastInteractionAt: Date = Date(),
    val updatedAt: Date = Date()
)

data class ConnectionDimensions(
    val reciprocity: Float = 0f,  
    val continuity: Float = 0f,   
    val affinity: Float = 0f,     
    val lightness: Float = 0f,    
    val depth: Float = 0f,        
    val initiative: Float = 0f    
)

enum class ConnectionStatus {
    ICE_COLD,
    COOLING_DOWN,
    WARMING_UP,
    ACTIVE,
    ON_FIRE
}
