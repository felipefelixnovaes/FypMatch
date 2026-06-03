package com.ideiassertiva.FypMatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AffiliateRepository
import com.ideiassertiva.FypMatch.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AffiliateViewModel @Inject constructor(
    private val affiliateRepository: AffiliateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AffiliateUiState())
    val uiState: StateFlow<AffiliateUiState> = _uiState.asStateFlow()

    private val _currentAffiliate = MutableStateFlow<Affiliate?>(null)
    val currentAffiliate: StateFlow<Affiliate?> = _currentAffiliate.asStateFlow()

    // Compatibilidade com screens que usam referrals como StateFlow
    private val _referrals = MutableStateFlow<List<Referral>>(emptyList())
    val referrals: StateFlow<List<Referral>> = _referrals.asStateFlow()

    private val _payoutRequests = MutableStateFlow<List<PayoutRequest>>(emptyList())
    val payoutRequests: StateFlow<List<PayoutRequest>> = _payoutRequests.asStateFlow()

    fun loadAffiliate(affiliateId: String) {
        viewModelScope.launch {
            _currentAffiliate.value = affiliateRepository.getAffiliate(affiliateId)
            if (affiliateId.isNotBlank()) loadDashboardStats(affiliateId)
        }
    }

    fun registerAffiliate(userId: String, name: String, email: String, phoneNumber: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = affiliateRepository.registerAffiliate(userId, name, email, phoneNumber)
            result.fold(
                onSuccess = { affiliate: Affiliate ->
                    _currentAffiliate.value = affiliate
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Parabéns! Você se tornou um afiliado FypMatch!\nSeu código: ${affiliate.code}"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Erro ao registrar afiliado"
                    )
                }
            )
        }
    }

    fun requestPayout(affiliateId: String, amount: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = affiliateRepository.requestPayout(affiliateId, amount)
            result.fold(
                onSuccess = { payoutRequest: PayoutRequest ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Solicitação de saque criada! Status: ${payoutRequest.status}"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Erro ao solicitar saque"
                    )
                }
            )
        }
    }

    fun registerReferral(
        affiliateCode: String,
        referredUserId: String,
        referredUserEmail: String,
        subscriptionType: String,
        subscriptionValue: Double
    ) {
        viewModelScope.launch {
            affiliateRepository.registerReferral(
                affiliateCode, referredUserId, referredUserEmail,
                subscriptionType, subscriptionValue
            )
        }
    }

    fun loadDashboardStats(affiliateId: String) {
        viewModelScope.launch {
            val stats = affiliateRepository.getDashboardStats(affiliateId)
            _uiState.value = _uiState.value.copy(dashboardStats = stats)
        }
    }

    fun generateReferralLink(code: String): String = "https://fypmatch.app/ref/$code"

    fun calculateEarningsPotential(referrals: Int, avgSubscriptionValue: Double = 29.90): Double =
        referrals * avgSubscriptionValue * 0.10

    fun clearMessages() { _uiState.value = _uiState.value.copy(error = null, successMessage = null) }
}

data class AffiliateUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val dashboardStats: AffiliateStats? = null
)
