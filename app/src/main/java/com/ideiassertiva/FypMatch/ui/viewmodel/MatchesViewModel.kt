package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AuthRepository
import com.ideiassertiva.FypMatch.data.repository.DiscoveryRepository
import com.ideiassertiva.FypMatch.data.repository.FirebaseChatRepository
import com.ideiassertiva.FypMatch.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MatchesUiState {
    /** Carregando lista de matches */
    object Loading : MatchesUiState()

    /** Matches carregados com sucesso */
    data class Success(val matches: List<Match>) : MatchesUiState()

    /** Nenhum match encontrado */
    object Empty : MatchesUiState()

    /** Erro ao carregar matches */
    data class Error(val message: String) : MatchesUiState()
}

@HiltViewModel
class MatchesViewModel @Inject constructor(
    private val discoveryRepository: DiscoveryRepository,
    private val authRepository: AuthRepository,
    private val chatRepository: FirebaseChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MatchesUiState>(MatchesUiState.Loading)
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    private val _chatNavigation = MutableStateFlow<String?>(null)
    val chatNavigation: StateFlow<String?> = _chatNavigation.asStateFlow()

    private var currentUserId = ""

    init {
        currentUserId = authRepository.getCurrentFirebaseUser()?.uid ?: ""
        loadMatches()
    }

    private fun loadMatches() {
        _uiState.value = MatchesUiState.Loading

        viewModelScope.launch {
            try {
                val userMatches = discoveryRepository.getUserMatches(currentUserId)
                _uiState.value = if (userMatches.isEmpty()) {
                    MatchesUiState.Empty
                } else {
                    MatchesUiState.Success(matches = userMatches)
                }
            } catch (e: Exception) {
                _uiState.value = MatchesUiState.Error(
                    message = e.message ?: "Erro ao carregar matches"
                )
            }
        }
    }

    fun refreshMatches() {
        loadMatches()
    }

    fun openMatchChat(match: Match) {
        viewModelScope.launch {
            try {
                if (currentUserId.isBlank()) {
                    _uiState.value = MatchesUiState.Error("Usuário não autenticado")
                    return@launch
                }
                _chatNavigation.value = chatRepository.getOrCreateConversationForMatch(
                    match = match,
                    currentUserId = currentUserId
                )
            } catch (e: Exception) {
                _uiState.value = MatchesUiState.Error(
                    message = e.message ?: "Erro ao abrir conversa"
                )
            }
        }
    }

    fun clearChatNavigation() {
        _chatNavigation.value = null
    }
}
