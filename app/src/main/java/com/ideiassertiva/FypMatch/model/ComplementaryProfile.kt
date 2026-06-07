package com.ideiassertiva.FypMatch.model

/**
 * Perfil Complementar de Compatibilidade.
 *
 * Camada extra de inteligência gerada por uma IA externa do próprio usuário
 * (aquela com quem ele conversa com frequência). NÃO substitui os questionários
 * do app — apenas complementa o perfil para melhorar a compatibilidade e dar
 * contexto à Conselheira IA.
 *
 * Espelha o bloco JSON `fypmatch_complementary_profile` + os pesos sugeridos.
 * Todos os campos têm default para serialização Firestore (POJO).
 */
data class ComplementaryProfile(
    val summary: String = "",
    val generalConfidence: String = "",            // alta | média | baixa
    val observedTraits: List<TraitItem> = emptyList(),
    val inferredTraits: List<TraitItem> = emptyList(),
    val relationshipStyle: RelationshipStyle = RelationshipStyle(),
    val coreValues: List<String> = emptyList(),
    val importantValues: List<String> = emptyList(),
    val desiredValues: List<String> = emptyList(),
    val lifestyleTags: List<String> = emptyList(),
    val culturalPreferences: List<String> = emptyList(),
    val hobbiesAndInterests: List<String> = emptyList(),
    val preferredPartnerTraits: List<String> = emptyList(),
    val incompatiblePartnerTraits: List<String> = emptyList(),
    val greenFlags: List<String> = emptyList(),
    val yellowFlags: List<String> = emptyList(),
    val redFlags: List<String> = emptyList(),
    val idealPartnerSummary: String = "",
    val incompatiblePartnerSummary: String = "",
    val algorithmicTags: List<String> = emptyList(),
    val missingOrUncertainData: List<String> = emptyList(),
    val sensitiveDataExcluded: List<String> = emptyList(),
    val weights: CompatibilityWeights = CompatibilityWeights(),
    val rawText: String = "",                       // texto bruto colado (auditoria/reprocessamento)
    val updatedAt: Long = 0L                         // epoch millis
)

data class TraitItem(
    val trait: String = "",
    val confidence: String = ""
)

data class RelationshipStyle(
    val affectionExpression: String = "",
    val affectionPreference: String = "",
    val communicationNeed: String = "",
    val emotionalSecurityNeed: String = "",
    val personalSpaceNeed: String = "",
    val conflictStyle: String = "",
    val reciprocitySensitivity: String = "",
    val ambiguityTolerance: String = ""
)

/** Pesos sugeridos pela IA (0–100, soma esperada = 100). */
data class CompatibilityWeights(
    val relationshipGoal: Int = 0,
    val values: Int = 0,
    val affectionStyle: Int = 0,
    val communicationStyle: Int = 0,
    val emotionalMaturity: Int = 0,
    val lifestyle: Int = 0,
    val personality: Int = 0,
    val hobbiesAndInterests: Int = 0,
    val culturalPreferences: Int = 0,
    val futurePlans: Int = 0
)

// ── Helpers (extension functions → não são serializados pelo Firestore) ──────

/** Indica se há um perfil complementar realmente preenchido. */
fun ComplementaryProfile.isPresent(): Boolean =
    summary.isNotBlank() || algorithmicTags.isNotEmpty() || coreValues.isNotEmpty()

/** Soma dos pesos (idealmente 100). */
fun CompatibilityWeights.total(): Int =
    relationshipGoal + values + affectionStyle + communicationStyle + emotionalMaturity +
        lifestyle + personality + hobbiesAndInterests + culturalPreferences + futurePlans

/** Resumo curto e seguro (sem dados sensíveis) para dar contexto à Conselheira IA. */
fun ComplementaryProfile.toCounselorContext(): String {
    if (!isPresent()) return ""
    val sb = StringBuilder()
    sb.appendLine("== Perfil Complementar do usuário (gerado pela IA pessoal dele) ==")
    if (summary.isNotBlank()) sb.appendLine("Resumo: $summary")
    if (coreValues.isNotEmpty()) sb.appendLine("Valores centrais: ${coreValues.joinToString(", ")}")
    if (importantValues.isNotEmpty()) sb.appendLine("Valores importantes: ${importantValues.joinToString(", ")}")
    with(relationshipStyle) {
        if (affectionExpression.isNotBlank()) sb.appendLine("Demonstra carinho por: $affectionExpression")
        if (communicationNeed.isNotBlank()) sb.appendLine("Necessidade de comunicação: $communicationNeed")
        if (conflictStyle.isNotBlank()) sb.appendLine("Estilo em conflitos: $conflictStyle")
    }
    if (greenFlags.isNotEmpty()) sb.appendLine("Sinais verdes (busca): ${greenFlags.joinToString(", ")}")
    if (redFlags.isNotEmpty()) sb.appendLine("Sinais vermelhos (evita): ${redFlags.joinToString(", ")}")
    if (idealPartnerSummary.isNotBlank()) sb.appendLine("Parceiro(a) ideal: $idealPartnerSummary")
    sb.appendLine("(Confiança geral: ${generalConfidence.ifBlank { "n/d" }}. Dados sensíveis foram excluídos pelo próprio usuário.)")
    return sb.toString().trim()
}
