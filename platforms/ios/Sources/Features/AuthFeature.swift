//
//  AuthFeature.swift
//  FypMatch iOS
//
//  Feature de autenticação usando The Composable Architecture
//  Gerencia login, registro e estado de autenticação
//

import Foundation
import ComposableArchitecture
import FirebaseAuth

/// Feature responsável por toda a autenticação do app
@Reducer
struct AuthFeature {
    
    // MARK: - State
    
    @ObservableState
    struct State: Equatable {
        // MARK: - Authentication State
        var isAuthenticated = false
        var currentUser: User?
        var isLoading = false
        var authError: String?
        
        // MARK: - Login Form
        var loginEmail = ""
        var loginPassword = ""
        var showLoginPassword = false
        
        // MARK: - Register Form
        var registerEmail = ""
        var registerPassword = ""
        var registerConfirmPassword = ""
        var registerDisplayName = ""
        var registerAge = 25
        var showRegisterPassword = false
        var showRegisterConfirmPassword = false
        
        // MARK: - UI State
        var showingRegister = false
        var showingForgotPassword = false
        var forgotPasswordEmail = ""
        var forgotPasswordSent = false
        
        // MARK: - Validation
        var isLoginFormValid: Bool {
            !loginEmail.isEmpty && 
            !loginPassword.isEmpty && 
            loginEmail.contains("@") &&
            loginPassword.count >= 6
        }
        
        var isRegisterFormValid: Bool {
            !registerEmail.isEmpty &&
            !registerPassword.isEmpty &&
            !registerDisplayName.isEmpty &&
            registerEmail.contains("@") &&
            registerPassword.count >= 6 &&
            registerPassword == registerConfirmPassword &&
            registerAge >= 18 &&
            registerAge <= 100
        }
        
        var passwordsMatch: Bool {
            registerPassword == registerConfirmPassword
        }
        
        // MARK: - Error Messages
        var loginEmailError: String? {
            if !loginEmail.isEmpty && !loginEmail.contains("@") {
                return "Email inválido"
            }
            return nil
        }
        
        var loginPasswordError: String? {
            if !loginPassword.isEmpty && loginPassword.count < 6 {
                return "Senha deve ter pelo menos 6 caracteres"
            }
            return nil
        }
        
        var registerEmailError: String? {
            if !registerEmail.isEmpty && !registerEmail.contains("@") {
                return "Email inválido"
            }
            return nil
        }
        
        var registerPasswordError: String? {
            if !registerPassword.isEmpty && registerPassword.count < 6 {
                return "Senha deve ter pelo menos 6 caracteres"
            }
            return nil
        }
        
        var registerConfirmPasswordError: String? {
            if !registerConfirmPassword.isEmpty && !passwordsMatch {
                return "Senhas não coincidem"
            }
            return nil
        }
        
        var registerDisplayNameError: String? {
            if !registerDisplayName.isEmpty && registerDisplayName.count < 2 {
                return "Nome deve ter pelo menos 2 caracteres"
            }
            return nil
        }
        
        var registerAgeError: String? {
            if registerAge < 18 {
                return "Você deve ter pelo menos 18 anos"
            } else if registerAge > 100 {
                return "Idade inválida"
            }
            return nil
        }
    }
    
    // MARK: - Action
    
    enum Action: BindableAction, Equatable {
        // MARK: - Binding
        case binding(BindingAction<State>)
        
        // MARK: - Lifecycle
        case onAppear
        case checkAuthStatus
        
        // MARK: - Authentication Actions
        case signIn
        case signUp
        case signOut
        case signInWithGoogle
        case signInWithApple
        case sendPasswordReset
        
        // MARK: - Navigation Actions
        case showRegister(Bool)
        case showForgotPassword(Bool)
        
        // MARK: - Form Actions
        case clearForms
        case dismissError
        
        // MARK: - Response Actions
        case authenticationResponse(Result<User, Error>)
        case passwordResetResponse(Result<Void, Error>)
        case authStateChanged(Bool, User?)
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
                return .run { send in
                    await send(.checkAuthStatus)
                }
                
            case .checkAuthStatus:
                state.isLoading = true
                return .run { send in
                    let isAuthenticated = firebaseService.isAuthenticated
                    let currentUser = firebaseService.currentUser
                    await send(.authStateChanged(isAuthenticated, currentUser))
                }
                
            // MARK: - Authentication
                
            case .signIn:
                guard state.isLoginFormValid else { return .none }
                
                state.isLoading = true
                state.authError = nil
                
                return .run { [email = state.loginEmail, password = state.loginPassword] send in
                    do {
                        try await firebaseService.signIn(email: email, password: password)
                        let user = firebaseService.currentUser
                        if let user = user {
                            await send(.authenticationResponse(.success(user)))
                        }
                    } catch {
                        await send(.authenticationResponse(.failure(error)))
                    }
                }
                
            case .signUp:
                guard state.isRegisterFormValid else { return .none }
                
                state.isLoading = true
                state.authError = nil
                
                return .run { [
                    email = state.registerEmail,
                    password = state.registerPassword,
                    displayName = state.registerDisplayName,
                    age = state.registerAge
                ] send in
                    do {
                        try await firebaseService.signUp(
                            email: email,
                            password: password,
                            displayName: displayName,
                            age: age
                        )
                        let user = firebaseService.currentUser
                        if let user = user {
                            await send(.authenticationResponse(.success(user)))
                        }
                    } catch {
                        await send(.authenticationResponse(.failure(error)))
                    }
                }
                
            case .signOut:
                state.isLoading = true
                return .run { send in
                    do {
                        try firebaseService.signOut()
                        await send(.authStateChanged(false, nil))
                    } catch {
                        await send(.authenticationResponse(.failure(error)))
                    }
                }
                
            case .signInWithGoogle:
                state.isLoading = true
                state.authError = nil
                
                return .run { send in
                    do {
                        try await firebaseService.signInWithGoogle()
                        let user = firebaseService.currentUser
                        if let user = user {
                            await send(.authenticationResponse(.success(user)))
                        }
                    } catch {
                        await send(.authenticationResponse(.failure(error)))
                    }
                }
                
            case .signInWithApple:
                state.isLoading = true
                state.authError = nil
                
                return .run { send in
                    do {
                        try await firebaseService.signInWithApple()
                        let user = firebaseService.currentUser
                        if let user = user {
                            await send(.authenticationResponse(.success(user)))
                        }
                    } catch {
                        await send(.authenticationResponse(.failure(error)))
                    }
                }
                
            case .sendPasswordReset:
                guard !state.forgotPasswordEmail.isEmpty && 
                      state.forgotPasswordEmail.contains("@") else { return .none }
                
                state.isLoading = true
                state.authError = nil
                
                return .run { [email = state.forgotPasswordEmail] send in
                    do {
                        try await firebaseService.resetPassword(email: email)
                        await send(.passwordResetResponse(.success(())))
                    } catch {
                        await send(.passwordResetResponse(.failure(error)))
                    }
                }
                
            // MARK: - Navigation
                
            case let .showRegister(show):
                state.showingRegister = show
                if show {
                    state.clearLoginForm()
                } else {
                    state.clearRegisterForm()
                }
                return .none
                
            case let .showForgotPassword(show):
                state.showingForgotPassword = show
                state.forgotPasswordSent = false
                if !show {
                    state.forgotPasswordEmail = ""
                }
                return .none
                
            // MARK: - Form Management
                
            case .clearForms:
                state.clearAllForms()
                return .none
                
            case .dismissError:
                state.authError = nil
                return .none
                
            // MARK: - Response Handling
                
            case let .authenticationResponse(.success(user)):
                state.isLoading = false
                state.currentUser = user
                state.isAuthenticated = true
                state.authError = nil
                state.clearAllForms()
                return .none
                
            case let .authenticationResponse(.failure(error)):
                state.isLoading = false
                state.authError = error.localizedDescription
                return .none
                
            case .passwordResetResponse(.success):
                state.isLoading = false
                state.forgotPasswordSent = true
                state.authError = nil
                return .none
                
            case let .passwordResetResponse(.failure(error)):
                state.isLoading = false
                state.authError = error.localizedDescription
                return .none
                
            case let .authStateChanged(isAuthenticated, user):
                state.isLoading = false
                state.isAuthenticated = isAuthenticated
                state.currentUser = user
                if isAuthenticated {
                    state.clearAllForms()
                }
                return .none
                
            // MARK: - Binding
                
            case .binding:
                return .none
            }
        }
    }
}

// MARK: - State Extensions

extension AuthFeature.State {
    mutating func clearLoginForm() {
        loginEmail = ""
        loginPassword = ""
        showLoginPassword = false
    }
    
    mutating func clearRegisterForm() {
        registerEmail = ""
        registerPassword = ""
        registerConfirmPassword = ""
        registerDisplayName = ""
        registerAge = 25
        showRegisterPassword = false
        showRegisterConfirmPassword = false
    }
    
    mutating func clearAllForms() {
        clearLoginForm()
        clearRegisterForm()
        forgotPasswordEmail = ""
        forgotPasswordSent = false
        authError = nil
    }
}

// MARK: - Dependencies

/// Dependência para o FirebaseService
private enum FirebaseServiceKey: DependencyKey {
    static let liveValue = FirebaseService.shared
}

extension DependencyValues {
    var firebaseService: FirebaseService {
        get { self[FirebaseServiceKey.self] }
        set { self[FirebaseServiceKey.self] = newValue }
    }
}