// DiscoveryFeatureTests.swift — FypMatch iOS
// Testes unitários para o DiscoveryFeature usando TCA TestStore

import XCTest
import ComposableArchitecture
import CoreLocation
@testable import FypMatch

@MainActor
final class DiscoveryFeatureTests: XCTestCase {

    // MARK: - Setup

    override func setUp() {
        super.setUp()
    }

    // MARK: - Load Discovery Users

    func testLoadDiscoveryUsersSuccess() async {
        let mockUsers = [User.mock(), User.mock(id: "user2"), User.mock(id: "user3")]

        let store = TestStore(initialState: DiscoveryFeature.State()) {
            DiscoveryFeature()
        } withDependencies: {
            $0.firebaseService = .init(
                fetchDiscoveryUsers: { _ in mockUsers },
                currentUser: { nil }
            )
            $0.locationService = .init(
                requestPermission: { .authorizedWhenInUse },
                getCurrentLocation: { nil }
            )
            $0.matchingService = .init(
                processSwipe: { _, _, _ in nil }
            )
        }

        await store.send(.loadDiscoveryUsers) {
            $0.isLoadingUsers = true
            $0.loadingError = nil
        }

        await store.receive(.discoveryUsersLoaded(.success(mockUsers))) {
            $0.isLoadingUsers = false
            $0.discoveryUsers = mockUsers
        }
    }

    func testLoadDiscoveryUsersFailure() async {
        struct TestError: Error, LocalizedError {
            var errorDescription: String? { "Erro de teste" }
        }

        let store = TestStore(initialState: DiscoveryFeature.State()) {
            DiscoveryFeature()
        } withDependencies: {
            $0.firebaseService = .init(
                fetchDiscoveryUsers: { _ in throw TestError() },
                currentUser: { nil }
            )
            $0.locationService = .init(
                requestPermission: { .denied },
                getCurrentLocation: { nil }
            )
            $0.matchingService = .init(
                processSwipe: { _, _, _ in nil }
            )
        }

        await store.send(.loadDiscoveryUsers) {
            $0.isLoadingUsers = true
        }

        await store.receive(.discoveryUsersLoaded(.failure(TestError()))) {
            $0.isLoadingUsers = false
            $0.loadingError = "Erro de teste"
        }
    }

    // MARK: - Swipe Actions

    func testSwipeLimitReached() async {
        var state = DiscoveryFeature.State()
        state.swipeCount = 50 // Limite atingido
        state.dailySwipeLimit = 50

        let store = TestStore(initialState: state) {
            DiscoveryFeature()
        } withDependencies: {
            $0.firebaseService = .init(
                fetchDiscoveryUsers: { _ in [] },
                currentUser: { nil }
            )
            $0.locationService = .init(
                requestPermission: { .notDetermined },
                getCurrentLocation: { nil }
            )
            $0.matchingService = .init(
                processSwipe: { _, _, _ in nil }
            )
        }

        let mockUser = User.mock()
        await store.send(.swipeUser(mockUser, .like)) {
            $0.showingPremiumUpsell = true
            $0.upsellReason = .swipeLimitReached
        }
    }

    func testDismissPremiumUpsell() async {
        var state = DiscoveryFeature.State()
        state.showingPremiumUpsell = true
        state.upsellReason = .swipeLimitReached

        let store = TestStore(initialState: state) {
            DiscoveryFeature()
        } withDependencies: {
            $0.firebaseService = .init(
                fetchDiscoveryUsers: { _ in [] },
                currentUser: { nil }
            )
            $0.locationService = .init(
                requestPermission: { .notDetermined },
                getCurrentLocation: { nil }
            )
            $0.matchingService = .init(
                processSwipe: { _, _, _ in nil }
            )
        }

        await store.send(.dismissPremiumUpsell) {
            $0.showingPremiumUpsell = false
            $0.upsellReason = nil
        }
    }

    // MARK: - Filters

    func testShowFilters() async {
        let store = TestStore(initialState: DiscoveryFeature.State()) {
            DiscoveryFeature()
        } withDependencies: {
            $0.firebaseService = .init(
                fetchDiscoveryUsers: { _ in [] },
                currentUser: { nil }
            )
            $0.locationService = .init(
                requestPermission: { .notDetermined },
                getCurrentLocation: { nil }
            )
            $0.matchingService = .init(
                processSwipe: { _, _, _ in nil }
            )
        }

        await store.send(.showFilters(true)) {
            $0.isShowingFilters = true
        }

        await store.send(.showFilters(false)) {
            $0.isShowingFilters = false
        }
    }

    func testResetFilters() async {
        var state = DiscoveryFeature.State()
        state.tempAgeMin = 30
        state.tempAgeMax = 40
        state.tempMaxDistance = 100

        let store = TestStore(initialState: state) {
            DiscoveryFeature()
        } withDependencies: {
            $0.firebaseService = .init(
                fetchDiscoveryUsers: { _ in [] },
                currentUser: { nil }
            )
            $0.locationService = .init(
                requestPermission: { .notDetermined },
                getCurrentLocation: { nil }
            )
            $0.matchingService = .init(
                processSwipe: { _, _, _ in nil }
            )
        }

        await store.send(.resetFilters) {
            $0.tempAgeMin = 18
            $0.tempAgeMax = 35
            $0.tempMaxDistance = 50
            $0.tempGenderInterest = .all
        }
    }

    // MARK: - Match Animation

    func testDismissMatchAnimation() async {
        var state = DiscoveryFeature.State()
        state.showingMatchAnimation = true
        state.newMatch = Match.mock()

        let store = TestStore(initialState: state) {
            DiscoveryFeature()
        } withDependencies: {
            $0.firebaseService = .init(
                fetchDiscoveryUsers: { _ in [] },
                currentUser: { nil }
            )
            $0.locationService = .init(
                requestPermission: { .notDetermined },
                getCurrentLocation: { nil }
            )
            $0.matchingService = .init(
                processSwipe: { _, _, _ in nil }
            )
            $0.mainQueue = .immediate
        }

        await store.send(.dismissMatchAnimation) {
            $0.showingMatchAnimation = false
        }
        // dismissMatchAnimation sends swipeCompleted(.like)
        await store.receive(.swipeCompleted(.like)) {
            $0.swipeCount = 1
            $0.currentUserIndex = 1
        }
    }

    // MARK: - Premium State

    func testPremiumUpsellReasons() {
        let reasons: [PremiumUpsellReason] = [
            .swipeLimitReached, .undoSwipe, .superLike, .seeWhoLikesYou, .boost, .premiumFilters
        ]
        for reason in reasons {
            XCTAssertFalse(reason.title.isEmpty, "Título vazio para \(reason)")
            XCTAssertFalse(reason.message.isEmpty, "Mensagem vazia para \(reason)")
        }
    }
}

// MARK: - AuthFeature Tests

@MainActor
final class AuthFeatureTests: XCTestCase {

    func testInitialState() {
        let state = AuthFeature.State()
        XCTAssertFalse(state.isAuthenticated)
        XCTAssertNil(state.currentUser)
        XCTAssertFalse(state.isLoading)
        XCTAssertNil(state.errorMessage)
    }

    func testSignInSuccess() async {
        let mockUser = User.mock()

        let store = TestStore(initialState: AuthFeature.State()) {
            AuthFeature()
        } withDependencies: {
            $0.firebaseService = .init(
                signIn: { _, _ in },
                currentUser: { mockUser },
                isAuthenticated: { true }
            )
        }

        await store.send(.signIn(email: "test@test.com", password: "123456")) {
            $0.isLoading = true
            $0.errorMessage = nil
        }

        await store.receive(.authStateChanged(true, mockUser)) {
            $0.isLoading = false
            $0.isAuthenticated = true
            $0.currentUser = mockUser
        }
    }

    func testSignOutClearsState() async {
        var state = AuthFeature.State()
        state.isAuthenticated = true
        state.currentUser = User.mock()

        let store = TestStore(initialState: state) {
            AuthFeature()
        } withDependencies: {
            $0.firebaseService = .init(
                signOut: {},
                currentUser: { nil },
                isAuthenticated: { false }
            )
        }

        await store.send(.signOut) {
            $0.isAuthenticated = false
            $0.currentUser = nil
        }
    }
}

// MARK: - Model Tests

final class UserModelTests: XCTestCase {

    func testUserPrimaryPhotoURL() {
        let user = User.mock(photos: ["https://example.com/photo1.jpg", "https://example.com/photo2.jpg"])
        XCTAssertEqual(user.primaryPhotoURL, "https://example.com/photo1.jpg")
    }

    func testUserPrimaryPhotoURLFallsBackToProfileImage() {
        let user = User.mock(photos: [], profileImageURL: "https://example.com/profile.jpg")
        XCTAssertEqual(user.primaryPhotoURL, "https://example.com/profile.jpg")
    }

    func testUserIsActivePremiumExpired() {
        let expiredDate = Calendar.current.date(byAdding: .day, value: -1, to: Date())!
        let user = User.mock(isPremium: true, premiumExpiresAt: expiredDate)
        XCTAssertFalse(user.isActivePremium)
    }

    func testUserIsActivePremiumValid() {
        let futureDate = Calendar.current.date(byAdding: .day, value: 30, to: Date())!
        let user = User.mock(isPremium: true, premiumExpiresAt: futureDate)
        XCTAssertTrue(user.isActivePremium)
    }

    func testUserDisplayNameFormatted() {
        let user = User.mock(displayName: "João Silva", age: 28)
        XCTAssertEqual(user.displayNameWithAge, "João, 28")
    }
}

final class MatchModelTests: XCTestCase {

    func testGetOtherUser() {
        let user1 = User.mock(id: "user1")
        let user2 = User.mock(id: "user2")
        let match = Match(
            user1Id: "user1",
            user2Id: "user2",
            user1: user1,
            user2: user2,
            user1SwipedAt: Date(),
            user2SwipedAt: Date()
        )

        XCTAssertEqual(match.getOtherUser(currentUserId: "user1")?.id, "user2")
        XCTAssertEqual(match.getOtherUser(currentUserId: "user2")?.id, "user1")
        XCTAssertNil(match.getOtherUser(currentUserId: "user3"))
    }

    func testMatchIsNew() {
        let match = Match(
            user1Id: "u1",
            user2Id: "u2",
            matchedAt: Date(),
            user1SwipedAt: Date(),
            user2SwipedAt: Date()
        )
        XCTAssertTrue(match.isNewMatch)
    }

    func testMatchTimeAgo() {
        let oldDate = Calendar.current.date(byAdding: .minute, value: -30, to: Date())!
        let match = Match(
            user1Id: "u1",
            user2Id: "u2",
            matchedAt: oldDate,
            user1SwipedAt: oldDate,
            user2SwipedAt: oldDate
        )
        XCTAssertEqual(match.timeAgoString, "30min")
    }
}

final class MessageModelTests: XCTestCase {

    func testMessageDisplayContent() {
        let textMsg = Message(
            conversationId: "conv1",
            senderId: "u1",
            receiverId: "u2",
            content: "Oi!"
        )
        XCTAssertEqual(textMsg.displayContent, "Oi!")

        let imageMsg = Message(
            conversationId: "conv1",
            senderId: "u1",
            receiverId: "u2",
            content: "url",
            type: .image
        )
        XCTAssertEqual(imageMsg.displayContent, "📷 Imagem")
    }

    func testMessageIsFromUser() {
        let msg = Message(
            conversationId: "conv1",
            senderId: "user1",
            receiverId: "user2",
            content: "Teste"
        )
        XCTAssertTrue(msg.isSentByUser("user1"))
        XCTAssertFalse(msg.isSentByUser("user2"))
    }
}

// MARK: - Mock Factories

extension User {
    static func mock(
        id: String = UUID().uuidString,
        displayName: String = "Usuário Teste",
        age: Int = 25,
        photos: [String] = [],
        profileImageURL: String? = nil,
        isPremium: Bool = false,
        premiumExpiresAt: Date? = nil
    ) -> User {
        User(
            id: id,
            email: "\(id)@test.com",
            displayName: displayName,
            age: age,
            bio: "Bio de teste",
            photos: photos,
            profileImageURL: profileImageURL,
            isPremium: isPremium,
            premiumExpiresAt: premiumExpiresAt
        )
    }
}

extension Match {
    static func mock(id: String = UUID().uuidString) -> Match {
        Match(
            id: id,
            user1Id: "user1",
            user2Id: "user2",
            matchedAt: Date(),
            user1SwipedAt: Date(),
            user2SwipedAt: Date(),
            compatibilityScore: 85.0
        )
    }
}
