//
//  FirebaseService.swift
//  FypMatch iOS
//
//  Serviço principal para integração com Firebase
//  Gerencia autenticação, Firestore e Storage
//

import Foundation
import FirebaseAuth
import FirebaseFirestore
import FirebaseStorage
import FirebaseMessaging
import Combine

/// Serviço principal para todas as operações Firebase no FypMatch
@MainActor
class FirebaseService: ObservableObject {
    
    // MARK: - Singleton
    static let shared = FirebaseService()
    
    // MARK: - Properties
    private let auth = Auth.auth()
    private let db = Firestore.firestore()
    private let storage = Storage.storage()
    
    @Published var currentUser: User?
    @Published var isAuthenticated = false
    @Published var authError: String?
    
    private var cancellables = Set<AnyCancellable>()
    
    // MARK: - Initialization
    private init() {
        setupAuthStateListener()
        configureFCM()
    }
    
    // MARK: - Authentication State
    
    /// Configura listener para mudanças no estado de autenticação
    private func setupAuthStateListener() {
        auth.addStateDidChangeListener { [weak self] _, user in
            Task { @MainActor in
                self?.isAuthenticated = user != nil
                if let user = user {
                    await self?.loadCurrentUser(firebaseUser: user)
                } else {
                    self?.currentUser = nil
                }
            }
        }
    }
    
    // MARK: - User Authentication
    
    /// Faz login com email e senha
    func signIn(email: String, password: String) async throws {
        do {
            let result = try await auth.signIn(withEmail: email, password: password)
            await loadCurrentUser(firebaseUser: result.user)
        } catch {
            await MainActor.run {
                self.authError = "Erro no login: \(error.localizedDescription)"
            }
            throw error
        }
    }
    
    /// Registra novo usuário com email e senha
    func signUp(email: String, password: String, displayName: String, age: Int) async throws {
        do {
            let result = try await auth.createUser(withEmail: email, password: password)
            
            // Criar perfil inicial do usuário
            let newUser = User(
                id: result.user.uid,
                email: email,
                displayName: displayName,
                age: age
            )
            
            try await saveUser(newUser)
            await loadCurrentUser(firebaseUser: result.user)
            
        } catch {
            await MainActor.run {
                self.authError = "Erro no registro: \(error.localizedDescription)"
            }
            throw error
        }
    }
    
    /// Login com Google (integração futura)
    func signInWithGoogle() async throws {
        // TODO: Implementar Google Sign In
        throw FirebaseServiceError.notImplemented("Google Sign In")
    }
    
    /// Login com Apple (integração futura)
    func signInWithApple() async throws {
        // TODO: Implementar Apple Sign In
        throw FirebaseServiceError.notImplemented("Apple Sign In")
    }
    
    /// Faz logout do usuário
    func signOut() throws {
        try auth.signOut()
        currentUser = nil
        isAuthenticated = false
    }
    
    /// Envia email de redefinição de senha
    func resetPassword(email: String) async throws {
        try await auth.sendPasswordReset(withEmail: email)
    }
    
    // MARK: - User Management
    
    /// Carrega dados do usuário atual do Firestore
    private func loadCurrentUser(firebaseUser: FirebaseAuth.User) async {
        do {
            let document = try await db.collection("users").document(firebaseUser.uid).getDocument()
            
            if document.exists, let data = document.data() {
                let user = try Firestore.Decoder().decode(User.self, from: data)
                await MainActor.run {
                    self.currentUser = user
                }
            } else {
                // Criar usuário básico se não existir
                let basicUser = User(
                    id: firebaseUser.uid,
                    email: firebaseUser.email ?? "",
                    displayName: firebaseUser.displayName ?? "Usuário",
                    age: 25
                )
                try await saveUser(basicUser)
            }
        } catch {
            print("Erro ao carregar usuário: \(error)")
        }
    }
    
    /// Salva dados do usuário no Firestore
    func saveUser(_ user: User) async throws {
        var updatedUser = user
        updatedUser = User(
            id: user.id,
            email: user.email,
            displayName: user.displayName,
            age: user.age,
            bio: user.bio,
            photos: user.photos,
            profileImageURL: user.profileImageURL,
            isPhotoVerified: user.isPhotoVerified,
            latitude: user.latitude,
            longitude: user.longitude,
            city: user.city,
            state: user.state,
            country: user.country,
            maxDistanceKm: user.maxDistanceKm,
            genderInterest: user.genderInterest,
            ageRangeMin: user.ageRangeMin,
            ageRangeMax: user.ageRangeMax,
            interestedInSeriousRelationship: user.interestedInSeriousRelationship,
            interestedInCasualDating: user.interestedInCasualDating,
            interestedInFriendship: user.interestedInFriendship,
            gender: user.gender,
            height: user.height,
            occupation: user.occupation,
            education: user.education,
            children: user.children,
            wantsChildren: user.wantsChildren,
            smoking: user.smoking,
            drinking: user.drinking,
            religion: user.religion,
            politicalViews: user.politicalViews,
            interests: user.interests,
            hobbies: user.hobbies,
            languages: user.languages,
            traveledCountries: user.traveledCountries,
            favoriteActivities: user.favoriteActivities,
            isPremium: user.isPremium,
            premiumExpiresAt: user.premiumExpiresAt,
            isVerified: user.isVerified,
            isOnline: user.isOnline,
            lastActiveAt: user.lastActiveAt,
            swipeCount: user.swipeCount,
            matchCount: user.matchCount,
            messageCount: user.messageCount,
            profileViews: user.profileViews,
            likes: user.likes,
            passes: user.passes,
            pushNotificationsEnabled: user.pushNotificationsEnabled,
            showOnlineStatus: user.showOnlineStatus,
            showDistanceInProfile: user.showDistanceInProfile,
            allowMessageFromMatches: user.allowMessageFromMatches,
            createdAt: user.createdAt,
            updatedAt: Date(), // Sempre atualizar timestamp
            deviceToken: user.deviceToken,
            isEmailVerified: user.isEmailVerified,
            isPhoneVerified: user.isPhoneVerified,
            phoneNumber: user.phoneNumber
        )
        
        let data = try Firestore.Encoder().encode(updatedUser)
        try await db.collection("users").document(user.id).setData(data)
        
        await MainActor.run {
            self.currentUser = updatedUser
        }
    }
    
    /// Busca usuários para discovery baseado nas preferências
    func fetchDiscoveryUsers(limit: Int = 20) async throws -> [User] {
        guard let currentUser = currentUser else {
            throw FirebaseServiceError.notAuthenticated
        }
        
        var query: Query = db.collection("users")
            .whereField("id", isNotEqualTo: currentUser.id)
            .limit(to: limit)
        
        // Filtros baseados nas preferências do usuário
        switch currentUser.genderInterest {
        case .men:
            query = query.whereField("gender", isEqualTo: Gender.male.rawValue)
        case .women:
            query = query.whereField("gender", isEqualTo: Gender.female.rawValue)
        case .nonBinary:
            query = query.whereField("gender", isEqualTo: Gender.nonBinary.rawValue)
        case .all:
            break // Sem filtro de gênero
        }
        
        // Filtro de idade
        query = query
            .whereField("age", isGreaterThanOrEqualTo: currentUser.ageRangeMin)
            .whereField("age", isLessThanOrEqualTo: currentUser.ageRangeMax)
        
        let snapshot = try await query.getDocuments()
        
        return try snapshot.documents.compactMap { document in
            try Firestore.Decoder().decode(User.self, from: document.data())
        }
    }
    
    // MARK: - Photo Upload
    
    /// Faz upload de foto do perfil
    func uploadPhoto(_ imageData: Data, userId: String) async throws -> String {
        let photoId = UUID().uuidString
        let ref = storage.reference().child("users/\(userId)/photos/\(photoId).jpg")
        
        let metadata = StorageMetadata()
        metadata.contentType = "image/jpeg"
        
        _ = try await ref.putDataAsync(imageData, metadata: metadata)
        let downloadURL = try await ref.downloadURL()
        
        return downloadURL.absoluteString
    }
    
    /// Remove foto do perfil
    func deletePhoto(url: String) async throws {
        let ref = storage.reference(forURL: url)
        try await ref.delete()
    }
    
    // MARK: - FCM Configuration
    
    /// Configura Firebase Cloud Messaging
    private func configureFCM() {
        Messaging.messaging().delegate = self
        
        // Solicitar token FCM
        Messaging.messaging().token { [weak self] token, error in
            if let error = error {
                print("Erro ao obter FCM token: \(error)")
            } else if let token = token {
                print("FCM token: \(token)")
                Task {
                    await self?.updateDeviceToken(token)
                }
            }
        }
    }
    
    /// Atualiza token do dispositivo no perfil do usuário
    private func updateDeviceToken(_ token: String) async {
        guard let currentUser = currentUser else { return }
        
        do {
            try await db.collection("users").document(currentUser.id).updateData([
                "deviceToken": token,
                "updatedAt": Timestamp(date: Date())
            ])
        } catch {
            print("Erro ao atualizar device token: \(error)")
        }
    }
    
    // MARK: - Real-time Updates
    
    /// Atualiza status online do usuário
    func updateOnlineStatus(_ isOnline: Bool) async {
        guard let currentUser = currentUser else { return }
        
        do {
            try await db.collection("users").document(currentUser.id).updateData([
                "isOnline": isOnline,
                "lastActiveAt": Timestamp(date: Date()),
                "updatedAt": Timestamp(date: Date())
            ])
        } catch {
            print("Erro ao atualizar status online: \(error)")
        }
    }
}

// MARK: - MessagingDelegate

extension FirebaseService: MessagingDelegate {
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        guard let token = fcmToken else { return }
        print("FCM token atualizado: \(token)")
        
        Task {
            await updateDeviceToken(token)
        }
    }
}

// MARK: - Discovery and Matching

extension FirebaseService {
    /// Fetch discovery users based on user preferences
    func fetchDiscoveryUsers(limit: Int = 20) async throws -> [User] {
        guard let currentUser = currentUser else {
            throw FirebaseServiceError.notAuthenticated
        }
        
        // Query users based on preferences
        var query = db.collection("users")
            .whereField("id", isNotEqualTo: currentUser.id)
            .limit(to: limit)
        
        // Filter by gender interest
        if currentUser.genderInterest != .all {
            query = query.whereField("gender", isEqualTo: currentUser.genderInterest.rawValue)
        }
        
        // Filter by age range
        query = query.whereField("age", isGreaterThanOrEqualTo: currentUser.ageRangeMin)
        query = query.whereField("age", isLessThanOrEqualTo: currentUser.ageRangeMax)
        
        let snapshot = try await query.getDocuments()
        
        return snapshot.documents.compactMap { doc in
            try? doc.data(as: User.self)
        }
    }
    
    /// Record a swipe action
    func recordSwipe(_ swipe: SwipeRecord) async throws {
        let encoder = Firestore.Encoder()
        let data = try encoder.encode(swipe)
        try await db.collection("swipes").document(swipe.id).setData(data)
    }
    
    /// Check if there's a reciprocal like
    func checkReciprocalLike(userId: String, targetUserId: String) async throws -> Bool {
        let snapshot = try await db.collection("swipes")
            .whereField("userId", isEqualTo: targetUserId)
            .whereField("targetUserId", isEqualTo: userId)
            .whereField("action", isEqualTo: SwipeAction.like.rawValue)
            .getDocuments()
        
        return !snapshot.documents.isEmpty
    }
    
    /// Create a match
    func createMatch(_ match: Match) async throws {
        let encoder = Firestore.Encoder()
        let data = try encoder.encode(match)
        try await db.collection("matches").document(match.id).setData(data)
        
        // Create conversation for the match
        let conversation = Conversation(
            matchId: match.id,
            participant1Id: match.user1Id,
            participant2Id: match.user2Id
        )
        
        let conversationData = try encoder.encode(conversation)
        try await db.collection("conversations").document(conversation.id).setData(conversationData)
    }
    
    /// Get user matches
    func getUserMatches(userId: String, limit: Int = 50) async throws -> [Match] {
        let snapshot1 = try await db.collection("matches")
            .whereField("user1Id", isEqualTo: userId)
            .whereField("isActive", isEqualTo: true)
            .limit(to: limit)
            .getDocuments()
        
        let snapshot2 = try await db.collection("matches")
            .whereField("user2Id", isEqualTo: userId)
            .whereField("isActive", isEqualTo: true)
            .limit(to: limit)
            .getDocuments()
        
        let matches1 = snapshot1.documents.compactMap { try? $0.data(as: Match.self) }
        let matches2 = snapshot2.documents.compactMap { try? $0.data(as: Match.self) }
        
        return (matches1 + matches2).sorted { $0.matchedAt > $1.matchedAt }
    }
    
    /// Get user conversations
    func getUserConversations(userId: String, limit: Int = 50) async throws -> [Conversation] {
        let snapshot1 = try await db.collection("conversations")
            .whereField("participant1Id", isEqualTo: userId)
            .whereField("isActive", isEqualTo: true)
            .order(by: "lastActivity", descending: true)
            .limit(to: limit)
            .getDocuments()
        
        let snapshot2 = try await db.collection("conversations")
            .whereField("participant2Id", isEqualTo: userId)
            .whereField("isActive", isEqualTo: true)
            .order(by: "lastActivity", descending: true)
            .limit(to: limit)
            .getDocuments()
        
        let conversations1 = snapshot1.documents.compactMap { try? $0.data(as: Conversation.self) }
        let conversations2 = snapshot2.documents.compactMap { try? $0.data(as: Conversation.self) }
        
        return (conversations1 + conversations2).sorted { 
            ($0.lastActivity) > ($1.lastActivity) 
        }
    }
}

// MARK: - Errors

enum FirebaseServiceError: LocalizedError {
    case notAuthenticated
    case notImplemented(String)
    case uploadFailed
    case userNotFound
    
    var errorDescription: String? {
        switch self {
        case .notAuthenticated:
            return "Usuário não está autenticado"
        case .notImplemented(let feature):
            return "\(feature) ainda não foi implementado"
        case .uploadFailed:
            return "Falha no upload da imagem"
        case .userNotFound:
            return "Usuário não encontrado"
        }
    }
}