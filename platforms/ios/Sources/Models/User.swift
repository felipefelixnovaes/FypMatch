//
//  User.swift
//  FypMatch iOS
//
//  Modelo principal do usuário - Port do Android User.kt
//  Mantém compatibilidade total com o backend Firebase
//

import Foundation
import FirebaseFirestore

/// Modelo principal do usuário do FypMatch
/// Port completo do modelo Android para manter compatibilidade
struct User: Codable, Identifiable, Equatable {
    // MARK: - Propriedades Básicas
    let id: String
    let email: String
    let displayName: String
    let age: Int
    let bio: String
    
    // MARK: - Fotos e Mídia
    let photos: [String]
    let profileImageURL: String?
    let isPhotoVerified: Bool
    
    // MARK: - Localização
    let latitude: Double?
    let longitude: Double?
    let city: String?
    let state: String?
    let country: String?
    let maxDistanceKm: Int
    
    // MARK: - Preferências de Matching
    let genderInterest: GenderInterest
    let ageRangeMin: Int
    let ageRangeMax: Int
    let interestedInSeriousRelationship: Bool
    let interestedInCasualDating: Bool
    let interestedInFriendship: Bool
    
    // MARK: - Informações Pessoais
    let gender: Gender
    let height: Int? // em centímetros
    let occupation: String?
    let education: String?
    let children: ChildrenStatus
    let wantsChildren: Bool?
    let smoking: SmokingStatus
    let drinking: DrinkingStatus
    let religion: String?
    let politicalViews: String?
    
    // MARK: - Interesses e Hobbies
    let interests: [String]
    let hobbies: [String]
    let languages: [String]
    let traveledCountries: [String]
    let favoriteActivities: [String]
    
    // MARK: - Premium e Status
    let isPremium: Bool
    let premiumExpiresAt: Date?
    let isVerified: Bool
    let isOnline: Bool
    let lastActiveAt: Date
    
    // MARK: - Métricas do App
    let swipeCount: Int
    let matchCount: Int
    let messageCount: Int
    let profileViews: Int
    let likes: Int
    let passes: Int
    
    // MARK: - Configurações
    let pushNotificationsEnabled: Bool
    let showOnlineStatus: Bool
    let showDistanceInProfile: Bool
    let allowMessageFromMatches: Bool
    
    // MARK: - Dados de Sistema
    let createdAt: Date
    let updatedAt: Date
    let deviceToken: String?
    let isEmailVerified: Bool
    let isPhoneVerified: Bool
    let phoneNumber: String?
    
    // MARK: - Inicializador
    init(
        id: String,
        email: String,
        displayName: String,
        age: Int,
        bio: String = "",
        photos: [String] = [],
        profileImageURL: String? = nil,
        isPhotoVerified: Bool = false,
        latitude: Double? = nil,
        longitude: Double? = nil,
        city: String? = nil,
        state: String? = nil,
        country: String? = nil,
        maxDistanceKm: Int = 50,
        genderInterest: GenderInterest = .all,
        ageRangeMin: Int = 18,
        ageRangeMax: Int = 35,
        interestedInSeriousRelationship: Bool = true,
        interestedInCasualDating: Bool = false,
        interestedInFriendship: Bool = false,
        gender: Gender = .notSpecified,
        height: Int? = nil,
        occupation: String? = nil,
        education: String? = nil,
        children: ChildrenStatus = .no,
        wantsChildren: Bool? = nil,
        smoking: SmokingStatus = .never,
        drinking: DrinkingStatus = .never,
        religion: String? = nil,
        politicalViews: String? = nil,
        interests: [String] = [],
        hobbies: [String] = [],
        languages: [String] = ["Português"],
        traveledCountries: [String] = [],
        favoriteActivities: [String] = [],
        isPremium: Bool = false,
        premiumExpiresAt: Date? = nil,
        isVerified: Bool = false,
        isOnline: Bool = false,
        lastActiveAt: Date = Date(),
        swipeCount: Int = 0,
        matchCount: Int = 0,
        messageCount: Int = 0,
        profileViews: Int = 0,
        likes: Int = 0,
        passes: Int = 0,
        pushNotificationsEnabled: Bool = true,
        showOnlineStatus: Bool = true,
        showDistanceInProfile: Bool = true,
        allowMessageFromMatches: Bool = true,
        createdAt: Date = Date(),
        updatedAt: Date = Date(),
        deviceToken: String? = nil,
        isEmailVerified: Bool = false,
        isPhoneVerified: Bool = false,
        phoneNumber: String? = nil
    ) {
        self.id = id
        self.email = email
        self.displayName = displayName
        self.age = age
        self.bio = bio
        self.photos = photos
        self.profileImageURL = profileImageURL
        self.isPhotoVerified = isPhotoVerified
        self.latitude = latitude
        self.longitude = longitude
        self.city = city
        self.state = state
        self.country = country
        self.maxDistanceKm = maxDistanceKm
        self.genderInterest = genderInterest
        self.ageRangeMin = ageRangeMin
        self.ageRangeMax = ageRangeMax
        self.interestedInSeriousRelationship = interestedInSeriousRelationship
        self.interestedInCasualDating = interestedInCasualDating
        self.interestedInFriendship = interestedInFriendship
        self.gender = gender
        self.height = height
        self.occupation = occupation
        self.education = education
        self.children = children
        self.wantsChildren = wantsChildren
        self.smoking = smoking
        self.drinking = drinking
        self.religion = religion
        self.politicalViews = politicalViews
        self.interests = interests
        self.hobbies = hobbies
        self.languages = languages
        self.traveledCountries = traveledCountries
        self.favoriteActivities = favoriteActivities
        self.isPremium = isPremium
        self.premiumExpiresAt = premiumExpiresAt
        self.isVerified = isVerified
        self.isOnline = isOnline
        self.lastActiveAt = lastActiveAt
        self.swipeCount = swipeCount
        self.matchCount = matchCount
        self.messageCount = messageCount
        self.profileViews = profileViews
        self.likes = likes
        self.passes = passes
        self.pushNotificationsEnabled = pushNotificationsEnabled
        self.showOnlineStatus = showOnlineStatus
        self.showDistanceInProfile = showDistanceInProfile
        self.allowMessageFromMatches = allowMessageFromMatches
        self.createdAt = createdAt
        self.updatedAt = updatedAt
        self.deviceToken = deviceToken
        self.isEmailVerified = isEmailVerified
        self.isPhoneVerified = isPhoneVerified
        self.phoneNumber = phoneNumber
    }
}

// MARK: - User Extensions

extension User {
    /// Retorna a primeira foto do perfil ou uma imagem padrão
    var primaryPhotoURL: String {
        return profileImageURL ?? photos.first ?? ""
    }
    
    /// Calcula a idade do usuário baseado na data de nascimento (se disponível)
    var formattedAge: String {
        return "\(age) anos"
    }
    
    /// Retorna se o usuário está premium e ativo
    var isActivePremium: Bool {
        guard isPremium else { return false }
        guard let expiresAt = premiumExpiresAt else { return isPremium }
        return expiresAt > Date()
    }
    
    /// Retorna a localização formatada para exibição
    var formattedLocation: String {
        var components: [String] = []
        if let city = city { components.append(city) }
        if let state = state { components.append(state) }
        return components.joined(separator: ", ")
    }
    
    /// Retorna se o perfil está completo o suficiente para matching
    var isProfileComplete: Bool {
        return !displayName.isEmpty &&
               !bio.isEmpty &&
               !photos.isEmpty &&
               age >= 18 &&
               !interests.isEmpty
    }
    
    /// Calcula pontuação de compatibilidade base para algoritmo
    var compatibilityScore: Double {
        var score: Double = 0.0
        
        // Completeness boost
        if isProfileComplete { score += 20.0 }
        if isPhotoVerified { score += 15.0 }
        if isVerified { score += 10.0 }
        
        // Activity boost
        if isOnline { score += 5.0 }
        
        // Premium boost
        if isActivePremium { score += 10.0 }
        
        return min(score, 100.0)
    }
}

// MARK: - Enums

/// Interesse de gênero para matching
enum GenderInterest: String, Codable, CaseIterable {
    case men = "men"
    case women = "women"
    case all = "all"
    case nonBinary = "non_binary"
    
    var displayName: String {
        switch self {
        case .men: return "Homens"
        case .women: return "Mulheres"
        case .all: return "Todos"
        case .nonBinary: return "Não-binário"
        }
    }
}

/// Gênero do usuário
enum Gender: String, Codable, CaseIterable {
    case male = "male"
    case female = "female"
    case nonBinary = "non_binary"
    case notSpecified = "not_specified"
    
    var displayName: String {
        switch self {
        case .male: return "Masculino"
        case .female: return "Feminino"
        case .nonBinary: return "Não-binário"
        case .notSpecified: return "Prefiro não informar"
        }
    }
}

/// Status de filhos
enum ChildrenStatus: String, Codable, CaseIterable {
    case no = "no"
    case yes = "yes"
    case someday = "someday"
    case notSure = "not_sure"
    
    var displayName: String {
        switch self {
        case .no: return "Não tenho filhos"
        case .yes: return "Tenho filhos"
        case .someday: return "Quero ter filhos"
        case .notSure: return "Não tenho certeza"
        }
    }
}

/// Status de fumante
enum SmokingStatus: String, Codable, CaseIterable {
    case never = "never"
    case sometimes = "sometimes"
    case regularly = "regularly"
    
    var displayName: String {
        switch self {
        case .never: return "Não fumo"
        case .sometimes: return "Fumo socialmente"
        case .regularly: return "Fumo regularmente"
        }
    }
}

/// Status de bebida
enum DrinkingStatus: String, Codable, CaseIterable {
    case never = "never"
    case sometimes = "sometimes"
    case regularly = "regularly"
    
    var displayName: String {
        switch self {
        case .never: return "Não bebo"
        case .sometimes: return "Bebo socialmente"
        case .regularly: return "Bebo regularmente"
        }
    }
}