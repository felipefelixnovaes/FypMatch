package com.ideiassertiva.FypMatch.model

import java.util.Date
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Machine Learning Compatibility System for Phase 4
 * Analyzes user behavior and predicts compatibility scores
 */

// Core compatibility data structures
data class CompatibilityScore(
    val overall: Float, // 0.0 - 1.0
    val personalityMatch: Float,
    val behaviorMatch: Float, 
    val interestMatch: Float,
    val communicationMatch: Float,
    val confidenceLevel: Float, // How confident we are in this score
    val factors: List<CompatibilityFactor>
)

data class CompatibilityFactor(
    val name: String,
    val weight: Float,
    val score: Float,
    val description: String
)

// User behavior tracking for ML
data class SwipeBehavior(
    val userId: String,
    val swipeHistory: List<SwipeRecord>,
    val patterns: SwipePatterns,
    val preferences: InferredPreferences,
    val lastUpdated: Date = Date()
)

data class SwipePatterns(
    val averageSwipeTime: Long, // milliseconds
    val likeRate: Float, // percentage of right swipes
    val agePreferencePattern: AgePattern,
    val distancePattern: DistancePattern,
    val photoPreferences: PhotoPreferences,
    val bioReadingBehavior: BioReadingBehavior
)

data class AgePattern(
    val preferredMinAge: Int,
    val preferredMaxAge: Int,
    val mostLikedAgeRange: IntRange,
    val confidence: Float
)

data class DistancePattern(
    val averageDistance: Float,
    val maxAcceptableDistance: Float,
    val preferredDistance: Float
)

data class PhotoPreferences(
    val prefersMultiplePhotos: Boolean,
    val likesSmiling: Boolean,
    val likesOutdoorPhotos: Boolean,
    val likesGroupPhotos: Boolean,
    val prefersCloseUp: Boolean
)

data class BioReadingBehavior(
    val averageReadingTime: Long,
    val likesLongerBios: Boolean,
    val prefersKeywords: List<String>,
    val avoidsKeywords: List<String>
)

data class InferredPreferences(
    val personalityTypes: List<String>,
    val lifestyle: List<String>,
    val interests: List<String>,
    val dealBreakers: List<String>
)

// Personality analysis system
data class PersonalityProfile(
    val userId: String,
    val mbtiType: String?, // Can be null if not determined
    val traits: PersonalityTraits,
    val communicationStyle: CommunicationStyle,
    val socialPreferences: SocialPreferences,
    val analyzed: Date = Date(),
    val confidence: Float // How confident we are in this analysis
)

data class PersonalityTraits(
    val extraversion: Float, // 0.0 (introverted) to 1.0 (extraverted)
    val agreeableness: Float,
    val conscientiousness: Float,
    val neuroticism: Float, // 0.0 (stable) to 1.0 (neurotic)
    val openness: Float
)

data class CommunicationStyle(
    val directness: Float, // 0.0 (indirect) to 1.0 (very direct)
    val emotionality: Float, // How much they express emotions
    val humor: Float, // How much they use humor
    val responseSpeed: ResponseSpeed,
    val messageLength: MessageLength
)

enum class ResponseSpeed {
    IMMEDIATE, // < 5 minutes
    QUICK, // 5-30 minutes
    MODERATE, // 30 minutes - 2 hours
    SLOW, // 2-12 hours
    VERY_SLOW // > 12 hours
}

enum class MessageLength {
    VERY_SHORT, // < 20 chars
    SHORT, // 20-50 chars
    MEDIUM, // 50-150 chars
    LONG, // 150-300 chars
    VERY_LONG // > 300 chars
}

data class SocialPreferences(
    val prefersGroupActivities: Boolean,
    val likesDeepConversations: Boolean,
    val enjoysDebates: Boolean,
    val prefersPlannedActivities: Boolean,
    val socialEnergyLevel: Float // 0.0 (low) to 1.0 (high)
)

// Neurodiversity support enhancements
data class NeuroProfile(
    val userId: String,
    val selfReported: NeuroSelfReport?,
    val detectedTraits: List<NeuroTrait>,
    val accommodations: List<Accommodation>,
    val preferences: NeuroPreferences
)

data class NeuroSelfReport(
    val conditions: List<NeuroCondition>,
    val needsSupport: Boolean,
    val sharingPreference: SharingLevel
)

enum class NeuroCondition {
    AUTISM,
    ADHD,
    ANXIETY,
    DEPRESSION,
    DYSLEXIA,
    OTHER
}

enum class SharingLevel {
    PRIVATE, // Don't share with potential matches
    SELECTIVE, // Only after matching
    PUBLIC, // Show in profile
    PROUD // Highlight as positive trait
}

data class NeuroTrait(
    val trait: String,
    val likelihood: Float,
    val supportingEvidence: List<String>
)

data class Accommodation(
    val type: AccommodationType,
    val description: String,
    val implemented: Boolean
)

enum class AccommodationType {
    SENSORY_FILTERING,
    SIMPLIFIED_INTERFACE,
    EXTENDED_TIME,
    CLEAR_INSTRUCTIONS,
    REDUCED_STIMULATION,
    STRUCTURED_CONVERSATION
}

data class NeuroPreferences(
    val needsClearCommunication: Boolean,
    val prefersDirectness: Boolean,
    val sensitiveToCriticism: Boolean,
    val needsRoutine: Boolean,
    val prefersTextOverVoice: Boolean
)

// ML Compatibility Engine
class CompatibilityMLEngine {
    
    // Weights for different compatibility factors
    private val PERSONALITY_WEIGHT = 0.35f
    private val BEHAVIOR_WEIGHT = 0.25f
    private val INTEREST_WEIGHT = 0.20f
    private val COMMUNICATION_WEIGHT = 0.20f
    
    fun calculateCompatibility(
        user1: UserProfile,
        user2: UserProfile,
        user1Behavior: SwipeBehavior?,
        user2Behavior: SwipeBehavior?,
        user1Personality: PersonalityProfile?,
        user2Personality: PersonalityProfile?
    ): CompatibilityScore {
        
        val factors = mutableListOf<CompatibilityFactor>()
        
        // Calculate personality compatibility
        val personalityScore = calculatePersonalityCompatibility(user1Personality, user2Personality)
        factors.add(CompatibilityFactor(
            "Personalidade", 
            PERSONALITY_WEIGHT, 
            personalityScore,
            getPersonalityDescription(personalityScore)
        ))
        
        // Calculate behavior compatibility
        val behaviorScore = calculateBehaviorCompatibility(user1Behavior, user2Behavior)
        factors.add(CompatibilityFactor(
            "Comportamento", 
            BEHAVIOR_WEIGHT, 
            behaviorScore,
            getBehaviorDescription(behaviorScore)
        ))
        
        // Calculate interest compatibility
        val interestScore = calculateInterestCompatibility(user1, user2)
        factors.add(CompatibilityFactor(
            "Interesses", 
            INTEREST_WEIGHT, 
            interestScore,
            getInterestDescription(interestScore)
        ))
        
        // Calculate communication compatibility
        val communicationScore = calculateCommunicationCompatibility(user1Personality, user2Personality)
        factors.add(CompatibilityFactor(
            "Comunicação", 
            COMMUNICATION_WEIGHT, 
            communicationScore,
            getCommunicationDescription(communicationScore)
        ))
        
        // Calculate overall score
        val overall = factors.sumOf { it.weight * it.score.toDouble() }.toFloat()
        
        // Calculate confidence level
        val confidence = calculateConfidenceLevel(user1Personality, user2Personality, user1Behavior, user2Behavior)
        
        return CompatibilityScore(
            overall = overall,
            personalityMatch = personalityScore,
            behaviorMatch = behaviorScore,
            interestMatch = interestScore,
            communicationMatch = communicationScore,
            confidenceLevel = confidence,
            factors = factors
        )
    }
    
    private fun calculatePersonalityCompatibility(
        personality1: PersonalityProfile?,
        personality2: PersonalityProfile?
    ): Float {
        if (personality1 == null || personality2 == null) return 0.5f
        
        val traits1 = personality1.traits
        val traits2 = personality2.traits
        
        // Some traits work better when similar, others when complementary
        val extraversionCompat = calculateComplementaryScore(traits1.extraversion, traits2.extraversion)
        val agreeablenessCompat = calculateSimilarityScore(traits1.agreeableness, traits2.agreeableness)
        val conscientiousnessCompat = calculateSimilarityScore(traits1.conscientiousness, traits2.conscientiousness)
        val neuroticismCompat = 1.0f - calculateSimilarityScore(traits1.neuroticism, traits2.neuroticism) // Less neurotic mismatch is better
        val opennessCompat = calculateSimilarityScore(traits1.openness, traits2.openness)
        
        return (extraversionCompat + agreeablenessCompat + conscientiousnessCompat + neuroticismCompat + opennessCompat) / 5.0f
    }
    
    private fun calculateBehaviorCompatibility(
        behavior1: SwipeBehavior?,
        behavior2: SwipeBehavior?
    ): Float {
        if (behavior1 == null || behavior2 == null) return 0.5f
        
        // Compare swipe patterns
        val patterns1 = behavior1.patterns
        val patterns2 = behavior2.patterns
        
        val swipeTimeCompat = calculateSimilarityScore(
            patterns1.averageSwipeTime.toFloat() / 10000f, // Normalize
            patterns2.averageSwipeTime.toFloat() / 10000f
        )
        
        val likeRateCompat = calculateSimilarityScore(patterns1.likeRate, patterns2.likeRate)
        
        val agePatternCompat = calculateAgePatternCompatibility(patterns1.agePreferencePattern, patterns2.agePreferencePattern)
        
        return (swipeTimeCompat + likeRateCompat + agePatternCompat) / 3.0f
    }
    
    private fun calculateInterestCompatibility(user1: UserProfile, user2: UserProfile): Float {
        val interests1 = user1.interests.toSet()
        val interests2 = user2.interests.toSet()
        
        if (interests1.isEmpty() || interests2.isEmpty()) return 0.3f
        
        val commonInterests = interests1.intersect(interests2)
        val totalUniqueInterests = interests1.union(interests2)
        
        // Jaccard similarity
        val jaccardSimilarity = if (totalUniqueInterests.isNotEmpty()) {
            commonInterests.size.toFloat() / totalUniqueInterests.size.toFloat()
        } else 0f
        
        // Boost score if they have several common interests
        val boost = when (commonInterests.size) {
            0 -> 0f
            1 -> 0.1f
            2 -> 0.2f
            else -> 0.3f
        }
        
        return (jaccardSimilarity + boost).coerceAtMost(1.0f)
    }
    
    private fun calculateCommunicationCompatibility(
        personality1: PersonalityProfile?,
        personality2: PersonalityProfile?
    ): Float {
        if (personality1 == null || personality2 == null) return 0.5f
        
        val comm1 = personality1.communicationStyle
        val comm2 = personality2.communicationStyle
        
        val directnessCompat = calculateMidRangeCompatibility(comm1.directness, comm2.directness)
        val emotionalityCompat = calculateSimilarityScore(comm1.emotionality, comm2.emotionality)
        val humorCompat = calculateSimilarityScore(comm1.humor, comm2.humor)
        
        return (directnessCompat + emotionalityCompat + humorCompat) / 3.0f
    }
    
    // Utility functions
    private fun calculateSimilarityScore(value1: Float, value2: Float): Float {
        return 1.0f - kotlin.math.abs(value1 - value2)
    }
    
    private fun calculateComplementaryScore(value1: Float, value2: Float): Float {
        // For traits where opposites attract (like extraversion)
        val difference = kotlin.math.abs(value1 - value2)
        return if (difference > 0.3f && difference < 0.7f) {
            0.8f // Good complementary match
        } else if (difference <= 0.3f) {
            0.6f // Similar is okay
        } else {
            0.4f // Too different might be challenging
        }
    }
    
    private fun calculateMidRangeCompatibility(value1: Float, value2: Float): Float {
        // Best compatibility when both are in mid-range or both are similar
        val midRange1 = value1 in 0.3f..0.7f
        val midRange2 = value2 in 0.3f..0.7f
        
        return when {
            midRange1 && midRange2 -> 0.9f
            midRange1 || midRange2 -> 0.7f
            else -> calculateSimilarityScore(value1, value2)
        }
    }
    
    private fun calculateAgePatternCompatibility(pattern1: AgePattern, pattern2: AgePattern): Float {
        val overlap = calculateRangeOverlap(
            pattern1.mostLikedAgeRange,
            pattern2.mostLikedAgeRange
        )
        return overlap
    }
    
    private fun calculateRangeOverlap(range1: IntRange, range2: IntRange): Float {
        val overlapStart = maxOf(range1.first, range2.first)
        val overlapEnd = minOf(range1.last, range2.last)
        
        if (overlapStart > overlapEnd) return 0f
        
        val overlapSize = overlapEnd - overlapStart + 1
        val totalRangeSize = maxOf(range1.last - range1.first + 1, range2.last - range2.first + 1)
        
        return overlapSize.toFloat() / totalRangeSize.toFloat()
    }
    
    private fun calculateConfidenceLevel(
        personality1: PersonalityProfile?,
        personality2: PersonalityProfile?,
        behavior1: SwipeBehavior?,
        behavior2: SwipeBehavior?
    ): Float {
        var confidence = 0.0f
        var factors = 0
        
        personality1?.let { 
            confidence += it.confidence
            factors++
        }
        personality2?.let { 
            confidence += it.confidence 
            factors++
        }
        
        if (behavior1 != null) {
            confidence += 0.8f // Behavior data is quite reliable
            factors++
        }
        
        if (behavior2 != null) {
            confidence += 0.8f
            factors++
        }
        
        return if (factors > 0) confidence / factors else 0.3f
    }
    
    // Description generators
    private fun getPersonalityDescription(score: Float): String = when {
        score >= 0.8f -> "Personalidades muito compatíveis"
        score >= 0.6f -> "Boa compatibilidade de personalidades"
        score >= 0.4f -> "Personalidades moderadamente compatíveis"
        else -> "Personalidades podem ter diferenças significativas"
    }
    
    private fun getBehaviorDescription(score: Float): String = when {
        score >= 0.8f -> "Comportamentos muito similares no app"
        score >= 0.6f -> "Padrões de uso compatíveis"
        score >= 0.4f -> "Alguns padrões similares"
        else -> "Comportamentos diferentes no app"
    }
    
    private fun getInterestDescription(score: Float): String = when {
        score >= 0.8f -> "Muitos interesses em comum"
        score >= 0.6f -> "Vários interesses compartilhados"
        score >= 0.4f -> "Alguns interesses em comum"
        else -> "Poucos interesses compartilhados"
    }
    
    private fun getCommunicationDescription(score: Float): String = when {
        score >= 0.8f -> "Estilos de comunicação muito compatíveis"
        score >= 0.6f -> "Boa compatibilidade de comunicação"
        score >= 0.4f -> "Comunicação pode fluir bem"
        else -> "Estilos de comunicação diferentes"
    }
}

// Behavior analysis for learning
class BehaviorAnalyzer {
    
    fun analyzeSwipeBehavior(userId: String, swipeHistory: List<SwipeRecord>): SwipeBehavior {
        val patterns = analyzeSwipePatterns(swipeHistory)
        val preferences = inferPreferences(swipeHistory, patterns)
        
        return SwipeBehavior(
            userId = userId,
            swipeHistory = swipeHistory,
            patterns = patterns,
            preferences = preferences
        )
    }
    
    private fun analyzeSwipePatterns(swipeHistory: List<SwipeRecord>): SwipePatterns {
        if (swipeHistory.isEmpty()) {
            return SwipePatterns(
                averageSwipeTime = 3000L,
                likeRate = 0.5f,
                agePreferencePattern = AgePattern(18, 35, 22..28, 0.3f),
                distancePattern = DistancePattern(10f, 25f, 15f),
                photoPreferences = PhotoPreferences(false, false, false, false, false),
                bioReadingBehavior = BioReadingBehavior(0L, false, emptyList(), emptyList())
            )
        }
        
        val likes = swipeHistory.filter { it.action == SwipeAction.LIKE }
        val likeRate = likes.size.toFloat() / swipeHistory.size.toFloat()
        
        val averageSwipeTime = if (swipeHistory.isNotEmpty()) {
            swipeHistory.map { it.swipeTime ?: 3000L }.average().toLong()
        } else 3000L
        
        val likedAges = likes.mapNotNull { it.targetAge }
        val agePattern = if (likedAges.isNotEmpty()) {
            val minAge = likedAges.minOrNull() ?: 18
            val maxAge = likedAges.maxOrNull() ?: 35
            val mostLikedRange = calculateMostLikedAgeRange(likedAges)
            AgePattern(minAge, maxAge, mostLikedRange, 0.8f)
        } else {
            AgePattern(18, 35, 22..28, 0.3f)
        }
        
        // Simple photo preference analysis
        val photoPrefs = analyzePhotoPreferences(likes)
        val bioReading = analyzeBioReadingBehavior(likes)
        
        val distances = likes.mapNotNull { it.distance }
        val distancePattern = if (distances.isNotEmpty()) {
            DistancePattern(
                averageDistance = distances.average().toFloat(),
                maxAcceptableDistance = distances.maxOrNull() ?: 50f,
                preferredDistance = distances.sortedBy { it }[distances.size / 2] // Median
            )
        } else {
            DistancePattern(10f, 25f, 15f)
        }
        
        return SwipePatterns(
            averageSwipeTime = averageSwipeTime,
            likeRate = likeRate,
            agePreferencePattern = agePattern,
            distancePattern = distancePattern,
            photoPreferences = photoPrefs,
            bioReadingBehavior = bioReading
        )
    }
    
    private fun calculateMostLikedAgeRange(ages: List<Int>): IntRange {
        if (ages.isEmpty()) return 22..28
        
        val sorted = ages.sorted()
        val q1Index = sorted.size / 4
        val q3Index = (3 * sorted.size) / 4
        
        val q1 = sorted.getOrNull(q1Index) ?: sorted.first()
        val q3 = sorted.getOrNull(q3Index) ?: sorted.last()
        
        return q1..q3
    }
    
    private fun analyzePhotoPreferences(likes: List<SwipeRecord>): PhotoPreferences {
        // This would analyze the photos of liked profiles
        // For now, return defaults
        return PhotoPreferences(
            prefersMultiplePhotos = likes.size > 10, // Simple heuristic
            likesSmiling = true,
            likesOutdoorPhotos = false,
            likesGroupPhotos = false,
            prefersCloseUp = true
        )
    }
    
    private fun analyzeBioReadingBehavior(likes: List<SwipeRecord>): BioReadingBehavior {
        val avgReadTime = likes.mapNotNull { it.profileViewTime }.average().toLong()
        
        return BioReadingBehavior(
            averageReadingTime = avgReadTime,
            likesLongerBios = avgReadTime > 5000L,
            prefersKeywords = extractPreferredKeywords(likes),
            avoidsKeywords = emptyList() // Would analyze dislikes
        )
    }
    
    private fun extractPreferredKeywords(likes: List<SwipeRecord>): List<String> {
        // This would analyze the bios of liked profiles for common keywords
        return listOf("viagem", "música", "exercício", "natureza") // Placeholder
    }
    
    private fun inferPreferences(swipeHistory: List<SwipeRecord>, patterns: SwipePatterns): InferredPreferences {
        // Analyze the swipe history to infer user preferences
        val likes = swipeHistory.filter { it.action == SwipeAction.LIKE }
        
        return InferredPreferences(
            personalityTypes = inferPersonalityTypes(likes),
            lifestyle = inferLifestylePreferences(likes),
            interests = inferInterests(likes),
            dealBreakers = inferDealBreakers(swipeHistory.filter { it.action == SwipeAction.PASS })
        )
    }
    
    private fun inferPersonalityTypes(likes: List<SwipeRecord>): List<String> {
        // Analyze liked profiles for personality indicators
        return listOf("extrovertido", "aventureiro") // Placeholder
    }
    
    private fun inferLifestylePreferences(likes: List<SwipeRecord>): List<String> {
        return listOf("ativo", "social") // Placeholder
    }
    
    private fun inferInterests(likes: List<SwipeRecord>): List<String> {
        return listOf("viagem", "música") // Placeholder
    }
    
    private fun inferDealBreakers(passes: List<SwipeRecord>): List<String> {
        return emptyList() // Would analyze common patterns in passed profiles
    }
}

// Personality analyzer
class PersonalityAnalyzer {
    
    fun analyzePersonality(
        user: UserProfile,
        messageHistory: List<Message> = emptyList(),
        swipeBehavior: SwipeBehavior? = null
    ): PersonalityProfile {
        
        val traits = analyzeTraitsFromProfile(user, messageHistory)
        val communicationStyle = analyzeCommunicationStyle(messageHistory)
        val socialPrefs = analyzeSocialPreferences(user, swipeBehavior)
        val mbti = inferMBTI(traits)
        
        return PersonalityProfile(
            userId = user.id,
            mbtiType = mbti,
            traits = traits,
            communicationStyle = communicationStyle,
            socialPreferences = socialPrefs,
            confidence = calculatePersonalityConfidence(user, messageHistory)
        )
    }
    
    private fun analyzeTraitsFromProfile(user: UserProfile, messages: List<Message>): PersonalityTraits {
        var extraversion = 0.5f
        var agreeableness = 0.7f // Default to slightly agreeable
        var conscientiousness = 0.6f
        var neuroticism = 0.3f // Default to stable
        var openness = 0.5f
        
        // Analyze bio for personality indicators
        val bioLower = user.bio.lowercase()
        
        // Extraversion indicators
        when {
            bioLower.contains("extrovertido") || bioLower.contains("social") || 
            bioLower.contains("festa") || bioLower.contains("conhecer pessoas") -> {
                extraversion = 0.8f
            }
            bioLower.contains("introvertido") || bioLower.contains("tranquilo") ||
            bioLower.contains("casa") || bioLower.contains("leitura") -> {
                extraversion = 0.3f
            }
            bioLower.contains("aventura") || bioLower.contains("viajar") -> {
                extraversion += 0.2f
                openness += 0.3f
            }
        }
        
        // Analyze messages for additional indicators
        if (messages.isNotEmpty()) {
            val userMessages = messages.filter { it.senderId == user.id }
            
            // Analyze message patterns
            val avgLength = userMessages.map { it.content.length }.average()
            val hasEmojis = userMessages.any { it.content.contains(Regex("[😀-🙿]")) }
            val askQuestions = userMessages.count { it.content.contains("?") }
            
            // Adjust traits based on communication patterns
            if (avgLength > 100) conscientiousness += 0.2f
            if (hasEmojis) agreeableness += 0.1f
            if (askQuestions.toFloat() / userMessages.size > 0.3f) agreeableness += 0.2f
        }
        
        return PersonalityTraits(
            extraversion = extraversion.coerceIn(0f, 1f),
            agreeableness = agreeableness.coerceIn(0f, 1f),
            conscientiousness = conscientiousness.coerceIn(0f, 1f),
            neuroticism = neuroticism.coerceIn(0f, 1f),
            openness = openness.coerceIn(0f, 1f)
        )
    }
    
    private fun analyzeCommunicationStyle(messages: List<Message>): CommunicationStyle {
        if (messages.isEmpty()) {
            return CommunicationStyle(
                directness = 0.5f,
                emotionality = 0.5f,
                humor = 0.3f,
                responseSpeed = ResponseSpeed.MODERATE,
                messageLength = MessageLength.MEDIUM
            )
        }
        
        // Analyze message characteristics
        val avgLength = messages.map { it.content.length }.average()
        val messageLength = when {
            avgLength < 20 -> MessageLength.VERY_SHORT
            avgLength < 50 -> MessageLength.SHORT
            avgLength < 150 -> MessageLength.MEDIUM
            avgLength < 300 -> MessageLength.LONG
            else -> MessageLength.VERY_LONG
        }
        
        // Analyze emotionality (emojis, exclamation marks)
        val emotionalIndicators = messages.count { message ->
            message.content.contains(Regex("[😀-🙿!]+"))
        }
        val emotionality = (emotionalIndicators.toFloat() / messages.size).coerceAtMost(1.0f)
        
        // Analyze humor (laughing indicators, jokes)
        val humorIndicators = messages.count { message ->
            val content = message.content.lowercase()
            content.contains("haha") || content.contains("kkk") || 
            content.contains("😂") || content.contains("😄")
        }
        val humor = (humorIndicators.toFloat() / messages.size).coerceAtMost(1.0f)
        
        return CommunicationStyle(
            directness = 0.6f, // Would need more sophisticated analysis
            emotionality = emotionality,
            humor = humor,
            responseSpeed = ResponseSpeed.MODERATE, // Would analyze timestamps
            messageLength = messageLength
        )
    }
    
    private fun analyzeSocialPreferences(user: UserProfile, swipeBehavior: SwipeBehavior?): SocialPreferences {
        val bioLower = user.bio.lowercase()
        
        val prefersGroup = bioLower.contains("amigos") || bioLower.contains("grupo") || 
                          bioLower.contains("festa") || bioLower.contains("eventos")
        
        val likesDeep = bioLower.contains("profundo") || bioLower.contains("filosofia") ||
                       bioLower.contains("conversa séria") || bioLower.contains("significativo")
        
        val enjoysDebates = bioLower.contains("debate") || bioLower.contains("discussão") ||
                           bioLower.contains("política") || bioLower.contains("opinião")
        
        val prefersPlanned = bioLower.contains("plano") || bioLower.contains("organizado") ||
                            bioLower.contains("agenda") || user.interests.contains("organização")
        
        val energyLevel = when {
            bioLower.contains("energia") || bioLower.contains("ativo") || 
            bioLower.contains("esporte") || bioLower.contains("academia") -> 0.8f
            bioLower.contains("tranquilo") || bioLower.contains("calmo") ||
            bioLower.contains("relaxar") -> 0.3f
            else -> 0.5f
        }
        
        return SocialPreferences(
            prefersGroupActivities = prefersGroup,
            likesDeepConversations = likesDeep,
            enjoysDebates = enjoysDebates,
            prefersPlannedActivities = prefersPlanned,
            socialEnergyLevel = energyLevel
        )
    }
    
    private fun inferMBTI(traits: PersonalityTraits): String? {
        // Simple MBTI inference based on traits
        val e_i = if (traits.extraversion > 0.6f) "E" else "I"
        val s_n = if (traits.openness > 0.6f) "N" else "S"
        val f_t = if (traits.agreeableness > 0.6f) "F" else "T"
        val j_p = if (traits.conscientiousness > 0.6f) "J" else "P"
        
        return "$e_i$s_n$f_t$j_p"
    }
    
    private fun calculatePersonalityConfidence(user: UserProfile, messages: List<Message>): Float {
        var confidence = 0.3f // Base confidence
        
        if (user.bio.length > 100) confidence += 0.2f
        if (messages.size > 10) confidence += 0.3f
        if (user.interests.size >= 3) confidence += 0.2f
        
        return confidence.coerceAtMost(1.0f)
    }
}