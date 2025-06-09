package com.example.matchreal.data.repository

import com.example.matchreal.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccessControlRepository {
    
    // Controles globais de acesso
    private val _globalAccessFlags = MutableStateFlow(GlobalAccessFlags())
    val globalAccessFlags: Flow<GlobalAccessFlags> = _globalAccessFlags.asStateFlow()
    
    // Usuários com acesso beta específico
    private val _betaUsers = MutableStateFlow<Set<String>>(emptySet())
    val betaUsers: Flow<Set<String>> = _betaUsers.asStateFlow()
    
    // Configuração de emails com acesso especial
    companion object {
        private val ADMIN_EMAILS = setOf(
            "felipe.felixnovaes@gmail.com"
        )
        
        private val VIP_EMAILS = setOf(
            "felix3designer@gmail.com"
        )
        
        private val BETA_ACCESS_EMAILS = setOf<String>(
            // Lista de emails beta (vazia por enquanto)
        )
    }
    
    // Determinar nível de acesso baseado no email
    fun getAccessLevelByEmail(email: String): AccessLevel {
        return when {
            ADMIN_EMAILS.contains(email.lowercase()) -> AccessLevel.ADMIN
            VIP_EMAILS.contains(email.lowercase()) -> AccessLevel.FULL_ACCESS
            BETA_ACCESS_EMAILS.contains(email.lowercase()) -> AccessLevel.BETA_ACCESS
            else -> AccessLevel.WAITLIST
        }
    }
    
    // Verificar se um email é administrativo
    fun isAdminEmail(email: String): Boolean {
        return ADMIN_EMAILS.contains(email.lowercase())
    }
    
    // Verificar se um email é VIP
    fun isVipEmail(email: String): Boolean {
        return VIP_EMAILS.contains(email.lowercase())
    }
    
    // Verificar se um email tem acesso beta
    fun isBetaAccessEmail(email: String): Boolean {
        return BETA_ACCESS_EMAILS.contains(email.lowercase())
    }
    
    // Verificar se um usuário tem acesso a uma funcionalidade
    fun hasAccess(user: User?, feature: AppFeature): Boolean {
        val globalFlags = _globalAccessFlags.value
        
        // Se o acesso global está liberado para todos
        if (globalFlags.isFullAccessEnabled) return true
        
        // Se o usuário não existe, sem acesso
        if (user == null) return false
        
        // Verificar se é um email especial (ADMIN, VIP ou BETA_ACCESS)
        val emailAccessLevel = getAccessLevelByEmail(user.email)
        val effectiveAccessLevel = when {
            emailAccessLevel == AccessLevel.ADMIN -> AccessLevel.ADMIN
            emailAccessLevel == AccessLevel.FULL_ACCESS -> AccessLevel.FULL_ACCESS
            emailAccessLevel == AccessLevel.BETA_ACCESS -> AccessLevel.BETA_ACCESS
            else -> user.accessLevel
        }
        
        // ADMIN tem acesso a tudo
        if (effectiveAccessLevel == AccessLevel.ADMIN) return true
        
        // Verificar acesso baseado no nível efetivo do usuário
        return when (feature) {
            AppFeature.WAITLIST -> true // Sempre disponível
            AppFeature.LOGIN -> true // Sempre disponível
            AppFeature.PROFILE_SETUP -> effectiveAccessLevel != AccessLevel.WAITLIST || globalFlags.isBetaAccessEnabled
            AppFeature.DISCOVERY -> {
                globalFlags.isDiscoveryEnabled ||
                effectiveAccessLevel == AccessLevel.FULL_ACCESS ||
                effectiveAccessLevel == AccessLevel.ADMIN ||
                (effectiveAccessLevel == AccessLevel.BETA_ACCESS && user.betaFlags.canAccessSwipe)
            }
            AppFeature.CHAT -> {
                globalFlags.isChatEnabled ||
                effectiveAccessLevel == AccessLevel.FULL_ACCESS ||
                effectiveAccessLevel == AccessLevel.ADMIN ||
                (effectiveAccessLevel == AccessLevel.BETA_ACCESS && user.betaFlags.canAccessChat)
            }
            AppFeature.PREMIUM -> {
                globalFlags.isPremiumEnabled ||
                effectiveAccessLevel == AccessLevel.FULL_ACCESS ||
                effectiveAccessLevel == AccessLevel.ADMIN ||
                (effectiveAccessLevel == AccessLevel.BETA_ACCESS && user.betaFlags.canAccessPremium)
            }
            AppFeature.AI_FEATURES -> {
                globalFlags.isAIEnabled ||
                effectiveAccessLevel == AccessLevel.ADMIN ||
                (effectiveAccessLevel == AccessLevel.BETA_ACCESS && user.betaFlags.canAccessAI)
            }
            AppFeature.AI_COUNSELOR -> {
                // Conselheiro de IA disponível para todos, mas limitado por créditos
                // VIP e Premium têm créditos diários, FREE precisa assistir anúncios
                globalFlags.isAIEnabled ||
                effectiveAccessLevel == AccessLevel.ADMIN ||
                effectiveAccessLevel == AccessLevel.FULL_ACCESS ||
                effectiveAccessLevel == AccessLevel.BETA_ACCESS ||
                user.subscription != SubscriptionStatus.FREE ||
                true // Permitir acesso para todos (controle será por créditos)
            }
        }
    }
    
    // Funções administrativas para controlar acesso
    fun enableFullAccess() {
        _globalAccessFlags.value = _globalAccessFlags.value.copy(isFullAccessEnabled = true)
    }
    
    fun disableFullAccess() {
        _globalAccessFlags.value = _globalAccessFlags.value.copy(isFullAccessEnabled = false)
    }
    
    fun enableBetaAccess() {
        _globalAccessFlags.value = _globalAccessFlags.value.copy(isBetaAccessEnabled = true)
    }
    
    fun enableDiscovery() {
        _globalAccessFlags.value = _globalAccessFlags.value.copy(isDiscoveryEnabled = true)
    }
    
    fun enableChat() {
        _globalAccessFlags.value = _globalAccessFlags.value.copy(isChatEnabled = true)
    }
    
    fun enablePremium() {
        _globalAccessFlags.value = _globalAccessFlags.value.copy(isPremiumEnabled = true)
    }
    
    fun enableAI() {
        _globalAccessFlags.value = _globalAccessFlags.value.copy(isAIEnabled = true)
    }
    
    // Adicionar usuário ao beta
    fun addBetaUser(userId: String) {
        _betaUsers.value = _betaUsers.value + userId
    }
    
    // Remover usuário do beta
    fun removeBetaUser(userId: String) {
        _betaUsers.value = _betaUsers.value - userId
    }
    
    // Upgrade de usuário para beta access
    fun upgradeUserToBeta(userId: String): BetaFlags {
        addBetaUser(userId)
        return BetaFlags(
            hasEarlyAccess = true,
            canAccessSwipe = _globalAccessFlags.value.isDiscoveryEnabled,
            canAccessChat = _globalAccessFlags.value.isChatEnabled,
            canAccessPremium = _globalAccessFlags.value.isPremiumEnabled,
            canAccessAI = _globalAccessFlags.value.isAIEnabled,
            isTestUser = false
        )
    }
    
    // Configurar acesso especial baseado no email durante login/cadastro
    fun getSpecialAccessConfig(email: String): Pair<AccessLevel, BetaFlags> {
        return when {
            isAdminEmail(email) -> {
                Pair(
                    AccessLevel.ADMIN,
                    BetaFlags(
                        hasEarlyAccess = true,
                        canAccessSwipe = true,
                        canAccessChat = true,
                        canAccessPremium = true,
                        canAccessAI = true,
                        isTestUser = false
                    )
                )
            }
            isVipEmail(email) -> {
                Pair(
                    AccessLevel.FULL_ACCESS,
                    BetaFlags(
                        hasEarlyAccess = true,
                        canAccessSwipe = true,
                        canAccessChat = true,
                        canAccessPremium = true,
                        canAccessAI = true,
                        isTestUser = false
                    )
                )
            }
            isBetaAccessEmail(email) -> {
                Pair(
                    AccessLevel.BETA_ACCESS,
                    BetaFlags(
                        hasEarlyAccess = true,
                        canAccessSwipe = true,
                        canAccessChat = true,
                        canAccessPremium = true,
                        canAccessAI = false,
                        isTestUser = true
                    )
                )
            }
            else -> {
                Pair(
                    AccessLevel.WAITLIST,
                    BetaFlags()
                )
            }
        }
    }
}

data class GlobalAccessFlags(
    val isFullAccessEnabled: Boolean = false,          // Libera tudo para todos
    val isBetaAccessEnabled: Boolean = false,          // Permite acesso beta geral
    val isDiscoveryEnabled: Boolean = false,           // Libera discovery/swipe
    val isChatEnabled: Boolean = false,                // Libera chat
    val isPremiumEnabled: Boolean = false,             // Libera funcionalidades premium
    val isAIEnabled: Boolean = false,                  // Libera funcionalidades de IA
    val isMaintenanceMode: Boolean = false             // Modo manutenção
)

enum class AppFeature {
    WAITLIST,
    LOGIN,
    PROFILE_SETUP,
    DISCOVERY,
    CHAT,
    PREMIUM,
    AI_FEATURES,
    AI_COUNSELOR
} 