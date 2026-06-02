package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.ChatRepository
import com.ideiassertiva.FypMatch.data.repository.UserRepository
import com.ideiassertiva.FypMatch.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChatUiState {
    /** Carregando conversa e mensagens iniciais */
    object Loading : ChatUiState()

    /** Conversa carregada — mensagens em tempo real */
    data class Success(
        val conversation: Conversation?,
        val messages: List<Message>,
        val otherUser: User?,
        val currentMessage: String = "",
        val isTyping: Boolean = false
    ) : ChatUiState()

    /** Erro ao carregar ou enviar */
    data class Error(val message: String) : ChatUiState()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var conversationId: String = ""
    private var currentUserId: String = ""
    private var messagesJob: Job? = null

    fun loadConversation(conversationId: String, currentUserId: String) {
        this.conversationId = conversationId
        this.currentUserId = currentUserId

        messagesJob?.cancel()
        _uiState.value = ChatUiState.Loading

        viewModelScope.launch {
            try {
                val conversation = chatRepository.getConversationById(conversationId)
                val otherUserId = conversation?.getOtherParticipant(currentUserId)?.userId
                val otherUser = otherUserId?.let { userRepository.getUserById(it) }

                _uiState.value = ChatUiState.Success(
                    conversation = conversation,
                    messages = emptyList(),
                    otherUser = otherUser
                )

                messagesJob = viewModelScope.launch {
                    chatRepository.getConversationMessages(conversationId)
                        .collect { messages ->
                            val current = _uiState.value
                            if (current is ChatUiState.Success) {
                                _uiState.value = current.copy(
                                    messages = messages.sortedBy { it.timestamp }
                                )
                            }
                        }
                }

            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error(
                    message = e.message ?: "Erro ao carregar conversa"
                )
            }
        }
    }

    fun updateMessageText(text: String) {
        val current = _uiState.value
        if (current is ChatUiState.Success) {
            _uiState.value = current.copy(currentMessage = text, isTyping = text.isNotEmpty())
        }
    }

    fun sendMessage() {
        val current = _uiState.value
        if (current !is ChatUiState.Success) return
        val message = current.currentMessage.trim()
        if (message.isEmpty()) return

        _uiState.value = current.copy(currentMessage = "", isTyping = false)

        viewModelScope.launch {
            try {
                chatRepository.sendMessage(
                    conversationId = conversationId,
                    senderId = currentUserId,
                    content = message
                )
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error(
                    message = e.message ?: "Erro ao enviar mensagem"
                )
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
                _uiState.value = ChatUiState.Error(
                    message = e.message ?: "Erro ao enviar localização"
                )
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
                _uiState.value = ChatUiState.Error(
                    message = e.message ?: "Erro ao enviar GIF"
                )
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
                _uiState.value = ChatUiState.Error(
                    message = e.message ?: "Erro ao adicionar reação"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        messagesJob?.cancel()
    }

    fun clearError() {
        val current = _uiState.value
        _uiState.value = if (current is ChatUiState.Error) {
            ChatUiState.Success(conversation = null, messages = emptyList(), otherUser = null)
        } else {
            current
        }
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
        val current = _uiState.value
        return if (current is ChatUiState.Success) {
            current.conversation?.isOtherUserTyping(currentUserId) ?: false
        } else {
            false
        }
    }

    fun getLastSeenText(): String {
        val current = _uiState.value
        return if (current is ChatUiState.Success) {
            current.conversation?.getLastSeenFormatted(currentUserId) ?: ""
        } else {
            ""
        }
    }
}
