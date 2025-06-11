package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AccessCodeRepository
import com.ideiassertiva.FypMatch.data.repository.UserRepository
import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccessCodeViewModel @Inject constructor(
    private val accessCodeRepository: AccessCodeRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    // Estado do código sendo inserido
    private val _codeInput = MutableStateFlow("")
    val codeInput: StateFlow<String> = _codeInput.asStateFlow()
    
    // Estado do processo de resgate
    private val _redeemState = MutableStateFlow<RedeemState>(RedeemState.Idle)
    val redeemState: StateFlow<RedeemState> = _redeemState.asStateFlow()
    
    // Estado do usuário atual
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    // Resultado do último resgate
    private val _lastResult = MutableStateFlow<AccessCodeResult?>(null)
    val lastResult: StateFlow<AccessCodeResult?> = _lastResult.asStateFlow()
    
    // Verificar se usuário pode usar códigos
    private val _canUseCode = MutableStateFlow(true)
    val canUseCode: StateFlow<Boolean> = _canUseCode.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            val currentUserId = userRepository.getCurrentUserId()
            if (currentUserId != null) {
                userRepository.observeUserInFirestore(currentUserId).collect { user ->
                    _currentUser.value = user
                    user?.let {
                        _canUseCode.value = accessCodeRepository.canUserUseCode(it.email)
                    }
                }
            }
        }
    }
    
    fun updateCodeInput(code: String) {
        _codeInput.value = code.uppercase()
    }
    
    fun redeemCode() {
        val user = _currentUser.value
        val code = _codeInput.value.trim()
        
        if (user == null) {
            _redeemState.value = RedeemState.Error("Usuário não encontrado")
            return
        }
        
        if (code.isEmpty()) {
            _redeemState.value = RedeemState.Error("Digite um código válido")
            return
        }
        
        if (!_canUseCode.value) {
            _redeemState.value = RedeemState.Error("Você já utilizou um código de acesso")
            return
        }
        
        viewModelScope.launch {
            _redeemState.value = RedeemState.Loading
            
            try {
                val result = accessCodeRepository.redeemAccessCode(code, user.email)
                _lastResult.value = result
                
                if (result.success && result.grantedAccess != null) {
                    // Atualizar o usuário com o novo nível de acesso
                    updateUserAccess(user, result.grantedAccess, result.expiresAt)
                    _redeemState.value = RedeemState.Success(result)
                    _codeInput.value = ""
                    _canUseCode.value = false
                } else {
                    _redeemState.value = RedeemState.Error(result.message)
                }
            } catch (e: Exception) {
                _redeemState.value = RedeemState.Error("Erro ao processar código: ${e.message}")
            }
        }
    }
    
    private suspend fun updateUserAccess(user: User, accessType: AccessCodeType, expiresAt: Date?) {
        val updatedUser = when (accessType) {
            AccessCodeType.BASIC -> user.copy(
                accessLevel = AccessLevel.FULL_ACCESS
            )
            AccessCodeType.PREMIUM -> user.copy(
                subscription = SubscriptionStatus.PREMIUM,
                accessLevel = AccessLevel.FULL_ACCESS
            )
            AccessCodeType.VIP -> user.copy(
                subscription = SubscriptionStatus.VIP,
                accessLevel = AccessLevel.FULL_ACCESS
            )
        }
        
        userRepository.updateUserInFirestore(updatedUser)
    }
    
    fun clearResult() {
        _redeemState.value = RedeemState.Idle
        _lastResult.value = null
    }
    
    fun validateCodeFormat(code: String): Boolean {
        return code.matches(Regex("MATCH-[A-Z]+-\\d{4}-[A-Z0-9]{4}"))
    }
    
    fun getCodePreview(code: String): String {
        return if (code.length > 4) {
            "${code.take(4)}...${code.takeLast(4)}"
        } else {
            code
        }
    }
}

sealed class RedeemState {
    object Idle : RedeemState()
    object Loading : RedeemState()
    data class Success(val result: AccessCodeResult) : RedeemState()
    data class Error(val message: String) : RedeemState()
}

// Dados para exibir informações sobre tipos de código
data class CodeTypeInfo(
    val type: AccessCodeType,
    val title: String,
    val description: String,
    val benefits: List<String>,
    val duration: String?
)

fun getCodeTypeInfoList(): List<CodeTypeInfo> {
    return listOf(
        CodeTypeInfo(
            type = AccessCodeType.BASIC,
            title = "Acesso Básico",
            description = "Acesso completo às funcionalidades do app",
            benefits = listOf(
                "Acesso ao conselheiro de IA",
                "Sistema de créditos via anúncios",  
                "Todas as funcionalidades básicas"
            ),
            duration = null
        ),
        CodeTypeInfo(
            type = AccessCodeType.PREMIUM,
            title = "Premium",
            description = "Experiência melhorada por 30 dias",
            benefits = listOf(
                "10 créditos diários para IA",
                "Sem necessidade de anúncios",
                "Suporte prioritário",
                "Recursos exclusivos"
            ),
            duration = "30 dias"
        ),
        CodeTypeInfo(
            type = AccessCodeType.VIP,
            title = "VIP",
            description = "Acesso máximo por 30 dias",
            benefits = listOf(
                "25 créditos diários para IA",
                "Acesso a recursos beta",
                "Suporte premium",
                "Personalização avançada",
                "Sem limitações"
            ),
            duration = "30 dias"
        )
    )
} 
