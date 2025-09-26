package com.ideiassertiva.FypMatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AffiliateRepository
import com.ideiassertiva.FypMatch.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AffiliateViewModel @Inject constructor(
    private val affiliateRepository: AffiliateRepository
) : ViewModel() {
    
    // Estados da UI
    private val _uiState = MutableStateFlow(AffiliateUiState())
    val uiState: StateFlow<AffiliateUiState> = _uiState.asStateFlow()
    
    // Dados do afiliado atual
    val currentAffiliate = affiliateRepository.currentAffiliate
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // Lista de referrals
    val referrals = affiliateRepository.referrals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Solicitações de saque
    val payoutRequests = affiliateRepository.payoutRequests
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    /**
     * Registra um novo afiliado
     */
    fun registerAffiliate(
        userId: String,
        name: String,
        email: String,
        phoneNumber: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            affiliateRepository.registerAffiliate(userId, name, email, phoneNumber)
                .onSuccess { affiliate ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Parabéns! Você se tornou um afiliado FypMatch!\nSeu código: ${affiliate.code}"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Erro ao registrar afiliado"
                    )
                }
        }
    }
    
    /**
     * Solicita saque de comissões
     */
    fun requestPayout(affiliateId: String, amount: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            affiliateRepository.requestPayout(affiliateId, amount)
                .onSuccess { payoutRequest ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Solicitação de saque criada! Status: ${payoutRequest.status}"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Erro ao solicitar saque"
                    )
                }
        }
    }
    
    /**
     * Registra um referral (quando alguém usa o código do afiliado)
     */
    fun registerReferral(
        affiliateCode: String,
        referredUserId: String,
        referredUserEmail: String,
        subscriptionType: String,
        subscriptionValue: Double
    ) {
        viewModelScope.launch {
            affiliateRepository.registerReferral(
                affiliateCode,
                referredUserId,
                referredUserEmail,
                subscriptionType,
                subscriptionValue
            ).onSuccess {
                // Sucesso silencioso - só notifica o afiliado posteriormente
            }.onFailure { error ->
                // Log do erro para debugging
            }
        }
    }
    
    /**
     * Obter estatísticas do dashboard
     */
    fun loadDashboardStats(affiliateId: String) {
        viewModelScope.launch {
            val stats = affiliateRepository.getDashboardStats(affiliateId)
            _uiState.value = _uiState.value.copy(dashboardStats = stats)
        }
    }
    
    /**
     * Gera link de referência do afiliado
     */
    fun generateReferralLink(code: String): String {
        return "https://fypmatch.app/ref/$code"
    }
    
    /**
     * Calcula potencial de ganhos mensal
     */
    fun calculateEarningsPotential(referrals: Int, avgSubscriptionValue: Double = 29.90): Double {
        val premiumCommission = avgSubscriptionValue * 0.10 // 10% comissão Premium
        return referrals * premiumCommission
    }
    
    /**
     * Limpa mensagens de erro/sucesso
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}

/**
 * Estado da UI do sistema de afiliados
 */
data class AffiliateUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val dashboardStats: AffiliateStats? = null
)