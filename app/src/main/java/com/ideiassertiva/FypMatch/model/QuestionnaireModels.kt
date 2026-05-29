// QuestionnaireModels.kt
// FypMatch — Modelos do sistema de questionários de compatibilidade
// Architecture Squad — Sprint 5

package com.ideiassertiva.FypMatch.model

import java.util.Date

// ─────────────────────────────────────────────
// Questionário do usuário (agregador raiz)
// ─────────────────────────────────────────────

data class UserQuestionnaire(
    val bigFive: BigFiveResult? = null,
    val values: ValuesResult? = null,
    val communicationStyle: CommunicationResult? = null,
    val routine: RoutineResult? = null,
    val dealBreakers: Set<DealBreaker> = emptySet(),
    val completedModes: Set<QuestionnaireMode> = emptySet(),
    val completedAt: Date? = null
) {
    /** Retorna true se o Modo Rápido está completamente preenchido */
    val isQuickModeComplete: Boolean
        get() = bigFive != null &&
                values != null &&
                communicationStyle != null &&
                routine != null
}

// ─────────────────────────────────────────────
// Modos do questionário
// ─────────────────────────────────────────────

enum class QuestionnaireMode(val displayName: String, val estimatedMinutes: Int) {
    QUICK("Modo Rápido", 5),
    DEEP("Modo Profundo", 20),
    SELF_KNOWLEDGE("Autoconhecimento", 10);

    val rawValue: String get() = name.lowercase()
}

// ─────────────────────────────────────────────
// Big Five (TIPI-10)
// ─────────────────────────────────────────────

/**
 * Resultado do TIPI-10 — cada fator em escala 1.0–7.0.
 *
 * Fórmulas (rev(x) = 8 - x):
 *   Extroversão        = (item1 + rev(item6)) / 2
 *   Amabilidade        = (rev(item2) + item7) / 2
 *   Conscienciosidade  = (item3 + rev(item8)) / 2
 *   Neuroticismo       = (item4 + rev(item9)) / 2
 *   Abertura           = (item5 + rev(item10)) / 2
 */
data class BigFiveResult(
    /** Extroversão */
    val extraversion: Double,
    /** Amabilidade */
    val agreeableness: Double,
    /** Conscienciosidade */
    val conscientiousness: Double,
    /** Neuroticismo */
    val neuroticism: Double,
    /** Abertura */
    val openness: Double,
    /** Versão do instrumento utilizado */
    val version: String = "TIPI-10"
) {
    /** Normaliza um fator da escala 1–7 para 0–100 */
    fun normalize(value: Double): Double = ((value - 1.0) / 6.0) * 100.0

    val extraversionNormalized: Double    get() = normalize(extraversion)
    val agreeablenessNormalized: Double   get() = normalize(agreeableness)
    val conscientiousnessNormalized: Double get() = normalize(conscientiousness)
    val neuroticismNormalized: Double     get() = normalize(neuroticism)
    val opennessNormalized: Double        get() = normalize(openness)
}

// ─────────────────────────────────────────────
// Valores (Schwartz simplificado)
// ─────────────────────────────────────────────

/** Top-3 valores em ordem de prioridade (índice 0 = 1º lugar) */
data class ValuesResult(
    val topValues: List<SchwartzValue>
) {
    init {
        require(topValues.size <= 3) { "ValuesResult aceita no máximo 3 valores" }
    }
}

enum class SchwartzValue(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    SECURITY(
        displayName = "Segurança",
        emoji = "🛡️",
        description = "Estabilidade, proteção e previsibilidade na vida"
    ),
    FREEDOM(
        displayName = "Liberdade",
        emoji = "🦋",
        description = "Autonomia, independência e autodeterminação"
    ),
    FAMILY(
        displayName = "Família",
        emoji = "🏡",
        description = "Vínculos próximos, cuidado e pertencimento"
    ),
    ACHIEVEMENT(
        displayName = "Realização",
        emoji = "🏆",
        description = "Sucesso, competência e reconhecimento"
    ),
    TRADITION(
        displayName = "Tradição",
        emoji = "🕯️",
        description = "Respeito ao legado, costumes e crenças herdadas"
    ),
    HEDONISM(
        displayName = "Hedonismo",
        emoji = "🎉",
        description = "Prazer, diversão e aproveitamento do presente"
    ),
    BENEVOLENCE(
        displayName = "Benevolência",
        emoji = "💛",
        description = "Cuidar do próximo e promover o bem-estar alheio"
    ),
    UNIVERSALISM(
        displayName = "Universalismo",
        emoji = "🌍",
        description = "Justiça social, sustentabilidade e igualdade"
    ),
    POWER(
        displayName = "Poder",
        emoji = "👑",
        description = "Influência, liderança e status social"
    ),
    CONFORMITY(
        displayName = "Conformidade",
        emoji = "🤝",
        description = "Harmonia social, respeito às normas e cooperação"
    )
}

// ─────────────────────────────────────────────
// Comunicação
// ─────────────────────────────────────────────

data class CommunicationResult(
    /** Q1 — Como lida quando está chateado(a) */
    val conflictStyle: ConflictStyle,
    /** Q2 — Preferência de frequência de mensagens */
    val messagingFrequency: MessagingPref,
    /** Q3 — Reação quando alguém some por horas (proxy de apego) */
    val absenceReaction: AbsenceReaction,
    /** Q4 — Profundidade de conversa preferida */
    val conversationDepth: ConvDepth,
    /** Q5 — Meio preferido para resolver conflito */
    val conflictMedium: ConflictMedium
)

/** Q1 — Estilo de conflito imediato */
enum class ConflictStyle(val displayName: String) {
    ON_THE_SPOT("Conversar na hora"),
    WAIT_COOL("Esperar esfriar"),
    WITHDRAW("Me afastar por um tempo")
}

/** Q2 — Frequência de mensagens */
enum class MessagingPref(val displayName: String) {
    FREQUENT("Gosto de frequência"),
    CONTEXTUAL("Depende do contexto"),
    SPACIOUS("Prefiro espaço")
}

/** Q3 — Reação à ausência (proxy de apego ansioso/evitante) */
enum class AbsenceReaction(val displayName: String) {
    NORMAL("Normal, não me afeta"),
    SLIGHTLY_ANXIOUS("Fico um pouco ansioso(a)"),
    VERY_ANXIOUS("Me preocupo muito")
}

/** Q4 — Profundidade de conversa */
enum class ConvDepth(val displayName: String) {
    DIRECT("Diretas e objetivas"),
    DEEP("Longas e profundas"),
    MIXED("Mistura dos dois")
}

/** Q5 — Meio preferido para conflito */
enum class ConflictMedium(val displayName: String) {
    VERBAL("Resolver na hora"),
    DEFERRED("Depois que a emoção baixa"),
    WRITTEN("Por escrito antes de falar")
}

// ─────────────────────────────────────────────
// Rotina e Energia
// ─────────────────────────────────────────────

data class RoutineResult(
    /** Q1 — Estilo de fim de semana */
    val weekendStyle: WeekendStyle,
    /** Q2 — Nível de planejamento vs espontaneidade */
    val planningStyle: PlanningStyle,
    /** Q3 — Fonte de energia (introversão/extroversão) */
    val energySource: EnergySource,
    /** Q4 — Equilíbrio trabalho-vida */
    val workLifeBalance: WorkLifeBalance,
    /** Q5 — Preferência de ambiente em casa */
    val homeNoise: HomeNoise
)

/** Q1 — Fim de semana */
enum class WeekendStyle(val displayName: String) {
    HOMEBODY("Em casa relaxando"),
    BALANCED("Equilíbrio"),
    EXPLORER("Sair e explorar")
}

/** Q2 — Planejamento vs espontaneidade */
enum class PlanningStyle(val displayName: String) {
    ROUTINE("Pessoa de rotina"),
    SPONTANEOUS("Espontâneo(a)"),
    MIXED("Depende do momento")
}

/** Q3 — Fonte de energia */
enum class EnergySource(val displayName: String) {
    INTROVERT("Sozinho(a), preciso de silêncio"),
    EXTROVERT("Com as pessoas")
}

/** Q4 — Trabalho e vida */
enum class WorkLifeBalance(val displayName: String) {
    AMBITIOUS("Trabalho muito, ambição alta"),
    BALANCED("Equilíbrio"),
    LEISURE_FIRST("Priorizo qualidade de vida")
}

/** Q5 — Ambiente em casa */
enum class HomeNoise(val displayName: String) {
    QUIET("Silêncio para me concentrar"),
    SOME_NOISE("Um pouco de som/movimento"),
    ANY_ENV("Qualquer ambiente")
}

// ─────────────────────────────────────────────
// Deal-Breakers
// ─────────────────────────────────────────────

enum class DealBreaker(val displayName: String, val icon: String) {
    SMOKING(
        displayName = "Fumar",
        icon = "🚬"
    ),
    EXCESSIVE_DRINKING(
        displayName = "Beber excessivamente",
        icon = "🍺"
    ),
    NO_CHILDREN_EVER(
        displayName = "Não quer filhos jamais",
        icon = "🚫"
    ),
    CHILDREN_MANDATORY(
        displayName = "Quer filhos obrigatoriamente",
        icon = "👶"
    ),
    INCOMPATIBLE_RELIGION(
        displayName = "Religiões incompatíveis",
        icon = "🙏"
    ),
    POLITICAL_OPPOSITE(
        displayName = "Politicamente muito oposto(a)",
        icon = "⚖️"
    ),
    OPEN_RELATIONSHIP(
        displayName = "Relacionamento aberto",
        icon = "🔓"
    ),
    VIOLENCE(
        displayName = "Violência/agressividade",
        icon = "⚡"
    ),
    INFIDELITY(
        displayName = "Infidelidade",
        icon = "💔"
    ),
    VERY_DIFFERENT_PACE(
        displayName = "Muito diferente de ritmo de vida",
        icon = "🕐"
    )
}
