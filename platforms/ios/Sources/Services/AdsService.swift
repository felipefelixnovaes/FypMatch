// AdsService.swift — FypMatch iOS
// Serviço de anúncios recompensados — ganhar créditos de IA assistindo anúncios

import Foundation
import ComposableArchitecture

// MARK: - Models

struct AdsCreditsInfo: Equatable {
    let adsWatchedToday: Int
    let maxAdsPerDay: Int       // 3 por dia
    let creditsEarnedToday: Int
    let creditsPerAd: Int       // 3 créditos
    var canWatchMore: Bool { adsWatchedToday < maxAdsPerDay }
}

// MARK: - AdsService

struct AdsService {
    var canWatchAd: (_ userId: String) -> Bool
    var watchAd: (_ userId: String) async throws -> Int     // retorna créditos ganhos (3)
    var getCreditsInfo: (_ userId: String) -> AdsCreditsInfo
}

// MARK: - Helpers

private func todayDateString() -> String {
    let f = DateFormatter()
    f.dateFormat = "yyyy-MM-dd"
    return f.string(from: Date())
}

private func adsWatchedKey(_ userId: String) -> String {
    "ads_watched_\(userId)_\(todayDateString())"
}

private let maxAdsPerDay = 3
private let creditsPerAd = 3

// MARK: - Live Value

extension AdsService {
    static let liveValue = AdsService(
        canWatchAd: { userId in
            let watched = UserDefaults.standard.integer(forKey: adsWatchedKey(userId))
            return watched < maxAdsPerDay
        },

        watchAd: { userId in
            let key = adsWatchedKey(userId)
            let watched = UserDefaults.standard.integer(forKey: key)
            guard watched < maxAdsPerDay else {
                throw AdsServiceError.dailyLimitReached
            }

            // Simula carregamento e exibição do anúncio (5 segundos)
            try await Task.sleep(for: .seconds(5))

            // Registra visualização
            UserDefaults.standard.set(watched + 1, forKey: key)

            return creditsPerAd
        },

        getCreditsInfo: { userId in
            let watched = UserDefaults.standard.integer(forKey: adsWatchedKey(userId))
            return AdsCreditsInfo(
                adsWatchedToday: watched,
                maxAdsPerDay: maxAdsPerDay,
                creditsEarnedToday: watched * creditsPerAd,
                creditsPerAd: creditsPerAd
            )
        }
    )
}

// MARK: - Errors

enum AdsServiceError: LocalizedError {
    case dailyLimitReached

    var errorDescription: String? {
        switch self {
        case .dailyLimitReached:
            return "Você já assistiu ao máximo de anúncios por hoje. Volte amanhã!"
        }
    }
}

// MARK: - TCA Dependency

private enum AdsServiceKey: DependencyKey {
    static let liveValue = AdsService.liveValue
}

extension DependencyValues {
    var adsService: AdsService {
        get { self[AdsServiceKey.self] }
        set { self[AdsServiceKey.self] = newValue }
    }
}
