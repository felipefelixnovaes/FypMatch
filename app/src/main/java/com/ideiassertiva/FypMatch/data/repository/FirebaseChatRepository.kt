package com.ideiassertiva.FypMatch.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    // Firestore collections
    private val conversationsCollection = firestore.collection("conversations")
    private val messagesCollection = firestore.collection("messages")
    private val usersCollection = firestore.collection("users")
    
    // Respostas automáticas inteligentes (mantidas do repositório anterior)
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
    
    suspend fun createConversationFromMatch(match: Match, currentUserId: String): String {
        val conversationId = UUID.randomUUID().toString()
        val otherUserId = if (match.user1Id == currentUserId) match.user2Id else match.user1Id
        
        val conversation = mapOf(
            "id" to conversationId,
            "matchId" to match.id,
            "participants" to listOf(
                mapOf(
                    "userId" to currentUserId,
                    "joinedAt" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                    "isOnline" to true
                ),
                mapOf(
                    "userId" to otherUserId,
                    "joinedAt" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                    "isOnline" to false,
                    "lastSeenAt" to LocalDateTime.now().minusMinutes(kotlin.random.Random.nextLong(1, 60)).toEpochSecond(ZoneOffset.UTC)
                )
            ),
            "status" to "ACTIVE",
            "createdAt" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
            "lastMessageAt" to null,
            "unreadCount" to mapOf(currentUserId to 0, otherUserId to 0),
            "typingIndicators" to emptyList<Map<String, Any>>()
        )
        
        // Create conversation in Firestore
        conversationsCollection.document(conversationId).set(conversation).await()
        
        // Create initial system message
        val systemMessageId = UUID.randomUUID().toString()
        val systemMessage = mapOf(
            "id" to systemMessageId,
            "conversationId" to conversationId,
            "senderId" to "system",
            "receiverId" to "",
            "content" to "Vocês deram match! 🎉",
            "type" to "SYSTEM_INFO",
            "status" to "DELIVERED",
            "timestamp" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
            "reactions" to emptyList<Map<String, Any>>(),
            "isEdited" to false
        )
        
        messagesCollection.document(systemMessageId).set(systemMessage).await()
        
        return conversationId
    }
    
    fun getConversations(): Flow<List<Conversation>> = callbackFlow {
        val listenerRegistration = conversationsCollection
            .orderBy("lastMessageAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val conversations = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            parseConversation(data)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(conversations)
                }
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    fun getConversationMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val listenerRegistration = messagesCollection
            .whereEqualTo("conversationId", conversationId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            parseMessage(data)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(messages)
                }
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        content: String = "",
        type: MessageType = MessageType.TEXT
    ): Message {
        val messageId = UUID.randomUUID().toString()
        val receiverId = getOtherParticipantId(conversationId, senderId) ?: ""
        
        val messageData = mapOf(
            "id" to messageId,
            "conversationId" to conversationId,
            "senderId" to senderId,
            "receiverId" to receiverId,
            "content" to content,
            "type" to type.name,
            "status" to MessageStatus.SENDING.name,
            "timestamp" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
            "reactions" to emptyList<Map<String, Any>>(),
            "isEdited" to false
        )
        
        // Save message to Firestore
        messagesCollection.document(messageId).set(messageData).await()
        
        // Update message status to SENT
        updateMessageStatus(conversationId, messageId, MessageStatus.SENT)
        
        // Update conversation's last message
        updateConversationLastMessage(conversationId, messageId, content, LocalDateTime.now())
        
        // Create Message object to return
        val message = Message(
            id = messageId,
            conversationId = conversationId,
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            type = type,
            status = MessageStatus.SENT
        )
        
        return message
    }
    
    suspend fun updateMessageStatus(conversationId: String, messageId: String, status: MessageStatus) {
        messagesCollection.document(messageId)
            .update("status", status.name)
            .await()
    }
    
    suspend fun setTypingIndicator(conversationId: String, userId: String, isTyping: Boolean) {
        val typingData = mapOf(
            "userId" to userId,
            "isTyping" to isTyping,
            "timestamp" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )
        
        // Update conversation with typing indicator
        conversationsCollection.document(conversationId)
            .update("typingIndicators.${userId}", typingData)
            .await()
    }
    
    suspend fun addReaction(conversationId: String, messageId: String, emoji: String, userId: String) {
        val reactionData = mapOf(
            "emoji" to emoji,
            "userId" to userId,
            "timestamp" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )
        
        // Add reaction to message
        messagesCollection.document(messageId)
            .update("reactions", com.google.firebase.firestore.FieldValue.arrayUnion(reactionData))
            .await()
    }
    
    suspend fun getConversationById(conversationId: String): Conversation? {
        return try {
            val doc = conversationsCollection.document(conversationId).get().await()
            val data = doc.data ?: return null
            parseConversation(data)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean) {
        val statusData = mapOf(
            "isOnline" to isOnline,
            "lastSeenAt" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )
        
        usersCollection.document(userId)
            .update(statusData)
            .await()
    }
    
    private suspend fun updateConversationLastMessage(conversationId: String, messageId: String, content: String, timestamp: LocalDateTime) {
        val updateData = mapOf(
            "lastMessage" to mapOf(
                "id" to messageId,
                "content" to content,
                "timestamp" to timestamp.toEpochSecond(ZoneOffset.UTC)
            ),
            "lastMessageAt" to timestamp.toEpochSecond(ZoneOffset.UTC)
        )
        
        conversationsCollection.document(conversationId)
            .update(updateData)
            .await()
    }
    
    private suspend fun getOtherParticipantId(conversationId: String, currentUserId: String): String? {
        val conversation = getConversationById(conversationId)
        return conversation?.participants?.find { it.userId != currentUserId }?.userId
    }
    
    private fun parseConversation(data: Map<String, Any>): Conversation? {
        return try {
            val participants = (data["participants"] as? List<Map<String, Any>>)?.map { participantData ->
                ConversationParticipant(
                    userId = participantData["userId"] as String,
                    joinedAt = LocalDateTime.ofEpochSecond(participantData["joinedAt"] as Long, 0, ZoneOffset.UTC),
                    lastSeenAt = (participantData["lastSeenAt"] as? Long)?.let { 
                        LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) 
                    },
                    isOnline = participantData["isOnline"] as? Boolean ?: false
                )
            } ?: emptyList()
            
            Conversation(
                id = data["id"] as String,
                matchId = data["matchId"] as String,
                participants = participants,
                status = ConversationStatus.valueOf(data["status"] as? String ?: "ACTIVE"),
                createdAt = LocalDateTime.ofEpochSecond(data["createdAt"] as Long, 0, ZoneOffset.UTC),
                lastMessageAt = (data["lastMessageAt"] as? Long)?.let { 
                    LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) 
                },
                unreadCount = (data["unreadCount"] as? Map<String, Number>)?.mapValues { it.value.toInt() } ?: emptyMap()
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseMessage(data: Map<String, Any>): Message? {
        return try {
            val reactions = (data["reactions"] as? List<Map<String, Any>>)?.map { reactionData ->
                MessageReaction(
                    emoji = reactionData["emoji"] as String,
                    userId = reactionData["userId"] as String,
                    timestamp = LocalDateTime.ofEpochSecond(reactionData["timestamp"] as Long, 0, ZoneOffset.UTC)
                )
            } ?: emptyList()
            
            Message(
                id = data["id"] as String,
                conversationId = data["conversationId"] as String,
                senderId = data["senderId"] as String,
                receiverId = data["receiverId"] as String,
                content = data["content"] as String,
                type = MessageType.valueOf(data["type"] as String),
                status = MessageStatus.valueOf(data["status"] as String),
                timestamp = LocalDateTime.ofEpochSecond(data["timestamp"] as Long, 0, ZoneOffset.UTC),
                reactions = reactions,
                isEdited = data["isEdited"] as? Boolean ?: false
            )
        } catch (e: Exception) {
            null
        }
    }
}