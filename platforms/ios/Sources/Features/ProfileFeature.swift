// ProfileFeature.swift — FypMatch iOS
// TCA Feature para edição e visualização de perfil

import Foundation
import ComposableArchitecture
import PhotosUI
import SwiftUI

@Reducer
struct ProfileFeature {

    // MARK: - State

    @ObservableState
    struct State: Equatable {
        var user: User?
        var isLoading = false
        var isSaving = false
        var saveError: String?
        var saveSuccess = false
        var isEditing = false

        // Edit fields
        var editBio = ""
        var editName = ""
        var editOccupation = ""
        var editAge = 25

        // Photos
        var selectedPhotoItem: PhotosPickerItem? = nil
        var isUploadingPhoto = false
        var uploadProgress: Double = 0.0

        // Interests editor
        var availableInterests: [String] = [
            "Música", "Viagem", "Esportes", "Cinema", "Gastronomia",
            "Leitura", "Arte", "Fotografia", "Dança", "Yoga",
            "Tecnologia", "Games", "Pets", "Culinária", "Natureza",
            "Teatro", "Meditação", "Crossfit", "Surf", "Ciclismo"
        ]
        var selectedInterests: Set<String> = []

        // Computed
        var hasUnsavedChanges: Bool {
            guard let user = user else { return false }
            return editBio != (user.bio ?? "")
                || editName != user.displayName
                || editOccupation != (user.occupation ?? "")
                || selectedInterests != Set(user.interests)
        }
    }

    // MARK: - Action

    enum Action: BindableAction, Equatable {
        case binding(BindingAction<State>)
        case onAppear
        case startEditing
        case cancelEditing
        case saveProfile
        case profileSaved(Result<Void, Error>)
        case photoSelected(PhotosPickerItem?)
        case photoUploaded(Result<String, Error>)
        case removePhoto(String)
        case reorderPhotos([String])
        case toggleInterest(String)
        case deleteAccount
        case signOut
    }

    // MARK: - Dependencies

    @Dependency(\.firebaseService) var firebaseService

    // MARK: - Body

    var body: some ReducerOf<Self> {
        BindingReducer()
        Reduce { state, action in
            switch action {

            case .onAppear:
                state.user = firebaseService.currentUser
                if let user = state.user {
                    state.editBio = user.bio ?? ""
                    state.editName = user.displayName
                    state.editOccupation = user.occupation ?? ""
                    state.editAge = user.age
                    state.selectedInterests = Set(user.interests)
                }
                return .none

            case .startEditing:
                state.isEditing = true
                return .none

            case .cancelEditing:
                state.isEditing = false
                // Restaurar valores originais
                if let user = state.user {
                    state.editBio = user.bio ?? ""
                    state.editName = user.displayName
                    state.editOccupation = user.occupation ?? ""
                    state.selectedInterests = Set(user.interests)
                }
                return .none

            case .saveProfile:
                guard var user = state.user else { return .none }
                state.isSaving = true
                state.saveError = nil

                user = User(
                    id: user.id,
                    email: user.email,
                    displayName: state.editName.trimmingCharacters(in: .whitespaces),
                    age: user.age,
                    bio: state.editBio.trimmingCharacters(in: .whitespaces),
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
                    occupation: state.editOccupation.trimmingCharacters(in: .whitespaces),
                    education: user.education,
                    children: user.children,
                    wantsChildren: user.wantsChildren,
                    smoking: user.smoking,
                    drinking: user.drinking,
                    religion: user.religion,
                    politicalViews: user.politicalViews,
                    interests: Array(state.selectedInterests).sorted(),
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
                    updatedAt: Date(),
                    deviceToken: user.deviceToken,
                    isEmailVerified: user.isEmailVerified,
                    isPhoneVerified: user.isPhoneVerified,
                    phoneNumber: user.phoneNumber
                )
                let updatedUser = user
                return .run { send in
                    do {
                        try await firebaseService.saveUser(updatedUser)
                        await send(.profileSaved(.success(())))
                    } catch {
                        await send(.profileSaved(.failure(error)))
                    }
                }

            case .profileSaved(.success):
                state.isSaving = false
                state.saveSuccess = true
                state.isEditing = false
                state.user = firebaseService.currentUser
                return .none

            case let .profileSaved(.failure(error)):
                state.isSaving = false
                state.saveError = error.localizedDescription
                return .none

            case let .photoSelected(item):
                state.selectedPhotoItem = item
                guard let item else { return .none }
                state.isUploadingPhoto = true

                return .run { [userId = state.user?.id ?? ""] send in
                    do {
                        guard let data = try await item.loadTransferable(type: Data.self) else {
                            throw FirebaseServiceError.uploadFailed
                        }
                        let url = try await firebaseService.uploadPhoto(data, userId: userId)
                        await send(.photoUploaded(.success(url)))
                    } catch {
                        await send(.photoUploaded(.failure(error)))
                    }
                }

            case let .photoUploaded(.success(url)):
                state.isUploadingPhoto = false
                state.selectedPhotoItem = nil
                if var user = state.user {
                    var photos = user.photos
                    photos.append(url)
                    state.user = User(
                        id: user.id, email: user.email,
                        displayName: user.displayName, age: user.age,
                        bio: user.bio, photos: photos,
                        profileImageURL: user.profileImageURL ?? url,
                        isPhotoVerified: user.isPhotoVerified,
                        latitude: user.latitude, longitude: user.longitude,
                        city: user.city, state: user.state, country: user.country,
                        maxDistanceKm: user.maxDistanceKm,
                        genderInterest: user.genderInterest,
                        ageRangeMin: user.ageRangeMin, ageRangeMax: user.ageRangeMax,
                        interestedInSeriousRelationship: user.interestedInSeriousRelationship,
                        interestedInCasualDating: user.interestedInCasualDating,
                        interestedInFriendship: user.interestedInFriendship,
                        gender: user.gender, height: user.height, occupation: user.occupation,
                        education: user.education, children: user.children,
                        wantsChildren: user.wantsChildren, smoking: user.smoking,
                        drinking: user.drinking, religion: user.religion,
                        politicalViews: user.politicalViews,
                        interests: user.interests, hobbies: user.hobbies,
                        languages: user.languages, traveledCountries: user.traveledCountries,
                        favoriteActivities: user.favoriteActivities,
                        isPremium: user.isPremium, premiumExpiresAt: user.premiumExpiresAt,
                        isVerified: user.isVerified, isOnline: user.isOnline,
                        lastActiveAt: user.lastActiveAt,
                        swipeCount: user.swipeCount, matchCount: user.matchCount,
                        messageCount: user.messageCount, profileViews: user.profileViews,
                        likes: user.likes, passes: user.passes,
                        pushNotificationsEnabled: user.pushNotificationsEnabled,
                        showOnlineStatus: user.showOnlineStatus,
                        showDistanceInProfile: user.showDistanceInProfile,
                        allowMessageFromMatches: user.allowMessageFromMatches,
                        createdAt: user.createdAt, updatedAt: Date(),
                        deviceToken: user.deviceToken,
                        isEmailVerified: user.isEmailVerified,
                        isPhoneVerified: user.isPhoneVerified,
                        phoneNumber: user.phoneNumber
                    )
                }
                return .none

            case .photoUploaded(.failure):
                state.isUploadingPhoto = false
                return .none

            case .removePhoto:
                return .send(.saveProfile)

            case .reorderPhotos:
                return .send(.saveProfile)

            case let .toggleInterest(interest):
                if state.selectedInterests.contains(interest) {
                    state.selectedInterests.remove(interest)
                } else if state.selectedInterests.count < 10 {
                    state.selectedInterests.insert(interest)
                }
                return .none

            case .deleteAccount:
                return .none // Implementar com confirmação na View

            case .signOut:
                try? firebaseService.signOut()
                return .none

            case .binding:
                return .none
            }
        }
    }
}
