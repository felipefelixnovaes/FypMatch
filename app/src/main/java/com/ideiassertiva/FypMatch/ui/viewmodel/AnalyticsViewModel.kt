package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.ideiassertiva.FypMatch.model.*
import java.util.Date

class AnalyticsViewModel : ViewModel() {
    private val _analytics = MutableStateFlow(ProfileAnalytics())
    val analytics: StateFlow<ProfileAnalytics> = _analytics.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadAnalytics(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            delay(1500)
            _analytics.value = generateMockAnalytics(userId)
            _isLoading.value = false
        }
    }
    
    private fun generateMockAnalytics(userId: String) = ProfileAnalytics()
}

data class ProfileAnalytics(
    val totalViews: Int = 0,
    val totalLikes: Int = 0,
    val totalMatches: Int = 0
)