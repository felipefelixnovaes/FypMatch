package com.ideiassertiva.FypMatch.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ListenerRegistration
import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscoveryRepository @Inject constructor(private val firestore: FirebaseFirestore) {

    private val compatibilityEngine = CompatibilityMLEngine()

    private val _discoveryCards = MutableStateFlow<List<DiscoveryCard>>(emptyList())
    val discoveryCards: Flow<List<DiscoveryCard>> = _discoveryCards.asStateFlow()

    private val _swipeActions = MutableStateFlow<List<SwipeRecord>>(emptyList())
    val swipeActions: Flow<List<SwipeRecord>> = _swipeActions.asStateFlow()

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: Flow<List<Match>> = _matches.asStateFlow()

    // Contador de novidades (curtidas recebidas + matches novos) para o badge do coração
    private val _newLikesCount = MutableStateFlow(0)
    val newLikesCount: StateFlow<Int> = _newLikesCount.asStateFlow()

    init {
        _newLikesCount.value = 0
    }

    // Perfis que curtiram o usuário — apenas dados reais.
    // TODO: consultar a coleção de likes no Firestore (toUser == currentUser). Sem mocks.
    fun getReceivedLikeProfiles(): List<User> = emptyList()

    // Zera o badge quando o usuário abre a tela de Curtidas
    fun markLikesSeen() {
        _newLikesCount.value = 0
    }

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
                    else doc.toFypUserOrNull(doc.id)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun loadCurrentUserFromFirestore(currentUserId: String): User? {
        if (currentUserId.isBlank()) return null
        return try {
            val doc = firestore.collection("users").document(currentUserId).get().await()
            if (doc.exists()) doc.toFypUserOrNull(doc.id) else null
        } catch (e: Exception) {
            null
        }
    }

    // IDs com quem o usuário já interagiu (curtiu/super/passou) — não devem reaparecer no deck
    private suspend fun loadActedUserIds(currentUserId: String): Set<String> {
        if (currentUserId.isBlank()) return emptySet()
        return try {
            val doc = firestore.collection("likes").document(currentUserId).get().await()
            val liked = (doc.get("liked") as? List<*>)?.filterIsInstance<String>().orEmpty()
            val passed = (doc.get("passed") as? List<*>)?.filterIsInstance<String>().orEmpty()
            (liked + passed).toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    // Carrega os cards de discovery para um usuário específico — apenas perfis reais do Firestore.
    suspend fun loadDiscoveryCards(currentUserId: String) {
        val acted = loadActedUserIds(currentUserId)
        val users = loadUsersFromFirestore(currentUserId).filter { it.id !in acted }
        val currentUser = loadCurrentUserFromFirestore(currentUserId)
        val cards = users.map { user ->
            val compatibilityScore = currentUser?.let {
                compatibilityEngine.analyzeCompatibility(it, user).overall
            } ?: ((60..98).random() / 100f)
            DiscoveryCard(
                user = user,
                distance = (1..25).random(),
                compatibilityScore = compatibilityScore,
                commonInterests = sharedInterests(currentUser, user).ifEmpty { user.profile.interests.take(2) },
                photos = user.profile.photos,
                isVerified = user.subscription != SubscriptionStatus.FREE
            )
        }.sortedByDescending { it.compatibilityScore }
        _discoveryCards.value = cards
    }

    private fun sharedInterests(currentUser: User?, targetUser: User): List<String> {
        if (currentUser == null) return emptyList()
        val currentTags = (currentUser.profile.interests + currentUser.profile.hobbies).toSet()
        return (targetUser.profile.interests + targetUser.profile.hobbies)
            .filter { it in currentTags }
            .distinct()
            .take(3)
    }

    // Registra like no Firestore e verifica match mútuo
    private suspend fun registerLikeAndCheckMatch(currentUserId: String, likedUserId: String): Boolean {
        return try {
            val likeData = mapOf(
                "liked" to FieldValue.arrayUnion(likedUserId),
                "updatedAt" to Date()
            )

            firestore.collection("likes")
                .document(currentUserId)
                .set(likeData, SetOptions.merge())
                .await()

            val doc = firestore.collection("likes").document(likedUserId).get().await()
            val likedByUser = doc.get("liked") as? List<*> ?: emptyList<String>()
            likedByUser.contains(currentUserId)
        } catch (e: Exception) {
            false
        }
    }

    // Registra "passar" no Firestore (para excluir do deck nas próximas cargas)
    private suspend fun registerPass(currentUserId: String, passedUserId: String) {
        try {
            firestore.collection("likes")
                .document(currentUserId)
                .set(
                    mapOf("passed" to FieldValue.arrayUnion(passedUserId), "updatedAt" to Date()),
                    SetOptions.merge()
                )
                .await()
        } catch (_: Exception) {
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
            
            // Persiste a ação no Firestore (like em "liked", pass em "passed") p/ não reaparecer no deck
            val mutualMatch = if (swipeType != SwipeType.PASS) {
                registerLikeAndCheckMatch(fromUserId, toUserId)
            } else {
                registerPass(fromUserId, toUserId)
                false
            }
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
                saveMatchToFirestore(newMatch)
                _newLikesCount.value += 1 // novo match → incrementa o badge
                newMatch
            } else null
            
            // Remover o card da lista de discovery
            _discoveryCards.value = _discoveryCards.value.filter { it.user.id != toUserId }
            
            Result.success(SwipeResult(isMatch, match, swipeType))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveMatchToFirestore(match: Match) {
        try {
            firestore.collection("matches")
                .document(match.id)
                .set(match)
                .await()
        } catch (_: Exception) {
            // The local match modal should still appear if the remote write is temporarily unavailable.
        }
    }
    
    // Obter cards para discovery
    fun getDiscoveryCards(): List<DiscoveryCard> {
        return _discoveryCards.value
    }

    fun getCachedUserById(userId: String): User? {
        return _discoveryCards.value
            .firstOrNull { it.user.id == userId }
            ?.user
    }
    
    // Obter matches do usuário
    fun getUserMatches(userId: String): List<Match> {
        return _matches.value.filter {
            (it.user1Id == userId || it.user2Id == userId) && it.isActive
        }
    }

    // Carrega matches reais do Firestore (ambos os lados: user1Id e user2Id).
    // Necessario para o OUTRO lado enxergar o match e para persistir entre sessoes
    // (antes os matches viviam so em memoria, populados apenas no swipe da sessao).
    suspend fun loadUserMatches(userId: String) {
        if (userId.isBlank()) return
        try {
            val asUser1 = firestore.collection("matches")
                .whereEqualTo("user1Id", userId).get().await().documents
            val asUser2 = firestore.collection("matches")
                .whereEqualTo("user2Id", userId).get().await().documents
            val loaded = (asUser1 + asUser2).mapNotNull { it.toObject(Match::class.java) }
            _matches.value = (_matches.value + loaded).distinctBy { it.id }
        } catch (_: Exception) {
        }
    }

    // ── Match em tempo real ──────────────────────────────────────────────────
    private var matchesListenerStarted = false
    private val matchListeners = mutableListOf<ListenerRegistration>()

    /**
     * Liga listeners em tempo real na colecao matches (dois lados: user1Id/user2Id).
     * Quando um match NOVO chega (ex.: o outro lado deu o like agora), atualiza
     * _matches e incrementa o badge do coracao na hora — notificacao in-app ao vivo.
     * Idempotente (so liga uma vez); a carga inicial nao notifica (sao matches antigos).
     */
    fun startMatchesListener(userId: String) {
        if (userId.isBlank() || matchesListenerStarted) return
        matchesListenerStarted = true
        listOf("user1Id", "user2Id").forEach { field ->
            var initialLoad = true
            val registration = firestore.collection("matches")
                .whereEqualTo(field, userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val loaded = snapshot.documents.mapNotNull { it.toObject(Match::class.java) }
                    val existingIds = _matches.value.map { it.id }.toSet()
                    val newOnes = loaded.filter { it.id !in existingIds }
                    if (newOnes.isNotEmpty()) {
                        _matches.value = (_matches.value + newOnes).distinctBy { it.id }
                        if (!initialLoad) {
                            _newLikesCount.value += newOnes.size
                        }
                    }
                    initialLoad = false
                }
            matchListeners.add(registration)
        }
    }

    // IDs das curtidas enviadas (like/super like) — para a tela de Curtidas
    fun getSentLikeIds(): List<String> =
        _swipeActions.value
            .filter { it.action != SwipeType.PASS }
            .map { it.toUserId }
            .distinct()

    // IDs dos usuários com quem deu match — para a tela de Curtidas
    fun getMatchUserIds(userId: String): List<String> =
        _matches.value.mapNotNull { match ->
            when (userId) {
                match.user1Id -> match.user2Id
                match.user2Id -> match.user1Id
                else -> null
            }
        }.distinct()
    
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
