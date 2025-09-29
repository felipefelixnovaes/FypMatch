//
//  Message.swift
//  FypMatch iOS
//
//  Modelos para sistema de chat em tempo real
//  Compatível com Firestore e sistema Android
//

import Foundation
import FirebaseFirestore

// MARK: - Message

/// Representa uma mensagem no chat
struct Message: Codable, Identifiable, Equatable {
    // MARK: - Basic Properties
    let id: String
    let conversationId: String
    let senderId: String
    let receiverId: String
    let content: String
    let type: MessageType
    
    // MARK: - Timestamps
    let sentAt: Date
    let deliveredAt: Date?
    let readAt: Date?
    
    // MARK: - Message Status
    let isRead: Bool
    let isDelivered: Bool
    let isEdited: Bool
    let editedAt: Date?
    let isDeleted: Bool
    let deletedAt: Date?
    
    // MARK: - Rich Content
    let mediaURL: String?
    let thumbnailURL: String?
    let mediaType: MediaType?
    let mediaDuration: Double? // Para áudios/vídeos
    let mediaSize: Int? // Em bytes
    
    // MARK: - Metadata
    let replyToMessageId: String?
    let mentionedUserIds: [String]
    let reactions: [MessageReaction]
    let localId: String? // Para sincronização offline
    
    // MARK: - System
    let createdAt: Date
    let updatedAt: Date
    
    // MARK: - Initializer
    init(
        id: String = UUID().uuidString,
        conversationId: String,
        senderId: String,
        receiverId: String,
        content: String,
        type: MessageType = .text,
        sentAt: Date = Date(),
        deliveredAt: Date? = nil,
        readAt: Date? = nil,
        isRead: Bool = false,
        isDelivered: Bool = false,
        isEdited: Bool = false,
        editedAt: Date? = nil,
        isDeleted: Bool = false,
        deletedAt: Date? = nil,
        mediaURL: String? = nil,
        thumbnailURL: String? = nil,
        mediaType: MediaType? = nil,
        mediaDuration: Double? = nil,
        mediaSize: Int? = nil,
        replyToMessageId: String? = nil,
        mentionedUserIds: [String] = [],
        reactions: [MessageReaction] = [],
        localId: String? = nil,
        createdAt: Date = Date(),
        updatedAt: Date = Date()
    ) {
        self.id = id
        self.conversationId = conversationId
        self.senderId = senderId
        self.receiverId = receiverId
        self.content = content
        self.type = type
        self.sentAt = sentAt
        self.deliveredAt = deliveredAt
        self.readAt = readAt
        self.isRead = isRead
        self.isDelivered = isDelivered
        self.isEdited = isEdited
        self.editedAt = editedAt
        self.isDeleted = isDeleted
        self.deletedAt = deletedAt
        self.mediaURL = mediaURL
        self.thumbnailURL = thumbnailURL
        self.mediaType = mediaType
        self.mediaDuration = mediaDuration
        self.mediaSize = mediaSize
        self.replyToMessageId = replyToMessageId
        self.mentionedUserIds = mentionedUserIds
        self.reactions = reactions
        self.localId = localId
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
}

// MARK: - Message Extensions

extension Message {
    /// Retorna se a mensagem foi enviada pelo usuário atual
    func isSentByUser(_ userId: String) -> Bool {
        return senderId == userId
    }
    
    /// Retorna o texto da mensagem ou placeholder para mídia
    var displayContent: String {
        if isDeleted {
            return "Mensagem excluída"
        }
        
        switch type {
        case .text:
            return content
        case .image:
            return "📷 Imagem"
        case .video:
            return "🎥 Vídeo"
        case .audio:
            return "🎵 Áudio"
        case .location:
            return "📍 Localização"
        case .gif:
            return "🎭 GIF"
        case .sticker:
            return "😄 Sticker"
        case .system:
            return content
        }
    }
    
    /// Retorna a hora formatada da mensagem
    var timeString: String {
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        return formatter.string(from: sentAt)
    }
    
    /// Retorna a data formatada da mensagem
    var dateString: String {
        let formatter = DateFormatter()
        if Calendar.current.isDateInToday(sentAt) {
            return "Hoje"
        } else if Calendar.current.isDateInYesterday(sentAt) {
            return "Ontem"
        } else {
            formatter.dateFormat = "dd/MM/yyyy"
            return formatter.string(from: sentAt)
        }
    }
    
    /// Retorna se a mensagem é do mesmo dia que outra
    func isSameDay(as otherMessage: Message) -> Bool {
        return Calendar.current.isDate(sentAt, inSameDayAs: otherMessage.sentAt)
    }
    
    /// Retorna o status da mensagem para display
    var statusIcon: String {
        if isDeleted { return "" }
        if isRead { return "checkmark.circle.fill" }
        if isDelivered { return "checkmark.circle" }
        return "clock"
    }
    
    /// Retorna a duração formatada para áudios/vídeos
    var formattedDuration: String? {
        guard let duration = mediaDuration else { return nil }
        let minutes = Int(duration) / 60
        let seconds = Int(duration) % 60
        return String(format: "%d:%02d", minutes, seconds)
    }
}

// MARK: - Conversation

/// Representa uma conversa entre dois usuários
struct Conversation: Codable, Identifiable, Equatable {
    // MARK: - Basic Properties
    let id: String
    let matchId: String
    let participant1Id: String
    let participant2Id: String
    
    // MARK: - Participants Info
    let participant1: User?
    let participant2: User?
    
    // MARK: - Last Message
    let lastMessage: Message?
    let lastMessageAt: Date?
    let lastActivity: Date
    
    // MARK: - Read Status
    let unreadCountUser1: Int
    let unreadCountUser2: Int
    let lastReadByUser1: Date?
    let lastReadByUser2: Date?
    
    // MARK: - Conversation State
    let isActive: Bool
    let isArchived: Bool
    let isMuted: Bool
    let mutedUntil: Date?
    
    // MARK: - Settings
    let isTypingUser1: Bool
    let isTypingUser2: Bool
    let typingStartedAt: Date?
    
    // MARK: - System
    let createdAt: Date
    let updatedAt: Date
    
    // MARK: - Initializer
    init(
        id: String = UUID().uuidString,
        matchId: String,
        participant1Id: String,
        participant2Id: String,
        participant1: User? = nil,
        participant2: User? = nil,
        lastMessage: Message? = nil,
        lastMessageAt: Date? = nil,
        lastActivity: Date = Date(),
        unreadCountUser1: Int = 0,
        unreadCountUser2: Int = 0,
        lastReadByUser1: Date? = nil,
        lastReadByUser2: Date? = nil,
        isActive: Bool = true,
        isArchived: Bool = false,
        isMuted: Bool = false,
        mutedUntil: Date? = nil,
        isTypingUser1: Bool = false,
        isTypingUser2: Bool = false,
        typingStartedAt: Date? = nil,
        createdAt: Date = Date(),
        updatedAt: Date = Date()
    ) {
        self.id = id
        self.matchId = matchId
        self.participant1Id = participant1Id
        self.participant2Id = participant2Id
        self.participant1 = participant1
        self.participant2 = participant2
        self.lastMessage = lastMessage
        self.lastMessageAt = lastMessageAt
        self.lastActivity = lastActivity
        self.unreadCountUser1 = unreadCountUser1
        self.unreadCountUser2 = unreadCountUser2
        self.lastReadByUser1 = lastReadByUser1
        self.lastReadByUser2 = lastReadByUser2
        self.isActive = isActive
        self.isArchived = isArchived
        self.isMuted = isMuted
        self.mutedUntil = mutedUntil
        self.isTypingUser1 = isTypingUser1
        self.isTypingUser2 = isTypingUser2
        self.typingStartedAt = typingStartedAt
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
}

// MARK: - Conversation Extensions

extension Conversation {
    /// Retorna o outro usuário da conversa
    func getOtherUser(currentUserId: String) -> User? {
        if participant1Id == currentUserId {
            return participant2
        } else if participant2Id == currentUserId {
            return participant1
        }
        return nil
    }
    
    /// Retorna o ID do outro usuário
    func getOtherUserId(currentUserId: String) -> String? {
        if participant1Id == currentUserId {
            return participant2Id
        } else if participant2Id == currentUserId {
            return participant1Id
        }
        return nil
    }
    
    /// Retorna o número de mensagens não lidas para o usuário
    func getUnreadCount(userId: String) -> Int {
        if participant1Id == userId {
            return unreadCountUser1
        } else if participant2Id == userId {
            return unreadCountUser2
        }
        return 0
    }
    
    /// Retorna se o usuário está digitando
    func isUserTyping(userId: String) -> Bool {
        if participant1Id == userId {
            return isTypingUser1
        } else if participant2Id == userId {
            return isTypingUser2
        }
        return false
    }
    
    /// Retorna se o outro usuário está digitando
    func isOtherUserTyping(currentUserId: String) -> Bool {
        if participant1Id == currentUserId {
            return isTypingUser2
        } else if participant2Id == currentUserId {
            return isTypingUser1
        }
        return false
    }
    
    /// Retorna preview da última mensagem
    var lastMessagePreview: String {
        guard let lastMessage = lastMessage else {
            return "Conversa iniciada"
        }
        return lastMessage.displayContent
    }
    
    /// Retorna tempo da última atividade
    var timeAgoString: String {
        guard let lastMessageAt = lastMessageAt else {
            return ""
        }
        
        let interval = Date().timeIntervalSince(lastMessageAt)
        
        if interval < 60 {
            return "Agora"
        } else if interval < 3600 {
            let minutes = Int(interval / 60)
            return "\(minutes)min"
        } else if interval < 86400 {
            let hours = Int(interval / 3600)
            return "\(hours)h"
        } else {
            let days = Int(interval / 86400)
            return "\(days)d"
        }
    }
}

// MARK: - Supporting Types

/// Tipos de mensagem suportados
enum MessageType: String, Codable, CaseIterable {
    case text = "text"
    case image = "image"
    case video = "video"
    case audio = "audio"
    case location = "location"
    case gif = "gif"
    case sticker = "sticker"
    case system = "system"
    
    var displayName: String {
        switch self {
        case .text: return "Texto"
        case .image: return "Imagem"
        case .video: return "Vídeo"
        case .audio: return "Áudio"
        case .location: return "Localização"
        case .gif: return "GIF"
        case .sticker: return "Sticker"
        case .system: return "Sistema"
        }
    }
}

/// Tipos de mídia suportados
enum MediaType: String, Codable, CaseIterable {
    case image = "image"
    case video = "video"
    case audio = "audio"
    case document = "document"
    
    var fileExtensions: [String] {
        switch self {
        case .image: return ["jpg", "jpeg", "png", "gif", "webp"]
        case .video: return ["mp4", "mov", "avi", "webm"]
        case .audio: return ["mp3", "aac", "wav", "m4a"]
        case .document: return ["pdf", "doc", "docx", "txt"]
        }
    }
}

/// Reação a uma mensagem
struct MessageReaction: Codable, Identifiable, Equatable {
    let id: String
    let messageId: String
    let userId: String
    let emoji: String
    let createdAt: Date
    
    init(
        id: String = UUID().uuidString,
        messageId: String,
        userId: String,
        emoji: String,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.messageId = messageId
        self.userId = userId
        self.emoji = emoji
        self.createdAt = createdAt
    }
}

/// Status de digitação
struct TypingStatus: Codable {
    let userId: String
    let conversationId: String
    let isTyping: Bool
    let startedAt: Date?
    
    init(
        userId: String,
        conversationId: String,
        isTyping: Bool,
        startedAt: Date? = nil
    ) {
        self.userId = userId
        self.conversationId = conversationId
        self.isTyping = isTyping
        self.startedAt = startedAt
    }
}