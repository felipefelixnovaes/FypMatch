// DiscoveryView.swift — FypMatch iOS
// Tela principal de discovery/swipe — port de DiscoveryScreen.kt

import SwiftUI
import ComposableArchitecture

struct DiscoveryView: View {
    @Bindable var store: StoreOf<DiscoveryFeature>

    var body: some View {
        NavigationStack {
            ZStack {
                Color(.systemGroupedBackground).ignoresSafeArea()

                if store.isLoadingUsers {
                    loadingView
                } else if store.discoveryUsers.isEmpty {
                    emptyView
                } else {
                    cardStack
                }

                // Match animation overlay
                if store.showingMatchAnimation, let match = store.newMatch {
                    MatchAnimationView(match: match) {
                        store.send(.dismissMatchAnimation)
                    }
                    .transition(.opacity)
                    .zIndex(10)
                }
            }
            .navigationTitle("Descobrir")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { toolbarItems }
        }
        .onAppear { store.send(.loadDiscoveryUsers) }
        .sheet(isPresented: $store.isShowingFilters) { FiltersSheet(store: store) }
        .sheet(isPresented: $store.showingPremiumUpsell) { PremiumUpsellSheet(reason: store.upsellReason) }
        .sheet(isPresented: $store.isShowingUserDetail) {
            if let user = store.selectedUserForDetail {
                UserDetailView(
                    store: Store(
                        initialState: UserDetailFeature.State(
                            user: user,
                            currentUser: store.currentUser
                        )
                    ) {
                        UserDetailFeature()
                    }
                )
                .onDisappear {
                    store.send(.dismissUserDetail)
                }
            }
        }
    }

    // MARK: - Card Stack

    private var cardStack: some View {
        VStack(spacing: 0) {
            ZStack {
                ForEach(Array(store.discoveryUsers.prefix(3).enumerated().reversed()), id: \.element.id) { index, user in
                    ZStack(alignment: .topTrailing) {
                        SwipeCardView(user: user) { direction in
                            store.send(direction == .right ? .swipeRight(user) : .swipeLeft(user))
                        }

                        // Botão de info — abre UserDetailView apenas no card do topo
                        if index == 0 {
                            Button {
                                store.send(.showUserProfile(user))
                            } label: {
                                Image(systemName: "info.circle.fill")
                                    .font(.system(size: 26))
                                    .foregroundColor(.white)
                                    .shadow(color: .black.opacity(0.4), radius: 4, y: 2)
                                    .padding(14)
                            }
                            .accessibilityLabel("Ver perfil completo de \(user.displayName)")
                        }
                    }
                    .scaleEffect(index == 0 ? 1.0 : 1.0 - CGFloat(index) * 0.04)
                    .offset(y: CGFloat(index) * -8)
                    .zIndex(Double(3 - index))
                }
            }
            .padding(.top, 12)

            // Action buttons
            actionButtons
                .padding(.top, 20)
                .padding(.bottom, 16)

            // Swipe counter
            swipeCounter
        }
        .padding(.horizontal, 16)
    }

    private var actionButtons: some View {
        HStack(spacing: 20) {
            // Pass
            CircleActionButton(icon: "xmark", color: .gray, size: 52) {
                if let user = store.discoveryUsers.first {
                    store.send(.swipeLeft(user))
                }
            }
            // Super Like
            CircleActionButton(icon: "star.fill", color: .yellow, size: 44) {
                if let user = store.discoveryUsers.first {
                    store.send(.superLike(user))
                }
            }
            // Like
            CircleActionButton(icon: "heart.fill", color: .fypPink, size: 52) {
                if let user = store.discoveryUsers.first {
                    store.send(.swipeRight(user))
                }
            }
            // Boost
            CircleActionButton(icon: "bolt.fill", color: .fypPurple, size: 44) {
                store.send(.activateBoost)
            }
        }
    }

    private var swipeCounter: some View {
        HStack {
            Image(systemName: "heart").foregroundColor(.fypPink).font(.caption)
            Text("\(store.remainingSwipes) curtidas restantes")
                .font(.caption).foregroundColor(.secondary)
            if store.remainingSwipes < 5 {
                Spacer()
                Button("Premium →") { store.send(.showPremiumUpsell(.dailyLimitReached)) }
                    .font(.caption.bold()).foregroundColor(.fypPink)
            }
        }
        .padding(.horizontal, 8)
    }

    // MARK: - Empty / Loading

    private var emptyView: some View {
        VStack(spacing: 20) {
            Image(systemName: "heart.slash").font(.system(size: 64))
                .foregroundStyle(LinearGradient.fypGradient)
            Text("Sem mais perfis por aqui").font(.title3.bold())
            Text("Volte mais tarde ou ajuste seus filtros para ver mais pessoas.")
                .font(.subheadline).foregroundColor(.secondary).multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            GradientButton(title: "Ajustar filtros", icon: "slider.horizontal.3", isFullWidth: false) {
                store.send(.showFilters(true))
            }
        }
    }

    private var loadingView: some View {
        VStack(spacing: 16) {
            ProgressView().scaleEffect(1.5).tint(.fypPink)
            Text("Encontrando perfis…").font(.subheadline).foregroundColor(.secondary)
        }
    }

    // MARK: - Toolbar

    @ToolbarContentBuilder
    private var toolbarItems: some ToolbarContent {
        ToolbarItem(placement: .topBarLeading) {
            Image(systemName: "heart.circle.fill")
                .font(.title2)
                .foregroundStyle(LinearGradient.fypGradient)
        }
        ToolbarItem(placement: .topBarTrailing) {
            Button { store.send(.showFilters(true)) } label: {
                Image(systemName: "slider.horizontal.3")
                    .foregroundColor(.fypPink)
            }
        }
    }
}

// MARK: - CircleActionButton

struct CircleActionButton: View {
    let icon: String
    let color: Color
    let size: CGFloat
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: icon)
                .font(.system(size: size * 0.38, weight: .bold))
                .foregroundColor(color)
                .frame(width: size, height: size)
                .background(Color(.systemBackground))
                .clipShape(Circle())
                .shadow(color: color.opacity(0.3), radius: 8, y: 4)
        }
        .accessibilityLabel(icon)
    }
}

// MARK: - Match Animation

struct MatchAnimationView: View {
    let match: Match
    let onDismiss: () -> Void
    @State private var scale: CGFloat = 0.5
    @State private var opacity: Double = 0

    var body: some View {
        ZStack {
            Color.black.opacity(0.75).ignoresSafeArea()
            VStack(spacing: 24) {
                Text("💕").font(.system(size: 72))
                    .scaleEffect(scale).opacity(opacity)
                GradientText(text: "É um Match!").font(.system(size: 36, weight: .black))
                Text("Você e \(match.userId) curtiram um ao outro!")
                    .font(.title3).foregroundColor(.white.opacity(0.9)).multilineTextAlignment(.center)
                HStack(spacing: 16) {
                    GradientButton(title: "Enviar mensagem", icon: "bubble.left.fill", isFullWidth: false) {
                        onDismiss()
                    }
                    OutlineButton(title: "Continuar", isFullWidth: false) { onDismiss() }
                }
            }
            .padding(32)
        }
        .onAppear {
            withAnimation(.spring(response: 0.5, dampingFraction: 0.6)) {
                scale = 1; opacity = 1
            }
        }
    }
}

// MARK: - Filters Sheet

struct FiltersSheet: View {
    @Bindable var store: StoreOf<DiscoveryFeature>
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            Form {
                Section("Faixa etária") {
                    HStack {
                        Text("\(store.tempAgeMin) – \(store.tempAgeMax) anos")
                        Spacer()
                    }
                    RangeSlider(low: Binding(get: { Double(store.tempAgeMin) }, set: { store.tempAgeMin = Int($0) }),
                                high: Binding(get: { Double(store.tempAgeMax) }, set: { store.tempAgeMax = Int($0) }),
                                range: 18...65)
                }
                Section("Distância máxima") {
                    HStack {
                        Text("\(store.tempMaxDistance) km")
                        Spacer()
                    }
                    Slider(value: Binding(get: { Double(store.tempMaxDistance) }, set: { store.tempMaxDistance = Int($0) }), in: 5...300, step: 5)
                        .accentColor(.fypPink)
                }
                Section("Interesse") {
                    Picker("Mostrar", selection: $store.tempGenderInterest) {
                        ForEach(GenderInterest.allCases, id: \.self) { g in
                            Text(g.displayName).tag(g)
                        }
                    }
                    .pickerStyle(.segmented)
                }
            }
            .navigationTitle("Filtros")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cancelar") { dismiss() }.foregroundColor(.fypPink)
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Aplicar") {
                        store.send(.applyFilters)
                        dismiss()
                    }.foregroundColor(.fypPink).fontWeight(.semibold)
                }
            }
        }
    }
}

// MARK: - RangeSlider (simple two-thumb)

struct RangeSlider: View {
    @Binding var low: Double
    @Binding var high: Double
    let range: ClosedRange<Double>
    var body: some View {
        VStack {
            Slider(value: $low, in: range.lowerBound...high - 1, step: 1).accentColor(.fypPink)
            Slider(value: $high, in: low + 1...range.upperBound, step: 1).accentColor(.fypPurple)
        }
    }
}

// MARK: - Premium Upsell Sheet

struct PremiumUpsellSheet: View {
    let reason: PremiumUpsellReason?
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "crown.fill").font(.system(size: 52))
                .foregroundStyle(LinearGradient.fypGradient).padding(.top, 32)
            GradientText(text: "Vá Premium")
            Text(reason?.message ?? "Desbloqueie tudo e encontre conexões reais.")
                .font(.subheadline).foregroundColor(.secondary).multilineTextAlignment(.center).padding(.horizontal, 24)
            GradientButton(title: "Ver planos") { dismiss() }
                .padding(.horizontal, 24)
            Button("Agora não") { dismiss() }
                .font(.subheadline).foregroundColor(.secondary).padding(.bottom, 32)
        }
        .presentationDetents([.medium])
    }
}

enum PremiumUpsellReason {
    case dailyLimitReached, viewLikes, credits
    var message: String {
        switch self {
        case .dailyLimitReached: return "Você chegou no limite de curtidas de hoje. Com Premium são 100/dia."
        case .viewLikes: return "Veja quem curtiu você antes de swipear — e responda na hora certa."
        case .credits: return "Tenha 10 créditos de IA todo dia sem precisar de anúncios."
        }
    }
}
