//
//  ProfileFeature.swift
//  FypMatch iOS
//
//  Feature de gerenciamento de perfil do usuário
//  Gerencia visualização, edição e atualização de perfis
//

import Foundation
import ComposableArchitecture
import FirebaseFirestore

/// Feature responsável pelo gerenciamento de perfil do usuário
@Reducer
struct ProfileFeature {
    
    // MARK: - State
    
    @ObservableState
    struct State: Equatable {
        // MARK: - User Data
        var user: User?
        var isCurrentUser: Bool = true
        var userId: String?
        
        // MARK: - Edit Mode
        var isEditMode = false
        var editedUser: User?
        
        // MARK: - Form Fields
        var displayName: String = ""
        var bio: String = ""
        var age: Int = 25
        var occupation: String = ""
        var education: String = ""
        var city: String = ""
        var interests: [String] = []
        var hobbies: [String] = []
        var languages: [String] = []
        
        // MARK: - Photos
        var photos: [String] = []
        var isUploadingPhoto = false
        var uploadProgress: Double = 0.0
        
        // MARK: - Preferences
        var genderInterest: GenderInterest = .all
        var ageRangeMin: Int = 18
        var ageRangeMax: Int = 35
        var maxDistanceKm: Int = 50
        var interestedInSeriousRelationship: Bool = true
        var interestedInCasualDating: Bool = false
        var interestedInFriendship: Bool = false
        
        // MARK: - Personal Info
        var gender: Gender = .notSpecified
        var height: Int? = nil
        var children: ChildrenStatus = .no
        var wantsChildren: Bool? = nil
        var smoking: SmokingStatus = .never
        var drinking: DrinkingStatus = .never
        var religion: String = ""
        var politicalViews: String = ""
        
        // MARK: - UI State
        var isLoading = false
        var isSaving = false
        var error: String?
        var showingImagePicker = false
        var showingInterestsPicker = false
        var showingSuccess = false
        var successMessage: String?
        
        // MARK: - Validation
        var isFormValid: Bool {
            !displayName.isEmpty &&
            !bio.isEmpty &&
            displayName.count >= 2 &&
            bio.count >= 10 &&
            age >= 18 &&
            age <= 100 &&
            !photos.isEmpty
        }
        
        var bioCharacterCount: Int {
            bio.count
        }
        
        var bioCharacterLimit: Int {
            500
        }
        
        var isProfileComplete: Bool {
            guard let user = user else { return false }
            return user.isProfileComplete
        }
        
        var completionPercentage: Double {
            guard let user = user else { return 0.0 }
            var completed: Double = 0
            var total: Double = 10
            
            if !user.displayName.isEmpty { completed += 1 }
            if !user.bio.isEmpty { completed += 1 }
            if !user.photos.isEmpty { completed += 1 }
            if user.age >= 18 { completed += 1 }
            if !user.interests.isEmpty { completed += 1 }
            if user.occupation != nil { completed += 1 }
            if user.education != nil { completed += 1 }
            if user.city != nil { completed += 1 }
            if !user.hobbies.isEmpty { completed += 1 }
            if !user.languages.isEmpty { completed += 1 }
            
            return (completed / total) * 100.0
        }
    }
    
    // MARK: - Action
    
    enum Action: BindableAction, Equatable {
        // MARK: - Binding
        case binding(BindingAction<State>)
        
        // MARK: - Lifecycle
        case onAppear
        case loadProfile(String?)
        case refresh
        
        // MARK: - Edit Mode
        case toggleEditMode
        case cancelEdit
        case saveProfile
        
        // MARK: - Photo Management
        case addPhoto
        case removePhoto(Int)
        case reorderPhotos([String])
        case uploadPhoto(Data)
        case photoUploadProgress(Double)
        
        // MARK: - Interests
        case addInterest(String)
        case removeInterest(String)
        case toggleInterestsPicker
        
        // MARK: - Hobbies
        case addHobby(String)
        case removeHobby(String)
        
        // MARK: - Languages
        case addLanguage(String)
        case removeLanguage(String)
        
        // MARK: - Response Actions
        case profileLoaded(Result<User, Error>)
        case profileSaved(Result<User, Error>)
        case photoUploaded(Result<String, Error>)
        case dismissError
        case dismissSuccess
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
                return .run { [userId = state.userId] send in
                    await send(.loadProfile(userId))
                }
                
            case let .loadProfile(userId):
                state.isLoading = true
                state.error = nil
                
                return .run { send in
                    do {
                        let user: User
                        if let userId = userId {
                            user = try await firebaseService.getUser(userId: userId)
                        } else {
                            guard let currentUser = firebaseService.currentUser else {
                                throw ProfileError.userNotFound
                            }
                            user = currentUser
                        }
                        await send(.profileLoaded(.success(user)))
                    } catch {
                        await send(.profileLoaded(.failure(error)))
                    }
                }
                
            case .refresh:
                return .run { [userId = state.userId] send in
                    await send(.loadProfile(userId))
                }
                
            // MARK: - Edit Mode
                
            case .toggleEditMode:
                state.isEditMode.toggle()
                
                if state.isEditMode {
                    // Populate form with current user data
                    if let user = state.user {
                        state.editedUser = user
                        state.populateFormFields(from: user)
                    }
                } else {
                    state.clearFormFields()
                    state.editedUser = nil
                }
                return .none
                
            case .cancelEdit:
                state.isEditMode = false
                state.clearFormFields()
                state.editedUser = nil
                return .none
                
            case .saveProfile:
                guard state.isFormValid else { return .none }
                guard let user = state.user else { return .none }
                
                state.isSaving = true
                state.error = nil
                
                // Create updated user
                let updatedUser = User(
                    id: user.id,
                    email: user.email,
                    displayName: state.displayName,
                    age: state.age,
                    bio: state.bio,
                    photos: state.photos,
                    profileImageURL: state.photos.first,
                    isPhotoVerified: user.isPhotoVerified,
                    latitude: user.latitude,
                    longitude: user.longitude,
                    city: state.city.isEmpty ? user.city : state.city,
                    state: user.state,
                    country: user.country,
                    maxDistanceKm: state.maxDistanceKm,
                    genderInterest: state.genderInterest,
                    ageRangeMin: state.ageRangeMin,
                    ageRangeMax: state.ageRangeMax,
                    interestedInSeriousRelationship: state.interestedInSeriousRelationship,
                    interestedInCasualDating: state.interestedInCasualDating,
                    interestedInFriendship: state.interestedInFriendship,
                    gender: state.gender,
                    height: state.height,
                    occupation: state.occupation.isEmpty ? user.occupation : state.occupation,
                    education: state.education.isEmpty ? user.education : state.education,
                    children: state.children,
                    wantsChildren: state.wantsChildren,
                    smoking: state.smoking,
                    drinking: state.drinking,
                    religion: state.religion.isEmpty ? user.religion : state.religion,
                    politicalViews: state.politicalViews.isEmpty ? user.politicalViews : state.politicalViews,
                    interests: state.interests,
                    hobbies: state.hobbies,
                    languages: state.languages,
                    traveledCountries: user.traveledCountries,
                    favoriteActivities: user.favoriteActivities,
                    isPremium: user.isPremium,
                    premiumExpiresAt: user.premiumExpiresAt,
                    isVerified: user.isVerified,
                    isOnline: user.isOnline,
                    lastActiveAt: Date(),
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
                    updatedAt: Date(),
                    deviceToken: user.deviceToken,
                    isEmailVerified: user.isEmailVerified,
                    isPhoneVerified: user.isPhoneVerified,
                    phoneNumber: user.phoneNumber
                )
                
                return .run { send in
                    do {
                        try await firebaseService.updateUser(updatedUser)
                        await send(.profileSaved(.success(updatedUser)))
                    } catch {
                        await send(.profileSaved(.failure(error)))
                    }
                }
                
            // MARK: - Photo Management
                
            case .addPhoto:
                state.showingImagePicker = true
                return .none
                
            case let .removePhoto(index):
                guard index < state.photos.count else { return .none }
                state.photos.remove(at: index)
                return .none
                
            case let .reorderPhotos(photos):
                state.photos = photos
                return .none
                
            case let .uploadPhoto(imageData):
                state.isUploadingPhoto = true
                state.uploadProgress = 0.0
                
                return .run { [userId = state.user?.id] send in
                    guard let userId = userId else { return }
                    
                    do {
                        // Simulate upload progress
                        for progress in stride(from: 0.0, through: 0.9, by: 0.1) {
                            await send(.photoUploadProgress(progress))
                            try await Task.sleep(nanoseconds: 100_000_000)
                        }
                        
                        let url = try await firebaseService.uploadImage(imageData, userId: userId)
                        await send(.photoUploaded(.success(url)))
                    } catch {
                        await send(.photoUploaded(.failure(error)))
                    }
                }
                
            case let .photoUploadProgress(progress):
                state.uploadProgress = progress
                return .none
                
            // MARK: - Interests
                
            case let .addInterest(interest):
                if !state.interests.contains(interest) {
                    state.interests.append(interest)
                }
                return .none
                
            case let .removeInterest(interest):
                state.interests.removeAll { $0 == interest }
                return .none
                
            case .toggleInterestsPicker:
                state.showingInterestsPicker.toggle()
                return .none
                
            // MARK: - Hobbies
                
            case let .addHobby(hobby):
                if !state.hobbies.contains(hobby) {
                    state.hobbies.append(hobby)
                }
                return .none
                
            case let .removeHobby(hobby):
                state.hobbies.removeAll { $0 == hobby }
                return .none
                
            // MARK: - Languages
                
            case let .addLanguage(language):
                if !state.languages.contains(language) {
                    state.languages.append(language)
                }
                return .none
                
            case let .removeLanguage(language):
                state.languages.removeAll { $0 == language }
                return .none
                
            // MARK: - Response Handling
                
            case let .profileLoaded(.success(user)):
                state.isLoading = false
                state.user = user
                state.error = nil
                
                if state.userId == nil || state.userId == user.id {
                    state.isCurrentUser = true
                }
                
                return .none
                
            case let .profileLoaded(.failure(error)):
                state.isLoading = false
                state.error = error.localizedDescription
                return .none
                
            case let .profileSaved(.success(user)):
                state.isSaving = false
                state.user = user
                state.isEditMode = false
                state.editedUser = nil
                state.clearFormFields()
                state.successMessage = "Perfil atualizado com sucesso!"
                state.showingSuccess = true
                return .none
                
            case let .profileSaved(.failure(error)):
                state.isSaving = false
                state.error = error.localizedDescription
                return .none
                
            case let .photoUploaded(.success(url)):
                state.isUploadingPhoto = false
                state.uploadProgress = 1.0
                state.photos.append(url)
                state.showingImagePicker = false
                return .none
                
            case let .photoUploaded(.failure(error)):
                state.isUploadingPhoto = false
                state.uploadProgress = 0.0
                state.error = error.localizedDescription
                return .none
                
            case .dismissError:
                state.error = nil
                return .none
                
            case .dismissSuccess:
                state.showingSuccess = false
                state.successMessage = nil
                return .none
                
            // MARK: - Binding
                
            case .binding:
                return .none
            }
        }
    }
}

// MARK: - State Extensions

extension ProfileFeature.State {
    mutating func populateFormFields(from user: User) {
        self.displayName = user.displayName
        self.bio = user.bio
        self.age = user.age
        self.occupation = user.occupation ?? ""
        self.education = user.education ?? ""
        self.city = user.city ?? ""
        self.interests = user.interests
        self.hobbies = user.hobbies
        self.languages = user.languages
        self.photos = user.photos
        self.genderInterest = user.genderInterest
        self.ageRangeMin = user.ageRangeMin
        self.ageRangeMax = user.ageRangeMax
        self.maxDistanceKm = user.maxDistanceKm
        self.interestedInSeriousRelationship = user.interestedInSeriousRelationship
        self.interestedInCasualDating = user.interestedInCasualDating
        self.interestedInFriendship = user.interestedInFriendship
        self.gender = user.gender
        self.height = user.height
        self.children = user.children
        self.wantsChildren = user.wantsChildren
        self.smoking = user.smoking
        self.drinking = user.drinking
        self.religion = user.religion ?? ""
        self.politicalViews = user.politicalViews ?? ""
    }
    
    mutating func clearFormFields() {
        self.displayName = ""
        self.bio = ""
        self.age = 25
        self.occupation = ""
        self.education = ""
        self.city = ""
        self.interests = []
        self.hobbies = []
        self.languages = []
        self.photos = []
        self.religion = ""
        self.politicalViews = ""
    }
}

// MARK: - Errors

enum ProfileError: Error, LocalizedError {
    case userNotFound
    case invalidData
    case uploadFailed
    case saveFailed
    
    var errorDescription: String? {
        switch self {
        case .userNotFound:
            return "Usuário não encontrado"
        case .invalidData:
            return "Dados inválidos"
        case .uploadFailed:
            return "Falha ao fazer upload"
        case .saveFailed:
            return "Falha ao salvar perfil"
        }
    }
}

// MARK: - Firebase Service Extension

extension FirebaseService {
    /// Get user by ID
    func getUser(userId: String) async throws -> User {
        let document = try await db.collection("users").document(userId).getDocument()
        
        guard document.exists, let data = document.data() else {
            throw ProfileError.userNotFound
        }
        
        return try Firestore.Decoder().decode(User.self, from: data)
    }
    
    /// Update user profile
    func updateUser(_ user: User) async throws {
        let encoder = Firestore.Encoder()
        let data = try encoder.encode(user)
        try await db.collection("users").document(user.id).setData(data, merge: true)
        
        // Update local current user if it's the same
        if currentUser?.id == user.id {
            await MainActor.run {
                self.currentUser = user
            }
        }
    }
    
    /// Upload image to Firebase Storage
    func uploadImage(_ imageData: Data, userId: String) async throws -> String {
        let imageId = UUID().uuidString
        let path = "users/\(userId)/photos/\(imageId).jpg"
        let storageRef = storage.reference().child(path)
        
        _ = try await storageRef.putDataAsync(imageData)
        let downloadURL = try await storageRef.downloadURL()
        
        return downloadURL.absoluteString
    }
}
