//
//  AccessCode.swift
//  FypMatch iOS
//
//  Modelo para sistema de códigos de acesso antecipado
//  Gerencia early access e convites
//

import Foundation

// MARK: - Access Code

/// Código de acesso para early access
struct AccessCode: Codable, Identifiable, Equatable {
    let id: String
    let code: String
    let type: AccessCodeType
    let status: AccessCodeStatus
    let maxUses: Int
    let currentUses: Int
    let expiresAt: Date?
    let createdBy: String?
    let createdAt: Date
    let usedBy: [String]
    let benefits: [AccessBenefit]
    
    init(
        id: String = UUID().uuidString,
        code: String,
        type: AccessCodeType,
        status: AccessCodeStatus = .active,
        maxUses: Int = 1,
        currentUses: Int = 0,
        expiresAt: Date? = nil,
        createdBy: String? = nil,
        createdAt: Date = Date(),
        usedBy: [String] = [],
        benefits: [AccessBenefit] = []
    ) {
        self.id = id
        self.code = code
        self.type = type
        self.status = status
        self.maxUses = maxUses
        self.currentUses = currentUses
        self.expiresAt = expiresAt
        self.createdBy = createdBy
        self.createdAt = createdAt
        self.usedBy = usedBy
        self.benefits = benefits
    }
    
    var isValid: Bool {
        guard status == .active else { return false }
        guard currentUses < maxUses else { return false }
        
        if let expiresAt = expiresAt {
            return Date() < expiresAt
        }
        
        return true
    }
    
    var remainingUses: Int {
        return max(0, maxUses - currentUses)
    }
    
    var usagePercentage: Double {
        guard maxUses > 0 else { return 0 }
        return Double(currentUses) / Double(maxUses) * 100
    }
}

// MARK: - Access Code Type

enum AccessCodeType: String, Codable, CaseIterable {
    case earlyAccess = "early_access"
    case betaTester = "beta_tester"
    case influencer = "influencer"
    case affiliate = "affiliate"
    case promotional = "promotional"
    case referral = "referral"
    case vip = "vip"
    
    var displayName: String {
        switch self {
        case .earlyAccess: return "Acesso Antecipado"
        case .betaTester: return "Beta Tester"
        case .influencer: return "Influenciador"
        case .affiliate: return "Afiliado"
        case .promotional: return "Promocional"
        case .referral: return "Indicação"
        case .vip: return "VIP"
        }
    }
    
    var description: String {
        switch self {
        case .earlyAccess:
            return "Acesso antecipado ao app antes do lançamento oficial"
        case .betaTester:
            return "Código para testadores beta com acesso a recursos experimentais"
        case .influencer:
            return "Código especial para influenciadores digitais"
        case .affiliate:
            return "Código para parceiros e afiliados"
        case .promotional:
            return "Código promocional com benefícios especiais"
        case .referral:
            return "Código de indicação de amigo"
        case .vip:
            return "Código VIP com todos os benefícios"
        }
    }
}

// MARK: - Access Code Status

enum AccessCodeStatus: String, Codable {
    case active = "active"
    case expired = "expired"
    case used = "used"
    case revoked = "revoked"
    
    var displayName: String {
        switch self {
        case .active: return "Ativo"
        case .expired: return "Expirado"
        case .used: return "Usado"
        case .revoked: return "Revogado"
        }
    }
}

// MARK: - Access Benefit

enum AccessBenefit: String, Codable {
    case premiumTrial = "premium_trial"
    case unlimitedSwipes = "unlimited_swipes"
    case superLikes = "super_likes"
    case prioritySupport = "priority_support"
    case exclusiveBadge = "exclusive_badge"
    case noAds = "no_ads"
    case earlyFeatures = "early_features"
    
    var displayName: String {
        switch self {
        case .premiumTrial: return "Período de teste Premium"
        case .unlimitedSwipes: return "Curtidas ilimitadas"
        case .superLikes: return "Super Likes gratuitos"
        case .prioritySupport: return "Suporte prioritário"
        case .exclusiveBadge: return "Badge exclusivo"
        case .noAds: return "Sem anúncios"
        case .earlyFeatures: return "Acesso antecipado a novos recursos"
        }
    }
    
    var iconName: String {
        switch self {
        case .premiumTrial: return "crown.fill"
        case .unlimitedSwipes: return "infinity"
        case .superLikes: return "star.fill"
        case .prioritySupport: return "person.fill.questionmark"
        case .exclusiveBadge: return "rosette"
        case .noAds: return "nosign"
        case .earlyFeatures: return "sparkles"
        }
    }
}

// MARK: - Access Code Usage

struct AccessCodeUsage: Codable, Identifiable {
    let id: String
    let codeId: String
    let code: String
    let userId: String
    let usedAt: Date
    let ipAddress: String?
    let deviceInfo: String?
    
    init(
        id: String = UUID().uuidString,
        codeId: String,
        code: String,
        userId: String,
        usedAt: Date = Date(),
        ipAddress: String? = nil,
        deviceInfo: String? = nil
    ) {
        self.id = id
        self.codeId = codeId
        self.code = code
        self.userId = userId
        self.usedAt = usedAt
        self.ipAddress = ipAddress
        self.deviceInfo = deviceInfo
    }
}

// MARK: - Waitlist User

struct WaitlistUser: Codable, Identifiable, Equatable {
    let id: String
    let email: String
    let name: String?
    let phoneNumber: String?
    let referralSource: String?
    let position: Int?
    let status: WaitlistStatus
    let accessCodeId: String?
    let accessCodeSentAt: Date?
    let registeredAt: Date?
    let createdAt: Date
    let updatedAt: Date
    
    init(
        id: String = UUID().uuidString,
        email: String,
        name: String? = nil,
        phoneNumber: String? = nil,
        referralSource: String? = nil,
        position: Int? = nil,
        status: WaitlistStatus = .pending,
        accessCodeId: String? = nil,
        accessCodeSentAt: Date? = nil,
        registeredAt: Date? = nil,
        createdAt: Date = Date(),
        updatedAt: Date = Date()
    ) {
        self.id = id
        self.email = email
        self.name = name
        self.phoneNumber = phoneNumber
        self.referralSource = referralSource
        self.position = position
        self.status = status
        self.accessCodeId = accessCodeId
        self.accessCodeSentAt = accessCodeSentAt
        self.registeredAt = registeredAt
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
    
    var hasAccessCode: Bool {
        return accessCodeId != nil
    }
    
    var isRegistered: Bool {
        return status == .registered && registeredAt != nil
    }
}

// MARK: - Waitlist Status

enum WaitlistStatus: String, Codable {
    case pending = "pending"
    case invited = "invited"
    case registered = "registered"
    case expired = "expired"
    
    var displayName: String {
        switch self {
        case .pending: return "Na lista de espera"
        case .invited: return "Convidado"
        case .registered: return "Registrado"
        case .expired: return "Convite expirado"
        }
    }
}

// MARK: - Code Validation Result

enum CodeValidationResult: Equatable {
    case valid(AccessCode)
    case invalid(CodeValidationError)
}

enum CodeValidationError: Error, LocalizedError {
    case notFound
    case expired
    case alreadyUsed
    case maxUsesReached
    case revoked
    case userAlreadyUsedCode
    
    var errorDescription: String? {
        switch self {
        case .notFound:
            return "Código não encontrado"
        case .expired:
            return "Código expirado"
        case .alreadyUsed:
            return "Código já foi utilizado"
        case .maxUsesReached:
            return "Código atingiu o limite máximo de usos"
        case .revoked:
            return "Código foi revogado"
        case .userAlreadyUsedCode:
            return "Você já utilizou este código"
        }
    }
}
