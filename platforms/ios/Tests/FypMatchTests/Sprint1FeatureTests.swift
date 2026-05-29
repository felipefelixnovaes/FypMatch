// Sprint1FeatureTests.swift — FypMatch iOS
// Testes unitários para as features do Sprint 1

import XCTest
import ComposableArchitecture
@testable import FypMatch

// MARK: - AccessCodeFeature Tests

@MainActor
final class AccessCodeFeatureTests: XCTestCase {

    func testSubmitValidCode() async {
        let store = TestStore(
            initialState: AccessCodeFeature.State()
        ) {
            AccessCodeFeature()
        } withDependencies: {
            $0.accessCodeService = .init(
                validateCode: { _ in
                    AccessCode(
                        id: "test",
                        code: "FYPM-PREM-2024",
                        type: .premium,
                        expiresAt: Calendar.current.date(byAdding: .day, value: 30, to: Date())!,
                        isUsed: false,
                        createdAt: Date()
                    )
                },
                redeemCode: { _, _ in .premium }
            )
            $0.firebaseService = .init(currentUser: { User.mock() })
        }

        await store.send(.binding(.set(\.code, "FYPM-PREM-2024"))) {
            $0.code = "FYPM-PREM-2024"
        }

        await store.send(.submitCode) {
            $0.isLoading = true
            $0.errorMessage = nil
        }

        await store.receive(.codeValidated(.success(.premium))) {
            $0.isLoading = false
            $0.isSuccess = true
            $0.redeemedType = .premium
        }
    }

    func testSubmitInvalidCode() async {
        struct InvalidCodeError: Error, LocalizedError {
            var errorDescription: String? { "Código inválido ou expirado" }
        }

        let store = TestStore(
            initialState: AccessCodeFeature.State()
        ) {
            AccessCodeFeature()
        } withDependencies: {
            $0.accessCodeService = .init(
                validateCode: { _ in throw InvalidCodeError() },
                redeemCode: { _, _ in throw InvalidCodeError() }
            )
            $0.firebaseService = .init(currentUser: { nil })
        }

        await store.send(.binding(.set(\.code, "CODIGO-ERRADO"))) {
            $0.code = "CODIGO-ERRADO"
        }

        await store.send(.submitCode) {
            $0.isLoading = true
        }

        await store.receive(.codeValidated(.failure(InvalidCodeError()))) {
            $0.isLoading = false
            $0.errorMessage = "Código inválido ou expirado"
        }
    }

    func testEmptyCodeCannotSubmit() {
        let state = AccessCodeFeature.State()
        XCTAssertFalse(state.canSubmit, "Código vazio não deve permitir submit")
    }

    func testSkipToWaitlist() async {
        let store = TestStore(
            initialState: AccessCodeFeature.State()
        ) {
            AccessCodeFeature()
        } withDependencies: {
            $0.accessCodeService = .init(
                validateCode: { _ in fatalError("não deve ser chamado") },
                redeemCode: { _, _ in fatalError("não deve ser chamado") }
            )
            $0.firebaseService = .init(currentUser: { nil })
        }

        await store.send(.skipToWaitlist) {
            $0.showingWaitlist = true
        }
    }

    func testAccessCodeTypeDisplayNames() {
        XCTAssertEqual(AccessCodeType.basic.displayName, "Básico")
        XCTAssertEqual(AccessCodeType.premium.displayName, "Premium")
        XCTAssertEqual(AccessCodeType.vip.displayName, "VIP")
    }

    func testCodeNormalization() {
        var state = AccessCodeFeature.State()
        state.code = "  fypm-beta-2024  "
        XCTAssertEqual(state.normalizedCode, "FYPM-BETA-2024")
    }
}

// MARK: - AnalyticsService Tests

final class AnalyticsServiceTests: XCTestCase {

    func testAnalyticsEventNames() {
        // Verificar que todos os eventos têm nomes ≤ 40 chars (limite Firebase)
        let events: [FypAnalyticsEvent] = [
            .swipeAction(targetUserId: "u1", action: "like"),
            .matchOccurred(matchId: "m1", compatibilityScore: 85.0),
            .messageSent(conversationId: "c1"),
            .premiumUpgradeAttempt(plan: "premium", screen: "discovery"),
            .premiumUpgradeComplete(plan: "vip", revenue: 39.90),
            .profileView(viewedUserId: "u2"),
            .appOpen(isReturning: true),
            .onboardingComplete,
            .accessCodeRedeemed(codeType: "PREMIUM"),
            .aiCounselorUsed(creditsUsed: 3),
            .adsWatched(creditsGranted: 3),
            .waitlistJoined(city: "São Paulo"),
            .photoUploaded(photoCount: 3),
            .screenView(screenName: "discovery"),
            .errorOccurred(errorType: "auth", details: "timeout")
        ]

        for event in events {
            XCTAssertLessThanOrEqual(
                event.eventName.count,
                40,
                "Evento '\(event.eventName)' tem \(event.eventName.count) chars (limite: 40)"
            )
        }
    }

    func testUserPropertyRawValues() {
        // Verificar que os valores raw são snake_case válidos para Firebase
        XCTAssertEqual(UserProperty.isPremium.rawValue, "is_premium")
        XCTAssertEqual(UserProperty.planType.rawValue, "plan_type")
        XCTAssertEqual(UserProperty.city.rawValue, "city")
        XCTAssertEqual(UserProperty.ageGroup.rawValue, "age_group")
    }
}

// MARK: - WaitlistFormView ViewModel Tests

final class WaitlistFormTests: XCTestCase {

    func testValidationRequiresName() {
        let form = WaitlistFormData(
            name: "",
            email: "test@test.com",
            city: "São Paulo",
            state: "SP"
        )
        XCTAssertFalse(form.isValid)
        XCTAssertTrue(form.nameError != nil)
    }

    func testValidationRequiresValidEmail() {
        let form = WaitlistFormData(
            name: "João",
            email: "email-invalido",
            city: "São Paulo",
            state: "SP"
        )
        XCTAssertFalse(form.isValid)
        XCTAssertTrue(form.emailError != nil)
    }

    func testValidationRequiresCity() {
        let form = WaitlistFormData(
            name: "João",
            email: "joao@email.com",
            city: "",
            state: "SP"
        )
        XCTAssertFalse(form.isValid)
    }

    func testValidFormPassesAllValidations() {
        let form = WaitlistFormData(
            name: "João Silva",
            email: "joao@email.com",
            city: "São Paulo",
            state: "SP",
            age: 28,
            gender: "Homem",
            intentions: ["Relacionamento sério"]
        )
        XCTAssertTrue(form.isValid)
        XCTAssertNil(form.nameError)
        XCTAssertNil(form.emailError)
    }

    func testBrazilianStatesCount() {
        XCTAssertEqual(BrazilianStates.all.count, 27, "Brasil tem 26 estados + DF = 27")
    }
}

// MARK: - Android Sprint 1 — Kotlin tests estão em:
// app/src/test/java/com/ideiassertiva/FypMatch/

// MARK: - Mock Helpers

struct WaitlistFormData {
    var name: String
    var email: String
    var city: String
    var state: String
    var age: Int = 25
    var gender: String = "Prefiro não dizer"
    var intentions: [String] = []
    var referralCode: String = ""

    var isValid: Bool {
        nameError == nil && emailError == nil && !city.isEmpty && !state.isEmpty
    }

    var nameError: String? {
        name.trimmingCharacters(in: .whitespaces).isEmpty ? "Nome é obrigatório" : nil
    }

    var emailError: String? {
        let emailRegex = #"^[^@\s]+@[^@\s]+\.[^@\s]+$"#
        return email.range(of: emailRegex, options: .regularExpression) == nil
            ? "Email inválido" : nil
    }
}

enum BrazilianStates {
    static let all = ["AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO",
                      "MA", "MG", "MS", "MT", "PA", "PB", "PE", "PI", "PR",
                      "RJ", "RN", "RO", "RR", "RS", "SC", "SE", "SP", "TO"]
}
