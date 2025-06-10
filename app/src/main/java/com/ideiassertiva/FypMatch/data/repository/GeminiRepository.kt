package com.ideiassertiva.FypMatch.data.repository

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class GeminiRepository @Inject constructor() {
    
    // Inicializa Firebase AI Logic com Gemini Developer API
    // API Key testada e funcionando: AIzaSyAsUX8dj3_OKuHWQlEsBEGa0d3mWFqat2E
    private val model = Firebase.ai.generativeModel("gemini-2.0-flash")
    
    private val isAvailable = true
    
    /**
     * Envia uma mensagem para o Gemini e retorna a resposta como Flow
     */
    suspend fun sendMessage(userMessage: String): Flow<String> = flow {
        try {
            if (!isAvailable) {
                emit("Serviço de IA indisponível no momento.")
                return@flow
            }
            
            // Cria prompt com contexto de relacionamentos
            val contextualPrompt = """
                Você é um assistente de relacionamentos do FypMatch, um app de encontros brasileiro.
                Responda de forma amigável, empática e útil sobre relacionamentos, encontros e conversas.
                Mantenha respostas em português brasileiro, seja positivo e dê conselhos práticos.
                
                Pergunta do usuário: $userMessage
            """.trimIndent()
            
            // Chama API real do Gemini
            val result = model.generateContent(contextualPrompt)
            

            val response = result.text ?: "Desculpe, não consegui gerar uma resposta no momento."
            
            emit(response)
        } catch (e: Exception) {
            // Fallback para respostas mock em caso de erro
            val fallbackResponse = when {
                userMessage.contains("ansiedade", ignoreCase = true) -> 
                    "É normal sentir ansiedade em encontros! Respire fundo, seja você mesmo e lembre-se que a outra pessoa também pode estar nervosa."
                
                userMessage.contains("conversa", ignoreCase = true) -> 
                    "Para manter uma boa conversa, faça perguntas abertas sobre os interesses da pessoa e escute ativamente."
                
                userMessage.contains("match", ignoreCase = true) -> 
                    "Parabéns pelo match! Mencione algo específico do perfil da pessoa para mostrar interesse genuíno."
                
                else -> 
                    "Entendo sua situação. Relacionamentos podem ser desafiadores, mas seja autêntico e paciente. Como posso ajudar mais?"
            }
            
            emit(fallbackResponse)
        }
    }
    
    /**
     * Analisa compatibilidade entre dois usuários usando IA
     */
    suspend fun analyzeCompatibility(user1Profile: String, user2Profile: String): Result<Int> {
        return try {
            if (!isAvailable) {
                return Result.failure(Exception("Serviço indisponível"))
            }
            
            val compatibilityPrompt = """
                Analise a compatibilidade entre estes dois perfis de usuários de um app de encontros.
                Retorne APENAS um número de 0 a 100 representando a porcentagem de compatibilidade.
                
                Perfil 1: $user1Profile
                Perfil 2: $user2Profile
                
                Considere: interesses em comum, personalidade, objetivos, estilo de vida.
                Responda apenas com o número, sem texto adicional.
            """.trimIndent()
            
            val result = model.generateContent(compatibilityPrompt)
            val response = result.text?.trim() ?: ""
            
            // Extrai número da resposta
            val score = response.replace(Regex("[^0-9]"), "").toIntOrNull()
                ?: run {
                    // Fallback para algoritmo simples se Gemini não retornar número
                    val profile1Words = user1Profile.lowercase().split(" ", ",", ".", ";")
                    val profile2Words = user2Profile.lowercase().split(" ", ",", ".", ";")
                    val commonInterests = profile1Words.intersect(profile2Words.toSet()).size
                    val totalWords = (profile1Words + profile2Words).distinct().size
                    
                    if (totalWords > 0) {
                        (commonInterests.toFloat() / totalWords * 100).toInt().coerceIn(25, 95)
                    } else {
                        Random.nextInt(40, 80)
                    }
                }
            
            Result.success(score.coerceIn(0, 100))
        } catch (e: Exception) {
            // Fallback para algoritmo simples
            val profile1Words = user1Profile.lowercase().split(" ", ",", ".", ";")
            val profile2Words = user2Profile.lowercase().split(" ", ",", ".", ";")
            val commonInterests = profile1Words.intersect(profile2Words.toSet()).size
            val totalWords = (profile1Words + profile2Words).distinct().size
            
            val fallbackScore = if (totalWords > 0) {
                (commonInterests.toFloat() / totalWords * 100).toInt().coerceIn(25, 95)
            } else {
                Random.nextInt(40, 80)
            }
            
            Result.success(fallbackScore)
        }
    }
    
    /**
     * Gera sugestões de conversa baseadas no perfil do match
     */
    suspend fun generateConversationStarters(profileInfo: String): Result<List<String>> {
        return try {
            if (!isAvailable) {
                return Result.failure(Exception("Serviço indisponível"))
            }
            
            val startersPrompt = """
                Baseado neste perfil de usuário de um app de encontros, crie 3 perguntas interessantes para iniciar uma conversa.
                
                Perfil: $profileInfo
                
                Regras:
                - Seja natural e casual
                - Use português brasileiro
                - Foque nos interesses da pessoa
                - Evite perguntas óbvias ou genéricas
                - Faça perguntas que gerem uma conversa interessante
                
                Retorne apenas as 3 perguntas, uma por linha, sem numeração.
            """.trimIndent()
            
            val result = model.generateContent(startersPrompt)
            val response = result.text?.trim() ?: ""
            
            // Processa resposta em lista
            val suggestions = response.split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.matches(Regex("^[0-9]+\\..*")) }
                .take(3)
            
            if (suggestions.size >= 3) {
                Result.success(suggestions)
            } else {
                // Fallback para sugestões baseadas em palavras-chave
                val fallbackSuggestions = generateFallbackStarters(profileInfo)
                Result.success(fallbackSuggestions)
            }
        } catch (e: Exception) {
            // Fallback para sugestões baseadas em palavras-chave
            val fallbackSuggestions = generateFallbackStarters(profileInfo)
            Result.success(fallbackSuggestions)
        }
    }
    
    private fun generateFallbackStarters(profileInfo: String): List<String> {
        val suggestions = mutableListOf<String>()
        val profileLower = profileInfo.lowercase()
        
        when {
            profileLower.contains("música") || profileLower.contains("music") -> {
                suggestions.add("Que tipo de música você mais curte? Tem algum artista favorito?")
                suggestions.add("Já foi em algum show marcante? Qual foi a experiência?")
                suggestions.add("Toca algum instrumento ou só curte escutar mesmo?")
            }
            profileLower.contains("viagem") || profileLower.contains("travel") -> {
                suggestions.add("Qual foi a viagem mais incrível que você já fez?")
                suggestions.add("Tem algum destino dos sonhos na sua lista?")
                suggestions.add("Prefere aventuras ou relaxar nas férias?")
            }
            profileLower.contains("esporte") || profileLower.contains("sport") -> {
                suggestions.add("Que esporte você pratica? Como começou?")
                suggestions.add("Tem algum time do coração ou atleta que admira?")
                suggestions.add("Prefere esportes individuais ou em equipe?")
            }
            else -> {
                suggestions.add("O que você mais gosta de fazer no tempo livre?")
                suggestions.add("Qual foi o melhor momento do seu fim de semana?")
                suggestions.add("Tem algum hobby que te deixa super animado(a)?")
            }
        }
        
        return suggestions.take(3)
    }
    
    /**
     * Detecta se a mensagem indica necessidade de ajuda profissional
     */
    suspend fun detectSeriousIssues(message: String): Result<Boolean> {
        return try {
            if (!isAvailable) {
                return Result.failure(Exception("Serviço indisponível"))
            }
            
            val detectionPrompt = """
                Analise esta mensagem e determine se indica problemas graves que requerem ajuda profissional:
                
                "$message"
                
                Considere indicadores como:
                - Ideação suicida ou autolesão
                - Depressão severa ou desespero extremo
                - Abuso ou violência
                - Transtornos mentais graves
                - Crises emocionais intensas
                
                Responda apenas "SIM" se detectar problemas sérios, ou "NÃO" caso contrário.
            """.trimIndent()
            
            val result = model.generateContent(detectionPrompt)
            val response = result.text?.trim()?.uppercase() ?: ""
            
            val hasSeriousIssues = response.contains("SIM")
            
            // Fallback com verificação por palavras-chave se resposta não for clara
            if (!response.contains("SIM") && !response.contains("NÃO")) {
                val fallbackResult = detectSeriousIssuesFallback(message)
                Result.success(fallbackResult)
            } else {
                Result.success(hasSeriousIssues)
            }
        } catch (e: Exception) {
            // Fallback para detecção por palavras-chave
            val fallbackResult = detectSeriousIssuesFallback(message)
            Result.success(fallbackResult)
        }
    }
    
    private fun detectSeriousIssuesFallback(message: String): Boolean {
        // Palavras-chave que indicam problemas sérios
        val seriousKeywords = listOf(
            "suicídio", "suicidar", "morrer", "acabar com tudo",
            "depressão severa", "não aguento mais", "sem esperança",
            "abuso", "violência", "agressão", "machuca",
            "transtorno", "surto", "crise", "desespero total"
        )
        
        val messageLower = message.lowercase()
        val hasSeriousIndicators = seriousKeywords.any { keyword ->
            messageLower.contains(keyword)
        }
        
        // Também verifica padrões de linguagem preocupantes
        val concerningPatterns = listOf(
            "não vejo saída", "nada faz sentido", "todo mundo me odeia",
            "sou um fracasso total", "nunca vai melhorar"
        )
        
        val hasConcerningPatterns = concerningPatterns.any { pattern ->
            messageLower.contains(pattern)
        }
        
        return hasSeriousIndicators || hasConcerningPatterns
    }
} 