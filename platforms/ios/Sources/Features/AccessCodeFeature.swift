// AccessCodeFeature.swift — FypMatch iOS
// Reducer TCA para fluxo de resgate de código de acesso beta

import Foundation
import ComposableArchitecture

// MARK: - Reducer

@Reducer
struct AccessCodeFeature {

    // MARK: - State

    @ObservableState
    struct State: Equatable {
        /// Código digitado pelo usuário
        var code: String = ""
        /// Indicador de carregamento durante validação/resgate
        var isLoading: Bool = false
        /// Mensagem de erro exibida abaixo do campo
        var errorMessage: String? = nil
        /// Tipo de código resgatado com sucesso
        var redeemedType: AccessCodeType? = nil
        /// Indica se o resgate foi concluído com sucesso
        var isSuccess: Bool = false
        /// Controla navegação para tela de waitlist
        var showingWaitlist: Bool = false

        /// Código normalizado (sem espaços, uppercase)
        var normalizedCode: String {
            code.uppercased().trimmingCharacters(in: .whitespaces)
        }

        /// Habilita o botão de submit quando há conteúdo mínimo
        var canSubmit: Bool {
            !isLoading && normalizedCode.count >= 6
        }
    }

    // MARK: - Action

    enum Action: BindableAction, Equatable {
        case binding(BindingAction<State>)
        /// Usuário pressionou "Resgatar acesso"
        case submitCode
        /// Resultado da validação/resgate assíncrono
        case codeValidated(Result<AccessCodeType, AccessCodeError>)
        /// Usuário tocou em "Não tenho código"
        case skipToWaitlist
        /// Fecha/descarta esta tela
        case dismiss
    }

    // MARK: - Dependencies

    @Dependency(\.accessCodeService) var accessCodeService
    @Dependency(\.firebaseService) var firebaseService
    @Dependency(\.dismiss) var dismiss

    // MARK: - Body

    var body: some ReducerOf<Self> {
        BindingReducer()

        Reduce { state, action in
            switch action {

            // MARK: Binding
            case .binding:
                // Limpa erro ao usuário editar o campo
                state.errorMessage = nil
                return .none

            // MARK: Submit
            case .submitCode:
                guard state.canSubmit else { return .none }

                state.isLoading = true
                state.errorMessage = nil

                let code = state.normalizedCode
                let userId = firebaseService.currentUser?.id ?? "anonymous"

                return .run { send in
                    do {
                        let codeType = try await accessCodeService.redeemCode(code, userId: userId)
                        await send(.codeValidated(.success(codeType)))
                    } catch let error as AccessCodeError {
                        await send(.codeValidated(.failure(error)))
                    } catch {
                        await send(.codeValidated(.failure(.firestoreError(error.localizedDescription))))
                    }
                }

            // MARK: Resultado
            case let .codeValidated(.success(type)):
                state.isLoading = false
                state.redeemedType = type
                state.isSuccess = true
                state.errorMessage = nil
                return .none

            case let .codeValidated(.failure(error)):
                state.isLoading = false
                state.isSuccess = false
                state.errorMessage = error.errorDescription
                return .none

            // MARK: Navegação
            case .skipToWaitlist:
                state.showingWaitlist = true
                return .none

            case .dismiss:
                return .run { _ in await dismiss() }
            }
        }
    }
}
