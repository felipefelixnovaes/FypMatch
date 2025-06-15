package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AICounselorRepository
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.data.repository.AdStats
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AICounselorViewModel @Inject constructor(
    private val counselorRepository: AICounselorRepository
) : ViewModel() {
    
    // Estado da UI
    private val _uiState = MutableStateFlow(AICounselorUiState())
    val uiState: StateFlow<AICounselorUiState> = _uiState.asStateFlow()
    
    // Sessão atual
    val currentSession = counselorRepository.currentSession
    
    // Estado de carregamento
    val isLoading = counselorRepository.isLoading
    
    // Créditos do usuário
    val userCredits = counselorRepository.userCredits
    
    // Iniciar nova sessão
    fun startSession(
        userId: String,
        sessionType: SessionType = SessionType.GENERAL,
        mood: UserMood? = null,
        userSubscription: SubscriptionStatus = SubscriptionStatus.FREE
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Inicializar créditos do usuário
            counselorRepository.initializeCredits(userId, userSubscription)
            
            val result = counselorRepository.startSession(userId, sessionType, mood)
            
            result.fold(
                onSuccess = { session ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUserId = userId,
                        hasActiveSession = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Erro ao iniciar sessão"
                    )
                }
            )
        }
    }
    
    // Enviar mensagem
    fun sendMessage(message: String) {
        val userId = _uiState.value.currentUserId
        if (userId.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Usuário não identificado")
            return
        }
        
        // Verificar se tem créditos antes de enviar
        if (!counselorRepository.canSendMessage(userId)) {
            _uiState.value = _uiState.value.copy(error = "Créditos insuficientes")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentMessage = "", error = null)
            
            val result = counselorRepository.sendMessage(userId, message)
            
            result.fold(
                onSuccess = { response ->
                    // Sucesso - a resposta já foi adicionada à sessão pelo repository
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Erro ao enviar mensagem"
                    )
                }
            )
        }
    }
    
    // Atualizar mensagem atual
    fun updateCurrentMessage(message: String) {
        _uiState.value = _uiState.value.copy(currentMessage = message)
    }
    
    // Avaliar mensagem
    fun rateMessage(messageId: String, isHelpful: Boolean) {
        val userId = _uiState.value.currentUserId
        if (userId.isBlank()) return
        
        viewModelScope.launch {
            // counselorRepository.rateMessage(messageId, userId, isHelpful)
        }
    }
    
    // Finalizar sessão
    fun endSession() {
        val userId = _uiState.value.currentUserId
        if (userId.isBlank()) return
        
        viewModelScope.launch {
            // counselorRepository.endSession(userId)
            _uiState.value = _uiState.value.copy(
                hasActiveSession = false,
                currentUserId = ""
            )
        }
    }
    
    // Limpar erro
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    // Obter créditos do usuário
    fun getUserCredits(): AiCredits {
        val userId = _uiState.value.currentUserId
        return if (userId.isNotBlank()) {
            counselorRepository.getUserCredits(userId)
        } else AiCredits()
    }
    
    // Verificar se pode assistir anúncio
    fun canWatchAd(): Boolean {
        val userId = _uiState.value.currentUserId
        return if (userId.isNotBlank()) {
            counselorRepository.canWatchAd(userId)
        } else false
    }
    
    // Assistir anúncio para ganhar créditos
    fun watchAdForCredits() {
        val userId = _uiState.value.currentUserId
        if (userId.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWatchingAd = true, error = null)
            
            val result = counselorRepository.watchAdForCredits(userId)
            
            result.fold(
                onSuccess = { earnedCredits ->
                    _uiState.value = _uiState.value.copy(
                        isWatchingAd = false,
                        showAdRewardModal = true,
                        lastEarnedCredits = earnedCredits
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isWatchingAd = false,
                        error = error.message ?: "Erro ao assistir anúncio"
                    )
                }
            )
        }
    }
    
    // Fechar modal de recompensa
    fun dismissAdRewardModal() {
        _uiState.value = _uiState.value.copy(
            showAdRewardModal = false,
            lastEarnedCredits = 0
        )
    }
    
    // Obter estatísticas de anúncios
    fun getAdStats(): AdStats {
        val userId = _uiState.value.currentUserId
        return if (userId.isNotBlank()) {
            counselorRepository.getAdStats(userId)
        } else AdStats(0, 0, 0, false)
    }
}

data class AICounselorUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentMessage: String = "",
    val currentUserId: String = "",
    val hasActiveSession: Boolean = false,
    val isWatchingAd: Boolean = false,
    val showAdRewardModal: Boolean = false,
    val lastEarnedCredits: Int = 0
) 
