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

sealed class ConversationsUiState {
    /** Carregando conversas do servidor */
    object Loading : ConversationsUiState()

    /** Conversas carregadas com sucesso */
    data class Success(
        val conversations: List<Conversation>,
        val users: Map<String, User>
    ) : ConversationsUiState()

    /** Erro ao carregar conversas */
    data class Error(val message: String) : ConversationsUiState()

    /** Nenhuma conversa encontrada */
    object Empty : ConversationsUiState()
}

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ConversationsUiState>(ConversationsUiState.Loading)
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    fun loadConversations(currentUserId: String) {
        _uiState.value = ConversationsUiState.Loading
        viewModelScope.launch {
            try {
                chatRepository.conversations
                    .collect { conversations ->
                        val userConversations = conversations.filter { conversation ->
                            conversation.participants.any { it.userId == currentUserId }
                        }.sortedByDescending { it.lastMessageAt ?: it.createdAt }

                        val otherUserIds = userConversations.mapNotNull { conversation ->
                            conversation.getOtherParticipant(currentUserId)?.userId
                        }.distinct()

                        val users = mutableMapOf<String, User>()
                        otherUserIds.forEach { userId ->
                            userRepository.getUserById(userId)?.let { user ->
                                users[userId] = user
                            }
                        }

                        _uiState.value = if (userConversations.isEmpty()) {
                            ConversationsUiState.Empty
                        } else {
                            ConversationsUiState.Success(
                                conversations = userConversations,
                                users = users
                            )
                        }
                    }

            } catch (e: Exception) {
                _uiState.value = ConversationsUiState.Error(
                    message = e.message ?: "Erro ao carregar conversas"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = ConversationsUiState.Loading
    }
}
