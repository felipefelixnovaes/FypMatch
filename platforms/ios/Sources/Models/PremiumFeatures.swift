//
//  PremiumFeatures.swift
//  FypMatch iOS
//
//  Modelo para features premium e monetização
//  Gerencia assinaturas e recursos pagos
//

import Foundation
import StoreKit

// MARK: - Premium Subscription

/// Modelo de assinatura premium
struct PremiumSubscription: Codable, Identifiable, Equatable {
    let id: String
    let userId: String
    let plan: PremiumPlan
    let status: SubscriptionStatus
    let startDate: Date
    let expirationDate: Date
    let autoRenew: Bool
    let transactionId: String?
    let receiptData: String?
    let createdAt: Date
    let updatedAt: Date
    
    init(
        id: String = UUID().uuidString,
        userId: String,
        plan: PremiumPlan,
        status: SubscriptionStatus = .active,
        startDate: Date = Date(),
        expirationDate: Date,
        autoRenew: Bool = true,
        transactionId: String? = nil,
        receiptData: String? = nil,
        createdAt: Date = Date(),
        updatedAt: Date = Date()
    ) {
        self.id = id
        self.userId = userId
        self.plan = plan
        self.status = status
        self.startDate = startDate
        self.expirationDate = expirationDate
        self.autoRenew = autoRenew
        self.transactionId = transactionId
        self.receiptData = receiptData
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
    
    var isActive: Bool {
        return status == .active && expirationDate > Date()
    }
    
    var daysRemaining: Int {
        let calendar = Calendar.current
        let days = calendar.dateComponents([.day], from: Date(), to: expirationDate).day ?? 0
        return max(0, days)
    }
}

// MARK: - Premium Plan

enum PremiumPlan: String, Codable, CaseIterable {
    case monthly = "premium_monthly"
    case quarterly = "premium_quarterly"
    case yearly = "premium_yearly"
    case lifetime = "premium_lifetime"
    
    var displayName: String {
        switch self {
        case .monthly: return "Premium Mensal"
        case .quarterly: return "Premium Trimestral"
        case .yearly: return "Premium Anual"
        case .lifetime: return "Premium Vitalício"
        }
    }
    
    var duration: TimeInterval {
        switch self {
        case .monthly: return 30 * 24 * 60 * 60
        case .quarterly: return 90 * 24 * 60 * 60
        case .yearly: return 365 * 24 * 60 * 60
        case .lifetime: return .infinity
        }
    }
    
    var priceInBRL: Double {
        switch self {
        case .monthly: return 29.90
        case .quarterly: return 69.90
        case .yearly: return 199.90
        case .lifetime: return 599.90
        }
    }
    
    var discountPercentage: Int? {
        switch self {
        case .monthly: return nil
        case .quarterly: return 22
        case .yearly: return 44
        case .lifetime: return nil
        }
    }
    
    var productId: String {
        return rawValue
    }
}

// MARK: - Subscription Status

enum SubscriptionStatus: String, Codable {
    case active = "active"
    case expired = "expired"
    case cancelled = "cancelled"
    case paused = "paused"
    case trial = "trial"
    
    var displayName: String {
        switch self {
        case .active: return "Ativo"
        case .expired: return "Expirado"
        case .cancelled: return "Cancelado"
        case .paused: return "Pausado"
        case .trial: return "Período de teste"
        }
    }
}

// MARK: - Premium Features List

struct PremiumFeaturesList {
    static let allFeatures: [PremiumFeature] = [
        .unlimitedSwipes,
        .undoSwipes,
        .superLikes,
        .boost,
        .seeWhoLikesYou,
        .readReceipts,
        .advancedFilters,
        .noAds,
        .priorityMatching,
        .incognitoMode
    ]
}

enum PremiumFeature: String, Codable, CaseIterable {
    case unlimitedSwipes = "unlimited_swipes"
    case undoSwipes = "undo_swipes"
    case superLikes = "super_likes"
    case boost = "boost"
    case seeWhoLikesYou = "see_who_likes_you"
    case readReceipts = "read_receipts"
    case advancedFilters = "advanced_filters"
    case noAds = "no_ads"
    case priorityMatching = "priority_matching"
    case incognitoMode = "incognito_mode"
    
    var displayName: String {
        switch self {
        case .unlimitedSwipes: return "Curtidas Ilimitadas"
        case .undoSwipes: return "Desfazer Swipes"
        case .superLikes: return "Super Likes Ilimitados"
        case .boost: return "Boost Mensal"
        case .seeWhoLikesYou: return "Ver Quem Te Curtiu"
        case .readReceipts: return "Confirmação de Leitura"
        case .advancedFilters: return "Filtros Avançados"
        case .noAds: return "Sem Anúncios"
        case .priorityMatching: return "Matching Prioritário"
        case .incognitoMode: return "Modo Invisível"
        }
    }
    
    var description: String {
        switch self {
        case .unlimitedSwipes:
            return "Curta quantos perfis quiser sem limites diários"
        case .undoSwipes:
            return "Desfaça seu último swipe se mudou de ideia"
        case .superLikes:
            return "Destaque-se e mostre interesse especial ilimitadamente"
        case .boost:
            return "Seja visto por mais pessoas uma vez por mês"
        case .seeWhoLikesYou:
            return "Veja quem já te curtiu antes de dar match"
        case .readReceipts:
            return "Saiba quando suas mensagens foram lidas"
        case .advancedFilters:
            return "Filtre por educação, altura, estilo de vida e mais"
        case .noAds:
            return "Navegue sem interrupções de anúncios"
        case .priorityMatching:
            return "Apareça primeiro na fila de descoberta"
        case .incognitoMode:
            return "Navegue anonimamente, apareça apenas para quem você curtir"
        }
    }
    
    var iconName: String {
        switch self {
        case .unlimitedSwipes: return "infinity"
        case .undoSwipes: return "arrow.uturn.left"
        case .superLikes: return "star.fill"
        case .boost: return "bolt.fill"
        case .seeWhoLikesYou: return "eye.fill"
        case .readReceipts: return "checkmark.circle.fill"
        case .advancedFilters: return "slider.horizontal.3"
        case .noAds: return "nosign"
        case .priorityMatching: return "flag.fill"
        case .incognitoMode: return "eye.slash.fill"
        }
    }
}

// MARK: - In-App Purchase Item

struct PremiumPurchaseItem: Identifiable {
    let id: String
    let plan: PremiumPlan
    let product: Product?
    
    var price: String {
        if let product = product {
            return product.displayPrice
        }
        return String(format: "R$ %.2f", plan.priceInBRL)
    }
    
    var pricePerMonth: String {
        let monthlyPrice: Double
        switch plan {
        case .monthly:
            monthlyPrice = plan.priceInBRL
        case .quarterly:
            monthlyPrice = plan.priceInBRL / 3
        case .yearly:
            monthlyPrice = plan.priceInBRL / 12
        case .lifetime:
            return "Único"
        }
        return String(format: "R$ %.2f/mês", monthlyPrice)
    }
}

// MARK: - Purchase Result

enum PurchaseResult {
    case success(PremiumSubscription)
    case failure(PurchaseError)
    case cancelled
    case pending
}

enum PurchaseError: Error, LocalizedError {
    case productNotFound
    case purchaseFailed
    case verificationFailed
    case networkError
    case userCancelled
    
    var errorDescription: String? {
        switch self {
        case .productNotFound:
            return "Produto não encontrado"
        case .purchaseFailed:
            return "Falha na compra"
        case .verificationFailed:
            return "Falha na verificação da compra"
        case .networkError:
            return "Erro de conexão"
        case .userCancelled:
            return "Compra cancelada pelo usuário"
        }
    }
}

// MARK: - Boost

struct Boost: Codable, Identifiable {
    let id: String
    let userId: String
    let startTime: Date
    let duration: TimeInterval
    let isActive: Bool
    let impressions: Int
    let matches: Int
    let createdAt: Date
    
    init(
        id: String = UUID().uuidString,
        userId: String,
        startTime: Date = Date(),
        duration: TimeInterval = 1800, // 30 minutes
        isActive: Bool = true,
        impressions: Int = 0,
        matches: Int = 0,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.userId = userId
        self.startTime = startTime
        self.duration = duration
        self.isActive = isActive
        self.impressions = impressions
        self.matches = matches
        self.createdAt = createdAt
    }
    
    var endTime: Date {
        return startTime.addingTimeInterval(duration)
    }
    
    var remainingTime: TimeInterval {
        return max(0, endTime.timeIntervalSince(Date()))
    }
    
    var isExpired: Bool {
        return Date() >= endTime
    }
}

// MARK: - Super Like

struct SuperLike: Codable, Identifiable {
    let id: String
    let senderId: String
    let receiverId: String
    let sentAt: Date
    let isViewed: Bool
    let viewedAt: Date?
    let resultedInMatch: Bool
    let matchedAt: Date?
    
    init(
        id: String = UUID().uuidString,
        senderId: String,
        receiverId: String,
        sentAt: Date = Date(),
        isViewed: Bool = false,
        viewedAt: Date? = nil,
        resultedInMatch: Bool = false,
        matchedAt: Date? = nil
    ) {
        self.id = id
        self.senderId = senderId
        self.receiverId = receiverId
        self.sentAt = sentAt
        self.isViewed = isViewed
        self.viewedAt = viewedAt
        self.resultedInMatch = resultedInMatch
        self.matchedAt = matchedAt
    }
}
