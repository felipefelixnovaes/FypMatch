package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.ChatRepository
import com.ideiassertiva.FypMatch.data.repository.UserRepository
import com.ideiassertiva.FypMatch.data.repository.GeminiRepository
import com.ideiassertiva.FypMatch.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ChatUiState(
    val conversation: Conversation? = null,
    val messages: List<Message> = emptyList(),
    val otherUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTyping: Boolean = false,
    val currentMessage: String = ""
)

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: String
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val geminiRepository: GeminiRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private var conversationId: String = ""
    private var currentUserId: String = ""
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()
    
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    fun loadConversation(conversationId: String, currentUserId: String) {
        this.conversationId = conversationId
        this.currentUserId = currentUserId
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val conversation = chatRepository.getConversationById(conversationId)
                val otherUserId = conversation?.getOtherParticipant(currentUserId)?.userId
                val otherUser = otherUserId?.let { userRepository.getUserById(it) }
                
                _uiState.value = _uiState.value.copy(
                    conversation = conversation,
                    otherUser = otherUser,
                    isLoading = false
                )
                
                // Observa mensagens da conversa
                chatRepository.getConversationMessages(conversationId)
                    .collect { messages ->
                        _uiState.value = _uiState.value.copy(
                            messages = messages.sortedBy { it.timestamp }
                        )
                    }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
    
    fun updateMessageText(text: String) {
        _uiState.value = _uiState.value.copy(currentMessage = text)
        
        // Simula typing indicator (em app real seria WebSocket)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTyping = text.isNotEmpty())
        }
    }
    
    fun sendMessage() {
        val message = _uiState.value.currentMessage.trim()
        if (message.isEmpty()) return
        
        viewModelScope.launch {
            try {
                chatRepository.sendMessage(
                    conversationId = conversationId,
                    senderId = currentUserId,
                    content = message
                )
                
                _uiState.value = _uiState.value.copy(
                    currentMessage = "",
                    isTyping = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun sendLocation(latitude: Double, longitude: Double, address: String? = null) {
        viewModelScope.launch {
            try {
                chatRepository.sendMessage(
                    conversationId = conversationId,
                    senderId = currentUserId,
                    content = address ?: "Localização compartilhada",
                    type = MessageType.LOCATION
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun sendGif(gifUrl: String) {
        viewModelScope.launch {
            try {
                chatRepository.sendMessage(
                    conversationId = conversationId,
                    senderId = currentUserId,
                    content = gifUrl,
                    type = MessageType.GIF
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun addReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            try {
                chatRepository.addReaction(
                    conversationId = conversationId,
                    messageId = messageId,
                    emoji = emoji,
                    userId = currentUserId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun getMessageStatusIcon(status: MessageStatus): String {
        return when (status) {
            MessageStatus.SENDING -> "⏳"
            MessageStatus.SENT -> "✓"
            MessageStatus.DELIVERED -> "✓✓"
            MessageStatus.READ -> "👁"
        }
    }
    
    fun isOtherUserTyping(): Boolean {
        return _uiState.value.conversation?.isOtherUserTyping(currentUserId) ?: false
    }
    
    fun getLastSeenText(): String {
        return _uiState.value.conversation?.getLastSeenFormatted(currentUserId) ?: ""
    }
    
    /**
     * Envia uma mensagem do usuário e obtém resposta da Fype
     */
    fun sendMessage(messageText: String) {
        viewModelScope.launch {
            // Adiciona mensagem do usuário
            val userMessage = ChatMessage(
                content = messageText,
                isUser = true,
                timestamp = timeFormatter.format(Date())
            )
            
            _messages.value = _messages.value + userMessage
            
            // Mostra indicador de digitação
            _isTyping.value = true
            
            try {
                // Cria prompt personalizado para a Fype
                val fypePrompt = """
                    Você é a Fype, uma conselheira de relacionamentos carinhosa e experiente do app FypMatch.
                    
                    Características da Fype:
                    - Amigável, empática e acolhedora
                    - Especialista em relacionamentos, amor e encontros
                    - Usa linguagem natural do português brasileiro
                    - Dá conselhos práticos e positivos
                    - Incentiva autoestima e crescimento pessoal
                    - Usa emojis ocasionalmente para ser mais calorosa
                    
                    Pergunta do usuário: $messageText
                    
                    Responda como a Fype daria um conselho pessoal e acolhedor.
                """.trimIndent()
                
                // Busca resposta da IA
                geminiRepository.sendMessage(fypePrompt).collect { response ->
                    _isTyping.value = false
                    
                    // Adiciona resposta da Fype
                    val fypeMessage = ChatMessage(
                        content = response,
                        isUser = false,
                        timestamp = timeFormatter.format(Date())
                    )
                    
                    _messages.value = _messages.value + fypeMessage
                }
                
            } catch (e: Exception) {
                _isTyping.value = false
                
                // Resposta de fallback em caso de erro
                val fallbackMessage = ChatMessage(
                    content = "Ops! Tive um probleminha técnico, mas estou aqui para te ajudar! 💕 Pode repetir sua pergunta?",
                    isUser = false,
                    timestamp = timeFormatter.format(Date())
                )
                
                _messages.value = _messages.value + fallbackMessage
            }
        }
    }
    
    /**
     * Limpa todas as mensagens do chat
     */
    fun clearChat() {
        _messages.value = emptyList()
        _isTyping.value = false
    }
    
    /**
     * Simula digitação da Fype (para melhor UX)
     */
    private suspend fun simulateTyping() {
        _isTyping.value = true
        delay(1500 + (Math.random() * 2000).toLong()) // 1.5-3.5 segundos
        _isTyping.value = false
    }
} 
