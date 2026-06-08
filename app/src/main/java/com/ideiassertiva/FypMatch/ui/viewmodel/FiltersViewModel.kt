package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.UserRepository
import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FiltersViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _filters = MutableStateFlow(SearchFilters())
    val filters: StateFlow<SearchFilters> = _filters.asStateFlow()

    private val _appliedFiltersCount = MutableStateFlow(0)
    val appliedFiltersCount: StateFlow<Int> = _appliedFiltersCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _savedSuccessfully = MutableStateFlow(false)
    val savedSuccessfully: StateFlow<Boolean> = _savedSuccessfully.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadFilters()
    }

    fun loadFilters() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                userRepository.loadCurrentUser()
                val user = userRepository.currentUser.value
                _filters.value = user?.preferences?.toSearchFilters() ?: SearchFilters()
                updateCount()
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao carregar filtros"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAgeRange(r: IntRange) {
        _filters.update { it.copy(ageRange = r) }
        updateCount()
    }

    fun updateMaxDistance(d: Int) {
        _filters.update { it.copy(maxDistance = d) }
        updateCount()
    }

    fun updateGenderPreference(genders: List<Gender>) {
        _filters.update { it.copy(genderPreference = genders) }
        updateCount()
    }

    fun updateIntentionPreference(intentions: List<Intention>) {
        _filters.update { it.copy(intentionPreference = intentions) }
        updateCount()
    }

    fun toggleVerifiedOnly(v: Boolean) {
        _filters.update { it.copy(verifiedOnly = v) }
        updateCount()
    }

    fun toggleRecentlyActive(v: Boolean) {
        _filters.update { it.copy(recentlyActive = v) }
        updateCount()
    }

    fun updateMinPhotos(m: Int) {
        _filters.update { it.copy(minPhotos = m) }
        updateCount()
    }

    fun updateHeightRange(r: IntRange?) {
        _filters.update { it.copy(heightRange = r) }
        updateCount()
    }

    fun updateSmokingStatus(s: List<SmokingStatus>) {
        _filters.update { it.copy(smokingStatus = s) }
        updateCount()
    }

    fun updateDrinkingStatus(d: List<DrinkingStatus>) {
        _filters.update { it.copy(drinkingStatus = d) }
        updateCount()
    }

    fun updateHasChildren(c: List<ChildrenStatus>) {
        _filters.update { it.copy(hasChildren = c) }
        updateCount()
    }

    fun updateWantsChildren(c: List<ChildrenStatus>) {
        _filters.update { it.copy(wantsChildren = c) }
        updateCount()
    }

    fun updateReligions(r: List<Religion>) {
        _filters.update { it.copy(religions = r) }
        updateCount()
    }

    fun toggleTravelMode(enabled: Boolean) {
        _filters.update { it.copy(travelModeEnabled = enabled) }
        updateCount()
    }

    fun updateTravelLocation(location: Location) {
        _filters.update { it.copy(travelLocation = location) }
        updateCount()
    }

    fun clearAllFilters() {
        _filters.value = SearchFilters()
        updateCount()
    }

    fun applyFilters() {
        viewModelScope.launch {
            _isLoading.value = true
            _savedSuccessfully.value = false
            _error.value = null
            try {
                userRepository.loadCurrentUser()
                val user = userRepository.currentUser.value
                    ?: throw IllegalStateException("Usuário não autenticado")
                val updatedUser = user.copy(
                    preferences = _filters.value.toPreferences(user.preferences)
                )
                userRepository.saveUserProfile(updatedUser).getOrThrow()
                _savedSuccessfully.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao aplicar filtros"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSavedFlag() {
        _savedSuccessfully.value = false
    }

    fun clearError() {
        _error.value = null
    }

    private fun updateCount() {
        _appliedFiltersCount.value = _filters.value.countActive()
    }
}
