//
//  MatchesFeature.swift
//  FypMatch iOS
//
//  Feature para gerenciar e visualizar matches
//  Lista de matches e navegação para conversas
//

import Foundation
import ComposableArchitecture

/// Feature responsável por gerenciar matches do usuário
@Reducer
struct MatchesFeature {
    
    // MARK: - State
    
    @ObservableState
    struct State: Equatable {
        // MARK: - Data
        var matches: [Match] = []
        var conversations: [Conversation] = []
        var isLoadingMatches = false
        var isLoadingConversations = false
        var error: String?
        
        // MARK: - Current User
        var currentUserId: String?
        
        // MARK: - Filters
        var showOnlyNewMatches = false
        var showOnlyUnread = false
        
        // MARK: - UI State
        var selectedMatch: Match?
        var selectedConversation: Conversation?
        var isRefreshing = false
        var showingMatchDetail = false
        
        // MARK: - Search
        var searchText: String = ""
        var isSearching: Bool {
            !searchText.isEmpty
        }
        
        // MARK: - Computed Properties
        var matchCount: Int {
            matches.count
        }
        
        var newMatchCount: Int {
            matches.filter { $0.isNewMatch }.count
        }
        
        var unreadConversationCount: Int {
            guard let currentUserId = currentUserId else { return 0 }
            return conversations.filter { $0.getUnreadCount(userId: currentUserId) > 0 }.count
        }
        
        var filteredMatches: [Match] {
            var filtered = matches
            
            if showOnlyNewMatches {
                filtered = filtered.filter { $0.isNewMatch }
            }
            
            if isSearching {
                filtered = filtered.filter { match in
                    let otherUser = match.getOtherUser(currentUserId: currentUserId ?? "")
                    return otherUser?.displayName.localizedCaseInsensitiveContains(searchText) ?? false
                }
            }
            
            return filtered.sorted { $0.matchedAt > $1.matchedAt }
        }
        
        var filteredConversations: [Conversation] {
            guard let currentUserId = currentUserId else { return [] }
            
            var filtered = conversations
            
            if showOnlyUnread {
                filtered = filtered.filter { $0.getUnreadCount(userId: currentUserId) > 0 }
            }
            
            if isSearching {
                filtered = filtered.filter { conversation in
                    let otherUser = conversation.getOtherUser(currentUserId: currentUserId)
                    return otherUser?.displayName.localizedCaseInsensitiveContains(searchText) ?? false
                }
            }
            
            return filtered.sorted { 
                ($0.lastActivity) > ($1.lastActivity)
            }
        }
        
        var isEmpty: Bool {
            matches.isEmpty && conversations.isEmpty
        }
    }
    
    // MARK: - Action
    
    enum Action: BindableAction, Equatable {
        // MARK: - Binding
        case binding(BindingAction<State>)
        
        // MARK: - Lifecycle
        case onAppear
        case refresh
        
        // MARK: - Data Loading
        case loadMatches
        case loadConversations
        case matchesLoaded(Result<[Match], Error>)
        case conversationsLoaded(Result<[Conversation], Error>)
        
        // MARK: - Navigation
        case selectMatch(Match)
        case selectConversation(Conversation)
        case openChat(Conversation)
        case closeMatchDetail
        
        // MARK: - Match Actions
        case unmatchUser(Match)
        case reportUser(Match)
        case blockUser(Match)
        case unmatchResponse(Result<Void, Error>)
        
        // MARK: - Filters
        case toggleNewMatchesOnly
        case toggleUnreadOnly
        case clearSearch
        
        // MARK: - Error Handling
        case dismissError
    }
    
    // MARK: - Dependencies
    
    @Dependency(\.firebaseService) var firebaseService
    @Dependency(\.dismiss) var dismiss
    
    // MARK: - Body
    
    var body: some ReducerOf<Self> {
        BindingReducer()
        
        Reduce { state, action in
            switch action {
                
            // MARK: - Lifecycle
                
            case .onAppear:
                state.currentUserId = firebaseService.currentUser?.id
                return .merge(
                    .send(.loadMatches),
                    .send(.loadConversations)
                )
                
            case .refresh:
                state.isRefreshing = true
                return .merge(
                    .send(.loadMatches),
                    .send(.loadConversations)
                )
                
            // MARK: - Data Loading
                
            case .loadMatches:
                guard let currentUserId = state.currentUserId else { return .none }
                
                state.isLoadingMatches = true
                state.error = nil
                
                return .run { send in
                    do {
                        let matches = try await firebaseService.getUserMatches(userId: currentUserId)
                        await send(.matchesLoaded(.success(matches)))
                    } catch {
                        await send(.matchesLoaded(.failure(error)))
                    }
                }
                
            case .loadConversations:
                guard let currentUserId = state.currentUserId else { return .none }
                
                state.isLoadingConversations = true
                state.error = nil
                
                return .run { send in
                    do {
                        let conversations = try await firebaseService.getUserConversations(userId: currentUserId)
                        await send(.conversationsLoaded(.success(conversations)))
                    } catch {
                        await send(.conversationsLoaded(.failure(error)))
                    }
                }
                
            case let .matchesLoaded(.success(matches)):
                state.isLoadingMatches = false
                state.isRefreshing = false
                state.matches = matches
                state.error = nil
                return .none
                
            case let .matchesLoaded(.failure(error)):
                state.isLoadingMatches = false
                state.isRefreshing = false
                state.error = error.localizedDescription
                return .none
                
            case let .conversationsLoaded(.success(conversations)):
                state.isLoadingConversations = false
                state.isRefreshing = false
                state.conversations = conversations
                state.error = nil
                return .none
                
            case let .conversationsLoaded(.failure(error)):
                state.isLoadingConversations = false
                state.isRefreshing = false
                state.error = error.localizedDescription
                return .none
                
            // MARK: - Navigation
                
            case let .selectMatch(match):
                state.selectedMatch = match
                state.showingMatchDetail = true
                
                // Load conversation for this match
                if let conversationId = match.conversationId {
                    return .run { send in
                        do {
                            let conversation = try await firebaseService.getConversation(conversationId)
                            await send(.selectConversation(conversation))
                        } catch {
                            // Handle error silently or show alert
                        }
                    }
                }
                return .none
                
            case let .selectConversation(conversation):
                state.selectedConversation = conversation
                return .none
                
            case let .openChat(conversation):
                state.selectedConversation = conversation
                // Navigation handled by parent coordinator
                return .none
                
            case .closeMatchDetail:
                state.showingMatchDetail = false
                state.selectedMatch = nil
                state.selectedConversation = nil
                return .none
                
            // MARK: - Match Actions
                
            case let .unmatchUser(match):
                return .run { send in
                    do {
                        try await firebaseService.unmatch(matchId: match.id)
                        await send(.unmatchResponse(.success(())))
                    } catch {
                        await send(.unmatchResponse(.failure(error)))
                    }
                }
                
            case let .reportUser(match):
                guard let currentUserId = state.currentUserId else { return .none }
                let targetUserId = match.getOtherUserId(currentUserId: currentUserId) ?? ""
                
                return .run { send in
                    do {
                        try await firebaseService.reportUser(
                            reporterId: currentUserId,
                            reportedUserId: targetUserId,
                            reason: "Comportamento inadequado"
                        )
                        await send(.unmatchResponse(.success(())))
                    } catch {
                        await send(.unmatchResponse(.failure(error)))
                    }
                }
                
            case let .blockUser(match):
                guard let currentUserId = state.currentUserId else { return .none }
                let targetUserId = match.getOtherUserId(currentUserId: currentUserId) ?? ""
                
                return .run { send in
                    do {
                        try await firebaseService.blockUser(
                            blockerId: currentUserId,
                            blockedUserId: targetUserId
                        )
                        await send(.unmatchResponse(.success(())))
                    } catch {
                        await send(.unmatchResponse(.failure(error)))
                    }
                }
                
            case .unmatchResponse(.success):
                state.showingMatchDetail = false
                state.selectedMatch = nil
                return .send(.refresh)
                
            case let .unmatchResponse(.failure(error)):
                state.error = error.localizedDescription
                return .none
                
            // MARK: - Filters
                
            case .toggleNewMatchesOnly:
                state.showOnlyNewMatches.toggle()
                return .none
                
            case .toggleUnreadOnly:
                state.showOnlyUnread.toggle()
                return .none
                
            case .clearSearch:
                state.searchText = ""
                return .none
                
            // MARK: - Error Handling
                
            case .dismissError:
                state.error = nil
                return .none
                
            // MARK: - Binding
                
            case .binding:
                return .none
            }
        }
    }
}

// MARK: - Firebase Service Extensions

extension FirebaseService {
    /// Unmatch with a user
    func unmatch(matchId: String) async throws {
        try await db.collection("matches").document(matchId).updateData([
            "isActive": false,
            "updatedAt": Date()
        ])
    }
    
    /// Report a user
    func reportUser(reporterId: String, reportedUserId: String, reason: String) async throws {
        let report: [String: Any] = [
            "reporterId": reporterId,
            "reportedUserId": reportedUserId,
            "reason": reason,
            "createdAt": Date(),
            "status": "pending"
        ]
        
        try await db.collection("reports").addDocument(data: report)
    }
    
    /// Block a user
    func blockUser(blockerId: String, blockedUserId: String) async throws {
        let block: [String: Any] = [
            "blockerId": blockerId,
            "blockedUserId": blockedUserId,
            "createdAt": Date()
        ]
        
        try await db.collection("blocks").addDocument(data: block)
        
        // Also unmatch if there's an active match
        let matches = try await getUserMatches(userId: blockerId)
        let matchToUnmatch = matches.first { match in
            match.getOtherUserId(currentUserId: blockerId) == blockedUserId
        }
        
        if let match = matchToUnmatch {
            try await unmatch(matchId: match.id)
        }
    }
}
