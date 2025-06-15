package com.ideiassertiva.FypMatch.data.repository

import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AICounselorRepository @Inject constructor(
    private val geminiRepository: GeminiRepository
) {
    
    private val adsRepository = AdsRepository()
    
    // Estado das sessões ativas
    private val _activeSessions = MutableStateFlow<Map<String, CounselorSession>>(emptyMap())
    val activeSessions: Flow<Map<String, CounselorSession>> = _activeSessions.asStateFlow()
    
    // Estado da sessão atual
    private val _currentSession = MutableStateFlow<CounselorSession?>(null)
    val currentSession: Flow<CounselorSession?> = _currentSession.asStateFlow()
    
    // Estado de carregamento
    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading.asStateFlow()
    
    // Créditos do usuário
    val userCredits = adsRepository.userCredits
    
    // Estatísticas do usuário
    private val _userStats = MutableStateFlow<Map<String, CounselorStats>>(emptyMap())
    val userStats: Flow<Map<String, CounselorStats>> = _userStats.asStateFlow()
    
    // Inicializar créditos do usuário
    fun initializeCredits(userId: String, subscription: SubscriptionStatus) {
        adsRepository.initializeUserCredits(userId, subscription)
    }
    
    // Verificar se usuário pode enviar mensagem
    fun canSendMessage(userId: String): Boolean {
        return adsRepository.canSendMessage(userId)
    }
    
    // Obter créditos do usuário
    fun getUserCredits(userId: String): AiCredits {
        return adsRepository.getUserCredits(userId)
    }
    
    // Assistir anúncio para ganhar créditos
    suspend fun watchAdForCredits(userId: String): Result<Int> {
        return adsRepository.showRewardedAd(userId)
    }
    
    // Verificar se pode assistir anúncio
    fun canWatchAd(userId: String): Boolean {
        return adsRepository.canWatchAd(userId)
    }
    
    // Obter estatísticas de anúncios
    fun getAdStats(userId: String): AdStats {
        return adsRepository.getAdStats(userId)
    }
    
    // Iniciar nova sessão
    suspend fun startSession(
        userId: String,
        sessionType: SessionType = SessionType.GENERAL,
        initialMood: UserMood? = null
    ): Result<CounselorSession> {
        return try {
            val session = CounselorSession(
                userId = userId,
                sessionType = sessionType,
                mood = initialMood,
                messages = listOf(generateWelcomeMessage(sessionType))
            )
            
            _activeSessions.value = _activeSessions.value + (userId to session)
            _currentSession.value = session
            
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Enviar mensagem
    suspend fun sendMessage(
        userId: String,
        content: String
    ): Result<CounselorMessage> {
        return try {
            // Verificar se tem créditos suficientes
            if (!canSendMessage(userId)) {
                return Result.failure(Exception("Créditos insuficientes"))
            }
            
            _isLoading.value = true
            
            val session = _activeSessions.value[userId]
                ?: return Result.failure(Exception("Sessão não encontrada"))
            
            // Consumir créditos antes de processar
            val creditResult = adsRepository.consumeCreditsForMessage(userId)
            if (creditResult.isFailure) {
                _isLoading.value = false
                return Result.failure(creditResult.exceptionOrNull() ?: Exception("Erro ao consumir créditos"))
            }
            
            delay(1500) // Simular processamento
            
            val userMessage = CounselorMessage(
                content = content,
                sender = MessageSender.USER
            )
            
            val aiResponse = generateAIResponse(content, session)
            
            val updatedSession = session.copy(
                messages = session.messages + userMessage + aiResponse,
                lastMessageAt = Date()
            )
            
            _activeSessions.value = _activeSessions.value + (userId to updatedSession)
            _currentSession.value = updatedSession
            
            _isLoading.value = false
            Result.success(aiResponse)
        } catch (e: Exception) {
            _isLoading.value = false
            Result.failure(e)
        }
    }
    
    // Finalizar sessão
    suspend fun endSession(userId: String): Result<Unit> {
        return try {
            val session = _activeSessions.value[userId]
                ?: return Result.failure(Exception("Sessão não encontrada"))
            
            val endedSession = session.copy(isActive = false)
            _activeSessions.value = _activeSessions.value + (userId to endedSession)
            _currentSession.value = null
            
            updateUserStats(userId, endedSession)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Avaliar mensagem da IA (útil/não útil)
    suspend fun rateMessage(
        messageId: String,
        userId: String,
        isHelpful: Boolean
    ): Result<Unit> {
        return try {
            val session = _activeSessions.value[userId]
                ?: return Result.failure(Exception("Sessão não encontrada"))
            
            val updatedMessages = session.messages.map { message ->
                if (message.id == messageId) {
                    message.copy(isHelpful = isHelpful)
                } else {
                    message
                }
            }
            
            val updatedSession = session.copy(messages = updatedMessages)
            _activeSessions.value = _activeSessions.value + (userId to updatedSession)
            _currentSession.value = updatedSession
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Obter estatísticas do usuário
    fun getUserStats(userId: String): CounselorStats {
        return _userStats.value[userId] ?: CounselorStats()
    }
    
    private fun generateWelcomeMessage(sessionType: SessionType): CounselorMessage {
        val text = when (sessionType) {
            SessionType.GENERAL -> "Olá! 😊 Sou seu conselheiro de relacionamentos. Como posso te ajudar hoje?"
            SessionType.DATING_ANXIETY -> "Oi! 💙 Vamos trabalhar juntos para diminuir sua ansiedade em encontros."
            else -> "Olá! Estou aqui para te apoiar. Como posso ajudar?"
        }
        
        return CounselorMessage(
            content = text,
            sender = MessageSender.AI_COUNSELOR
        )
    }
    
    private suspend fun generateAIResponse(message: String, session: CounselorSession): CounselorMessage {
        val needsHelp = message.lowercase().contains("suicida") || 
                       message.lowercase().contains("depressão severa") ||
                       message.lowercase().contains("me matar") ||
                       message.lowercase().contains("não aguento mais")
        
        // Se detectar sinais de risco, resposta imediata de segurança
        if (needsHelp) {
            return CounselorMessage(
                content = "Agradeço por compartilhar isso comigo. É muito importante que você busque ajuda de um profissional qualificado como psicólogo ou psiquiatra. Você merece o melhor cuidado e apoio. 💙\n\nCVV: 188 (24h gratuito)\nCaps: Centros de Atenção Psicossocial",
                sender = MessageSender.AI_COUNSELOR,
                containsWarning = true
            )
        }
        
        return try {
            // Criar contexto da sessão para a IA
            val sessionContext = buildSessionContext(session, message)
            
            // Chamar API real do Gemini
            val aiResponse = geminiRepository.sendMessage(sessionContext).first()
            
            CounselorMessage(
                content = aiResponse,
                sender = MessageSender.AI_COUNSELOR,
                containsWarning = false
            )
            
        } catch (e: Exception) {
            // Fallback em caso de erro na API
            val fallbackResponse = generateFallbackResponse(message)
            
            CounselorMessage(
                content = fallbackResponse,
                sender = MessageSender.AI_COUNSELOR,
                containsWarning = false
            )
        }
    }
    
    private fun buildSessionContext(session: CounselorSession, currentMessage: String): String {
        val sessionTypeContext = when (session.sessionType) {
            SessionType.DATING_ANXIETY -> "O usuário está buscando ajuda com ansiedade em encontros."
            SessionType.COMMUNICATION -> "O usuário quer melhorar suas habilidades de comunicação."
            SessionType.SELF_ESTEEM -> "O usuário está trabalhando autoestima e confiança."
            SessionType.RELATIONSHIP_GOALS -> "O usuário está definindo objetivos de relacionamento."
            else -> "Conversa geral sobre relacionamentos e encontros."
        }
        
        val moodContext = session.mood?.let { mood ->
            when (mood) {
                UserMood.ANXIOUS -> "O usuário está se sentindo ansioso."
                UserMood.CONFUSED -> "O usuário está confuso sobre sua situação."
                UserMood.LONELY -> "O usuário está se sentindo sozinho."
                UserMood.FRUSTRATED -> "O usuário está frustrado."
                UserMood.HOPEFUL -> "O usuário está esperançoso."
                else -> ""
            }
        } ?: ""
        
        val recentMessages = session.messages.takeLast(3).joinToString("\n") { msg ->
            "${if (msg.sender == MessageSender.USER) "Usuário" else "Conselheiro"}: ${msg.content}"
        }
        
        return """
            Você é um conselheiro especializado em relacionamentos do FypMatch.
            
            CONTEXTO DA SESSÃO:
            - Tipo: $sessionTypeContext
            - Estado emocional: $moodContext
            
            HISTÓRICO RECENTE:
            $recentMessages
            
            MENSAGEM ATUAL DO USUÁRIO: $currentMessage
            
            INSTRUÇÕES:
            - Seja empático, acolhedor e profissional
            - Dê conselhos práticos e específicos
            - Use linguagem natural do português brasileiro
            - Mantenha respostas entre 50-150 palavras
            - Use emojis ocasionalmente (máximo 2)
            - Foque em soluções e crescimento pessoal
            - Se necessário, recomende ajuda profissional
            
            Responda como um conselheiro experiente daria um conselho personalizado.
        """.trimIndent()
    }
    
    private fun generateFallbackResponse(message: String): String {
        val message = message.lowercase()
        
        return when {
            message.contains("ansioso") || message.contains("nervoso") -> {
                "É completamente normal sentir ansiedade em relacionamentos! 💕 Respire fundo e lembre-se: você tem qualidades únicas. A pessoa certa vai valorizar sua autenticidade."
            }
            message.contains("conversa") || message.contains("falar") -> {
                "Para conversas envolventes, faça perguntas abertas sobre os interesses da pessoa! 😊 Mostre curiosidade genuína e escute ativamente. Isso cria conexões reais."
            }
            message.contains("match") || message.contains("primeira mensagem") -> {
                "Parabéns pelo match! 💕 Mencione algo específico do perfil da pessoa para mostrar interesse genuíno. Evite apenas 'oi, tudo bem?' - seja criativo mas autêntico!"
            }
            message.contains("autoestima") || message.contains("confiança") -> {
                "Sua autoestima é seu bem mais precioso! 💕 Faça uma lista das suas qualidades, cuide bem de si mesmo(a) e lembre-se: você merece amor e respeito."
            }
            else -> {
                "Entendo sua situação! 💕 Relacionamentos podem ser desafiadores, mas cada experiência nos ensina algo valioso. Que aspecto específico você gostaria de explorar mais?"
            }
        }
    }
    
    // Atualizar estatísticas do usuário
    private fun updateUserStats(userId: String, session: CounselorSession) {
        val currentStats = _userStats.value[userId] ?: CounselorStats()
        val sessionDuration = session.getDurationMinutes()
        
        val updatedStats = currentStats.copy(
            totalSessions = currentStats.totalSessions + (if (!session.isActive) 1 else 0),
            totalMessages = session.getMessageCount(),
            averageSessionLength = if (currentStats.totalSessions > 0) {
                (currentStats.averageSessionLength + sessionDuration) / 2
            } else sessionDuration,
            lastSessionDate = session.lastMessageAt
        )
        
        _userStats.value = _userStats.value + (userId to updatedStats)
    }
} 
