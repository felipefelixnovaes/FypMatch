package com.ideiassertiva.FypMatch.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ideiassertiva.FypMatch.model.AttachmentStyle
import com.ideiassertiva.FypMatch.model.ApologyStyle
import com.ideiassertiva.FypMatch.model.ArchetypeResult
import com.ideiassertiva.FypMatch.model.CareerPriority
import com.ideiassertiva.FypMatch.model.ChildrenDesire
import com.ideiassertiva.FypMatch.model.ConflictResolutionStyle
import com.ideiassertiva.FypMatch.model.DeepConflictResult
import com.ideiassertiva.FypMatch.model.DeepModeQuestionnaire
import com.ideiassertiva.FypMatch.model.ECRRSResult
import com.ideiassertiva.FypMatch.model.EmotionalExpression
import com.ideiassertiva.FypMatch.model.EnneagramResult
import com.ideiassertiva.FypMatch.model.FeedbackTolerance
import com.ideiassertiva.FypMatch.model.FinancialApproach
import com.ideiassertiva.FypMatch.model.IPIP20Result
import com.ideiassertiva.FypMatch.model.LifeProjectResult
import com.ideiassertiva.FypMatch.model.LocationFlexibility
import com.ideiassertiva.FypMatch.model.LoveLanguageResult
import com.ideiassertiva.FypMatch.model.PVQ21Result
import com.ideiassertiva.FypMatch.model.RepairBehavior
import com.ideiassertiva.FypMatch.model.SelfKnowledgeQuestionnaire
import com.ideiassertiva.FypMatch.model.SilencePeriod
import com.ideiassertiva.FypMatch.model.SpiritualityRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// MODELOS DE QUESTIONÁRIO (Modo Rápido — legado do QuestionnaireRepository)
// ─────────────────────────────────────────────────────────────────────────────

data class UserQuestionnaire(
    val bigFive: BigFiveResult? = null,
    val values: ValuesResult? = null,
    val communication: CommunicationResult? = null,
    val routine: RoutineResult? = null,
    val dealBreakers: Set<String> = emptySet(),
    val completedAt: Date? = null
)

data class BigFiveResult(
    val extraversion: Float,
    val agreeableness: Float,
    val conscientiousness: Float,
    val neuroticism: Float,
    val openness: Float,
    val version: String = "TIPI-10"
)

/** Top-3 valores em ordem de prioridade */
data class ValuesResult(val topValues: List<String>)

data class CommunicationResult(
    val conflictStyle: String,    // "onTheSpot" | "waitCool" | "withdraw"
    val messagingFreq: String,    // "frequent" | "contextual" | "spacious"
    val absenceReaction: String,  // "normal" | "slightlyAnxious" | "veryAnxious"
    val convDepth: String,        // "direct" | "deep" | "mixed"
    val conflictMedium: String    // "verbal" | "written" | "deferred"
)

data class RoutineResult(
    val weekendStyle: String,     // "homebody" | "balanced" | "explorer"
    val planningStyle: String,    // "routine" | "spontaneous" | "mixed"
    val energySource: String,     // "introvert" | "extrovert"
    val workLifeBalance: String,  // "ambitious" | "balanced" | "leisureFirst"
    val homeNoise: String         // "quiet" | "someNoise" | "anyEnv"
)

data class QuestionnaireCompatibility(
    val overall: Int,                  // 0-100
    val dealBreakerConflict: Boolean,
    val layer1Score: Int,              // valores + comunicação
    val layer2Score: Int,              // BigFive + rotina
    val explanation: String,           // texto pt-BR
    val highlights: List<String>,
    val differences: List<String>
)

// ─────────────────────────────────────────────────────────────────────────────
// REPOSITORY
// ─────────────────────────────────────────────────────────────────────────────

class QuestionnaireRepository @Inject constructor() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collection = "questionnaires"

    // ── Salvar ────────────────────────────────────────────────────────────────
    suspend fun saveQuestionnaire(userId: String, q: UserQuestionnaire): Result<Unit> {
        return try {
            val data = mapOf(
                "bigFive" to q.bigFive?.let {
                    mapOf(
                        "extraversion" to it.extraversion,
                        "agreeableness" to it.agreeableness,
                        "conscientiousness" to it.conscientiousness,
                        "neuroticism" to it.neuroticism,
                        "openness" to it.openness,
                        "version" to it.version
                    )
                },
                "values" to q.values?.let { mapOf("topValues" to it.topValues) },
                "communication" to q.communication?.let {
                    mapOf(
                        "conflictStyle" to it.conflictStyle,
                        "messagingFreq" to it.messagingFreq,
                        "absenceReaction" to it.absenceReaction,
                        "convDepth" to it.convDepth,
                        "conflictMedium" to it.conflictMedium
                    )
                },
                "routine" to q.routine?.let {
                    mapOf(
                        "weekendStyle" to it.weekendStyle,
                        "planningStyle" to it.planningStyle,
                        "energySource" to it.energySource,
                        "workLifeBalance" to it.workLifeBalance,
                        "homeNoise" to it.homeNoise
                    )
                },
                "dealBreakers" to q.dealBreakers.toList(),
                "completedAt" to (q.completedAt ?: Date())
            )
            db.collection(collection).document(userId).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Carregar ──────────────────────────────────────────────────────────────
    @Suppress("UNCHECKED_CAST")
    suspend fun loadQuestionnaire(userId: String): Result<UserQuestionnaire?> {
        return try {
            val doc = db.collection(collection).document(userId).get().await()
            if (!doc.exists()) return Result.success(null)

            val bigFiveMap = doc.get("bigFive") as? Map<String, Any>
            val bigFive = bigFiveMap?.let {
                BigFiveResult(
                    extraversion = (it["extraversion"] as? Number)?.toFloat() ?: 4f,
                    agreeableness = (it["agreeableness"] as? Number)?.toFloat() ?: 4f,
                    conscientiousness = (it["conscientiousness"] as? Number)?.toFloat() ?: 4f,
                    neuroticism = (it["neuroticism"] as? Number)?.toFloat() ?: 4f,
                    openness = (it["openness"] as? Number)?.toFloat() ?: 4f,
                    version = it["version"] as? String ?: "TIPI-10"
                )
            }

            val valuesMap = doc.get("values") as? Map<String, Any>
            val values = valuesMap?.let {
                ValuesResult(topValues = (it["topValues"] as? List<String>) ?: emptyList())
            }

            val commMap = doc.get("communication") as? Map<String, Any>
            val comm = commMap?.let {
                CommunicationResult(
                    conflictStyle = it["conflictStyle"] as? String ?: "",
                    messagingFreq = it["messagingFreq"] as? String ?: "",
                    absenceReaction = it["absenceReaction"] as? String ?: "",
                    convDepth = it["convDepth"] as? String ?: "",
                    conflictMedium = it["conflictMedium"] as? String ?: ""
                )
            }

            val routMap = doc.get("routine") as? Map<String, Any>
            val rout = routMap?.let {
                RoutineResult(
                    weekendStyle = it["weekendStyle"] as? String ?: "",
                    planningStyle = it["planningStyle"] as? String ?: "",
                    energySource = it["energySource"] as? String ?: "",
                    workLifeBalance = it["workLifeBalance"] as? String ?: "",
                    homeNoise = it["homeNoise"] as? String ?: ""
                )
            }

            val dealBreakers = ((doc.get("dealBreakers") as? List<String>) ?: emptyList()).toSet()

            Result.success(
                UserQuestionnaire(
                    bigFive = bigFive,
                    values = values,
                    communication = comm,
                    routine = rout,
                    dealBreakers = dealBreakers,
                    completedAt = doc.getDate("completedAt")
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Algoritmo de compatibilidade (Modo Rápido) ────────────────────────────
    fun calculateCompatibility(
        a: UserQuestionnaire,
        b: UserQuestionnaire
    ): QuestionnaireCompatibility {

        // 1. Deal-breaker check (veto imediato)
        val aBreakers = a.dealBreakers.map { it.lowercase() }
        val bBreakers = b.dealBreakers.map { it.lowercase() }
        val dealBreakerConflict = aBreakers.isNotEmpty() && bBreakers.isNotEmpty() &&
            (aBreakers.any { ab -> bBreakers.any { bb -> ab.contains(bb) || bb.contains(ab) } })

        if (dealBreakerConflict) {
            return QuestionnaireCompatibility(
                overall = 0,
                dealBreakerConflict = true,
                layer1Score = 0,
                layer2Score = 0,
                explanation = "Há um conflito de deal-breakers entre vocês. Infelizmente a compatibilidade é incompatível neste ponto fundamental.",
                highlights = emptyList(),
                differences = listOf("Conflito em preferências fundamentais")
            )
        }

        // 2. Camada 1 — Valores (25%) + Comunicação (10%)
        val valoresScore = calcValoresScore(a.values, b.values)
        val commScore = calcCommScore(a.communication, b.communication)
        val layer1Raw = (valoresScore * 0.25f + commScore * 0.10f) / 0.35f  // normalizado à camada
        val layer1Score = (layer1Raw).toInt().coerceIn(0, 100)

        // 3. Camada 2 — BigFive (20%) + Rotina (15%)
        val bigFiveScore = calcBigFiveScore(a.bigFive, b.bigFive)
        val routineScore = calcRoutineScore(a.routine, b.routine)
        val layer2Raw = (bigFiveScore * 0.20f + routineScore * 0.15f) / 0.35f  // normalizado
        val layer2Score = (layer2Raw).toInt().coerceIn(0, 100)

        // 4. Score total (camada1 raw * 0.5 + camada2 raw * 0.35 + 15 placeholder)
        val totalRaw = (valoresScore * 0.25f + commScore * 0.10f) * 0.5f / 0.35f +
                       (bigFiveScore * 0.20f + routineScore * 0.15f) * 0.35f / 0.35f + 15f
        val overall = totalRaw.toInt().coerceIn(0, 100)

        // 5. Pontos em comum e diferenças
        val highlights = mutableListOf<String>()
        val differences = mutableListOf<String>()

        if (valoresScore >= 60) highlights.add("Valores em comum")
        else differences.add("Valores diferentes")

        if (commScore >= 70) highlights.add("Estilos de comunicação compatíveis")
        else if (commScore < 45) differences.add("Estilos de comunicação muito distintos")

        if (bigFiveScore >= 65) highlights.add("Personalidades que se complementam")
        else if (bigFiveScore < 40) differences.add("Perfis de personalidade opostos")

        if (routineScore >= 70) highlights.add("Rotinas e ritmos parecidos")
        else if (routineScore < 45) differences.add("Estilos de vida com ritmos distintos")

        // 6. Explicação em pt-BR
        val explanation = buildExplanation(overall, layer1Score, layer2Score, highlights, differences)

        return QuestionnaireCompatibility(
            overall = overall,
            dealBreakerConflict = false,
            layer1Score = layer1Score,
            layer2Score = layer2Score,
            explanation = explanation,
            highlights = highlights,
            differences = differences
        )
    }

    // ── Helpers de cálculo ────────────────────────────────────────────────────

    /** Jaccard dos top-3 valores → 0..100 */
    private fun calcValoresScore(a: ValuesResult?, b: ValuesResult?): Float {
        if (a == null || b == null) return 50f
        val setA = a.topValues.toSet()
        val setB = b.topValues.toSet()
        val union = setA.union(setB).size
        if (union == 0) return 50f
        val intersection = setA.intersect(setB).size
        return (intersection.toFloat() / union.toFloat()) * 100f
    }

    /** Comunicação: mesmo=100, compatível=70, oposto=30 → média → 0..100 */
    private fun calcCommScore(a: CommunicationResult?, b: CommunicationResult?): Float {
        if (a == null || b == null) return 50f
        val pairs = listOf(
            Pair(a.conflictStyle, b.conflictStyle),
            Pair(a.messagingFreq, b.messagingFreq),
            Pair(a.absenceReaction, b.absenceReaction),
            Pair(a.convDepth, b.convDepth),
            Pair(a.conflictMedium, b.conflictMedium)
        )
        val scores = pairs.map { (x, y) ->
            when {
                x == y -> 100f
                isCommCompat(x, y) -> 70f
                else -> 30f
            }
        }
        return scores.average().toFloat()
    }

    private fun isCommCompat(x: String, y: String): Boolean {
        val compatGroups = listOf(
            setOf("onTheSpot", "waitCool"),
            setOf("frequent", "contextual"),
            setOf("contextual", "spacious"),
            setOf("normal", "slightlyAnxious"),
            setOf("direct", "mixed"),
            setOf("deep", "mixed"),
            setOf("verbal", "deferred")
        )
        return compatGroups.any { it.contains(x) && it.contains(y) }
    }

    /** BigFive: distância normalizada por dimensão, invertida → 0..100 */
    private fun calcBigFiveScore(a: BigFiveResult?, b: BigFiveResult?): Float {
        if (a == null || b == null) return 50f
        val maxDiff = 6f  // escala 1-7, max diff = 6
        val diffs = listOf(
            kotlin.math.abs(a.extraversion - b.extraversion),
            kotlin.math.abs(a.agreeableness - b.agreeableness),
            kotlin.math.abs(a.conscientiousness - b.conscientiousness),
            kotlin.math.abs(a.neuroticism - b.neuroticism),
            kotlin.math.abs(a.openness - b.openness)
        )
        val avgDiff = diffs.average().toFloat()
        return ((1f - avgDiff / maxDiff) * 100f).coerceIn(0f, 100f)
    }

    /** Rotina: regras específicas por campo → média → 0..100 */
    private fun calcRoutineScore(a: RoutineResult?, b: RoutineResult?): Float {
        if (a == null || b == null) return 50f

        // energySource
        val energyScore = when {
            a.energySource == b.energySource && a.energySource == "introvert" -> 100f
            a.energySource == b.energySource && a.energySource == "extrovert" -> 90f
            a.energySource != b.energySource -> 50f
            else -> 70f
        }

        // weekendStyle: mesmo=100, 1 nível=70, oposto=40
        val weekendOrder = listOf("homebody", "balanced", "explorer")
        val idxA = weekendOrder.indexOf(a.weekendStyle)
        val idxB = weekendOrder.indexOf(b.weekendStyle)
        val weekendScore = when {
            idxA == -1 || idxB == -1 -> 50f
            idxA == idxB -> 100f
            kotlin.math.abs(idxA - idxB) == 1 -> 70f
            else -> 40f
        }

        // planningStyle
        val planScore = when {
            a.planningStyle == b.planningStyle -> 100f
            a.planningStyle == "mixed" || b.planningStyle == "mixed" -> 70f
            else -> 40f
        }

        // workLifeBalance
        val wlbScore = when {
            a.workLifeBalance == b.workLifeBalance -> 100f
            setOf(a.workLifeBalance, b.workLifeBalance) == setOf("balanced", "ambitious") -> 65f
            setOf(a.workLifeBalance, b.workLifeBalance) == setOf("balanced", "leisureFirst") -> 65f
            else -> 35f
        }

        // homeNoise
        val noiseScore = when {
            a.homeNoise == b.homeNoise -> 100f
            a.homeNoise == "anyEnv" || b.homeNoise == "anyEnv" -> 80f
            else -> 40f
        }

        return ((energyScore + weekendScore + planScore + wlbScore + noiseScore) / 5f)
    }

    private fun buildExplanation(
        overall: Int,
        layer1: Int,
        layer2: Int,
        highlights: List<String>,
        differences: List<String>
    ): String {
        val intro = when {
            overall >= 80 -> "Vocês têm uma compatibilidade muito alta!"
            overall >= 65 -> "Vocês têm uma boa compatibilidade."
            overall >= 50 -> "Há compatibilidade moderada entre vocês."
            overall >= 35 -> "A compatibilidade é baixa, mas diferenças podem se complementar."
            else -> "A compatibilidade é limitada neste momento."
        }
        val camadaTexto = when {
            layer1 >= layer2 + 20 -> " Os valores e a comunicação são os pontos mais fortes."
            layer2 >= layer1 + 20 -> " A personalidade e a rotina de vocês se alinham bem."
            else -> " Há equilíbrio entre valores, comunicação e estilo de vida."
        }
        val destaqueTexto = if (highlights.isNotEmpty()) {
            " Em especial: ${highlights.take(2).joinToString(" e ")}."
        } else ""
        return "$intro$camadaTexto$destaqueTexto"
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MODO PROFUNDO — Persistência
    // ─────────────────────────────────────────────────────────────────────────

    private val deepCollection = "deepQuestionnaires"

    /**
     * Salva o DeepModeQuestionnaire no Firestore.
     * Enums são serializados como String (.name) para compatibilidade futura.
     */
    suspend fun saveDeepMode(q: DeepModeQuestionnaire) {
        val conflictMap = q.conflictDeep?.let {
            mapOf(
                "resolutionStyle"     to it.resolutionStyle.name,
                "emotionalExpression" to it.emotionalExpression.name,
                "repairBehavior"      to it.repairBehavior.name,
                "silencePeriod"       to it.silencePeriod.name,
                "apologyStyle"        to it.apologyStyle.name,
                "feedbackTolerance"   to it.feedbackTolerance.name
            )
        }
        val lifeMap = q.lifeProject?.let {
            mapOf(
                "childrenDesire"      to it.childrenDesire.name,
                "locationFlexibility" to it.locationFlexibility.name,
                "careerPriority"      to it.careerPriority.name,
                "financialApproach"   to it.financialApproach.name,
                "spiritualityRole"    to it.spiritualityRole.name
            )
        }
        val data = mapOf(
            "userId"       to q.userId,
            "completedAt"  to (q.completedAt ?: Date()),
            "ipip20"       to q.ipip20?.responses,
            "pvq21"        to q.pvq21?.responses,
            "ecrrs"        to q.ecrrs?.responses,
            "conflictDeep" to conflictMap,
            "lifeProject"  to lifeMap
        )
        db.collection(deepCollection).document(q.userId).set(data).await()
    }

    /**
     * Carrega o DeepModeQuestionnaire do Firestore para um usuário.
     * Retorna null se o documento não existir.
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun loadDeepMode(userId: String): DeepModeQuestionnaire? {
        val doc = db.collection(deepCollection).document(userId).get().await()
        if (!doc.exists()) return null

        val ipip20 = (doc.get("ipip20") as? List<Long>)
            ?.map { it.toInt() }
            ?.let { IPIP20Result(it) }

        val pvq21 = (doc.get("pvq21") as? List<Long>)
            ?.map { it.toInt() }
            ?.let { PVQ21Result(it) }

        val ecrrs = (doc.get("ecrrs") as? List<Long>)
            ?.map { it.toInt() }
            ?.let { ECRRSResult(it) }

        val conflictMap = doc.get("conflictDeep") as? Map<String, String>
        val conflict = conflictMap?.let {
            runCatching {
                DeepConflictResult(
                    resolutionStyle     = ConflictResolutionStyle.valueOf(it["resolutionStyle"] ?: "COOLING_OFF"),
                    emotionalExpression = EmotionalExpression.valueOf(it["emotionalExpression"] ?: "MODERATE"),
                    repairBehavior      = RepairBehavior.valueOf(it["repairBehavior"] ?: "GRADUAL"),
                    silencePeriod       = SilencePeriod.valueOf(it["silencePeriod"] ?: "HOURS"),
                    apologyStyle        = ApologyStyle.valueOf(it["apologyStyle"] ?: "BOTH"),
                    feedbackTolerance   = FeedbackTolerance.valueOf(it["feedbackTolerance"] ?: "CONTEXTUAL")
                )
            }.getOrNull()
        }

        val lifeMap = doc.get("lifeProject") as? Map<String, String>
        val life = lifeMap?.let {
            runCatching {
                LifeProjectResult(
                    childrenDesire      = ChildrenDesire.valueOf(it["childrenDesire"] ?: "UNDECIDED"),
                    locationFlexibility = LocationFlexibility.valueOf(it["locationFlexibility"] ?: "OPEN_SAME_CITY"),
                    careerPriority      = CareerPriority.valueOf(it["careerPriority"] ?: "BALANCED"),
                    financialApproach   = FinancialApproach.valueOf(it["financialApproach"] ?: "BALANCED"),
                    spiritualityRole    = SpiritualityRole.valueOf(it["spiritualityRole"] ?: "PERSONAL")
                )
            }.getOrNull()
        }

        return DeepModeQuestionnaire(
            userId       = userId,
            completedAt  = doc.getDate("completedAt"),
            ipip20       = ipip20,
            pvq21        = pvq21,
            ecrrs        = ecrrs,
            conflictDeep = conflict,
            lifeProject  = life
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // COMPATIBILIDADE ENRIQUECIDA COM MODO PROFUNDO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calcula compatibilidade combinando Modo Rápido + Modo Profundo (quando disponível).
     *
     * Se ambos têm deepA?.ecrrs e deepB?.ecrrs: usar AttachmentStyle na Camada 1 (15%).
     *   Tabela: SECURE+SECURE=100, SECURE+ANXIOUS=70, SECURE+AVOIDANT=65,
     *           ANXIOUS+ANXIOUS=50, AVOIDANT+AVOIDANT=55, DISORGANIZED+any=40
     *
     * Se ambos têm deepA?.ipip20 e deepB?.ipip20: usar IPIP-20 normalizado na Camada 2
     *   com distância euclidiana ponderada nos 5 fatores (pesos OCEAN padrão).
     *
     * Caso contrário: usar dados do Modo Rápido como fallback.
     */
    fun calculateCompatibilityFull(
        quickA: UserQuestionnaire,
        quickB: UserQuestionnaire,
        deepA: DeepModeQuestionnaire?,
        deepB: DeepModeQuestionnaire?
    ): QuestionnaireCompatibility {

        // 1. Deal-breaker check
        val aBreakers = quickA.dealBreakers.map { it.lowercase() }
        val bBreakers = quickB.dealBreakers.map { it.lowercase() }
        val dealBreakerConflict = aBreakers.isNotEmpty() && bBreakers.isNotEmpty() &&
            aBreakers.any { ab -> bBreakers.any { bb -> ab.contains(bb) || bb.contains(ab) } }

        if (dealBreakerConflict) {
            return QuestionnaireCompatibility(
                overall = 0,
                dealBreakerConflict = true,
                layer1Score = 0,
                layer2Score = 0,
                explanation = "Há um conflito de deal-breakers entre vocês. A compatibilidade é incompatível neste ponto fundamental.",
                highlights = emptyList(),
                differences = listOf("Conflito em preferências fundamentais")
            )
        }

        val highlights = mutableListOf<String>()
        val differences = mutableListOf<String>()

        // 2. Camada 1 — ECR-RS (Deep) ou fallback valores + comunicação
        val hasDeepECRRS = deepA?.ecrrs != null && deepB?.ecrrs != null
        val layer1Score: Int

        if (hasDeepECRRS) {
            val styleA = deepA!!.ecrrs!!.attachmentStyle()
            val styleB = deepB!!.ecrrs!!.attachmentStyle()
            layer1Score = calcAttachmentCompatibility(styleA, styleB)
            if (layer1Score >= 80)
                highlights.add("Estilos de apego muito compatíveis (${styleA.displayName} + ${styleB.displayName})")
            else if (layer1Score < 50)
                differences.add("Estilos de apego distintos (${styleA.displayName} + ${styleB.displayName})")
        } else {
            val valoresScore = calcValoresScore(quickA.values, quickB.values)
            val commScore = calcCommScore(quickA.communication, quickB.communication)
            val raw = (valoresScore * 0.25f + commScore * 0.10f) / 0.35f
            layer1Score = raw.toInt().coerceIn(0, 100)
            if (valoresScore >= 60) highlights.add("Valores em comum") else differences.add("Valores diferentes")
            if (commScore >= 70) highlights.add("Estilos de comunicação compatíveis")
            else if (commScore < 45) differences.add("Estilos de comunicação muito distintos")
        }

        // 3. Camada 2 — IPIP-20 normalizado (Deep) ou BigFive local + rotina
        val hasDeepIPIP20 = deepA?.ipip20 != null && deepB?.ipip20 != null
        val bigFiveScore: Float = if (hasDeepIPIP20) {
            // IPIP-20 → BigFiveResult com valores 0–100 e pesos OCEAN
            val normA = deepA!!.ipip20!!.normalized()
            val normB = deepB!!.ipip20!!.normalized()
            // Pesos: E=1.0, A=1.1, C=1.2, N=0.9, O=1.0
            val weights = listOf(1.0, 1.1, 1.2, 0.9, 1.0)
            val diffs = listOf(
                kotlin.math.abs(normA.extraversion      - normB.extraversion),
                kotlin.math.abs(normA.agreeableness     - normB.agreeableness),
                kotlin.math.abs(normA.conscientiousness - normB.conscientiousness),
                kotlin.math.abs(normA.neuroticism       - normB.neuroticism),
                kotlin.math.abs(normA.openness          - normB.openness)
            )
            val wAvg = diffs.zip(weights).sumOf { (d, w) -> d * w } / weights.sum()
            ((1.0 - wAvg / 100.0) * 100.0).toFloat().coerceIn(0f, 100f)
        } else {
            calcBigFiveScore(quickA.bigFive, quickB.bigFive)
        }

        val routineScore = calcRoutineScore(quickA.routine, quickB.routine)
        val layer2Raw = (bigFiveScore * 0.20f + routineScore * 0.15f) / 0.35f
        val layer2Score = layer2Raw.toInt().coerceIn(0, 100)

        if (bigFiveScore >= 65f) highlights.add("Personalidades que se complementam")
        else if (bigFiveScore < 40f) differences.add("Perfis de personalidade opostos")
        if (routineScore >= 70f) highlights.add("Rotinas e ritmos parecidos")
        else if (routineScore < 45f) differences.add("Estilos de vida com ritmos distintos")

        // 4. Score total
        val deepBonus = if (hasDeepECRRS || hasDeepIPIP20) 8f else 0f
        val overall = ((layer1Score.toFloat() * 0.50f + layer2Score.toFloat() * 0.50f) + deepBonus)
            .toInt().coerceIn(0, 100)

        val explanation = buildExplanationFull(
            overall, layer1Score, layer2Score, highlights, differences, hasDeepECRRS, hasDeepIPIP20
        )

        return QuestionnaireCompatibility(
            overall = overall,
            dealBreakerConflict = false,
            layer1Score = layer1Score,
            layer2Score = layer2Score,
            explanation = explanation,
            highlights = highlights,
            differences = differences
        )
    }

    // ── Helpers Deep Mode ─────────────────────────────────────────────────────

    /**
     * Tabela de compatibilidade de estilos de apego:
     * SECURE+SECURE=100, SECURE+ANXIOUS=70, SECURE+AVOIDANT=65,
     * ANXIOUS+ANXIOUS=50, AVOIDANT+AVOIDANT=55, DISORGANIZED+any=40
     */
    private fun calcAttachmentCompatibility(a: AttachmentStyle, b: AttachmentStyle): Int {
        return when {
            a == b && a == AttachmentStyle.SECURE    -> 100
            a == b && a == AttachmentStyle.AVOIDANT  -> 55
            a == b && a == AttachmentStyle.ANXIOUS   -> 50
            a == AttachmentStyle.DISORGANIZED || b == AttachmentStyle.DISORGANIZED -> 40
            (a == AttachmentStyle.SECURE && b == AttachmentStyle.ANXIOUS) ||
            (b == AttachmentStyle.SECURE && a == AttachmentStyle.ANXIOUS) -> 70
            (a == AttachmentStyle.SECURE && b == AttachmentStyle.AVOIDANT) ||
            (b == AttachmentStyle.SECURE && a == AttachmentStyle.AVOIDANT) -> 65
            else -> 45
        }
    }

    private fun buildExplanationFull(
        overall: Int,
        layer1: Int,
        layer2: Int,
        highlights: List<String>,
        differences: List<String>,
        hasECRRS: Boolean,
        hasIPIP20: Boolean
    ): String {
        val intro = when {
            overall >= 80 -> "Vocês têm uma compatibilidade muito alta!"
            overall >= 65 -> "Vocês têm uma boa compatibilidade."
            overall >= 50 -> "Há compatibilidade moderada entre vocês."
            overall >= 35 -> "A compatibilidade é baixa, mas as diferenças podem se complementar."
            else -> "A compatibilidade é limitada neste momento."
        }
        val deepNote = when {
            hasECRRS && hasIPIP20 -> " Análise enriquecida com perfil psicológico completo (IPIP-20 + ECR-RS)."
            hasECRRS  -> " Inclui análise de estilos de apego (ECR-RS)."
            hasIPIP20 -> " Inclui perfil de personalidade aprofundado (IPIP-20)."
            else -> ""
        }
        val destaqueTexto = if (highlights.isNotEmpty())
            " Em especial: ${highlights.take(2).joinToString(" e ")}."
        else ""
        return "$intro$deepNote$destaqueTexto"
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MODO AUTOCONHECIMENTO — Eneagrama (Sprint 7a)
    // ─────────────────────────────────────────────────────────────────────────

    private val selfKnowledgeCollection = "selfKnowledge"

    /**
     * Salva SelfKnowledgeQuestionnaire no Firestore.
     * Usa merge = true para preservar campos existentes (loveLanguage, archetype, etc.).
     */
    suspend fun saveSelfKnowledge(q: SelfKnowledgeQuestionnaire) {
        withContext(Dispatchers.IO) {
            val data = hashMapOf<String, Any>(
                "userId" to q.userId,
                "completedAt" to (q.completedAt ?: System.currentTimeMillis())
            )
            q.enneagram?.let { enn ->
                data["enneagram_responses"] = enn.responses
                data["enneagram_dominant"] = enn.dominantType().name
                data["enneagram_top3"] = enn.topThree().map { it.name }
            }
            q.loveLanguage?.let { ll ->
                data["loveLanguage_responses"] = ll.responses
                data["loveLanguage_primary"] = ll.primaryLanguage().name
                data["loveLanguage_secondary"] = ll.secondaryLanguage().name
            }
            q.archetype?.let { arc ->
                data["archetype_responses"] = arc.responses
                data["archetype_dominant"] = arc.dominantArchetype().name
                data["archetype_top3"] = arc.topThree().map { it.name }
            }
            db.collection(selfKnowledgeCollection)
                .document(q.userId)
                .set(data, SetOptions.merge())
                .await()
        }
    }

    /**
     * Carrega SelfKnowledgeQuestionnaire do Firestore.
     * Retorna null se o documento não existir.
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun loadSelfKnowledge(userId: String): SelfKnowledgeQuestionnaire? {
        return withContext(Dispatchers.IO) {
            val doc = db.collection(selfKnowledgeCollection).document(userId).get().await()
            if (!doc.exists()) return@withContext null

            val ennResponses = (doc.get("enneagram_responses") as? List<*>)
                ?.map { it as? Boolean ?: false }
            val enneagram = if (ennResponses?.size == 27) EnneagramResult(ennResponses) else null

            val llResponses = (doc.get("loveLanguage_responses") as? List<*>)
                ?.map { it as? Boolean ?: false }
            val loveLanguage = if (llResponses?.size == 30) LoveLanguageResult(llResponses) else null

            val arcResponses = (doc.get("archetype_responses") as? List<*>)
                ?.map { it as? Boolean ?: false }
            val archetype = if (arcResponses?.size == 24) ArchetypeResult(arcResponses) else null

            SelfKnowledgeQuestionnaire(
                userId = userId,
                completedAt = doc.getLong("completedAt"),
                enneagram = enneagram,
                loveLanguage = loveLanguage,
                archetype = archetype
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CAMADA 3 — COMPATIBILIDADE DO MODO AUTOCONHECIMENTO (Sprint 7b)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calcula a compatibilidade com base nos dados do Modo Autoconhecimento.
     *
     * Pesos:
     *   Eneagrama:           20% (naturalPartners=100, mesmo=70, outros=50)
     *   Linguagem do Cuidado: 40% (LoveLanguageResult.compatibility())
     *   Arquétipo:            40% (mesmo dominante=100, diferentes=60)
     *
     * Se algum dos lados não tiver o dado, usa 60 como base neutra.
     */
    fun calculateSelfKnowledgeCompatibility(
        qA: SelfKnowledgeQuestionnaire?,
        qB: SelfKnowledgeQuestionnaire?
    ): Int {
        if (qA == null || qB == null) return 60

        val ennScore = if (qA.enneagram != null && qB.enneagram != null) {
            val tA = qA.enneagram.dominantType()
            val tB = qB.enneagram.dominantType()
            when {
                tA.naturalPartners.contains(tB) || tB.naturalPartners.contains(tA) -> 100
                tA == tB -> 70
                else -> 50
            }
        } else 60

        val langScore = if (qA.loveLanguage != null && qB.loveLanguage != null)
            qA.loveLanguage.compatibility(qB.loveLanguage) else 60

        val archetypeScore = if (qA.archetype != null && qB.archetype != null)
            if (qA.archetype.dominantArchetype() == qB.archetype.dominantArchetype()) 100 else 60
        else 60

        return (ennScore * 0.20 + langScore * 0.40 + archetypeScore * 0.40).toInt()
    }
}
