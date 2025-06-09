package com.example.matchreal.model

import java.util.Date

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val profile: UserProfile = UserProfile(),
    val preferences: UserPreferences = UserPreferences(),
    val subscription: SubscriptionStatus = SubscriptionStatus.FREE,
    val accessLevel: AccessLevel = AccessLevel.WAITLIST,
    val betaFlags: BetaFlags = BetaFlags(),
    val createdAt: Date = Date(),
    val lastActive: Date = Date(),
    val isActive: Boolean = true,
    val waitlistData: WaitlistUser? = null,
    val aiCredits: AiCredits = AiCredits()
)

data class UserProfile(
    val fullName: String = "",
    val age: Int = 18,
    val bio: String = "",
    val photos: List<String> = emptyList(),
    val location: Location = Location(),
    val gender: Gender = Gender.NOT_SPECIFIED,
    val orientation: Orientation = Orientation.NOT_SPECIFIED,
    val intention: Intention = Intention.NOT_SPECIFIED,
    val interests: List<String> = emptyList(),
    val education: String = "",
    val profession: String = "",
    val height: Int = 0, // em cm
    val isProfileComplete: Boolean = false
)

data class Location(
    val city: String = "",
    val state: String = "",
    val country: String = "Brasil",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

enum class Gender {
    MALE,
    FEMALE,
    NON_BINARY,
    OTHER,
    NOT_SPECIFIED
}

enum class Orientation {
    STRAIGHT,
    GAY,
    LESBIAN,
    BISEXUAL,
    PANSEXUAL,
    ASEXUAL,
    DEMISEXUAL,
    OTHER,
    NOT_SPECIFIED
}

enum class Intention {
    FRIENDSHIP,      // Amizade
    DATING,          // Namoro sério
    CASUAL,          // Relacionamento casual
    NETWORKING,      // Networking profissional
    NOT_SPECIFIED
}

data class UserPreferences(
    val ageRange: IntRange = 18..99,
    val maxDistance: Int = 50, // em km
    val genderPreference: List<Gender> = emptyList(),
    val intentionPreference: List<Intention> = emptyList(),
    val showMe: Boolean = true,
    val onlyVerified: Boolean = false
)

enum class SubscriptionStatus {
    FREE,
    PREMIUM,
    VIP
}

enum class AccessLevel {
    WAITLIST,        // Na lista de espera
    BETA_ACCESS,     // Acesso beta individual
    FULL_ACCESS,     // Acesso completo liberado
    ADMIN           // Acesso administrativo
}

data class BetaFlags(
    val hasEarlyAccess: Boolean = false,
    val canAccessSwipe: Boolean = false,
    val canAccessChat: Boolean = false,
    val canAccessPremium: Boolean = false,
    val canAccessAI: Boolean = false,
    val isTestUser: Boolean = false
)

data class AiCredits(
    val current: Int = 0,
    val dailyLimit: Int = 0,
    val usedToday: Int = 0,
    val lastResetDate: Date = Date(),
    val totalEarned: Int = 0,
    val totalSpent: Int = 0
)

// Créditos por tipo de assinatura
object AiCreditLimits {
    const val FREE_DAILY = 0           // Usuários gratuitos não têm créditos diários
    const val PREMIUM_DAILY = 10       // Premium: 10 créditos por dia
    const val VIP_DAILY = 25           // VIP: 25 créditos por dia
    
    const val AD_REWARD = 3            // Créditos ganhos por anúncio assistido
    const val MAX_AD_CREDITS_DAILY = 9 // Máximo de créditos por anúncios por dia (3 anúncios)
    
    const val COST_PER_MESSAGE = 1     // Custo de 1 crédito por mensagem enviada
}

// Extensões úteis
fun User.isProfileComplete(): Boolean {
    return profile.fullName.isNotBlank() &&
            profile.age >= 18 &&
            profile.bio.isNotBlank() &&
            profile.photos.isNotEmpty() &&
            profile.location.city.isNotBlank() &&
            profile.gender != Gender.NOT_SPECIFIED &&
            profile.orientation != Orientation.NOT_SPECIFIED &&
            profile.intention != Intention.NOT_SPECIFIED
}

fun Gender.getDisplayName(): String {
    return when (this) {
        Gender.MALE -> "Masculino"
        Gender.FEMALE -> "Feminino"
        Gender.NON_BINARY -> "Não-binário"
        Gender.OTHER -> "Outro"
        Gender.NOT_SPECIFIED -> "Não especificado"
    }
}

fun Orientation.getDisplayName(): String {
    return when (this) {
        Orientation.STRAIGHT -> "Heterossexual"
        Orientation.GAY -> "Gay"
        Orientation.LESBIAN -> "Lésbica"
        Orientation.BISEXUAL -> "Bissexual"
        Orientation.PANSEXUAL -> "Pansexual"
        Orientation.ASEXUAL -> "Assexual"
        Orientation.DEMISEXUAL -> "Demissexual"
        Orientation.OTHER -> "Outro"
        Orientation.NOT_SPECIFIED -> "Não especificado"
    }
}

fun Intention.getDisplayName(): String {
    return when (this) {
        Intention.FRIENDSHIP -> "Amizade"
        Intention.DATING -> "Relacionamento sério"
        Intention.CASUAL -> "Algo casual"
        Intention.NETWORKING -> "Networking"
        Intention.NOT_SPECIFIED -> "Não especificado"
    }
} 