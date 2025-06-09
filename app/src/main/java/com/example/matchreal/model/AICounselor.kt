package com.example.matchreal.model

import java.util.Date
import java.util.UUID

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
    val messageType: MessageType = MessageType.TEXT,
    val isHelpful: Boolean? = null, // Feedback do usuário
    val containsWarning: Boolean = false // Se contém recomendação de buscar ajuda profissional
)

enum class MessageSender {
    USER,
    AI_COUNSELOR
}

enum class MessageType {
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
Você é um conselheiro especializado em relacionamentos e habilidades sociais do MatchReal, um aplicativo de relacionamentos inclusivo. Sua missão é ajudar usuários a desenvolverem relacionamentos saudáveis e melhorarem suas habilidades sociais.

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