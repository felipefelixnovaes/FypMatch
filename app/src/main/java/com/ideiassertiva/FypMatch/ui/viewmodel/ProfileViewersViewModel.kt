package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.ProfileViewer
import com.ideiassertiva.FypMatch.data.repository.ProfileViewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewersViewModel @Inject constructor(
    private val profileViewsRepository: ProfileViewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileViewersUiState())
    val uiState: StateFlow<ProfileViewersUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val viewers = profileViewsRepository.fetchProfileViewers()
            _uiState.value = _uiState.value.copy(isLoading = false, viewers = viewers)
        }
    }
}

data class ProfileViewersUiState(
    val isLoading: Boolean = false,
    val viewers: List<ProfileViewer> = emptyList()
)
