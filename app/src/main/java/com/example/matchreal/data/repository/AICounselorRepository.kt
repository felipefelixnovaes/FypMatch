package com.example.matchreal.data.repository

import com.example.matchreal.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import java.util.Date
import java.util.UUID

class AICounselorRepository {
    
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
    
    private fun generateAIResponse(message: String, session: CounselorSession): CounselorMessage {
        val needsHelp = message.lowercase().contains("suicida") || 
                       message.lowercase().contains("depressão severa")
        
        val response = when {
            needsHelp -> {
                "Agradeço por compartilhar isso. Recomendo buscar ajuda de um profissional " +
                "qualificado como psicólogo ou psiquiatra. Você merece o melhor cuidado. 💙"
            }
            message.lowercase().contains("ansioso") -> {
                "Ansiedade em relacionamentos é normal. Vamos trabalhar algumas técnicas " +
                "de respiração e preparação mental para encontros."
            }
            else -> {
                "Entendo sua situação. Relacionamentos são complexos. " +
                "Vamos focar no que você pode controlar - suas ações e reações."
            }
        }
        
        return CounselorMessage(
            content = response,
            sender = MessageSender.AI_COUNSELOR,
            containsWarning = needsHelp
        )
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