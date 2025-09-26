package com.ideiassertiva.FypMatch.model

/**
 * Enhanced Neurodiversity Support System for Phase 4
 * Provides adaptive interfaces and assistance for neurodivergent users
 */

// Enhanced neurodiversity support models
data class NeuroAssistant(
    val userId: String,
    val profile: NeuroProfile,
    val activeAssistances: List<ActiveAssistance>,
    val settings: NeuroAssistanceSettings,
    val insights: List<NeuroInsight>
)

data class ActiveAssistance(
    val type: AssistanceType,
    val description: String,
    val isEnabled: Boolean,
    val customization: AssistanceCustomization
)

enum class AssistanceType {
    // Communication assistance
    CONVERSATION_STARTER_SUGGESTIONS,
    TONE_ANALYSIS,
    MESSAGE_CLARITY_CHECK,
    SOCIAL_CUE_EXPLANATION,
    
    // Interface assistance
    SIMPLIFIED_INTERFACE,
    REDUCED_ANIMATIONS,
    HIGH_CONTRAST_MODE,
    FOCUSED_MODE,
    
    // Sensory assistance
    PHOTO_FILTER_BRIGHT,
    PHOTO_FILTER_BUSY,
    SOUND_NOTIFICATION_CONTROL,
    HAPTIC_FEEDBACK_CONTROL,
    
    // Cognitive assistance
    STEP_BY_STEP_GUIDANCE,
    PROGRESS_TRACKING,
    ROUTINE_REMINDERS,
    DECISION_SUPPORT,
    
    // Emotional assistance
    EMOTION_RECOGNITION_HELP,
    ANXIETY_MANAGEMENT,
    SELF_REGULATION_TOOLS,
    POSITIVE_REINFORCEMENT
}

data class AssistanceCustomization(
    val intensity: Float, // 0.0 to 1.0
    val frequency: AssistanceFrequency,
    val customSettings: Map<String, Any>
)

enum class AssistanceFrequency {
    ALWAYS,
    WHEN_NEEDED,
    ON_REQUEST,
    SCHEDULED
}

data class NeuroAssistanceSettings(
    val enabledAssistances: Set<AssistanceType>,
    val globalIntensity: Float,
    val adaptiveMode: Boolean, // Automatically adjust based on behavior
    val privacyMode: PrivacyMode,
    val learningEnabled: Boolean // Allow system to learn preferences
)

enum class PrivacyMode {
    FULL_PRIVACY, // No data sharing about neurodiversity
    SELECTIVE_SHARING, // Share only chosen accommodations
    TRANSPARENT, // Open about neurodiversity needs
    ADVOCACY // Proud and educational about neurodiversity
}

data class NeuroInsight(
    val type: InsightType,
    val title: String,
    val description: String,
    val suggestions: List<String>,
    val priority: InsightPriority,
    val timestamp: java.util.Date
)

enum class InsightType {
    COMMUNICATION_PATTERN,
    SOCIAL_INTERACTION,
    SENSORY_PREFERENCE,
    ROUTINE_DISRUPTION,
    ENERGY_MANAGEMENT,
    SUCCESSFUL_STRATEGY
}

enum class InsightPriority {
    LOW, MEDIUM, HIGH, URGENT
}

// Conversation assistance for neurodivergent users
data class ConversationAssistance(
    val suggestions: List<ConversationSuggestion>,
    val socialCueExplanations: List<SocialCueExplanation>,
    val toneAnalysis: ToneAnalysis?,
    val clarityCheck: ClarityCheck?
)

data class ConversationSuggestion(
    val suggestion: String,
    val reason: String,
    val type: SuggestionType,
    val confidence: Float
)

enum class SuggestionType {
    OPENER,
    RESPONSE,
    FOLLOW_UP_QUESTION,
    TOPIC_TRANSITION,
    CLOSING,
    CLARIFICATION
}

data class SocialCueExplanation(
    val cue: String,
    val explanation: String,
    val examples: List<String>,
    val appropriateResponses: List<String>
)

data class ToneAnalysis(
    val detectedTone: String,
    val confidence: Float,
    val suggestions: List<String>,
    val warnings: List<String>
)

data class ClarityCheck(
    val isMessageClear: Boolean,
    val clarityScore: Float,
    val issues: List<ClarityIssue>,
    val improvements: List<String>
)

data class ClarityIssue(
    val type: ClarityIssueType,
    val description: String,
    val suggestion: String
)

enum class ClarityIssueType {
    TOO_VAGUE,
    TOO_COMPLEX,
    AMBIGUOUS,
    TOO_LONG,
    MISSING_CONTEXT,
    UNCLEAR_INTENT
}

// Neurodiversity-aware conversation analyzer
class NeuroConversationAnalyzer {
    
    fun analyzeMessage(
        message: String,
        context: List<Message>,
        senderProfile: NeuroProfile?
    ): ConversationAssistance {
        
        val suggestions = generateNeuroDiversityAwareSuggestions(message, context, senderProfile)
        val socialCues = analyzeSocialCues(message, context)
        val toneAnalysis = analyzeTone(message, senderProfile)
        val clarityCheck = checkClarity(message, senderProfile)
        
        return ConversationAssistance(
            suggestions = suggestions,
            socialCueExplanations = socialCues,
            toneAnalysis = toneAnalysis,
            clarityCheck = clarityCheck
        )
    }
    
    private fun generateNeuroDiversityAwareSuggestions(
        message: String,
        context: List<Message>,
        profile: NeuroProfile?
    ): List<ConversationSuggestion> {
        val suggestions = mutableListOf<ConversationSuggestion>()
        
        // If user needs clear communication
        if (profile?.preferences?.needsClearCommunication == true) {
            if (message.length > 200) {
                suggestions.add(ConversationSuggestion(
                    suggestion = "Considere dividir esta mensagem em partes menores para melhor clareza",
                    reason = "Mensagens mais curtas são mais fáceis de processar",
                    type = SuggestionType.CLARIFICATION,
                    confidence = 0.8f
                ))
            }
            
            if (!message.contains("?") && context.isNotEmpty()) {
                suggestions.add(ConversationSuggestion(
                    suggestion = "Que tal fazer uma pergunta para manter a conversa fluindo?",
                    reason = "Perguntas ajudam a estruturar a conversa",
                    type = SuggestionType.FOLLOW_UP_QUESTION,
                    confidence = 0.7f
                ))
            }
        }
        
        // If user prefers directness
        if (profile?.preferences?.prefersDirectness == true) {
            if (message.contains("talvez") || message.contains("meio que") || message.contains("tipo")) {
                suggestions.add(ConversationSuggestion(
                    suggestion = "Considere ser mais direto: diga exatamente o que você pensa",
                    reason = "Comunicação direta evita mal-entendidos",
                    type = SuggestionType.CLARIFICATION,
                    confidence = 0.9f
                ))
            }
        }
        
        // Context-based suggestions
        if (context.isEmpty()) {
            // First message suggestions
            suggestions.addAll(getNeuroFriendlyOpeners(profile))
        } else {
            // Response suggestions
            val lastMessage = context.lastOrNull()
            if (lastMessage != null) {
                suggestions.addAll(getNeuroFriendlyResponses(lastMessage, profile))
            }
        }
        
        return suggestions.take(5) // Limit to avoid overwhelming
    }
    
    private fun getNeuroFriendlyOpeners(profile: NeuroProfile?): List<ConversationSuggestion> {
        val openers = mutableListOf<ConversationSuggestion>()
        
        // Clear, structured openers
        openers.add(ConversationSuggestion(
            suggestion = "Olá! Vi seu perfil e gostei de [mencionar interesse específico]. Você pratica há muito tempo?",
            reason = "Opener estruturado que menciona interesse específico",
            type = SuggestionType.OPENER,
            confidence = 0.9f
        ))
        
        openers.add(ConversationSuggestion(
            suggestion = "Oi! Que coincidência deu match. Fiquei curioso sobre [algo do perfil]. Pode me contar mais?",
            reason = "Demonstra interesse genuíno e pede informação específica",
            type = SuggestionType.OPENER,
            confidence = 0.8f
        ))
        
        if (profile?.preferences?.needsClearCommunication == true) {
            openers.add(ConversationSuggestion(
                suggestion = "Olá! Sou [nome] e gostaria de conhecer você melhor. O que mais gosta de fazer no tempo livre?",
                reason = "Apresentação clara seguida de pergunta específica",
                type = SuggestionType.OPENER,
                confidence = 0.9f
            ))
        }
        
        return openers
    }
    
    private fun getNeuroFriendlyResponses(lastMessage: Message, profile: NeuroProfile?): List<ConversationSuggestion> {
        val responses = mutableListOf<ConversationSuggestion>()
        val content = lastMessage.content.lowercase()
        
        // If the last message was a question
        if (content.contains("?")) {
            responses.add(ConversationSuggestion(
                suggestion = "Responda à pergunta diretamente primeiro, depois adicione algo relacionado",
                reason = "Estrutura clara: resposta + informação adicional",
                type = SuggestionType.RESPONSE,
                confidence = 0.8f
            ))
        }
        
        // If they shared something personal
        if (content.contains("gosto") || content.contains("amo") || content.contains("adoro")) {
            responses.add(ConversationSuggestion(
                suggestion = "Mostre interesse genuíno: 'Que interessante! O que mais te atrai nisso?' ou compartilhe algo similar",
                reason = "Demonstra escuta ativa e mantém o foco no interesse da pessoa",
                type = SuggestionType.RESPONSE,
                confidence = 0.9f
            ))
        }
        
        return responses
    }
    
    private fun analyzeSocialCues(message: String, context: List<Message>): List<SocialCueExplanation> {
        val explanations = mutableListOf<SocialCueExplanation>()
        
        // Analyze emojis and their meanings
        if (message.contains("😅")) {
            explanations.add(SocialCueExplanation(
                cue = "😅 (emoji de sorriso nervoso)",
                explanation = "Pode indicar nervosismo, constrangimento leve, ou humor auto-depreciativo",
                examples = listOf("Acabei de tropeçar 😅", "Não sou muito bom nisso 😅"),
                appropriateResponses = listOf("Tudo bem, acontece com todo mundo", "Que nada, tenho certeza que você se sai bem")
            ))
        }
        
        if (message.contains("haha") || message.contains("kkk")) {
            explanations.add(SocialCueExplanation(
                cue = "Riso por escrito (haha, kkk)",
                explanation = "Indica que a pessoa achou algo engraçado ou está tentando manter o clima leve",
                examples = listOf("Que história engraçada haha", "Verdade kkk"),
                appropriateResponses = listOf("Também achei engraçado", "Fico feliz que tenha gostado", "😄")
            ))
        }
        
        // Analyze conversation patterns
        if (context.isNotEmpty()) {
            val lastFewMessages = context.takeLast(3)
            if (lastFewMessages.all { it.content.length < 50 }) {
                explanations.add(SocialCueExplanation(
                    cue = "Mensagens ficando mais curtas",
                    explanation = "Pode indicar perda de interesse, pressa, ou cansaço na conversa",
                    examples = listOf("ok", "ah", "sim"),
                    appropriateResponses = listOf(
                        "Mudar de assunto para algo mais interessante",
                        "Perguntar se está tudo bem",
                        "Sugerir continuar a conversa outro momento"
                    )
                ))
            }
        }
        
        return explanations
    }
    
    private fun analyzeTone(message: String, profile: NeuroProfile?): ToneAnalysis? {
        val content = message.lowercase()
        
        // Simple tone detection
        val tone = when {
            content.contains("!") && content.contains("😊") -> "Animado e positivo"
            content.contains("...") -> "Pensativo ou hesitante"
            content.contains("sorry") || content.contains("desculpa") -> "Apologético"
            content.contains("😢") || content.contains("triste") -> "Triste"
            content.contains("😡") || content.contains("raiva") -> "Irritado"
            else -> "Neutro"
        }
        
        val suggestions = when (tone) {
            "Pensativo ou hesitante" -> listOf(
                "A pessoa pode estar insegura. Seja encorajador",
                "Pode ser boa hora para fazer uma pergunta direta mas gentil"
            )
            "Apologético" -> listOf(
                "A pessoa pode estar se sentindo insegura",
                "Reassegure que está tudo bem"
            )
            else -> emptyList()
        }
        
        return ToneAnalysis(
            detectedTone = tone,
            confidence = 0.6f, // Simple detection has lower confidence
            suggestions = suggestions,
            warnings = if (tone == "Irritado") listOf("A pessoa pode estar chateada. Responda com cuidado") else emptyList()
        )
    }
    
    private fun checkClarity(message: String, profile: NeuroProfile?): ClarityCheck {
        val issues = mutableListOf<ClarityIssue>()
        val improvements = mutableListOf<String>()
        
        // Check message length
        if (message.length > 300) {
            issues.add(ClarityIssue(
                type = ClarityIssueType.TOO_LONG,
                description = "Mensagem muito longa pode ser difícil de processar",
                suggestion = "Considere dividir em mensagens menores"
            ))
            improvements.add("Dividir em parágrafos ou mensagens separadas")
        }
        
        // Check for vague language
        val vagueWords = listOf("coisa", "negócio", "sei lá", "tipo assim", "meio que")
        if (vagueWords.any { message.lowercase().contains(it) }) {
            issues.add(ClarityIssue(
                type = ClarityIssueType.TOO_VAGUE,
                description = "Linguagem vaga pode causar confusão",
                suggestion = "Seja mais específico sobre o que você quer dizer"
            ))
            improvements.add("Usar palavras mais específicas e precisas")
        }
        
        // Check for ambiguous pronouns
        val pronounCount = message.lowercase().split(" ").count { 
            it in listOf("isso", "aquilo", "ele", "ela", "isto")
        }
        if (pronounCount > 3) {
            issues.add(ClarityIssue(
                type = ClarityIssueType.AMBIGUOUS,
                description = "Muitos pronomes podem tornar a mensagem confusa",
                suggestion = "Repetir os nomes/objetos em vez de usar pronomes"
            ))
            improvements.add("Usar nomes específicos em vez de 'isso', 'aquilo', etc.")
        }
        
        val clarityScore = when (issues.size) {
            0 -> 1.0f
            1 -> 0.8f
            2 -> 0.6f
            3 -> 0.4f
            else -> 0.2f
        }
        
        return ClarityCheck(
            isMessageClear = issues.isEmpty(),
            clarityScore = clarityScore,
            issues = issues,
            improvements = improvements
        )
    }
}

// Sensory assistance for photo processing
class SensoryPhotoProcessor {
    
    fun processPhotoForSensoryNeeds(
        photoUrl: String,
        sensoryProfile: SensoryProfile
    ): ProcessedPhoto {
        
        val filters = mutableListOf<AppliedFilter>()
        var processedUrl = photoUrl
        
        // Apply brightness filter if needed
        if (sensoryProfile.reduceBrightness) {
            filters.add(AppliedFilter("brightness", -20))
            processedUrl = "$processedUrl?brightness=-20"
        }
        
        // Apply blur to busy backgrounds
        if (sensoryProfile.reduceBusyness) {
            filters.add(AppliedFilter("blur_background", 5))
            processedUrl = "$processedUrl&blur=5"
        }
        
        // Apply high contrast if needed
        if (sensoryProfile.increaseContrast) {
            filters.add(AppliedFilter("contrast", 30))
            processedUrl = "$processedUrl&contrast=30"
        }
        
        return ProcessedPhoto(
            originalUrl = photoUrl,
            processedUrl = processedUrl,
            appliedFilters = filters,
            description = generatePhotoDescription(photoUrl, sensoryProfile)
        )
    }
    
    private fun generatePhotoDescription(photoUrl: String, profile: SensoryProfile): String {
        // In a real implementation, this would use image recognition
        // For now, return a helpful placeholder
        return "Foto processada para reduzir sobrecarga sensorial"
    }
}

data class SensoryProfile(
    val reduceBrightness: Boolean = false,
    val reduceBusyness: Boolean = false,
    val increaseContrast: Boolean = false,
    val preferSimpleLayouts: Boolean = false,
    val reduceAnimations: Boolean = false
)

data class ProcessedPhoto(
    val originalUrl: String,
    val processedUrl: String,
    val appliedFilters: List<AppliedFilter>,
    val description: String
)

data class AppliedFilter(
    val type: String,
    val intensity: Int
)

// Interface adaptation system
class InterfaceAdaptation {
    
    fun adaptInterface(
        profile: NeuroProfile,
        currentScreen: String
    ): InterfaceConfiguration {
        
        val adaptations = mutableListOf<InterfaceAdaptation>()
        
        // Simplify interface if needed
        if (profile.accommodations.any { it.type == AccommodationType.SIMPLIFIED_INTERFACE }) {
            adaptations.add(InterfaceAdaptation(
                type = "simplified_layout",
                description = "Layout simplificado com menos elementos",
                isActive = true
            ))
        }
        
        // Reduce animations
        if (profile.accommodations.any { it.type == AccommodationType.REDUCED_STIMULATION }) {
            adaptations.add(InterfaceAdaptation(
                type = "reduced_animations",
                description = "Animações reduzidas ou desativadas",
                isActive = true
            ))
        }
        
        // Add clear instructions
        if (profile.accommodations.any { it.type == AccommodationType.CLEAR_INSTRUCTIONS }) {
            adaptations.add(InterfaceAdaptation(
                type = "enhanced_instructions",
                description = "Instruções mais claras e detalhadas",
                isActive = true
            ))
        }
        
        return InterfaceConfiguration(
            screenName = currentScreen,
            adaptations = adaptations,
            colorScheme = determineOptimalColorScheme(profile),
            fontSize = determineOptimalFontSize(profile),
            spacing = determineOptimalSpacing(profile)
        )
    }
    
    private fun determineOptimalColorScheme(profile: NeuroProfile): ColorScheme {
        return if (profile.accommodations.any { it.type == AccommodationType.SENSORY_FILTERING }) {
            ColorScheme.HIGH_CONTRAST
        } else {
            ColorScheme.STANDARD
        }
    }
    
    private fun determineOptimalFontSize(profile: NeuroProfile): FontSize {
        return if (profile.accommodations.any { it.type == AccommodationType.CLEAR_INSTRUCTIONS }) {
            FontSize.LARGE
        } else {
            FontSize.STANDARD
        }
    }
    
    private fun determineOptimalSpacing(profile: NeuroProfile): Spacing {
        return if (profile.accommodations.any { it.type == AccommodationType.REDUCED_STIMULATION }) {
            Spacing.EXPANDED
        } else {
            Spacing.STANDARD
        }
    }
}

data class InterfaceConfiguration(
    val screenName: String,
    val adaptations: List<InterfaceAdaptation>,
    val colorScheme: ColorScheme,
    val fontSize: FontSize,
    val spacing: Spacing
)

data class InterfaceAdaptation(
    val type: String,
    val description: String,
    val isActive: Boolean
)

enum class ColorScheme {
    STANDARD,
    HIGH_CONTRAST,
    DARK_MODE,
    REDUCED_BLUE_LIGHT
}

enum class FontSize {
    SMALL,
    STANDARD,
    LARGE,
    EXTRA_LARGE
}

enum class Spacing {
    COMPACT,
    STANDARD,
    EXPANDED,
    EXTRA_EXPANDED
}

// Neuro-friendly matching enhancements
class NeuroMatchingEnhancer {
    
    fun enhanceMatching(
        userProfile: NeuroProfile,
        potentialMatch: UserProfile,
        baseCompatibility: CompatibilityScore
    ): EnhancedCompatibilityScore {
        
        val neuroCompatibilityFactors = analyzeNeuroCompatibility(userProfile, potentialMatch)
        val communicationCompatibility = analyzeCommunicationCompatibility(userProfile, potentialMatch)
        val supportCompatibility = analyzeSupportCompatibility(userProfile, potentialMatch)
        
        val enhancedScore = baseCompatibility.overall + 
                           (neuroCompatibilityFactors * 0.2f) + 
                           (communicationCompatibility * 0.15f) + 
                           (supportCompatibility * 0.1f)
        
        return EnhancedCompatibilityScore(
            baseScore = baseCompatibility,
            neuroFactors = neuroCompatibilityFactors,
            communicationAlignment = communicationCompatibility,
            supportAlignment = supportCompatibility,
            finalScore = enhancedScore.coerceAtMost(1.0f),
            recommendations = generateNeuroMatchingRecommendations(userProfile, potentialMatch)
        )
    }
    
    private fun analyzeNeuroCompatibility(
        userProfile: NeuroProfile,
        potentialMatch: UserProfile
    ): Float {
        // This would analyze if both users have compatible neurodiversity profiles
        // For now, return a baseline score
        return 0.7f
    }
    
    private fun analyzeCommunicationCompatibility(
        userProfile: NeuroProfile,
        potentialMatch: UserProfile
    ): Float {
        // Analyze if their communication styles would work well together
        return 0.8f
    }
    
    private fun analyzeSupportCompatibility(
        userProfile: NeuroProfile,
        potentialMatch: UserProfile
    ): Float {
        // Analyze if they could be supportive of each other's needs
        return 0.6f
    }
    
    private fun generateNeuroMatchingRecommendations(
        userProfile: NeuroProfile,
        potentialMatch: UserProfile
    ): List<MatchingRecommendation> {
        val recommendations = mutableListOf<MatchingRecommendation>()
        
        if (userProfile.preferences.needsClearCommunication) {
            recommendations.add(MatchingRecommendation(
                type = "communication_tip",
                title = "Dica de Comunicação",
                description = "Seja direto e claro nas suas mensagens. Esta pessoa aprecia comunicação objetiva.",
                priority = InsightPriority.HIGH
            ))
        }
        
        if (userProfile.preferences.prefersTextOverVoice) {
            recommendations.add(MatchingRecommendation(
                type = "interaction_preference",
                title = "Preferência de Interação",
                description = "Esta pessoa prefere mensagens de texto. Evite chamadas de voz inicialmente.",
                priority = InsightPriority.MEDIUM
            ))
        }
        
        return recommendations
    }
}

data class EnhancedCompatibilityScore(
    val baseScore: CompatibilityScore,
    val neuroFactors: Float,
    val communicationAlignment: Float,
    val supportAlignment: Float,
    val finalScore: Float,
    val recommendations: List<MatchingRecommendation>
)

data class MatchingRecommendation(
    val type: String,
    val title: String,
    val description: String,
    val priority: InsightPriority
)