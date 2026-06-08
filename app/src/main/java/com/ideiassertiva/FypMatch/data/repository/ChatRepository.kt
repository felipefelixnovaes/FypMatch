package com.ideiassertiva.FypMatch.data.repository

import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class ChatRepository @Inject constructor() {
    
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: Flow<List<Conversation>> = _conversations.asStateFlow()
    
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: Flow<Map<String, List<Message>>> = _messages.asStateFlow()

    private val _liveConnections = MutableStateFlow<Map<String, LiveConnection>>(emptyMap())
    val liveConnections: Flow<Map<String, LiveConnection>> = _liveConnections.asStateFlow()
    
    fun createConversationFromMatch(match: Match, currentUserId: String): String {
        val conversationId = UUID.randomUUID().toString()
        val otherUserId = if (match.user1Id == currentUserId) match.user2Id else match.user1Id
        
        val conversation = Conversation(
            id = conversationId,
            matchId = match.id,
            participants = listOf(
                ConversationParticipant(
                    userId = currentUserId,
                    joinedAt = LocalDateTime.now(),
                    isOnline = true
                ),
                ConversationParticipant(
                    userId = otherUserId,
                    joinedAt = LocalDateTime.now(),
                    isOnline = kotlin.random.Random.nextBoolean(),
                    lastSeenAt = LocalDateTime.now().minusMinutes(kotlin.random.Random.nextLong(1, 60))
                )
            )
        )
        
        val currentConversations = _conversations.value.toMutableList()
        currentConversations.add(conversation)
        _conversations.value = currentConversations
        
        val systemMessage = Message(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = "system",
            receiverId = "",
            content = "Vocês deram match! 🎉",
            type = MessageType.SYSTEM_INFO,
            status = MessageStatus.DELIVERED
        )
        
        val currentMessages = _messages.value.toMutableMap()
        currentMessages[conversationId] = listOf(systemMessage)
        _messages.value = currentMessages

        ensureLiveConnection(conversation, currentUserId, otherUserId)

        return conversationId
    }
    
    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        content: String,
        type: MessageType = MessageType.TEXT
    ): Message {
        val messageId = UUID.randomUUID().toString()
        val receiverId = getOtherParticipantId(conversationId, senderId) ?: ""

        val message = Message(
            id = messageId,
            conversationId = conversationId,
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            type = type,
            status = MessageStatus.SENT
        )

        val currentMessages = _messages.value.toMutableMap()
        val conversationMessages = currentMessages[conversationId]?.toMutableList() ?: mutableListOf()
        conversationMessages.add(message)
        currentMessages[conversationId] = conversationMessages
        _messages.value = currentMessages

        updateConversationLastMessage(conversationId, message)
        recordConnectionEvent(conversationId, senderId, eventTypeForMessage(type))

        return message
    }

    suspend fun sendConnectionMission(
        conversationId: String,
        senderId: String,
        content: String
    ): Message {
        val message = sendMessage(
            conversationId = conversationId,
            senderId = senderId,
            content = content,
            type = MessageType.TEXT
        )
        recordConnectionEvent(conversationId, senderId, ConnectionEventType.GAME_PLAYED)
        return message
    }

    private suspend fun updateMessageStatus(conversationId: String, messageId: String, status: MessageStatus) {
        val currentMessages = _messages.value.toMutableMap()
        val conversationMessages = currentMessages[conversationId]?.toMutableList() ?: return
        
        val index = conversationMessages.indexOfFirst { it.id == messageId }
        if (index >= 0) {
            conversationMessages[index] = conversationMessages[index].copy(status = status)
            currentMessages[conversationId] = conversationMessages
            _messages.value = currentMessages
        }
    }
    
    private suspend fun updateConversationLastMessage(conversationId: String, message: Message) {
        val currentConversations = _conversations.value.toMutableList()
        val index = currentConversations.indexOfFirst { it.id == conversationId }
        
        if (index >= 0) {
            currentConversations[index] = currentConversations[index].copy(
                lastMessage = message,
                lastMessageAt = message.timestamp
            )
            _conversations.value = currentConversations
        }
    }
    
    private suspend fun setTypingIndicator(conversationId: String, userId: String, isTyping: Boolean) {
        val currentConversations = _conversations.value.toMutableList()
        val index = currentConversations.indexOfFirst { it.id == conversationId }
        
        if (index >= 0) {
            val conversation = currentConversations[index]
            val updatedTypingIndicators = conversation.typingIndicators.toMutableList()
            
            updatedTypingIndicators.removeAll { it.userId == userId }
            
            if (isTyping) {
                updatedTypingIndicators.add(TypingIndicator(userId = userId, isTyping = true))
            }
            
            currentConversations[index] = conversation.copy(typingIndicators = updatedTypingIndicators)
            _conversations.value = currentConversations
        }
    }
    
    suspend fun addReaction(conversationId: String, messageId: String, emoji: String, userId: String) {
        val currentMessages = _messages.value.toMutableMap()
        val conversationMessages = currentMessages[conversationId]?.toMutableList() ?: return
        
        val messageIndex = conversationMessages.indexOfFirst { it.id == messageId }
        if (messageIndex >= 0) {
            val message = conversationMessages[messageIndex]
            val reactions = message.reactions.toMutableList()
            
            reactions.removeAll { it.userId == userId && it.emoji == emoji }
            reactions.add(MessageReaction(emoji, userId, LocalDateTime.now()))
            
            conversationMessages[messageIndex] = message.copy(reactions = reactions)
            currentMessages[conversationId] = conversationMessages
            _messages.value = currentMessages
            recordConnectionEvent(conversationId, userId, ConnectionEventType.REACTION_ADDED)
        }
    }
    
    fun getConversationMessages(conversationId: String): Flow<List<Message>> {
        return messages.map { messagesMap ->
            messagesMap[conversationId] ?: emptyList()
        }
    }

    fun getLiveConnection(conversationId: String): Flow<LiveConnection?> {
        return liveConnections.map { connections ->
            connections[conversationId]
        }
    }
    
    private fun getOtherParticipantId(conversationId: String, currentUserId: String): String? {
        val conversation = _conversations.value.find { it.id == conversationId }
        return conversation?.participants?.find { it.userId != currentUserId }?.userId
    }
    
    fun getConversationById(conversationId: String): Conversation? {
        return _conversations.value.find { it.id == conversationId }
    }

    private fun ensureLiveConnection(
        conversation: Conversation,
        user1Id: String,
        user2Id: String
    ): LiveConnection {
        val current = _liveConnections.value[conversation.id]
        if (current != null) return current

        val seededDimensions = ConnectionDimensions(
            reciprocity = 12f,
            continuity = 18f,
            affinity = 12f,
            lightness = 16f,
            depth = 8f,
            initiative = 12f
        )
        val seededConnection = LiveConnection(
            id = conversation.id,
            matchId = conversation.matchId,
            conversationId = conversation.id,
            user1Id = user1Id,
            user2Id = user2Id,
            overallScore = seededDimensions.averageScore(),
            status = ConnectionStatus.WARMING_UP,
            dimensions = seededDimensions,
            lastInteractionAt = Date(),
            updatedAt = Date()
        )

        val currentConnections = _liveConnections.value.toMutableMap()
        currentConnections[conversation.id] = seededConnection
        _liveConnections.value = currentConnections
        return seededConnection
    }

    private fun recordConnectionEvent(
        conversationId: String,
        senderId: String,
        type: ConnectionEventType
    ) {
        val conversation = getConversationById(conversationId) ?: return
        val otherUserId = getOtherParticipantId(conversationId, senderId) ?: return
        val currentConnection = ensureLiveConnection(conversation, senderId, otherUserId)
        val updatedConnection = currentConnection.applyEvent(type)

        val currentConnections = _liveConnections.value.toMutableMap()
        currentConnections[conversationId] = updatedConnection
        _liveConnections.value = currentConnections
    }

    private fun eventTypeForMessage(type: MessageType): ConnectionEventType {
        return when (type) {
            MessageType.IMAGE,
            MessageType.VIDEO,
            MessageType.GIF,
            MessageType.STICKER -> ConnectionEventType.MEDIA_SHARED
            MessageType.AUDIO -> ConnectionEventType.VOICE_NOTE_SENT
            else -> ConnectionEventType.MESSAGE_SENT
        }
    }

    private fun LiveConnection.applyEvent(type: ConnectionEventType): LiveConnection {
        var reciprocity = dimensions.reciprocity
        var continuity = dimensions.continuity
        var affinity = dimensions.affinity
        var lightness = dimensions.lightness
        var depth = dimensions.depth
        var initiative = dimensions.initiative

        when (type) {
            ConnectionEventType.MESSAGE_SENT -> {
                continuity = min(100f, continuity + 1.5f)
                initiative = min(100f, initiative + 1f)
            }
            ConnectionEventType.MEDIA_SHARED -> lightness = min(100f, lightness + 2f)
            ConnectionEventType.REACTION_ADDED -> {
                reciprocity = min(100f, reciprocity + 1f)
                lightness = min(100f, lightness + 1f)
            }
            ConnectionEventType.GAME_PLAYED -> {
                affinity = min(100f, affinity + 5f)
                lightness = min(100f, lightness + 2f)
                depth = min(100f, depth + 1f)
            }
            ConnectionEventType.VOICE_NOTE_SENT -> {
                continuity = min(100f, continuity + 1f)
                depth = min(100f, depth + 2f)
            }
            ConnectionEventType.CONVERSATION_INITIATED -> continuity = min(100f, continuity + 5f)
        }

        val updatedDimensions = dimensions.copy(
            reciprocity = reciprocity,
            continuity = continuity,
            affinity = affinity,
            lightness = lightness,
            depth = depth,
            initiative = initiative
        )
        val score = updatedDimensions.averageScore()

        return copy(
            overallScore = score,
            status = score.toConnectionStatus(),
            dimensions = updatedDimensions,
            lastInteractionAt = Date(),
            updatedAt = Date()
        )
    }

    private fun ConnectionDimensions.averageScore(): Float {
        return listOf(
            reciprocity,
            continuity,
            affinity,
            lightness,
            depth,
            initiative
        ).average().toFloat()
    }

    private fun Float.toConnectionStatus(): ConnectionStatus {
        return when {
            this > 80f -> ConnectionStatus.ON_FIRE
            this > 50f -> ConnectionStatus.ACTIVE
            this > 15f -> ConnectionStatus.WARMING_UP
            else -> ConnectionStatus.ICE_COLD
        }
    }
} 
