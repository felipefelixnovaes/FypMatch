package com.example.matchreal.model

import java.util.Date

data class WaitlistUser(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val city: String = "",
    val state: String = "",
    val age: Int = 18,
    val gender: Gender = Gender.NOT_SPECIFIED,
    val orientation: Orientation = Orientation.NOT_SPECIFIED,
    val intention: Intention = Intention.NOT_SPECIFIED,
    val interests: List<String> = emptyList(),
    val inviteCode: String = "",
    val invitedBy: String? = null, // Código de quem convidou
    val joinedAt: Date = Date(),
    val position: Int = 0,
    val invitesSent: List<String> = emptyList(), // Lista de códigos dos convidados
    val invitesAccepted: Int = 0,
    val status: WaitlistStatus = WaitlistStatus.WAITING,
    val rewards: List<WaitlistReward> = emptyList(),
    val accessLevel: AccessLevel = AccessLevel.WAITLIST
)

enum class WaitlistStatus {
    WAITING,           // Na lista de espera
    VIP_ACCESS,        // Acesso VIP (primeira onda)
    EARLY_ACCESS,      // Acesso antecipado (segunda onda)
    MAIN_ACCESS,       // Acesso principal (terceira onda)
    FULL_ACCESS        // Acesso completo (lançamento público)
}

enum class WaitlistReward {
    BETTER_POSITION,    // 1+ convites: Posição melhor na fila
    PREMIUM_7_DAYS,     // 3+ convites: 7 dias Premium grátis
    PASSPORT_MODE,      // 5+ convites: Modo Passaporte
    VIP_ACCESS,         // 10+ convites: Acesso VIP
    EARLY_ADOPTER       // 20+ convites: Badge Early Adopter
}

data class WaitlistStats(
    val totalUsers: Int = 0,
    val userPosition: Int = 0,
    val invitesSent: Int = 0,
    val invitesAccepted: Int = 0,
    val estimatedWaitTime: String = "",
    val nextReward: WaitlistReward? = null,
    val invitesNeededForNextReward: Int = 0
) 