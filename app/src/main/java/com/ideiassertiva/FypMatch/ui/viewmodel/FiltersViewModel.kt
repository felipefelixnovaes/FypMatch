package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import com.ideiassertiva.FypMatch.model.*

class FiltersViewModel : ViewModel() {
    private val _filters = MutableStateFlow(AdvancedFilters())
    val filters: StateFlow<AdvancedFilters> = _filters.asStateFlow()
    
    val appliedFiltersCount: StateFlow<Int> = _filters.map { filters ->
        var count = 0
        
        // Check if basic filters are different from default
        if (filters.ageRange != 18..99) count++
        if (filters.maxDistance != 50) count++
        if (filters.verifiedOnly) count++
        
        // Check advanced filters
        if (filters.recentlyActive) count++
        if (filters.minPhotos > 1) count++
        if (filters.heightRange != null) count++
        
        // Check lifestyle filters
        if (filters.smokingStatus.isNotEmpty()) count++
        if (filters.drinkingStatus.isNotEmpty()) count++
        if (filters.hasChildren.isNotEmpty()) count++
        if (filters.wantsChildren.isNotEmpty()) count++
        if (filters.religions.isNotEmpty()) count++
        if (filters.petPreference.isNotEmpty()) count++
        
        count
    }.stateIn(
        scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main),
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    
    fun updateAgeRange(range: IntRange) {
        _filters.value = _filters.value.copy(ageRange = range)
    }
    
    fun updateMaxDistance(distance: Int) {
        _filters.value = _filters.value.copy(maxDistance = distance)
    }
    
    fun toggleVerifiedOnly(verified: Boolean) {
        _filters.value = _filters.value.copy(verifiedOnly = verified)
    }
    
    fun toggleRecentlyActive(active: Boolean) {
        _filters.value = _filters.value.copy(recentlyActive = active)
    }
    
    fun updateMinPhotos(min: Int) {
        _filters.value = _filters.value.copy(minPhotos = min)
    }
    
    fun updateHeightRange(range: IntRange?) {
        _filters.value = _filters.value.copy(heightRange = range)
    }
    
    fun updateSmokingStatus(statuses: List<SmokingStatus>) {
        _filters.value = _filters.value.copy(smokingStatus = statuses)
    }
    
    fun updateDrinkingStatus(statuses: List<DrinkingStatus>) {
        _filters.value = _filters.value.copy(drinkingStatus = statuses)
    }
    
    fun updateHasChildren(statuses: List<ChildrenStatus>) {
        _filters.value = _filters.value.copy(hasChildren = statuses)
    }
    
    fun updateWantsChildren(statuses: List<ChildrenStatus>) {
        _filters.value = _filters.value.copy(wantsChildren = statuses)
    }
    
    fun updateReligions(religions: List<Religion>) {
        _filters.value = _filters.value.copy(religions = religions)
    }
    
    fun updatePetPreference(preferences: List<PetPreference>) {
        _filters.value = _filters.value.copy(petPreference = preferences)
    }
    
    fun updateInterests(interests: List<String>) {
        _filters.value = _filters.value.copy(interests = interests)
    }
    
    fun clearAllFilters() {
        _filters.value = AdvancedFilters()
    }
    
    fun saveFilters() {
        // In real app, this would save to repository/preferences
        // For now, just keeping in memory
    }
    
    fun loadSavedFilters(userId: String) {
        // In real app, this would load from repository/preferences
        // For now, using default values
    }
}