package com.ideiassertiva.FypMatch.model

import java.util.Date

// Phase 5 - Premium Features Models

data class PhotoUploadLimits(
    val maxPhotos: Int,
    val requiresQualityCheck: Boolean,
    val canUploadHD: Boolean
) {
    companion object {
        fun forSubscription(subscription: SubscriptionStatus): PhotoUploadLimits {
            return when (subscription) {
                SubscriptionStatus.FREE -> PhotoUploadLimits(
                    maxPhotos = 6,
                    requiresQualityCheck = true,
                    canUploadHD = false
                )
                SubscriptionStatus.PREMIUM -> PhotoUploadLimits(
                    maxPhotos = 12,
                    requiresQualityCheck = false,
                    canUploadHD = true
                )
                SubscriptionStatus.VIP -> PhotoUploadLimits(
                    maxPhotos = 20,
                    requiresQualityCheck = false,
                    canUploadHD = true
                )
            }
        }
    }
}

data class ProfilePhoto(
    val id: String = "",
    val url: String = "",
    val isMain: Boolean = false,
    val uploadedAt: Date = Date(),
    val qualityScore: Float = 0f, // 0.0 - 1.0
    val isVerified: Boolean = false,
    val blurDetected: Boolean = false,
    val faceDetected: Boolean = true
)

data class ProfileBoost(
    val id: String = "",
    val userId: String = "",
    val type: BoostType = BoostType.REGULAR,
    val startTime: Date = Date(),
    val duration: Int = 30, // minutes
    val isActive: Boolean = false,
    val viewsGained: Int = 0,
    val matchesGained: Int = 0
)

enum class BoostType {
    REGULAR,      // 30 minutos
    SUPER_BOOST   // 2 horas, apenas VIP
}

data class VerificationBadge(
    val type: BadgeType,
    val isVerified: Boolean = false,
    val verifiedAt: Date? = null,
    val expiresAt: Date? = null
)

enum class BadgeType {
    PHOTO_VERIFIED,      // Verificação por selfie
    IDENTITY_VERIFIED,   // Verificação por documento
    LGBTQIA_PLUS,       // Auto-declaração LGBTQIA+
    NEURODIVERSE,       // Auto-declaração neurodiversidade
    ACTIVE_USER,        // Badge por atividade
    PREMIUM_MEMBER,     // Badge visual premium
    VIP_MEMBER         // Badge visual VIP
}

// Advanced Filters
data class AdvancedFilters(
    val ageRange: IntRange = 18..99,
    val maxDistance: Int = 50,
    val genderPreference: List<Gender> = emptyList(),
    val intentionPreference: List<Intention> = emptyList(),
    val verifiedOnly: Boolean = false,
    val hasPhotos: Boolean = true,
    val minPhotos: Int = 1,
    val recentlyActive: Boolean = false, // Ativo nas últimas 24h
    val heightRange: IntRange? = null,
    val educationLevel: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val smokingStatus: List<SmokingStatus> = emptyList(),
    val drinkingStatus: List<DrinkingStatus> = emptyList(),
    val hasChildren: List<ChildrenStatus> = emptyList(),
    val wantsChildren: List<ChildrenStatus> = emptyList(),
    val zodiacSigns: List<ZodiacSign> = emptyList(),
    val religions: List<Religion> = emptyList(),
    val petPreference: List<PetPreference> = emptyList()
)

// Analytics & Insights
data class ProfileAnalytics(
    val userId: String = "",
    val profileViews: ProfileViewStats = ProfileViewStats(),
    val matchStats: MatchStats = MatchStats(),
    val chatStats: ChatStats = ChatStats(),
    val activityStats: ActivityStats = ActivityStats(),
    val lastUpdated: Date = Date()
)

data class ProfileViewStats(
    val totalViews: Int = 0,
    val uniqueViews: Int = 0,
    val viewsToday: Int = 0,
    val viewsThisWeek: Int = 0,
    val viewsThisMonth: Int = 0,
    val peakHour: Int = 12, // 0-23
    val peakDay: String = "Sunday",
    val averageViewsPerDay: Double = 0.0
)

data class MatchStats(
    val totalMatches: Int = 0,
    val matchesToday: Int = 0,
    val matchesThisWeek: Int = 0,
    val matchesThisMonth: Int = 0,
    val matchRate: Double = 0.0, // percentage
    val averageMatchesPerDay: Double = 0.0,
    val bestMatchingTime: String = "Evening"
)

data class ChatStats(
    val conversationsStarted: Int = 0,
    val messagesReceived: Int = 0,
    val messagesSent: Int = 0,
    val responseRate: Double = 0.0, // percentage
    val averageResponseTime: Int = 0, // minutes
    val activeConversations: Int = 0
)

data class ActivityStats(
    val daysActive: Int = 0,
    val totalSwipes: Int = 0,
    val likesGiven: Int = 0,
    val likesReceived: Int = 0,
    val superLikesReceived: Int = 0,
    val profileUpdates: Int = 0,
    val lastActiveTime: Date = Date()
)

// Premium Features Usage Tracking
data class PremiumUsage(
    val userId: String = "",
    val subscription: SubscriptionStatus = SubscriptionStatus.FREE,
    val boostsUsed: Int = 0,
    val boostsRemaining: Int = 0,
    val superLikesUsed: Int = 0,
    val superLikesRemaining: Int = 0,
    val filtersUsed: Boolean = false,
    val analyticsViewed: Int = 0,
    val photosUploaded: Int = 0,
    val lastBoostDate: Date? = null,
    val subscriptionStartDate: Date = Date(),
    val subscriptionEndDate: Date? = null
)

// Helper extensions
fun ProfilePhoto.isHighQuality(): Boolean = qualityScore >= 0.7f

fun AdvancedFilters.isBasicFilter(): Boolean {
    return verifiedOnly == false && 
           hasPhotos == true && 
           minPhotos <= 1 && 
           recentlyActive == false && 
           heightRange == null &&
           educationLevel.isEmpty() &&
           interests.isEmpty() &&
           smokingStatus.isEmpty() &&
           drinkingStatus.isEmpty() &&
           hasChildren.isEmpty() &&
           wantsChildren.isEmpty() &&
           zodiacSigns.isEmpty() &&
           religions.isEmpty() &&
           petPreference.isEmpty()
}

fun BoostType.getDurationMinutes(): Int {
    return when (this) {
        BoostType.REGULAR -> 30
        BoostType.SUPER_BOOST -> 120
    }
}

fun BadgeType.getDisplayName(): String {
    return when (this) {
        BadgeType.PHOTO_VERIFIED -> "Foto Verificada"
        BadgeType.IDENTITY_VERIFIED -> "Identidade Verificada"
        BadgeType.LGBTQIA_PLUS -> "LGBTQIA+"
        BadgeType.NEURODIVERSE -> "Neurodiversidade"
        BadgeType.ACTIVE_USER -> "Usuário Ativo"
        BadgeType.PREMIUM_MEMBER -> "Premium"
        BadgeType.VIP_MEMBER -> "VIP"
    }
}

fun BadgeType.getDescription(): String {
    return when (this) {
        BadgeType.PHOTO_VERIFIED -> "Verificado por selfie"
        BadgeType.IDENTITY_VERIFIED -> "Verificado por documento"
        BadgeType.LGBTQIA_PLUS -> "Membro da comunidade LGBTQIA+"
        BadgeType.NEURODIVERSE -> "Neurodiversidade declarada"
        BadgeType.ACTIVE_USER -> "Usuário muito ativo"
        BadgeType.PREMIUM_MEMBER -> "Assinante Premium"
        BadgeType.VIP_MEMBER -> "Assinante VIP"
    }
}