// AppView.swift — FypMatch iOS
// Navegação principal (TabBar) e root do app

import SwiftUI
import ComposableArchitecture

struct AppView: View {
    @StateObject private var service = FirebaseService.shared
    @State private var selectedTab: AppTab = .discovery
    @State private var showPremium = false

    var body: some View {
        Group {
            if service.isAuthenticated {
                mainTabView
            } else {
                WelcomeView(store: Store(initialState: AuthFeature.State()) { AuthFeature() })
            }
        }
        .animation(.easeInOut(duration: 0.3), value: service.isAuthenticated)
    }

    private var mainTabView: some View {
        TabView(selection: $selectedTab) {
            DiscoveryView(store: Store(initialState: DiscoveryFeature.State()) { DiscoveryFeature() })
                .tabItem { Label("Descobrir", systemImage: "flame.fill") }
                .tag(AppTab.discovery)

            MatchesView()
                .tabItem { Label("Matches", systemImage: "heart.fill") }
                .tag(AppTab.matches)
                .badge(service.unreadMatchCount > 0 ? "\(service.unreadMatchCount)" : nil)

            ConversationsView()
                .tabItem { Label("Mensagens", systemImage: "bubble.left.and.bubble.right.fill") }
                .tag(AppTab.messages)
                .badge(service.unreadMessageCount > 0 ? "\(service.unreadMessageCount)" : nil)

            AICounselorView()
                .tabItem { Label("Conselheiro", systemImage: "brain.head.profile") }
                .tag(AppTab.counselor)

            ProfileView()
                .tabItem { Label("Perfil", systemImage: "person.circle.fill") }
                .tag(AppTab.profile)
        }
        .tint(.fypPink)
        .sheet(isPresented: $showPremium) { PremiumView() }
    }
}

enum AppTab: Hashable {
    case discovery, matches, messages, counselor, profile
}

extension FirebaseService {
    var unreadMatchCount: Int { 0 }
    var unreadMessageCount: Int { 0 }
}
