package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ideiassertiva.FypMatch.model.Conversation
import com.ideiassertiva.FypMatch.model.Message
import com.ideiassertiva.FypMatch.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EnhancedChatUiState(
    val messages: List<Message> = emptyList(),
    val otherUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTyping: Boolean = false,
    val currentMessage: String = "",
    val isOtherUserTyping: Boolean = false,
    val isOtherUserOnline: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val conversation: Conversation? = null
)

enum class ConnectionState { CONNECTED, CONNECTING, DISCONNECTED, ERROR }

class EnhancedChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EnhancedChatUiState())
    val uiState: StateFlow<EnhancedChatUiState> = _uiState.asStateFlow()
    
    fun loadConversation(id: String, uid: String, fb: Boolean = false) {
        // Implementation to be added
    }
    
    fun updateMessage(msg: String) {
        _uiState.value = _uiState.value.copy(currentMessage = msg)
    }
    
    fun sendMessage() {
        // Implementation to be added
    }
    
    fun sendLocation(lat: Double, lng: Double, addr: String? = null) {
        // Implementation to be added
    }
    
    fun sendGif(url: String) {
        // Implementation to be added
    }
    
    fun addReaction(msgId: String, emoji: String) {
        // Implementation to be added
    }
    
    fun retryConnection() {
        // Implementation to be added
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
