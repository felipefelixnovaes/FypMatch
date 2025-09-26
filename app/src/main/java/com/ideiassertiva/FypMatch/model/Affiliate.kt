package com.ideiassertiva.FypMatch.model

import java.util.Date

/**
 * Modelo para o Sistema de Afiliados da Fase 6
 * Representa um afiliado e seus dados de comissão
 */
data class Affiliate(
    val id: String = "",
    val userId: String = "", // Usuário que se tornou afiliado
    val code: String = "", // Código único do afiliado (ex: "FYPMATCH_FELIPE123")
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val bankInfo: BankInfo = BankInfo(),
    val commission: CommissionConfig = CommissionConfig(),
    val stats: AffiliateStats = AffiliateStats(),
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * Informações bancárias do afiliado
 */
data class BankInfo(
    val bankName: String = "",
    val accountType: String = "", // "CONTA_CORRENTE", "CONTA_POUPANCA", "PIX"
    val agencyNumber: String = "",
    val accountNumber: String = "",
    val pixKey: String = "",
    val cpfCnpj: String = "",
    val isValidated: Boolean = false
)

/**
 * Configuração de comissões do afiliado
 */
data class CommissionConfig(
    val premiumFirstMonth: Double = 0.10, // 10% primeira mensalidade Premium
    val vipFirstMonth: Double = 0.15, // 15% primeira mensalidade VIP
    val renewalRate: Double = 0.05, // 5% renovações
    val volumeBonus: Double = 0.05, // +5% se 10+ referrals/mês
    val minimumPayout: Double = 50.0 // Saque mínimo R$ 50
)

/**
 * Estatísticas do afiliado
 */
data class AffiliateStats(
    val totalReferrals: Int = 0,
    val activeReferrals: Int = 0,
    val totalEarnings: Double = 0.0,
    val pendingEarnings: Double = 0.0,
    val paidEarnings: Double = 0.0,
    val monthlyReferrals: Int = 0,
    val monthlyEarnings: Double = 0.0,
    val bestMonth: Double = 0.0,
    val conversionRate: Double = 0.0, // % de clicks que viram assinatura
    val totalClicks: Int = 0
)

/**
 * Referral de um afiliado
 */
data class Referral(
    val id: String = "",
    val affiliateId: String = "",
    val affiliateCode: String = "",
    val referredUserId: String = "",
    val referredUserEmail: String = "",
    val subscriptionType: String = "", // "PREMIUM", "VIP"
    val subscriptionValue: Double = 0.0,
    val commissionEarned: Double = 0.0,
    val status: ReferralStatus = ReferralStatus.PENDING,
    val createdAt: Date = Date(),
    val paidAt: Date? = null
)

/**
 * Status do referral
 */
enum class ReferralStatus {
    PENDING, // Aguardando confirmação de pagamento
    CONFIRMED, // Confirmado, comissão calculada
    PAID, // Comissão paga ao afiliado
    CANCELLED // Cancelado (usuário cancelou assinatura nos primeiros 7 dias)
}

/**
 * Solicitação de saque
 */
data class PayoutRequest(
    val id: String = "",
    val affiliateId: String = "",
    val amount: Double = 0.0,
    val status: PayoutStatus = PayoutStatus.PENDING,
    val requestedAt: Date = Date(),
    val processedAt: Date? = null,
    val notes: String = "",
    val transactionId: String = ""
)

/**
 * Status do saque
 */
enum class PayoutStatus {
    PENDING, // Aguardando processamento
    APPROVED, // Aprovado pela administração
    PROCESSING, // Em processamento pelo sistema de pagamento
    COMPLETED, // Pago com sucesso
    REJECTED // Rejeitado
}