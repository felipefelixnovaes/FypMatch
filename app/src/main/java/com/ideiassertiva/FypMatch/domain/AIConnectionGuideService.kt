package com.ideiassertiva.FypMatch.domain

import com.ideiassertiva.FypMatch.model.LiveConnection

class AIConnectionGuideService {
    
    // Simulação do LLM Injection via API 
    // Integraria com a classe que consome Claude/OpenAI (ex: AICounselorRepository)
    suspend fun generateConnectionSummary(connection: LiveConnection): String {
        // Formatar o JSON com as regras do AI_PROMPT_CONEXAO_VIVA.md
        val promptData = """
            {
              "status": "${connection.status.name}",
              "dimensions": {
                "reciprocity": ${connection.dimensions.reciprocity},
                "affinity": ${connection.dimensions.affinity},
                "lightness": ${connection.dimensions.lightness},
                "continuity": ${connection.dimensions.continuity},
                "depth": ${connection.dimensions.depth},
                "initiative": ${connection.dimensions.initiative}
              }
            }
        """.trimIndent()
        
        // Chamada fake para a API de LLM. Na implementação real, usaria retroFit/Ktor.
        // val response = llmClient.generateText(systemPrompt = SYSTEM_PROMPT, userPrompt = promptData)
        return "A conexão está evoluindo! Que tal fazerem uma pergunta divertida agora?"
    }
}
