package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FiltersViewModel : ViewModel() {
    private val _filters = MutableStateFlow(SearchFilters())
    val filters: StateFlow<SearchFilters> = _filters.asStateFlow()

    private val _appliedFiltersCount = MutableStateFlow(0)
    val appliedFiltersCount: StateFlow<Int> = _appliedFiltersCount.asStateFlow()

    fun updateAgeRange(r: IntRange) {
        _filters.update { it.copy(ageRange = r) }
        updateCount()
    }

    fun updateMaxDistance(d: Int) {
        _filters.update { it.copy(maxDistance = d) }
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

    fun clearAllFilters() {
        _filters.value = SearchFilters()
        updateCount()
    }

    private fun updateCount() {
        _appliedFiltersCount.value = _filters.value.countActive()
    }
}
