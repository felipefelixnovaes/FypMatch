package com.ideiassertiva.FypMatch.data.repository

import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.data.TestUsers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class UserRepository {
    
    // Estado do usuário atual
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: Flow<User?> = _currentUser.asStateFlow()
    
    // Usuários simulados para teste
    private val testUsers = mutableMapOf<String, User>()
    
    init {
        // Carregar todos os usuários de teste
        TestUsers.allUsers.forEach { user ->
            // Configurar créditos de IA baseado na assinatura
            val creditsLimit = when (user.subscription) {
                SubscriptionStatus.FREE -> AiCreditLimits.FREE_DAILY
                SubscriptionStatus.PREMIUM -> AiCreditLimits.PREMIUM_DAILY
                SubscriptionStatus.VIP -> AiCreditLimits.VIP_DAILY
            }
            
            val userWithCredits = user.copy(
                aiCredits = AiCredits(
                    current = creditsLimit,
                    dailyLimit = creditsLimit,
                    usedToday = 0,
                    lastResetDate = Date()
                )
            )
            testUsers[userWithCredits.email] = userWithCredits
        }
        
        // Criar usuário Felix especial
        val felixUser = User(
            id = "felix_test_id",
            email = "felix3designer@gmail.com",
            displayName = "Felix",
            subscription = SubscriptionStatus.VIP,
            accessLevel = AccessLevel.FULL_ACCESS,
            profile = UserProfile(
                fullName = "Felix Designer",
                age = 28,
                bio = "Designer e desenvolvedor que criou o FypMatch! 💻🚀❤️",
                location = Location(city = "São Paulo", state = "SP"),
                gender = Gender.MALE,
                orientation = Orientation.STRAIGHT,
                intention = Intention.DATING,
                interests = listOf("Design", "Programação", "Tecnologia", "Inovação", "Apps", "UX"),
                education = "Superior Completo - Design",
                profession = "Designer & Developer",
                height = 180,
                isProfileComplete = true
            ),
            aiCredits = AiCredits(
                current = 25,
                dailyLimit = AiCreditLimits.VIP_DAILY,
                usedToday = 0,
                lastResetDate = Date()
            )
        )
        
        testUsers[felixUser.email] = felixUser
        _currentUser.value = felixUser // Auto-login para teste
    }
    
    suspend fun getCurrentUser(): Flow<User?> {
        return currentUser
    }
    
    suspend fun getUserByEmail(email: String): User? {
        return testUsers[email]
    }
    
    suspend fun updateUser(user: User) {
        testUsers[user.email] = user
        if (_currentUser.value?.email == user.email) {
            _currentUser.value = user
        }
    }
    
    suspend fun loginUser(email: String): User? {
        val user = testUsers[email]
        if (user != null) {
            _currentUser.value = user
        }
        return user
    }
    
    suspend fun createUser(user: User): User {
        testUsers[user.email] = user
        _currentUser.value = user
        return user
    }
    
    suspend fun logoutUser() {
        _currentUser.value = null
    }
    
    // Métodos específicos para créditos de IA
    suspend fun updateAiCredits(userId: String, credits: AiCredits) {
        val user = testUsers.values.find { it.id == userId }
        if (user != null) {
            val updatedUser = user.copy(aiCredits = credits)
            updateUser(updatedUser)
        }
    }
    
    suspend fun consumeAiCredit(userId: String): Boolean {
        val user = testUsers.values.find { it.id == userId }
        if (user != null && user.aiCredits.current > 0) {
            val updatedCredits = user.aiCredits.copy(
                current = user.aiCredits.current - 1,
                usedToday = user.aiCredits.usedToday + 1,
                totalSpent = user.aiCredits.totalSpent + 1
            )
            updateAiCredits(userId, updatedCredits)
            return true
        }
        return false
    }
    
    suspend fun addAiCredits(userId: String, amount: Int) {
        val user = testUsers.values.find { it.id == userId }
        if (user != null) {
            val updatedCredits = user.aiCredits.copy(
                current = user.aiCredits.current + amount,
                totalEarned = user.aiCredits.totalEarned + amount
            )
            updateAiCredits(userId, updatedCredits)
        }
    }
    
    // Resetar créditos diários
    suspend fun resetDailyCredits(userId: String) {
        val user = testUsers.values.find { it.id == userId }
        if (user != null) {
            val dailyLimit = when (user.subscription) {
                SubscriptionStatus.FREE -> AiCreditLimits.FREE_DAILY
                SubscriptionStatus.PREMIUM -> AiCreditLimits.PREMIUM_DAILY
                SubscriptionStatus.VIP -> AiCreditLimits.VIP_DAILY
            }
            
            val updatedCredits = user.aiCredits.copy(
                current = dailyLimit,
                dailyLimit = dailyLimit,
                usedToday = 0,
                lastResetDate = Date()
            )
            updateAiCredits(userId, updatedCredits)
        }
    }
    
    // Métodos para Discovery e Match
    suspend fun getAllUsers(): List<User> {
        return testUsers.values.toList()
    }
    
    suspend fun getUsersForDiscovery(currentUserId: String): List<User> {
        return testUsers.values.filter { it.id != currentUserId }.shuffled()
    }
    
    suspend fun getUserById(userId: String): User? {
        return testUsers.values.find { it.id == userId }
    }
    
    fun getTotalUsersCount(): Int {
        return testUsers.size
    }
} 
