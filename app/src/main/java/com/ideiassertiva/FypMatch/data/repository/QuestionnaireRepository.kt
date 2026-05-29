package com.ideiassertiva.FypMatch.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// MODELOS DE QUESTIONÁRIO
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

    // ── Algoritmo de compatibilidade ──────────────────────────────────────────
    fun calculateCompatibility(
        a: UserQuestionnaire,
        b: UserQuestionnaire
    ): QuestionnaireCompatibility {

        // 1. Deal-breaker check (veto imediato)
        val aBreakers = a.dealBreakers.map { it.lowercase() }
        val bBreakers = b.dealBreakers.map { it.lowercase() }
        // Verifica se algum item declarado pelo A está nos deal-breakers do B ou vice-versa
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
}
