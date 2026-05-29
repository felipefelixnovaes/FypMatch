// WelcomeView.swift — FypMatch iOS
// Tela de boas-vindas e onboarding — port de WelcomeScreen.kt

import SwiftUI
import ComposableArchitecture

struct WelcomeView: View {
    @Bindable var store: StoreOf<AuthFeature>

    private let pages: [(icon: String, title: String, body: String)] = [
        ("heart.fill",
         "Bem-vindo ao FypMatch",
         "O app de relacionamento criado para quem nunca se sentiu em casa nos outros."),
        ("person.2.fill",
         "Um espaço feito para você",
         "LGBTQIA+, neurodiverso ou simplesmente cansado da superficialidade? Aqui você é bem-vindo do jeito que é."),
        ("brain.head.profile",
         "Matches que fazem sentido",
         "Nosso score analisa personalidade, valores e intenções — não só fotos. Menos swipes, mais conexões reais."),
        ("bubble.left.and.bubble.right.fill",
         "Você não está sozinho",
         "Seu conselheiro IA está sempre aqui. Para sugerir icebreakers, ajudar na conversa ou simplesmente apoiar quando a ansiedade aparecer."),
        ("sparkles",
         "Pronto para começar?",
         "Junte-se a milhares de pessoas encontrando conexões reais todos os dias. Seu match está esperando."),
    ]

    @State private var currentPage = 0
    @State private var autoTimer: Timer?

    var body: some View {
        ZStack {
            // Background gradient
            LinearGradient.fypGradientVertical.ignoresSafeArea()

            VStack(spacing: 0) {
                // Logo
                Spacer()
                Image(systemName: "heart.circle.fill")
                    .font(.system(size: 72))
                    .foregroundColor(.white)
                    .shadow(color: .black.opacity(0.2), radius: 12, y: 6)
                    .padding(.bottom, 8)

                GradientText(text: "FypMatch")
                    .font(.system(size: 38, weight: .bold))
                    .foregroundColor(.white)

                Text("Conexões Reais")
                    .font(.title3)
                    .foregroundColor(.white.opacity(0.85))
                    .padding(.bottom, 40)

                // Onboarding carousel
                TabView(selection: $currentPage) {
                    ForEach(pages.indices, id: \.self) { i in
                        OnboardingCard(
                            icon: pages[i].icon,
                            title: pages[i].title,
                            body: pages[i].body
                        )
                        .tag(i)
                    }
                }
                .tabViewStyle(.page(indexDisplayMode: .always))
                .frame(height: 220)
                .padding(.bottom, 32)

                Spacer()

                // CTAs
                VStack(spacing: 12) {
                    Button {
                        store.send(.showRegister(true))
                    } label: {
                        Text("Criar conta grátis")
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundColor(.fypPink)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                    }

                    Button {
                        store.send(.showRegister(false))
                    } label: {
                        Text("Já tenho conta")
                            .font(.system(size: 15, weight: .medium))
                            .foregroundColor(.white.opacity(0.9))
                    }
                }
                .padding(.horizontal, 24)
                .padding(.bottom, 40)
            }
        }
        .onAppear { startAutoScroll() }
        .onDisappear { autoTimer?.invalidate() }
        .sheet(isPresented: $store.showingRegister) {
            RegisterView(store: store)
        }
        .sheet(isPresented: .init(
            get: { !store.showingRegister && store.currentUser == nil },
            set: { _ in }
        )) {
            LoginView(store: store)
        }
    }

    private func startAutoScroll() {
        autoTimer = Timer.scheduledTimer(withTimeInterval: 3.5, repeats: true) { _ in
            withAnimation { currentPage = (currentPage + 1) % pages.count }
        }
    }
}

// MARK: - OnboardingCard

private struct OnboardingCard: View {
    let icon: String
    let title: String
    let body: String

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 40))
                .foregroundColor(.white)
            Text(title)
                .font(.title3.bold())
                .foregroundColor(.white)
                .multilineTextAlignment(.center)
            Text(body)
                .font(.subheadline)
                .foregroundColor(.white.opacity(0.85))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 24)
        }
        .padding(.vertical, 8)
    }
}
