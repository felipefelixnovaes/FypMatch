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

// ─────────────────────────────────────────────
// Deep Mode Models (Sprint 6)
// ─────────────────────────────────────────────

// ─────────────────────────────────────────────
// IPIP-20 (Big Five — 20 itens representativos)
// ─────────────────────────────────────────────

/**
 * Resultado do IPIP-20 — 20 respostas em escala 1–5.
 *
 * Itens por fator (0-based):
 *   Extroversão:       0, 1, 2, 3
 *   Amabilidade:       4, 5, 6, 7
 *   Conscienciosidade: 8, 9, 10, 11
 *   Neuroticismo:      12, 13, 14, 15
 *   Abertura:          16, 17, 18, 19
 *
 * Itens reversos (0-based): 1, 5, 9, 13, 17
 *   Reversão: 6 - value (escala 1–5)
 */
data class IPIP20Result(val responses: List<Int>) {

    private fun rev(v: Int): Double = (6 - v).toDouble()
    private fun d(v: Int): Double   = v.toDouble()

    /** Extroversão — média dos itens 0–3 (item 1 reverso) */
    fun extroversion(): Double =
        listOf(d(responses[0]), rev(responses[1]), d(responses[2]), d(responses[3])).average()

    /** Amabilidade — média dos itens 4–7 (item 5 reverso) */
    fun agreeableness(): Double =
        listOf(d(responses[4]), rev(responses[5]), d(responses[6]), d(responses[7])).average()

    /** Conscienciosidade — média dos itens 8–11 (item 9 reverso) */
    fun conscientiousness(): Double =
        listOf(d(responses[8]), rev(responses[9]), d(responses[10]), d(responses[11])).average()

    /** Neuroticismo — média dos itens 12–15 (item 13 reverso) */
    fun neuroticism(): Double =
        listOf(d(responses[12]), rev(responses[13]), d(responses[14]), d(responses[15])).average()

    /** Abertura — média dos itens 16–19 (item 17 reverso) */
    fun openness(): Double =
        listOf(d(responses[16]), rev(responses[17]), d(responses[18]), d(responses[19])).average()

    /** Converte valor na escala 1–5 para 0–100 */
    private fun toPercent(v: Double): Double = (v - 1.0) / 4.0 * 100.0

    /** Scores normalizados 0–100 como BigFiveResult */
    fun normalized(): BigFiveResult = BigFiveResult(
        extraversion      = toPercent(extroversion()),
        agreeableness     = toPercent(agreeableness()),
        conscientiousness = toPercent(conscientiousness()),
        neuroticism       = toPercent(neuroticism()),
        openness          = toPercent(openness()),
        version           = "IPIP-20"
    )
}

// ─────────────────────────────────────────────
// PVQ-21 (Schwartz Portrait Values Questionnaire)
// ─────────────────────────────────────────────

/**
 * Resultado do PVQ-21 — 21 respostas em escala 1–6.
 *
 * Escala original: 1 = Muito parecido comigo → 6 = Não se parece nada comigo.
 * Score de valor = 7 - resposta  (inversão para 1 = baixo, 6 = alto alinhamento).
 *
 * Mapeamento dos 10 valores (0-based):
 *   Conformidade:    [0, 10]
 *   Tradição:        [1, 11]
 *   Benevolência:    [2, 12]
 *   Universalismo:   [3, 13, 19]
 *   Autodireção:     [4, 14]
 *   Estimulação:     [5, 15]
 *   Hedonismo:       [6, 20]
 *   Realização:      [7, 16]
 *   Poder:           [8, 17]
 *   Segurança:       [9, 18]
 */
data class PVQ21Result(val responses: List<Int>) {

    private fun inv(idx: Int): Double = (7 - responses[idx]).toDouble()
    private fun mean(indices: List<Int>): Double = indices.map { inv(it) }.average()

    fun conformity():    Double = mean(listOf(0, 10))
    fun tradition():     Double = mean(listOf(1, 11))
    fun benevolence():   Double = mean(listOf(2, 12))
    fun universalism():  Double = mean(listOf(3, 13, 19))
    fun selfDirection(): Double = mean(listOf(4, 14))
    fun stimulation():   Double = mean(listOf(5, 15))
    fun hedonism():      Double = mean(listOf(6, 20))
    fun achievement():   Double = mean(listOf(7, 16))
    fun power():         Double = mean(listOf(8, 17))
    fun security():      Double = mean(listOf(9, 18))

    /** Top 3 valores com maior score */
    fun topValues(): List<SchwartzValue> {
        val ranked = listOf(
            SchwartzValue.CONFORMITY   to conformity(),
            SchwartzValue.TRADITION    to tradition(),
            SchwartzValue.BENEVOLENCE  to benevolence(),
            SchwartzValue.UNIVERSALISM to universalism(),
            SchwartzValue.FREEDOM      to selfDirection(),
            SchwartzValue.HEDONISM     to hedonism(),
            SchwartzValue.ACHIEVEMENT  to achievement(),
            SchwartzValue.POWER        to power(),
            SchwartzValue.SECURITY     to security()
        ).sortedByDescending { it.second }
        return ranked.take(3).map { it.first }
    }
}

// ─────────────────────────────────────────────
// ECR-RS (Experiences in Close Relationships — Revised Short, 12 itens)
// ─────────────────────────────────────────────

/**
 * Resultado do ECR-RS-12 — 12 respostas em escala 1–7.
 *
 * Ansiedade:  itens 0–5  (6 itens, todos diretos)
 * Evitação:   itens 6–11 (6 itens, todos diretos)
 *
 * Cutoffs: ansiedade alta = score > 3.5 | evitação alta = score > 3.5
 */
data class ECRRSResult(val responses: List<Int>) {

    /** Média dos 6 itens de ansiedade (índices 0–5) */
    fun anxietyScore(): Double = responses.subList(0, 6).map { it.toDouble() }.average()

    /** Média dos 6 itens de evitação (índices 6–11) */
    fun avoidanceScore(): Double = responses.subList(6, 12).map { it.toDouble() }.average()

    /** Classificação do estilo de apego baseada nos cutoffs */
    fun attachmentStyle(): AttachmentStyle {
        val highAnxiety   = anxietyScore()   > 3.5
        val highAvoidance = avoidanceScore() > 3.5
        return when {
            !highAnxiety && !highAvoidance -> AttachmentStyle.SECURE
            highAnxiety  && !highAvoidance -> AttachmentStyle.ANXIOUS
            !highAnxiety &&  highAvoidance -> AttachmentStyle.AVOIDANT
            else                           -> AttachmentStyle.DISORGANIZED
        }
    }
}

enum class AttachmentStyle(
    val displayName: String,
    val description: String,
    val emoji: String
) {
    SECURE(
        displayName = "Seguro",
        description = "Confortável com intimidade e independência",
        emoji = "💚"
    ),
    ANXIOUS(
        displayName = "Ansioso",
        description = "Busca muita proximidade, teme abandono",
        emoji = "💛"
    ),
    AVOIDANT(
        displayName = "Evitante",
        description = "Prefere independência, desconforto com intimidade",
        emoji = "🩶"
    ),
    DISORGANIZED(
        displayName = "Desorganizado",
        description = "Ambivalente entre proximidade e distância",
        emoji = "🔮"
    )
}

// ─────────────────────────────────────────────
// Conflito Profundo (6 cenários)
// ─────────────────────────────────────────────

data class DeepConflictResult(
    /** Como resolve conflitos */
    val resolutionStyle: ConflictResolutionStyle,
    /** Como expressa emoções */
    val emotionalExpression: EmotionalExpression,
    /** Como repara após conflito */
    val repairBehavior: RepairBehavior,
    /** Quanto tempo precisa depois de uma briga */
    val silencePeriod: SilencePeriod,
    /** Como pede desculpa */
    val apologyStyle: ApologyStyle,
    /** Como recebe críticas */
    val feedbackTolerance: FeedbackTolerance
)

enum class ConflictResolutionStyle(val displayName: String) {
    IMMEDIATE("Resolver na hora"),
    COOLING_OFF("Processar antes de conversar"),
    AVOIDANT("Evitar conflito, ceder pela paz"),
    ANALYTICAL("Entender a raiz antes de resolver")
}

enum class EmotionalExpression(val displayName: String) {
    EXPRESSIVE("Expresso com intensidade"),
    MODERATE("Expresso de forma controlada"),
    CONTAINED("Guardo até estar seguro(a)"),
    WRITTEN("Escrevo antes de falar")
}

enum class RepairBehavior(val displayName: String) {
    IMMEDIATE("Busco reconciliação imediatamente"),
    GRADUAL("Reaproximento aos poucos"),
    WAIT_OTHER("Espero o outro dar o primeiro passo"),
    GESTURE("Demonstro com gesto ou ato")
}

enum class SilencePeriod(val displayName: String) {
    NONE("Não preciso de silêncio"),
    HOURS("Algumas horas"),
    DAY("Pelo menos um dia"),
    DAYS("Alguns dias")
}

enum class ApologyStyle(val displayName: String) {
    VERBAL("Digo claramente que errei"),
    ACTION("Demonstro com atitudes"),
    BOTH("Falo e mostro com ações"),
    DIFFICULT("Tenho dificuldade em pedir desculpa")
}

enum class FeedbackTolerance(val displayName: String) {
    RECEPTIVE("Recebo bem, gosto de melhorar"),
    CONTEXTUAL("Depende de como é dado"),
    SENSITIVE("Sinto muito, preciso de tempo"),
    RESISTANT("Fico na defensiva inicialmente")
}

// ─────────────────────────────────────────────
// Projeto de Vida (5 dimensões)
// ─────────────────────────────────────────────

data class LifeProjectResult(
    val childrenDesire:      ChildrenDesire,
    val locationFlexibility: LocationFlexibility,
    val careerPriority:      CareerPriority,
    val financialApproach:   FinancialApproach,
    val spiritualityRole:    SpiritualityRole
)

enum class ChildrenDesire(val displayName: String) {
    DEFINITELY_YES("Quero filhos, é prioridade"),
    OPEN_TO_IT("Aberto(a), se a relação caminhar bem"),
    UNDECIDED("Ainda não sei"),
    PROBABLY_NOT("Provavelmente não"),
    DEFINITELY_NO("Não quero filhos")
}

enum class LocationFlexibility(val displayName: String) {
    STAY_HERE("Ficar na minha cidade"),
    OPEN_SAME_CITY("Mudar dentro da mesma cidade"),
    OPEN_SAME_STATE("Aberto(a) a mudar de estado"),
    OPEN_BRAZIL("Qualquer lugar do Brasil"),
    OPEN_WORLD("Qualquer lugar do mundo")
}

enum class CareerPriority(val displayName: String) {
    VERY_HIGH("Carreira é central na minha vida"),
    BALANCED("Equilíbrio entre carreira e vida pessoal"),
    LIFE_FIRST("Vida pessoal vem primeiro"),
    TRANSITIONING("Estou em transição de carreira")
}

enum class FinancialApproach(val displayName: String) {
    SAVER("Poupo muito, priorizo segurança financeira"),
    BALANCED("Equilíbrio entre poupar e aproveitar"),
    SPENDER("Prefiro viver bem hoje"),
    INVESTOR("Invisto ativamente, penso em patrimônio")
}

enum class SpiritualityRole(val displayName: String) {
    CENTRAL("Espiritualidade é central na minha vida"),
    IMPORTANT("É importante, mas não define tudo"),
    PERSONAL("É pessoal, prefiro não misturar"),
    NOT_IMPORTANT("Não é importante para mim")
}

// ─────────────────────────────────────────────
// Container do Modo Profundo
// ─────────────────────────────────────────────

/**
 * Agrega todos os módulos do Modo Profundo (Sprint 6).
 * Complementa UserQuestionnaire sem substituí-lo.
 */
data class DeepModeQuestionnaire(
    val userId: String,
    val completedAt: Date? = null,
    val ipip20: IPIP20Result? = null,
    val pvq21: PVQ21Result? = null,
    val ecrrs: ECRRSResult? = null,
    val conflictDeep: DeepConflictResult? = null,
    val lifeProject: LifeProjectResult? = null
) {
    /** Retorna true se todos os 5 módulos foram respondidos */
    val isComplete: Boolean
        get() = ipip20 != null && pvq21 != null && ecrrs != null &&
                conflictDeep != null && lifeProject != null

    /** Percentual de conclusão em múltiplos de 20 */
    val completionPercentage: Int
        get() {
            val completed = listOf(
                ipip20 != null,
                pvq21 != null,
                ecrrs != null,
                conflictDeep != null,
                lifeProject != null
            ).count { it }
            return completed * 20
        }
}
