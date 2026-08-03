package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AuthRepository
import com.ideiassertiva.FypMatch.data.repository.UserRepository
import com.ideiassertiva.FypMatch.data.repository.UsernameRepository
import com.ideiassertiva.FypMatch.model.User
import com.ideiassertiva.FypMatch.model.withCompletionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val usernameRepository: UsernameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileEditUiState())
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                userRepository.loadCurrentUser()
                val user = userRepository.currentUser.value
                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Usuário não encontrado"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao carregar perfil"
                )
            }
        }
    }

    fun saveUser(user: User) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                if (user.profile.age < 18) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "O FypMatch é exclusivo para maiores de 18 anos."
                    )
                    return@launch
                }
                val userId = user.id.ifBlank { authRepository.getCurrentUserId() ?: "" }
                val uploadedPhotos = if (userId.isNotBlank()) {
                    userRepository.uploadPhotos(userId, user.profile.photos)
                } else {
                    user.profile.photos
                }
                val userWithPhotos = user.copy(
                    profile = user.profile.copy(
                        photos = uploadedPhotos
                    ).withCompletionStatus()
                )
                val result = userRepository.saveUserProfile(userWithPhotos)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            savedSuccessfully = true,
                            user = userWithPhotos
                        )
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = e.message ?: "Erro ao salvar perfil"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro inesperado"
                )
            }
        }
    }

    fun claimUsername(rawUsername: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSavingUsername = true, usernameError = null)
            val result = usernameRepository.claimUsername(rawUsername)
            result.fold(
                onSuccess = { normalized ->
                    _uiState.value = _uiState.value.copy(
                        isSavingUsername = false,
                        user = _uiState.value.user?.copy(username = normalized),
                        usernameSavedSuccessfully = true
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isSavingUsername = false,
                        usernameError = e.message ?: "Erro ao salvar username"
                    )
                }
            )
        }
    }

    fun clearUsernameStatus() {
        _uiState.value = _uiState.value.copy(usernameSavedSuccessfully = false, usernameError = null)
    }
}

data class ProfileEditUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val savedSuccessfully: Boolean = false,
    val error: String? = null,
    val isSavingUsername: Boolean = false,
    val usernameSavedSuccessfully: Boolean = false,
    val usernameError: String? = null
)
