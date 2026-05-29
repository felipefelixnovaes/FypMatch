package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

/**
 * ViewModel responsável pelo cadastro de novos usuários via email e senha.
 * Utiliza FirebaseAuth diretamente, pois AuthRepository só suporta Google Sign-In.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {

    // ─── Estado da UI ────────────────────────────────────────────────────────

    sealed class RegisterUiState {
        /** Estado inicial — nenhuma operação em andamento */
        object Idle : RegisterUiState()

        /** Cadastro em progresso — mostrar indicador de carregamento */
        object Loading : RegisterUiState()

        /** Cadastro concluído com sucesso */
        data class Success(val userId: String) : RegisterUiState()

        /** Erro durante o cadastro */
        data class Error(val message: String) : RegisterUiState()
    }

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // ─── Ações públicas ──────────────────────────────────────────────────────

    /**
     * Cria conta com email/senha e salva dados adicionais (nome, idade, gênero) no Firestore.
     */
    fun register(
        email: String,
        password: String,
        displayName: String,
        age: Int,
        gender: String
    ) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            try {
                // 1. Criar usuário no Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
                val firebaseUser = authResult.user
                    ?: throw Exception("Falha ao criar usuário")

                // 2. Atualizar displayName no perfil do Auth
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                firebaseUser.updateProfile(profileUpdate).await()

                // 3. Salvar dados adicionais no Firestore
                val userData = mapOf(
                    "id" to firebaseUser.uid,
                    "email" to email.trim(),
                    "displayName" to displayName.trim(),
                    "age" to age,
                    "gender" to gender,
                    "photoUrl" to "",
                    "createdAt" to Date(),
                    "lastActive" to Date()
                )
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(userData)
                    .await()

                _uiState.value = RegisterUiState.Success(firebaseUser.uid)
            } catch (e: Exception) {
                val mensagem = when {
                    e.message?.contains("email address is already in use") == true ->
                        "Este email já está cadastrado."
                    e.message?.contains("badly formatted") == true ->
                        "Formato de email inválido."
                    e.message?.contains("password is too weak") == true ->
                        "Senha muito fraca. Use pelo menos 6 caracteres."
                    else -> e.message ?: "Erro ao criar conta. Tente novamente."
                }
                _uiState.value = RegisterUiState.Error(mensagem)
            }
        }
    }

    /** Volta ao estado inicial para permitir nova tentativa */
    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}
