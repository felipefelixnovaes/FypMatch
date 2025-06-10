package com.ideiassertiva.FypMatch.data.repository

import android.util.Log
import com.ideiassertiva.FypMatch.BuildConfig
import com.ideiassertiva.FypMatch.util.AnalyticsManager
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class GeminiRepository @Inject constructor(
    private val analyticsManager: AnalyticsManager
) {
    
    companion object {
        private const val TAG = "GeminiRepository"
        // API Key testada e funcionando: AIzaSyAsUX8dj3_OKuHWQlEsBEGa0d3mWFqat2E
        private const val GEMINI_API_KEY = "AIzaSyAsUX8dj3_OKuHWQlEsBEGa0d3mWFqat2E"
    }
    
    // Inicializa Firebase AI Logic com Gemini Developer API
    private val model = try {
        Firebase.ai.generativeModel("gemini-2.0-flash")
    } catch (e: Exception) {
        Log.e(TAG, "Erro ao inicializar Gemini model", e)
        analyticsManager.logError(e, "gemini_init_error")
        null
    }
    
    private val isAvailable = model != null
    
    /**
     * Envia uma mensagem para o Gemini e retorna a resposta como Flow
     */
    suspend fun sendMessage(userMessage: String): Flow<String> = flow {
        try {
            if (BuildConfig.FIREBASE_DEBUG) {
                Log.d(TAG, "Enviando mensagem para Gemini: $userMessage")
            }
            
            if (!isAvailable || model == null) {
                Log.w(TAG, "Gemini indisponível, usando fallback")
                emit(getFallbackResponse(userMessage))
                return@flow
            }
            
            // Cria prompt com contexto de relacionamentos
            val contextualPrompt = """
                Você é a Fype, uma conselheira de relacionamentos carinhosa e experiente do FypMatch.
                
                CARACTERÍSTICAS DA FYPE:
                - Amigável, empática e acolhedora
                - Especialista em relacionamentos, amor e encontros
                - Usa linguagem natural do português brasileiro
                - Dá conselhos práticos e positivos
                - Incentiva autoestima e crescimento pessoal
                - Usa emojis ocasionalmente para ser mais calorosa (máximo 2 por resposta)
                - Respostas entre 50-150 palavras
                
                CONTEXTO: Usuário do app de encontros FypMatch precisa de ajuda.
                
                PERGUNTA DO USUÁRIO: $userMessage
                
                IMPORTANTE: Responda SEMPRE como a Fype daria um conselho pessoal, acolhedor e específico para a situação. Seja autêntica e humana.
            """.trimIndent()
            
            // Analytics: Tentativa de chamada Gemini
            analyticsManager.logCustomCrash("gemini_request_attempt", mapOf(
                "message_length" to userMessage.length.toString(),
                "has_question_mark" to userMessage.contains("?").toString()
            ))
            
            // Chama API real do Gemini
            val result = model.generateContent(contextualPrompt)
            val response = result.text
            
            if (BuildConfig.FIREBASE_DEBUG) {
                Log.d(TAG, "Resposta do Gemini recebida: ${response?.take(100)}...")
            }
            
            if (!response.isNullOrBlank()) {
                // Analytics: Sucesso
                analyticsManager.logCustomCrash("gemini_response_success", mapOf(
                    "response_length" to response.length.toString(),
                    "response_has_emoji" to (response.contains("💕") || response.contains("😊") || response.contains("❤️")).toString()
                ))
                
                emit(response.trim())
            } else {
                throw Exception("Resposta vazia do Gemini")
            }
        
        } catch (e: Exception) {
            Log.e(TAG, "Erro na chamada Gemini: ${e.message}", e)
            
            // Analytics: Erro
            analyticsManager.logError(e, "gemini_api_error")
            
            // Fallback para respostas inteligentes em caso de erro
            val fallbackResponse = getFallbackResponse(userMessage)
            
            if (BuildConfig.FIREBASE_DEBUG) {
                Log.d(TAG, "Usando fallback response: $fallbackResponse")
            }
            
            emit(fallbackResponse)
        }
    }
    
    /**
     * Respostas de fallback inteligentes baseadas no contexto
     */
    private fun getFallbackResponse(userMessage: String): String {
        val message = userMessage.lowercase()
        
        return when {
            // Ansiedade e nervosismo
            message.contains("ansioso") || message.contains("nervoso") || message.contains("ansiedade") -> {
                listOf(
                    "É super normal sentir ansiedade em encontros! 💕 Respire fundo e lembre-se: a outra pessoa também pode estar nervosa. Seja você mesmo(a), isso é o mais atrativo!",
                    "Nervosismo pré-encontro é clássico! 😊 Tente algumas respirações profundas. Foque no que você tem de especial - sua personalidade única é seu maior charme!",
                    "Ansiedade é sinal de que você se importa, e isso é lindo! 💕 Prepare alguns tópicos de conversa e confie em si mesmo(a). Você vai se sair bem!"
                ).random()
            }
            
            // Conversa e comunicação
            message.contains("conversa") || message.contains("falar") || message.contains("assunto") -> {
                listOf(
                    "Para uma boa conversa, faça perguntas abertas sobre os interesses da pessoa! 😊 'O que te faz feliz?' é sempre melhor que 'Como foi seu dia?'",
                    "O segredo de uma conversa envolvente é escutar ativamente! 💕 Mostre interesse genuíno e faça perguntas baseadas no que a pessoa compartilhou.",
                    "Converse sobre experiências e sonhos! Pergunte sobre a melhor viagem, hobbies favoritos ou planos para o futuro. Isso cria conexão real!"
                ).random()
            }
            
            // Match e primeiras mensagens
            message.contains("match") || message.contains("primeira mensagem") || message.contains("inicial") -> {
                listOf(
                    "Parabéns pelo match! 💕 Mencione algo específico do perfil da pessoa para mostrar que você realmente olhou. Isso demonstra interesse genuíno!",
                    "Para a primeira mensagem, seja criativo(a) mas autêntico(a)! 😊 Um comentário sobre uma foto ou interesse em comum funciona muito bem.",
                    "Evite apenas 'oi, tudo bem?' Tente algo como: 'Vi que você curte [interesse]. Qual foi sua melhor experiência com isso?' Muito mais envolvente!"
                ).random()
            }
            
            // Relacionamentos e amor
            message.contains("relacionamento") || message.contains("amor") || message.contains("parceiro") -> {
                listOf(
                    "Relacionamentos saudáveis começam com autoconhecimento! 💕 Saiba o que você busca e valoriza. Assim atrairá alguém compatível.",
                    "O amor verdadeiro floresce quando somos autênticos! 😊 Não mude sua essência por ninguém. A pessoa certa vai te amar exatamente como você é.",
                    "Paciência é fundamental no amor! 💕 O relacionamento certo vale a espera. Enquanto isso, invista em si mesmo(a) e seja feliz sozinho(a) também."
                ).random()
            }
            
            // Autoestima e confiança
            message.contains("insegur") || message.contains("confiança") || message.contains("autoestima") -> {
                listOf(
                    "Você é único(a) e especial! 💕 Faça uma lista das suas qualidades e conquistas. Lembre-se delas sempre que a insegurança bater.",
                    "Confiança vem de dentro! 😊 Cuide bem de si mesmo(a), faça coisas que te fazem feliz e cerque-se de pessoas que te valorizam.",
                    "Sua autoestima é seu bem mais precioso! 💕 Trate-se com carinho, celebre pequenas vitórias e lembre-se: você merece amor e respeito."
                ).random()
            }
            
            // Encontros e dates
            message.contains("encontro") || message.contains("date") || message.contains("sair") -> {
                listOf(
                    "Para um primeiro encontro, escolha um lugar público e confortável! 😊 Café ou almoço são ótimas opções. O importante é ter um ambiente para conversar.",
                    "Vista-se de forma que você se sinta bem e confiante! 💕 Seja pontual, desligue o celular e esteja presente. Demonstre interesse genuíno!",
                    "Encontros são para se conhecerem melhor! 😊 Não crie expectativas muito altas. Relaxe, se divirta e veja se há química natural entre vocês."
                ).random()
            }
            
            // Resposta geral empática
            else -> {
                listOf(
                    "Entendo sua situação! 💕 Relacionamentos podem ser desafiadores, mas cada experiência nos ensina algo. Que parte específica te preocupa mais?",
                    "Você veio ao lugar certo para conversar sobre isso! 😊 Estou aqui para te apoiar. Pode me contar mais detalhes sobre o que está sentindo?",
                    "Cada pessoa é única, e sua jornada amorosa também é! 💕 Vamos trabalhar juntas para encontrar a melhor abordagem para sua situação. Me conte mais!",
                    "É corajoso buscar ajuda e reflexão! 😊 Isso mostra que você se importa com seu crescimento. Qual aspecto você gostaria de explorar primeiro?"
                ).random()
            }
        }
    }
    
    /**
     * Analisa compatibilidade entre dois usuários usando IA
     */
    suspend fun analyzeCompatibility(user1Bio: String, user2Bio: String): Flow<String> = flow {
        try {
            if (!isAvailable || model == null) {
                emit("Análise indisponível no momento. Que tal conversarem e descobrirem por si mesmos? 😊")
                return@flow
            }
            
            val prompt = """
                Você é a Fype, especialista em relacionamentos do FypMatch.
                
                Analise a compatibilidade entre estas duas pessoas baseado em suas biografias:
                
                PESSOA 1: $user1Bio
                PESSOA 2: $user2Bio
                
                Forneça uma análise de compatibilidade focando em:
                1. Pontos em comum
                2. Diferenças complementares
                3. Sugestões de conversa
                
                Seja otimista mas realista. Máximo 100 palavras.
            """.trimIndent()
            
            val result = model.generateContent(prompt)
            val response = result.text
            
            emit(response ?: "Vocês parecem ter potencial! Que tal começar conversando sobre interesses em comum? 💕")
        
        } catch (e: Exception) {
            Log.e(TAG, "Erro na análise de compatibilidade", e)
            emit("O importante é a conexão que vocês vão construir conversando! 😊 Sejam autênticos um com o outro.")
        }
    }
} 