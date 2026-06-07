package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AdsRepository
import com.ideiassertiva.FypMatch.model.AiCredits
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AdsViewModel @Inject constructor(
    private val adsRepository: AdsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdsUiState())
    val uiState: StateFlow<AdsUiState> = _uiState.asStateFlow()

    fun load(userId: String) {
        refresh(userId)
    }

    fun watchAd(userId: String) {
        if (userId.isBlank() || _uiState.value.isWatchingAd) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isWatchingAd = true,
                showSuccess = false,
                error = null
            )

            adsRepository.showRewardedAd(userId).fold(
                onSuccess = { earnedCredits ->
                    refresh(userId)
                    _uiState.value = _uiState.value.copy(
                        isWatchingAd = false,
                        showSuccess = true,
                        lastEarnedCredits = earnedCredits,
                        error = null
                    )
                },
                onFailure = { error ->
                    refresh(userId)
                    _uiState.value = _uiState.value.copy(
                        isWatchingAd = false,
                        showSuccess = false,
                        error = error.message ?: "Erro ao assistir anuncio"
                    )
                }
            )
        }
    }

    fun dismissSuccess() {
        _uiState.value = _uiState.value.copy(showSuccess = false, lastEarnedCredits = 0)
    }

    private fun refresh(userId: String) {
        val stats = adsRepository.getAdStats(userId)
        val credits = adsRepository.getUserCredits(userId)
        _uiState.value = _uiState.value.copy(
            adsWatchedToday = stats.adsWatchedToday,
            maxAdsPerDay = stats.maxAdsPerDay,
            creditsEarnedToday = stats.creditsEarnedToday,
            canWatchMore = stats.canWatchMore,
            credits = credits
        )
    }
}

data class AdsUiState(
    val adsWatchedToday: Int = 0,
    val maxAdsPerDay: Int = 3,
    val creditsEarnedToday: Int = 0,
    val canWatchMore: Boolean = true,
    val credits: AiCredits = AiCredits(),
    val isWatchingAd: Boolean = false,
    val showSuccess: Boolean = false,
    val lastEarnedCredits: Int = 0,
    val error: String? = null
)
