//
//  Match.swift
//  FypMatch iOS
//
//  Modelo para gerenciar matches entre usuários
//  Port do sistema de matching do Android
//

import Foundation
import FirebaseFirestore

/// Representa um match entre dois usuários
struct Match: Codable, Identifiable, Equatable {
    // MARK: - Properties
    let id: String
    let user1Id: String
    let user2Id: String
    let user1: User?
    let user2: User?
    let matchedAt: Date
    let isActive: Bool
    let conversationId: String?
    
    // MARK: - Match Metadata
    let user1SwipedAt: Date
    let user2SwipedAt: Date
    let compatibilityScore: Double
    let matchType: MatchType
    
    // MARK: - Interaction Data
    let hasUser1ViewedMatch: Bool
    let hasUser2ViewedMatch: Bool
    let lastInteractionAt: Date?
    let isBlocked: Bool
    let blockedBy: String?
    let blockedAt: Date?
    
    // MARK: - System Data
    let createdAt: Date
    let updatedAt: Date
    
    // MARK: - Initializer
    init(
        id: String = UUID().uuidString,
        user1Id: String,
        user2Id: String,
        user1: User? = nil,
        user2: User? = nil,
        matchedAt: Date = Date(),
        isActive: Bool = true,
        conversationId: String? = nil,
        user1SwipedAt: Date,
        user2SwipedAt: Date,
        compatibilityScore: Double = 0.0,
        matchType: MatchType = .regular,
        hasUser1ViewedMatch: Bool = false,
        hasUser2ViewedMatch: Bool = false,
        lastInteractionAt: Date? = nil,
        isBlocked: Bool = false,
        blockedBy: String? = nil,
        blockedAt: Date? = nil,
        createdAt: Date = Date(),
        updatedAt: Date = Date()
    ) {
        self.id = id
        self.user1Id = user1Id
        self.user2Id = user2Id
        self.user1 = user1
        self.user2 = user2
        self.matchedAt = matchedAt
        self.isActive = isActive
        self.conversationId = conversationId
        self.user1SwipedAt = user1SwipedAt
        self.user2SwipedAt = user2SwipedAt
        self.compatibilityScore = compatibilityScore
        self.matchType = matchType
        self.hasUser1ViewedMatch = hasUser1ViewedMatch
        self.hasUser2ViewedMatch = hasUser2ViewedMatch
        self.lastInteractionAt = lastInteractionAt
        self.isBlocked = isBlocked
        self.blockedBy = blockedBy
        self.blockedAt = blockedAt
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
}

// MARK: - Match Extensions

extension Match {
    /// Retorna o outro usuário do match (que não é o usuário atual)
    func getOtherUser(currentUserId: String) -> User? {
        if user1Id == currentUserId {
            return user2
        } else if user2Id == currentUserId {
            return user1
        }
        return nil
    }
    
    /// Retorna o ID do outro usuário
    func getOtherUserId(currentUserId: String) -> String? {
        if user1Id == currentUserId {
            return user2Id
        } else if user2Id == currentUserId {
            return user1Id
        }
        return nil
    }
    
    /// Verifica se o usuário atual já visualizou o match
    func hasUserViewedMatch(userId: String) -> Bool {
        if user1Id == userId {
            return hasUser1ViewedMatch
        } else if user2Id == userId {
            return hasUser2ViewedMatch
        }
        return false
    }
    
    /// Retorna há quanto tempo o match foi feito
    var timeAgoString: String {
        let interval = Date().timeIntervalSince(matchedAt)
        
        if interval < 60 {
            return "Agora"
        } else if interval < 3600 {
            let minutes = Int(interval / 60)
            return "\(minutes)min"
        } else if interval < 86400 {
            let hours = Int(interval / 3600)
            return "\(hours)h"
        } else {
            let days = Int(interval / 86400)
            return "\(days)d"
        }
    }
    
    /// Retorna se é um match novo (menos de 24h)
    var isNewMatch: Bool {
        let interval = Date().timeIntervalSince(matchedAt)
        return interval < 86400 // 24 horas
    }
    
    /// Retorna o score de compatibilidade formatado
    var formattedCompatibilityScore: String {
        return String(format: "%.0f%%", compatibilityScore)
    }
}

// MARK: - MatchType Enum

/// Tipos de match no sistema
enum MatchType: String, Codable, CaseIterable {
    case regular = "regular"
    case superLike = "super_like"
    case boost = "boost"
    case premium = "premium"
    
    var displayName: String {
        switch self {
        case .regular:
            return "Match Regular"
        case .superLike:
            return "Super Like"
        case .boost:
            return "Boost"
        case .premium:
            return "Premium Match"
        }
    }
    
    var iconName: String {
        switch self {
        case .regular:
            return "heart.fill"
        case .superLike:
            return "star.fill"
        case .boost:
            return "bolt.fill"
        case .premium:
            return "crown.fill"
        }
    }
}

// MARK: - SwipeAction

/// Ações de swipe no sistema de discovery
enum SwipeAction: String, Codable, CaseIterable {
    case like = "like"
    case pass = "pass"
    case superLike = "super_like"
    case report = "report"
    case block = "block"
    
    var displayName: String {
        switch self {
        case .like:
            return "Curtir"
        case .pass:
            return "Passar"
        case .superLike:
            return "Super Curtir"
        case .report:
            return "Denunciar"
        case .block:
            return "Bloquear"
        }
    }
    
    var iconName: String {
        switch self {
        case .like:
            return "heart.fill"
        case .pass:
            return "xmark"
        case .superLike:
            return "star.fill"
        case .report:
            return "exclamationmark.triangle.fill"
        case .block:
            return "hand.raised.fill"
        }
    }
    
    var color: String {
        switch self {
        case .like:
            return "green"
        case .pass:
            return "red"
        case .superLike:
            return "blue"
        case .report:
            return "orange"
        case .block:
            return "gray"
        }
    }
}

// MARK: - SwipeRecord

/// Registro de swipe para tracking e analytics
struct SwipeRecord: Codable, Identifiable {
    let id: String
    let userId: String
    let targetUserId: String
    let action: SwipeAction
    let swipedAt: Date
    let deviceInfo: String?
    let location: SwipeLocation?
    
    init(
        id: String = UUID().uuidString,
        userId: String,
        targetUserId: String,
        action: SwipeAction,
        swipedAt: Date = Date(),
        deviceInfo: String? = nil,
        location: SwipeLocation? = nil
    ) {
        self.id = id
        self.userId = userId
        self.targetUserId = targetUserId
        self.action = action
        self.swipedAt = swipedAt
        self.deviceInfo = deviceInfo
        self.location = location
    }
}

// MARK: - SwipeLocation

/// Localização do swipe para analytics
struct SwipeLocation: Codable {
    let latitude: Double
    let longitude: Double
    let accuracy: Double
    let timestamp: Date
    
    init(
        latitude: Double,
        longitude: Double,
        accuracy: Double,
        timestamp: Date = Date()
    ) {
        self.latitude = latitude
        self.longitude = longitude
        self.accuracy = accuracy
        self.timestamp = timestamp
    }
}

// MARK: - MatchPreview

/// Preview simplificado do match para listas
struct MatchPreview: Codable, Identifiable {
    let id: String
    let otherUserId: String
    let otherUserName: String
    let otherUserPhoto: String?
    let matchedAt: Date
    let hasUnreadMessages: Bool
    let lastMessagePreview: String?
    let lastMessageAt: Date?
    let compatibilityScore: Double
    let matchType: MatchType
    let isOnline: Bool
    
    var timeAgoString: String {
        guard let lastMessageAt = lastMessageAt else {
            return Match.timeAgoFromDate(matchedAt)
        }
        return Match.timeAgoFromDate(lastMessageAt)
    }
}

// MARK: - Helper Extensions

extension Match {
    static func timeAgoFromDate(_ date: Date) -> String {
        let interval = Date().timeIntervalSince(date)
        
        if interval < 60 {
            return "Agora"
        } else if interval < 3600 {
            let minutes = Int(interval / 60)
            return "\(minutes)min"
        } else if interval < 86400 {
            let hours = Int(interval / 3600)
            return "\(hours)h"
        } else if interval < 604800 {
            let days = Int(interval / 86400)
            return "\(days)d"
        } else {
            let formatter = DateFormatter()
            formatter.dateFormat = "dd/MM"
            return formatter.string(from: date)
        }
    }
}