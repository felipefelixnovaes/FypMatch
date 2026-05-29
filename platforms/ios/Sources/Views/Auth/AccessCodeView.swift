// AccessCodeView.swift — FypMatch iOS
// Tela de resgate de código de acesso beta

import SwiftUI
import ComposableArchitecture

struct AccessCodeView: View {
    @Bindable var store: StoreOf<AccessCodeFeature>

    var body: some View {
        ZStack {
            // Fundo gradient vertical igual à WelcomeView
            LinearGradient.fypGradientVertical
                .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 32) {

                    // MARK: - Header

                    VStack(spacing: 16) {
                        // Ícone 🔑 com círculo branco translúcido
                        ZStack {
                            Circle()
                                .fill(Color.white.opacity(0.2))
                                .frame(width: 96, height: 96)

                            Text("🔑")
                                .font(.system(size: 48))
                        }

                        Text("Código de acesso beta")
                            .font(.title.bold())
                            .foregroundColor(.white)
                            .multilineTextAlignment(.center)

                        Text("Digite seu código de convite para entrar no FypMatch")
                            .font(.subheadline)
                            .foregroundColor(.white.opacity(0.85))
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 8)
                    }
                    .padding(.top, 56)

                    // MARK: - Formulário

                    VStack(spacing: 16) {
                        // TextField com estilo branco arredondado
                        VStack(alignment: .leading, spacing: 6) {
                            TextField("Ex: FYPM-PREM-2024", text: $store.code)
                                .font(.system(size: 16, weight: .medium, design: .monospaced))
                                .foregroundColor(.fypPurple)
                                .autocorrectionDisabled()
                                .textInputAutocapitalization(.characters)
                                .onChange(of: store.code) { _, newValue in
                                    // Limite de 20 caracteres
                                    if newValue.count > 20 {
                                        store.code = String(newValue.prefix(20))
                                    }
                                    // Uppercase automático
                                    store.code = newValue.uppercased()
                                }
                                .padding(.horizontal, 16)
                                .padding(.vertical, 14)
                                .background(Color.white)
                                .clipShape(RoundedRectangle(cornerRadius: 14))
                                .overlay(
                                    RoundedRectangle(cornerRadius: 14)
                                        .stroke(
                                            store.errorMessage != nil ? Color.red : Color.clear,
                                            lineWidth: 1.5
                                        )
                                )
                                .shadow(color: .black.opacity(0.1), radius: 6, y: 3)

                            // Mensagem de erro
                            if let error = store.errorMessage {
                                HStack(spacing: 6) {
                                    Image(systemName: "exclamationmark.circle.fill")
                                        .font(.caption)
                                    Text(error)
                                        .font(.caption)
                                }
                                .foregroundColor(.red)
                                .padding(.horizontal, 4)
                                .transition(.opacity.combined(with: .move(edge: .top)))
                            }
                        }

                        // Botão principal
                        GradientButton(
                            title: "Resgatar acesso",
                            icon: store.isLoading ? nil : "checkmark.seal.fill",
                            isLoading: store.isLoading
                        ) {
                            store.send(.submitCode)
                        }
                        .disabled(!store.canSubmit)
                        .opacity(store.canSubmit ? 1 : 0.6)
                    }
                    .padding(.horizontal, 24)

                    // MARK: - Estado de Sucesso

                    if store.isSuccess, let type = store.redeemedType {
                        SuccessCard(codeType: type)
                            .transition(.scale.combined(with: .opacity))
                            .padding(.horizontal, 24)
                    }

                    // MARK: - Link para waitlist

                    Button {
                        store.send(.skipToWaitlist)
                    } label: {
                        VStack(spacing: 4) {
                            Text("Não tenho código")
                                .font(.subheadline)
                                .foregroundColor(.white.opacity(0.75))
                            Text("quero entrar na lista de espera →")
                                .font(.subheadline.bold())
                                .foregroundColor(.white)
                        }
                    }
                    .padding(.bottom, 40)
                }
            }
        }
        .animation(.easeInOut(duration: 0.3), value: store.isSuccess)
        .animation(.easeInOut(duration: 0.2), value: store.errorMessage)
        .sheet(isPresented: $store.showingWaitlist) {
            NavigationStack {
                WaitlistFormView()
            }
        }
    }
}

// MARK: - SuccessCard

private struct SuccessCard: View {
    let codeType: AccessCodeType

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 48))
                .foregroundColor(.green)

            Text("Acesso liberado!")
                .font(.title2.bold())
                .foregroundColor(.white)

            Text("Bem-vindo ao FypMatch \(codeType.displayName)")
                .font(.subheadline)
                .foregroundColor(.white.opacity(0.9))

            // Badge do tipo de acesso
            HStack(spacing: 6) {
                Circle()
                    .fill(codeType.color)
                    .frame(width: 10, height: 10)
                Text("Plano \(codeType.displayName)")
                    .font(.caption.bold())
                    .foregroundColor(.white)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(Color.white.opacity(0.2))
            .clipShape(Capsule())
        }
        .padding(24)
        .background(Color.white.opacity(0.15))
        .clipShape(RoundedRectangle(cornerRadius: 20))
    }
}

// MARK: - Preview

#Preview {
    AccessCodeView(
        store: Store(initialState: AccessCodeFeature.State()) {
            AccessCodeFeature()
        }
    )
}
