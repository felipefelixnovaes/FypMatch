package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ideiassertiva.FypMatch.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun deleteAccount(onComplete: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeletingAccount = true)
            try {
                val userId = authRepository.getCurrentUserId() ?: run {
                    onError("Usuário não autenticado")
                    _uiState.value = _uiState.value.copy(isDeletingAccount = false)
                    return@launch
                }
                // Deletar dados do Firestore
                firestore.collection("users").document(userId).delete().await()
                firestore.collection("likes").document(userId).delete().await()
                // Deletar conta Firebase Auth
                auth.currentUser?.delete()?.await()
                _uiState.value = _uiState.value.copy(isDeletingAccount = false)
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isDeletingAccount = false)
                onError(e.message ?: "Erro ao deletar conta")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}

data class SettingsUiState(
    val isDeletingAccount: Boolean = false,
    val error: String? = null
)
