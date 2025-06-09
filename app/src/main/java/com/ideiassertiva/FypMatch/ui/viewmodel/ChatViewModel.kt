package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.ChatRepository
import com.ideiassertiva.FypMatch.data.repository.UserRepository
import com.ideiassertiva.FypMatch.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private var conversationId: String = ""
    private var currentUserId: String = ""
    
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
} 
