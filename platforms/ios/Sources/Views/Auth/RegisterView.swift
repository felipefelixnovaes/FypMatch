// RegisterView.swift — FypMatch iOS
import SwiftUI
import ComposableArchitecture

struct RegisterView: View {
    @Bindable var store: StoreOf<AuthFeature>
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    VStack(spacing: 6) {
                        GradientText(text: "Criar conta").padding(.top, 24)
                        Text("É grátis e leva menos de 2 minutos")
                            .font(.subheadline).foregroundColor(.secondary)
                    }

                    VStack(spacing: 14) {
                        FypTextField(placeholder: "Nome", text: $store.registerDisplayName,
                                     error: store.registerDisplayNameError, icon: "person")
                        FypTextField(placeholder: "Email", text: $store.registerEmail,
                                     error: store.registerEmailError, icon: "envelope")
                            .keyboardType(.emailAddress).autocapitalization(.none)
                        FypTextField(placeholder: "Senha", text: $store.registerPassword,
                                     isSecure: !store.showRegisterPassword,
                                     error: store.registerPasswordError, icon: "lock")
                        FypTextField(placeholder: "Confirmar senha", text: $store.registerConfirmPassword,
                                     isSecure: !store.showRegisterConfirmPassword,
                                     error: store.registerConfirmPasswordError, icon: "lock.shield")

                        // Age picker
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Idade: \(store.registerAge) anos")
                                .font(.subheadline).foregroundColor(.secondary)
                            Slider(value: Binding(
                                get: { Double(store.registerAge) },
                                set: { store.registerAge = Int($0) }
                            ), in: 18...80, step: 1)
                            .accentColor(.fypPink)
                            if let err = store.registerAgeError {
                                Text(err).font(.caption).foregroundColor(.red)
                            }
                        }
                        .padding(14).background(Color(.secondarySystemBackground))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }

                    if let error = store.authError {
                        HStack {
                            Image(systemName: "exclamationmark.triangle.fill").foregroundColor(.orange)
                            Text(error).font(.subheadline).foregroundColor(.orange)
                        }
                        .padding(12).background(Color.orange.opacity(0.1))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                    }

                    GradientButton(title: "Criar conta grátis", icon: "sparkles",
                                   isLoading: store.isLoading) { store.send(.signUp) }
                        .disabled(!store.isRegisterFormValid)
                        .opacity(store.isRegisterFormValid ? 1 : 0.5)

                    Text("Ao criar uma conta você concorda com nossos Termos de Uso e Política de Privacidade.")
                        .font(.caption).foregroundColor(.secondary).multilineTextAlignment(.center)

                    Button {
                        dismiss()
                    } label: {
                        HStack(spacing: 4) {
                            Text("Já tem conta?").foregroundColor(.secondary)
                            Text("Entrar").foregroundColor(.fypPink).fontWeight(.semibold)
                        }.font(.subheadline)
                    }
                    .padding(.bottom, 32)
                }
                .padding(.horizontal, 24)
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cancelar") { dismiss() }.foregroundColor(.fypPink)
                }
            }
        }
    }
}
