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

data class ConversationsUiState(
    val conversations: List<Conversation> = emptyList(),
    val users: Map<String, User> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()
    
    fun loadConversations(currentUserId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                chatRepository.conversations
                    .collect { conversations ->
                        val userConversations = conversations.filter { conversation ->
                            conversation.participants.any { it.userId == currentUserId }
                        }.sortedByDescending { it.lastMessageAt ?: it.createdAt }
                        
                        // Carrega informações dos outros usuários
                        val otherUserIds = userConversations.mapNotNull { conversation ->
                            conversation.getOtherParticipant(currentUserId)?.userId
                        }.distinct()
                        
                        val users = mutableMapOf<String, User>()
                        otherUserIds.forEach { userId ->
                            userRepository.getUserFromFirestore(userId).fold(
                                onSuccess = { user -> 
                                    if (user != null) users[userId] = user 
                                },
                                onFailure = { /* Ignorar erro */ }
                            )
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            conversations = userConversations,
                            users = users,
                            isLoading = false
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
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 
