//
//  DiscoveryFeature.swift
//  FypMatch iOS
//
//  Feature principal do discovery/swipe cards
//  Core do sistema de matching do FypMatch
//

import Foundation
import ComposableArchitecture
import CoreLocation

/// Feature responsável pelo sistema de discovery e swipe
@Reducer
struct DiscoveryFeature {
    
    // MARK: - State
    
    @ObservableState
    struct State: Equatable {
        // MARK: - Users Data
        var discoveryUsers: [User] = []
        var currentUserIndex = 0
        var loadedUsers: [User] = []
        var isLoadingUsers = false
        var loadingError: String?
        
        // MARK: - Current User
        var currentUser: User?
        
        // MARK: - Swipe State
        var isSwipeInProgress = false
        var lastSwipeAction: SwipeAction?
        var swipeCount = 0
        var dailySwipeLimit = 50 // Free users
        var remainingSwipes: Int {
            return max(0, dailySwipeLimit - swipeCount)
        }
        
        // MARK: - Match State
        var showingMatchAnimation = false
        var newMatch: Match?
        var matchAnimationDuration = 2.0
        
        // MARK: - Filters
        var isShowingFilters = false
        var tempAgeMin = 18
        var tempAgeMax = 35
        var tempMaxDistance = 50
        var tempGenderInterest: GenderInterest = .all
        
        // MARK: - Premium Features
        var showingPremiumUpsell = false
        var upsellReason: PremiumUpsellReason?
        
        // MARK: - Location
        var userLocation: CLLocation?
        var locationPermissionStatus: CLAuthorizationStatus = .notDetermined
        
        // MARK: - UI State
        var isRefreshing = false
        var showingSettings = false
        var alertMessage: String?
        
        // MARK: - Computed Properties
        var currentDisplayUser: User? {
            guard currentUserIndex < discoveryUsers.count else { return nil }
            return discoveryUsers[currentUserIndex]
        }
        
        var hasMoreUsers: Bool {
            return currentUserIndex < discoveryUsers.count
        }
        
        var isNearSwipeLimit: Bool {
            return remainingSwipes <= 5
        }
        
        var isAtSwipeLimit: Bool {
            return remainingSwipes <= 0
        }
        
        var progressPercentage: Double {
            guard !discoveryUsers.isEmpty else { return 0.0 }
            return Double(currentUserIndex) / Double(discoveryUsers.count)
        }
    }
    
    // MARK: - Action
    
    enum Action: BindableAction, Equatable {
        // MARK: - Binding
        case binding(BindingAction<State>)
        
        // MARK: - Lifecycle
        case onAppear
        case onDisappear
        case refresh
        
        // MARK: - Data Loading
        case loadDiscoveryUsers
        case loadMoreUsers
        case discoveryUsersLoaded(Result<[User], Error>)
        
        // MARK: - Swipe Actions
        case swipeUser(User, SwipeAction)
        case undoLastSwipe
        case swipeCompleted(SwipeAction)
        case swipeResponse(Result<Match?, Error>)
        
        // MARK: - Navigation
        case showUserProfile(User)
        case showFilters(Bool)
        case showPremiumUpsell(PremiumUpsellReason)
        case showSettings(Bool)
        
        // MARK: - Match Handling
        case newMatchDetected(Match)
        case dismissMatchAnimation
        case viewMatch(Match)
        
        // MARK: - Filters
        case applyFilters
        case resetFilters
        case filtersChanged
        
        // MARK: - Location
        case requestLocationPermission
        case locationPermissionChanged(CLAuthorizationStatus)
        case locationUpdated(CLLocation)
        
        // MARK: - Premium
        case upgradeToPremium
        case dismissPremiumUpsell
        
        // MARK: - Error Handling
        case dismissAlert
        case retryLastAction
    }
    
    // MARK: - Dependencies
    
    @Dependency(\.firebaseService) var firebaseService
    @Dependency(\.locationService) var locationService
    @Dependency(\.matchingService) var matchingService
    @Dependency(\.mainQueue) var mainQueue
    
    // MARK: - Body
    
    var body: some ReducerOf<Self> {
        BindingReducer()
        
        Reduce { state, action in
            switch action {
                
            // MARK: - Lifecycle
                
            case .onAppear:
                state.currentUser = firebaseService.currentUser
                return .merge(
                    .send(.requestLocationPermission),
                    .send(.loadDiscoveryUsers)
                )
                
            case .onDisappear:
                return .none
                
            case .refresh:
                state.isRefreshing = true
                state.currentUserIndex = 0
                state.discoveryUsers = []
                return .send(.loadDiscoveryUsers)
                
            // MARK: - Data Loading
                
            case .loadDiscoveryUsers:
                guard !state.isLoadingUsers else { return .none }
                
                state.isLoadingUsers = true
                state.loadingError = nil
                
                return .run { send in
                    do {
                        let users = try await firebaseService.fetchDiscoveryUsers(limit: 20)
                        await send(.discoveryUsersLoaded(.success(users)))
                    } catch {
                        await send(.discoveryUsersLoaded(.failure(error)))
                    }
                }
                
            case .loadMoreUsers:
                guard !state.isLoadingUsers && state.currentUserIndex >= state.discoveryUsers.count - 3 else {
                    return .none
                }
                
                return .send(.loadDiscoveryUsers)
                
            case let .discoveryUsersLoaded(.success(users)):
                state.isLoadingUsers = false
                state.isRefreshing = false
                state.loadingError = nil
                
                // Adicionar novos usuários, evitando duplicatas
                let newUsers = users.filter { newUser in
                    !state.discoveryUsers.contains { $0.id == newUser.id }
                }
                state.discoveryUsers.append(contentsOf: newUsers)
                
                return .none
                
            case let .discoveryUsersLoaded(.failure(error)):
                state.isLoadingUsers = false
                state.isRefreshing = false
                state.loadingError = error.localizedDescription
                return .none
                
            // MARK: - Swipe Actions
                
            case let .swipeUser(user, action):
                guard !state.isSwipeInProgress && !state.isAtSwipeLimit else {
                    if state.isAtSwipeLimit {
                        return .send(.showPremiumUpsell(.swipeLimitReached))
                    }
                    return .none
                }
                
                state.isSwipeInProgress = true
                state.lastSwipeAction = action
                
                return .run { send in
                    do {
                        let match = try await matchingService.processSwipe(
                            userId: firebaseService.currentUser?.id ?? "",
                            targetUserId: user.id,
                            action: action
                        )
                        await send(.swipeResponse(.success(match)))
                    } catch {
                        await send(.swipeResponse(.failure(error)))
                    }
                }
                
            case let .swipeCompleted(action):
                state.isSwipeInProgress = false
                state.swipeCount += 1
                state.currentUserIndex += 1
                
                // Carregar mais usuários se necessário
                if state.currentUserIndex >= state.discoveryUsers.count - 3 {
                    return .send(.loadMoreUsers)
                }
                
                return .none
                
            case let .swipeResponse(.success(match)):
                state.isSwipeInProgress = false
                
                if let match = match {
                    return .send(.newMatchDetected(match))
                } else {
                    return .send(.swipeCompleted(state.lastSwipeAction ?? .pass))
                }
                
            case let .swipeResponse(.failure(error)):
                state.isSwipeInProgress = false
                state.alertMessage = "Erro ao processar swipe: \(error.localizedDescription)"
                return .none
                
            case .undoLastSwipe:
                guard state.currentUserIndex > 0 else { return .none }
                
                // Funcionalidade premium
                guard state.currentUser?.isActivePremium == true else {
                    return .send(.showPremiumUpsell(.undoSwipe))
                }
                
                state.currentUserIndex -= 1
                state.swipeCount = max(0, state.swipeCount - 1)
                
                return .run { send in
                    // TODO: Implementar undo no backend
                    await send(.swipeCompleted(.pass)) // Placeholder
                }
                
            // MARK: - Match Handling
                
            case let .newMatchDetected(match):
                state.newMatch = match
                state.showingMatchAnimation = true
                
                return .run { send in
                    try await mainQueue.sleep(for: .seconds(state.matchAnimationDuration))
                    await send(.dismissMatchAnimation)
                }
                
            case .dismissMatchAnimation:
                state.showingMatchAnimation = false
                return .send(.swipeCompleted(.like))
                
            case let .viewMatch(match):
                state.showingMatchAnimation = false
                // TODO: Navegar para tela de chat
                return .send(.swipeCompleted(.like))
                
            // MARK: - Filters
                
            case .showFilters(let show):
                state.isShowingFilters = show
                if show {
                    // Carregar valores atuais nos filtros temporários
                    if let user = state.currentUser {
                        state.tempAgeMin = user.ageRangeMin
                        state.tempAgeMax = user.ageRangeMax
                        state.tempMaxDistance = user.maxDistanceKm
                        state.tempGenderInterest = user.genderInterest
                    }
                }
                return .none
                
            case .applyFilters:
                state.isShowingFilters = false
                
                // Aplicar filtros ao usuário atual
                guard var user = state.currentUser else { return .none }
                
                // Atualizar preferências do usuário
                user = User(
                    id: user.id,
                    email: user.email,
                    displayName: user.displayName,
                    age: user.age,
                    bio: user.bio,
                    photos: user.photos,
                    profileImageURL: user.profileImageURL,
                    isPhotoVerified: user.isPhotoVerified,
                    latitude: user.latitude,
                    longitude: user.longitude,
                    city: user.city,
                    state: user.state,
                    country: user.country,
                    maxDistanceKm: state.tempMaxDistance,
                    genderInterest: state.tempGenderInterest,
                    ageRangeMin: state.tempAgeMin,
                    ageRangeMax: state.tempAgeMax,
                    interestedInSeriousRelationship: user.interestedInSeriousRelationship,
                    interestedInCasualDating: user.interestedInCasualDating,
                    interestedInFriendship: user.interestedInFriendship,
                    gender: user.gender,
                    height: user.height,
                    occupation: user.occupation,
                    education: user.education,
                    children: user.children,
                    wantsChildren: user.wantsChildren,
                    smoking: user.smoking,
                    drinking: user.drinking,
                    religion: user.religion,
                    politicalViews: user.politicalViews,
                    interests: user.interests,
                    hobbies: user.hobbies,
                    languages: user.languages,
                    traveledCountries: user.traveledCountries,
                    favoriteActivities: user.favoriteActivities,
                    isPremium: user.isPremium,
                    premiumExpiresAt: user.premiumExpiresAt,
                    isVerified: user.isVerified,
                    isOnline: user.isOnline,
                    lastActiveAt: user.lastActiveAt,
                    swipeCount: user.swipeCount,
                    matchCount: user.matchCount,
                    messageCount: user.messageCount,
                    profileViews: user.profileViews,
                    likes: user.likes,
                    passes: user.passes,
                    pushNotificationsEnabled: user.pushNotificationsEnabled,
                    showOnlineStatus: user.showOnlineStatus,
                    showDistanceInProfile: user.showDistanceInProfile,
                    allowMessageFromMatches: user.allowMessageFromMatches,
                    createdAt: user.createdAt,
                    updatedAt: Date(),
                    deviceToken: user.deviceToken,
                    isEmailVerified: user.isEmailVerified,
                    isPhoneVerified: user.isPhoneVerified,
                    phoneNumber: user.phoneNumber
                )
                
                state.currentUser = user
                
                return .run { send in
                    try await firebaseService.saveUser(user)
                    await send(.refresh)
                }
                
            case .resetFilters:
                state.tempAgeMin = 18
                state.tempAgeMax = 35
                state.tempMaxDistance = 50
                state.tempGenderInterest = .all
                return .none
                
            case .filtersChanged:
                return .none
                
            // MARK: - Location
                
            case .requestLocationPermission:
                return .run { send in
                    let status = await locationService.requestPermission()
                    await send(.locationPermissionChanged(status))
                }
                
            case let .locationPermissionChanged(status):
                state.locationPermissionStatus = status
                
                if status == .authorizedWhenInUse || status == .authorizedAlways {
                    return .run { send in
                        if let location = await locationService.getCurrentLocation() {
                            await send(.locationUpdated(location))
                        }
                    }
                }
                return .none
                
            case let .locationUpdated(location):
                state.userLocation = location
                
                // Atualizar localização do usuário no perfil
                guard var user = state.currentUser else { return .none }
                
                user = User(
                    id: user.id,
                    email: user.email,
                    displayName: user.displayName,
                    age: user.age,
                    bio: user.bio,
                    photos: user.photos,
                    profileImageURL: user.profileImageURL,
                    isPhotoVerified: user.isPhotoVerified,
                    latitude: location.coordinate.latitude,
                    longitude: location.coordinate.longitude,
                    city: user.city,
                    state: user.state,
                    country: user.country,
                    maxDistanceKm: user.maxDistanceKm,
                    genderInterest: user.genderInterest,
                    ageRangeMin: user.ageRangeMin,
                    ageRangeMax: user.ageRangeMax,
                    interestedInSeriousRelationship: user.interestedInSeriousRelationship,
                    interestedInCasualDating: user.interestedInCasualDating,
                    interestedInFriendship: user.interestedInFriendship,
                    gender: user.gender,
                    height: user.height,
                    occupation: user.occupation,
                    education: user.education,
                    children: user.children,
                    wantsChildren: user.wantsChildren,
                    smoking: user.smoking,
                    drinking: user.drinking,
                    religion: user.religion,
                    politicalViews: user.politicalViews,
                    interests: user.interests,
                    hobbies: user.hobbies,
                    languages: user.languages,
                    traveledCountries: user.traveledCountries,
                    favoriteActivities: user.favoriteActivities,
                    isPremium: user.isPremium,
                    premiumExpiresAt: user.premiumExpiresAt,
                    isVerified: user.isVerified,
                    isOnline: user.isOnline,
                    lastActiveAt: user.lastActiveAt,
                    swipeCount: user.swipeCount,
                    matchCount: user.matchCount,
                    messageCount: user.messageCount,
                    profileViews: user.profileViews,
                    likes: user.likes,
                    passes: user.passes,
                    pushNotificationsEnabled: user.pushNotificationsEnabled,
                    showOnlineStatus: user.showOnlineStatus,
                    showDistanceInProfile: user.showDistanceInProfile,
                    allowMessageFromMatches: user.allowMessageFromMatches,
                    createdAt: user.createdAt,
                    updatedAt: Date(),
                    deviceToken: user.deviceToken,
                    isEmailVerified: user.isEmailVerified,
                    isPhoneVerified: user.isPhoneVerified,
                    phoneNumber: user.phoneNumber
                )
                
                state.currentUser = user
                
                return .run { send in
                    try await firebaseService.saveUser(user)
                }
                
            // MARK: - Premium
                
            case let .showPremiumUpsell(reason):
                state.showingPremiumUpsell = true
                state.upsellReason = reason
                return .none
                
            case .upgradeToPremium:
                state.showingPremiumUpsell = false
                // TODO: Implementar compra premium
                return .none
                
            case .dismissPremiumUpsell:
                state.showingPremiumUpsell = false
                state.upsellReason = nil
                return .none
                
            // MARK: - Navigation
                
            case .showUserProfile:
                // TODO: Implementar visualização de perfil
                return .none
                
            case .showSettings:
                // TODO: Implementar tela de configurações
                return .none
                
            // MARK: - Error Handling
                
            case .dismissAlert:
                state.alertMessage = nil
                return .none
                
            case .retryLastAction:
                state.alertMessage = nil
                return .send(.loadDiscoveryUsers)
                
            case .binding:
                return .none
            }
        }
    }
}

// MARK: - Supporting Types

/// Razões para mostrar upgrade premium
enum PremiumUpsellReason: Equatable {
    case swipeLimitReached
    case undoSwipe
    case superLike
    case seeWhoLikesYou
    case boost
    case premiumFilters
    
    var title: String {
        switch self {
        case .swipeLimitReached:
            return "Limite de curtidas atingido"
        case .undoSwipe:
            return "Desfazer último swipe"
        case .superLike:
            return "Super Like"
        case .seeWhoLikesYou:
            return "Ver quem te curtiu"
        case .boost:
            return "Boost seu perfil"
        case .premiumFilters:
            return "Filtros avançados"
        }
    }
    
    var message: String {
        switch self {
        case .swipeLimitReached:
            return "Você atingiu seu limite diário de curtidas. Upgrade para Premium para curtidas ilimitadas!"
        case .undoSwipe:
            return "Desfazer swipes é um recurso Premium. Upgrade agora!"
        case .superLike:
            return "Destaque-se com Super Likes ilimitados no Premium!"
        case .seeWhoLikesYou:
            return "Descubra quem já te curtiu com o Premium!"
        case .boost:
            return "Seja visto por mais pessoas com o Boost Premium!"
        case .premiumFilters:
            return "Use filtros avançados para encontrar matches perfeitos!"
        }
    }
}

// MARK: - Dependencies

private enum LocationServiceKey: DependencyKey {
    static let liveValue = LocationService()
}

private enum MatchingServiceKey: DependencyKey {
    static let liveValue = MatchingService()
}

extension DependencyValues {
    var locationService: LocationService {
        get { self[LocationServiceKey.self] }
        set { self[LocationServiceKey.self] = newValue }
    }
    
    var matchingService: MatchingService {
        get { self[MatchingServiceKey.self] }
        set { self[MatchingServiceKey.self] = newValue }
    }
}

// MARK: - Placeholder Services

/// Serviço de localização (implementação simplificada)
class LocationService {
    func requestPermission() async -> CLAuthorizationStatus {
        // TODO: Implementar solicitação de permissão real
        return .authorizedWhenInUse
    }
    
    func getCurrentLocation() async -> CLLocation? {
        // TODO: Implementar obtenção de localização real
        return CLLocation(latitude: -23.5505, longitude: -46.6333) // São Paulo
    }
}

/// Serviço de matching (implementação simplificada)
class MatchingService {
    func processSwipe(userId: String, targetUserId: String, action: SwipeAction) async throws -> Match? {
        // TODO: Implementar lógica de matching real
        
        // Simular chance de match (apenas para likes)
        if action == .like && Bool.random() {
            return Match(
                user1Id: userId,
                user2Id: targetUserId,
                user1SwipedAt: Date(),
                user2SwipedAt: Date(),
                compatibilityScore: Double.random(in: 70...95)
            )
        }
        
        return nil
    }
}