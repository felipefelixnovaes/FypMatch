package com.ideiassertiva.FypMatch.data.repository

import android.util.Log
import com.ideiassertiva.FypMatch.BuildConfig
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.util.AnalyticsManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.time.LocalDateTime
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val analyticsManager: AnalyticsManager
) {
    
    companion object {
        private const val TAG = "ChatRepository"
        private const val CONVERSATIONS_NODE = "conversas"
        private const val MESSAGES_NODE = "mensagens"
        private const val PARTICIPANTS_NODE = "participantes"
        private const val ASSISTENTE_ID = "assistente_fypmatch"
    }
    
    // Firebase Realtime Database - URL do projeto fypmatch-8ac3c
    private val database = FirebaseDatabase.getInstance("https://fypmatch-8ac3c-default-rtdb.firebaseio.com/")
    private val conversationsRef = database.getReference(CONVERSATIONS_NODE)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: Flow<List<Conversation>> = _conversations.asStateFlow()
    
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: Flow<Map<String, List<Message>>> = _messages.asStateFlow()
    
    private val messageListeners = mutableMapOf<String, ValueEventListener>()
    
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
    
    /**
     * Gera ID determinístico para conversa baseado nos IDs dos participantes
     * IDs são ordenados alfabeticamente e concatenados com underscore
     */
    private fun generateConversationId(userId1: String, userId2: String): String {
        val sortedIds = listOf(userId1, userId2).sorted()
        return "${sortedIds[0]}_${sortedIds[1]}"
    }
    
    /**
     * Gera ID para conversa com assistente
     */
    fun generateAssistantConversationId(userId: String): String {
        return generateConversationId(ASSISTENTE_ID, userId)
    }
    
    /**
     * Determina o tipo de conversa baseado nos participantes
     */
    private fun getConversationType(userId1: String, userId2: String): String {
        return if (userId1 == ASSISTENTE_ID || userId2 == ASSISTENTE_ID) {
            "usuario-assistente"
        } else {
            "usuario-usuario"
        }
    }
    
    /**
     * Inicializa listeners para conversas do usuário
     */
    fun initializeConversations(userId: String) {
        if (BuildConfig.FIREBASE_DEBUG) {
            Log.d(TAG, "Inicializando listeners para usuário: $userId")
        }
        
        // Query para buscar todas as conversas onde o usuário participa
        conversationsRef.orderByKey().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conversations = mutableListOf<Conversation>()
                
                snapshot.children.forEach { conversationSnapshot ->
                    val conversationId = conversationSnapshot.key ?: return@forEach
                    val participantsSnapshot = conversationSnapshot.child(PARTICIPANTS_NODE)
                    
                    // Verifica se o usuário participa desta conversa
                    if (participantsSnapshot.hasChild(userId)) {
                        try {
                            val conversation = parseConversationFromSnapshot(conversationSnapshot, conversationId)
                            conversations.add(conversation)
                        } catch (e: Exception) {
                            Log.e(TAG, "Erro ao converter conversa: $conversationId", e)
                        }
                    }
                }
                
                // Ordenar por última mensagem
                conversations.sortByDescending { 
                    it.lastMessageAt?.atZone(ZoneId.systemDefault())?.toEpochSecond() ?: 0L
                }
                _conversations.value = conversations
                
                if (BuildConfig.FIREBASE_DEBUG) {
                    Log.d(TAG, "Conversas carregadas: ${conversations.size}")
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Erro ao escutar conversas", error.toException())
                analyticsManager.logError(error.toException(), "realtime_conversation_listener_error")
            }
        })
    }
    
    /**
     * Inicializa listener para mensagens de uma conversa específica
     */
    fun initializeMessages(conversationId: String) {
        if (BuildConfig.FIREBASE_DEBUG) {
            Log.d(TAG, "Inicializando listener para mensagens: $conversationId")
        }
        
        // Remove listener anterior se existir
        messageListeners[conversationId]?.let { oldListener ->
            conversationsRef.child(conversationId).child(MESSAGES_NODE).removeEventListener(oldListener)
        }
        
        // Listener para mensagens
        val messageListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                
                snapshot.children.forEach { messageSnapshot ->
                    try {
                        val message = parseMessageFromSnapshot(messageSnapshot, conversationId)
                        messages.add(message)
                    } catch (e: Exception) {
                        Log.e(TAG, "Erro ao converter mensagem: ${messageSnapshot.key}", e)
                    }
                }
                
                // Ordenar por timestamp
                messages.sortBy { it.timestamp.atZone(ZoneId.systemDefault()).toEpochSecond() }
                
                val currentMessages = _messages.value.toMutableMap()
                currentMessages[conversationId] = messages
                _messages.value = currentMessages
                
                if (BuildConfig.FIREBASE_DEBUG) {
                    Log.d(TAG, "Mensagens carregadas para $conversationId: ${messages.size}")
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Erro ao escutar mensagens", error.toException())
                analyticsManager.logError(error.toException(), "realtime_messages_listener_error")
            }
        }
        
        messageListeners[conversationId] = messageListener
        conversationsRef.child(conversationId).child(MESSAGES_NODE)
            .orderByChild("timestamp")
            .addValueEventListener(messageListener)
    }
    
    /**
     * Converte DataSnapshot para Conversation
     */
    private fun parseConversationFromSnapshot(snapshot: DataSnapshot, conversationId: String): Conversation {
        val tipo = snapshot.child("tipo").getValue(String::class.java) ?: "usuario-usuario"
        val participantIds = mutableListOf<String>()
        val participants = mutableListOf<ConversationParticipant>()
        
        // Extrair participantes
        snapshot.child(PARTICIPANTS_NODE).children.forEach { participantSnapshot ->
            val participantId = participantSnapshot.key ?: return@forEach
            participantIds.add(participantId)
            participants.add(
                ConversationParticipant(
                    userId = participantId,
                    joinedAt = LocalDateTime.now(),
                    isOnline = kotlin.random.Random.nextBoolean()
                )
            )
        }
        
        // Última mensagem (se existir)
        val lastMessageContent = snapshot.child("ultimaMensagem").child("texto").getValue(String::class.java) ?: ""
        val lastMessageSenderId = snapshot.child("ultimaMensagem").child("remetenteId").getValue(String::class.java) ?: ""
        val lastMessageTimestamp = snapshot.child("ultimaMensagem").child("timestamp").getValue(Long::class.java) ?: 0L
        
        return Conversation(
            id = conversationId,
            participantIds = participantIds,
            participants = participants,
            status = ConversationStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            lastMessageAt = if (lastMessageTimestamp > 0) {
                Instant.ofEpochMilli(lastMessageTimestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
            } else null,
            lastMessageContent = lastMessageContent,
            lastMessageSenderId = lastMessageSenderId
        )
    }
    
    /**
     * Converte DataSnapshot para Message
     */
    private fun parseMessageFromSnapshot(snapshot: DataSnapshot, conversationId: String): Message {
        val messageId = snapshot.key ?: UUID.randomUUID().toString()
        val remetenteId = snapshot.child("remetenteId").getValue(String::class.java) ?: ""
        val texto = snapshot.child("texto").getValue(String::class.java) ?: ""
        val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
        val tipo = snapshot.child("tipo").getValue(String::class.java) ?: "TEXT"
        val status = snapshot.child("status").getValue(String::class.java) ?: "DELIVERED"
        
        return Message(
            id = messageId,
            conversationId = conversationId,
            senderId = remetenteId,
            receiverId = "",
            content = texto,
            type = try { MessageType.valueOf(tipo) } catch (e: Exception) { MessageType.TEXT },
            status = try { MessageStatus.valueOf(status) } catch (e: Exception) { MessageStatus.DELIVERED },
            timestamp = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
        )
    }
    
    /**
     * Cria ou encontra conversa entre dois usuários
     */
    suspend fun createOrFindConversation(userId1: String, userId2: String): String {
        val conversationId = generateConversationId(userId1, userId2)
        val conversationType = getConversationType(userId1, userId2)
        
        if (BuildConfig.FIREBASE_DEBUG) {
            Log.d(TAG, "Criando/encontrando conversa: $conversationId ($conversationType)")
        }
        
        try {
            val conversationRef = conversationsRef.child(conversationId)
            
            // Verificar se conversa já existe
            val snapshot = conversationRef.get().await()
            
            if (!snapshot.exists()) {
                // Criar nova conversa
                val conversationData = mapOf(
                    "tipo" to conversationType,
                    PARTICIPANTS_NODE to mapOf(
                        userId1 to true,
                        userId2 to true
                    ),
                    "criadaEm" to ServerValue.TIMESTAMP
                )
                
                conversationRef.setValue(conversationData).await()
                
                if (BuildConfig.FIREBASE_DEBUG) {
                    Log.d(TAG, "Nova conversa criada: $conversationId")
                }
                
                // Analytics
                analyticsManager.logCustomCrash("conversation_created", mapOf(
                    "conversation_id" to conversationId,
                    "type" to conversationType
                ))
                
                // Se for conversa com assistente, enviar mensagem de boas-vindas
                if (conversationType == "usuario-assistente") {
                    sendWelcomeMessage(conversationId)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar/encontrar conversa", e)
            analyticsManager.logError(e, "realtime_create_conversation_error")
        }
        
        return conversationId
    }
    
    /**
     * Envia mensagem de boas-vindas da assistente
     */
    private suspend fun sendWelcomeMessage(conversationId: String) {
        val welcomeText = "Olá! 💕 Eu sou a Fype, sua conselheira pessoal de relacionamentos! Como posso te ajudar hoje?"
        
        sendMessage(
            conversationId = conversationId,
            senderId = ASSISTENTE_ID,
            content = welcomeText,
            type = MessageType.TEXT
        )
    }
    
    /**
     * Envia uma mensagem
     */
    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        content: String,
        type: MessageType = MessageType.TEXT
    ): Message {
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        
        val message = Message(
            id = messageId,
            conversationId = conversationId,
            senderId = senderId,
            receiverId = "",
            content = content,
            type = type,
            status = MessageStatus.SENDING,
            timestamp = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
        )
        
        try {
            val messageData = mapOf(
                "remetenteId" to senderId,
                "texto" to content,
                "tipo" to type.name,
                "timestamp" to timestamp,
                "status" to MessageStatus.SENT.name
            )
            
            val conversationRef = conversationsRef.child(conversationId)
            
            // Salvar mensagem
            conversationRef.child(MESSAGES_NODE).child(messageId).setValue(messageData).await()
            
            // Atualizar última mensagem da conversa
            val lastMessageData = mapOf(
                "remetenteId" to senderId,
                "texto" to content,
                "timestamp" to timestamp
            )
            conversationRef.child("ultimaMensagem").setValue(lastMessageData).await()
            
            if (BuildConfig.FIREBASE_DEBUG) {
                Log.d(TAG, "Mensagem enviada: $messageId em $conversationId")
            }
            
            // Analytics
            analyticsManager.logCustomCrash("message_sent", mapOf(
                "conversation_id" to conversationId,
                "message_type" to type.name,
                "content_length" to content.length.toString(),
                "sender_is_assistant" to (senderId == ASSISTENTE_ID).toString()
            ))
            
            // Simular resposta automática para conversas usuário-usuário
            if (senderId != ASSISTENTE_ID && !conversationId.contains(ASSISTENTE_ID)) {
                coroutineScope.launch {
                    delay(kotlin.random.Random.nextLong(2000, 8000))
                    sendAutomaticReply(conversationId, senderId, content)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enviar mensagem", e)
            analyticsManager.logError(e, "realtime_send_message_error")
        }
        
        return message
    }
    
    /**
     * Método legacy para compatibilidade - cria conversa de match
     */
    fun createConversationFromMatch(match: Match, currentUserId: String): String {
        val otherUserId = if (match.user1Id == currentUserId) match.user2Id else match.user1Id
        
        coroutineScope.launch {
            createOrFindConversation(currentUserId, otherUserId)
        }
        
        return generateConversationId(currentUserId, otherUserId)
    }
    
    /**
     * Envia resposta automática simulada
     */
    private suspend fun sendAutomaticReply(conversationId: String, originalSenderId: String, originalMessage: String) {
        // Determinar quem é o outro participante
        val otherParticipantId = getOtherParticipantId(conversationId, originalSenderId) ?: return
        
        val reply = generateSmartReply(originalMessage)
        
        sendMessage(
            conversationId = conversationId,
            senderId = otherParticipantId,
            content = reply,
            type = MessageType.TEXT
        )
    }
    
    /**
     * Gera resposta inteligente baseada na mensagem
     */
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
    
    /**
     * Obtém ID do outro participante da conversa
     */
    private suspend fun getOtherParticipantId(conversationId: String, currentUserId: String): String? {
        return try {
            val snapshot = conversationsRef.child(conversationId).child(PARTICIPANTS_NODE).get().await()
            snapshot.children.find { it.key != currentUserId }?.key
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter outro participante", e)
            null
        }
    }
    
    /**
     * Obtém mensagens de uma conversa
     */
    fun getConversationMessages(conversationId: String): Flow<List<Message>> {
        // Inicializar listener se ainda não existir
        if (!messageListeners.containsKey(conversationId)) {
            initializeMessages(conversationId)
        }
        
        return messages.map { messagesMap ->
            messagesMap[conversationId] ?: emptyList()
        }
    }
    
    /**
     * Obtém conversa por ID
     */
    fun getConversationById(conversationId: String): Conversation? {
        return _conversations.value.find { it.id == conversationId }
    }
    
    /**
     * Adiciona reação a uma mensagem - método legacy
     */
    suspend fun addReaction(conversationId: String, messageId: String, emoji: String, userId: String) {
        // Implementação simplificada para o Realtime Database
        try {
            val reactionRef = conversationsRef
                .child(conversationId)
                .child(MESSAGES_NODE)
                .child(messageId)
                .child("reactions")
                .child("${userId}_$emoji")
            
            val reactionData = mapOf(
                "emoji" to emoji,
                "userId" to userId,
                "timestamp" to ServerValue.TIMESTAMP
            )
            
            reactionRef.setValue(reactionData).await()
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao adicionar reação", e)
            analyticsManager.logError(e, "realtime_add_reaction_error")
        }
    }
    
    /**
     * Limpa listeners
     */
    fun cleanup() {
        messageListeners.forEach { (conversationId, listener) ->
            conversationsRef.child(conversationId).child(MESSAGES_NODE).removeEventListener(listener)
        }
        messageListeners.clear()
    }
} 