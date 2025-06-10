package com.ideiassertiva.FypMatch.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AdMobRepository
import com.ideiassertiva.FypMatch.data.repository.DiscoveryRepository
import com.ideiassertiva.FypMatch.data.repository.ChatRepository
import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val discoveryRepository: DiscoveryRepository,
    private val chatRepository: ChatRepository,
    private val adMobRepository: AdMobRepository
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
                    // 🎯 Registrar swipe para AdMob (somente usuários básicos)
                    val isBasicUser = currentUserSubscription == SubscriptionStatus.FREE
                    adMobRepository.onUserSwipe(isBasicUser)
                    
                    if (swipeResult.isMatch) {
                        // Criar conversa automaticamente quando há match
                        val conversationId = chatRepository.createConversationFromMatch(
                            match = swipeResult.match!!,
                            currentUserId = currentUserId
                        )
                        
                        // Mostrar modal de match com opção de ir para chat
                        _uiState.value = _uiState.value.copy(
                            showMatchModal = true,
                            lastMatch = swipeResult.match,
                            conversationId = conversationId,
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
            lastMatch = null,
            conversationId = null
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
    
    /**
     * 🎯 Tenta exibir anúncio AdMob se necessário
     * Chame após swipes para usuários básicos
     */
    suspend fun tryShowAdMobAd(activity: Activity) {
        val isBasicUser = currentUserSubscription == SubscriptionStatus.FREE
        adMobRepository.showAdIfNeeded(activity, isBasicUser)
    }
}

data class DiscoveryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showMatchModal: Boolean = false,
    val showLimitModal: Boolean = false,
    val lastMatch: Match? = null,
    val conversationId: String? = null,
    val limitType: String = ""
) 
