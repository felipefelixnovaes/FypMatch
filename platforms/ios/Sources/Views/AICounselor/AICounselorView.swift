// AICounselorView.swift — FypMatch iOS
// Conselheiro IA de relacionamentos — port de AICounselorScreen.kt

import SwiftUI

struct AICounselorView: View {
    var contextUserId: String? = nil
    @StateObject private var viewModel = AICounselorViewModel()
    @State private var messageText = ""
    @State private var showCreditsInfo = false
    @FocusState private var inputFocused: Bool

    // Suggested prompts
    private let suggestions = [
        "Como quebrar o gelo?",
        "Ele sumiu, e agora?",
        "Tenho ansiedade social",
        "Como saber se há interesse?",
        "Preciso de um icebreaker",
    ]

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Credits bar
                creditsBar

                Divider()

                // Messages
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            if viewModel.messages.isEmpty { welcomeMessage }
                            ForEach(viewModel.messages) { msg in
                                AICounselorBubble(message: msg)
                                    .id(msg.id)
                            }
                            if viewModel.isTyping { AITypingIndicator() }
                        }
                        .padding(16)
                    }
                    .onChange(of: viewModel.messages.count) { _, _ in
                        if let last = viewModel.messages.last {
                            withAnimation { proxy.scrollTo(last.id, anchor: .bottom) }
                        }
                    }
                }

                // Suggestions chips
                if viewModel.messages.isEmpty {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(suggestions, id: \.self) { s in
                                Button(s) { sendMessage(s) }
                                    .font(.subheadline)
                                    .padding(.horizontal, 12).padding(.vertical, 7)
                                    .background(Color.fypPink.opacity(0.1))
                                    .foregroundColor(.fypPink)
                                    .clipShape(Capsule())
                                    .overlay(Capsule().stroke(Color.fypPink.opacity(0.4), lineWidth: 1))
                            }
                        }
                        .padding(.horizontal, 16).padding(.vertical, 8)
                    }
                }

                Divider()

                // Input
                HStack(spacing: 10) {
                    TextField("Pergunte ao seu conselheiro…", text: $messageText, axis: .vertical)
                        .lineLimit(1...3).font(.system(size: 15))
                        .padding(.horizontal, 12).padding(.vertical, 9)
                        .background(Color(.secondarySystemBackground))
                        .clipShape(RoundedRectangle(cornerRadius: 20))
                        .focused($inputFocused)

                    Button {
                        guard !messageText.trimmingCharacters(in: .whitespaces).isEmpty else { return }
                        sendMessage(messageText)
                        messageText = ""
                    } label: {
                        Image(systemName: "arrow.up.circle.fill")
                            .font(.system(size: 32))
                            .foregroundStyle(messageText.isEmpty
                                ? AnyShapeStyle(Color.secondary.opacity(0.3))
                                : AnyShapeStyle(LinearGradient.fypGradient))
                    }
                    .disabled(messageText.trimmingCharacters(in: .whitespaces).isEmpty || viewModel.credits <= 0)
                }
                .padding(.horizontal, 12).padding(.vertical, 10)
            }
            .navigationTitle("Conselheiro IA 🤖")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { showCreditsInfo = true } label: {
                        HStack(spacing: 4) {
                            Image(systemName: "brain.head.profile").font(.caption)
                            Text("\(viewModel.credits)").font(.subheadline.bold())
                        }
                        .foregroundColor(.fypPink)
                    }
                }
            }
            .alert("Créditos de IA", isPresented: $showCreditsInfo) {
                Button("OK") {}
            } message: {
                Text("Você tem \(viewModel.credits) créditos. Cada mensagem usa 1 crédito. Usuários FREE ganham 9 créditos/dia assistindo anúncios. Premium: 10/dia. VIP: 25/dia.")
            }
        }
        .onAppear { viewModel.loadCredits() }
    }

    private var creditsBar: some View {
        HStack {
            Image(systemName: "brain.head.profile").font(.caption).foregroundColor(.fypPink)
            Text(viewModel.credits > 0 ? "\(viewModel.credits) créditos restantes" : "Sem créditos")
                .font(.caption).foregroundColor(viewModel.credits > 0 ? .secondary : .red)
            Spacer()
            if viewModel.credits == 0 {
                Button("Ganhar créditos") { viewModel.watchAd() }
                    .font(.caption.bold()).foregroundColor(.fypPink)
            }
        }
        .padding(.horizontal, 16).padding(.vertical, 8)
        .background(Color(.secondarySystemBackground).opacity(0.5))
    }

    private var welcomeMessage: some View {
        VStack(spacing: 12) {
            Image(systemName: "brain.head.profile").font(.system(size: 48))
                .foregroundStyle(LinearGradient.fypGradient)
            Text("Olá! Sou seu conselheiro de relacionamentos 💜")
                .font(.headline).multilineTextAlignment(.center)
            Text("Posso ajudar com icebreakers, ansiedade social, comunicação ou qualquer dúvida sobre seus matches. O que você gostaria de saber?")
                .font(.subheadline).foregroundColor(.secondary).multilineTextAlignment(.center)
        }
        .padding(24)
    }

    private func sendMessage(_ text: String) {
        guard viewModel.credits > 0 else { return }
        viewModel.sendMessage(text, contextUserId: contextUserId)
        inputFocused = false
    }
}

// MARK: - AICounselorBubble

struct AICounselorBubble: View {
    let message: AICounselorMessage

    var body: some View {
        HStack(alignment: .bottom, spacing: 8) {
            if !message.isUser {
                Image(systemName: "brain.head.profile")
                    .font(.system(size: 22))
                    .foregroundStyle(LinearGradient.fypGradient)
                    .frame(width: 34, height: 34)
                    .background(Color.fypPink.opacity(0.1))
                    .clipShape(Circle())
            }
            VStack(alignment: message.isUser ? .trailing : .leading, spacing: 3) {
                Text(message.content)
                    .font(.system(size: 15))
                    .foregroundColor(message.isUser ? .white : .primary)
                    .padding(.horizontal, 14).padding(.vertical, 10)
                    .background(message.isUser
                        ? AnyShapeStyle(LinearGradient.fypGradient)
                        : AnyShapeStyle(Color(.secondarySystemBackground))
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 18))
                Text(message.timeString).font(.caption2).foregroundColor(.secondary)
            }
            if message.isUser { Spacer(minLength: 40) }
        }
        .frame(maxWidth: .infinity, alignment: message.isUser ? .trailing : .leading)
    }
}

// MARK: - AITypingIndicator

struct AITypingIndicator: View {
    @State private var animate = false
    var body: some View {
        HStack(alignment: .bottom, spacing: 8) {
            Image(systemName: "brain.head.profile")
                .foregroundStyle(LinearGradient.fypGradient)
                .frame(width: 34, height: 34)
                .background(Color.fypPink.opacity(0.1))
                .clipShape(Circle())
            HStack(spacing: 4) {
                ForEach(0..<3) { i in
                    Circle().fill(Color.secondary.opacity(0.6)).frame(width: 7, height: 7)
                        .scaleEffect(animate ? 1.3 : 0.7)
                        .animation(.easeInOut(duration: 0.5).repeatForever().delay(Double(i) * 0.18), value: animate)
                }
            }
            .padding(.horizontal, 14).padding(.vertical, 12)
            .background(Color(.secondarySystemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 18))
            Spacer()
        }
        .onAppear { animate = true }
    }
}

// MARK: - Models

struct AICounselorMessage: Identifiable {
    let id = UUID()
    let content: String
    let isUser: Bool
    let timestamp: Date
    var timeString: String {
        let f = DateFormatter(); f.dateFormat = "HH:mm"; return f.string(from: timestamp)
    }
}

// MARK: - ViewModel

@MainActor
class AICounselorViewModel: ObservableObject {
    @Published var messages: [AICounselorMessage] = []
    @Published var credits = 9
    @Published var isTyping = false

    func loadCredits() {
        // Load from FirebaseService / local cache
        credits = UserDefaults.standard.integer(forKey: "ai_credits_remaining").clamped(to: 0...25)
        if credits == 0 { credits = 9 } // default for new users
    }

    func sendMessage(_ text: String, contextUserId: String? = nil) {
        guard credits > 0 else { return }
        messages.append(AICounselorMessage(content: text, isUser: true, timestamp: Date()))
        credits -= 1
        UserDefaults.standard.set(credits, forKey: "ai_credits_remaining")
        isTyping = true
        Task {
            try? await Task.sleep(for: .seconds(1.8))
            let response = await generateResponse(for: text, contextUserId: contextUserId)
            isTyping = false
            messages.append(AICounselorMessage(content: response, isUser: false, timestamp: Date()))
        }
    }

    func watchAd() {
        // AdMob rewarded ad — grant 3 credits on completion
        credits = min(credits + 3, 9)
        UserDefaults.standard.set(credits, forKey: "ai_credits_remaining")
    }

    private func generateResponse(for query: String, contextUserId: String?) async -> String {
        // In production: calls DeepSeek/Anthropic API via Firebase Function
        let responses = [
            "Que ótima pergunta! Uma dica poderosa: comece com algo específico do perfil da pessoa — mostra que você realmente prestou atenção. Tente: \"Adorei que você viajou para [lugar]. Qual foi o momento mais marcante?\" 🌍",
            "Entendo a ansiedade! Lembre-se: a outra pessoa também pode estar nervosa. Uma mensagem curta e autêntica é sempre melhor que algo perfeito mas artificial. O que você tem em comum com ela?",
            "Quando alguém some, pode ser por mil razões que não têm nada a ver com você. Enviar uma mensagem leve e sem pressão é uma opção — mas também está tudo bem seguir em frente. Você merece alguém presente 💜",
        ]
        return responses.randomElement() ?? "Estou aqui para ajudar! Pode me contar mais sobre a situação? Quanto mais contexto, melhor meu conselho."
    }
}

extension Comparable {
    func clamped(to range: ClosedRange<Self>) -> Self {
        return min(max(self, range.lowerBound), range.upperBound)
    }
}
