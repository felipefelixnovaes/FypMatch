package com.ideiassertiva.FypMatch.data.repository

import android.app.Activity
import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsRepository @Inject constructor(
    private val userRepository: UserRepository?,
    private val rewardedAdGateway: RewardedAdGateway = ImmediateRewardedAdGateway()
) {
    constructor() : this(null)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Estado dos anúncios
    private val _isAdLoading = MutableStateFlow(false)
    val isAdLoading: Flow<Boolean> = _isAdLoading.asStateFlow()

    private val _isAdReady = MutableStateFlow(true) // Simulado como sempre pronto
    val isAdReady: Flow<Boolean> = _isAdReady.asStateFlow()

    // Gerenciamento de créditos dos usuários
    private val _userCredits = MutableStateFlow<Map<String, AiCredits>>(emptyMap())
    val userCredits: Flow<Map<String, AiCredits>> = _userCredits.asStateFlow()

    // Rastreia anúncios assistidos hoje por usuário (userId -> count)
    private val _adsWatchedToday = MutableStateFlow<Map<String, Int>>(emptyMap())

    // Data do último reset de anúncios por usuário
    private val _lastAdResetDate = MutableStateFlow<Map<String, Date>>(emptyMap())

    // Inicializar créditos do usuário baseado na assinatura + Firestore
    suspend fun initializeUserCredits(userId: String, subscription: SubscriptionStatus): AiCredits {
        val dailyLimit = when (subscription) {
            SubscriptionStatus.FREE -> AiCreditLimits.FREE_DAILY
            SubscriptionStatus.PREMIUM -> AiCreditLimits.PREMIUM_DAILY
            SubscriptionStatus.VIP -> AiCreditLimits.VIP_DAILY
        }

        val existingCredits = _userCredits.value[userId]
        val today = Date()

        // Tentar carregar créditos salvos no Firestore
        val firestoreCredits = try {
            userRepository?.getUserById(userId)?.aiCredits
        } catch (_: Exception) { null }

        val credits = when {
            // Créditos no Firestore com totalEarned > 0 usa os do Firestore
            firestoreCredits != null && firestoreCredits.totalEarned > 0 -> {
                if (!isSameDay(firestoreCredits.lastResetDate, today)) {
                    firestoreCredits.copy(
                        current = firestoreCredits.current.coerceAtLeast(dailyLimit),
                        dailyLimit = dailyLimit,
                        usedToday = 0,
                        lastResetDate = today
                    )
                } else {
                    firestoreCredits.copy(dailyLimit = dailyLimit)
                }
            }
            // Reset diário se necessário
            existingCredits != null && !isSameDay(existingCredits.lastResetDate, today) ->
                existingCredits.copy(
                    current = dailyLimit,
                    usedToday = 0,
                    lastResetDate = today,
                    dailyLimit = dailyLimit
                )
            existingCredits != null ->
                existingCredits.copy(dailyLimit = dailyLimit)
            else ->
                AiCredits(
                    current = dailyLimit,
                    dailyLimit = dailyLimit,
                    lastResetDate = today
                )
        }

        _userCredits.value = _userCredits.value + (userId to credits)
        persistCredits(userId, credits)
        return credits
    }

    private fun persistCredits(userId: String, credits: AiCredits) {
        scope.launch {
            try {
                userRepository?.updateUser(userId, mapOf("aiCredits" to credits))
            } catch (_: Exception) { }
        }
    }

    // Obter créditos atuais do usuário
    fun getUserCredits(userId: String): AiCredits {
        return _userCredits.value[userId] ?: AiCredits()
    }

    // Verificar se usuário pode enviar mensagem
    fun canSendMessage(userId: String): Boolean {
        val credits = getUserCredits(userId)
        return credits.current >= AiCreditLimits.COST_PER_MESSAGE
    }

    // Consumir créditos ao enviar mensagem
    suspend fun consumeCreditsForMessage(userId: String): Result<Unit> {
        return try {
            val credits = getUserCredits(userId)

            if (credits.current < AiCreditLimits.COST_PER_MESSAGE) {
                return Result.failure(Exception("Créditos insuficientes"))
            }

            val updatedCredits = credits.copy(
                current = credits.current - AiCreditLimits.COST_PER_MESSAGE,
                usedToday = credits.usedToday + AiCreditLimits.COST_PER_MESSAGE,
                totalSpent = credits.totalSpent + AiCreditLimits.COST_PER_MESSAGE
            )

            _userCredits.value = _userCredits.value + (userId to updatedCredits)
            persistCredits(userId, updatedCredits)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Resetar contagem diária de anúncios se o dia mudou
    private fun resetDailyAdsIfNeeded(userId: String) {
        val lastReset = _lastAdResetDate.value[userId]
        val today = Date()
        if (lastReset == null || !isSameDay(lastReset, today)) {
            _adsWatchedToday.value = _adsWatchedToday.value + (userId to 0)
            _lastAdResetDate.value = _lastAdResetDate.value + (userId to today)
        }
    }

    // Verificar se pode assistir anúncio (fix: rastreia ads assistidos hoje separadamente)
    fun canWatchAd(userId: String): Boolean {
        resetDailyAdsIfNeeded(userId)
        val maxAdsPerDay = AiCreditLimits.MAX_AD_CREDITS_DAILY / AiCreditLimits.AD_REWARD
        val watchedToday = _adsWatchedToday.value[userId] ?: 0
        return watchedToday < maxAdsPerDay
    }

    // Exibe anúncio recompensado real; só credita após o callback de recompensa.
    suspend fun showRewardedAd(userId: String, activity: Activity? = null): Result<Int> {
        return try {
            if (!canWatchAd(userId)) {
                return Result.failure(Exception("Limite diário de anúncios atingido"))
            }

            _isAdLoading.value = true

            val adResult = rewardedAdGateway.showRewardedAd(activity)
            if (adResult.isFailure) {
                _isAdLoading.value = false
                return Result.failure(
                    adResult.exceptionOrNull() ?: Exception("Anúncio não disponível")
                )
            }

            // Incrementar contador de anúncios hoje
            val currentCount = _adsWatchedToday.value[userId] ?: 0
            _adsWatchedToday.value = _adsWatchedToday.value + (userId to (currentCount + 1))

            // Recompensar com créditos
            val earnedCredits = rewardCredits(userId, AiCreditLimits.AD_REWARD)

            _isAdLoading.value = false
            Result.success(earnedCredits)
        } catch (e: Exception) {
            _isAdLoading.value = false
            Result.failure(e)
        }
    }

    // Adicionar créditos por recompensa
    private fun rewardCredits(userId: String, amount: Int): Int {
        val credits = getUserCredits(userId)
        val updatedCredits = credits.copy(
            current = credits.current + amount,
            totalEarned = credits.totalEarned + amount
        )

        _userCredits.value = _userCredits.value + (userId to updatedCredits)
        persistCredits(userId, updatedCredits)
        return amount
    }

    // Verificar se é o mesmo dia
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    // Obter estatísticas de anúncios
    fun getAdStats(userId: String): AdStats {
        resetDailyAdsIfNeeded(userId)
        val maxAdsPerDay = AiCreditLimits.MAX_AD_CREDITS_DAILY / AiCreditLimits.AD_REWARD
        val watchedToday = _adsWatchedToday.value[userId] ?: 0
        val creditsEarnedToday = watchedToday * AiCreditLimits.AD_REWARD

        return AdStats(
            adsWatchedToday = watchedToday,
            maxAdsPerDay = maxAdsPerDay,
            creditsEarnedToday = creditsEarnedToday,
            canWatchMore = watchedToday < maxAdsPerDay
        )
    }
}

data class AdStats(
    val adsWatchedToday: Int,
    val maxAdsPerDay: Int,
    val creditsEarnedToday: Int,
    val canWatchMore: Boolean
)
