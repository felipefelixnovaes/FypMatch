//
//  ChatFeature.swift
//  FypMatch iOS
//
//  Feature de chat em tempo real
//  Gerencia conversas e mensagens entre matches
//

import Foundation
import ComposableArchitecture
import Combine

/// Feature responsável pelo sistema de chat
@Reducer
struct ChatFeature {
    
    // MARK: - State
    
    @ObservableState
    struct State: Equatable {
        // MARK: - Conversation Data
        var conversation: Conversation?
        var messages: [Message] = []
        var isLoadingMessages = false
        var hasMoreMessages = true
        
        // MARK: - Current User
        var currentUserId: String?
        var otherUser: User?
        
        // MARK: - Message Composition
        var messageText: String = ""
        var isSendingMessage = false
        var sendError: String?
        
        // MARK: - Real-time Updates
        var isTyping = false
        var otherUserIsTyping = false
        var typingTimer: Date?
        
        // MARK: - Media
        var isRecordingAudio = false
        var recordingDuration: TimeInterval = 0
        var selectedImage: Data?
        var isUploadingMedia = false
        var uploadProgress: Double = 0.0
        
        // MARK: - UI State
        var showingImagePicker = false
        var showingMediaOptions = false
        var error: String?
        var isRefreshing = false
        
        // MARK: - Message Actions
        var selectedMessage: Message?
        var showingMessageActions = false
        var replyingToMessage: Message?
        
        // MARK: - Computed Properties
        var canSendMessage: Bool {
            !messageText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && !isSendingMessage
        }
        
        var messageCount: Int {
            messages.count
        }
        
        var unreadCount: Int {
            guard let currentUserId = currentUserId else { return 0 }
            return messages.filter { !$0.isRead && $0.receiverId == currentUserId }.count
        }
        
        var isEmptyConversation: Bool {
            messages.isEmpty
        }
        
        var otherUserName: String {
            otherUser?.displayName ?? "Usuário"
        }
        
        var conversationId: String? {
            conversation?.id
        }
    }
    
    // MARK: - Action
    
    enum Action: BindableAction, Equatable {
        // MARK: - Binding
        case binding(BindingAction<State>)
        
        // MARK: - Lifecycle
        case onAppear
        case onDisappear
        case loadConversation(String)
        case loadMessages
        case loadMoreMessages
        case refresh
        
        // MARK: - Messaging
        case sendMessage
        case sendTextMessage(String)
        case sendImageMessage(Data)
        case sendAudioMessage(Data)
        case deleteMessage(Message)
        case editMessage(Message, String)
        
        // MARK: - Reply
        case startReply(Message)
        case cancelReply
        
        // MARK: - Typing
        case startTyping
        case stopTyping
        case otherUserTypingChanged(Bool)
        
        // MARK: - Audio Recording
        case startAudioRecording
        case stopAudioRecording
        case cancelAudioRecording
        case updateRecordingDuration(TimeInterval)
        
        // MARK: - Media
        case selectImage
        case imageSelected(Data)
        case uploadMedia(Data, MediaType)
        case mediaUploadProgress(Double)
        
        // MARK: - Message Actions
        case showMessageActions(Message)
        case hideMessageActions
        case reactToMessage(Message, String)
        
        // MARK: - Read Receipts
        case markMessagesAsRead
        case messageReadByOther(String)
        
        // MARK: - Real-time Updates
        case messagesUpdated([Message])
        case newMessageReceived(Message)
        case conversationUpdated(Conversation)
        
        // MARK: - Response Actions
        case conversationLoaded(Result<Conversation, Error>)
        case messagesLoaded(Result<[Message], Error>)
        case messageSent(Result<Message, Error>)
        case messageDeleted(Result<Void, Error>)
        case mediaUploaded(Result<String, Error>)
        case dismissError
    }
    
    // MARK: - Dependencies
    
    @Dependency(\.firebaseService) var firebaseService
    @Dependency(\.continuousClock) var clock
    @Dependency(\.dismiss) var dismiss
    
    // MARK: - Body
    
    var body: some ReducerOf<Self> {
        BindingReducer()
        
        Reduce { state, action in
            switch action {
                
            // MARK: - Lifecycle
                
            case .onAppear:
                return .merge(
                    .run { send in
                        await send(.loadMessages)
                        await send(.markMessagesAsRead)
                    },
                    .run { send in
                        // Subscribe to real-time message updates
                        // In a real app, this would use Firebase real-time listeners
                        for await _ in self.clock.timer(interval: .seconds(2)) {
                            await send(.loadMessages)
                        }
                    }
                )
                
            case .onDisappear:
                return .run { send in
                    await send(.stopTyping)
                }
                
            case let .loadConversation(conversationId):
                state.isLoadingMessages = true
                state.error = nil
                
                return .run { send in
                    do {
                        let conversation = try await firebaseService.getConversation(conversationId)
                        await send(.conversationLoaded(.success(conversation)))
                    } catch {
                        await send(.conversationLoaded(.failure(error)))
                    }
                }
                
            case .loadMessages:
                guard let conversationId = state.conversation?.id else { return .none }
                
                return .run { send in
                    do {
                        let messages = try await firebaseService.getMessages(conversationId: conversationId)
                        await send(.messagesLoaded(.success(messages)))
                    } catch {
                        await send(.messagesLoaded(.failure(error)))
                    }
                }
                
            case .loadMoreMessages:
                guard state.hasMoreMessages else { return .none }
                guard let conversationId = state.conversation?.id else { return .none }
                guard let oldestMessage = state.messages.first else { return .none }
                
                return .run { send in
                    do {
                        let messages = try await firebaseService.getMessagesBefore(
                            conversationId: conversationId,
                            before: oldestMessage.sentAt
                        )
                        
                        if !messages.isEmpty {
                            await send(.messagesLoaded(.success(messages)))
                        }
                    } catch {
                        await send(.messagesLoaded(.failure(error)))
                    }
                }
                
            case .refresh:
                state.isRefreshing = true
                return .run { send in
                    await send(.loadMessages)
                }
                
            // MARK: - Messaging
                
            case .sendMessage:
                guard state.canSendMessage else { return .none }
                return .run { [text = state.messageText] send in
                    await send(.sendTextMessage(text))
                }
                
            case let .sendTextMessage(text):
                guard let conversationId = state.conversation?.id else { return .none }
                guard let currentUserId = state.currentUserId else { return .none }
                guard let receiverId = state.conversation?.getOtherUserId(currentUserId: currentUserId) else {
                    return .none
                }
                
                state.isSendingMessage = true
                state.sendError = nil
                
                let message = Message(
                    conversationId: conversationId,
                    senderId: currentUserId,
                    receiverId: receiverId,
                    content: text,
                    type: .text,
                    replyToMessageId: state.replyingToMessage?.id
                )
                
                return .run { send in
                    do {
                        try await firebaseService.sendMessage(message)
                        await send(.messageSent(.success(message)))
                    } catch {
                        await send(.messageSent(.failure(error)))
                    }
                }
                
            case let .sendImageMessage(imageData):
                guard let conversationId = state.conversation?.id else { return .none }
                guard let currentUserId = state.currentUserId else { return .none }
                guard let receiverId = state.conversation?.getOtherUserId(currentUserId: currentUserId) else {
                    return .none
                }
                
                state.isUploadingMedia = true
                state.uploadProgress = 0.0
                
                return .run { send in
                    do {
                        // Upload image first
                        let imageURL = try await firebaseService.uploadChatImage(imageData, conversationId: conversationId)
                        
                        // Create and send message
                        let message = Message(
                            conversationId: conversationId,
                            senderId: currentUserId,
                            receiverId: receiverId,
                            content: "",
                            type: .image,
                            mediaURL: imageURL,
                            mediaType: .image
                        )
                        
                        try await firebaseService.sendMessage(message)
                        await send(.messageSent(.success(message)))
                    } catch {
                        await send(.messageSent(.failure(error)))
                    }
                }
                
            case let .sendAudioMessage(audioData):
                guard let conversationId = state.conversation?.id else { return .none }
                guard let currentUserId = state.currentUserId else { return .none }
                guard let receiverId = state.conversation?.getOtherUserId(currentUserId: currentUserId) else {
                    return .none
                }
                
                state.isUploadingMedia = true
                
                return .run { send in
                    do {
                        let audioURL = try await firebaseService.uploadChatAudio(audioData, conversationId: conversationId)
                        
                        let message = Message(
                            conversationId: conversationId,
                            senderId: currentUserId,
                            receiverId: receiverId,
                            content: "",
                            type: .audio,
                            mediaURL: audioURL,
                            mediaType: .audio,
                            mediaDuration: 0 // Would be calculated from audio data
                        )
                        
                        try await firebaseService.sendMessage(message)
                        await send(.messageSent(.success(message)))
                    } catch {
                        await send(.messageSent(.failure(error)))
                    }
                }
                
            case let .deleteMessage(message):
                return .run { send in
                    do {
                        try await firebaseService.deleteMessage(message.id)
                        await send(.messageDeleted(.success(())))
                    } catch {
                        await send(.messageDeleted(.failure(error)))
                    }
                }
                
            case let .editMessage(message, newContent):
                return .run { send in
                    do {
                        try await firebaseService.editMessage(message.id, content: newContent)
                        await send(.loadMessages)
                    } catch {
                        await send(.messageSent(.failure(error)))
                    }
                }
                
            // MARK: - Reply
                
            case let .startReply(message):
                state.replyingToMessage = message
                return .none
                
            case .cancelReply:
                state.replyingToMessage = nil
                return .none
                
            // MARK: - Typing
                
            case .startTyping:
                guard let conversationId = state.conversation?.id else { return .none }
                guard let currentUserId = state.currentUserId else { return .none }
                
                state.isTyping = true
                state.typingTimer = Date()
                
                return .run { send in
                    try await firebaseService.setTypingStatus(
                        conversationId: conversationId,
                        userId: currentUserId,
                        isTyping: true
                    )
                }
                
            case .stopTyping:
                guard let conversationId = state.conversation?.id else { return .none }
                guard let currentUserId = state.currentUserId else { return .none }
                guard state.isTyping else { return .none }
                
                state.isTyping = false
                state.typingTimer = nil
                
                return .run { send in
                    try await firebaseService.setTypingStatus(
                        conversationId: conversationId,
                        userId: currentUserId,
                        isTyping: false
                    )
                }
                
            case let .otherUserTypingChanged(isTyping):
                state.otherUserIsTyping = isTyping
                return .none
                
            // MARK: - Audio Recording
                
            case .startAudioRecording:
                state.isRecordingAudio = true
                state.recordingDuration = 0
                
                return .run { send in
                    // Simulate recording timer
                    for await _ in self.clock.timer(interval: .seconds(1)) {
                        await send(.updateRecordingDuration(1))
                    }
                }
                
            case .stopAudioRecording:
                state.isRecordingAudio = false
                // In real app, would save and send audio
                return .none
                
            case .cancelAudioRecording:
                state.isRecordingAudio = false
                state.recordingDuration = 0
                return .none
                
            case let .updateRecordingDuration(duration):
                state.recordingDuration += duration
                return .none
                
            // MARK: - Media
                
            case .selectImage:
                state.showingImagePicker = true
                return .none
                
            case let .imageSelected(imageData):
                state.selectedImage = imageData
                state.showingImagePicker = false
                return .run { send in
                    await send(.sendImageMessage(imageData))
                }
                
            case let .uploadMedia(data, type):
                state.isUploadingMedia = true
                state.uploadProgress = 0.0
                
                return .run { send in
                    // Simulate upload progress
                    for progress in stride(from: 0.0, through: 1.0, by: 0.1) {
                        await send(.mediaUploadProgress(progress))
                        try await Task.sleep(nanoseconds: 100_000_000)
                    }
                }
                
            case let .mediaUploadProgress(progress):
                state.uploadProgress = progress
                return .none
                
            // MARK: - Message Actions
                
            case let .showMessageActions(message):
                state.selectedMessage = message
                state.showingMessageActions = true
                return .none
                
            case .hideMessageActions:
                state.selectedMessage = nil
                state.showingMessageActions = false
                return .none
                
            case let .reactToMessage(message, emoji):
                return .run { send in
                    try await firebaseService.addReaction(messageId: message.id, emoji: emoji)
                    await send(.loadMessages)
                }
                
            // MARK: - Read Receipts
                
            case .markMessagesAsRead:
                guard let conversationId = state.conversation?.id else { return .none }
                guard let currentUserId = state.currentUserId else { return .none }
                
                let unreadMessages = state.messages.filter { !$0.isRead && $0.receiverId == currentUserId }
                
                guard !unreadMessages.isEmpty else { return .none }
                
                return .run { send in
                    for message in unreadMessages {
                        try await firebaseService.markMessageAsRead(message.id)
                    }
                    await send(.loadMessages)
                }
                
            case let .messageReadByOther(messageId):
                if let index = state.messages.firstIndex(where: { $0.id == messageId }) {
                    var message = state.messages[index]
                    // Would update message read status
                }
                return .none
                
            // MARK: - Real-time Updates
                
            case let .messagesUpdated(messages):
                state.messages = messages.sorted { $0.sentAt < $1.sentAt }
                return .none
                
            case let .newMessageReceived(message):
                if !state.messages.contains(where: { $0.id == message.id }) {
                    state.messages.append(message)
                    state.messages.sort { $0.sentAt < $1.sentAt }
                }
                return .run { send in
                    await send(.markMessagesAsRead)
                }
                
            case let .conversationUpdated(conversation):
                state.conversation = conversation
                return .none
                
            // MARK: - Response Handling
                
            case let .conversationLoaded(.success(conversation)):
                state.isLoadingMessages = false
                state.conversation = conversation
                state.otherUser = conversation.getOtherUser(currentUserId: state.currentUserId ?? "")
                state.error = nil
                return .run { send in
                    await send(.loadMessages)
                }
                
            case let .conversationLoaded(.failure(error)):
                state.isLoadingMessages = false
                state.error = error.localizedDescription
                return .none
                
            case let .messagesLoaded(.success(messages)):
                state.isLoadingMessages = false
                state.isRefreshing = false
                state.messages = messages.sorted { $0.sentAt < $1.sentAt }
                state.hasMoreMessages = messages.count >= 50 // Default page size
                state.error = nil
                return .none
                
            case let .messagesLoaded(.failure(error)):
                state.isLoadingMessages = false
                state.isRefreshing = false
                state.error = error.localizedDescription
                return .none
                
            case let .messageSent(.success(message)):
                state.isSendingMessage = false
                state.isUploadingMedia = false
                state.messageText = ""
                state.replyingToMessage = nil
                state.sendError = nil
                
                if !state.messages.contains(where: { $0.id == message.id }) {
                    state.messages.append(message)
                }
                
                return .run { send in
                    await send(.stopTyping)
                }
                
            case let .messageSent(.failure(error)):
                state.isSendingMessage = false
                state.isUploadingMedia = false
                state.sendError = error.localizedDescription
                return .none
                
            case .messageDeleted(.success):
                return .run { send in
                    await send(.loadMessages)
                }
                
            case let .messageDeleted(.failure(error)):
                state.error = error.localizedDescription
                return .none
                
            case let .mediaUploaded(.success(url)):
                state.isUploadingMedia = false
                state.uploadProgress = 1.0
                return .none
                
            case let .mediaUploaded(.failure(error)):
                state.isUploadingMedia = false
                state.error = error.localizedDescription
                return .none
                
            case .dismissError:
                state.error = nil
                state.sendError = nil
                return .none
                
            // MARK: - Binding
                
            case .binding(\.$messageText):
                // Start typing indicator when user types
                if !state.messageText.isEmpty && !state.isTyping {
                    return .run { send in
                        await send(.startTyping)
                    }
                }
                return .none
                
            case .binding:
                return .none
            }
        }
    }
}

// MARK: - Firebase Service Extension for Chat

extension FirebaseService {
    /// Get conversation by ID
    func getConversation(_ conversationId: String) async throws -> Conversation {
        let document = try await db.collection("conversations").document(conversationId).getDocument()
        
        guard document.exists, let data = document.data() else {
            throw ChatError.conversationNotFound
        }
        
        return try Firestore.Decoder().decode(Conversation.self, from: data)
    }
    
    /// Get messages for a conversation
    func getMessages(conversationId: String, limit: Int = 50) async throws -> [Message] {
        let snapshot = try await db.collection("messages")
            .whereField("conversationId", isEqualTo: conversationId)
            .order(by: "sentAt", descending: true)
            .limit(to: limit)
            .getDocuments()
        
        return try snapshot.documents.compactMap { doc in
            try doc.data(as: Message.self)
        }
    }
    
    /// Get messages before a certain date
    func getMessagesBefore(conversationId: String, before: Date, limit: Int = 50) async throws -> [Message] {
        let snapshot = try await db.collection("messages")
            .whereField("conversationId", isEqualTo: conversationId)
            .whereField("sentAt", isLessThan: before)
            .order(by: "sentAt", descending: true)
            .limit(to: limit)
            .getDocuments()
        
        return try snapshot.documents.compactMap { doc in
            try doc.data(as: Message.self)
        }
    }
    
    /// Send a message
    func sendMessage(_ message: Message) async throws {
        let encoder = Firestore.Encoder()
        let data = try encoder.encode(message)
        try await db.collection("messages").document(message.id).setData(data)
        
        // Update conversation last message
        try await db.collection("conversations").document(message.conversationId).updateData([
            "lastMessageAt": message.sentAt,
            "lastActivity": Date(),
            "updatedAt": Date()
        ])
    }
    
    /// Delete a message
    func deleteMessage(_ messageId: String) async throws {
        try await db.collection("messages").document(messageId).updateData([
            "isDeleted": true,
            "deletedAt": Date(),
            "content": ""
        ])
    }
    
    /// Edit a message
    func editMessage(_ messageId: String, content: String) async throws {
        try await db.collection("messages").document(messageId).updateData([
            "content": content,
            "isEdited": true,
            "editedAt": Date()
        ])
    }
    
    /// Mark message as read
    func markMessageAsRead(_ messageId: String) async throws {
        try await db.collection("messages").document(messageId).updateData([
            "isRead": true,
            "readAt": Date()
        ])
    }
    
    /// Set typing status
    func setTypingStatus(conversationId: String, userId: String, isTyping: Bool) async throws {
        let field = userId == "user1" ? "isTypingUser1" : "isTypingUser2"
        try await db.collection("conversations").document(conversationId).updateData([
            field: isTyping,
            "typingStartedAt": isTyping ? Date() : nil
        ])
    }
    
    /// Add reaction to message
    func addReaction(messageId: String, emoji: String) async throws {
        guard let currentUser = currentUser else { return }
        
        let reaction = MessageReaction(
            messageId: messageId,
            userId: currentUser.id,
            emoji: emoji
        )
        
        // In a real app, would update reactions array in message document
        // For now, just a placeholder
    }
    
    /// Upload chat image
    func uploadChatImage(_ imageData: Data, conversationId: String) async throws -> String {
        let imageId = UUID().uuidString
        let path = "chats/\(conversationId)/images/\(imageId).jpg"
        let storageRef = storage.reference().child(path)
        
        _ = try await storageRef.putDataAsync(imageData)
        let downloadURL = try await storageRef.downloadURL()
        
        return downloadURL.absoluteString
    }
    
    /// Upload chat audio
    func uploadChatAudio(_ audioData: Data, conversationId: String) async throws -> String {
        let audioId = UUID().uuidString
        let path = "chats/\(conversationId)/audio/\(audioId).m4a"
        let storageRef = storage.reference().child(path)
        
        _ = try await storageRef.putDataAsync(audioData)
        let downloadURL = try await storageRef.downloadURL()
        
        return downloadURL.absoluteString
    }
}

// MARK: - Errors

enum ChatError: Error, LocalizedError {
    case conversationNotFound
    case messageSendFailed
    case mediaUploadFailed
    
    var errorDescription: String? {
        switch self {
        case .conversationNotFound:
            return "Conversa não encontrada"
        case .messageSendFailed:
            return "Falha ao enviar mensagem"
        case .mediaUploadFailed:
            return "Falha ao fazer upload da mídia"
        }
    }
}
