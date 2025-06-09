package com.ideiassertiva.FypMatch.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class GeminiRepository @Inject constructor() {
    
    // TODO: Implementar integração real com Firebase AI Logic quando a API estiver estável
    // Por enquanto, usando implementação mock para desenvolvimento
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
            
            // Simula processamento
            delay(1000)
            
            // Respostas mock baseadas no contexto
            val response = when {
                userMessage.contains("ansiedade", ignoreCase = true) -> 
                    "É normal sentir ansiedade em encontros! Respire fundo, seja você mesmo e lembre-se que a outra pessoa também pode estar nervosa. Que tal começar com uma conversa leve sobre interesses em comum?"
                
                userMessage.contains("conversa", ignoreCase = true) -> 
                    "Para manter uma boa conversa, faça perguntas abertas sobre os interesses da pessoa, escute ativamente e compartilhe suas próprias experiências. Evite temas polêmicos no início!"
                
                userMessage.contains("match", ignoreCase = true) -> 
                    "Parabéns pelo match! Uma boa estratégia é mencionar algo específico do perfil da pessoa para mostrar que você prestou atenção. Seja genuíno e mantenha um tom positivo."
                
                userMessage.contains("encontro", ignoreCase = true) -> 
                    "Para um primeiro encontro, escolha um local público e confortável, chegue pontualmente e seja você mesmo. O importante é se divertir e conhecer a pessoa melhor!"
                
                else -> 
                    "Entendo sua situação. Relacionamentos podem ser desafiadores, mas lembre-se de ser autêntico e paciente. Cada pessoa é única, então foque em conexões genuínas. Como posso ajudar mais especificamente?"
            }
            
            emit(response)
        } catch (e: Exception) {
            emit("Desculpe, não consegui processar sua mensagem no momento. Tente novamente em alguns instantes.")
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
            
            // Simula processamento
            delay(500)
            
            // Algoritmo mock de compatibilidade baseado em palavras-chave
            val profile1Words = user1Profile.lowercase().split(" ", ",", ".", ";")
            val profile2Words = user2Profile.lowercase().split(" ", ",", ".", ";")
            
            val commonInterests = profile1Words.intersect(profile2Words.toSet()).size
            val totalWords = (profile1Words + profile2Words).distinct().size
            
            // Calcula compatibilidade base
            val baseCompatibility = if (totalWords > 0) {
                (commonInterests.toFloat() / totalWords * 100).toInt()
            } else {
                50
            }
            
            // Adiciona variação aleatória para simular análise mais complexa
            val variation = Random.nextInt(-15, 16)
            val finalScore = (baseCompatibility + variation).coerceIn(25, 95)
            
            Result.success(finalScore)
        } catch (e: Exception) {
            Result.failure(e)
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
            
            // Simula processamento
            delay(300)
            
            // Gera sugestões baseadas em palavras-chave do perfil
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
                profileLower.contains("livro") || profileLower.contains("leitura") -> {
                    suggestions.add("Qual foi o último livro que te marcou?")
                    suggestions.add("Que gênero literário você mais curte?")
                    suggestions.add("Tem algum autor favorito ou descoberta recente?")
                }
                else -> {
                    suggestions.add("O que você mais gosta de fazer no tempo livre?")
                    suggestions.add("Qual foi o melhor momento do seu fim de semana?")
                    suggestions.add("Tem algum hobby que te deixa super animado(a)?")
                }
            }
            
            // Garante que sempre temos 3 sugestões
            while (suggestions.size < 3) {
                val generic = listOf(
                    "Conte uma curiosidade sobre você que poucos sabem!",
                    "Qual é sua definição de um dia perfeito?",
                    "Se pudesse jantar com qualquer pessoa, quem seria?"
                )
                suggestions.add(generic.random())
            }
            
            Result.success(suggestions.take(3))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Detecta se a mensagem indica necessidade de ajuda profissional
     */
    suspend fun detectSeriousIssues(message: String): Result<Boolean> {
        return try {
            if (!isAvailable) {
                return Result.failure(Exception("Serviço indisponível"))
            }
            
            // Simula processamento
            delay(200)
            
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
            
            Result.success(hasSeriousIndicators || hasConcerningPatterns)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 