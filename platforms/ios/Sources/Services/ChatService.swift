// ChatService.swift — FypMatch iOS
// Serviço de chat em tempo real com Firestore

import Foundation
import FirebaseFirestore
import Combine

@MainActor
class ChatService: ObservableObject {
    static let shared = ChatService()
    private let db = Firestore.firestore()
    private var listeners: [String: ListenerRegistration] = [:]

    // MARK: - Send Message

    func sendMessage(_ message: Message) {
        guard let data = try? Firestore.Encoder().encode(message) else { return }
        db.collection("conversations")
            .document(message.conversationId)
            .collection("messages")
            .document(message.id)
            .setData(data)

        // Update conversation's last message
        db.collection("conversations")
            .document(message.conversationId)
            .updateData([
                "lastMessage": message.content,
                "lastMessageTimestamp": Timestamp(date: message.timestamp),
                "lastMessageSenderId": message.senderId,
            ])
    }

    // MARK: - Listen to Messages

    func listenToMessages(conversationId: String, completion: @escaping ([Message]) -> Void) {
        let listener = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .order(by: "timestamp", descending: false)
            .addSnapshotListener { snapshot, _ in
                guard let docs = snapshot?.documents else { return }
                let messages = docs.compactMap { try? $0.data(as: Message.self) }
                completion(messages)
            }
        listeners[conversationId] = listener
    }

    func stopListening(conversationId: String) {
        listeners[conversationId]?.remove()
        listeners.removeValue(forKey: conversationId)
    }

    // MARK: - Mark as Read

    func markMessagesAsRead(conversationId: String, userId: String) {
        db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .whereField("senderId", isNotEqualTo: userId)
            .whereField("isRead", isEqualTo: false)
            .getDocuments { snapshot, _ in
                snapshot?.documents.forEach { $0.reference.updateData(["isRead": true]) }
            }
    }

    // MARK: - Typing Indicator

    func setTyping(_ isTyping: Bool, conversationId: String, userId: String) {
        db.collection("conversations")
            .document(conversationId)
            .updateData(["typing.\(userId)": isTyping])
    }

    // MARK: - Load Conversations

    func loadConversations(userId: String, completion: @escaping ([Conversation]) -> Void) {
        db.collection("conversations")
            .whereField("participants", arrayContains: userId)
            .order(by: "lastMessageTimestamp", descending: true)
            .addSnapshotListener { snapshot, _ in
                guard let docs = snapshot?.documents else { return }
                let conversations = docs.compactMap { doc -> Conversation? in
                    let data = doc.data()
                    let participants = data["participants"] as? [String] ?? []
                    let otherId = participants.first { $0 != userId } ?? ""
                    return Conversation(
                        id: doc.documentID,
                        otherUserId: otherId,
                        otherUserName: data["otherUserName"] as? String ?? "",
                        otherUserPhotoURL: data["otherUserPhotoURL"] as? String ?? "",
                        lastMessage: data["lastMessage"] as? String ?? "",
                        lastMessageTimestamp: (data["lastMessageTimestamp"] as? Timestamp)?.dateValue() ?? Date(),
                        unreadCount: data["unreadCount"] as? Int ?? 0,
                        isOtherUserOnline: data["isOtherUserOnline"] as? Bool ?? false
                    )
                }
                completion(conversations)
            }
    }
}

// MARK: - FirebaseService extensions for chat

extension FirebaseService {
    func listenToMessages(conversationId: String, completion: @escaping ([Message]) -> Void) {
        ChatService.shared.listenToMessages(conversationId: conversationId, completion: completion)
    }
    func sendMessage(_ message: Message) {
        ChatService.shared.sendMessage(message)
    }
    func stopListening(conversationId: String) {
        ChatService.shared.stopListening(conversationId: conversationId)
    }
}
