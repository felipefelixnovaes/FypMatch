package com.example.matchreal.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchreal.data.repository.AuthRepository
import com.example.matchreal.model.User
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(context: Context) : ViewModel() {
    
    private val authRepository = AuthRepository(context)
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    val currentUser: StateFlow<User?> = authRepository.currentUser as StateFlow<User?>
    val isLoading: StateFlow<Boolean> = authRepository.isLoading as StateFlow<Boolean>
    
    fun getGoogleSignInIntent(): Intent {
        return authRepository.getGoogleSignInClient().signInIntent
    }
    
    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = authRepository.signInWithGoogle(account)
            
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignedIn = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Erro desconhecido no login"
                    )
                }
            )
        }
    }
    
    fun handleSignInError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
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
    val errorMessage: String? = null
) 