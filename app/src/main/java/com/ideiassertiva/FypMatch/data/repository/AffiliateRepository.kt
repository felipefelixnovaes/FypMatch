package com.ideiassertiva.FypMatch.data.repository

import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import kotlin.random.Random

/**
 * Repository para o Sistema de Afiliados - Fase 6
 * Gerencia afiliados, referrals e comissões
 */
class AffiliateRepository {
    
    private val _affiliates = MutableStateFlow<List<Affiliate>>(emptyList())
    val affiliates: Flow<List<Affiliate>> = _affiliates.asStateFlow()
    
    private val _currentAffiliate = MutableStateFlow<Affiliate?>(null)
    val currentAffiliate: Flow<Affiliate?> = _currentAffiliate.asStateFlow()
    
    private val _referrals = MutableStateFlow<List<Referral>>(emptyList())
    val referrals: Flow<List<Referral>> = _referrals.asStateFlow()
    
    private val _payoutRequests = MutableStateFlow<List<PayoutRequest>>(emptyList())
    val payoutRequests: Flow<List<PayoutRequest>> = _payoutRequests.asStateFlow()
    
    /**
     * Registra um novo afiliado
     */
    suspend fun registerAffiliate(
        userId: String,
        name: String,
        email: String,
        phoneNumber: String = ""
    ): Result<Affiliate> {
        return try {
            val affiliateCode = generateAffiliateCode(name)
            
            val newAffiliate = Affiliate(
                id = UUID.randomUUID().toString(),
                userId = userId,
                code = affiliateCode,
                name = name,
                email = email,
                phoneNumber = phoneNumber,
                createdAt = Date(),
                updatedAt = Date()
            )
            
            // Simular salvamento no Firebase
            val currentList = _affiliates.value.toMutableList()
            currentList.add(newAffiliate)
            _affiliates.value = currentList
            
            Result.success(newAffiliate)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Gera código único do afiliado
     */
    private fun generateAffiliateCode(name: String): String {
        val cleanName = name.replace(" ", "").uppercase().take(8)
        val randomSuffix = Random.nextInt(100, 999)
        return "FYP_${cleanName}_$randomSuffix"
    }
    
    /**
     * Busca afiliado por código
     */
    suspend fun getAffiliateByCode(code: String): Affiliate? {
        return _affiliates.value.find { it.code == code }
    }
    
    /**
     * Registra um novo referral
     */
    suspend fun registerReferral(
        affiliateCode: String,
        referredUserId: String,
        referredUserEmail: String,
        subscriptionType: String,
        subscriptionValue: Double
    ): Result<Referral> {
        return try {
            val affiliate = getAffiliateByCode(affiliateCode)
                ?: return Result.failure(Exception("Código de afiliado não encontrado"))
            
            val commission = calculateCommission(subscriptionType, subscriptionValue, affiliate.commission)
            
            val referral = Referral(
                id = UUID.randomUUID().toString(),
                affiliateId = affiliate.id,
                affiliateCode = affiliateCode,
                referredUserId = referredUserId,
                referredUserEmail = referredUserEmail,
                subscriptionType = subscriptionType,
                subscriptionValue = subscriptionValue,
                commissionEarned = commission,
                status = ReferralStatus.PENDING,
                createdAt = Date()
            )
            
            // Simular salvamento
            val currentReferrals = _referrals.value.toMutableList()
            currentReferrals.add(referral)
            _referrals.value = currentReferrals
            
            // Atualizar stats do afiliado
            updateAffiliateStats(affiliate.id, commission)
            
            Result.success(referral)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Calcula comissão baseada no tipo de assinatura
     */
    private fun calculateCommission(
        subscriptionType: String,
        subscriptionValue: Double,
        config: CommissionConfig
    ): Double {
        return when (subscriptionType) {
            "PREMIUM" -> subscriptionValue * config.premiumFirstMonth
            "VIP" -> subscriptionValue * config.vipFirstMonth
            else -> 0.0
        }
    }
    
    /**
     * Atualiza estatísticas do afiliado
     */
    private suspend fun updateAffiliateStats(affiliateId: String, newCommission: Double) {
        val currentAffiliates = _affiliates.value.toMutableList()
        val affiliateIndex = currentAffiliates.indexOfFirst { it.id == affiliateId }
        
        if (affiliateIndex != -1) {
            val affiliate = currentAffiliates[affiliateIndex]
            val updatedStats = affiliate.stats.copy(
                totalReferrals = affiliate.stats.totalReferrals + 1,
                pendingEarnings = affiliate.stats.pendingEarnings + newCommission,
                monthlyReferrals = affiliate.stats.monthlyReferrals + 1,
                monthlyEarnings = affiliate.stats.monthlyEarnings + newCommission
            )
            
            currentAffiliates[affiliateIndex] = affiliate.copy(
                stats = updatedStats,
                updatedAt = Date()
            )
            
            _affiliates.value = currentAffiliates
        }
    }
    
    /**
     * Solicita saque de comissões
     */
    suspend fun requestPayout(affiliateId: String, amount: Double): Result<PayoutRequest> {
        return try {
            val affiliate = _affiliates.value.find { it.id == affiliateId }
                ?: return Result.failure(Exception("Afiliado não encontrado"))
            
            if (amount < affiliate.commission.minimumPayout) {
                return Result.failure(Exception("Valor mínimo para saque é R$ ${affiliate.commission.minimumPayout}"))
            }
            
            if (amount > affiliate.stats.pendingEarnings) {
                return Result.failure(Exception("Valor solicitado maior que o disponível"))
            }
            
            val payoutRequest = PayoutRequest(
                id = UUID.randomUUID().toString(),
                affiliateId = affiliateId,
                amount = amount,
                status = PayoutStatus.PENDING,
                requestedAt = Date()
            )
            
            val currentRequests = _payoutRequests.value.toMutableList()
            currentRequests.add(payoutRequest)
            _payoutRequests.value = currentRequests
            
            Result.success(payoutRequest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obter estatísticas do dashboard
     */
    suspend fun getDashboardStats(affiliateId: String): AffiliateStats? {
        return _affiliates.value.find { it.id == affiliateId }?.stats
    }
    
    /**
     * Confirma pagamento de um referral (para admin)
     */
    suspend fun confirmReferralPayment(referralId: String): Result<Boolean> {
        return try {
            val currentReferrals = _referrals.value.toMutableList()
            val referralIndex = currentReferrals.indexOfFirst { it.id == referralId }
            
            if (referralIndex != -1) {
                val referral = currentReferrals[referralIndex]
                currentReferrals[referralIndex] = referral.copy(
                    status = ReferralStatus.CONFIRMED,
                    paidAt = Date()
                )
                _referrals.value = currentReferrals
                
                // Mover de pending para total earnings
                moveEarningsFromPendingToTotal(referral.affiliateId, referral.commissionEarned)
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun moveEarningsFromPendingToTotal(affiliateId: String, amount: Double) {
        val currentAffiliates = _affiliates.value.toMutableList()
        val affiliateIndex = currentAffiliates.indexOfFirst { it.id == affiliateId }
        
        if (affiliateIndex != -1) {
            val affiliate = currentAffiliates[affiliateIndex]
            val updatedStats = affiliate.stats.copy(
                pendingEarnings = affiliate.stats.pendingEarnings - amount,
                totalEarnings = affiliate.stats.totalEarnings + amount
            )
            
            currentAffiliates[affiliateIndex] = affiliate.copy(stats = updatedStats)
            _affiliates.value = currentAffiliates
        }
    }
}