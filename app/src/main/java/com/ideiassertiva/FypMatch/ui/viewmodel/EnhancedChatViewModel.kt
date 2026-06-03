package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.FirebaseChatRepository
import com.ideiassertiva.FypMatch.data.repository.AuthRepository
import com.ideiassertiva.FypMatch.model.Conversation
import com.ideiassertiva.FypMatch.model.Message
import com.ideiassertiva.FypMatch.model.MessageType
import com.ideiassertiva.FypMatch.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

@HiltViewModel
class EnhancedChatViewModel @Inject constructor(
    private val chatRepository: FirebaseChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnhancedChatUiState())
    val uiState: StateFlow<EnhancedChatUiState> = _uiState.asStateFlow()

    private var currentConversationId: String = ""
    private var currentUserId: String = ""

    fun loadConversation(id: String, uid: String, fb: Boolean = false) {
        currentUserId = uid.ifBlank { authRepository.getCurrentUserId() ?: return }
        currentConversationId = id

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                connectionState = ConnectionState.CONNECTING
            )
            try {
                val conversation = chatRepository.getConversationById(id)
                _uiState.value = _uiState.value.copy(
                    conversation = conversation,
                    isOtherUserOnline = conversation?.isOtherUserOnline(currentUserId) ?: false,
                    connectionState = ConnectionState.CONNECTED
                )

                chatRepository.getConversationMessages(id).collect { msgs ->
                    _uiState.value = _uiState.value.copy(
                        messages = msgs.sortedBy { it.timestamp },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    connectionState = ConnectionState.ERROR,
                    error = e.message
                )
            }
        }
    }

    fun updateMessage(msg: String) {
        _uiState.value = _uiState.value.copy(currentMessage = msg)
    }

    fun sendMessage() {
        val content = _uiState.value.currentMessage.trim()
        if (content.isBlank() || currentConversationId.isBlank()) return

        viewModelScope.launch {
            try {
                chatRepository.sendMessage(
                    conversationId = currentConversationId,
                    senderId = currentUserId,
                    content = content,
                    type = MessageType.TEXT
                )
                _uiState.value = _uiState.value.copy(currentMessage = "")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun sendLocation(lat: Double, lng: Double, addr: String? = null) {
        val content = addr ?: "Localização compartilhada"
        viewModelScope.launch {
            try {
                chatRepository.sendMessage(
                    conversationId = currentConversationId,
                    senderId = currentUserId,
                    content = content,
                    type = MessageType.LOCATION
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun sendGif(url: String) {
        if (url.isBlank() || currentConversationId.isBlank()) return
        viewModelScope.launch {
            try {
                chatRepository.sendMessage(
                    conversationId = currentConversationId,
                    senderId = currentUserId,
                    content = url,
                    type = MessageType.GIF
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun addReaction(msgId: String, emoji: String) {
        if (currentConversationId.isBlank()) return
        viewModelScope.launch {
            try {
                chatRepository.addReaction(
                    conversationId = currentConversationId,
                    messageId = msgId,
                    emoji = emoji,
                    userId = currentUserId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun retryConnection() {
        if (currentConversationId.isNotBlank()) {
            loadConversation(currentConversationId, currentUserId)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
