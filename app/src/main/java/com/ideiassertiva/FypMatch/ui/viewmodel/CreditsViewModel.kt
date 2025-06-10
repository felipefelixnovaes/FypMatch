package com.ideiassertiva.FypMatch.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.RewardedAdsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreditsViewModel @Inject constructor(
    private val rewardedAdsRepository: RewardedAdsRepository
) : ViewModel() {
    
    // Estados dos créditos
    val credits = rewardedAdsRepository.credits
    val isAdLoaded = rewardedAdsRepository.isAdLoaded
    val isLoading = rewardedAdsRepository.isLoading
    
    // Estados da UI
    private val _showRewardDialog = MutableStateFlow(false)
    val showRewardDialog: StateFlow<Boolean> = _showRewardDialog.asStateFlow()
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    private val _canChatWithFype = MutableStateFlow(false)
    val canChatWithFype: StateFlow<Boolean> = _canChatWithFype.asStateFlow()
    
    init {
        // Observar mudanças nos créditos
        viewModelScope.launch {
            credits.collect { creditCount ->
                _canChatWithFype.value = creditCount > 0
            }
        }
    }
    
    /**
     * 🎥 Assistir anúncio para ganhar créditos
     */
    fun watchAdForCredits(activity: Activity) {
        if (!isAdLoaded.value) {
            _message.value = "Carregando anúncio... Aguarde alguns segundos."
            rewardedAdsRepository.loadRewardedAd()
            return
        }
        
        rewardedAdsRepository.showRewardedAd(
            activity = activity,
            onReward = { earnedCredits ->
                _message.value = "🎉 Você ganhou $earnedCredits créditos para falar com a Fype!"
                _showRewardDialog.value = true
            },
            onClosed = {
                // Anúncio foi fechado
            },
            onFailed = { error ->
                _message.value = "Erro ao exibir anúncio: $error"
            }
        )
    }
    
    /**
     * 💬 Tentar iniciar chat com a Fype
     */
    fun startChatWithFype(): Boolean {
        return if (rewardedAdsRepository.hasCredits(1)) {
            rewardedAdsRepository.spendCredits(1)
            _message.value = "Iniciando conversa com a Fype... ✨"
            true
        } else {
            _message.value = "Você precisa de créditos para falar com a Fype. Assista a um vídeo!"
            false
        }
    }
    
    /**
     * ✉️ Enviar mensagem para a Fype (gasta 1 crédito)
     */
    fun sendMessageToFype(message: String): Boolean {
        return if (rewardedAdsRepository.hasCredits(1)) {
            rewardedAdsRepository.spendCredits(1)
            true
        } else {
            _message.value = "Créditos insuficientes! Assista a um vídeo para continuar."
            false
        }
    }
    
    /**
     * 🔄 Recarregar anúncio
     */
    fun reloadAd() {
        rewardedAdsRepository.loadRewardedAd()
    }
    
    /**
     * ❌ Fechar dialog de recompensa
     */
    fun closeRewardDialog() {
        _showRewardDialog.value = false
    }
    
    /**
     * 🧹 Limpar mensagem
     */
    fun clearMessage() {
        _message.value = ""
    }
    
    /**
     * 🔧 Resetar créditos (para debug)
     */
    fun resetCredits() {
        rewardedAdsRepository.resetCredits()
    }
} 