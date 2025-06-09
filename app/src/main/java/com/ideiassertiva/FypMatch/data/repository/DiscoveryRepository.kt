package com.ideiassertiva.FypMatch.data.repository

import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import java.util.UUID
import kotlin.random.Random

class DiscoveryRepository {
    
    // Dados simulados para desenvolvimento
    private val _discoveryCards = MutableStateFlow<List<DiscoveryCard>>(emptyList())
    val discoveryCards: Flow<List<DiscoveryCard>> = _discoveryCards.asStateFlow()
    
    private val _swipeActions = MutableStateFlow<List<SwipeRecord>>(emptyList())
    val swipeActions: Flow<List<SwipeRecord>> = _swipeActions.asStateFlow()
    
    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: Flow<List<Match>> = _matches.asStateFlow()
    
    init {
        generateMockUsers()
    }
    
    // Gerar usuários fictícios para demonstração
    private fun generateMockUsers() {
        val mockCards = listOf(
            createMockCard("Ana Silva", 25, "São Paulo", "Amo viajar e conhecer pessoas novas! 🌎", 85.5f),
            createMockCard("Maria Santos", 28, "Rio de Janeiro", "Adoro música e arte. Procuro alguém especial 🎵", 78.2f),
            createMockCard("Julia Costa", 24, "Belo Horizonte", "Formada em psicologia, apaixonada por livros 📚", 92.1f),
            createMockCard("Carla Lima", 30, "Porto Alegre", "Empresária, busco relacionamento sério 💼", 88.7f),
            createMockCard("Sofia Oliveira", 26, "Salvador", "Professora, amo crianças e natureza 🌱", 81.3f),
            createMockCard("Bruno Ferreira", 29, "São Paulo", "Desenvolvedor, gamer e cinéfilo 🎮", 76.9f),
            createMockCard("Pedro Santos", 27, "Curitiba", "Médico, adoro esportes e música 🏃‍♂️", 89.4f),
            createMockCard("Lucas Almeida", 31, "Brasília", "Advogado, procuro algo sério e duradouro ⚖️", 83.6f)
        )
        _discoveryCards.value = mockCards
    }
    
    private fun createMockCard(name: String, age: Int, city: String, bio: String, score: Float): DiscoveryCard {
        val user = User(
            id = UUID.randomUUID().toString(),
            displayName = name,
            profile = UserProfile(
                fullName = name,
                age = age,
                bio = bio,
                location = Location(city = city),
                gender = if (Random.nextBoolean()) Gender.FEMALE else Gender.MALE,
                orientation = Orientation.STRAIGHT,
                intention = Intention.DATING,
                photos = listOf("https://picsum.photos/400/600?random=${Random.nextInt(100)}")
            )
        )
        
        return DiscoveryCard(
            user = user,
            distance = Random.nextInt(1, 50),
            compatibilityScore = score / 100f,
            commonInterests = listOf("Música", "Viagem", "Cinema").shuffled().take(Random.nextInt(1, 3)),
            photos = user.profile.photos,
            isVerified = Random.nextBoolean()
        )
    }
    
    // Executar ação de swipe
    suspend fun performSwipe(
        fromUserId: String,
        toUserId: String,
        swipeType: SwipeType
    ): Result<SwipeResult> {
        return try {
            val swipeAction = SwipeRecord(
                id = UUID.randomUUID().toString(),
                fromUserId = fromUserId,
                toUserId = toUserId,
                action = swipeType,
                createdAt = Date()
            )
            
            // Verificar se o outro usuário já curtiu este usuário
            val existingSwipe = _swipeActions.value.find { 
                it.fromUserId == toUserId && it.toUserId == fromUserId && it.action != SwipeType.PASS 
            }
            
            // Para facilitar testes, simular que 70% dos likes resultam em match
            val simulatedMatch = swipeType != SwipeType.PASS && Random.nextFloat() < 0.7f
            val isMatch = existingSwipe != null || simulatedMatch
            
            val updatedSwipe = swipeAction.copy(isMatch = isMatch)
            _swipeActions.value = _swipeActions.value + updatedSwipe
            
            // Se houve match, criar o objeto Match
            val match = if (isMatch) {
                val newMatch = Match(
                    id = UUID.randomUUID().toString(),
                    user1Id = fromUserId,
                    user2Id = toUserId,
                    createdAt = Date()
                )
                _matches.value = _matches.value + newMatch
                newMatch
            } else null
            
            // Remover o card da lista de discovery
            _discoveryCards.value = _discoveryCards.value.filter { it.user.id != toUserId }
            
            Result.success(SwipeResult(isMatch, match, swipeType))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Obter cards para discovery
    fun getDiscoveryCards(): List<DiscoveryCard> {
        return _discoveryCards.value
    }
    
    // Obter matches do usuário
    fun getUserMatches(userId: String): List<Match> {
        return _matches.value.filter { 
            (it.user1Id == userId || it.user2Id == userId) && it.isActive 
        }
    }
    
    // Verificar limites de likes (para monetização)
    fun checkLikeLimit(userId: String, subscription: SubscriptionStatus): Boolean {
        val today = Date()
        val todaySwipes = _swipeActions.value.filter { swipe ->
            swipe.fromUserId == userId && 
            swipe.action == SwipeType.LIKE &&
            isSameDay(swipe.createdAt, today)
        }
        
        return when (subscription) {
            SubscriptionStatus.FREE -> todaySwipes.size < 10 // 10 likes por dia
            SubscriptionStatus.PREMIUM -> todaySwipes.size < 100 // 100 likes por dia
            SubscriptionStatus.VIP -> true // Ilimitado
        }
    }
    
    // Verificar limites de super likes
    fun checkSuperLikeLimit(userId: String, subscription: SubscriptionStatus): Boolean {
        val today = Date()
        val todaySuperLikes = _swipeActions.value.filter { swipe ->
            swipe.fromUserId == userId && 
            swipe.action == SwipeType.SUPER_LIKE &&
            isSameDay(swipe.createdAt, today)
        }
        
        return when (subscription) {
            SubscriptionStatus.FREE -> todaySuperLikes.isEmpty() // 1 super like por dia
            SubscriptionStatus.PREMIUM -> todaySuperLikes.size < 5 // 5 super likes por dia
            SubscriptionStatus.VIP -> true // Ilimitado
        }
    }
    
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { time = date1 }
        val cal2 = java.util.Calendar.getInstance().apply { time = date2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
}

data class SwipeResult(
    val isMatch: Boolean,
    val match: Match?,
    val swipeType: SwipeType
) 
