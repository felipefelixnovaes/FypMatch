package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.UserRepository
import com.ideiassertiva.FypMatch.model.ComplementaryProfile
import com.ideiassertiva.FypMatch.model.ComplementaryProfileParser
import com.ideiassertiva.FypMatch.model.ComplementaryProfilePrompt
import com.ideiassertiva.FypMatch.model.isPresent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ComplementaryProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComplementaryProfileUiState())
    val uiState: StateFlow<ComplementaryProfileUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val currentUser = userRepository.currentUser.value
            val userId = currentUser?.id ?: userRepository.getCurrentUserId().orEmpty()
            if (currentUser == null && userId.isNotBlank()) {
                userRepository.loadCurrentUser()
            }
            val loadedUser = userRepository.currentUser.value
            val profile = loadedUser?.complementaryProfile ?: ComplementaryProfile()
            _uiState.value = _uiState.value.copy(
                currentUserId = loadedUser?.id ?: userId,
                savedProfile = profile,
                importedText = profile.rawText,
                error = null
            )
        }
    }

    fun updateImportedText(text: String) {
        _uiState.value = _uiState.value.copy(
            importedText = text,
            error = null,
            saveSuccess = false
        )
    }

    fun saveImportedText() {
        val state = _uiState.value
        val userId = state.currentUserId.ifBlank { userRepository.getCurrentUserId().orEmpty() }
        if (userId.isBlank()) {
            _uiState.value = state.copy(error = "Usuário não autenticado.")
            return
        }

        val profile = runCatching {
            ComplementaryProfileParser.parse(state.importedText)
        }.getOrElse { error ->
            _uiState.value = state.copy(error = error.message ?: "Não foi possível importar o perfil.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, saveSuccess = false)
            userRepository.saveComplementaryProfile(userId, profile).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        savedProfile = profile,
                        importedText = profile.rawText,
                        saveSuccess = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = error.message ?: "Erro ao salvar perfil complementar."
                    )
                }
            )
        }
    }

    fun clearProfile() {
        val state = _uiState.value
        val userId = state.currentUserId.ifBlank { userRepository.getCurrentUserId().orEmpty() }
        if (userId.isBlank()) {
            _uiState.value = state.copy(error = "Usuário não autenticado.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, saveSuccess = false)
            userRepository.clearComplementaryProfile(userId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        savedProfile = ComplementaryProfile(),
                        importedText = "",
                        saveSuccess = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = error.message ?: "Erro ao apagar perfil complementar."
                    )
                }
            )
        }
    }
}

data class ComplementaryProfileUiState(
    val currentUserId: String = "",
    val prompt: String = ComplementaryProfilePrompt.PERSONAL_AI_PROMPT,
    val importedText: String = "",
    val savedProfile: ComplementaryProfile = ComplementaryProfile(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
) {
    val hasSavedProfile: Boolean
        get() = savedProfile.isPresent()
}
