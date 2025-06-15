package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AuthRepository
import com.ideiassertiva.FypMatch.data.repository.EmailVerificationState
import com.ideiassertiva.FypMatch.util.AnalyticsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class SignUpUiState(
    val isLoading: Boolean = false,
    val isSignUpSuccess: Boolean = false,
    val errorMessage: String? = null,
    val needsEmailVerification: Boolean = false,
    val verificationMessage: String? = null
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()
    
    init {
        // Observar estado de verificação de email
        viewModelScope.launch {
            authRepository.emailVerificationState.collect { verificationState ->
                when (verificationState) {
                    is EmailVerificationState.Idle -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            needsEmailVerification = false
                        )
                    }
                    is EmailVerificationState.SendingCode -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                    is EmailVerificationState.CodeSent -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            needsEmailVerification = true,
                            verificationMessage = "Email de verificação enviado! Verifique sua caixa de entrada."
                        )
                    }
                    is EmailVerificationState.Verified -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSignUpSuccess = true,
                            needsEmailVerification = false
                        )
                    }
                    is EmailVerificationState.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = verificationState.message,
                            needsEmailVerification = false
                        )
                    }
                }
            }
        }
    }
    
    fun signUpWithEmail(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            // Validações básicas
            if (fullName.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Nome completo é obrigatório"
                )
                return@launch
            }
            
            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Email inválido"
                )
                return@launch
            }
            
            if (password.length < 6) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Senha deve ter pelo menos 6 caracteres"
                )
                return@launch
            }
            
            // Analytics: Tentativa de cadastro
            analyticsManager.logCustomCrash("email_signup_attempt", mapOf(
                "email_domain" to email.substringAfter("@")
            ))
            
            println("🔍 DEBUG - Iniciando cadastro com email: $email")
            
            val result = authRepository.signUpWithEmail(email, password)
            
            result.fold(
                onSuccess = { message ->
                    println("🔍 DEBUG - Cadastro iniciado com sucesso: $message")
                    
                    // O estado será atualizado automaticamente pelo observer
                    // do EmailVerificationState
                    
                    // Analytics: Cadastro iniciado
                    analyticsManager.logCustomCrash("email_signup_verification_sent", mapOf(
                        "email" to email
                    ))
                },
                onFailure = { error ->
                    val errorMessage = error.message ?: "Erro no cadastro"
                    
                    println("🔍 DEBUG - Erro no cadastro: $errorMessage")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                    
                    analyticsManager.logError(error, "email_signup_failure")
                }
            )
        }
    }
    
    fun checkEmailVerification() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            val result = authRepository.checkEmailVerification()
            
            result.fold(
                onSuccess = { user ->
                    println("🔍 DEBUG - Email verificado com sucesso: ${user.email}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignUpSuccess = true,
                        needsEmailVerification = false
                    )
                    
                    // Analytics: Cadastro completo
                    analyticsManager.logUserSignUp("email")
                    analyticsManager.logUserProfile(
                        user.profile.age.takeIf { it > 0 },
                        user.profile.gender.name.takeIf { it != "NOT_SPECIFIED" }
                    )
                },
                onFailure = { error ->
                    val errorMessage = error.message ?: "Erro na verificação"
                    
                    println("🔍 DEBUG - Erro na verificação: $errorMessage")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
            )
        }
    }
    
    fun resendEmailVerification() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            val result = authRepository.resendEmailVerification()
            
            result.fold(
                onSuccess = { message ->
                    println("🔍 DEBUG - Email reenviado: $message")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        verificationMessage = message
                    )
                    
                    analyticsManager.logCustomCrash("email_verification_resent", emptyMap())
                },
                onFailure = { error ->
                    val errorMessage = error.message ?: "Erro ao reenviar email"
                    
                    println("🔍 DEBUG - Erro ao reenviar: $errorMessage")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun clearVerificationMessage() {
        _uiState.value = _uiState.value.copy(verificationMessage = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        // Limpar estado de verificação quando ViewModel for destruído
        authRepository.clearEmailVerificationState()
    }
} 