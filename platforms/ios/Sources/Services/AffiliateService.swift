// AffiliateService.swift — FypMatch iOS
// Serviço de afiliados — programa de indicação com comissões

import Foundation
import FirebaseFirestore
import ComposableArchitecture

// MARK: - Models

struct Affiliate: Codable, Equatable, Identifiable {
    var id: String
    var userId: String
    var code: String           // ex: "FYP_JOAO_123"
    var name: String
    var email: String
    var stats: AffiliateStats
    var commission: CommissionConfig
    var createdAt: Date

    enum CodingKeys: String, CodingKey {
        case id, userId, code, name, email, stats, commission, createdAt
    }
}

struct AffiliateStats: Codable, Equatable {
    var totalReferrals: Int
    var pendingEarnings: Double
    var totalEarnings: Double
    var monthlyReferrals: Int
    var monthlyEarnings: Double
    var conversionRate: Double   // 0.0–1.0
}

struct CommissionConfig: Codable, Equatable {
    var premiumFirstMonth: Double   // 0.20 = 20%
    var vipFirstMonth: Double       // 0.25 = 25%
    var minimumPayout: Double       // R$ 50,00
}

extension CommissionConfig {
    static let `default` = CommissionConfig(
        premiumFirstMonth: 0.20,
        vipFirstMonth: 0.25,
        minimumPayout: 50.00
    )
}

struct Referral: Codable, Equatable, Identifiable {
    var id: String
    var affiliateId: String
    var referredUserId: String
    var referredUserName: String
    var planPurchased: String?       // "premium" | "vip" | nil
    var commissionAmount: Double
    var status: ReferralStatus
    var createdAt: Date
}

enum ReferralStatus: String, Codable, Equatable {
    case pending    = "pending"
    case confirmed  = "confirmed"
    case paid       = "paid"
    case cancelled  = "cancelled"
}

struct PayoutRequest: Codable, Equatable, Identifiable {
    var id: String
    var affiliateId: String
    var amount: Double
    var status: PayoutStatus
    var requestedAt: Date
    var processedAt: Date?
}

enum PayoutStatus: String, Codable, Equatable {
    case pending    = "pending"
    case processing = "processing"
    case completed  = "completed"
    case rejected   = "rejected"
}

// MARK: - AffiliateService

struct AffiliateService {
    var registerAffiliate: (_ userId: String, _ name: String, _ email: String) async throws -> Affiliate
    var getAffiliate: (_ userId: String) async throws -> Affiliate?
    var getReferrals: (_ affiliateId: String) async throws -> [Referral]
    var requestPayout: (_ affiliateId: String, _ amount: Double) async throws -> PayoutRequest
    var generateLink: (_ code: String) -> String
}

// MARK: - Live Value

extension AffiliateService {
    static let liveValue = AffiliateService(
        registerAffiliate: { userId, name, email in
            let db = Firestore.firestore()
            let affiliateId = UUID().uuidString
            // Gera código único: FYP_PRIMEIRANOME_SUFIXO
            let firstName = name.components(separatedBy: " ").first?
                .uppercased()
                .folding(options: .diacriticInsensitive, locale: .current) ?? "USER"
            let suffix = String(affiliateId.prefix(6).uppercased())
            let code = "FYP_\(firstName)_\(suffix)"

            let affiliate = Affiliate(
                id: affiliateId,
                userId: userId,
                code: code,
                name: name,
                email: email,
                stats: AffiliateStats(
                    totalReferrals: 0,
                    pendingEarnings: 0,
                    totalEarnings: 0,
                    monthlyReferrals: 0,
                    monthlyEarnings: 0,
                    conversionRate: 0
                ),
                commission: .default,
                createdAt: Date()
            )

            let data = try Firestore.Encoder().encode(affiliate)
            try await db.collection("affiliates").document(affiliateId).setData(data)
            return affiliate
        },

        getAffiliate: { userId in
            let db = Firestore.firestore()
            let snapshot = try await db.collection("affiliates")
                .whereField("userId", isEqualTo: userId)
                .limit(to: 1)
                .getDocuments()
            guard let document = snapshot.documents.first else { return nil }
            return try Firestore.Decoder().decode(Affiliate.self, from: document.data())
        },

        getReferrals: { affiliateId in
            let db = Firestore.firestore()
            let snapshot = try await db.collection("referrals")
                .whereField("affiliateId", isEqualTo: affiliateId)
                .order(by: "createdAt", descending: true)
                .limit(to: 50)
                .getDocuments()
            return try snapshot.documents.compactMap { doc in
                try Firestore.Decoder().decode(Referral.self, from: doc.data())
            }
        },

        requestPayout: { affiliateId, amount in
            let db = Firestore.firestore()
            let payoutId = UUID().uuidString
            let payout = PayoutRequest(
                id: payoutId,
                affiliateId: affiliateId,
                amount: amount,
                status: .pending,
                requestedAt: Date(),
                processedAt: nil
            )
            let data = try Firestore.Encoder().encode(payout)
            try await db.collection("payoutRequests").document(payoutId).setData(data)
            return payout
        },

        generateLink: { code in
            "https://fypmatch.app/ref/\(code)"
        }
    )
}

// MARK: - TCA Dependency

private enum AffiliateServiceKey: DependencyKey {
    static let liveValue = AffiliateService.liveValue
}

extension DependencyValues {
    var affiliateService: AffiliateService {
        get { self[AffiliateServiceKey.self] }
        set { self[AffiliateServiceKey.self] = newValue }
    }
}
