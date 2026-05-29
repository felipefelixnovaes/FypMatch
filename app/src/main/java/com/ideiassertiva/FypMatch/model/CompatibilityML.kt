package com.ideiassertiva.FypMatch.model

// FIXME: BehaviorAnalyzer rewritten as stubs — SwipeRecord/SwipeAction models missing

data class CompatibilityScore(
    val overall: Float,
    val factors: List<CompatibilityFactor>,
    val personalityMatch: Float = 0.5f,
    val communicationMatch: Float = 0.5f
)

data class CompatibilityFactor(
    val name: String,
    val score: Float,
    val description: String = ""
)

data class SwipeBehavior(
    val userId: String,
    val swipeHistory: List<Any>,
    val patterns: SwipePatterns,
    val preferences: Any?
)

data class SwipePatterns(
    val averageSwipeTime: Long,
    val likeRate: Float,
    val agePreferencePattern: AgePattern,
    val distancePattern: DistancePattern,
    val photoPreferences: PhotoPreferences,
    val bioReadingBehavior: BioReadingBehavior
)

data class AgePattern(
    val minAge: Int,
    val maxAge: Int,
    val mostLikedAgeRange: IntRange,
    val intensity: Float
)

data class DistancePattern(
    val minDistance: Float,
    val maxDistance: Float,
    val averageDistance: Float
)

data class PhotoPreferences(
    val prefersPhotos: Boolean,
    val prefersFullBody: Boolean,
    val prefersSelfies: Boolean,
    val prefersGroupPhotos: Boolean,
    val prefersProfessionalPhotos: Boolean
)

data class BioReadingBehavior(
    val averageReadingTime: Long,
    val readsFullBio: Boolean,
    val keywords: List<String>,
    val preferredTopics: List<String>
)

data class PersonalityProfile(
    val mbtiType: String = "INFP",
    val confidence: Float = 0.7f,
    val traits: PersonalityTraits = PersonalityTraits(),
    val communicationStyle: CommunicationStyle = CommunicationStyle()
)

data class CommunicationStyle(
    val emotionality: Float = 0.5f,
    val directness: Float = 0.5f,
    val formality: Float = 0.5f
)

// Behavior analysis for learning
class BehaviorAnalyzer {
    
    fun analyzeSwipeBehavior(userId: String, swipeHistory: List<Any>): SwipeBehavior {
        return SwipeBehavior(
            userId = userId,
            swipeHistory = emptyList(),
            patterns = SwipePatterns(3000L, 0.5f, AgePattern(18, 35, 22..28, 0.3f), DistancePattern(10f, 25f, 15f), PhotoPreferences(false, false, false, false, false), BioReadingBehavior(0L, false, emptyList(), emptyList())),
            preferences = null
        )
    }
}

class CompatibilityMLEngine {
    fun analyzeCompatibility(userId: String, targetId: String, currentUser: Any?, targetUser: Any?): CompatibilityScore {
        return CompatibilityScore(
            overall = 0.75f,
            factors = listOf(
                CompatibilityFactor("Interesses", 0.8f, "Vocês compartilham interesses em comum"),
                CompatibilityFactor("Estilo de Vida", 0.7f, "Seus estilos de vida são compatíveis")
            ),
            personalityMatch = 0.8f,
            communicationMatch = 0.7f
        )
    }
}

class PersonalityAnalyzer {
    fun analyzePersonality(currentUser: Any?, mockMessages: List<Any>): PersonalityProfile {
        return PersonalityProfile()
    }
}