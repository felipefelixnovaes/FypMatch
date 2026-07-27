package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.ideiassertiva.FypMatch.model.Gender
import com.ideiassertiva.FypMatch.model.User
import com.ideiassertiva.FypMatch.model.UserProfile
import com.ideiassertiva.FypMatch.model.withCompletionStatus
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

                // 3. Salvar dados adicionais no Firestore — inclui o "profile" aninhado
                // (fullName/age/gender) para que ProfileEditViewModel.loadCurrentUser()
                // encontre os dados do cadastro em vez de valores padrão em branco.
                val now = Date()
                val newUser = User(
                    id = firebaseUser.uid,
                    email = email.trim(),
                    displayName = displayName.trim(),
                    photoUrl = "",
                    profile = UserProfile(
                        fullName = displayName.trim(),
                        age = age,
                        gender = mapGenderLabelToEnum(gender)
                    ).withCompletionStatus(),
                    createdAt = now,
                    lastActive = now
                )
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(newUser)
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

    /** Converte o rótulo em português escolhido na tela de cadastro para o enum do domínio. */
    private fun mapGenderLabelToEnum(label: String): Gender = when (label) {
        "Homem" -> Gender.MALE
        "Mulher" -> Gender.FEMALE
        "Não-binário" -> Gender.NON_BINARY
        else -> Gender.NOT_SPECIFIED
    }

    /** Volta ao estado inicial para permitir nova tentativa */
    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}
