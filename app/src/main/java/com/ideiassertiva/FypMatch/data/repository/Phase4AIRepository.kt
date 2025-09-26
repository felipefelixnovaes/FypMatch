package com.ideiassertiva.FypMatch.data.repository

import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 4 - Advanced AI and ML Repository
 * Manages compatibility scoring, personality analysis, and neurodiversity support
 */
@Singleton
class Phase4AIRepository @Inject constructor() {
    
    // ML and AI engines
    private val compatibilityEngine = CompatibilityMLEngine()
    private val behaviorAnalyzer = BehaviorAnalyzer()
    private val personalityAnalyzer = PersonalityAnalyzer()
    private val neuroConversationAnalyzer = NeuroConversationAnalyzer()
    private val sensoryPhotoProcessor = SensoryPhotoProcessor()
    private val interfaceAdaptation = InterfaceAdaptation()
    private val neuroMatchingEnhancer = NeuroMatchingEnhancer()
    
    // State management
    private val _compatibilityScores = MutableStateFlow<Map<String, CompatibilityScore>>(emptyMap())
    val compatibilityScores: Flow<Map<String, CompatibilityScore>> = _compatibilityScores.asStateFlow()
    
    private val _personalityProfiles = MutableStateFlow<Map<String, PersonalityProfile>>(emptyMap())
    val personalityProfiles: Flow<Map<String, PersonalityProfile>> = _personalityProfiles.asStateFlow()
    
    private val _swipeBehaviors = MutableStateFlow<Map<String, SwipeBehavior>>(emptyMap())
    val swipeBehaviors: Flow<Map<String, SwipeBehavior>> = _swipeBehaviors.asStateFlow()
    
    private val _neuroProfiles = MutableStateFlow<Map<String, NeuroProfile>>(emptyMap())
    val neuroProfiles: Flow<Map<String, NeuroProfile>> = _neuroProfiles.asStateFlow()
    
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: Flow<Boolean> = _isAnalyzing.asStateFlow()
    
    // Compatibility Analysis
    suspend fun analyzeCompatibility(
        userId1: String,
        userId2: String,
        user1Profile: UserProfile,
        user2Profile: UserProfile
    ): Result<CompatibilityScore> {
        return try {
            _isAnalyzing.value = true
            delay(1500) // Simulate ML processing time
            
            val user1Behavior = _swipeBehaviors.value[userId1]
            val user2Behavior = _swipeBehaviors.value[userId2]
            val user1Personality = _personalityProfiles.value[userId1]
            val user2Personality = _personalityProfiles.value[userId2]
            
            val compatibilityScore = compatibilityEngine.calculateCompatibility(
                user1Profile, user2Profile,
                user1Behavior, user2Behavior,
                user1Personality, user2Personality
            )
            
            // Cache the result
            val pairKey = "${userId1}_$userId2"
            _compatibilityScores.value = _compatibilityScores.value + (pairKey to compatibilityScore)
            
            _isAnalyzing.value = false
            Result.success(compatibilityScore)
        } catch (e: Exception) {
            _isAnalyzing.value = false
            Result.failure(e)
        }
    }
    
    // Personality Analysis
    suspend fun analyzePersonality(
        user: UserProfile,
        messageHistory: List<Message> = emptyList()
    ): Result<PersonalityProfile> {
        return try {
            _isAnalyzing.value = true
            delay(2000) // Simulate personality analysis processing
            
            val swipeBehavior = _swipeBehaviors.value[user.id]
            val personalityProfile = personalityAnalyzer.analyzePersonality(
                user, messageHistory, swipeBehavior
            )
            
            _personalityProfiles.value = _personalityProfiles.value + (user.id to personalityProfile)
            
            _isAnalyzing.value = false
            Result.success(personalityProfile)
        } catch (e: Exception) {
            _isAnalyzing.value = false
            Result.failure(e)
        }
    }
    
    // Behavior Analysis
    suspend fun analyzeSwipeBehavior(
        userId: String,
        swipeHistory: List<SwipeRecord>
    ): Result<SwipeBehavior> {
        return try {
            val swipeBehavior = behaviorAnalyzer.analyzeSwipeBehavior(userId, swipeHistory)
            _swipeBehaviors.value = _swipeBehaviors.value + (userId to swipeBehavior)
            
            Result.success(swipeBehavior)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Enhanced Conversation Analysis for Neurodiversity
    suspend fun analyzeConversationForNeuroSupport(
        message: String,
        context: List<Message>,
        senderUserId: String
    ): Result<ConversationAssistance> {
        return try {
            val neuroProfile = _neuroProfiles.value[senderUserId]
            val assistance = neuroConversationAnalyzer.analyzeMessage(message, context, neuroProfile)
            
            Result.success(assistance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Neurodiversity Profile Management
    suspend fun createNeuroProfile(
        userId: String,
        selfReport: NeuroSelfReport? = null,
        preferences: NeuroPreferences
    ): Result<NeuroProfile> {
        return try {
            val neuroProfile = NeuroProfile(
                userId = userId,
                selfReported = selfReport,
                detectedTraits = emptyList(), // Would be populated through behavioral analysis
                accommodations = generateRecommendedAccommodations(preferences),
                preferences = preferences
            )
            
            _neuroProfiles.value = _neuroProfiles.value + (userId to neuroProfile)
            
            Result.success(neuroProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Generate Smart Conversation Suggestions
    suspend fun generateSmartSuggestions(
        userId: String,
        conversationContext: List<Message>,
        targetUserId: String
    ): Result<List<AISuggestion>> {
        return try {
            delay(800) // Simulate AI processing
            
            val userPersonality = _personalityProfiles.value[userId]
            val targetPersonality = _personalityProfiles.value[targetUserId]
            val neuroProfile = _neuroProfiles.value[userId]
            
            val suggestions = generateContextAwareSuggestions(
                conversationContext, userPersonality, targetPersonality, neuroProfile
            )
            
            Result.success(suggestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Process Photos for Sensory Needs
    suspend fun processPhotoForSensoryNeeds(
        photoUrl: String,
        userId: String
    ): Result<ProcessedPhoto> {
        return try {
            val neuroProfile = _neuroProfiles.value[userId]
            val sensoryProfile = neuroProfile?.let { 
                extractSensoryProfile(it)
            } ?: SensoryProfile() // Default profile
            
            val processedPhoto = sensoryPhotoProcessor.processPhotoForSensoryNeeds(photoUrl, sensoryProfile)
            
            Result.success(processedPhoto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Adapt Interface for Neurodiversity
    fun getInterfaceConfiguration(
        userId: String,
        currentScreen: String
    ): InterfaceConfiguration {
        val neuroProfile = _neuroProfiles.value[userId]
        
        return if (neuroProfile != null) {
            interfaceAdaptation.adaptInterface(neuroProfile, currentScreen)
        } else {
            // Default configuration
            InterfaceConfiguration(
                screenName = currentScreen,
                adaptations = emptyList(),
                colorScheme = ColorScheme.STANDARD,
                fontSize = FontSize.STANDARD,
                spacing = Spacing.STANDARD
            )
        }
    }
    
    // Enhanced Compatibility with Neurodiversity Support
    suspend fun getEnhancedCompatibility(
        userId1: String,
        userId2: String,
        user1Profile: UserProfile,
        user2Profile: UserProfile
    ): Result<EnhancedCompatibilityScore> {
        return try {
            // First get base compatibility
            val baseCompatibility = compatibilityEngine.calculateCompatibility(
                user1Profile, user2Profile,
                _swipeBehaviors.value[userId1],
                _swipeBehaviors.value[userId2],
                _personalityProfiles.value[userId1],
                _personalityProfiles.value[userId2]
            )
            
            val user1NeuroProfile = _neuroProfiles.value[userId1]
            
            val enhancedScore = if (user1NeuroProfile != null) {
                neuroMatchingEnhancer.enhanceMatching(user1NeuroProfile, user2Profile, baseCompatibility)
            } else {
                // Convert base score to enhanced score without neuro factors
                EnhancedCompatibilityScore(
                    baseScore = baseCompatibility,
                    neuroFactors = 0.0f,
                    communicationAlignment = 0.5f,
                    supportAlignment = 0.5f,
                    finalScore = baseCompatibility.overall,
                    recommendations = emptyList()
                )
            }
            
            Result.success(enhancedScore)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Batch Analysis for Multiple Users
    suspend fun batchAnalyzePersonalities(
        users: List<UserProfile>,
        messageHistories: Map<String, List<Message>> = emptyMap()
    ): Result<Map<String, PersonalityProfile>> {
        return try {
            _isAnalyzing.value = true
            delay(3000) // Simulate batch processing
            
            val results = users.associate { user ->
                val messages = messageHistories[user.id] ?: emptyList()
                val swipeBehavior = _swipeBehaviors.value[user.id]
                val personality = personalityAnalyzer.analyzePersonality(user, messages, swipeBehavior)
                user.id to personality
            }
            
            _personalityProfiles.value = _personalityProfiles.value + results
            
            _isAnalyzing.value = false
            Result.success(results)
        } catch (e: Exception) {
            _isAnalyzing.value = false
            Result.failure(e)
        }
    }
    
    // Get Cached Data
    fun getCachedCompatibilityScore(userId1: String, userId2: String): CompatibilityScore? {
        val pairKey1 = "${userId1}_$userId2"
        val pairKey2 = "${userId2}_$userId1"
        return _compatibilityScores.value[pairKey1] ?: _compatibilityScores.value[pairKey2]
    }
    
    fun getCachedPersonalityProfile(userId: String): PersonalityProfile? {
        return _personalityProfiles.value[userId]
    }
    
    fun getCachedSwipeBehavior(userId: String): SwipeBehavior? {
        return _swipeBehaviors.value[userId]
    }
    
    fun getCachedNeuroProfile(userId: String): NeuroProfile? {
        return _neuroProfiles.value[userId]
    }
    
    // Clear Cache (for memory management)
    fun clearCache() {
        _compatibilityScores.value = emptyMap()
        _personalityProfiles.value = emptyMap()
        _swipeBehaviors.value = emptyMap()
        _neuroProfiles.value = emptyMap()
    }
    
    // Private helper methods
    private fun generateRecommendedAccommodations(preferences: NeuroPreferences): List<Accommodation> {
        val accommodations = mutableListOf<Accommodation>()
        
        if (preferences.needsClearCommunication) {
            accommodations.add(Accommodation(
                type = AccommodationType.CLEAR_INSTRUCTIONS,
                description = "Fornecer instruções claras e detalhadas",
                implemented = true
            ))
        }
        
        if (preferences.prefersDirectness) {
            accommodations.add(Accommodation(
                type = AccommodationType.STRUCTURED_CONVERSATION,
                description = "Estruturar conversas de forma mais direta",
                implemented = true
            ))
        }
        
        if (preferences.sensitiveToCriticism) {
            accommodations.add(Accommodation(
                type = AccommodationType.EXTENDED_TIME,
                description = "Dar tempo extra para processar feedback",
                implemented = true
            ))
        }
        
        if (preferences.needsRoutine) {
            accommodations.add(Accommodation(
                type = AccommodationType.SIMPLIFIED_INTERFACE,
                description = "Interface simplificada e consistente",
                implemented = true
            ))
        }
        
        return accommodations
    }
    
    private fun extractSensoryProfile(neuroProfile: NeuroProfile): SensoryProfile {
        val hasSensoryAccommodations = neuroProfile.accommodations.any { 
            it.type == AccommodationType.SENSORY_FILTERING 
        }
        
        val hasReductionNeeds = neuroProfile.accommodations.any {
            it.type == AccommodationType.REDUCED_STIMULATION
        }
        
        return SensoryProfile(
            reduceBrightness = hasSensoryAccommodations,
            reduceBusyness = hasReductionNeeds,
            increaseContrast = hasSensoryAccommodations,
            preferSimpleLayouts = neuroProfile.accommodations.any { 
                it.type == AccommodationType.SIMPLIFIED_INTERFACE 
            },
            reduceAnimations = hasReductionNeeds
        )
    }
    
    private fun generateContextAwareSuggestions(
        context: List<Message>,
        userPersonality: PersonalityProfile?,
        targetPersonality: PersonalityProfile?,
        neuroProfile: NeuroProfile?
    ): List<AISuggestion> {
        val suggestions = mutableListOf<AISuggestion>()
        
        // Personality-based suggestions
        if (userPersonality != null && targetPersonality != null) {
            if (targetPersonality.traits.extraversion > 0.7f) {
                suggestions.add(AISuggestion(
                    text = "Que tal sugerir uma atividade social? A pessoa parece ser bem extrovertida!",
                    reason = "Baseado no perfil de personalidade extrovertida"
                ))
            }
            
            if (targetPersonality.socialPreferences.likesDeepConversations) {
                suggestions.add(AISuggestion(
                    text = "Faça uma pergunta mais profunda sobre valores ou sonhos",
                    reason = "A pessoa gosta de conversas mais significativas"
                ))
            }
        }
        
        // Neurodiversity-aware suggestions
        if (neuroProfile?.preferences?.needsClearCommunication == true) {
            suggestions.add(AISuggestion(
                text = "Seja direto e específico na sua próxima mensagem",
                reason = "Comunicação clara funciona melhor para você"
            ))
        }
        
        // Context-based suggestions
        if (context.isNotEmpty()) {
            val lastMessage = context.lastOrNull()
            if (lastMessage != null && lastMessage.content.contains("?")) {
                suggestions.add(AISuggestion(
                    text = "Responda à pergunta dela primeiro, depois faça uma pergunta de volta",
                    reason = "Manter o fluxo de perguntas e respostas"
                ))
            }
        } else {
            // Opening message suggestions
            suggestions.add(AISuggestion(
                text = "Comente algo específico que você viu no perfil dela",
                reason = "Openers personalizados têm maior taxa de resposta"
            ))
        }
        
        return suggestions.take(3) // Limit to avoid overwhelming
    }
}

// Extension functions for easier data access
suspend fun Phase4AIRepository.getOrAnalyzePersonality(
    user: UserProfile,
    messageHistory: List<Message> = emptyList()
): PersonalityProfile {
    return getCachedPersonalityProfile(user.id) 
        ?: analyzePersonality(user, messageHistory).getOrThrow()
}

suspend fun Phase4AIRepository.getOrAnalyzeCompatibility(
    userId1: String,
    userId2: String,
    user1Profile: UserProfile,
    user2Profile: UserProfile
): CompatibilityScore {
    return getCachedCompatibilityScore(userId1, userId2)
        ?: analyzeCompatibility(userId1, userId2, user1Profile, user2Profile).getOrThrow()
}

// Data class for batch operations
data class BatchAnalysisRequest(
    val users: List<UserProfile>,
    val messageHistories: Map<String, List<Message>> = emptyMap(),
    val swipeHistories: Map<String, List<SwipeRecord>> = emptyMap()
)

data class BatchAnalysisResult(
    val personalities: Map<String, PersonalityProfile>,
    val behaviors: Map<String, SwipeBehavior>,
    val compatibilityMatrix: Map<Pair<String, String>, CompatibilityScore>
)