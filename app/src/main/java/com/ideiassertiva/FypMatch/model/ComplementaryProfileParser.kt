package com.ideiassertiva.FypMatch.model

import org.json.JSONArray
import org.json.JSONObject

object ComplementaryProfilePrompt {
    val PERSONAL_AI_PROMPT: String = """
Voce e uma IA que conversa comigo ha bastante tempo ou com frequencia.

Quero que voce gere um Perfil Complementar de Compatibilidade para o FypMatch, usando apenas o que voce ja sabe sobre mim a partir do nosso historico de conversas, padroes de comportamento, preferencias, decisoes, gostos, valores, estilo de vida e forma de me relacionar.

O FypMatch ja possui formularios proprios, questionarios de personalidade e campos objetivos.
Sua funcao aqui nao e me entrevistar e nao e substituir os questionarios do app.

Sua funcao e gerar uma camada extra de inteligencia: dados complementares, inferidos com cuidado, para ajudar o FypMatch a entender melhor que tipo de parceiro(a) tende a ser mais compativel comigo.

Regras obrigatorias:
1. Nao faca perguntas.
2. Nao invente informacoes.
3. Use apenas dados que voce ja sabe ou padroes que consegue inferir com seguranca.
4. Quando nao souber algo, use "nao informado", "sem evidencia suficiente" ou "baixa confianca".
5. Separe claramente fatos observados, preferencias declaradas, padroes inferidos e hipoteses de baixa confianca.
6. Nao inclua informacoes sensiveis demais, intimas, medicas, religiosas, politicas ou familiares, a menos que sejam essenciais para compatibilidade e possam ser descritas de forma generica.
7. Nao faca diagnostico psicologico.
8. Nao rotule a pessoa de forma rigida.
9. Escreva de forma util para um algoritmo de compatibilidade.
10. O objetivo nao e encontrar alguem perfeito, mas aumentar a chance de indicar pessoas com maior compatibilidade afetiva, emocional, pratica e de estilo de vida.

Gere o perfil no formato abaixo. No item 12, inclua obrigatoriamente um JSON valido para o FypMatch importar.

# Perfil Complementar FypMatch

## 1. Resumo geral de compatibilidade
Escreva um resumo curto sobre meu estilo afetivo, meus valores, meu jeito de viver e o tipo de pessoa que provavelmente combina comigo. Inclua nivel de confianca geral: Alta, Media ou Baixa.

## 2. Dados observados
Liste informacoes claramente declaradas por mim em conversas anteriores.

## 3. Preferencias afetivas inferidas
Inclua, quando houver evidencia: como tendo a demonstrar carinho, como gosto de receber carinho, frequencia ideal de contato, necessidade de seguranca emocional, necessidade de liberdade individual, intensidade afetiva, sensibilidade a reciprocidade, tolerancia a ambiguidades, forma provavel de lidar com conflitos, necessidades emocionais e gatilhos de desconforto.

## 4. Valores relevantes para compatibilidade
Separe em valores centrais, importantes e desejaveis, sempre com evidencia e confianca.

## 5. Estilo de vida complementar
Inclua rotina, trabalho/estudo, familia, filhos se aplicavel, vida social, programas preferidos, viagens, alimentacao, hobbies, assuntos de interesse, preferencias culturais, dinheiro, planos futuros e ambiente ideal de convivencia.

## 6. Gostos e preferencias pessoais
Liste musicas/artistas, filmes/series, livros, jogos, hobbies, comidas, lugares, programas de casal e assuntos que geram conexao ou desinteresse. Classifique cada dado como declarado, inferido ou baixa confianca.

## 7. Perfil provavel de parceiro(a) compativel
Descreva personalidade, estilo afetivo, comunicacao, estilo de vida, valores, ritmo de relacionamento, relacao com familia, trabalho, maturidade emocional, conflitos e tipo de presenca ideal.

## 8. Perfil provavelmente incompativel
Separe incompatibilidade forte, incompatibilidade moderada e pontos de atencao.

## 9. Sinais verdes
Liste sinais de boa compatibilidade com motivo e peso.

## 10. Sinais amarelos
Liste sinais que exigem atencao, sem descarte automatico.

## 11. Sinais vermelhos
Liste sinais de alto risco de incompatibilidade com linguagem tecnica e objetiva.

## 12. Campos complementares para o algoritmo
Retorne exatamente este JSON, preenchido quando houver evidencia:
{
  "fypmatch_complementary_profile": {
    "summary": "",
    "general_confidence": "",
    "observed_traits": [],
    "inferred_traits": [],
    "relationship_style": {
      "affection_expression": "",
      "affection_preference": "",
      "communication_need": "",
      "emotional_security_need": "",
      "personal_space_need": "",
      "conflict_style": "",
      "reciprocity_sensitivity": "",
      "ambiguity_tolerance": ""
    },
    "core_values": [],
    "important_values": [],
    "desired_values": [],
    "lifestyle_tags": [],
    "cultural_preferences": [],
    "hobbies_and_interests": [],
    "preferred_partner_traits": [],
    "incompatible_partner_traits": [],
    "green_flags": [],
    "yellow_flags": [],
    "red_flags": [],
    "ideal_partner_summary": "",
    "incompatible_partner_summary": "",
    "algorithmic_tags": [],
    "missing_or_uncertain_data": [],
    "sensitive_data_excluded": []
  }
}

## 13. Pesos sugeridos para compatibilidade
Sugira pesos de 0 a 100 cuja soma seja exatamente 100:
{
  "relationship_goal": 0,
  "values": 0,
  "affection_style": 0,
  "communication_style": 0,
  "emotional_maturity": 0,
  "lifestyle": 0,
  "personality": 0,
  "hobbies_and_interests": 0,
  "cultural_preferences": 0,
  "future_plans": 0
}

## 14. Dados que o FypMatch deve tratar com cuidado
Liste apenas categorias sensiveis que devem ser opcionais, privadas e protegidas.

## 15. Qualidade do perfil
Informe nivel geral de confianca, principais dados conhecidos, inferidos, lacunas, campos do FypMatch que este perfil complementa e informacoes que nao devem ser usadas automaticamente.

Se voce nao tiver historico suficiente sobre mim, responda:
"Nao tenho historico suficiente para gerar um perfil complementar confiavel. Posso apenas criar um perfil generico, mas ele nao deve ser usado pelo FypMatch como dado de compatibilidade individual."
""".trimIndent()
}

object ComplementaryProfileParser {
    private const val ROOT_KEY = "fypmatch_complementary_profile"

    fun parse(rawText: String, nowMillis: Long = System.currentTimeMillis()): ComplementaryProfile {
        val cleanText = rawText.trim()
        require(cleanText.isNotBlank()) { "Cole a resposta gerada pela sua IA antes de salvar." }

        val root = extractJsonObjects(cleanText)
            .mapNotNull { candidate -> runCatching { JSONObject(candidate) }.getOrNull() }
            .firstOrNull { it.has(ROOT_KEY) || it.has("summary") }

        if (root == null) {
            return fallbackProfile(cleanText, nowMillis)
        }

        val profileJson = root.optJSONObject(ROOT_KEY) ?: root
        val weightsJson = root.optJSONObject("weights") ?: extractWeightsObject(cleanText)

        return ComplementaryProfile(
            summary = profileJson.optStringAny("summary"),
            generalConfidence = profileJson.optStringAny("general_confidence", "generalConfidence"),
            observedTraits = profileJson.traitList("observed_traits", "observedTraits"),
            inferredTraits = profileJson.traitList("inferred_traits", "inferredTraits"),
            relationshipStyle = profileJson.relationshipStyle(),
            coreValues = profileJson.stringList("core_values", "coreValues"),
            importantValues = profileJson.stringList("important_values", "importantValues"),
            desiredValues = profileJson.stringList("desired_values", "desiredValues"),
            lifestyleTags = profileJson.stringList("lifestyle_tags", "lifestyleTags"),
            culturalPreferences = profileJson.stringList("cultural_preferences", "culturalPreferences"),
            hobbiesAndInterests = profileJson.stringList("hobbies_and_interests", "hobbiesAndInterests"),
            preferredPartnerTraits = profileJson.stringList("preferred_partner_traits", "preferredPartnerTraits"),
            incompatiblePartnerTraits = profileJson.stringList("incompatible_partner_traits", "incompatiblePartnerTraits"),
            greenFlags = profileJson.stringList("green_flags", "greenFlags"),
            yellowFlags = profileJson.stringList("yellow_flags", "yellowFlags"),
            redFlags = profileJson.stringList("red_flags", "redFlags"),
            idealPartnerSummary = profileJson.optStringAny("ideal_partner_summary", "idealPartnerSummary"),
            incompatiblePartnerSummary = profileJson.optStringAny(
                "incompatible_partner_summary",
                "incompatiblePartnerSummary"
            ),
            algorithmicTags = profileJson.stringList("algorithmic_tags", "algorithmicTags"),
            missingOrUncertainData = profileJson.stringList("missing_or_uncertain_data", "missingOrUncertainData"),
            sensitiveDataExcluded = profileJson.stringList("sensitive_data_excluded", "sensitiveDataExcluded"),
            weights = weightsJson?.weights() ?: CompatibilityWeights(),
            rawText = cleanText,
            updatedAt = nowMillis
        )
    }

    private fun JSONObject.relationshipStyle(): RelationshipStyle {
        val json = optJSONObjectAny("relationship_style", "relationshipStyle") ?: JSONObject()
        return RelationshipStyle(
            affectionExpression = json.optStringAny("affection_expression", "affectionExpression"),
            affectionPreference = json.optStringAny("affection_preference", "affectionPreference"),
            communicationNeed = json.optStringAny("communication_need", "communicationNeed"),
            emotionalSecurityNeed = json.optStringAny("emotional_security_need", "emotionalSecurityNeed"),
            personalSpaceNeed = json.optStringAny("personal_space_need", "personalSpaceNeed"),
            conflictStyle = json.optStringAny("conflict_style", "conflictStyle"),
            reciprocitySensitivity = json.optStringAny("reciprocity_sensitivity", "reciprocitySensitivity"),
            ambiguityTolerance = json.optStringAny("ambiguity_tolerance", "ambiguityTolerance")
        )
    }

    private fun JSONObject.weights(): CompatibilityWeights {
        return CompatibilityWeights(
            relationshipGoal = optIntAny("relationship_goal", "relationshipGoal"),
            values = optIntAny("values"),
            affectionStyle = optIntAny("affection_style", "affectionStyle"),
            communicationStyle = optIntAny("communication_style", "communicationStyle"),
            emotionalMaturity = optIntAny("emotional_maturity", "emotionalMaturity"),
            lifestyle = optIntAny("lifestyle"),
            personality = optIntAny("personality"),
            hobbiesAndInterests = optIntAny("hobbies_and_interests", "hobbiesAndInterests"),
            culturalPreferences = optIntAny("cultural_preferences", "culturalPreferences"),
            futurePlans = optIntAny("future_plans", "futurePlans")
        )
    }

    private fun JSONObject.traitList(vararg keys: String): List<TraitItem> {
        val array = optJSONArrayAny(*keys) ?: return emptyList()
        return array.items().mapNotNull { value ->
            when (value) {
                is JSONObject -> {
                    val trait = value.optStringAny("trait", "valor", "value", "campo", "field")
                    val confidence = value.optStringAny("confidence", "confianca")
                    if (trait.isBlank()) null else TraitItem(trait = trait, confidence = confidence)
                }
                is String -> if (value.isBlank()) null else TraitItem(trait = value)
                else -> value?.toString()?.takeIf { it.isNotBlank() }?.let { TraitItem(trait = it) }
            }
        }
    }

    private fun JSONObject.stringList(vararg keys: String): List<String> {
        val array = optJSONArrayAny(*keys) ?: return emptyList()
        return array.items().mapNotNull { value ->
            when (value) {
                is JSONObject -> value.optStringAny("trait", "valor", "value", "sinal", "campo", "categoria")
                is String -> value
                else -> value?.toString().orEmpty()
            }.trim().takeIf { it.isNotBlank() }
        }
    }

    private fun JSONArray.items(): List<Any?> = (0 until length()).map { opt(it) }

    private fun JSONObject.optStringAny(vararg keys: String): String {
        return keys.firstNotNullOfOrNull { key ->
            if (has(key) && !isNull(key)) optString(key).trim().takeIf { it.isNotBlank() } else null
        }.orEmpty()
    }

    private fun JSONObject.optIntAny(vararg keys: String): Int {
        return keys.firstNotNullOfOrNull { key ->
            if (!has(key) || isNull(key)) null else runCatching { getInt(key) }.getOrNull()
        } ?: 0
    }

    private fun JSONObject.optJSONArrayAny(vararg keys: String): JSONArray? {
        return keys.firstNotNullOfOrNull { key -> optJSONArray(key) }
    }

    private fun JSONObject.optJSONObjectAny(vararg keys: String): JSONObject? {
        return keys.firstNotNullOfOrNull { key -> optJSONObject(key) }
    }

    private fun extractWeightsObject(rawText: String): JSONObject? {
        return extractJsonObjects(rawText)
            .mapNotNull { candidate -> runCatching { JSONObject(candidate) }.getOrNull() }
            .firstOrNull { it.has("relationship_goal") && it.has("future_plans") }
    }

    private fun extractJsonObjects(rawText: String): List<String> {
        val objects = mutableListOf<String>()
        rawText.forEachIndexed { index, char ->
            if (char == '{') {
                val end = findMatchingBrace(rawText, index)
                if (end > index) objects += rawText.substring(index, end + 1)
            }
        }
        return objects
    }

    private fun findMatchingBrace(text: String, start: Int): Int {
        var depth = 0
        var inString = false
        var escaped = false

        for (i in start until text.length) {
            val char = text[i]
            if (escaped) {
                escaped = false
                continue
            }
            if (char == '\\' && inString) {
                escaped = true
                continue
            }
            if (char == '"') {
                inString = !inString
                continue
            }
            if (inString) continue

            when (char) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return -1
    }

    private fun fallbackProfile(rawText: String, nowMillis: Long): ComplementaryProfile {
        val summary = rawText
            .lineSequence()
            .map { it.trim() }
            .firstOrNull { line ->
                line.length >= 80 &&
                    !line.startsWith("#") &&
                    !line.startsWith("{") &&
                    !line.startsWith("[")
            }
            ?: rawText.take(500)

        return ComplementaryProfile(
            summary = summary.take(500),
            generalConfidence = "baixa",
            missingOrUncertainData = listOf("json_estruturado_nao_detectado"),
            rawText = rawText,
            updatedAt = nowMillis
        )
    }
}
