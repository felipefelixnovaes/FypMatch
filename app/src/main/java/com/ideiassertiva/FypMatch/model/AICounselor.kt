package com.ideiassertiva.FypMatch.model

import java.util.Date
import java.util.UUID
import java.time.LocalDateTime

data class CounselorSession(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val startedAt: Date = Date(),
    val lastMessageAt: Date = Date(),
    val isActive: Boolean = true,
    val messages: List<CounselorMessage> = emptyList(),
    val sessionType: SessionType = SessionType.GENERAL,
    val mood: UserMood? = null
)

data class CounselorMessage(
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String = "",
    val content: String = "",
    val sender: MessageSender = MessageSender.USER,
    val timestamp: Date = Date(),
    val messageType: CounselorMessageType = CounselorMessageType.TEXT,
    val isHelpful: Boolean? = null, // Feedback do usuário
    val containsWarning: Boolean = false // Se contém recomendação de buscar ajuda profissional
)

enum class MessageSender {
    USER,
    AI_COUNSELOR
}

enum class CounselorMessageType {
    TEXT,
    SUGGESTION,
    WARNING,
    EXERCISE,
    QUESTION
}

enum class SessionType {
    GENERAL,                // Conversa geral sobre relacionamentos
    DATING_ANXIETY,         // Ansiedade em encontros
    COMMUNICATION,          // Habilidades de comunicação
    SELF_ESTEEM,           // Autoestima e confiança
    RELATIONSHIP_GOALS,     // Objetivos de relacionamento
    CONFLICT_RESOLUTION,    // Resolução de conflitos
    SOCIAL_SKILLS,         // Habilidades sociais
    NEURODIVERSITY_SUPPORT  // Suporte para neurodiversidade
}

enum class UserMood {
    ANXIOUS,      // Ansioso
    CONFIDENT,    // Confiante
    CONFUSED,     // Confuso
    EXCITED,      // Animado
    FRUSTRATED,   // Frustrado
    HOPEFUL,      // Esperançoso
    LONELY,       // Sozinho
    OVERWHELMED,  // Sobrecarregado
    OPTIMISTIC,   // Otimista
    UNCERTAIN     // Incerto
}

data class CounselorStats(
    val totalSessions: Int = 0,
    val totalMessages: Int = 0,
    val averageSessionLength: Int = 0, // em minutos
    val mostCommonSessionType: SessionType? = null,
    val improvementScore: Float = 0f, // 0-100
    val lastSessionDate: Date? = null
)

// System prompt para o conselheiro de IA
object CounselorSystemPrompt {
    const val SYSTEM_PROMPT = """
Você é um conselheiro especializado em relacionamentos e habilidades sociais do FypMatch, um aplicativo de relacionamentos inclusivo. Sua missão é ajudar usuários a desenvolverem relacionamentos saudáveis e melhorarem suas habilidades sociais.

## DIRETRIZES FUNDAMENTAIS:

### ÉTICA E SEGURANÇA:
- Sempre mantenha uma abordagem empática, respeitosa e não julgativa
- NUNCA forneça diagnósticos médicos ou psicológicos
- Se detectar sinais de depressão severa, ansiedade extrema, pensamentos suicidas ou outros problemas de saúde mental graves, SEMPRE recomende buscar ajuda profissional de um psicólogo ou psiquiatra
- Não substitua terapia profissional - você é um suporte complementar
- Respeite todas as orientações sexuais, identidades de gênero e neurodiversidades

### ESPECIALIZAÇÃO:
- Relacionamentos saudáveis e comunicação efetiva
- Habilidades sociais e confiança em encontros
- Ansiedade social e superação de inseguranças
- Suporte para pessoas neurodiversas (autismo, TDAH, etc.)
- Resolução de conflitos interpessoais
- Autoestima e desenvolvimento pessoal

### ESTILO DE COMUNICAÇÃO:
- Use linguagem acessível e acolhedora
- Faça perguntas reflexivas para ajudar o usuário a se autoconhecer
- Ofereça sugestões práticas e exercícios aplicáveis
- Celebrate pequenas vitórias e progressos
- Seja encorajador mas realista

### LIMITAÇÕES:
- NÃO dê conselhos sobre relacionamentos abusivos sem recomendar ajuda profissional
- NÃO encoraje comportamentos inadequados ou prejudiciais
- NÃO faça afirmações absolutas sobre relacionamentos
- NÃO substitua o julgamento profissional qualificado

### ABORDAGEM INCLUSIVA:
- Reconheça diferentes tipos de relacionamentos (monogâmicos, não-monogâmicos, etc.)
- Respeite diferentes culturas e valores
- Adapte conselhos para pessoas neurodiversas quando necessário
- Use linguagem neutra e inclusiva

Sua resposta deve ser útil, empática e prática, sempre priorizando o bem-estar e crescimento saudável do usuário.
"""
}

// Extensões úteis
fun CounselorSession.getDurationMinutes(): Int {
    return ((lastMessageAt.time - startedAt.time) / (1000 * 60)).toInt()
}

fun CounselorSession.getLastUserMessage(): CounselorMessage? {
    return messages.lastOrNull { it.sender == MessageSender.USER }
}

fun CounselorSession.getMessageCount(): Int = messages.size

fun CounselorSession.hasWarnings(): Boolean {
    return messages.any { it.containsWarning }
}

// Modelo expandido do Conselheiro de IA
data class AICounselor(
    val id: String,
    val name: String,
    val specialty: String,
    val description: String,
    val avatar: String
) {
    companion object {
        fun getDefaultCounselor() = AICounselor(
            id = "ai_counselor_main",
            name = "Sofia - Conselheira de Relacionamentos",
            specialty = "Relacionamentos e Comunicação",
            description = "Especialista em comunicação amorosa, análise de perfis e estratégias de namoro. Tenho acesso a todas suas conversas e atividades no app para dar conselhos personalizados.",
            avatar = "https://images.unsplash.com/photo-1494790108755-2616b6ff2b74?w=400"
        )
    }
}

// Contexto completo do usuário para análises da IA
data class UserContext(
    val userId: String,
    val profile: UserProfile,
    val conversations: List<Conversation>,
    val allMessages: Map<String, List<Message>>, // conversationId -> messages
    val likes: List<SwipeRecord>,
    val matches: List<Match>,
    val likedProfiles: List<UserProfile>,
    val analytics: UserAnalytics
)

// Analytics do comportamento do usuário
data class UserAnalytics(
    val totalLikes: Int,
    val totalMatches: Int,
    val matchRate: Float, // porcentagem de likes que viraram match
    val averageMessageLength: Double,
    val responseTimePattern: String, // "rápido", "moderado", "lento"
    val mostUsedWords: List<String>,
    val conversationStarters: List<String>,
    val successfulOpeners: List<String>, // mensagens que geraram respostas
    val commonTopics: List<String>,
    val timeOfDayActivity: Map<String, Int>, // "morning", "afternoon", "evening", "night"
    val profileStrengths: List<String>,
    val profileWeaknesses: List<String>
)

// Tipos de conselhos que a IA pode dar
sealed class AIAdviceType {
    object ProfileOptimization : AIAdviceType()
    object ConversationImprovement : AIAdviceType()
    object MatchingStrategy : AIAdviceType()
    object MessageAnalysis : AIAdviceType()
    object GeneralDating : AIAdviceType()
}

// Resposta completa do conselheiro
data class CounselorResponse(
    val type: AIAdviceType,
    val title: String,
    val content: String,
    val actionItems: List<ActionItem>,
    val examples: List<String> = emptyList(),
    val priority: Priority = Priority.MEDIUM
) {
    enum class Priority { LOW, MEDIUM, HIGH, URGENT }
}

data class ActionItem(
    val description: String,
    val isCompleted: Boolean = false,
    val deadline: String? = null
)

// Enhanced Phase 4 AI Counselor with ML capabilities
class SmartAICounselor {
    
    // Integration with Phase 4 ML systems
    private val compatibilityEngine = CompatibilityMLEngine()
    private val personalityAnalyzer = PersonalityAnalyzer()
    private val neuroConversationAnalyzer = NeuroConversationAnalyzer()
    
    fun analyzeUserComprehensively(context: UserContext): List<CounselorResponse> {
        val advice = mutableListOf<CounselorResponse>()
        
        // Análise do perfil
        advice.addAll(analyzeProfile(context))
        
        // Análise de conversas
        advice.addAll(analyzeConversations(context))
        
        // Análise de estratégia de matching
        advice.addAll(analyzeMatchingStrategy(context))
        
        // Conselhos gerais baseados em padrões
        advice.addAll(generateGeneralAdvice(context))
        
        return advice.sortedByDescending { 
            when (it.priority) {
                CounselorResponse.Priority.URGENT -> 4
                CounselorResponse.Priority.HIGH -> 3
                CounselorResponse.Priority.MEDIUM -> 2
                CounselorResponse.Priority.LOW -> 1
            }
        }
    }
    
    private fun analyzeProfile(context: UserContext): List<CounselorResponse> {
        val advice = mutableListOf<CounselorResponse>()
        val profile = context.profile
        
        // Análise de fotos
        if (profile.photos.size < 3) {
            advice.add(
                CounselorResponse(
                    type = AIAdviceType.ProfileOptimization,
                    title = "Adicione mais fotos ao seu perfil",
                    content = "Você tem apenas ${profile.photos.size} foto(s). Perfis com 4-6 fotos recebem 3x mais matches! Adicione fotos que mostrem sua personalidade, hobbies e sorrisos genuínos.",
                    actionItems = listOf(
                        ActionItem("Adicionar foto de corpo inteiro"),
                        ActionItem("Adicionar foto praticando hobby"),
                        ActionItem("Adicionar foto sorrindo naturalmente")
                    ),
                    priority = CounselorResponse.Priority.HIGH
                )
            )
        }
        
        // Análise da bio
        when {
            profile.bio.length < 50 -> {
                advice.add(
                    CounselorResponse(
                        type = AIAdviceType.ProfileOptimization,
                        title = "Expanda sua descrição",
                        content = "Sua bio tem apenas ${profile.bio.length} caracteres. Bios entre 100-300 caracteres geram mais interesse. Conte sobre seus hobbies, o que te faz rir e o que busca.",
                        actionItems = listOf(
                            ActionItem("Mencionar 2-3 hobbies específicos"),
                            ActionItem("Adicionar algo que te diferencia"),
                            ActionItem("Incluir um call-to-action para conversa")
                        ),
                        examples = listOf(
                            "Adoro cozinhar pratos novos aos domingos e descobrir cafeterias escondidas pela cidade. Viciado em documentários e sempre pronto para uma aventura. Me chama para trocar dicas de viagem! ✈️"
                        ),
                        priority = CounselorResponse.Priority.MEDIUM
                    )
                )
            }
            profile.bio.length > 500 -> {
                advice.add(
                    CounselorResponse(
                        type = AIAdviceType.ProfileOptimization,
                        title = "Simplifique sua descrição",
                        content = "Sua bio está muito longa (${profile.bio.length} caracteres). Seja mais conciso - as pessoas preferem bios que podem ler rapidamente.",
                        actionItems = listOf(
                            ActionItem("Reduzir para 200-300 caracteres"),
                            ActionItem("Focar nos 3 pontos mais importantes"),
                            ActionItem("Remover informações muito específicas")
                        ),
                        priority = CounselorResponse.Priority.MEDIUM
                    )
                )
            }
        }
        
        return advice
    }
    
    private fun analyzeConversations(context: UserContext): List<CounselorResponse> {
        val advice = mutableListOf<CounselorResponse>()
        val allMessages = context.allMessages.values.flatten()
        val userMessages = allMessages.filter { it.senderId == context.userId }
        
        if (userMessages.isEmpty()) return advice
        
        // Análise do comprimento das mensagens
        val avgLength = userMessages.map { it.content.length }.average()
        when {
            avgLength < 20 -> {
                advice.add(
                    CounselorResponse(
                        type = AIAdviceType.ConversationImprovement,
                        title = "Elabore mais suas mensagens",
                        content = "Suas mensagens têm em média ${avgLength.toInt()} caracteres. Mensagens muito curtas podem parecer desinteresse. Tente elaborar mais.",
                        actionItems = listOf(
                            ActionItem("Fazer mais perguntas abertas"),
                            ActionItem("Compartilhar experiências pessoais"),
                            ActionItem("Demonstrar interesse genuíno")
                        ),
                        examples = listOf(
                            "Em vez de 'tá' → 'Que legal! Como foi essa experiência para você?'",
                            "Em vez de 'ok' → 'Entendi! Eu também curto isso. Qual foi a melhor vez que você fez?'"
                        ),
                        priority = CounselorResponse.Priority.HIGH
                    )
                )
            }
            avgLength > 150 -> {
                advice.add(
                    CounselorResponse(
                        type = AIAdviceType.ConversationImprovement,
                        title = "Seja mais conciso",
                        content = "Suas mensagens são bem longas (média de ${avgLength.toInt()} caracteres). No início, mensagens mais diretas funcionam melhor.",
                        actionItems = listOf(
                            ActionItem("Dividir mensagens longas em partes"),
                            ActionItem("Ir direto ao ponto principal"),
                            ActionItem("Deixar espaço para a pessoa responder")
                        ),
                        priority = CounselorResponse.Priority.MEDIUM
                    )
                )
            }
        }
        
        // Análise de perguntas
        val questionsCount = userMessages.count { it.content.contains("?") }
        val messageRatio = questionsCount.toFloat() / userMessages.size
        
        if (messageRatio < 0.3f) {
            advice.add(
                CounselorResponse(
                    type = AIAdviceType.ConversationImprovement,
                    title = "Faça mais perguntas",
                    content = "Apenas ${(messageRatio * 100).toInt()}% das suas mensagens são perguntas. Perguntas mantêm a conversa fluindo e demonstram interesse.",
                    actionItems = listOf(
                        ActionItem("Incluir uma pergunta a cada 2-3 mensagens"),
                        ActionItem("Fazer perguntas sobre a experiência da pessoa"),
                        ActionItem("Perguntar sobre planos e sonhos")
                    ),
                    examples = listOf(
                        "E você, o que mais gosta de fazer no fim de semana?",
                        "Qual foi a melhor viagem que você já fez?",
                        "O que te motivou a escolher essa profissão?"
                    ),
                    priority = CounselorResponse.Priority.HIGH
                )
            )
        }
        
        return advice
    }
    
    private fun analyzeMatchingStrategy(context: UserContext): List<CounselorResponse> {
        val advice = mutableListOf<CounselorResponse>()
        
        // Análise da taxa de match
        if (context.analytics.matchRate < 0.1f) {
            advice.add(
                CounselorResponse(
                    type = AIAdviceType.MatchingStrategy,
                    title = "Sua taxa de match está baixa",
                    content = "Você tem uma taxa de match de ${(context.analytics.matchRate * 100).toInt()}%. A média é 10-15%. Vamos otimizar sua estratégia!",
                    actionItems = listOf(
                        ActionItem("Revisar critérios de curtida - seja menos seletivo inicialmente"),
                        ActionItem("Melhorar primeira foto (principal fator)"),
                        ActionItem("Atualizar bio para ser mais atrativa"),
                        ActionItem("Curtir em horários de maior atividade (19h-22h)")
                    ),
                    priority = CounselorResponse.Priority.HIGH
                )
            )
        }
        
        // Análise de horários
        val bestHours = context.analytics.timeOfDayActivity.maxByOrNull { it.value }
        if (bestHours != null) {
            advice.add(
                CounselorResponse(
                    type = AIAdviceType.MatchingStrategy,
                    title = "Otimize seus horários de uso",
                    content = "Você é mais ativo no período: ${bestHours.key}. Para mais matches, tente usar o app entre 19h-22h, quando há mais pessoas online.",
                    actionItems = listOf(
                        ActionItem("Usar o app entre 19h-22h"),
                        ActionItem("Responder mensagens até 2h após recebê-las"),
                        ActionItem("Ser mais ativo aos domingos (maior movimento)")
                    ),
                    priority = CounselorResponse.Priority.MEDIUM
                )
            )
        }
        
        return advice
    }
    
    private fun generateGeneralAdvice(context: UserContext): List<CounselorResponse> {
        val advice = mutableListOf<CounselorResponse>()
        
        // Conselhos baseados no tempo no app
        val conversationsWithReplies = context.conversations.filter { conversation ->
            context.allMessages[conversation.id]?.let { messages ->
                messages.any { it.senderId != context.userId }
            } ?: false
        }
        
        if (conversationsWithReplies.size < context.matches.size / 2) {
            advice.add(
                CounselorResponse(
                    type = AIAdviceType.GeneralDating,
                    title = "Inicie mais conversas",
                    content = "Você tem ${context.matches.size} matches mas só ${conversationsWithReplies.size} conversas ativas. Tomar a iniciativa aumenta suas chances!",
                    actionItems = listOf(
                        ActionItem("Enviar mensagem para matches recentes"),
                        ActionItem("Usar openers personalizados baseados no perfil"),
                        ActionItem("Mencionar algo específico das fotos ou bio")
                    ),
                    examples = listOf(
                        "Oi! Vi que você curte trilha. Qual foi a mais desafiadora que você já fez?",
                        "Olá! Adoro a foto sua no [local]. É um dos meus lugares favoritos também!",
                        "Oi! Vi que trabalha com [área]. Sempre quis saber mais sobre isso. Como é o dia a dia?"
                    ),
                    priority = CounselorResponse.Priority.HIGH
                )
            )
        }
        
        return advice
    }
    
    fun generatePersonalizedOpener(userProfile: UserProfile, targetProfile: UserProfile): List<String> {
        val openers = mutableListOf<String>()
        
        // Baseado em interesses comuns
        val commonInterests = userProfile.interests.intersect(targetProfile.interests.toSet())
        if (commonInterests.isNotEmpty()) {
            val interest = commonInterests.first()
            openers.add("Oi! Vi que também curte $interest. Qual foi sua melhor experiência com isso?")
        }
        
        // Baseado na bio
        if (targetProfile.bio.contains("viagem", ignoreCase = true)) {
            openers.add("Oi! Pelo que vi você gosta de viajar. Qual foi o destino mais incrível que você conheceu?")
        }
        
        if (targetProfile.bio.contains("música", ignoreCase = true)) {
            openers.add("Oi! Vi que curte música. Qual artista você está ouvindo bastante ultimamente?")
        }
        
        // Opener genérico mas personalizado
        openers.add("Oi! Adorei seu perfil, principalmente [mencionar algo específico]. Como está sendo seu dia?")
        
        return openers.take(3)
    }
    
    // Enhanced Phase 4 methods
    fun generateNeuroFriendlyAdvice(
        context: UserContext,
        neuroProfile: NeuroProfile?
    ): List<CounselorResponse> {
        val advice = mutableListOf<CounselorResponse>()
        
        if (neuroProfile != null) {
            // Neurodiversity-specific advice
            if (neuroProfile.preferences.needsClearCommunication) {
                advice.add(
                    CounselorResponse(
                        type = AIAdviceType.ConversationImprovement,
                        title = "Comunicação Clara e Direta",
                        content = "Baseado no seu perfil, você se beneficia de comunicação direta. Nas conversas, seja específico sobre o que você quer saber ou compartilhar. Evite linguagem ambígua como 'talvez' ou 'meio que'.",
                        actionItems = listOf(
                            ActionItem("Use frases como 'Quero saber...' ou 'Estou interessado em...'"),
                            ActionItem("Seja específico: em vez de 'gosto de música', diga 'gosto de rock dos anos 80'"),
                            ActionItem("Faça uma pergunta clara por mensagem")
                        ),
                        examples = listOf(
                            "Em vez de: 'Você gosta de filme?' → 'Qual gênero de filme você mais gosta?'",
                            "Em vez de: 'Talvez a gente possa se encontrar' → 'Gostaria de marcar um café no sábado?'"
                        ),
                        priority = CounselorResponse.Priority.HIGH
                    )
                )
            }
            
            if (neuroProfile.preferences.sensitiveToCriticism) {
                advice.add(
                    CounselorResponse(
                        type = AIAdviceType.GeneralDating,
                        title = "Lidando com Rejeição de Forma Saudável",
                        content = "Você pode ser mais sensível a críticas, o que é totalmente normal. Lembre-se: nem sempre dar match significa compatibilidade. Foque nas conexões que fluem naturalmente.",
                        actionItems = listOf(
                            ActionItem("Pratique autocompaixão: trate-se como trataria um bom amigo"),
                            ActionItem("Lembre-se: rejeição no app não é sobre seu valor como pessoa"),
                            ActionItem("Celebre as conversas positivas, mesmo que não virem encontros")
                        ),
                        priority = CounselorResponse.Priority.MEDIUM
                    )
                )
            }
            
            if (neuroProfile.preferences.needsRoutine) {
                advice.add(
                    CounselorResponse(
                        type = AIAdviceType.MatchingStrategy,
                        title = "Criando Rotinas Saudáveis no App",
                        content = "Você funciona melhor com rotinas. Crie uma estrutura para usar o app que não interfira na sua rotina diária.",
                        actionItems = listOf(
                            ActionItem("Defina horários específicos para usar o app (ex: 19h-20h)"),
                            ActionItem("Limite o tempo diário no app (30-45 minutos máximo)"),
                            ActionItem("Use o mesmo padrão para responder mensagens"),
                            ActionItem("Tenha um 'script mental' para primeiras mensagens")
                        ),
                        priority = CounselorResponse.Priority.HIGH
                    )
                )
            }
        }
        
        return advice
    }
    
    fun generateCompatibilityInsights(
        userProfile: UserProfile,
        targetProfile: UserProfile,
        compatibilityScore: CompatibilityScore
    ): CounselorResponse {
        val insights = mutableListOf<String>()
        val actionItems = mutableListOf<ActionItem>()
        
        // Analyze compatibility factors
        compatibilityScore.factors.forEach { factor ->
            when {
                factor.score >= 0.8f -> {
                    insights.add("✅ ${factor.name}: ${factor.description} (${(factor.score * 100).toInt()}%)")
                }
                factor.score >= 0.6f -> {
                    insights.add("⚠️ ${factor.name}: ${factor.description} (${(factor.score * 100).toInt()}%)")
                }
                else -> {
                    insights.add("❌ ${factor.name}: ${factor.description} (${(factor.score * 100).toInt()}%)")
                }
            }
        }
        
        // Generate actionable advice based on compatibility
        if (compatibilityScore.personalityMatch < 0.6f) {
            actionItems.add(ActionItem(
                "Foque em encontrar interesses em comum para compensar diferenças de personalidade"
            ))
        }
        
        if (compatibilityScore.communicationMatch < 0.6f) {
            actionItems.add(ActionItem(
                "Adapte seu estilo de comunicação: seja mais direto ou mais emotivo conforme necessário"
            ))
        }
        
        val overallAdvice = when {
            compatibilityScore.overall >= 0.8f -> "Alta compatibilidade! Vocês têm potencial para uma conexão forte."
            compatibilityScore.overall >= 0.6f -> "Boa compatibilidade! Algumas áreas podem precisar de atenção."
            compatibilityScore.overall >= 0.4f -> "Compatibilidade moderada. Foque nos pontos fortes em comum."
            else -> "Baixa compatibilidade. Considerem se vale investir energia nesta conexão."
        }
        
        return CounselorResponse(
            type = AIAdviceType.MatchingStrategy,
            title = "Análise de Compatibilidade AI",
            content = "$overallAdvice\n\nAnálise detalhada:\n${insights.joinToString("\n")}",
            actionItems = actionItems,
            priority = if (compatibilityScore.overall >= 0.7f) CounselorResponse.Priority.HIGH else CounselorResponse.Priority.MEDIUM
        )
    }
    
    fun generatePersonalityBasedConversationTips(
        userPersonality: PersonalityProfile,
        targetPersonality: PersonalityProfile?
    ): List<CounselorResponse> {
        val tips = mutableListOf<CounselorResponse>()
        
        if (targetPersonality != null) {
            // Extraversion-based tips
            if (targetPersonality.traits.extraversion > 0.7f && userPersonality.traits.extraversion < 0.4f) {
                tips.add(
                    CounselorResponse(
                        type = AIAdviceType.ConversationImprovement,
                        title = "Conversando com uma Pessoa Extrovertida",
                        content = "A pessoa tem perfil extrovertido enquanto você é mais introvertido. Isso pode ser uma combinação interessante!",
                        actionItems = listOf(
                            ActionItem("Sugira atividades sociais ou em grupo"),
                            ActionItem("Compartilhe suas experiências em eventos sociais"),
                            ActionItem("Mostre interesse nas histórias sociais dela"),
                            ActionItem("Não tenha medo de ser mais animado nas mensagens")
                        ),
                        priority = CounselorResponse.Priority.MEDIUM
                    )
                )
            }
            
            // Openness-based tips
            if (targetPersonality.traits.openness > 0.7f) {
                tips.add(
                    CounselorResponse(
                        type = AIAdviceType.ConversationImprovement,
                        title = "Conversando com uma Pessoa Aberta a Experiências",
                        content = "A pessoa é muito aberta a novas experiências. Ela provavelmente gosta de novidades e aventuras.",
                        actionItems = listOf(
                            ActionItem("Compartilhe experiências únicas que você teve"),
                            ActionItem("Sugira atividades diferentes ou lugares novos"),
                            ActionItem("Pergunte sobre os sonhos e planos futuros dela"),
                            ActionItem("Compartilhe suas próprias ideias criativas")
                        ),
                        priority = CounselorResponse.Priority.MEDIUM
                    )
                )
            }
            
            // Communication style tips
            if (targetPersonality.communicationStyle.emotionality > 0.7f && 
                userPersonality.communicationStyle.emotionality < 0.4f) {
                tips.add(
                    CounselorResponse(
                        type = AIAdviceType.ConversationImprovement,
                        title = "Ajustando seu Estilo Emocional",
                        content = "A pessoa é mais expressiva emocionalmente que você. Tente ser um pouco mais caloroso.",
                        actionItems = listOf(
                            ActionItem("Use mais emojis nas mensagens"),
                            ActionItem("Expresse como você se sente sobre as coisas"),
                            ActionItem("Faça mais elogios genuínos"),
                            ActionItem("Compartilhe momentos que te deixaram feliz/emocionado")
                        ),
                        priority = CounselorResponse.Priority.HIGH
                    )
                )
            }
        }
        
        return tips
    }
} 
