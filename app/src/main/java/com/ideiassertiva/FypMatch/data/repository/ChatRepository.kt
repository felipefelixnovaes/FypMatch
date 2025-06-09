package com.ideiassertiva.FypMatch.data.repository

import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor() {
    
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: Flow<List<Conversation>> = _conversations.asStateFlow()
    
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: Flow<Map<String, List<Message>>> = _messages.asStateFlow()
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    // Respostas automáticas inteligentes
    private val smartReplies = listOf(
        "Oi! Que bom que deu match! 😊",
        "Olá! Vi que temos muito em comum! 🤗",
        "E aí, como foi seu dia?",
        "Qual seu lugar favorito da cidade?",
        "Que legal! Eu também gosto disso! 😄",
        "Nossa, que interessante! Conta mais!",
        "Que tal tomarmos um café qualquer dia?",
        "Você tem um sorriso lindo! 😍",
        "Estou gostando muito de conversar com você",
        "❤️", "😘", "🥰", "😊"
    )
    
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
        
        coroutineScope.launch {
            delay(kotlin.random.Random.nextLong(3000, 8000))
            sendAutomaticReply(conversationId, otherUserId, currentUserId)
        }
        
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
            status = MessageStatus.SENDING
        )
        
        val currentMessages = _messages.value.toMutableMap()
        val conversationMessages = currentMessages[conversationId]?.toMutableList() ?: mutableListOf()
        conversationMessages.add(message)
        currentMessages[conversationId] = conversationMessages
        _messages.value = currentMessages
        
        delay(500)
        updateMessageStatus(conversationId, messageId, MessageStatus.SENT)
        
        delay(1000)
        updateMessageStatus(conversationId, messageId, MessageStatus.DELIVERED)
        
        updateConversationLastMessage(conversationId, message)
        
        if (senderId != "system") {
            coroutineScope.launch {
                delay(kotlin.random.Random.nextLong(2000, 10000))
                setTypingIndicator(conversationId, receiverId, true)
                delay(kotlin.random.Random.nextLong(1000, 3000))
                setTypingIndicator(conversationId, receiverId, false)
                sendAutomaticReply(conversationId, receiverId, senderId, content)
            }
        }
        
        return message
    }
    
    private suspend fun sendAutomaticReply(
        conversationId: String, 
        senderId: String, 
        receiverId: String,
        originalMessage: String = ""
    ) {
        val reply = generateSmartReply(originalMessage)
        
        val message = Message(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = senderId,
            receiverId = receiverId,
            content = reply,
            type = MessageType.TEXT,
            status = MessageStatus.DELIVERED
        )
        
        val currentMessages = _messages.value.toMutableMap()
        val conversationMessages = currentMessages[conversationId]?.toMutableList() ?: mutableListOf()
        conversationMessages.add(message)
        currentMessages[conversationId] = conversationMessages
        _messages.value = currentMessages
        
        updateConversationLastMessage(conversationId, message)
        
        coroutineScope.launch {
            delay(kotlin.random.Random.nextLong(1000, 5000))
            updateMessageStatus(conversationId, message.id, MessageStatus.READ)
        }
    }
    
    private fun generateSmartReply(originalMessage: String): String {
        val lowerMessage = originalMessage.lowercase()
        
        return when {
            lowerMessage.contains("oi") || lowerMessage.contains("olá") -> 
                listOf("Oi! Como você está?", "Olá! Tudo bem?", "Oi! Que bom falar com você! 😊").random()
            lowerMessage.contains("como") && lowerMessage.contains("você") -> 
                listOf("Estou bem! E você?", "Tudo tranquilo! 😊", "Indo bem, obrigado(a)!").random()
            lowerMessage.contains("música") -> 
                listOf("Gosto de vários estilos! E você?", "Amo música! 🎵", "Qual seu gênero favorito?").random()
            lowerMessage.contains("café") -> 
                listOf("Que tal mesmo! Conheço uns lugares legais", "Adoraria! 😄", "Ótima ideia!").random()
            else -> smartReplies.random()
        }
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
        }
    }
    
    fun getConversationMessages(conversationId: String): Flow<List<Message>> {
        return messages.map { messagesMap ->
            messagesMap[conversationId] ?: emptyList()
        }
    }
    
    private fun getOtherParticipantId(conversationId: String, currentUserId: String): String? {
        val conversation = _conversations.value.find { it.id == conversationId }
        return conversation?.participants?.find { it.userId != currentUserId }?.userId
    }
    
    fun getConversationById(conversationId: String): Conversation? {
        return _conversations.value.find { it.id == conversationId }
    }
} 
