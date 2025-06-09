package com.ideiassertiva.FypMatch.data.repository

import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Date

class AdsRepository {
    
    // Estado dos anúncios
    private val _isAdLoading = MutableStateFlow(false)
    val isAdLoading: Flow<Boolean> = _isAdLoading.asStateFlow()
    
    private val _isAdReady = MutableStateFlow(true) // Simulado como sempre pronto
    val isAdReady: Flow<Boolean> = _isAdReady.asStateFlow()
    
    // Gerenciamento de créditos dos usuários
    private val _userCredits = MutableStateFlow<Map<String, AiCredits>>(emptyMap())
    val userCredits: Flow<Map<String, AiCredits>> = _userCredits.asStateFlow()
    
    // Inicializar créditos do usuário baseado na assinatura
    fun initializeUserCredits(userId: String, subscription: SubscriptionStatus): AiCredits {
        val dailyLimit = when (subscription) {
            SubscriptionStatus.FREE -> AiCreditLimits.FREE_DAILY
            SubscriptionStatus.PREMIUM -> AiCreditLimits.PREMIUM_DAILY
            SubscriptionStatus.VIP -> AiCreditLimits.VIP_DAILY
        }
        
        val existingCredits = _userCredits.value[userId]
        val today = Date()
        
        // Se é um novo dia, resetar créditos diários
        val credits = if (existingCredits != null && !isSameDay(existingCredits.lastResetDate, today)) {
            existingCredits.copy(
                current = dailyLimit,
                usedToday = 0,
                lastResetDate = today,
                dailyLimit = dailyLimit
            )
        } else if (existingCredits != null) {
            existingCredits.copy(dailyLimit = dailyLimit)
        } else {
            AiCredits(
                current = dailyLimit,
                dailyLimit = dailyLimit,
                lastResetDate = today
            )
        }
        
        _userCredits.value = _userCredits.value + (userId to credits)
        return credits
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
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Verificar se pode assistir anúncio
    fun canWatchAd(userId: String): Boolean {
        val credits = getUserCredits(userId)
        val adCreditsToday = credits.totalEarned - 
                           (_userCredits.value[userId]?.totalEarned ?: 0)
        
        return adCreditsToday < AiCreditLimits.MAX_AD_CREDITS_DAILY
    }
    
    // Simular carregamento e exibição de anúncio
    suspend fun showRewardedAd(userId: String): Result<Int> {
        return try {
            if (!canWatchAd(userId)) {
                return Result.failure(Exception("Limite diário de anúncios atingido"))
            }
            
            _isAdLoading.value = true
            
            // Simular carregamento do anúncio (1-3 segundos)
            delay((1000..3000).random().toLong())
            
            if (!_isAdReady.value) {
                _isAdLoading.value = false
                return Result.failure(Exception("Anúncio não disponível"))
            }
            
            // Simular exibição do anúncio (5-15 segundos)
            delay((5000..15000).random().toLong())
            
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
        val credits = getUserCredits(userId)
        val adCreditsToday = minOf(
            credits.totalEarned % AiCreditLimits.MAX_AD_CREDITS_DAILY,
            AiCreditLimits.MAX_AD_CREDITS_DAILY
        )
        
        return AdStats(
            adsWatchedToday = adCreditsToday / AiCreditLimits.AD_REWARD,
            maxAdsPerDay = AiCreditLimits.MAX_AD_CREDITS_DAILY / AiCreditLimits.AD_REWARD,
            creditsEarnedToday = adCreditsToday,
            canWatchMore = canWatchAd(userId)
        )
    }
}

data class AdStats(
    val adsWatchedToday: Int,
    val maxAdsPerDay: Int,
    val creditsEarnedToday: Int,
    val canWatchMore: Boolean
) 
