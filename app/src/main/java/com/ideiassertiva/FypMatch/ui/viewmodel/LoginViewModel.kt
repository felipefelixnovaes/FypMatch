package com.ideiassertiva.FypMatch.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AuthRepository
import com.ideiassertiva.FypMatch.data.repository.NavigationState
import com.ideiassertiva.FypMatch.data.repository.PhoneVerificationState
import com.ideiassertiva.FypMatch.data.repository.EmailVerificationState
import com.ideiassertiva.FypMatch.data.repository.GoogleSignInInteractiveRequiredException
import com.ideiassertiva.FypMatch.model.User
import com.ideiassertiva.FypMatch.model.isProfileComplete
import com.ideiassertiva.FypMatch.util.AnalyticsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    val currentUser: StateFlow<User?> = authRepository.currentUser as StateFlow<User?>
    val isLoading: StateFlow<Boolean> = authRepository.isLoading as StateFlow<Boolean>
    val navigationState: StateFlow<NavigationState> = authRepository.navigationState as StateFlow<NavigationState>
    val needsInteractiveSignIn: StateFlow<Boolean> = authRepository.needsInteractiveSignIn as StateFlow<Boolean>
    val phoneVerificationState: StateFlow<PhoneVerificationState> = authRepository.phoneVerificationState as StateFlow<PhoneVerificationState>
    val emailVerificationState: StateFlow<EmailVerificationState> = authRepository.emailVerificationState as StateFlow<EmailVerificationState>
    
    // === GOOGLE SIGN-IN ===
    
    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                errorMessage = null,
                cancelledMessage = null,
                needsInteractiveSignIn = false
            )
            
            // Analytics: Tentativa de login
            analyticsManager.logCustomCrash("google_signin_attempt", mapOf(
                "method" to "credential_manager"
            ))
            
            println("🔍 DEBUG - Iniciando login com Google via CredentialManager...")
            
            val result = authRepository.signInWithGoogle()
            
            result.fold(
                onSuccess = { user ->
                    println("🔍 DEBUG - Login bem-sucedido: ${user.email}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignedIn = true,
                        userProfileComplete = user.isProfileComplete()
                    )
                    
                    // Analytics: Login bem-sucedido
                    analyticsManager.logUserProfile(
                        user.profile.age.takeIf { it > 0 },
                        user.profile.gender.name.takeIf { it != "NOT_SPECIFIED" }
                    )
                    
                    // Log de sucesso
                    analyticsManager.logCustomCrash("google_signin_success", mapOf(
                        "user_id" to user.id,
                        "profile_complete" to user.isProfileComplete().toString(),
                        "has_photo" to user.photoUrl.isNotEmpty().toString(),
                        "access_level" to user.accessLevel.name
                    ))
                },
                onFailure = { error ->
                    val errorMessage = error.message ?: "Erro desconhecido no login"
                    
                    println("🔍 DEBUG - Erro no login: $errorMessage")
                    
                    // Verificar se é necessário fluxo interativo
                    if (error is GoogleSignInInteractiveRequiredException) {
                        println("🔍 DEBUG - Fluxo interativo necessário - sinalizando para UI")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            needsInteractiveSignIn = true,
                            errorMessage = null
                        )
                    } else if (errorMessage.contains("cancelado", ignoreCase = true)) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            cancelledMessage = "Login cancelado pelo usuário"
                        )
                    } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                            errorMessage = errorMessage
                    )
                    }
                    
                    // Analytics: Erro no login
                    analyticsManager.logError(error, "google_signin_failure")
                }
            )
        }
    }
    
    // Método para obter o Intent de login interativo
    fun getGoogleSignInIntent(): Intent {
        return authRepository.getGoogleSignInIntent()
    }
    
    // Método para processar resultado do Intent
    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                needsInteractiveSignIn = false
            )
            
            val result = authRepository.handleGoogleSignInResult(data)
            
            result.fold(
                onSuccess = { user ->
                    println("🔍 DEBUG - Login interativo bem-sucedido: ${user.email}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignedIn = true,
                        userProfileComplete = user.isProfileComplete()
                    )
                    
                    // Analytics
                    analyticsManager.logCustomCrash("google_signin_interactive_success", mapOf(
                        "user_id" to user.id
                    ))
                },
                onFailure = { error ->
                    val errorMessage = error.message ?: "Erro no login interativo"
                    
                    println("🔍 DEBUG - Erro no login interativo: $errorMessage")
                    
                    if (errorMessage.contains("cancelado", ignoreCase = true)) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            cancelledMessage = "Login cancelado pelo usuário"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = errorMessage
                        )
                    }
                    
                    analyticsManager.logError(error, "google_signin_interactive_failure")
                }
            )
        }
    }
    
    // === EMAIL SIGN-IN ===
    
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                loginMode = LoginMode.EMAIL
            )
            
            // Analytics: Tentativa de login
            analyticsManager.logCustomCrash("email_signin_attempt", mapOf(
                "email_domain" to email.substringAfter("@")
            ))
            
            val result = authRepository.signInWithEmail(email, password)
            
            result.fold(
                onSuccess = { user ->
                    println("🔍 DEBUG - Login com email bem-sucedido: ${user.email}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignedIn = true,
                        userProfileComplete = user.isProfileComplete()
                    )
                    
                    // Analytics: Login bem-sucedido
                    analyticsManager.logCustomCrash("email_signin_success", mapOf(
                        "user_id" to user.id,
                        "profile_complete" to user.isProfileComplete().toString()
                    ))
                },
                onFailure = { error ->
                    println("🔍 DEBUG - Erro no login com email: ${error.message}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro no login com email"
                    )
                    
                    // Analytics: Erro no login
                    analyticsManager.logError(error, "email_signin_failure")
                }
            )
        }
    }
    
    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                loginMode = LoginMode.EMAIL,
                email = email
            )
            
            // Analytics: Tentativa de cadastro
            analyticsManager.logCustomCrash("email_signup_attempt", mapOf(
                "email_domain" to email.substringAfter("@")
            ))
            
            val result = authRepository.signUpWithEmail(email, password)
            
            result.fold(
                onSuccess = { message ->
                    println("🔍 DEBUG - Cadastro iniciado: $message")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        emailVerificationSent = true,
                        successMessage = message
                    )
                    
                    // Analytics: Verificação enviada
                    analyticsManager.logCustomCrash("email_verification_sent", mapOf(
                        "email" to email
                    ))
                },
                onFailure = { error ->
                    println("🔍 DEBUG - Erro no cadastro com email: ${error.message}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro no cadastro com email"
                    )
                    
                    // Analytics: Erro no cadastro
                    analyticsManager.logError(error, "email_signup_failure")
                }
            )
        }
    }
    
    // Verificar se o email foi confirmado
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
                        isSignedIn = true,
                        userProfileComplete = user.isProfileComplete(),
                        emailVerificationSent = false,
                        successMessage = "Email verificado com sucesso!"
                    )
                    
                    // Analytics: Verificação bem-sucedida
                    analyticsManager.logCustomCrash("email_verification_success", mapOf(
                        "user_id" to user.id
                    ))
                },
                onFailure = { error ->
                    println("🔍 DEBUG - Erro na verificação do email: ${error.message}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Email ainda não foi verificado"
                    )
                    
                    // Analytics: Erro na verificação
                    analyticsManager.logError(error, "email_verification_check_failure")
                }
            )
        }
    }
    
    // Reenviar email de verificação
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
                        successMessage = message
                    )
                },
                onFailure = { error ->
                    println("🔍 DEBUG - Erro ao reenviar email: ${error.message}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao reenviar email"
                    )
                }
            )
        }
    }
    
    // === PHONE SIGN-IN ===
    
    fun startPhoneVerification(phoneNumber: String, activity: Activity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                loginMode = LoginMode.PHONE,
                phoneNumber = phoneNumber
            )
            
            // Analytics: Tentativa de verificação
            analyticsManager.logCustomCrash("phone_verification_attempt", mapOf(
                "phone_country_code" to phoneNumber.take(3)
            ))
            
            val result = authRepository.startPhoneVerification(phoneNumber, activity)
            
            result.fold(
                onSuccess = { message ->
                    println("🔍 DEBUG - Verificação de telefone iniciada: $message")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        phoneVerificationStarted = true
                    )
                },
                onFailure = { error ->
                    println("🔍 DEBUG - Erro na verificação de telefone: ${error.message}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro na verificação do telefone"
                    )
                    
                    // Analytics: Erro na verificação
                    analyticsManager.logError(error, "phone_verification_failure")
                }
            )
        }
    }
    
    fun verifyPhoneCode(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            val result = authRepository.verifyPhoneCode(code)
            
            result.fold(
                onSuccess = { user ->
                    println("🔍 DEBUG - Login com telefone bem-sucedido: ${user.id}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignedIn = true,
                        userProfileComplete = user.isProfileComplete()
                    )
                    
                    // Analytics: Login bem-sucedido
                    analyticsManager.logCustomCrash("phone_signin_success", mapOf(
                        "user_id" to user.id,
                        "profile_complete" to user.isProfileComplete().toString()
                    ))
                },
                onFailure = { error ->
                    println("🔍 DEBUG - Erro na verificação do código: ${error.message}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Código inválido"
                    )
                    
                    // Analytics: Erro na verificação
                    analyticsManager.logError(error, "phone_code_verification_failure")
                }
            )
        }
    }
    
    fun resendPhoneCode(activity: Activity) {
        val phoneNumber = _uiState.value.phoneNumber
        if (phoneNumber.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Número de telefone não encontrado"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            val result = authRepository.resendPhoneCode(phoneNumber, activity)
            
            result.fold(
                onSuccess = { message ->
                    println("🔍 DEBUG - Código reenviado: $message")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    println("🔍 DEBUG - Erro ao reenviar código: ${error.message}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro ao reenviar código"
                    )
                }
            )
        }
    }
    
    // === MÉTODOS DE CONTROLE ===
    
    fun setLoginMode(mode: LoginMode) {
        _uiState.value = _uiState.value.copy(
            loginMode = mode,
            errorMessage = null,
            cancelledMessage = null,
            successMessage = null
        )
        
        // Limpar estados específicos ao mudar de modo
        when (mode) {
            LoginMode.PHONE -> {
                authRepository.clearEmailVerificationState()
                _uiState.value = _uiState.value.copy(
                    emailVerificationSent = false,
                    email = ""
                )
            }
            LoginMode.EMAIL -> {
                authRepository.clearPhoneVerificationState()
                _uiState.value = _uiState.value.copy(
                    phoneVerificationStarted = false,
                    phoneNumber = ""
                )
            }
            LoginMode.GOOGLE -> {
                authRepository.clearPhoneVerificationState()
                authRepository.clearEmailVerificationState()
                _uiState.value = _uiState.value.copy(
                    phoneVerificationStarted = false,
                    phoneNumber = "",
                    emailVerificationSent = false,
                    email = ""
                )
            }
        }
    }
    
    fun handleSignInError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message
        )
        
        // Analytics: Erro de UI
        analyticsManager.logError(Exception(message), "google_signin_ui_error")
    }
    
    fun handleSignInCancellation(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = null, // Não mostrar como erro
            cancelledMessage = message
        )
        
        // Analytics: Login cancelado (info, não erro)
        analyticsManager.logCustomCrash("google_signin_cancelled", mapOf(
            "reason" to "user_cancelled",
            "message" to message
        ))
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            cancelledMessage = null,
            successMessage = null,
            needsInteractiveSignIn = false
        )
    }
    
    fun clearNavigationState() {
        authRepository.clearNavigationState()
    }
    
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = LoginUiState()
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            cancelledMessage = null,
            successMessage = null,
            needsInteractiveSignIn = false
        )
    }
}

// Estados para diferentes modos de login
enum class LoginMode {
    GOOGLE,
    EMAIL,
    PHONE
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val userProfileComplete: Boolean = false,
    val errorMessage: String? = null,
    val cancelledMessage: String? = null,
    val needsInteractiveSignIn: Boolean = false,
    val loginMode: LoginMode = LoginMode.GOOGLE,
    val phoneNumber: String = "",
    val phoneVerificationStarted: Boolean = false,
    val emailVerificationSent: Boolean = false,
    val email: String = "",
    val successMessage: String? = null
) 
