package com.ideiassertiva.FypMatch.model

import java.time.LocalDateTime

enum class MessageType {
    TEXT,
    IMAGE,
    AUDIO,
    VIDEO,
    LOCATION,
    GIF,
    STICKER,
    SYSTEM_INFO
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

data class MessageReaction(
    val emoji: String = "",
    val userId: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

data class MediaData(
    val url: String,
    val thumbnailUrl: String? = null,
    val duration: Long? = null, // Para áudio/vídeo em segundos
    val size: Long? = null // Tamanho do arquivo em bytes
)

data class Message(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "", // Texto da mensagem
    val type: MessageType = MessageType.TEXT,
    val status: MessageStatus = MessageStatus.SENDING,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val reactions: List<MessageReaction> = emptyList(),
    val mediaData: MediaData? = null,
    val locationData: LocationData? = null,
    val replyToMessageId: String? = null, // Para respostas
    val isEdited: Boolean = false,
    val editedAt: LocalDateTime? = null
) {
    fun getDisplayContent(): String {
        return when (type) {
            MessageType.TEXT -> content
            MessageType.IMAGE -> "📷 Foto"
            MessageType.AUDIO -> "🎤 Áudio"
            MessageType.VIDEO -> "🎥 Vídeo"
            MessageType.LOCATION -> "📍 Localização"
            MessageType.GIF -> "🎭 GIF"
            MessageType.STICKER -> "😀 Sticker"
            MessageType.SYSTEM_INFO -> content
        }
    }
    
    fun hasReaction(emoji: String, userId: String): Boolean {
        return reactions.any { it.emoji == emoji && it.userId == userId }
    }
    
    fun getReactionCount(emoji: String): Int {
        return reactions.count { it.emoji == emoji }
    }
} 
