// ChatView.swift — FypMatch iOS
// Chat em tempo real — port de ChatScreen.kt + EnhancedChatScreen.kt

import SwiftUI

struct ChatView: View {
    let conversation: Conversation
    @StateObject private var viewModel: ChatViewModel
    @State private var messageText = ""
    @State private var showAICounselor = false
    @FocusState private var inputFocused: Bool

    init(conversation: Conversation) {
        self.conversation = conversation
        self._viewModel = StateObject(wrappedValue: ChatViewModel(conversationId: conversation.id))
    }

    var body: some View {
        VStack(spacing: 0) {
            // Messages list
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 8) {
                        ForEach(viewModel.messages) { message in
                            MessageBubble(message: message, isCurrentUser: message.senderId == FirebaseService.shared.currentUser?.id)
                                .id(message.id)
                        }
                        if viewModel.isOtherUserTyping {
                            TypingIndicator()
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                }
                .onChange(of: viewModel.messages.count) { _, _ in
                    if let last = viewModel.messages.last {
                        withAnimation { proxy.scrollTo(last.id, anchor: .bottom) }
                    }
                }
            }

            Divider()

            // Input bar
            VStack(spacing: 0) {
                // AI suggestion banner
                if let suggestion = viewModel.aiSuggestion {
                    AISuggestionBanner(suggestion: suggestion) {
                        messageText = suggestion
                        viewModel.clearAISuggestion()
                    } onDismiss: {
                        viewModel.clearAISuggestion()
                    }
                }

                HStack(spacing: 10) {
                    // AI button
                    Button { showAICounselor = true } label: {
                        Image(systemName: "brain.head.profile")
                            .font(.system(size: 20))
                            .foregroundStyle(LinearGradient.fypGradient)
                    }
                    .accessibilityLabel("Abrir conselheiro IA")

                    // Text input
                    HStack {
                        TextField("Mensagem…", text: $messageText, axis: .vertical)
                            .lineLimit(1...4)
                            .font(.system(size: 15))
                            .focused($inputFocused)
                        if !messageText.isEmpty {
                            Button { messageText = "" } label: {
                                Image(systemName: "xmark.circle.fill")
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                    .padding(.horizontal, 12).padding(.vertical, 8)
                    .background(Color(.secondarySystemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 20))

                    // Send button
                    Button {
                        guard !messageText.trimmingCharacters(in: .whitespaces).isEmpty else { return }
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    } label: {
                        Image(systemName: "arrow.up.circle.fill")
                            .font(.system(size: 32))
                            .foregroundStyle(messageText.isEmpty
                                ? AnyShapeStyle(Color.secondary.opacity(0.4))
                                : AnyShapeStyle(LinearGradient.fypGradient))
                    }
                    .disabled(messageText.trimmingCharacters(in: .whitespaces).isEmpty)
                    .accessibilityLabel("Enviar mensagem")
                }
                .padding(.horizontal, 12).padding(.vertical, 10)
            }
        }
        .navigationTitle(conversation.otherUserName)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                NavigationLink {
                    Text("Perfil de \(conversation.otherUserName)") // UserDetailView
                } label: {
                    UserAvatar(url: conversation.otherUserPhotoURL, size: 32,
                               showOnline: conversation.isOtherUserOnline)
                }
            }
        }
        .sheet(isPresented: $showAICounselor) {
            AICounselorView(contextUserId: conversation.otherUserId)
        }
        .onAppear { viewModel.loadMessages() }
        .onDisappear { viewModel.stopListening() }
    }
}

// MARK: - MessageBubble

struct MessageBubble: View {
    let message: Message
    let isCurrentUser: Bool

    var body: some View {
        HStack {
            if isCurrentUser { Spacer(minLength: 60) }
            VStack(alignment: isCurrentUser ? .trailing : .leading, spacing: 3) {
                Text(message.content)
                    .font(.system(size: 15))
                    .foregroundColor(isCurrentUser ? .white : .primary)
                    .padding(.horizontal, 14).padding(.vertical, 10)
                    .background(isCurrentUser
                        ? AnyShapeStyle(LinearGradient.fypGradient)
                        : AnyShapeStyle(Color(.secondarySystemBackground))
                    )
                    .clipShape(BubbleShape(isCurrentUser: isCurrentUser))
                HStack(spacing: 4) {
                    Text(message.timeString).font(.caption2).foregroundColor(.secondary)
                    if isCurrentUser {
                        Image(systemName: message.isRead ? "checkmark.circle.fill" : "checkmark.circle")
                            .font(.caption2)
                            .foregroundColor(message.isRead ? .fypPink : .secondary)
                    }
                }
            }
            if !isCurrentUser { Spacer(minLength: 60) }
        }
    }
}

// MARK: - BubbleShape

struct BubbleShape: Shape {
    let isCurrentUser: Bool
    func path(in rect: CGRect) -> Path {
        let r: CGFloat = 18
        let tail: CGFloat = 6
        var path = Path()
        if isCurrentUser {
            path.addRoundedRect(in: CGRect(x: 0, y: 0, width: rect.width - tail, height: rect.height),
                                cornerSize: CGSize(width: r, height: r))
        } else {
            path.addRoundedRect(in: CGRect(x: tail, y: 0, width: rect.width - tail, height: rect.height),
                                cornerSize: CGSize(width: r, height: r))
        }
        return path
    }
}

// MARK: - TypingIndicator

struct TypingIndicator: View {
    @State private var dot1: CGFloat = 0
    @State private var dot2: CGFloat = 0
    @State private var dot3: CGFloat = 0

    var body: some View {
        HStack {
            HStack(spacing: 4) {
                ForEach([(dot1, 0.0), (dot2, 0.2), (dot3, 0.4)], id: \.1) { (y, delay) in
                    Circle().fill(Color.secondary.opacity(0.6))
                        .frame(width: 7, height: 7)
                        .offset(y: y)
                }
            }
            .padding(.horizontal, 14).padding(.vertical, 12)
            .background(Color(.secondarySystemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 18))
            Spacer()
        }
        .onAppear {
            let animation = Animation.easeInOut(duration: 0.45).repeatForever().delay(0)
            withAnimation(animation) { dot1 = -5 }
            withAnimation(animation.delay(0.2)) { dot2 = -5 }
            withAnimation(animation.delay(0.4)) { dot3 = -5 }
        }
    }
}

// MARK: - AISuggestionBanner

struct AISuggestionBanner: View {
    let suggestion: String
    let onUse: () -> Void
    let onDismiss: () -> Void

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: "brain.head.profile").foregroundStyle(LinearGradient.fypGradient)
            Text(suggestion).font(.subheadline).lineLimit(2).foregroundColor(.primary)
            Spacer()
            VStack(spacing: 4) {
                Button("Usar", action: onUse)
                    .font(.caption.bold()).foregroundColor(.fypPink)
                Button("Ignorar", action: onDismiss)
                    .font(.caption).foregroundColor(.secondary)
            }
        }
        .padding(10)
        .background(Color(.tertiarySystemBackground))
        .overlay(Rectangle().frame(height: 1).foregroundColor(.secondary.opacity(0.2)), alignment: .top)
    }
}

// MARK: - ChatViewModel

@MainActor
class ChatViewModel: ObservableObject {
    @Published var messages: [Message] = []
    @Published var isOtherUserTyping = false
    @Published var aiSuggestion: String?

    private let conversationId: String

    init(conversationId: String) {
        self.conversationId = conversationId
    }

    func loadMessages() {
        FirebaseService.shared.listenToMessages(conversationId: conversationId) { [weak self] msgs in
            self?.messages = msgs
            if let lastMsg = msgs.last, lastMsg.senderId != FirebaseService.shared.currentUser?.id {
                self?.generateAISuggestion(for: lastMsg.content)
            }
        }
    }

    func sendMessage(_ content: String) {
        guard let userId = FirebaseService.shared.currentUser?.id else { return }
        let message = Message(
            id: UUID().uuidString,
            conversationId: conversationId,
            senderId: userId,
            content: content,
            timestamp: Date(),
            isRead: false
        )
        FirebaseService.shared.sendMessage(message)
    }

    func stopListening() {
        FirebaseService.shared.stopListening(conversationId: conversationId)
    }

    func clearAISuggestion() { aiSuggestion = nil }

    private func generateAISuggestion(for receivedMessage: String) {
        // AI suggestion generation - integrates with AICounselorRepository
        // Simplified: trigger only for short messages to avoid noise
        guard receivedMessage.count < 50 else { return }
        Task {
            try? await Task.sleep(for: .seconds(1.5))
            aiSuggestion = "Que legal! Conta mais sobre isso 😊"
        }
    }
}
