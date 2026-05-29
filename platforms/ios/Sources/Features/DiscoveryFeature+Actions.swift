// DiscoveryFeature+Actions.swift — FypMatch iOS
// Extensão com todas as actions do DiscoveryFeature

import Foundation
import ComposableArchitecture

// Completando as ações e reducer do DiscoveryFeature
extension DiscoveryFeature {
    // MARK: - Body (Reducer)
    var body: some ReducerOf<Self> {
        BindingReducer()
        Reduce { state, action in
            switch action {
            case .loadDiscoveryUsers:
                state.isLoadingUsers = true
                state.loadingError = nil
                return .run { send in
                    do {
                        let users = try await firebaseService.loadDiscoveryUsers()
                        await send(.usersLoaded(.success(users)))
                    } catch {
                        await send(.usersLoaded(.failure(error)))
                    }
                }

            case let .usersLoaded(.success(users)):
                state.isLoadingUsers = false
                state.discoveryUsers = users
                return .none

            case let .usersLoaded(.failure(error)):
                state.isLoadingUsers = false
                state.loadingError = error.localizedDescription
                return .none

            case let .swipeRight(user):
                state.swipeCount += 1
                guard let idx = state.discoveryUsers.firstIndex(where: { $0.id == user.id }) else { return .none }
                state.discoveryUsers.remove(at: idx)
                return .run { [currentUserId = state.currentUser?.id] send in
                    guard let myId = currentUserId else { return }
                    let matched = try? await firebaseService.recordLike(fromUser: myId, toUser: user.id)
                    if let match = matched {
                        await send(.matchOccurred(match))
                    }
                    // Load more if running low
                    if state.discoveryUsers.count < 3 {
                        await send(.loadDiscoveryUsers)
                    }
                }

            case let .swipeLeft(user):
                state.swipeCount += 1
                state.discoveryUsers.removeAll { $0.id == user.id }
                return .run { _ in
                    // Record pass (optional, for ML improvement)
                }

            case let .superLike(user):
                guard state.currentUser?.isActivePremium == true else {
                    state.showingPremiumUpsell = true
                    state.upsellReason = .dailyLimitReached
                    return .none
                }
                state.discoveryUsers.removeAll { $0.id == user.id }
                return .none

            case let .matchOccurred(match):
                state.newMatch = match
                state.showingMatchAnimation = true
                return .none

            case .dismissMatchAnimation:
                state.showingMatchAnimation = false
                state.newMatch = nil
                return .none

            case .activateBoost:
                guard state.currentUser?.isActivePremium == true else {
                    state.showingPremiumUpsell = true
                    state.upsellReason = .dailyLimitReached
                    return .none
                }
                return .none

            case let .showFilters(show):
                state.isShowingFilters = show
                return .none

            case .applyFilters:
                state.isShowingFilters = false
                return .run { send in
                    await send(.loadDiscoveryUsers)
                }

            case let .showPremiumUpsell(reason):
                state.showingPremiumUpsell = true
                state.upsellReason = reason
                return .none

            case .binding:
                return .none
            }
        }
    }
}

// MARK: - DiscoveryFeature Actions

extension DiscoveryFeature {
    enum Action: BindableAction, Equatable {
        case binding(BindingAction<State>)
        case loadDiscoveryUsers
        case usersLoaded(Result<[User], Error>)
        case swipeRight(User)
        case swipeLeft(User)
        case superLike(User)
        case matchOccurred(Match)
        case dismissMatchAnimation
        case activateBoost
        case showFilters(Bool)
        case applyFilters
        case showPremiumUpsell(PremiumUpsellReason)
    }
}

// MARK: - Dependencies

extension DiscoveryFeature {
    @Dependency(\.firebaseService) var firebaseService
}

extension FirebaseService {
    func loadDiscoveryUsers() async throws -> [User] {
        // Firestore query with filters
        return []
    }

    func recordLike(fromUser: String, toUser: String) async throws -> Match? {
        // Check for mutual like and create match
        return nil
    }
}
