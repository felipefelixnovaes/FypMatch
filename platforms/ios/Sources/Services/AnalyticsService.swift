// AnalyticsService.swift — FypMatch iOS
// Wrapper Firebase Analytics com eventos customizados

import Foundation
import FirebaseAnalytics
import ComposableArchitecture

// MARK: - FypAnalyticsEvent

/// Todos os eventos rastreados no FypMatch
enum FypAnalyticsEvent {
    case swipeAction(targetUserId: String, action: String)
    case matchOccurred(matchId: String, compatibilityScore: Double)
    case messageSent(conversationId: String)
    case premiumUpgradeAttempt(plan: String, screen: String)
    case premiumUpgradeComplete(plan: String, revenue: Double)
    case profileView(viewedUserId: String)
    case appOpen(isReturning: Bool)
    case onboardingComplete
    case accessCodeRedeemed(codeType: String)
    case aiCounselorUsed(creditsUsed: Int)
    case adsWatched(creditsGranted: Int)
    case waitlistJoined(city: String)
    case photoUploaded(photoCount: Int)
    case screenView(screenName: String)
    case errorOccurred(errorType: String, details: String)
}

// MARK: - UserProperty

/// Propriedades de usuário para segmentação no Firebase
enum UserProperty: String {
    case isPremium  = "is_premium"
    case planType   = "plan_type"
    case gender     = "gender"
    case city       = "city"
    case ageGroup   = "age_group"   // "18-24", "25-34", "35-44", "45+"
}

// MARK: - AnalyticsService

@MainActor
final class AnalyticsService {

    static let shared = AnalyticsService()

    private init() {}

    // MARK: - track

    /// Registra um evento Firebase Analytics com os parâmetros corretos.
    /// Nomes de eventos em snake_case, máximo 40 caracteres conforme exigido pelo Firebase.
    func track(_ event: FypAnalyticsEvent) {
        switch event {

        case let .swipeAction(targetUserId, action):
            Analytics.logEvent("swipe_action", parameters: [
                "target_user_id": targetUserId,
                "action": action                  // "like", "dislike", "super_like"
            ])

        case let .matchOccurred(matchId, compatibilityScore):
            Analytics.logEvent("match_occurred", parameters: [
                "match_id": matchId,
                "compatibility_score": compatibilityScore
            ])

        case let .messageSent(conversationId):
            Analytics.logEvent("message_sent", parameters: [
                "conversation_id": conversationId
            ])

        case let .premiumUpgradeAttempt(plan, screen):
            Analytics.logEvent("premium_upgrade_attempt", parameters: [
                "plan": plan,
                "screen": screen
            ])

        case let .premiumUpgradeComplete(plan, revenue):
            // Usar evento padrão Firebase para compras
            Analytics.logEvent(AnalyticsEventPurchase, parameters: [
                AnalyticsParameterItemID: plan,
                AnalyticsParameterValue: revenue,
                AnalyticsParameterCurrency: "BRL"
            ])

        case let .profileView(viewedUserId):
            Analytics.logEvent("profile_view", parameters: [
                "viewed_user_id": viewedUserId
            ])

        case let .appOpen(isReturning):
            Analytics.logEvent("app_open", parameters: [
                "is_returning": isReturning ? "true" : "false"
            ])

        case .onboardingComplete:
            Analytics.logEvent("onboarding_complete", parameters: [:])

        case let .accessCodeRedeemed(codeType):
            Analytics.logEvent("access_code_redeemed", parameters: [
                "code_type": codeType
            ])

        case let .aiCounselorUsed(creditsUsed):
            Analytics.logEvent("ai_counselor_used", parameters: [
                "credits_used": creditsUsed
            ])

        case let .adsWatched(creditsGranted):
            Analytics.logEvent("ads_watched", parameters: [
                "credits_granted": creditsGranted
            ])

        case let .waitlistJoined(city):
            Analytics.logEvent("waitlist_joined", parameters: [
                "city": city
            ])

        case let .photoUploaded(photoCount):
            Analytics.logEvent("photo_uploaded", parameters: [
                "photo_count": photoCount
            ])

        case let .screenView(screenName):
            Analytics.logEvent(AnalyticsEventScreenView, parameters: [
                AnalyticsParameterScreenName: screenName
            ])

        case let .errorOccurred(errorType, details):
            Analytics.logEvent("error_occurred", parameters: [
                "error_type": errorType,
                "details": String(details.prefix(100)) // Firebase limita tamanho de params
            ])
        }
    }

    // MARK: - Propriedades de Usuário

    /// Define uma propriedade de usuário para segmentação
    func setUserProperty(_ property: UserProperty, value: String) {
        Analytics.setUserProperty(value, forName: property.rawValue)
    }

    /// Define o userId para vinculação de eventos
    func setUserId(_ userId: String) {
        Analytics.setUserID(userId)
    }

    /// Identifica o usuário com todas as propriedades de uma vez
    func identify(user: User) {
        setUserId(user.id)
        setUserProperty(.isPremium, value: user.isPremium ? "true" : "false")
        setUserProperty(.planType, value: user.isPremium ? "premium" : "free")
        setUserProperty(.gender, value: user.gender.rawValue)
        if let city = user.city {
            setUserProperty(.city, value: city)
        }
        setUserProperty(.ageGroup, value: ageGroup(for: user.age))
    }

    // MARK: - Helpers Privados

    private func ageGroup(for age: Int) -> String {
        switch age {
        case 18...24: return "18-24"
        case 25...34: return "25-34"
        case 35...44: return "35-44"
        default:      return "45+"
        }
    }
}

// MARK: - TCA Dependency

private enum AnalyticsServiceKey: DependencyKey {
    static let liveValue = AnalyticsService.shared
}

extension DependencyValues {
    var analyticsService: AnalyticsService {
        get { self[AnalyticsServiceKey.self] }
        set { self[AnalyticsServiceKey.self] = newValue }
    }
}
