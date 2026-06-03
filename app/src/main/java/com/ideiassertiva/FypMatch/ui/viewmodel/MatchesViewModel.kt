package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AuthRepository
import com.ideiassertiva.FypMatch.data.repository.DiscoveryRepository
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MatchesUiState>(MatchesUiState.Loading)
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

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
}
