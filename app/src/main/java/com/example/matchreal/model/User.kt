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
    val aboutMe: String = "", // Descrição livre sobre a pessoa
    val photos: List<String> = emptyList(),
    val location: Location = Location(),
    val gender: Gender = Gender.NOT_SPECIFIED,
    val orientation: Orientation = Orientation.NOT_SPECIFIED,
    val intention: Intention = Intention.NOT_SPECIFIED,
    val interests: List<String> = emptyList(),
    val education: String = "",
    val profession: String = "",
    val height: Int = 0, // em cm
    
    // Novos campos pessoais
    val relationshipStatus: RelationshipStatus = RelationshipStatus.NOT_SPECIFIED,
    val hasChildren: ChildrenStatus = ChildrenStatus.NOT_SPECIFIED,
    val wantsChildren: ChildrenStatus = ChildrenStatus.NOT_SPECIFIED,
    val smokingStatus: SmokingStatus = SmokingStatus.NOT_SPECIFIED,
    val drinkingStatus: DrinkingStatus = DrinkingStatus.NOT_SPECIFIED,
    val zodiacSign: ZodiacSign = ZodiacSign.NOT_SPECIFIED,
    val religion: Religion = Religion.NOT_SPECIFIED,
    
    // Interesses específicos
    val favoriteMovies: List<String> = emptyList(),
    val favoriteGenres: List<String> = emptyList(),
    val favoriteBooks: List<String> = emptyList(),
    val favoriteMusic: List<String> = emptyList(),
    val hobbies: List<String> = emptyList(),
    val sports: List<String> = emptyList(),
    val favoriteTeam: String = "", // Time de futebol ou esporte
    val languages: List<String> = emptyList(),
    val traveledCountries: List<String> = emptyList(),
    val petPreference: PetPreference = PetPreference.NOT_SPECIFIED,
    
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

enum class RelationshipStatus {
    SINGLE,          // Solteiro(a)
    DIVORCED,        // Divorciado(a)
    WIDOWED,         // Viúvo(a)
    SEPARATED,       // Separado(a)
    ITS_COMPLICATED, // É complicado
    NOT_SPECIFIED
}

enum class ChildrenStatus {
    YES,             // Sim
    NO,              // Não
    PREFER_NOT_TO_SAY, // Prefiro não dizer
    NOT_SPECIFIED
}

enum class SmokingStatus {
    NEVER,           // Nunca fumei
    SOCIALLY,        // Socialmente
    REGULARLY,       // Regularmente
    TRYING_TO_QUIT,  // Tentando parar
    PREFER_NOT_TO_SAY,
    NOT_SPECIFIED
}

enum class DrinkingStatus {
    NEVER,           // Nunca bebo
    SOCIALLY,        // Socialmente
    REGULARLY,       // Regularmente
    PREFER_NOT_TO_SAY,
    NOT_SPECIFIED
}

enum class ZodiacSign {
    ARIES,           // Áries
    TAURUS,          // Touro
    GEMINI,          // Gêmeos
    CANCER,          // Câncer
    LEO,             // Leão
    VIRGO,           // Virgem
    LIBRA,           // Libra
    SCORPIO,         // Escorpião
    SAGITTARIUS,     // Sagitário
    CAPRICORN,       // Capricórnio
    AQUARIUS,        // Aquário
    PISCES,          // Peixes
    NOT_SPECIFIED
}

enum class Religion {
    CATHOLIC,        // Católica
    PROTESTANT,      // Protestante
    EVANGELICAL,     // Evangélica
    SPIRITIST,       // Espírita
    UMBANDA,         // Umbanda
    CANDOMBLE,       // Candomblé
    BUDDHIST,        // Budista
    JEWISH,          // Judaica
    ISLAMIC,         // Islâmica
    HINDU,           // Hindu
    ATHEIST,         // Ateu
    AGNOSTIC,        // Agnóstico
    SPIRITUAL,       // Espiritual
    OTHER,           // Outra
    PREFER_NOT_TO_SAY,
    NOT_SPECIFIED
}

enum class PetPreference {
    LOVE_PETS,       // Amo animais
    LIKE_PETS,       // Gosto de animais
    ALLERGIC,        // Alérgico
    DONT_LIKE,       // Não gosto
    PREFER_NOT_TO_SAY,
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

fun RelationshipStatus.getDisplayName(): String {
    return when (this) {
        RelationshipStatus.SINGLE -> "Solteiro(a)"
        RelationshipStatus.DIVORCED -> "Divorciado(a)"
        RelationshipStatus.WIDOWED -> "Viúvo(a)"
        RelationshipStatus.SEPARATED -> "Separado(a)"
        RelationshipStatus.ITS_COMPLICATED -> "É complicado"
        RelationshipStatus.NOT_SPECIFIED -> "Não informado"
    }
}

fun ChildrenStatus.getDisplayName(): String {
    return when (this) {
        ChildrenStatus.YES -> "Sim"
        ChildrenStatus.NO -> "Não"
        ChildrenStatus.PREFER_NOT_TO_SAY -> "Prefiro não dizer"
        ChildrenStatus.NOT_SPECIFIED -> "Não informado"
    }
}

fun SmokingStatus.getDisplayName(): String {
    return when (this) {
        SmokingStatus.NEVER -> "Nunca fumei"
        SmokingStatus.SOCIALLY -> "Socialmente"
        SmokingStatus.REGULARLY -> "Regularmente"
        SmokingStatus.TRYING_TO_QUIT -> "Tentando parar"
        SmokingStatus.PREFER_NOT_TO_SAY -> "Prefiro não dizer"
        SmokingStatus.NOT_SPECIFIED -> "Não informado"
    }
}

fun DrinkingStatus.getDisplayName(): String {
    return when (this) {
        DrinkingStatus.NEVER -> "Nunca bebo"
        DrinkingStatus.SOCIALLY -> "Socialmente"
        DrinkingStatus.REGULARLY -> "Regularmente"
        DrinkingStatus.PREFER_NOT_TO_SAY -> "Prefiro não dizer"
        DrinkingStatus.NOT_SPECIFIED -> "Não informado"
    }
}

fun ZodiacSign.getDisplayName(): String {
    return when (this) {
        ZodiacSign.ARIES -> "Áries"
        ZodiacSign.TAURUS -> "Touro"
        ZodiacSign.GEMINI -> "Gêmeos"
        ZodiacSign.CANCER -> "Câncer"
        ZodiacSign.LEO -> "Leão"
        ZodiacSign.VIRGO -> "Virgem"
        ZodiacSign.LIBRA -> "Libra"
        ZodiacSign.SCORPIO -> "Escorpião"
        ZodiacSign.SAGITTARIUS -> "Sagitário"
        ZodiacSign.CAPRICORN -> "Capricórnio"
        ZodiacSign.AQUARIUS -> "Aquário"
        ZodiacSign.PISCES -> "Peixes"
        ZodiacSign.NOT_SPECIFIED -> "Não informado"
    }
}

fun Religion.getDisplayName(): String {
    return when (this) {
        Religion.CATHOLIC -> "Católica"
        Religion.PROTESTANT -> "Protestante"
        Religion.EVANGELICAL -> "Evangélica"
        Religion.SPIRITIST -> "Espírita"
        Religion.UMBANDA -> "Umbanda"
        Religion.CANDOMBLE -> "Candomblé"
        Religion.BUDDHIST -> "Budista"
        Religion.JEWISH -> "Judaica"
        Religion.ISLAMIC -> "Islâmica"
        Religion.HINDU -> "Hindu"
        Religion.ATHEIST -> "Ateu"
        Religion.AGNOSTIC -> "Agnóstico"
        Religion.SPIRITUAL -> "Espiritual"
        Religion.OTHER -> "Outra"
        Religion.PREFER_NOT_TO_SAY -> "Prefiro não dizer"
        Religion.NOT_SPECIFIED -> "Não informado"
    }
}

fun PetPreference.getDisplayName(): String {
    return when (this) {
        PetPreference.LOVE_PETS -> "Amo animais"
        PetPreference.LIKE_PETS -> "Gosto de animais"
        PetPreference.ALLERGIC -> "Alérgico"
        PetPreference.DONT_LIKE -> "Não gosto"
        PetPreference.PREFER_NOT_TO_SAY -> "Prefiro não dizer"
        PetPreference.NOT_SPECIFIED -> "Não informado"
    }
} 