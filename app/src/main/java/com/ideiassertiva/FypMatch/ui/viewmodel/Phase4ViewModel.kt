package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.Phase4AIRepository
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.ui.screens.DemoResult
import com.ideiassertiva.FypMatch.ui.screens.Phase4Stats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Phase4UIState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val demoResults: List<DemoResult> = emptyList(),
    val phase4Stats: Phase4Stats = Phase4Stats(),
    val showNeuroSupportDialog: Boolean = false
)

class Phase4ViewModel @Inject constructor(
    private val phase4Repository: Phase4AIRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(Phase4UIState())
    val uiState: StateFlow<Phase4UIState> = _uiState.asStateFlow()
    
    private val _personalityProfile = MutableStateFlow<PersonalityProfile?>(null)
    val personalityProfile: StateFlow<PersonalityProfile?> = _personalityProfile.asStateFlow()
    
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()
    
    private val _currentUserId = MutableStateFlow("")
    
    fun initializeUser(userId: String) {
        _currentUserId.value = userId
    }
    
    // FIXME: All demo methods simplified — original models (UserProfile, SwipeRecord) not available
    fun analyzePersonality() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val result = phase4Repository.analyzePersonality(null, emptyList())
                _personalityProfile.value = result.getOrNull() as? PersonalityProfile
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun runCompatibilityDemo() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                phase4Repository.analyzeCompatibility("", "", null, null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun showNeuroSupportDemo() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                phase4Repository.createNeuroProfile("", null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun generateSmartSuggestions() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                phase4Repository.generateSmartSuggestions("", emptyList(), "")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun analyzeBehaviorPatterns() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                phase4Repository.analyzeSwipeBehavior("", emptyList())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
    
    fun clearDemoResults() {
        _uiState.value = _uiState.value.copy(demoResults = emptyList())
    }
    
    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}