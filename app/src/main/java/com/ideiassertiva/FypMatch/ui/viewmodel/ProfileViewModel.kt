package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AuthRepository
import com.ideiassertiva.FypMatch.data.repository.UserRepository
import com.ideiassertiva.FypMatch.model.Gender
import com.ideiassertiva.FypMatch.model.Intention
import com.ideiassertiva.FypMatch.model.Location
import com.ideiassertiva.FypMatch.model.Orientation
import com.ideiassertiva.FypMatch.model.UserProfile
import com.ideiassertiva.FypMatch.model.withCompletionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun saveProfile(
        fullName: String,
        age: String,
        location: Location,
        bio: String,
        gender: Gender,
        orientation: Orientation,
        intention: Intention
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val userId = authRepository.getCurrentUserId() ?: run {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Usuário não autenticado")
                    return@launch
                }
                val profile = UserProfile(
                    fullName = fullName,
                    age = age.toIntOrNull() ?: 0,
                    bio = bio,
                    location = location,
                    gender = gender,
                    orientation = orientation,
                    intention = intention
                ).withCompletionStatus()
                val result = userRepository.saveProfile(userId, profile)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(isLoading = false, savedSuccessfully = true)
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao salvar perfil")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro inesperado")
            }
        }
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val error: String? = null
)
