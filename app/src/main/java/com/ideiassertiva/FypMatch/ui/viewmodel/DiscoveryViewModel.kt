package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AuthRepository
import com.ideiassertiva.FypMatch.data.repository.DiscoveryRepository
import com.ideiassertiva.FypMatch.data.repository.ChatRepository
import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class DiscoveryUiState {
    /** Carregando cards do servidor */
    object Loading : DiscoveryUiState()

    /** Conteúdo normal — cards disponíveis ou nenhum */
    data class Content(
        val cardsEmpty: Boolean = false
    ) : DiscoveryUiState()

    /** Match detectado — exibir modal de match */
    data class MatchModal(
        val match: Match,
        val conversationId: String
    ) : DiscoveryUiState()

    /** Limite diário atingido — exibir modal de upgrade */
    data class LimitModal(
        val limitType: String
    ) : DiscoveryUiState()

    /** Erro — exibir snackbar/toast */
    data class Error(val message: String) : DiscoveryUiState()
}

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val discoveryRepository: DiscoveryRepository,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiscoveryUiState>(DiscoveryUiState.Loading)
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    private val _currentCard = MutableStateFlow<DiscoveryCard?>(null)
    val currentCard: StateFlow<DiscoveryCard?> = _currentCard.asStateFlow()

    private val _boostActive = MutableStateFlow(false)
    val boostActive: StateFlow<Boolean> = _boostActive.asStateFlow()

    private var currentUserId = ""
    private val currentUserSubscription = SubscriptionStatus.FREE

    /** Stack dos últimos cards swipados para rewind */
    private val swipeHistory = ArrayDeque<DiscoveryCard>()

    init {
        currentUserId = authRepository.getCurrentFirebaseUser()?.uid ?: ""
        loadNextCard()
    }

    private fun loadNextCard() {
        val cards = discoveryRepository.getDiscoveryCards()
        _currentCard.value = cards.firstOrNull()
    }

    fun performSwipe(swipeType: SwipeType) {
        val card = _currentCard.value ?: return

        when (swipeType) {
            SwipeType.LIKE -> {
                if (!discoveryRepository.checkLikeLimit(currentUserId, currentUserSubscription)) {
                    _uiState.value = DiscoveryUiState.LimitModal(limitType = "likes")
                    return
                }
            }
            SwipeType.SUPER_LIKE -> {
                if (!discoveryRepository.checkSuperLikeLimit(currentUserId, currentUserSubscription)) {
                    _uiState.value = DiscoveryUiState.LimitModal(limitType = "super_likes")
                    return
                }
            }
            SwipeType.PASS -> { /* sem limites */ }
        }

        swipeHistory.addFirst(card)
        if (swipeHistory.size > 10) swipeHistory.removeLast()

        _uiState.value = DiscoveryUiState.Loading

        viewModelScope.launch {
            try {
                val result = discoveryRepository.performSwipe(
                    fromUserId = currentUserId,
                    toUserId = card.user.id,
                    swipeType = swipeType
                )

                result.onSuccess { swipeResult ->
                    if (swipeResult.isMatch) {
                        val conversationId = chatRepository.createConversationFromMatch(
                            match = swipeResult.match!!,
                            currentUserId = currentUserId
                        )
                        _uiState.value = DiscoveryUiState.MatchModal(
                            match = swipeResult.match,
                            conversationId = conversationId
                        )
                    } else {
                        loadNextCard()
                        _uiState.value = DiscoveryUiState.Content(
                            cardsEmpty = _currentCard.value == null
                        )
                    }
                }

                result.onFailure { exception ->
                    _uiState.value = DiscoveryUiState.Error(
                        message = exception.message ?: "Erro desconhecido"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = DiscoveryUiState.Error(
                    message = e.message ?: "Erro desconhecido"
                )
            }
        }
    }

    /** Desfaz o último swipe, reinserindo o card no topo */
    fun rewindLastSwipe() {
        val lastCard = swipeHistory.removeFirstOrNull() ?: return
        _currentCard.value = lastCard
    }

    /** Ativa o boost por 30 minutos */
    fun activateBoost() {
        if (_boostActive.value) return
        viewModelScope.launch {
            _boostActive.value = true
            delay(30 * 60 * 1000L)
            _boostActive.value = false
        }
    }

    fun dismissMatchModal() {
        loadNextCard()
        _uiState.value = DiscoveryUiState.Content(
            cardsEmpty = _currentCard.value == null
        )
    }

    fun dismissLimitModal() {
        _uiState.value = DiscoveryUiState.Content()
    }

    fun refreshCards() {
        _uiState.value = DiscoveryUiState.Loading
        viewModelScope.launch {
            try {
                delay(1000)
                loadNextCard()
                _uiState.value = DiscoveryUiState.Content(
                    cardsEmpty = _currentCard.value == null
                )
            } catch (e: Exception) {
                _uiState.value = DiscoveryUiState.Error(
                    message = e.message ?: "Erro ao atualizar"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = DiscoveryUiState.Content()
    }
}
