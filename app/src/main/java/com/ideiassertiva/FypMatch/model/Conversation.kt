package com.ideiassertiva.FypMatch.model

import java.time.LocalDateTime

enum class ConversationStatus {
    ACTIVE,
    ARCHIVED,
    BLOCKED,
    DELETED
}

data class TypingIndicator(
    val userId: String,
    val isTyping: Boolean,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class ConversationParticipant(
    val userId: String,
    val joinedAt: LocalDateTime,
    val lastSeenAt: LocalDateTime? = null,
    val isOnline: Boolean = false,
    val notificationsEnabled: Boolean = true
)

data class Conversation(
    val id: String = "",
    val matchId: String = "",
    val participantIds: List<String> = emptyList(), // Para queries do Firestore
    val participants: List<ConversationParticipant> = emptyList(),
    val status: ConversationStatus = ConversationStatus.ACTIVE,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastMessageAt: LocalDateTime? = null,
    val lastMessage: Message? = null,
    val lastMessageContent: String = "",
    val lastMessageSenderId: String = "",
    val unreadCount: Map<String, Int> = emptyMap(), // userId -> unread count
    val typingIndicators: List<TypingIndicator> = emptyList(),
    val pinnedMessages: List<String> = emptyList(), // Message IDs
    val muteUntil: LocalDateTime? = null
) {
    fun getOtherParticipant(currentUserId: String): ConversationParticipant? {
        return participants.find { it.userId != currentUserId }
    }
    
    fun getUnreadCount(userId: String): Int {
        return unreadCount[userId] ?: 0
    }
    
    fun isOtherUserOnline(currentUserId: String): Boolean {
        return getOtherParticipant(currentUserId)?.isOnline ?: false
    }
    
    fun isOtherUserTyping(currentUserId: String): Boolean {
        val otherUserId = getOtherParticipant(currentUserId)?.userId ?: return false
        return typingIndicators.any { 
            it.userId == otherUserId && 
            it.isTyping && 
            it.timestamp.isAfter(LocalDateTime.now().minusSeconds(10)) 
        }
    }
    
    fun getLastSeenFormatted(currentUserId: String): String {
        val otherParticipant = getOtherParticipant(currentUserId)
        return when {
            otherParticipant?.isOnline == true -> "Online"
            otherParticipant?.lastSeenAt != null -> {
                val minutes = java.time.Duration.between(
                    otherParticipant.lastSeenAt, 
                    LocalDateTime.now()
                ).toMinutes()
                
                when {
                    minutes < 1 -> "Agora há pouco"
                    minutes < 60 -> "Visto há ${minutes}min"
                    minutes < 1440 -> "Visto há ${minutes / 60}h"
                    else -> "Visto há ${minutes / 1440} dias"
                }
            }
            else -> "Offline"
        }
    }
    
    fun isMuted(): Boolean {
        return muteUntil?.isAfter(LocalDateTime.now()) ?: false
    }
} 
