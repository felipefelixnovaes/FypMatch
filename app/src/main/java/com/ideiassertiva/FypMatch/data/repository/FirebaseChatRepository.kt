package com.ideiassertiva.FypMatch.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.ideiassertiva.FypMatch.domain.LiveConnectionEngine
import com.ideiassertiva.FypMatch.model.ConnectionEvent
import com.ideiassertiva.FypMatch.model.ConnectionEventType
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
    private val liveConnectionRepository: LiveConnectionRepository,
    private val liveConnectionEngine: LiveConnectionEngine,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ChatRepositoryInterface {

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

    suspend fun createConversationFromMatch(
        match: Match,
        currentUserId: String,
        preferredConversationId: String? = null
    ): String {
        val otherUserId = if (match.user1Id == currentUserId) match.user2Id else match.user1Id
        val participantIds = listOf(currentUserId, otherUserId)
            .filter { it.isNotBlank() }
            .distinct()
        val conversationId = preferredConversationId ?: UUID.randomUUID().toString()

        val conversation = mapOf(
            "id" to conversationId,
            "matchId" to match.id,
            "participantIds" to participantIds,
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

        conversationsCollection.document(conversationId)
            .set(conversation, com.google.firebase.firestore.SetOptions.merge())
            .await()

        val systemMessageId = UUID.randomUUID().toString()
        val systemMessage = mapOf(
            "id" to systemMessageId,
            "conversationId" to conversationId,
            "participantIds" to participantIds,
            "senderId" to "system",
            "receiverId" to otherUserId,
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

    suspend fun getOrCreateConversationForMatch(match: Match, currentUserId: String): String {
        val otherUserId = if (match.user1Id == currentUserId) match.user2Id else match.user1Id
        return getOrCreateConversationBetween(currentUserId, otherUserId, match.id)
    }

    suspend fun getOrCreateConversationBetween(
        currentUserId: String,
        otherUserId: String,
        matchId: String = UUID.randomUUID().toString()
    ): String {
        if (currentUserId.isBlank() || otherUserId.isBlank() || currentUserId == otherUserId) {
            throw IllegalArgumentException("Participantes inválidos para conversa")
        }

        val existingConversation = conversationsCollection
            .whereArrayContains("participantIds", currentUserId)
            .get()
            .await()
            .documents
            .firstOrNull { doc ->
                val participantIds = doc.get("participantIds") as? List<*>
                participantIds?.contains(otherUserId) == true
            }

        if (existingConversation != null) {
            return existingConversation.id
        }

        val match = Match(
            id = matchId,
            user1Id = currentUserId,
            user2Id = otherUserId,
            createdAt = Date()
        )
        return createConversationFromMatch(
            match = match,
            currentUserId = currentUserId,
            preferredConversationId = stableConversationIdForParticipants(currentUserId, otherUserId)
        )
    }

    override fun getConversations(): Flow<List<Conversation>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid.orEmpty()
        if (currentUserId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = conversationsCollection
            .whereArrayContains("participantIds", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val conversations = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            parseConversationDocument(data)
                        } catch (e: Exception) {
                            null
                        }
                    }.sortedByDescending { it.lastMessageAt ?: it.createdAt }
                    trySend(conversations)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getConversationMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid.orEmpty()
        if (currentUserId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        // Filtra por participantIds (array-contains) para satisfazer as regras de
        // seguranca — a query so por conversationId era rejeitada (PERMISSION_DENIED).
        // O filtro por conversationId e feito no cliente, evitando indice composto.
        val listenerRegistration = messagesCollection
            .whereArrayContains("participantIds", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            if (data["conversationId"] != conversationId) return@mapNotNull null
                            parseMessageDocument(data)
                        } catch (e: Exception) {
                            null
                        }
                    }.sortedBy { it.timestamp }
                    trySend(messages)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        content: String,
        type: MessageType
    ): Message {
        val messageId = UUID.randomUUID().toString()
        val conversation = getConversationById(conversationId)
        val receiverId = conversation?.getOtherParticipant(senderId)?.userId ?: ""
        val participantIds = conversation?.participants
            ?.map { it.userId }
            ?.filter { it.isNotBlank() }
            ?.distinct()
            ?.ifEmpty { null }
            ?: listOf(senderId, receiverId).filter { it.isNotBlank() }.distinct()

        val messageData = mapOf(
            "id" to messageId,
            "conversationId" to conversationId,
            "participantIds" to participantIds,
            "senderId" to senderId,
            "receiverId" to receiverId,
            "content" to content,
            "type" to type.name,
            "status" to MessageStatus.SENDING.name,
            "timestamp" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
            "reactions" to emptyList<Map<String, Any>>(),
            "isEdited" to false
        )

        messagesCollection.document(messageId).set(messageData).await()
        processConnectionEvent(conversationId, senderId, eventTypeForMessage(type))

        updateMessageStatus(conversationId, messageId, MessageStatus.SENT)

        updateConversationLastMessage(conversationId, messageId, content, LocalDateTime.now())

        return Message(
            id = messageId,
            conversationId = conversationId,
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            type = type,
            status = MessageStatus.SENT
        )
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
        processConnectionEvent(conversationId, senderId, ConnectionEventType.GAME_PLAYED)
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

        conversationsCollection.document(conversationId)
            .update("typingIndicators.${userId}", typingData)
            .await()
    }

    override suspend fun addReaction(conversationId: String, messageId: String, emoji: String, userId: String) {
        val reactionData = mapOf(
            "emoji" to emoji,
            "userId" to userId,
            "timestamp" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        )

        messagesCollection.document(messageId)
            .update("reactions", com.google.firebase.firestore.FieldValue.arrayUnion(reactionData))
            .await()
        processConnectionEvent(conversationId, userId, ConnectionEventType.REACTION_ADDED)
    }

    override suspend fun getConversationById(conversationId: String): Conversation? {
        return try {
            val doc = conversationsCollection.document(conversationId).get().await()
            val data = doc.data ?: return null
            parseConversationDocument(data)
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
            .set(updateData, com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    private suspend fun getOtherParticipantId(conversationId: String, currentUserId: String): String? {
        val conversation = getConversationById(conversationId)
        return conversation?.participants?.find { it.userId != currentUserId }?.userId
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

    private suspend fun processConnectionEvent(
        conversationId: String,
        senderId: String,
        eventType: ConnectionEventType
    ) {
        try {
            val conversation = getConversationById(conversationId) ?: return
            val receiverId = conversation.getOtherParticipant(senderId)?.userId ?: return
            val connection = liveConnectionRepository.getOrCreateLiveConnection(
                matchId = conversation.matchId,
                user1Id = senderId,
                user2Id = receiverId
            )
            val event = ConnectionEvent(
                initiatorUserId = senderId,
                type = eventType
            )
            liveConnectionEngine.processNewEvent(connection.id, event)
        } catch (_: Exception) {
            // Chat delivery must not fail because the connection radar could not be updated.
        }
    }

}

internal fun stableConversationIdForParticipants(userA: String, userB: String): String =
    "conv_" + listOf(userA, userB).sorted().joinToString("_")

internal fun parseConversationDocument(data: Map<String, Any>): Conversation? {
    return try {
        val participantIds = (data["participantIds"] as? List<*>)
            ?.filterIsInstance<String>()
            .orEmpty()
        val participants = (data["participants"] as? List<*>)
            ?.mapNotNull { it as? Map<*, *> }
            ?.mapNotNull { participantData ->
                val userId = participantData["userId"] as? String ?: return@mapNotNull null
                ConversationParticipant(
                    userId = userId,
                    joinedAt = localDateTimeFrom(participantData["joinedAt"]) ?: LocalDateTime.now(),
                    lastSeenAt = localDateTimeFrom(participantData["lastSeenAt"]),
                    isOnline = participantData["isOnline"] as? Boolean ?: false
                )
            }
            .orEmpty()
            .ifEmpty {
                participantIds.map {
                    ConversationParticipant(userId = it, joinedAt = LocalDateTime.now())
                }
            }

        Conversation(
            id = data["id"] as? String ?: return null,
            matchId = data["matchId"] as? String ?: "",
            participants = participants,
            status = runCatching {
                ConversationStatus.valueOf(data["status"] as? String ?: "ACTIVE")
            }.getOrDefault(ConversationStatus.ACTIVE),
            createdAt = localDateTimeFrom(data["createdAt"]) ?: LocalDateTime.now(),
            lastMessageAt = localDateTimeFrom(data["lastMessageAt"]),
            unreadCount = (data["unreadCount"] as? Map<*, *>)
                ?.mapNotNull { (key, value) ->
                    val userId = key as? String ?: return@mapNotNull null
                    val count = (value as? Number)?.toInt() ?: return@mapNotNull null
                    userId to count
                }
                ?.toMap()
                .orEmpty()
        )
    } catch (_: Exception) {
        null
    }
}

internal fun parseMessageDocument(data: Map<String, Any>): Message? {
    return try {
        val reactions = (data["reactions"] as? List<*>)
            ?.mapNotNull { it as? Map<*, *> }
            ?.mapNotNull { reactionData ->
                MessageReaction(
                    emoji = reactionData["emoji"] as? String ?: return@mapNotNull null,
                    userId = reactionData["userId"] as? String ?: return@mapNotNull null,
                    timestamp = localDateTimeFrom(reactionData["timestamp"]) ?: LocalDateTime.now()
                )
            }
            .orEmpty()

        Message(
            id = data["id"] as? String ?: return null,
            conversationId = data["conversationId"] as? String ?: return null,
            senderId = data["senderId"] as? String ?: return null,
            receiverId = data["receiverId"] as? String ?: "",
            content = data["content"] as? String ?: "",
            type = runCatching {
                MessageType.valueOf(data["type"] as? String ?: MessageType.TEXT.name)
            }.getOrDefault(MessageType.TEXT),
            status = runCatching {
                MessageStatus.valueOf(data["status"] as? String ?: MessageStatus.SENT.name)
            }.getOrDefault(MessageStatus.SENT),
            timestamp = localDateTimeFrom(data["timestamp"]) ?: LocalDateTime.now(),
            reactions = reactions,
            isEdited = data["isEdited"] as? Boolean ?: false
        )
    } catch (_: Exception) {
        null
    }
}

private fun localDateTimeFrom(value: Any?): LocalDateTime? {
    val epochSeconds = when (value) {
        is Timestamp -> value.seconds
        is Date -> value.toInstant().epochSecond
        is Number -> value.toLong()
        else -> return null
    }
    return LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC)
}
