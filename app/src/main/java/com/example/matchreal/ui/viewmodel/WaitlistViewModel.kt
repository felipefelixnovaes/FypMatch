package com.example.matchreal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchreal.data.repository.WaitlistRepository
import com.example.matchreal.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WaitlistViewModel(
    private val waitlistRepository: WaitlistRepository = WaitlistRepository()
) : ViewModel() {
    
    // Estado da UI
    private val _uiState = MutableStateFlow(WaitlistUiState())
    val uiState: StateFlow<WaitlistUiState> = _uiState.asStateFlow()
    
    // Dados do usuário atual
    private val _currentUser = MutableStateFlow<WaitlistUser?>(null)
    val currentUser: StateFlow<WaitlistUser?> = _currentUser.asStateFlow()
    
    // Estatísticas da lista de espera
    private val _stats = MutableStateFlow(WaitlistStats())
    val stats: StateFlow<WaitlistStats> = _stats.asStateFlow()
    
    init {
        observeWaitlistData()
    }
    
    private fun observeWaitlistData() {
        viewModelScope.launch {
            waitlistRepository.currentUser.collect { user ->
                _currentUser.value = user
                if (user != null) {
                    _stats.value = waitlistRepository.getWaitlistStats()
                    _uiState.value = _uiState.value.copy(
                        currentScreen = WaitlistScreen.DASHBOARD
                    )
                }
            }
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
            
            val result = waitlistRepository.joinWaitlist(
                fullName = fullName.trim(),
                email = email.trim().lowercase(),
                city = city.trim(),
                state = state.trim(),
                age = age,
                gender = gender,
                orientation = orientation,
                intention = intention,
                invitedByCode = inviteCode?.trim()?.uppercase()
            )
            
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentScreen = WaitlistScreen.SUCCESS,
                        successMessage = "Parabéns! Você está na lista de espera!"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro desconhecido"
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
        else if (!isValidEmail(email)) errors.add("Email inválido")
        if (city.isBlank()) errors.add("Cidade é obrigatória")
        if (state.isBlank()) errors.add("Estado é obrigatório")
        if (age.isBlank()) errors.add("Idade é obrigatória")
        else if (age.toIntOrNull() == null || age.toInt() < 18) errors.add("Idade deve ser maior que 18 anos")
        if (gender == Gender.NOT_SPECIFIED) errors.add("Gênero é obrigatório")
        if (orientation == Orientation.NOT_SPECIFIED) errors.add("Orientação sexual é obrigatória")
        if (intention == Intention.NOT_SPECIFIED) errors.add("Intenção de uso é obrigatória")
        
        return FormValidation(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    fun validateInviteCode(code: String): Boolean {
        return if (code.isBlank()) true // Código é opcional
        else waitlistRepository.validateInviteCode(code.uppercase())
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun navigateToForm() {
        _uiState.value = _uiState.value.copy(currentScreen = WaitlistScreen.FORM)
    }
    
    fun navigateToDashboard() {
        _uiState.value = _uiState.value.copy(currentScreen = WaitlistScreen.DASHBOARD)
    }
    
    fun navigateToShare() {
        _uiState.value = _uiState.value.copy(currentScreen = WaitlistScreen.SHARE)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
    
    fun refreshStats() {
        viewModelScope.launch {
            _stats.value = waitlistRepository.getWaitlistStats()
        }
    }
}

data class WaitlistUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentScreen: WaitlistScreen = WaitlistScreen.FORM
)

enum class WaitlistScreen {
    FORM,       // Formulário de cadastro
    SUCCESS,    // Tela de sucesso
    DASHBOARD,  // Dashboard com estatísticas
    SHARE       // Tela de compartilhamento
}

data class FormValidation(
    val isValid: Boolean,
    val errors: List<String>
) 