package com.ideiassertiva.FypMatch.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ideiassertivas.FypMatch.model.Affiliate
import com.ideiassertivas.FypMatch.model.AffiliateStats
import com.ideiassertivas.FypMatch.model.CommissionConfig
import com.ideiassertivas.FypMatch.model.PayoutRequest
import com.ideiassertivas.FypMatch.model.PayoutStatus
import com.ideiassertivas.FypMatch.model.Referral
import com.ideiassertivas.FypMatch.model.ReferralStatus
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Repository para o Sistema de Afiliados - Fase 6
 * Gerencia afiliados, referrals e comissões com persistência real no Firestore
 */
@Singleton
class AffiliateRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val affiliatesCollection = firestore.collection("affiliates")
    private val referralsCollection = firestore.collection("referrals")
    private val payoutsCollection = firestore.collection("payouts")

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

            affiliatesCollection.document(newAffiliate.id).set(newAffiliate).await()
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
     * Salva/atualiza um afiliado no Firestore
     */
    suspend fun saveAffiliate(affiliate: Affiliate): Result<Unit> {
        return try {
            affiliatesCollection.document(affiliate.id).set(affiliate).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Busca afiliado por ID
     */
    suspend fun getAffiliate(affiliateId: String): Affiliate? {
        return try {
            val doc = affiliatesCollection.document(affiliateId).get().await()
            if (doc.exists()) doc.toObject(Affiliate::class.java)?.copy(id = doc.id) else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Busca afiliado por código
     */
    suspend fun getAffiliateByCode(code: String): Affiliate? {
        return try {
            val snapshot = affiliatesCollection
                .whereEqualTo("code", code)
                .limit(1)
                .get()
                .await()
            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                doc.toObject(Affiliate::class.java)?.copy(id = doc.id)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Registra um novo referral com persistência real
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

            referralsCollection.document(referral.id).set(referral).await()

            // Atualizar stats do afiliado no Firestore
            affiliatesCollection.document(affiliate.id).update(
                mapOf(
                    "stats.totalReferrals" to FieldValue.increment(1),
                    "stats.pendingEarnings" to FieldValue.increment(commission),
                    "stats.monthlyReferrals" to FieldValue.increment(1),
                    "stats.monthlyEarnings" to FieldValue.increment(commission),
                    "updatedAt" to Date()
                )
            ).await()

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
     * Solicita saque de comissões
     */
    suspend fun requestPayout(affiliateId: String, amount: Double): Result<PayoutRequest> {
        return try {
            val affiliate = getAffiliate(affiliateId)
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

            payoutsCollection.document(payoutRequest.id).set(payoutRequest).await()
            Result.success(payoutRequest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obter estatísticas do dashboard
     */
    suspend fun getDashboardStats(affiliateId: String): AffiliateStats? {
        return getAffiliate(affiliateId)?.stats
    }

    /**
     * Confirma pagamento de um referral (para admin)
     */
    suspend fun confirmReferralPayment(referralId: String): Result<Boolean> {
        return try {
            val doc = referralsCollection.document(referralId).get().await()
            val referral = doc.toObject(Referral::class.java)
                ?: return Result.failure(Exception("Referral não encontrado"))

            referralsCollection.document(referralId).update(
                mapOf(
                    "status" to ReferralStatus.CONFIRMED.name,
                    "paidAt" to Date()
                )
            ).await()

            // Mover de pendingEarnings para totalEarnings
            affiliatesCollection.document(referral.affiliateId).update(
                mapOf(
                    "stats.pendingEarnings" to FieldValue.increment(-referral.commissionEarned),
                    "stats.totalEarnings" to FieldValue.increment(referral.commissionEarned)
                )
            ).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
