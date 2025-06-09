package com.example.matchreal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchreal.data.repository.DiscoveryRepository
import com.example.matchreal.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiscoveryViewModel(
    private val discoveryRepository: DiscoveryRepository = DiscoveryRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DiscoveryUiState())
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()
    
    private val _currentCard = MutableStateFlow<DiscoveryCard?>(null)
    val currentCard: StateFlow<DiscoveryCard?> = _currentCard.asStateFlow()
    
    // Simular usuário atual (no app real, viria da autenticação)
    private val currentUserId = "current_user_123"
    private val currentUserSubscription = SubscriptionStatus.FREE
    
    init {
        loadNextCard()
    }
    
    private fun loadNextCard() {
        val cards = discoveryRepository.getDiscoveryCards()
        _currentCard.value = cards.firstOrNull()
    }
    
    fun performSwipe(swipeType: SwipeType) {
        val card = _currentCard.value ?: return
        
        // Verificar limites baseados na assinatura
        when (swipeType) {
            SwipeType.LIKE -> {
                if (!discoveryRepository.checkLikeLimit(currentUserId, currentUserSubscription)) {
                    _uiState.value = _uiState.value.copy(
                        showLimitModal = true,
                        limitType = "likes"
                    )
                    return
                }
            }
            SwipeType.SUPER_LIKE -> {
                if (!discoveryRepository.checkSuperLikeLimit(currentUserId, currentUserSubscription)) {
                    _uiState.value = _uiState.value.copy(
                        showLimitModal = true,
                        limitType = "super_likes"
                    )
                    return
                }
            }
            SwipeType.PASS -> {
                // Passar não tem limites
            }
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val result = discoveryRepository.performSwipe(
                    fromUserId = currentUserId,
                    toUserId = card.user.id,
                    swipeType = swipeType
                )
                
                result.onSuccess { swipeResult ->
                    if (swipeResult.isMatch) {
                        // Mostrar modal de match
                        _uiState.value = _uiState.value.copy(
                            showMatchModal = true,
                            lastMatch = swipeResult.match,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    
                    // Carregar próximo card
                    loadNextCard()
                }
                
                result.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Erro desconhecido"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro desconhecido"
                )
            }
        }
    }
    
    fun dismissMatchModal() {
        _uiState.value = _uiState.value.copy(
            showMatchModal = false,
            lastMatch = null
        )
    }
    
    fun dismissLimitModal() {
        _uiState.value = _uiState.value.copy(
            showLimitModal = false,
            limitType = ""
        )
    }
    
    fun refreshCards() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                // Simular reload (no app real, faria nova busca no servidor)
                kotlinx.coroutines.delay(1000)
                loadNextCard()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao atualizar"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class DiscoveryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showMatchModal: Boolean = false,
    val showLimitModal: Boolean = false,
    val lastMatch: Match? = null,
    val limitType: String = ""
) 