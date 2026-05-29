// MatchesFeature.swift — FypMatch iOS
// TCA Feature para lista de matches e navegação para chat

import Foundation
import ComposableArchitecture

@Reducer
struct MatchesFeature {

    // MARK: - State

    @ObservableState
    struct State: Equatable {
        var matches: [Match] = []
        var conversations: [Conversation] = []
        var isLoading = false
        var loadingError: String?
        var selectedConversation: Conversation?
        var isShowingChat = false
        var currentUser: User?

        // Matches organizados: novos (sem mensagem) e com conversa ativa
        var newMatches: [Match] {
            matches.filter { match in
                !conversations.contains { $0.matchId == match.id }
            }
        }

        var activeConversations: [Conversation] {
            conversations.sorted { $0.lastActivity > $1.lastActivity }
        }

        var totalUnread: Int {
            conversations.reduce(0) { sum, conv in
                let userId = currentUser?.id ?? ""
                return sum + conv.getUnreadCount(userId: userId)
            }
        }
    }

    // MARK: - Action

    enum Action: BindableAction, Equatable {
        case binding(BindingAction<State>)
        case onAppear
        case loadMatches
        case matchesLoaded(Result<[Match], Error>)
        case loadConversations
        case conversationsLoaded(Result<[Conversation], Error>)
        case selectConversation(Conversation)
        case dismissChat
        case startConversation(Match)
        case conversationStarted(Result<Conversation, Error>)
        case unmatchUser(Match)
        case unmatchConfirmed(Result<Void, Error>)
        case refresh
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
                return .merge(
                    .send(.loadMatches),
                    .send(.loadConversations)
                )

            case .refresh:
                state.isLoading = true
                return .merge(
                    .send(.loadMatches),
                    .send(.loadConversations)
                )

            case .loadMatches:
                state.isLoading = true
                state.loadingError = nil
                return .run { [userId = state.currentUser?.id] send in
                    guard let userId else {
                        await send(.matchesLoaded(.success([])))
                        return
                    }
                    do {
                        let matches = try await firebaseService.loadMatches(userId: userId)
                        await send(.matchesLoaded(.success(matches)))
                    } catch {
                        await send(.matchesLoaded(.failure(error)))
                    }
                }

            case let .matchesLoaded(.success(matches)):
                state.isLoading = false
                state.matches = matches
                return .none

            case let .matchesLoaded(.failure(error)):
                state.isLoading = false
                state.loadingError = error.localizedDescription
                return .none

            case .loadConversations:
                return .run { [userId = state.currentUser?.id] send in
                    guard let userId else {
                        await send(.conversationsLoaded(.success([])))
                        return
                    }
                    do {
                        let conversations = try await firebaseService.loadConversations(userId: userId)
                        await send(.conversationsLoaded(.success(conversations)))
                    } catch {
                        await send(.conversationsLoaded(.failure(error)))
                    }
                }

            case let .conversationsLoaded(.success(conversations)):
                state.conversations = conversations
                return .none

            case let .conversationsLoaded(.failure(error)):
                state.loadingError = error.localizedDescription
                return .none

            case let .selectConversation(conversation):
                state.selectedConversation = conversation
                state.isShowingChat = true
                return .none

            case .dismissChat:
                state.isShowingChat = false
                state.selectedConversation = nil
                return .none

            case let .startConversation(match):
                return .run { [userId = state.currentUser?.id] send in
                    guard let userId else { return }
                    do {
                        let conversation = try await firebaseService.getOrCreateConversation(
                            matchId: match.id,
                            userId: userId,
                            otherUserId: match.getOtherUserId(currentUserId: userId) ?? ""
                        )
                        await send(.conversationStarted(.success(conversation)))
                    } catch {
                        await send(.conversationStarted(.failure(error)))
                    }
                }

            case let .conversationStarted(.success(conversation)):
                state.selectedConversation = conversation
                state.isShowingChat = true
                return .none

            case .conversationStarted(.failure):
                return .none

            case let .unmatchUser(match):
                return .run { [userId = state.currentUser?.id] send in
                    guard let userId else { return }
                    do {
                        try await firebaseService.unmatch(matchId: match.id, userId: userId)
                        await send(.unmatchConfirmed(.success(())))
                    } catch {
                        await send(.unmatchConfirmed(.failure(error)))
                    }
                }

            case .unmatchConfirmed(.success):
                return .send(.refresh)

            case .unmatchConfirmed(.failure):
                return .none

            case .binding:
                return .none
            }
        }
    }
}
