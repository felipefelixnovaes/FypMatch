package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.ChatRepository
import com.ideiassertiva.FypMatch.data.repository.FirebaseChatRepository
import com.ideiassertiva.FypMatch.data.repository.UserRepository
import com.ideiassertiva.FypMatch.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EnhancedChatUiState(
    val conversation: Conversation? = null,
    val messages: List<Message> = emptyList(),
    val otherUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTyping: Boolean = false,
    val currentMessage: String = "",
    val isOtherUserTyping: Boolean = false,
    val isOtherUserOnline: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED
)

enum class ConnectionState {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    ERROR
}

@HiltViewModel
class EnhancedChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val firebaseChatRepository: FirebaseChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EnhancedChatUiState())
    val uiState: StateFlow<EnhancedChatUiState> = _uiState.asStateFlow()
    
    private var conversationId: String = ""
    private var currentUserId: String = ""
    private var useFirebase: Boolean = false // Toggle between implementations
    
    fun loadConversation(conversationId: String, currentUserId: String, useFirebase: Boolean = false) {
        this.conversationId = conversationId
        this.currentUserId = currentUserId
        this.useFirebase = useFirebase
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                connectionState = if (useFirebase) ConnectionState.CONNECTING else ConnectionState.CONNECTED
            )
            
            try {
                if (useFirebase) {
                    loadFirebaseConversation(conversationId, currentUserId)
                } else {
                    loadMockConversation(conversationId, currentUserId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false,
                    connectionState = ConnectionState.ERROR
                )
            }
        }
    }
    
    private suspend fun loadFirebaseConversation(conversationId: String, currentUserId: String) {
        try {
            val conversation = firebaseChatRepository.getConversationById(conversationId)
            val otherUserId = conversation?.getOtherParticipant(currentUserId)?.userId
            val otherUser = otherUserId?.let { userRepository.getUserById(it) }
            
            _uiState.value = _uiState.value.copy(
                conversation = conversation,
                otherUser = otherUser,
                isLoading = false,
                connectionState = ConnectionState.CONNECTED,
                isOtherUserOnline = conversation?.isOtherUserOnline(currentUserId) ?: false
            )
            
            // Observe Firebase messages in real-time
            firebaseChatRepository.getConversationMessages(conversationId)
                .collect { messages ->
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        connectionState = ConnectionState.CONNECTED
                    )
                }
                
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Erro ao conectar com Firebase: ${e.message}",
                isLoading = false,
                connectionState = ConnectionState.ERROR
            )
        }
    }
    
    private suspend fun loadMockConversation(conversationId: String, currentUserId: String) {
        val conversation = chatRepository.getConversationById(conversationId)
        val otherUserId = conversation?.getOtherParticipant(currentUserId)?.userId
        val otherUser = otherUserId?.let { userRepository.getUserById(it) }
        
        _uiState.value = _uiState.value.copy(
            conversation = conversation,
            otherUser = otherUser,
            isLoading = false,
            connectionState = ConnectionState.CONNECTED,
            isOtherUserOnline = conversation?.isOtherUserOnline(currentUserId) ?: false
        )
        
        // Observe mock messages
        chatRepository.getConversationMessages(conversationId)
            .collect { messages ->
                _uiState.value = _uiState.value.copy(
                    messages = messages,
                    isOtherUserTyping = conversation?.isOtherUserTyping(currentUserId) ?: false
                )
            }
    }
    
    fun updateMessage(message: String) {
        _uiState.value = _uiState.value.copy(currentMessage = message)
        
        // Handle typing indicators for Firebase
        if (useFirebase && message.isNotBlank() && !_uiState.value.isTyping) {
            setTypingIndicator(true)
        } else if (useFirebase && message.isBlank() && _uiState.value.isTyping) {
            setTypingIndicator(false)
        }
    }
    
    fun sendMessage() {
        val message = _uiState.value.currentMessage.trim()
        if (message.isEmpty()) return
        
        viewModelScope.launch {
            try {
                if (useFirebase) {
                    firebaseChatRepository.sendMessage(
                        conversationId = conversationId,
                        senderId = currentUserId,
                        content = message
                    )
                } else {
                    val receiverId = _uiState.value.conversation?.getOtherParticipant(currentUserId)?.userId ?: ""
                    chatRepository.sendMessage(
                        conversationId = conversationId,
                        senderId = currentUserId,
                        receiverId = receiverId,
                        content = message
                    )
                }
                
                _uiState.value = _uiState.value.copy(currentMessage = "")
                setTypingIndicator(false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun sendLocation(latitude: Double, longitude: Double, address: String? = null) {
        viewModelScope.launch {
            try {
                if (useFirebase) {
                    firebaseChatRepository.sendMessage(
                        conversationId = conversationId,
                        senderId = currentUserId,
                        content = address ?: "Localização compartilhada",
                        type = MessageType.LOCATION
                    )
                } else {
                    val receiverId = _uiState.value.conversation?.getOtherParticipant(currentUserId)?.userId ?: ""
                    chatRepository.sendMessage(
                        conversationId = conversationId,
                        senderId = currentUserId,
                        receiverId = receiverId,
                        content = address ?: "Localização compartilhada",
                        type = MessageType.LOCATION
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun sendGif(gifUrl: String) {
        viewModelScope.launch {
            try {
                if (useFirebase) {
                    firebaseChatRepository.sendMessage(
                        conversationId = conversationId,
                        senderId = currentUserId,
                        content = gifUrl,
                        type = MessageType.GIF
                    )
                } else {
                    val receiverId = _uiState.value.conversation?.getOtherParticipant(currentUserId)?.userId ?: ""
                    chatRepository.sendMessage(
                        conversationId = conversationId,
                        senderId = currentUserId,
                        receiverId = receiverId,
                        content = gifUrl,
                        type = MessageType.GIF
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun addReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            try {
                if (useFirebase) {
                    firebaseChatRepository.addReaction(conversationId, messageId, emoji, currentUserId)
                } else {
                    chatRepository.addReaction(conversationId, messageId, emoji, currentUserId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    private fun setTypingIndicator(isTyping: Boolean) {
        if (!useFirebase) return
        
        _uiState.value = _uiState.value.copy(isTyping = isTyping)
        
        viewModelScope.launch {
            try {
                firebaseChatRepository.setTypingIndicator(conversationId, currentUserId, isTyping)
            } catch (e: Exception) {
                // Handle typing indicator errors silently
            }
        }
    }
    
    fun retryConnection() {
        if (useFirebase) {
            loadConversation(conversationId, currentUserId, useFirebase = true)
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up any Firebase listeners or resources
        if (useFirebase) {
            setTypingIndicator(false)
            viewModelScope.launch {
                try {
                    firebaseChatRepository.updateUserOnlineStatus(currentUserId, false)
                } catch (e: Exception) {
                    // Handle silently
                }
            }
        }
    }
}