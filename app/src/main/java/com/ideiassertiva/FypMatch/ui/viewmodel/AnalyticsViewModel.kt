package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.ideiassertiva.FypMatch.model.*
import java.util.Date

class AnalyticsViewModel : ViewModel() {
    private val _analytics = MutableStateFlow(ProfileAnalytics())
    val analytics: StateFlow<ProfileAnalytics> = _analytics.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadAnalytics(userId: String) {
        _isLoading.value = true
        
        // Simulate loading analytics data
        // In real app, this would fetch from repository/API
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            kotlinx.coroutines.delay(1500) // Simulate API call
            
            _analytics.value = generateMockAnalytics(userId)
            _isLoading.value = false
        }
    }
    
    private fun generateMockAnalytics(userId: String): ProfileAnalytics {
        return ProfileAnalytics(
            userId = userId,
            profileViews = ProfileViewStats(
                totalViews = 1247,
                uniqueViews = 982,
                viewsToday = 23,
                viewsThisWeek = 158,
                viewsThisMonth = 623,
                peakHour = 20,
                peakDay = "Sexta-feira",
                averageViewsPerDay = 18.5
            ),
            matchStats = MatchStats(
                totalMatches = 89,
                matchesToday = 2,
                matchesThisWeek = 12,
                matchesThisMonth = 34,
                matchRate = 28.5,
                averageMatchesPerDay = 1.8,
                bestMatchingTime = "Noite"
            ),
            chatStats = ChatStats(
                conversationsStarted = 67,
                messagesReceived = 234,
                messagesSent = 189,
                responseRate = 72.5,
                averageResponseTime = 25,
                activeConversations = 8
            ),
            activityStats = ActivityStats(
                daysActive = 45,
                totalSwipes = 3420,
                likesGiven = 987,
                likesReceived = 456,
                superLikesReceived = 23,
                profileUpdates = 7,
                lastActiveTime = Date()
            ),
            lastUpdated = Date()
        )
    }
    
    fun refreshAnalytics(userId: String) {
        loadAnalytics(userId)
    }
}