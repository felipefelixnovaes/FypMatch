package com.ideiassertiva.FypMatch.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscoveryRepository @Inject constructor(private val firestore: FirebaseFirestore) {

    private val _discoveryCards = MutableStateFlow<List<DiscoveryCard>>(emptyList())
    val discoveryCards: Flow<List<DiscoveryCard>> = _discoveryCards.asStateFlow()

    private val _swipeActions = MutableStateFlow<List<SwipeRecord>>(emptyList())
    val swipeActions: Flow<List<SwipeRecord>> = _swipeActions.asStateFlow()

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: Flow<List<Match>> = _matches.asStateFlow()

    // Carrega usuários reais do Firestore (excluindo o próprio usuário corrente)
    private suspend fun loadUsersFromFirestore(currentUserId: String): List<User> {
        return try {
            firestore.collection("users")
                .limit(20)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    if (doc.id == currentUserId) null
                    else doc.toObject(User::class.java)?.copy(id = doc.id)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Carrega os cards de discovery para um usuário específico
    suspend fun loadDiscoveryCards(currentUserId: String) {
        val users = loadUsersFromFirestore(currentUserId)
        val cards = users.map { user ->
            DiscoveryCard(
                user = user,
                distance = 0,
                compatibilityScore = 0f,
                commonInterests = emptyList(),
                photos = user.profile.photos,
                isVerified = false
            )
        }
        _discoveryCards.value = cards
    }

    // Registra like no Firestore e verifica match mútuo
    private suspend fun registerLikeAndCheckMatch(currentUserId: String, likedUserId: String): Boolean {
        return try {
            // Registra que currentUser deu like em likedUser
            firestore.collection("likes")
                .document(currentUserId)
                .update("liked", FieldValue.arrayUnion(likedUserId))
                .await()
            // Verifica se likedUser também deu like em currentUser
            val doc = firestore.collection("likes").document(likedUserId).get().await()
            val likedByUser = doc.get("liked") as? List<*> ?: emptyList<String>()
            likedByUser.contains(currentUserId)
        } catch (e: Exception) {
            false
        }
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
            
            // Registra like no Firestore e verifica match mútuo
            val mutualMatch = if (swipeType != SwipeType.PASS) {
                registerLikeAndCheckMatch(fromUserId, toUserId)
            } else false
            val isMatch = existingSwipe != null || mutualMatch
            
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
