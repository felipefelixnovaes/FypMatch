// AppFeature.swift — FypMatch iOS
// Feature raiz que gerencia estado global do app

import Foundation
import ComposableArchitecture

@Reducer
struct AppFeature {
    @ObservableState
    struct State: Equatable {
        var auth = AuthFeature.State()
        var discovery = DiscoveryFeature.State()
        var matches = MatchesFeature.State()
        var profile = ProfileFeature.State()
        var selectedTab: AppTab = .discovery
        var isAppReady = false
        var deepLink: DeepLink?
        var badgeCount: Int = 0
    }

    enum Action: Equatable {
        case auth(AuthFeature.Action)
        case discovery(DiscoveryFeature.Action)
        case matches(MatchesFeature.Action)
        case profile(ProfileFeature.Action)
        case appLaunched
        case appReady
        case selectTab(AppTab)
        case handleDeepLink(DeepLink)
        case clearDeepLink
        case updateBadgeCount(Int)
    }

    var body: some ReducerOf<Self> {
        Scope(state: \.auth,      action: \.auth)      { AuthFeature()      }
        Scope(state: \.discovery, action: \.discovery) { DiscoveryFeature() }
        Scope(state: \.matches,   action: \.matches)   { MatchesFeature()   }
        Scope(state: \.profile,   action: \.profile)   { ProfileFeature()   }

        Reduce { state, action in
            switch action {
            case .appLaunched:
                return .run { send in
                    try? await Task.sleep(for: .seconds(0.3))
                    await send(.appReady)
                    await send(.auth(.checkAuthStatus))
                }
            case .appReady:
                state.isAppReady = true
                return .none
            case let .selectTab(tab):
                state.selectedTab = tab
                return .none
            case let .handleDeepLink(link):
                state.deepLink = link
                switch link {
                case .match, .conversation: state.selectedTab = .messages
                case .discovery:            state.selectedTab = .discovery
                case .premium:              break
                }
                return .none
            case .clearDeepLink:
                state.deepLink = nil
                return .none
            case .auth(.authStateChanged(true, _)):
                return .merge(
                    .send(.discovery(.loadDiscoveryUsers)),
                    .send(.matches(.onAppear))
                )
            case .auth(.authStateChanged(false, _)):
                // Limpar estado ao sair
                state.discovery = DiscoveryFeature.State()
                state.matches = MatchesFeature.State()
                return .none
            case let .updateBadgeCount(count):
                state.badgeCount = count
                return .none
            case .auth, .discovery, .matches, .profile:
                return .none
            }
        }
    }
}

extension AppFeature.Action: CustomStringConvertible {
    var description: String {
        switch self {
        case .appLaunched: return "appLaunched"
        case .appReady:    return "appReady"
        case let .selectTab(t): return "selectTab(\(t))"
        default: return "\(self)"
        }
    }
}
