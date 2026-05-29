// ChatFeature.swift — FypMatch iOS
// TCA Feature para chat em tempo real

import Foundation
import ComposableArchitecture

@Reducer
struct ChatFeature {

    // MARK: - State

    @ObservableState
    struct State: Equatable {
        var conversation: Conversation
        var messages: [Message] = []
        var currentUser: User?
        var otherUser: User?
        var isLoadingMessages = false
        var isSendingMessage = false
        var messageText = ""
        var isOtherUserTyping = false
        var isCurrentUserTyping = false
        var loadingError: String?
        var showAISuggestions = false
        var aiSuggestions: [String] = []
        var isLoadingAISuggestions = false
        var selectedMediaURL: URL?
        var isShowingMediaPicker = false
        var reportedMessage: Message?
        var isShowingReportSheet = false

        // AI credits (mostra banner após N mensagens sem resposta)
        var messagesSentWithoutResponse = 0
        var showAIBanner: Bool { messagesSentWithoutResponse >= 2 }

        var canSendMessage: Bool {
            !messageText.trimmingCharacters(in: .whitespaces).isEmpty && !isSendingMessage
        }
    }

    // MARK: - Action

    enum Action: BindableAction, Equatable {
        case binding(BindingAction<State>)
        case onAppear
        case onDisappear
        case loadMessages
        case messagesLoaded([Message])
        case sendMessage
        case messageSent(Result<Void, Error>)
        case setTyping(Bool)
        case typingStatusUpdated(Bool) // outro usuário
        case sendAISuggestion(String)
        case loadAISuggestions
        case aiSuggestionsLoaded([String])
        case selectMedia(URL)
        case sendMedia
        case reportMessage(Message)
        case dismissReport
        case markAsRead
        case scrollToBottom
        case showProfile(User)
    }

    // MARK: - Dependencies

    @Dependency(\.firebaseService) var firebaseService

    // MARK: - Body

    var body: some ReducerOf<Self> {
        BindingReducer()
        Reduce { state, action in
            switch action {

            case .onAppear:
                state.currentUser = firebaseService.currentUser
                if let conversationId = state.conversation.id as String? {
                    let otherUserId = state.conversation.getOtherUserId(
                        currentUserId: state.currentUser?.id ?? ""
                    )
                    return .merge(
                        .send(.loadMessages),
                        .send(.markAsRead)
                    )
                }
                return .none

            case .onDisappear:
                // Parar de digitar ao sair
                return .run { [
                    conversationId = state.conversation.id,
                    userId = state.currentUser?.id ?? ""
                ] _ in
                    await firebaseService.setTypingStatus(
                        false,
                        conversationId: conversationId,
                        userId: userId
                    )
                }

            case .loadMessages:
                state.isLoadingMessages = true
                return .run { [conversationId = state.conversation.id] send in
                    let messages = await firebaseService.loadMessagesOnce(conversationId: conversationId)
                    await send(.messagesLoaded(messages))
                }

            case let .messagesLoaded(messages):
                state.isLoadingMessages = false
                state.messages = messages
                // Atualizar contador de mensagens sem resposta
                let currentUserId = state.currentUser?.id ?? ""
                let lastMessages = messages.suffix(5)
                let allFromMe = lastMessages.allSatisfy { $0.senderId == currentUserId }
                state.messagesSentWithoutResponse = allFromMe ? lastMessages.count : 0
                return .send(.scrollToBottom)

            case .sendMessage:
                guard state.canSendMessage,
                      let currentUser = state.currentUser else { return .none }

                let message = Message(
                    conversationId: state.conversation.id,
                    senderId: currentUser.id,
                    receiverId: state.conversation.getOtherUserId(currentUserId: currentUser.id) ?? "",
                    content: state.messageText.trimmingCharacters(in: .whitespaces)
                )

                let text = state.messageText
                state.messageText = ""
                state.isSendingMessage = true

                return .run { [conversationId = state.conversation.id, userId = currentUser.id] send in
                    do {
                        try await firebaseService.sendMessageAsync(message)
                        await send(.messageSent(.success(())))
                    } catch {
                        await send(.messageSent(.failure(error)))
                    }
                }

            case .messageSent(.success):
                state.isSendingMessage = false
                return .none

            case let .messageSent(.failure(error)):
                state.isSendingMessage = false
                state.loadingError = error.localizedDescription
                return .none

            case let .setTyping(isTyping):
                state.isCurrentUserTyping = isTyping
                return .run { [
                    conversationId = state.conversation.id,
                    userId = state.currentUser?.id ?? ""
                ] _ in
                    await firebaseService.setTypingStatus(
                        isTyping,
                        conversationId: conversationId,
                        userId: userId
                    )
                }

            case let .typingStatusUpdated(isTyping):
                state.isOtherUserTyping = isTyping
                return .none

            case let .sendAISuggestion(suggestion):
                state.messageText = suggestion
                state.showAISuggestions = false
                return .send(.sendMessage)

            case .loadAISuggestions:
                state.isLoadingAISuggestions = true
                let lastMessages = state.messages.suffix(10).map { "\($0.senderId): \($0.content)" }
                return .run { send in
                    // Gerar sugestões baseadas no contexto da conversa
                    let suggestions = await generateAISuggestions(context: lastMessages)
                    await send(.aiSuggestionsLoaded(suggestions))
                }

            case let .aiSuggestionsLoaded(suggestions):
                state.isLoadingAISuggestions = false
                state.aiSuggestions = suggestions
                state.showAISuggestions = true
                return .none

            case let .selectMedia(url):
                state.selectedMediaURL = url
                return .none

            case .sendMedia:
                guard let mediaURL = state.selectedMediaURL,
                      let currentUser = state.currentUser else { return .none }
                state.selectedMediaURL = nil

                return .run { [
                    conversationId = state.conversation.id,
                    receiverId = state.conversation.getOtherUserId(currentUserId: currentUser.id) ?? ""
                ] send in
                    do {
                        let remoteURL = try await firebaseService.uploadMessageMedia(
                            localURL: mediaURL,
                            conversationId: conversationId
                        )
                        let message = Message(
                            conversationId: conversationId,
                            senderId: currentUser.id,
                            receiverId: receiverId,
                            content: remoteURL,
                            type: .image,
                            mediaURL: remoteURL,
                            mediaType: .image
                        )
                        try await firebaseService.sendMessageAsync(message)
                        await send(.messageSent(.success(())))
                    } catch {
                        await send(.messageSent(.failure(error)))
                    }
                }

            case let .reportMessage(message):
                state.reportedMessage = message
                state.isShowingReportSheet = true
                return .none

            case .dismissReport:
                state.reportedMessage = nil
                state.isShowingReportSheet = false
                return .none

            case .markAsRead:
                return .run { [
                    conversationId = state.conversation.id,
                    userId = state.currentUser?.id ?? ""
                ] _ in
                    await firebaseService.markConversationAsRead(
                        conversationId: conversationId,
                        userId: userId
                    )
                }

            case .scrollToBottom:
                return .none

            case .showProfile:
                return .none

            case .binding:
                // Detectar digitação quando messageText muda
                if state.messageText.isEmpty && state.isCurrentUserTyping {
                    return .send(.setTyping(false))
                } else if !state.messageText.isEmpty && !state.isCurrentUserTyping {
                    return .send(.setTyping(true))
                }
                return .none
            }
        }
    }
}

// MARK: - AI Suggestions (local logic, sem API externa)

private func generateAISuggestions(context: [String]) async -> [String] {
    // Sugestões baseadas em análise simples do contexto
    // Em produção, chamar API de IA (DeepSeek, OpenAI etc.)
    let defaultSuggestions = [
        "Que tipo de música você curte? 🎵",
        "Você já foi a algum festival? 🎪",
        "Qual foi o melhor lugar que você já visitou? ✈️",
        "O que você faz quando não está trabalhando? 😊",
        "Você prefere praia ou cachoeira? 🏖️"
    ]

    // Rotacionar sugestões baseado no número de mensagens
    let offset = context.count % defaultSuggestions.count
    var rotated = Array(defaultSuggestions[offset...] + defaultSuggestions[..<offset])
    return Array(rotated.prefix(3))
}

// MARK: - FirebaseService Chat Extensions

extension FirebaseService {

    func loadMessagesOnce(conversationId: String) async -> [Message] {
        let db = Firestore.firestore()
        guard let snapshot = try? await db
            .collection("conversations")
            .document(conversationId)
            .collection("messages")
            .order(by: "sentAt", descending: false)
            .limit(to: 100)
            .getDocuments()
        else { return [] }

        return snapshot.documents.compactMap { doc -> Message? in
            let data = doc.data()
            return Message(
                id: doc.documentID,
                conversationId: conversationId,
                senderId: data["senderId"] as? String ?? "",
                receiverId: data["receiverId"] as? String ?? "",
                content: data["content"] as? String ?? "",
                type: MessageType(rawValue: data["type"] as? String ?? "text") ?? .text,
                sentAt: (data["sentAt"] as? Timestamp)?.dateValue() ?? Date(),
                isRead: data["isRead"] as? Bool ?? false,
                isDelivered: data["isDelivered"] as? Bool ?? false,
                mediaURL: data["mediaURL"] as? String
            )
        }
    }

    func sendMessageAsync(_ message: Message) async throws {
        let db = Firestore.firestore()
        let data: [String: Any] = [
            "id": message.id,
            "conversationId": message.conversationId,
            "senderId": message.senderId,
            "receiverId": message.receiverId,
            "content": message.content,
            "type": message.type.rawValue,
            "sentAt": Timestamp(date: message.sentAt),
            "isRead": message.isRead,
            "isDelivered": false,
            "mediaURL": message.mediaURL as Any
        ]

        try await db.collection("conversations")
            .document(message.conversationId)
            .collection("messages")
            .document(message.id)
            .setData(data)

        // Atualizar última mensagem da conversa
        try await db.collection("conversations")
            .document(message.conversationId)
            .updateData([
                "lastMessage": message.content,
                "lastMessageAt": Timestamp(date: message.sentAt),
                "lastActivity": Timestamp(date: Date()),
                "updatedAt": Timestamp(date: Date())
            ])
    }

    func setTypingStatus(_ isTyping: Bool, conversationId: String, userId: String) async {
        let db = Firestore.firestore()
        try? await db.collection("conversations")
            .document(conversationId)
            .updateData(["typing.\(userId)": isTyping])
    }

    func markConversationAsRead(conversationId: String, userId: String) async {
        let db = Firestore.firestore()
        // Obter conversa para saber qual campo atualizar
        guard let doc = try? await db.collection("conversations").document(conversationId).getDocument(),
              let data = doc.data()
        else { return }

        let participant1Id = data["participant1Id"] as? String ?? ""
        let field = participant1Id == userId ? "unreadCountUser1" : "unreadCountUser2"

        try? await db.collection("conversations")
            .document(conversationId)
            .updateData([field: 0])
    }

    func getOrCreateConversation(matchId: String, userId: String, otherUserId: String) async throws -> Conversation {
        let db = Firestore.firestore()

        // Procurar conversa existente
        let snapshot = try await db.collection("conversations")
            .whereField("matchId", isEqualTo: matchId)
            .limit(to: 1)
            .getDocuments()

        if let existingDoc = snapshot.documents.first {
            let data = existingDoc.data()
            return Conversation(
                id: existingDoc.documentID,
                matchId: matchId,
                participant1Id: data["participant1Id"] as? String ?? userId,
                participant2Id: data["participant2Id"] as? String ?? otherUserId
            )
        }

        // Criar nova conversa
        let conversationId = UUID().uuidString
        let conversationData: [String: Any] = [
            "id": conversationId,
            "matchId": matchId,
            "participant1Id": min(userId, otherUserId),
            "participant2Id": max(userId, otherUserId),
            "isActive": true,
            "isArchived": false,
            "unreadCountUser1": 0,
            "unreadCountUser2": 0,
            "isTypingUser1": false,
            "isTypingUser2": false,
            "createdAt": Timestamp(date: Date()),
            "updatedAt": Timestamp(date: Date()),
            "lastActivity": Timestamp(date: Date())
        ]

        try await db.collection("conversations").document(conversationId).setData(conversationData)

        return Conversation(
            id: conversationId,
            matchId: matchId,
            participant1Id: min(userId, otherUserId),
            participant2Id: max(userId, otherUserId)
        )
    }

    func loadConversations(userId: String) async throws -> [Conversation] {
        let db = Firestore.firestore()

        let snapshot = try await db.collection("conversations")
            .whereFilter(Filter.orFilter([
                Filter.whereField("participant1Id", isEqualTo: userId),
                Filter.whereField("participant2Id", isEqualTo: userId)
            ]))
            .whereField("isActive", isEqualTo: true)
            .order(by: "lastActivity", descending: true)
            .getDocuments()

        return try snapshot.documents.compactMap { doc in
            let data = doc.data()
            return Conversation(
                id: doc.documentID,
                matchId: data["matchId"] as? String ?? "",
                participant1Id: data["participant1Id"] as? String ?? "",
                participant2Id: data["participant2Id"] as? String ?? "",
                lastActivity: (data["lastActivity"] as? Timestamp)?.dateValue() ?? Date(),
                unreadCountUser1: data["unreadCountUser1"] as? Int ?? 0,
                unreadCountUser2: data["unreadCountUser2"] as? Int ?? 0,
                isActive: data["isActive"] as? Bool ?? true,
                isArchived: data["isArchived"] as? Bool ?? false,
                isTypingUser1: data["isTypingUser1"] as? Bool ?? false,
                isTypingUser2: data["isTypingUser2"] as? Bool ?? false
            )
        }
    }

    func unmatch(matchId: String, userId: String) async throws {
        let db = Firestore.firestore()
        try await db.collection("matches").document(matchId).updateData([
            "isActive": false,
            "blockedBy": userId,
            "blockedAt": Timestamp(date: Date()),
            "updatedAt": Timestamp(date: Date())
        ])
    }

    func uploadMessageMedia(localURL: URL, conversationId: String) async throws -> String {
        let data = try Data(contentsOf: localURL)
        let fileName = "\(UUID().uuidString).jpg"
        return try await uploadPhoto(data, userId: "conversations/\(conversationId)/\(fileName)")
    }
}

// MARK: - Firestore import

import FirebaseFirestore
