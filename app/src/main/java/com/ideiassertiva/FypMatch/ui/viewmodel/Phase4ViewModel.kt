package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.Phase4AIRepository
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.ui.screens.DemoResult
import com.ideiassertiva.FypMatch.ui.screens.Phase4Stats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Phase4UIState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val demoResults: List<DemoResult> = emptyList(),
    val phase4Stats: Phase4Stats = Phase4Stats(),
    val showNeuroSupportDialog: Boolean = false
)

class Phase4ViewModel @Inject constructor(
    private val phase4Repository: Phase4AIRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(Phase4UIState())
    val uiState: StateFlow<Phase4UIState> = _uiState.asStateFlow()
    
    private val _personalityProfile = MutableStateFlow<PersonalityProfile?>(null)
    val personalityProfile: StateFlow<PersonalityProfile?> = _personalityProfile.asStateFlow()
    
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()
    
    private val _currentUserId = MutableStateFlow("")
    
    // Mock data for demonstration
    private val mockUsers = listOf(
        UserProfile(
            id = "user1",
            name = "Ana Silva",
            age = 28,
            bio = "Amo viajar, ler e conhecer pessoas novas. Sempre pronta para uma aventura! Trabalho com design e adoro cafeterias aconchegantes.",
            photos = listOf("photo1", "photo2"),
            interests = listOf("viagem", "leitura", "design", "café", "fotografia")
        ),
        UserProfile(
            id = "user2", 
            name = "Carlos Mendes",
            age = 32,
            bio = "Desenvolvedor apaixonado por tecnologia. Nas horas vagas, gosto de jogar videogame e assistir séries de ficção científica.",
            photos = listOf("photo3"),
            interests = listOf("tecnologia", "games", "séries", "programação", "música")
        ),
        UserProfile(
            id = "user3",
            name = "Mariana Costa",
            age = 26,
            bio = "Psicóloga e amante da natureza. Prefiro conversas profundas a small talk. Yoga e meditação fazem parte da minha rotina.",
            photos = listOf("photo4", "photo5", "photo6"),
            interests = listOf("psicologia", "natureza", "yoga", "meditação", "livros", "trilhas")
        )
    )
    
    fun initializeUser(userId: String) {
        _currentUserId.value = userId
        updateStats()
    }
    
    fun analyzePersonality() {
        viewModelScope.launch {
            try {
                _isAnalyzing.value = true
                
                // Create a mock user profile for the current user
                val currentUser = UserProfile(
                    id = _currentUserId.value,
                    name = "Você",
                    age = 25,
                    bio = "Pessoa curiosa que gosta de tecnologia, música e bons livros. Sempre em busca de conexões genuínas.",
                    photos = listOf("current_user_photo"),
                    interests = listOf("tecnologia", "música", "livros", "cinema", "viagem")
                )
                
                // Simulate message history for better analysis
                val mockMessages = listOf(
                    Message(
                        id = "msg1",
                        conversationId = "conv1",
                        senderId = _currentUserId.value,
                        content = "Oi! Como você está? Vi que curte música também 😊",
                        timestamp = System.currentTimeMillis(),
                        isRead = true
                    ),
                    Message(
                        id = "msg2", 
                        conversationId = "conv1",
                        senderId = _currentUserId.value,
                        content = "Que legal! Qual foi o último show que você foi? Eu ando muito viciado em indie rock ultimamente haha",
                        timestamp = System.currentTimeMillis() + 60000,
                        isRead = true
                    )
                )
                
                val result = phase4Repository.analyzePersonality(currentUser, mockMessages)
                if (result.isSuccess) {
                    _personalityProfile.value = result.getOrNull()
                    addDemoResult(DemoResult(
                        type = "personality",
                        title = "✅ Análise de Personalidade Concluída",
                        description = "Sua personalidade foi analisada com base no perfil e padrões de mensagem. A IA identificou características como extroversão, abertura a experiências e estilo de comunicação.",
                        details = listOf(
                            "MBTI: ${result.getOrNull()?.mbtiType ?: "Não determinado"}",
                            "Confiança: ${((result.getOrNull()?.confidence ?: 0f) * 100).toInt()}%",
                            "Fatores analisados: Bio, mensagens, interesses"
                        )
                    ))
                } else {
                    showError("Erro ao analisar personalidade: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                showError("Erro inesperado: ${e.message}")
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
    
    fun runCompatibilityDemo() {
        viewModelScope.launch {
            try {
                _isAnalyzing.value = true
                
                val currentUser = UserProfile(
                    id = _currentUserId.value,
                    name = "Você",
                    age = 25,
                    bio = "Pessoa curiosa que gosta de tecnologia, música e bons livros.",
                    photos = listOf("current_user_photo"),
                    interests = listOf("tecnologia", "música", "livros", "cinema")
                )
                
                val targetUser = mockUsers.random()
                
                val result = phase4Repository.analyzeCompatibility(
                    _currentUserId.value,
                    targetUser.id,
                    currentUser,
                    targetUser
                )
                
                if (result.isSuccess) {
                    val compatibility = result.getOrNull()!!
                    addDemoResult(DemoResult(
                        type = "compatibility",
                        title = "🤖 Compatibilidade ML Analisada",
                        description = "IA analisou sua compatibilidade com ${targetUser.name}. Score geral: ${(compatibility.overall * 100).toInt()}%",
                        details = compatibility.factors.map { 
                            "${it.name}: ${(it.score * 100).toInt()}%" 
                        }
                    ))
                } else {
                    showError("Erro na análise de compatibilidade")
                }
            } catch (e: Exception) {
                showError("Erro: ${e.message}")
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
    
    fun showNeuroSupportDemo() {
        viewModelScope.launch {
            try {
                _isAnalyzing.value = true
                
                // Simulate neuro profile creation
                val neuroPreferences = NeuroPreferences(
                    needsClearCommunication = true,
                    prefersDirectness = true,
                    sensitiveToCriticism = false,
                    needsRoutine = true,
                    prefersTextOverVoice = true
                )
                
                val result = phase4Repository.createNeuroProfile(
                    _currentUserId.value,
                    preferences = neuroPreferences
                )
                
                if (result.isSuccess) {
                    addDemoResult(DemoResult(
                        type = "neurosupport",
                        title = "🧠 Suporte para Neurodiversidade Ativado",
                        description = "Sistema adaptado para suas necessidades específicas. Recursos de acessibilidade e comunicação foram personalizados.",
                        details = listOf(
                            "Comunicação clara ativada",
                            "Interface simplificada",
                            "Sugestões diretas habilitadas",
                            "Tempo estendido para leitura"
                        )
                    ))
                } else {
                    showError("Erro ao configurar suporte neurodiverso")
                }
            } catch (e: Exception) {
                showError("Erro: ${e.message}")
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
    
    fun generateSmartSuggestions() {
        viewModelScope.launch {
            try {
                _isAnalyzing.value = true
                
                // Mock conversation context
                val mockContext = listOf(
                    Message(
                        id = "context1",
                        conversationId = "demo_conv",
                        senderId = "other_user",
                        content = "Oi! Tudo bem? Vi que você gosta de música, eu também sou muito fã!",
                        timestamp = System.currentTimeMillis(),
                        isRead = true
                    )
                )
                
                val result = phase4Repository.generateSmartSuggestions(
                    _currentUserId.value,
                    mockContext,
                    "other_user"
                )
                
                if (result.isSuccess) {
                    val suggestions = result.getOrNull()!!
                    addDemoResult(DemoResult(
                        type = "suggestions",
                        title = "💡 Sugestões Inteligentes Geradas",
                        description = "IA analisou o contexto da conversa e gerou ${suggestions.size} sugestões personalizadas para você.",
                        details = suggestions.map { it.text }
                    ))
                } else {
                    showError("Erro ao gerar sugestões")
                }
            } catch (e: Exception) {
                showError("Erro: ${e.message}")
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
    
    fun analyzeBehaviorPatterns() {
        viewModelScope.launch {
            try {
                _isAnalyzing.value = true
                
                // Mock swipe history
                val mockSwipeHistory = listOf(
                    SwipeRecord(
                        userId = _currentUserId.value,
                        targetUserId = "target1",
                        action = SwipeAction.LIKE,
                        timestamp = System.currentTimeMillis() - 3600000,
                        targetAge = 26,
                        distance = 5.2f,
                        swipeTime = 3500L,
                        profileViewTime = 8000L
                    ),
                    SwipeRecord(
                        userId = _currentUserId.value,
                        targetUserId = "target2", 
                        action = SwipeAction.PASS,
                        timestamp = System.currentTimeMillis() - 3000000,
                        targetAge = 35,
                        distance = 15.8f,
                        swipeTime = 1200L,
                        profileViewTime = 2000L
                    ),
                    SwipeRecord(
                        userId = _currentUserId.value,
                        targetUserId = "target3",
                        action = SwipeAction.LIKE,
                        timestamp = System.currentTimeMillis() - 7200000,
                        targetAge = 24,
                        distance = 8.5f,
                        swipeTime = 4200L,
                        profileViewTime = 12000L
                    )
                )
                
                val result = phase4Repository.analyzeSwipeBehavior(_currentUserId.value, mockSwipeHistory)
                
                if (result.isSuccess) {
                    val behavior = result.getOrNull()!!
                    addDemoResult(DemoResult(
                        type = "behavior",
                        title = "📊 Padrões de Comportamento Identificados",
                        description = "IA analisou seus padrões de swipe e identificou preferências automáticas.",
                        details = listOf(
                            "Taxa de curtidas: ${(behavior.patterns.likeRate * 100).toInt()}%",
                            "Tempo médio por perfil: ${behavior.patterns.averageSwipeTime / 1000}s",
                            "Idade preferida: ${behavior.patterns.agePreferencePattern.mostLikedAgeRange}",
                            "Distância média: ${behavior.patterns.distancePattern.averageDistance.toInt()}km"
                        )
                    ))
                } else {
                    showError("Erro na análise comportamental")
                }
            } catch (e: Exception) {
                showError("Erro: ${e.message}")
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
    
    private fun addDemoResult(result: DemoResult) {
        _uiState.value = _uiState.value.copy(
            demoResults = _uiState.value.demoResults + result
        )
        updateStats()
    }
    
    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }
    
    private fun updateStats() {
        val currentStats = _uiState.value.phase4Stats
        _uiState.value = _uiState.value.copy(
            phase4Stats = currentStats.copy(
                totalAnalyses = currentStats.totalAnalyses + 1,
                avgCompatibility = if (_uiState.value.demoResults.any { it.type == "compatibility" }) 0.75f else currentStats.avgCompatibility,
                aiAccuracy = 0.87f // Mock high accuracy score
            )
        )
    }
    
    fun clearDemoResults() {
        _uiState.value = _uiState.value.copy(demoResults = emptyList())
    }
    
    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}