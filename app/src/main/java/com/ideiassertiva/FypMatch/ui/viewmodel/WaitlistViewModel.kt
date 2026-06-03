package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.WaitlistRepository
import com.ideiassertiva.FypMatch.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WaitlistViewModel @Inject constructor(
    private val waitlistRepository: WaitlistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WaitlistUiState())
    val uiState: StateFlow<WaitlistUiState> = _uiState.asStateFlow()

    private val _waitlistCount = MutableStateFlow(0)
    val waitlistCount: StateFlow<Int> = _waitlistCount.asStateFlow()

    // Compatibilidade com WaitlistScreen
    val currentUser: StateFlow<WaitlistUser?> = MutableStateFlow<WaitlistUser?>(null).asStateFlow()
    val stats: StateFlow<WaitlistStats> = MutableStateFlow(WaitlistStats()).asStateFlow()

    init {
        loadWaitlistCount()
    }

    private fun loadWaitlistCount() {
        viewModelScope.launch {
            _waitlistCount.value = waitlistRepository.getWaitlistCount()
        }
    }

    fun joinWaitlist(
        fullName: String,
        email: String,
        city: String,
        state: String,
        age: Int,
        gender: Gender,
        orientation: Orientation,
        intention: Intention,
        inviteCode: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val alreadyOnList = waitlistRepository.isOnWaitlist(email.trim().lowercase())
            if (alreadyOnList) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Este email já está na lista de espera"
                )
                return@launch
            }

            val user = WaitlistUser(
                fullName = fullName.trim(),
                email = email.trim().lowercase(),
                city = city.trim(),
                state = state.trim(),
                age = age,
                gender = gender,
                orientation = orientation,
                intention = intention
            )

            val result = waitlistRepository.addToWaitlist(user)
            result.fold(
                onSuccess = {
                    _waitlistCount.value = waitlistRepository.getWaitlistCount()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentScreen = WaitlistScreen.SUCCESS,
                        successMessage = "Parabéns! Você está na lista de espera!"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao entrar na lista"
                    )
                }
            )
        }
    }

    fun validateForm(
        fullName: String,
        email: String,
        city: String,
        state: String,
        age: String,
        gender: Gender,
        orientation: Orientation,
        intention: Intention
    ): FormValidation {
        val errors = mutableListOf<String>()
        if (fullName.isBlank()) errors.add("Nome completo é obrigatório")
        if (email.isBlank()) errors.add("Email é obrigatório")
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) errors.add("Email inválido")
        if (city.isBlank()) errors.add("Cidade é obrigatória")
        if (state.isBlank()) errors.add("Estado é obrigatório")
        if (age.isBlank()) errors.add("Idade é obrigatória")
        else if (age.toIntOrNull() == null || age.toInt() < 18) errors.add("Idade deve ser maior que 18 anos")
        if (gender == Gender.NOT_SPECIFIED) errors.add("Gênero é obrigatório")
        if (orientation == Orientation.NOT_SPECIFIED) errors.add("Orientação sexual é obrigatória")
        if (intention == Intention.NOT_SPECIFIED) errors.add("Intenção de uso é obrigatória")
        return FormValidation(isValid = errors.isEmpty(), errors = errors)
    }

    fun validateInviteCode(code: String): Boolean = code.isBlank() // código é opcional

    fun navigateToForm() { _uiState.value = _uiState.value.copy(currentScreen = WaitlistScreen.FORM) }
    fun navigateToDashboard() { _uiState.value = _uiState.value.copy(currentScreen = WaitlistScreen.DASHBOARD) }
    fun navigateToShare() { _uiState.value = _uiState.value.copy(currentScreen = WaitlistScreen.SHARE) }
    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = null) }
    fun clearSuccess() { _uiState.value = _uiState.value.copy(successMessage = null) }
    fun refreshStats() { loadWaitlistCount() }
}

data class WaitlistUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentScreen: WaitlistScreen = WaitlistScreen.FORM
)

enum class WaitlistScreen { FORM, SUCCESS, DASHBOARD, SHARE }

data class FormValidation(val isValid: Boolean, val errors: List<String>)
