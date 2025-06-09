package com.example.matchreal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchreal.data.repository.DiscoveryRepository
import com.example.matchreal.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchesViewModel(
    private val discoveryRepository: DiscoveryRepository = DiscoveryRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MatchesUiState())
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()
    
    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()
    
    // Simular usuário atual
    private val currentUserId = "current_user_123"
    
    init {
        loadMatches()
    }
    
    private fun loadMatches() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val userMatches = discoveryRepository.getUserMatches(currentUserId)
                _matches.value = userMatches
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao carregar matches"
                )
            }
        }
    }
    
    fun refreshMatches() {
        loadMatches()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class MatchesUiState(
    val isLoading: Boolean = false,
    val error: String? = null
) 