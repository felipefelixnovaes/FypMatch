// ConversationsView.swift — FypMatch iOS
// Lista de conversas — port de ConversationsScreen.kt

import SwiftUI

struct ConversationsView: View {
    @StateObject private var service = FirebaseService.shared
    @State private var searchText = ""
    @State private var selectedConversation: Conversation?

    var filteredConversations: [Conversation] {
        guard !searchText.isEmpty else { return service.conversations }
        return service.conversations.filter {
            $0.otherUserName.localizedCaseInsensitiveContains(searchText)
        }
    }

    var body: some View {
        NavigationStack {
            Group {
                if service.conversations.isEmpty {
                    emptyView
                } else {
                    List {
                        // New matches (no messages yet)
                        let newMatches = service.conversations.filter { $0.lastMessage.isEmpty }
                        if !newMatches.isEmpty {
                            Section("Novos matches 💕") {
                                ScrollView(.horizontal, showsIndicators: false) {
                                    HStack(spacing: 16) {
                                        ForEach(newMatches) { conv in
                                            NewMatchBubble(conversation: conv)
                                                .onTapGesture { selectedConversation = conv }
                                        }
                                    }
                                    .padding(.horizontal, 4)
                                }
                                .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                            }
                        }

                        // Active conversations
                        let active = filteredConversations.filter { !$0.lastMessage.isEmpty }
                        if !active.isEmpty {
                            Section("Mensagens") {
                                ForEach(active) { conv in
                                    ConversationRow(conversation: conv)
                                        .onTapGesture { selectedConversation = conv }
                                }
                            }
                        }
                    }
                    .listStyle(.insetGrouped)
                    .searchable(text: $searchText, prompt: "Buscar conversas")
                }
            }
            .navigationTitle("Mensagens")
            .navigationDestination(item: $selectedConversation) { conv in
                ChatView(conversation: conv)
            }
        }
        .onAppear { service.loadConversations() }
    }

    private var emptyView: some View {
        VStack(spacing: 16) {
            Image(systemName: "bubble.left.and.bubble.right")
                .font(.system(size: 64)).foregroundStyle(LinearGradient.fypGradient)
            Text("Nenhuma conversa ainda").font(.title3.bold())
            Text("Quando você e alguém curtirem um ao outro, a conversa aparece aqui.")
                .font(.subheadline).foregroundColor(.secondary).multilineTextAlignment(.center)
                .padding(.horizontal, 32)
        }
    }
}

// MARK: - NewMatchBubble

struct NewMatchBubble: View {
    let conversation: Conversation
    var body: some View {
        VStack(spacing: 6) {
            ZStack(alignment: .bottomTrailing) {
                UserAvatar(url: conversation.otherUserPhotoURL, size: 64, showOnline: conversation.isOtherUserOnline)
                Circle().fill(LinearGradient.fypGradient)
                    .frame(width: 18, height: 18)
                    .overlay(Image(systemName: "heart.fill").font(.system(size: 9)).foregroundColor(.white))
            }
            Text(conversation.otherUserName.components(separatedBy: " ").first ?? "")
                .font(.caption).lineLimit(1)
        }
        .frame(width: 72)
    }
}

// MARK: - ConversationRow

struct ConversationRow: View {
    let conversation: Conversation

    var body: some View {
        HStack(spacing: 12) {
            UserAvatar(url: conversation.otherUserPhotoURL, size: 54,
                       showOnline: conversation.isOtherUserOnline)

            VStack(alignment: .leading, spacing: 3) {
                HStack {
                    Text(conversation.otherUserName)
                        .font(.system(size: 15, weight: conversation.unreadCount > 0 ? .semibold : .regular))
                    Spacer()
                    Text(conversation.lastMessageTimeAgo)
                        .font(.caption).foregroundColor(.secondary)
                }
                HStack {
                    Text(conversation.lastMessage)
                        .font(.subheadline)
                        .foregroundColor(conversation.unreadCount > 0 ? .primary : .secondary)
                        .lineLimit(1)
                    Spacer()
                    if conversation.unreadCount > 0 {
                        Text("\(conversation.unreadCount)")
                            .font(.caption.bold()).foregroundColor(.white)
                            .padding(.horizontal, 6).padding(.vertical, 2)
                            .background(Color.fypPink).clipShape(Capsule())
                    }
                }
            }
        }
        .padding(.vertical, 4)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(conversation.otherUserName), \(conversation.lastMessage), \(conversation.lastMessageTimeAgo)")
    }
}

// MARK: - Conversation model stub

struct Conversation: Identifiable, Hashable {
    let id: String
    let otherUserId: String
    let otherUserName: String
    let otherUserPhotoURL: String
    var lastMessage: String
    var lastMessageTimestamp: Date
    var unreadCount: Int
    var isOtherUserOnline: Bool

    var lastMessageTimeAgo: String {
        let diff = Date().timeIntervalSince(lastMessageTimestamp)
        if diff < 60 { return "agora" }
        if diff < 3600 { return "\(Int(diff/60))min" }
        if diff < 86400 { return "\(Int(diff/3600))h" }
        return "\(Int(diff/86400))d"
    }
}

extension FirebaseService {
    var conversations: [Conversation] { [] } // populated by Firestore listener
    func loadConversations() { /* Firestore listener setup */ }
}
