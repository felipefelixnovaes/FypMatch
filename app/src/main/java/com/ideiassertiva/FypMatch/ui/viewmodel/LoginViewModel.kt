package com.ideiassertiva.FypMatch.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AuthRepository
import com.ideiassertiva.FypMatch.data.repository.NavigationState
import com.ideiassertiva.FypMatch.model.User
import com.ideiassertiva.FypMatch.model.isProfileComplete
import com.ideiassertiva.FypMatch.util.AnalyticsManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
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
    
    fun getGoogleSignInIntent(): Intent {
        return authRepository.getGoogleSignInClient().signInIntent
    }
    
    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Analytics: Tentativa de login
            analyticsManager.logCustomCrash("google_signin_attempt", mapOf(
                "user_email" to (account.email ?: "unknown"),
                "has_photo" to (account.photoUrl != null).toString(),
                "family_name" to (account.familyName ?: "unknown"),
                "given_name" to (account.givenName ?: "unknown")
            ))
            
            val result = authRepository.signInWithGoogle(account)
            
            result.fold(
                onSuccess = { user ->
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
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro desconhecido no login"
                    )
                    
                    // Analytics: Erro no login
                    analyticsManager.logError(error, "google_signin_failure")
                }
            )
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
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
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
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val userProfileComplete: Boolean = false,
    val errorMessage: String? = null
) 
