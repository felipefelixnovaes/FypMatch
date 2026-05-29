// LoginView.swift — FypMatch iOS
// Tela de login — port de LoginScreen.kt

import SwiftUI
import ComposableArchitecture

struct LoginView: View {
    @Bindable var store: StoreOf<AuthFeature>
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // Header
                    VStack(spacing: 8) {
                        Image(systemName: "heart.circle.fill")
                            .font(.system(size: 56))
                            .foregroundStyle(LinearGradient.fypGradient)
                        GradientText(text: "Entrar no FypMatch")
                        Text("Bem-vindo de volta 💜")
                            .font(.subheadline).foregroundColor(.secondary)
                    }
                    .padding(.top, 24)

                    // Form
                    VStack(spacing: 16) {
                        FypTextField(
                            placeholder: "Email",
                            text: $store.loginEmail,
                            icon: "envelope"
                        )
                        .keyboardType(.emailAddress)
                        .autocapitalization(.none)

                        FypTextField(
                            placeholder: "Senha",
                            text: $store.loginPassword,
                            isSecure: !store.showLoginPassword,
                            error: store.loginPasswordError,
                            icon: "lock"
                        )

                        HStack {
                            Spacer()
                            Button("Esqueci a senha") { store.send(.showForgotPassword(true)) }
                                .font(.subheadline).foregroundColor(.fypPink)
                        }
                    }

                    // Error
                    if let error = store.authError {
                        HStack {
                            Image(systemName: "exclamationmark.triangle.fill").foregroundColor(.orange)
                            Text(error).font(.subheadline).foregroundColor(.orange)
                        }
                        .padding(12)
                        .background(Color.orange.opacity(0.1))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                    }

                    // Login button
                    GradientButton(
                        title: "Entrar",
                        icon: "arrow.right",
                        isLoading: store.isLoading
                    ) { store.send(.signIn) }
                    .disabled(!store.isLoginFormValid)
                    .opacity(store.isLoginFormValid ? 1 : 0.5)

                    // Divider
                    HStack {
                        Rectangle().frame(height: 1).foregroundColor(.secondary.opacity(0.3))
                        Text("ou").font(.subheadline).foregroundColor(.secondary)
                        Rectangle().frame(height: 1).foregroundColor(.secondary.opacity(0.3))
                    }

                    // Google Sign-In
                    Button {
                        store.send(.signInWithGoogle)
                    } label: {
                        HStack(spacing: 12) {
                            Image(systemName: "globe").font(.system(size: 18))
                            Text("Continuar com Google").font(.system(size: 15, weight: .medium))
                        }
                        .foregroundColor(.primary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(Color(.secondarySystemBackground))
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.secondary.opacity(0.3), lineWidth: 1))
                    }

                    // Apple Sign-In
                    Button {
                        store.send(.signInWithApple)
                    } label: {
                        HStack(spacing: 12) {
                            Image(systemName: "apple.logo").font(.system(size: 18))
                            Text("Continuar com Apple").font(.system(size: 15, weight: .medium))
                        }
                        .foregroundColor(.primary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(Color(.secondarySystemBackground))
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.secondary.opacity(0.3), lineWidth: 1))
                    }

                    // Create account
                    Button {
                        dismiss()
                        store.send(.showRegister(true))
                    } label: {
                        HStack(spacing: 4) {
                            Text("Não tem conta?").foregroundColor(.secondary)
                            Text("Criar agora").foregroundColor(.fypPink).fontWeight(.semibold)
                        }
                        .font(.subheadline)
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
        .sheet(isPresented: $store.showingForgotPassword) {
            ForgotPasswordView(store: store)
        }
    }
}

// MARK: - ForgotPasswordView

struct ForgotPasswordView: View {
    @Bindable var store: StoreOf<AuthFeature>
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                Image(systemName: "key.fill")
                    .font(.system(size: 48)).foregroundStyle(LinearGradient.fypGradient)
                    .padding(.top, 32)

                Text("Recuperar senha").font(.title2.bold())
                Text("Enviaremos um link para redefinir sua senha.")
                    .font(.subheadline).foregroundColor(.secondary).multilineTextAlignment(.center)

                if store.forgotPasswordSent {
                    VStack(spacing: 8) {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 40)).foregroundColor(.green)
                        Text("Email enviado!").font(.headline)
                        Text("Verifique sua caixa de entrada.").font(.subheadline).foregroundColor(.secondary)
                    }
                    .padding(16)
                    .background(Color.green.opacity(0.1))
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                } else {
                    FypTextField(
                        placeholder: "Seu email",
                        text: $store.forgotPasswordEmail,
                        icon: "envelope"
                    )
                    .keyboardType(.emailAddress).autocapitalization(.none)

                    GradientButton(title: "Enviar link", isLoading: store.isLoading) {
                        store.send(.sendPasswordReset)
                    }
                }

                Spacer()
            }
            .padding(.horizontal, 24)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Fechar") { dismiss() }.foregroundColor(.fypPink)
                }
            }
        }
    }
}
