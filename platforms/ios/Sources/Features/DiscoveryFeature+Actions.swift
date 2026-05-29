// DiscoveryFeature+Actions.swift — FypMatch iOS
// Extensões de FirebaseService para o sistema de Discovery
// NOTA: A implementação principal está em DiscoveryFeature.swift

import Foundation
import FirebaseFirestore
import ComposableArchitecture

// MARK: - FirebaseService Extensions para Discovery

extension FirebaseService {

    /// Registra um like e verifica match mútuo
    func recordLike(fromUser myId: String, toUser targetId: String) async throws -> Match? {
        let db = Firestore.firestore()

        // 1. Salvar o like na coleção de likes
        try await db.collection("likes")
            .document("\(myId)_\(targetId)")
            .setData([
                "fromUserId": myId,
                "toUserId": targetId,
                "likedAt": Timestamp(date: Date()),
                "type": "like"
            ])

        // 2. Verificar se o outro usuário já curtiu de volta (match mútuo)
        let mutualDoc = try await db.collection("likes")
            .document("\(targetId)_\(myId)")
            .getDocument()

        guard mutualDoc.exists else { return nil }

        // 3. Criar match
        let matchId = [myId, targetId].sorted().joined(separator: "_")
        let conversationId = UUID().uuidString

        let matchData: [String: Any] = [
            "id": matchId,
            "user1Id": min(myId, targetId),
            "user2Id": max(myId, targetId),
            "matchedAt": Timestamp(date: Date()),
            "isActive": true,
            "conversationId": conversationId,
            "user1SwipedAt": Timestamp(date: Date()),
            "user2SwipedAt": Timestamp(date: Date()),
            "compatibilityScore": Double.random(in: 70...95),
            "matchType": "regular",
            "hasUser1ViewedMatch": false,
            "hasUser2ViewedMatch": false,
            "isBlocked": false,
            "createdAt": Timestamp(date: Date()),
            "updatedAt": Timestamp(date: Date())
        ]

        try await db.collection("matches").document(matchId).setData(matchData)

        // 4. Criar conversa associada
        try await db.collection("conversations").document(conversationId).setData([
            "id": conversationId,
            "matchId": matchId,
            "participant1Id": min(myId, targetId),
            "participant2Id": max(myId, targetId),
            "isActive": true,
            "isArchived": false,
            "unreadCountUser1": 0,
            "unreadCountUser2": 0,
            "isTypingUser1": false,
            "isTypingUser2": false,
            "createdAt": Timestamp(date: Date()),
            "updatedAt": Timestamp(date: Date())
        ])

        return Match(
            id: matchId,
            user1Id: min(myId, targetId),
            user2Id: max(myId, targetId),
            matchedAt: Date(),
            isActive: true,
            conversationId: conversationId,
            user1SwipedAt: Date(),
            user2SwipedAt: Date(),
            compatibilityScore: 85.0,
            matchType: .regular
        )
    }

    /// Carrega matches do usuário
    func loadMatches(userId: String) async throws -> [Match] {
        let db = Firestore.firestore()

        let snapshot = try await db.collection("matches")
            .whereFilter(Filter.orFilter([
                Filter.whereField("user1Id", isEqualTo: userId),
                Filter.whereField("user2Id", isEqualTo: userId)
            ]))
            .whereField("isActive", isEqualTo: true)
            .order(by: "matchedAt", descending: true)
            .getDocuments()

        return try snapshot.documents.compactMap { doc in
            let data = doc.data()
            return Match(
                id: doc.documentID,
                user1Id: data["user1Id"] as? String ?? "",
                user2Id: data["user2Id"] as? String ?? "",
                matchedAt: (data["matchedAt"] as? Timestamp)?.dateValue() ?? Date(),
                isActive: data["isActive"] as? Bool ?? true,
                conversationId: data["conversationId"] as? String,
                user1SwipedAt: (data["user1SwipedAt"] as? Timestamp)?.dateValue() ?? Date(),
                user2SwipedAt: (data["user2SwipedAt"] as? Timestamp)?.dateValue() ?? Date(),
                compatibilityScore: data["compatibilityScore"] as? Double ?? 0,
                matchType: MatchType(rawValue: data["matchType"] as? String ?? "regular") ?? .regular,
                hasUser1ViewedMatch: data["hasUser1ViewedMatch"] as? Bool ?? false,
                hasUser2ViewedMatch: data["hasUser2ViewedMatch"] as? Bool ?? false,
                isBlocked: data["isBlocked"] as? Bool ?? false
            )
        }
    }

    /// Registra super like
    func recordSuperLike(fromUser myId: String, toUser targetId: String) async throws -> Match? {
        let db = Firestore.firestore()

        try await db.collection("likes")
            .document("\(myId)_\(targetId)")
            .setData([
                "fromUserId": myId,
                "toUserId": targetId,
                "likedAt": Timestamp(date: Date()),
                "type": "super_like"
            ])

        let mutualDoc = try await db.collection("likes")
            .document("\(targetId)_\(myId)")
            .getDocument()

        guard mutualDoc.exists else { return nil }

        return try await recordLike(fromUser: myId, toUser: targetId)
    }

    /// Registra passe (pass/skip)
    func recordPass(fromUser myId: String, toUser targetId: String) async {
        let db = Firestore.firestore()
        try? await db.collection("passes")
            .document("\(myId)_\(targetId)")
            .setData([
                "fromUserId": myId,
                "toUserId": targetId,
                "passedAt": Timestamp(date: Date())
            ])
    }
}
