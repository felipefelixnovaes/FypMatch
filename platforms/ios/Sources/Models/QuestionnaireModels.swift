// QuestionnaireModels.swift
// FypMatch — Modelos do sistema de questionários de compatibilidade
// Architecture Squad — Sprint 5

import Foundation

// MARK: - Questionário do usuário (agregador raiz)

struct UserQuestionnaire: Codable, Equatable {
    var bigFive: BigFiveResult?
    var values: ValuesResult?
    var communicationStyle: CommunicationResult?
    var routine: RoutineResult?
    var dealBreakers: Set<DealBreaker>
    var completedModes: Set<QuestionnaireMode>
    var completedAt: Date?

    init(
        bigFive: BigFiveResult? = nil,
        values: ValuesResult? = nil,
        communicationStyle: CommunicationResult? = nil,
        routine: RoutineResult? = nil,
        dealBreakers: Set<DealBreaker> = [],
        completedModes: Set<QuestionnaireMode> = [],
        completedAt: Date? = nil
    ) {
        self.bigFive = bigFive
        self.values = values
        self.communicationStyle = communicationStyle
        self.routine = routine
        self.dealBreakers = dealBreakers
        self.completedModes = completedModes
        self.completedAt = completedAt
    }

    /// Retorna true se o Modo Rápido está completamente preenchido
    var isQuickModeComplete: Bool {
        bigFive != nil &&
        values != nil &&
        communicationStyle != nil &&
        routine != nil
    }
}

// MARK: - Modos do questionário

enum QuestionnaireMode: String, Codable, CaseIterable {
    case quick          = "quick"
    case deep           = "deep"
    case selfKnowledge  = "selfKnowledge"

    var displayName: String {
        switch self {
        case .quick:         return "Modo Rápido"
        case .deep:          return "Modo Profundo"
        case .selfKnowledge: return "Autoconhecimento"
        }
    }

    var estimatedMinutes: Int {
        switch self {
        case .quick:         return 5
        case .deep:          return 20
        case .selfKnowledge: return 10
        }
    }
}

// MARK: - Big Five (TIPI-10)

/// Resultado do TIPI-10 — cada fator em escala 1.0–7.0
struct BigFiveResult: Codable, Equatable {
    /// Extroversão: (item1 + rev(item6)) / 2
    var extraversion: Double
    /// Amabilidade: (rev(item2) + item7) / 2
    var agreeableness: Double
    /// Conscienciosidade: (item3 + rev(item8)) / 2
    var conscientiousness: Double
    /// Neuroticismo: (item4 + rev(item9)) / 2
    var neuroticism: Double
    /// Abertura: (item5 + rev(item10)) / 2
    var openness: Double
    /// Versão do instrumento utilizado
    var version: String = "TIPI-10"

    /// Normaliza um fator da escala 1–7 para 0–100
    static func normalize(_ value: Double) -> Double {
        ((value - 1.0) / 6.0) * 100.0
    }

    var extraversionNormalized: Double   { BigFiveResult.normalize(extraversion) }
    var agreeablenessNormalized: Double  { BigFiveResult.normalize(agreeableness) }
    var conscientiousnessNormalized: Double { BigFiveResult.normalize(conscientiousness) }
    var neuroticismNormalized: Double    { BigFiveResult.normalize(neuroticism) }
    var opennessNormalized: Double       { BigFiveResult.normalize(openness) }
}

// MARK: - Valores (Schwartz simplificado)

struct ValuesResult: Codable, Equatable {
    /// Top-3 valores em ordem de prioridade (índice 0 = 1º lugar)
    var topValues: [SchwartzValue]

    init(topValues: [SchwartzValue]) {
        precondition(topValues.count <= 3, "ValuesResult aceita no máximo 3 valores")
        self.topValues = topValues
    }
}

enum SchwartzValue: String, Codable, CaseIterable {
    case security       = "security"
    case freedom        = "freedom"
    case family         = "family"
    case achievement    = "achievement"
    case tradition      = "tradition"
    case hedonism       = "hedonism"
    case benevolence    = "benevolence"
    case universalism   = "universalism"
    case power          = "power"
    case conformity     = "conformity"

    var displayName: String {
        switch self {
        case .security:     return "Segurança"
        case .freedom:      return "Liberdade"
        case .family:       return "Família"
        case .achievement:  return "Realização"
        case .tradition:    return "Tradição"
        case .hedonism:     return "Hedonismo"
        case .benevolence:  return "Benevolência"
        case .universalism: return "Universalismo"
        case .power:        return "Poder"
        case .conformity:   return "Conformidade"
        }
    }

    var emoji: String {
        switch self {
        case .security:     return "🛡️"
        case .freedom:      return "🦋"
        case .family:       return "🏡"
        case .achievement:  return "🏆"
        case .tradition:    return "🕯️"
        case .hedonism:     return "🎉"
        case .benevolence:  return "💛"
        case .universalism: return "🌍"
        case .power:        return "👑"
        case .conformity:   return "🤝"
        }
    }

    var description: String {
        switch self {
        case .security:     return "Estabilidade, proteção e previsibilidade na vida"
        case .freedom:      return "Autonomia, independência e autodeterminação"
        case .family:       return "Vínculos próximos, cuidado e pertencimento"
        case .achievement:  return "Sucesso, competência e reconhecimento"
        case .tradition:    return "Respeito ao legado, costumes e crenças herdadas"
        case .hedonism:     return "Prazer, diversão e aproveitamento do presente"
        case .benevolence:  return "Cuidar do próximo e promover o bem-estar alheio"
        case .universalism: return "Justiça social, sustentabilidade e igualdade"
        case .power:        return "Influência, liderança e status social"
        case .conformity:   return "Harmonia social, respeito às normas e cooperação"
        }
    }
}

// MARK: - Comunicação

struct CommunicationResult: Codable, Equatable {
    /// Q1 — Como lida quando está chateado(a)
    var conflictStyle: ConflictStyle
    /// Q2 — Preferência de frequência de mensagens
    var messagingFrequency: MessagingPref
    /// Q3 — Reação quando alguém some por horas (proxy de apego)
    var absenceReaction: AbsenceReaction
    /// Q4 — Profundidade de conversa preferida
    var conversationDepth: ConvDepth
    /// Q5 — Meio preferido para resolver conflito
    var conflictMedium: ConflictMedium
}

/// Q1 — Estilo de conflito imediato
enum ConflictStyle: String, Codable, CaseIterable {
    case onTheSpot  = "onTheSpot"   // Conversar na hora
    case waitCool   = "waitCool"    // Esperar esfriar
    case withdraw   = "withdraw"    // Me afastar por um tempo

    var displayName: String {
        switch self {
        case .onTheSpot: return "Conversar na hora"
        case .waitCool:  return "Esperar esfriar"
        case .withdraw:  return "Me afastar por um tempo"
        }
    }
}

/// Q2 — Frequência de mensagens
enum MessagingPref: String, Codable, CaseIterable {
    case frequent   = "frequent"    // Gosto de frequência
    case contextual = "contextual"  // Depende do contexto
    case spacious   = "spacious"    // Prefiro espaço

    var displayName: String {
        switch self {
        case .frequent:   return "Gosto de frequência"
        case .contextual: return "Depende do contexto"
        case .spacious:   return "Prefiro espaço"
        }
    }
}

/// Q3 — Reação à ausência (proxy de apego ansioso/evitante)
enum AbsenceReaction: String, Codable, CaseIterable {
    case normal          = "normal"          // Normal, não me afeta
    case slightlyAnxious = "slightlyAnxious" // Fico um pouco ansioso(a)
    case veryAnxious     = "veryAnxious"     // Me preocupo muito

    var displayName: String {
        switch self {
        case .normal:          return "Normal, não me afeta"
        case .slightlyAnxious: return "Fico um pouco ansioso(a)"
        case .veryAnxious:     return "Me preocupo muito"
        }
    }
}

/// Q4 — Profundidade de conversa
enum ConvDepth: String, Codable, CaseIterable {
    case direct = "direct" // Diretas e objetivas
    case deep   = "deep"   // Longas e profundas
    case mixed  = "mixed"  // Mistura dos dois

    var displayName: String {
        switch self {
        case .direct: return "Diretas e objetivas"
        case .deep:   return "Longas e profundas"
        case .mixed:  return "Mistura dos dois"
        }
    }
}

/// Q5 — Meio preferido para conflito
enum ConflictMedium: String, Codable, CaseIterable {
    case verbal   = "verbal"   // Resolver na hora (verbal imediato)
    case deferred = "deferred" // Depois que a emoção baixa
    case written  = "written"  // Por escrito antes de falar

    var displayName: String {
        switch self {
        case .verbal:   return "Resolver na hora"
        case .deferred: return "Depois que a emoção baixa"
        case .written:  return "Por escrito antes de falar"
        }
    }
}

// MARK: - Rotina e Energia

struct RoutineResult: Codable, Equatable {
    /// Q1 — Estilo de fim de semana
    var weekendStyle: WeekendStyle
    /// Q2 — Nível de planejamento vs espontaneidade
    var planningStyle: PlanningStyle
    /// Q3 — Fonte de energia (introversão/extroversão)
    var energySource: EnergySource
    /// Q4 — Equilíbrio trabalho-vida
    var workLifeBalance: WorkLifeBalance
    /// Q5 — Preferência de ambiente em casa
    var homeNoise: HomeNoise
}

/// Q1 — Fim de semana
enum WeekendStyle: String, Codable, CaseIterable {
    case homebody  = "homebody"  // Em casa relaxando
    case balanced  = "balanced"  // Equilíbrio
    case explorer  = "explorer"  // Sair e explorar

    var displayName: String {
        switch self {
        case .homebody: return "Em casa relaxando"
        case .balanced: return "Equilíbrio"
        case .explorer: return "Sair e explorar"
        }
    }
}

/// Q2 — Planejamento vs espontaneidade
enum PlanningStyle: String, Codable, CaseIterable {
    case routine      = "routine"      // Pessoa de rotina
    case spontaneous  = "spontaneous"  // Espontâneo(a)
    case mixed        = "mixed"        // Depende do momento

    var displayName: String {
        switch self {
        case .routine:     return "Pessoa de rotina"
        case .spontaneous: return "Espontâneo(a)"
        case .mixed:       return "Depende do momento"
        }
    }
}

/// Q3 — Fonte de energia
enum EnergySource: String, Codable, CaseIterable {
    case introvert  = "introvert"  // Sozinho(a), preciso de silêncio
    case extrovert  = "extrovert"  // Com as pessoas

    var displayName: String {
        switch self {
        case .introvert: return "Sozinho(a), preciso de silêncio"
        case .extrovert: return "Com as pessoas"
        }
    }
}

/// Q4 — Trabalho e vida
enum WorkLifeBalance: String, Codable, CaseIterable {
    case ambitious    = "ambitious"    // Trabalho muito, ambição alta
    case balanced     = "balanced"     // Equilíbrio
    case leisureFirst = "leisureFirst" // Priorizo qualidade de vida

    var displayName: String {
        switch self {
        case .ambitious:    return "Trabalho muito, ambição alta"
        case .balanced:     return "Equilíbrio"
        case .leisureFirst: return "Priorizo qualidade de vida"
        }
    }
}

/// Q5 — Ambiente em casa
enum HomeNoise: String, Codable, CaseIterable {
    case quiet     = "quiet"     // Silêncio para me concentrar
    case someNoise = "someNoise" // Um pouco de som/movimento
    case anyEnv    = "anyEnv"    // Qualquer ambiente

    var displayName: String {
        switch self {
        case .quiet:     return "Silêncio para me concentrar"
        case .someNoise: return "Um pouco de som/movimento"
        case .anyEnv:    return "Qualquer ambiente"
        }
    }
}

// MARK: - Deal-Breakers

enum DealBreaker: String, Codable, CaseIterable {
    case smoking               = "smoking"
    case excessiveDrinking     = "excessiveDrinking"
    case noChildrenEver        = "noChildrenEver"
    case childrenMandatory     = "childrenMandatory"
    case incompatibleReligion  = "incompatibleReligion"
    case politicalOpposite     = "politicalOpposite"
    case openRelationship      = "openRelationship"
    case violence              = "violence"
    case infidelity            = "infidelity"
    case veryDifferentPace     = "veryDifferentPace"

    var displayName: String {
        switch self {
        case .smoking:              return "Fumar"
        case .excessiveDrinking:    return "Beber excessivamente"
        case .noChildrenEver:       return "Não quer filhos jamais"
        case .childrenMandatory:    return "Quer filhos obrigatoriamente"
        case .incompatibleReligion: return "Religiões incompatíveis"
        case .politicalOpposite:    return "Politicamente muito oposto(a)"
        case .openRelationship:     return "Relacionamento aberto"
        case .violence:             return "Violência/agressividade"
        case .infidelity:           return "Infidelidade"
        case .veryDifferentPace:    return "Muito diferente de ritmo de vida"
        }
    }

    var icon: String {
        switch self {
        case .smoking:              return "🚬"
        case .excessiveDrinking:    return "🍺"
        case .noChildrenEver:       return "🚫"
        case .childrenMandatory:    return "👶"
        case .incompatibleReligion: return "🙏"
        case .politicalOpposite:    return "⚖️"
        case .openRelationship:     return "🔓"
        case .violence:             return "⚡"
        case .infidelity:           return "💔"
        case .veryDifferentPace:    return "🕐"
        }
    }
}
