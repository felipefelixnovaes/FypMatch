package com.example.matchreal.model

import java.util.Date

data class AccessCode(
    val code: String = "",
    val type: AccessCodeType = AccessCodeType.BASIC,
    val isUsed: Boolean = false,
    val usedBy: String = "",
    val createdAt: Date = Date(),
    val usedAt: Date? = null,
    val description: String = ""
)

enum class AccessCodeType {
    BASIC,
    PREMIUM, 
    VIP
}

data class AccessCodeResult(
    val success: Boolean,
    val message: String,
    val grantedAccess: AccessCodeType? = null,
    val expiresAt: Date? = null
)

object PreGeneratedCodes {
    val BASIC_CODES = listOf(
        AccessCode("MATCH-BASIC-2024-A1B2", AccessCodeType.BASIC, description = "Código básico #1"),
        AccessCode("MATCH-BASIC-2024-C3D4", AccessCodeType.BASIC, description = "Código básico #2"),
        AccessCode("MATCH-BASIC-2024-E5F6", AccessCodeType.BASIC, description = "Código básico #3"),
        AccessCode("MATCH-BASIC-2024-G7H8", AccessCodeType.BASIC, description = "Código básico #4"),
        AccessCode("MATCH-BASIC-2024-I9J0", AccessCodeType.BASIC, description = "Código básico #5")
    )
    
    val PREMIUM_CODES = listOf(
        AccessCode("MATCH-PREMIUM-2024-K1L2", AccessCodeType.PREMIUM, description = "Código Premium #1"),
        AccessCode("MATCH-PREMIUM-2024-M3N4", AccessCodeType.PREMIUM, description = "Código Premium #2"),
        AccessCode("MATCH-PREMIUM-2024-O5P6", AccessCodeType.PREMIUM, description = "Código Premium #3"),
        AccessCode("MATCH-PREMIUM-2024-Q7R8", AccessCodeType.PREMIUM, description = "Código Premium #4"),
        AccessCode("MATCH-PREMIUM-2024-S9T0", AccessCodeType.PREMIUM, description = "Código Premium #5")
    )
    
    val VIP_CODES = listOf(
        AccessCode("MATCH-VIP-2024-U1V2", AccessCodeType.VIP, description = "Código VIP #1"),
        AccessCode("MATCH-VIP-2024-W3X4", AccessCodeType.VIP, description = "Código VIP #2"),
        AccessCode("MATCH-VIP-2024-Y5Z6", AccessCodeType.VIP, description = "Código VIP #3"),
        AccessCode("MATCH-VIP-2024-A7B8", AccessCodeType.VIP, description = "Código VIP #4"),
        AccessCode("MATCH-VIP-2024-C9D0", AccessCodeType.VIP, description = "Código VIP #5"),
        AccessCode("MATCH-VIP-2024-E1F2", AccessCodeType.VIP, description = "Código VIP #6"),
        AccessCode("MATCH-VIP-2024-G3H4", AccessCodeType.VIP, description = "Código VIP #7"),
        AccessCode("MATCH-VIP-2024-I5J6", AccessCodeType.VIP, description = "Código VIP #8"),
        AccessCode("MATCH-VIP-2024-K7L8", AccessCodeType.VIP, description = "Código VIP #9"),
        AccessCode("MATCH-VIP-2024-M9N0", AccessCodeType.VIP, description = "Código VIP #10")
    )
    
    val ALL_CODES = BASIC_CODES + PREMIUM_CODES + VIP_CODES
}

// Extensões úteis
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
