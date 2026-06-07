package com.ideiassertiva.FypMatch.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComplementaryProfileParserTest {

    @Test
    fun `parse importa JSON estruturado e pesos sugeridos`() {
        val raw = """
Texto antes.
{
  "fypmatch_complementary_profile": {
    "summary": "Pessoa que valoriza reciprocidade e clareza.",
    "general_confidence": "alta",
    "observed_traits": [
      { "trait": "gosta de conversas profundas", "confidence": "alta" }
    ],
    "relationship_style": {
      "communication_need": "clara e frequente",
      "emotional_security_need": "alta"
    },
    "core_values": ["reciprocidade", "clareza"],
    "algorithmic_tags": ["alta_recprocidade", "conversa_profunda"],
    "green_flags": ["demonstra presença consistente"],
    "red_flags": ["ambiguidade persistente"]
  }
}
{
  "relationship_goal": 16,
  "values": 14,
  "affection_style": 13,
  "communication_style": 13,
  "emotional_maturity": 14,
  "lifestyle": 10,
  "personality": 8,
  "hobbies_and_interests": 4,
  "cultural_preferences": 3,
  "future_plans": 5
}
""".trimIndent()

        val profile = ComplementaryProfileParser.parse(raw, nowMillis = 123L)

        assertEquals("Pessoa que valoriza reciprocidade e clareza.", profile.summary)
        assertEquals("alta", profile.generalConfidence)
        assertEquals(listOf("reciprocidade", "clareza"), profile.coreValues)
        assertEquals("clara e frequente", profile.relationshipStyle.communicationNeed)
        assertEquals(100, profile.weights.total())
        assertEquals(123L, profile.updatedAt)
        assertTrue(profile.isPresent())
    }

    @Test
    fun `parse sem JSON salva texto bruto com baixa confianca`() {
        val raw = "Este perfil ainda nao possui o bloco estruturado, mas tem uma descricao longa o suficiente para virar resumo bruto."

        val profile = ComplementaryProfileParser.parse(raw, nowMillis = 456L)

        assertEquals("baixa", profile.generalConfidence)
        assertEquals(raw, profile.rawText)
        assertEquals(listOf("json_estruturado_nao_detectado"), profile.missingOrUncertainData)
        assertEquals(456L, profile.updatedAt)
    }
}
