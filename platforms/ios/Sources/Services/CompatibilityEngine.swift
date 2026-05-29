// CompatibilityEngine.swift — FypMatch iOS
// Algoritmo de compatibilidade real — 6 dimensões ponderadas

import Foundation
import ComposableArchitecture

// MARK: - Modelos de resultado

struct CompatibilityResult: Equatable {
    let overall: Int                   // 0-100
    let factors: [CompatibilityFactor]

    var colorName: String {
        overall >= 80 ? "green" : overall >= 60 ? "yellow" : "gray"
    }
    var displayText: String { "\(overall)% compatíveis" }
    var emoji: String {
        overall >= 80 ? "💚" : overall >= 60 ? "💛" : "🩶"
    }
}

struct CompatibilityFactor: Equatable, Identifiable {
    let id = UUID()
    let dimension: String
    let score: Int        // 0-100 para esta dimensão
    let weight: Double    // 0.0-1.0
    let description: String
    var weightedScore: Double { Double(score) * weight }
}

// MARK: - Engine

struct CompatibilityEngine {

    // MARK: - Ponto de entrada
    func calculate(current: User, target: User) -> CompatibilityResult {
        let f1 = interestsScore(current, target)
        let f2 = intentionsScore(current, target)
        let f3 = lifestyleScore(current, target)
        let f4 = ageScore(current, target)
        let f5 = valuesScore(current, target)
        let f6 = activityBonus(target)

        let weighted = f1.weightedScore + f2.weightedScore + f3.weightedScore
                     + f4.weightedScore + f5.weightedScore + f6.weightedScore
        let overall = min(100, max(0, Int(weighted.rounded())))

        return CompatibilityResult(
            overall: overall,
            factors: [f1, f2, f3, f4, f5, f6]
        )
    }

    // MARK: - Dimensão 1: Interesses (30%)
    private func interestsScore(_ a: User, _ b: User) -> CompatibilityFactor {
        let setA = Set(a.interests + a.hobbies)
        let setB = Set(b.interests + b.hobbies)
        let intersection = setA.intersection(setB).count
        let union = setA.union(setB).count
        let jaccard = union > 0 ? Double(intersection) / Double(union) : 0.0
        let score = Int((jaccard * 100).rounded())

        let desc: String
        switch score {
        case 70...100: desc = "Vocês têm muitos interesses em comum!"
        case 40..<70:  desc = "Alguns interesses compartilhados"
        default:       desc = "Interesses diferentes — ótimo para descobrir coisas novas"
        }

        return CompatibilityFactor(
            dimension: "Interesses", score: score, weight: 0.30, description: desc
        )
    }

    // MARK: - Dimensão 2: Intenções (25%)
    private func intentionsScore(_ a: User, _ b: User) -> CompatibilityFactor {
        var matches = 0
        var total = 0

        let pairs: [(Bool, Bool)] = [
            (a.interestedInSeriousRelationship, b.interestedInSeriousRelationship),
            (a.interestedInCasualDating,        b.interestedInCasualDating),
            (a.interestedInFriendship,           b.interestedInFriendship)
        ]
        for (x, y) in pairs {
            if x || y { total += 1 }
            if x && y { matches += 1 }
        }

        let score: Int
        let desc: String
        if total == 0 {
            score = 50
            desc = "Nenhum dos dois definiu suas intenções ainda"
        } else {
            score = Int((Double(matches) / Double(total) * 100).rounded())
            switch score {
            case 80...100: desc = "Querem a mesma coisa — ótimo sinal! 💪"
            case 40..<80:  desc = "Têm algumas intenções parecidas"
            default:       desc = "Buscam coisas diferentes no momento"
            }
        }

        return CompatibilityFactor(
            dimension: "Intenções", score: score, weight: 0.25, description: desc
        )
    }

    // MARK: - Dimensão 3: Estilo de vida (20%)
    private func lifestyleScore(_ a: User, _ b: User) -> CompatibilityFactor {
        let smokingScore = lifestyleCompatibility(a.smoking.rawValue, b.smoking.rawValue,
            neutral: "sometimes", dealbreaker: "regularly")
        let drinkingScore = lifestyleCompatibility(a.drinking.rawValue, b.drinking.rawValue,
            neutral: "sometimes", dealbreaker: "regularly")
        let childrenScore = childrenCompatibility(a, b)

        let avg = (smokingScore + drinkingScore + childrenScore) / 3
        let desc: String
        switch avg {
        case 80...100: desc = "Estilos de vida muito compatíveis"
        case 50..<80:  desc = "Diferenças de estilo de vida gerenciáveis"
        default:       desc = "Alguns hábitos podem precisar de conversa"
        }

        return CompatibilityFactor(
            dimension: "Estilo de vida", score: avg, weight: 0.20, description: desc
        )
    }

    private func lifestyleCompatibility(_ a: String, _ b: String,
                                        neutral: String, dealbreaker: String) -> Int {
        if a == b { return 100 }
        if (a == "never" && b == dealbreaker) || (b == "never" && a == dealbreaker) { return 20 }
        return 60
    }

    private func childrenCompatibility(_ a: User, _ b: User) -> Int {
        switch (a.children, b.children, a.wantsChildren, b.wantsChildren) {
        case (.yes, .yes, _, _): return 80
        case (.no, .no, _, _):   return 90
        case (_, _, let wa, let wb) where wa == wb && wa != nil: return 85
        case (.yes, .no, _, _), (.no, .yes, _, _): return 30
        default: return 60
        }
    }

    // MARK: - Dimensão 4: Compatibilidade de idade (10%)
    private func ageScore(_ a: User, _ b: User) -> CompatibilityFactor {
        let aInB = b.ageRangeMin <= a.age && a.age <= b.ageRangeMax
        let bInA = a.ageRangeMin <= b.age && b.age <= a.ageRangeMax

        let score: Int
        let desc: String
        switch (aInB, bInA) {
        case (true, true):
            score = 100
            desc = "Faixa etária ideal para os dois"
        case (true, false), (false, true):
            score = 60
            desc = "Uma das partes está fora da faixa preferida"
        default:
            score = 20
            desc = "Fora da faixa etária preferida"
        }

        return CompatibilityFactor(
            dimension: "Faixa etária", score: score, weight: 0.10, description: desc
        )
    }

    // MARK: - Dimensão 5: Valores (10%)
    private func valuesScore(_ a: User, _ b: User) -> CompatibilityFactor {
        // Idiomas
        let langA = Set(a.languages)
        let langB = Set(b.languages)
        let langUnion = langA.union(langB).count
        let langInter = langA.intersection(langB).count
        let langScore = langUnion > 0 ? Double(langInter) / Double(langUnion) : 1.0

        // Religião
        let relScore: Double
        if let ra = a.religion, let rb = b.religion {
            relScore = ra == rb ? 1.0 : 0.4
        } else {
            relScore = 0.7  // não informado = neutro
        }

        let combined = Int(((langScore * 0.6 + relScore * 0.4) * 100).rounded())
        let desc = combined >= 70 ? "Valores e origem cultural próximos" : "Backgrounds culturais diferentes"

        return CompatibilityFactor(
            dimension: "Valores", score: combined, weight: 0.10, description: desc
        )
    }

    // MARK: - Dimensão 6: Bônus de atividade (5%)
    private func activityBonus(_ target: User) -> CompatibilityFactor {
        var score = 0
        if target.isOnline        { score += 60 }
        if target.isVerified      { score += 20 }
        if target.isPhotoVerified { score += 20 }
        let desc = target.isOnline ? "Online agora — ótimo momento para curtir!" : "Visto recentemente"

        return CompatibilityFactor(
            dimension: "Atividade", score: score, weight: 0.05, description: desc
        )
    }
}

// MARK: - TCA Dependency

extension CompatibilityEngine: DependencyKey {
    static let liveValue = CompatibilityEngine()
    static let testValue = CompatibilityEngine()
}

extension DependencyValues {
    var compatibilityEngine: CompatibilityEngine {
        get { self[CompatibilityEngine.self] }
        set { self[CompatibilityEngine.self] = newValue }
    }
}
