package com.ideiassertiva.FypMatch.data.repository

import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class AccessCodeRepository @Inject constructor() {
    
    // Estado dos códigos utilizados
    private val _usedCodes = MutableStateFlow<Map<String, AccessCode>>(emptyMap())
    val usedCodes: Flow<Map<String, AccessCode>> = _usedCodes.asStateFlow()
    
    // Códigos disponíveis (carregados dos pré-gerados)
    private val availableCodes = PreGeneratedCodes.ALL_CODES.associateBy { it.code }.toMutableMap()
    
    // Validar e aplicar código de acesso
    suspend fun redeemAccessCode(code: String, userEmail: String): AccessCodeResult {
        return try {
            val trimmedCode = code.trim().uppercase()
            
            // Verificar se o código existe
            val accessCode = availableCodes[trimmedCode]
                ?: return AccessCodeResult(
                    success = false,
                    message = "Código não encontrado ou inválido"
                )
            
            // Verificar se já foi usado
            if (accessCode.isUsed || _usedCodes.value.containsKey(trimmedCode)) {
                return AccessCodeResult(
                    success = false,
                    message = "Este código já foi utilizado"
                )
            }
            
            // Marcar como usado
            val usedCode = accessCode.copy(
                isUsed = true,
                usedBy = userEmail,
                usedAt = Date()
            )
            
            _usedCodes.value = _usedCodes.value + (trimmedCode to usedCode)
            availableCodes[trimmedCode] = usedCode
            
            // Calcular data de expiração para Premium/VIP (30 dias)
            val expirationDate = if (accessCode.type != AccessCodeType.BASIC) {
                Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 30)
                }.time
            } else null
            
            AccessCodeResult(
                success = true,
                message = "Código aplicado com sucesso! ${accessCode.type.getDisplayName()}",
                grantedAccess = accessCode.type,
                expiresAt = expirationDate
            )
        } catch (e: Exception) {
            AccessCodeResult(
                success = false,
                message = "Erro ao validar código: ${e.message}"
            )
        }
    }
    
    // Obter informações sobre um código específico
    fun getCodeInfo(code: String): AccessCode? {
        val trimmedCode = code.trim().uppercase()
        return availableCodes[trimmedCode]
    }
    
    // Verificar se um usuário pode usar um código
    fun canUserUseCode(userEmail: String): Boolean {
        // Verificar se o usuário já usou algum código
        return !_usedCodes.value.values.any { it.usedBy == userEmail }
    }
    
    // Obter estatísticas de uso dos códigos
    fun getCodeStatistics(): CodeStatistics {
        val allCodes = PreGeneratedCodes.ALL_CODES
        val usedCodesCount = _usedCodes.value.size
        
        val basicUsed = _usedCodes.value.values.count { it.type == AccessCodeType.BASIC }
        val premiumUsed = _usedCodes.value.values.count { it.type == AccessCodeType.PREMIUM }
        val vipUsed = _usedCodes.value.values.count { it.type == AccessCodeType.VIP }
        
        return CodeStatistics(
            totalCodes = allCodes.size,
            usedCodes = usedCodesCount,
            availableCodes = allCodes.size - usedCodesCount,
            basicUsed = basicUsed,
            premiumUsed = premiumUsed,
            vipUsed = vipUsed,
            basicAvailable = PreGeneratedCodes.BASIC_CODES.size - basicUsed,
            premiumAvailable = PreGeneratedCodes.PREMIUM_CODES.size - premiumUsed,
            vipAvailable = PreGeneratedCodes.VIP_CODES.size - vipUsed
        )
    }
    
    // Listar códigos não utilizados (para admin)
    fun getAvailableCodes(): List<AccessCode> {
        return availableCodes.values.filter { !it.isUsed }
    }
    
    // Listar códigos utilizados (para admin)
    fun getUsedCodesList(): List<AccessCode> {
        return _usedCodes.value.values.toList()
    }
}

data class CodeStatistics(
    val totalCodes: Int,
    val usedCodes: Int,
    val availableCodes: Int,
    val basicUsed: Int,
    val premiumUsed: Int,
    val vipUsed: Int,
    val basicAvailable: Int,
    val premiumAvailable: Int,
    val vipAvailable: Int
)

// Extensões
fun AccessCodeType.getDisplayName(): String {
    return when (this) {
        AccessCodeType.BASIC -> "Acesso Básico"
        AccessCodeType.PREMIUM -> "Premium (30 dias)"
        AccessCodeType.VIP -> "VIP (30 dias)"
    }
}

fun AccessCodeType.getSubscriptionStatus(): SubscriptionStatus {
    return when (this) {
        AccessCodeType.BASIC -> SubscriptionStatus.FREE
        AccessCodeType.PREMIUM -> SubscriptionStatus.PREMIUM
        AccessCodeType.VIP -> SubscriptionStatus.VIP
    }
}

fun AccessCodeType.getAccessLevel(): AccessLevel {
    return when (this) {
        AccessCodeType.BASIC -> AccessLevel.FULL_ACCESS
        AccessCodeType.PREMIUM -> AccessLevel.FULL_ACCESS
        AccessCodeType.VIP -> AccessLevel.FULL_ACCESS
    }
}

 
