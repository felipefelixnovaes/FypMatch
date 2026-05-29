//
//  UserDetailFeature.swift
//  FypMatch iOS
//
//  Feature TCA para a tela de perfil completo (UserDetailView)
//  Apresentada ao tocar no card de discovery
//

import Foundation
import ComposableArchitecture
import FirebaseFirestore

// MARK: - UserDetailFeature

@Reducer
struct UserDetailFeature {

    // MARK: - State

    @ObservableState
    struct State: Equatable {
        var user: User
        var currentUser: User?
        var currentPhotoIndex: Int = 0
        var compatibilityScore: Double?
        var isLoadingCompatibility: Bool = false
        var swipeAction: SwipeAction? = nil
        var isShowingReport: Bool = false
        var isShowingBlock: Bool = false
        var reportReason: String = ""
        var isSendingSwipe: Bool = false
        var isShowingPremiumUpsell: Bool = false
        var alertMessage: String? = nil
    }

    // MARK: - Action

    enum Action: BindableAction, Equatable {
        case binding(BindingAction<State>)

        // Lifecycle
        case onAppear
        case dismiss

        // Navegação de fotos
        case nextPhoto
        case previousPhoto

        // Ações de swipe
        case like
        case pass
        case superLike
        case swipeCompleted(SwipeAction)
        case swipeFailed(String)

        // Compatibilidade
        case loadCompatibility
        case compatibilityLoaded(Double)

        // Denúncia
        case showReport
        case submitReport(reason: String)
        case reportSubmitted
        case reportFailed(String)

        // Bloqueio
        case showBlock
        case confirmBlock
        case blockConfirmed
        case blockFailed(String)

        // Upsell
        case showPremiumUpsell
        case dismissPremiumUpsell

        // Alerta
        case dismissAlert
    }

    // MARK: - Dependencies

    @Dependency(\.firebaseService) var firebaseService
    @Dependency(\.compatibilityEngine) var compatibilityEngine

    // MARK: - Body

    var body: some ReducerOf<Self> {
        BindingReducer()

        Reduce { state, action in
            switch action {

            // MARK: Lifecycle

            case .onAppear:
                state.currentUser = firebaseService.currentUser
                return .send(.loadCompatibility)

            case .dismiss:
                return .none

            // MARK: Navegação de fotos

            case .nextPhoto:
                guard state.currentPhotoIndex < state.user.photos.count - 1 else { return .none }
                state.currentPhotoIndex += 1
                return .none

            case .previousPhoto:
                guard state.currentPhotoIndex > 0 else { return .none }
                state.currentPhotoIndex -= 1
                return .none

            // MARK: Compatibilidade

            case .loadCompatibility:
                guard !state.isLoadingCompatibility else { return .none }
                state.isLoadingCompatibility = true

                let targetUser = state.user
                let currentUser = state.currentUser

                return .run { [engine = compatibilityEngine] send in
                    if let current = currentUser {
                        let result = engine.calculate(current: current, target: targetUser)
                        await send(.compatibilityLoaded(Double(result.overall)))
                    } else {
                        await send(.compatibilityLoaded(targetUser.compatibilityScore))
                    }
                }

            case let .compatibilityLoaded(score):
                state.isLoadingCompatibility = false
                state.compatibilityScore = score
                return .none

            // MARK: Swipe Actions

            case .like:
                guard !state.isSendingSwipe else { return .none }
                state.isSendingSwipe = true

                let myId = state.currentUser?.id ?? ""
                let targetId = state.user.id

                return .run { send in
                    do {
                        _ = try await firebaseService.recordLike(fromUser: myId, toUser: targetId)
                        await send(.swipeCompleted(.like))
                    } catch {
                        await send(.swipeFailed(error.localizedDescription))
                    }
                }

            case .pass:
                guard !state.isSendingSwipe else { return .none }
                state.isSendingSwipe = true

                let myId = state.currentUser?.id ?? ""
                let targetId = state.user.id

                return .run { send in
                    await firebaseService.recordPass(fromUser: myId, toUser: targetId)
                    await send(.swipeCompleted(.pass))
                }

            case .superLike:
                // Verificar se o usuário é premium
                guard state.currentUser?.isActivePremium == true else {
                    state.isShowingPremiumUpsell = true
                    return .none
                }

                guard !state.isSendingSwipe else { return .none }
                state.isSendingSwipe = true

                let myId = state.currentUser?.id ?? ""
                let targetId = state.user.id

                return .run { send in
                    do {
                        _ = try await firebaseService.recordSuperLike(fromUser: myId, toUser: targetId)
                        await send(.swipeCompleted(.superLike))
                    } catch {
                        await send(.swipeFailed(error.localizedDescription))
                    }
                }

            case let .swipeCompleted(action):
                state.isSendingSwipe = false
                state.swipeAction = action
                return .none

            case let .swipeFailed(message):
                state.isSendingSwipe = false
                state.alertMessage = "Erro ao processar ação: \(message)"
                return .none

            // MARK: Denúncia

            case .showReport:
                state.isShowingReport = true
                return .none

            case let .submitReport(reason):
                let myId = state.currentUser?.id ?? ""
                let targetId = state.user.id
                let reportData: [String: Any] = [
                    "userId": myId,
                    "targetId": targetId,
                    "reason": reason,
                    "reportedAt": Timestamp(date: Date()),
                    "status": "pending"
                ]

                return .run { send in
                    do {
                        let db = Firestore.firestore()
                        try await db.collection("reports").addDocument(data: reportData)
                        await send(.reportSubmitted)
                    } catch {
                        await send(.reportFailed(error.localizedDescription))
                    }
                }

            case .reportSubmitted:
                state.isShowingReport = false
                state.reportReason = ""
                state.alertMessage = "Denúncia enviada. Obrigado por contribuir com a segurança da comunidade."
                return .none

            case let .reportFailed(message):
                state.isShowingReport = false
                state.alertMessage = "Erro ao enviar denúncia: \(message)"
                return .none

            // MARK: Bloqueio

            case .showBlock:
                state.isShowingBlock = true
                return .none

            case .confirmBlock:
                let myId = state.currentUser?.id ?? ""
                let targetId = state.user.id

                return .run { send in
                    do {
                        // Tentar desfazer match caso exista
                        try await firebaseService.unmatch(userId: myId, matchedUserId: targetId)
                        // Adicionar à blocklist
                        let db = Firestore.firestore()
                        try await db.collection("blocklist")
                            .document("\(myId)_\(targetId)")
                            .setData([
                                "blockerId": myId,
                                "blockedId": targetId,
                                "blockedAt": Timestamp(date: Date())
                            ])
                        await send(.blockConfirmed)
                    } catch {
                        await send(.blockFailed(error.localizedDescription))
                    }
                }

            case .blockConfirmed:
                state.isShowingBlock = false
                state.swipeAction = .block
                return .none

            case let .blockFailed(message):
                state.isShowingBlock = false
                state.alertMessage = "Erro ao bloquear usuário: \(message)"
                return .none

            // MARK: Upsell

            case .showPremiumUpsell:
                state.isShowingPremiumUpsell = true
                return .none

            case .dismissPremiumUpsell:
                state.isShowingPremiumUpsell = false
                return .none

            // MARK: Alerta

            case .dismissAlert:
                state.alertMessage = nil
                return .none

            case .binding:
                return .none
            }
        }
    }

}

// MARK: - FirebaseService Extension para Unmatch/Block

extension FirebaseService {
    /// Desativa um match existente entre dois usuários
    func unmatch(userId: String, matchedUserId: String) async throws {
        let db = Firestore.firestore()
        let matchId = [userId, matchedUserId].sorted().joined(separator: "_")

        let snapshot = try await db.collection("matches")
            .whereField("id", isEqualTo: matchId)
            .getDocuments()

        for doc in snapshot.documents {
            try await doc.reference.updateData([
                "isActive": false,
                "isBlocked": true,
                "blockedBy": userId,
                "blockedAt": Timestamp(date: Date()),
                "updatedAt": Timestamp(date: Date())
            ])
        }
    }
}
