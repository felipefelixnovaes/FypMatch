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

// MARK: - Deep Mode Models (Sprint 6)

// MARK: - IPIP-20 (Big Five — 20 itens representativos)

/// Resultado do IPIP-20 — 20 respostas em escala 1–5.
///
/// Itens por fator (índices 0-based):
///   Extroversão:       0, 1, 2, 3
///   Amabilidade:       4, 5, 6, 7
///   Conscienciosidade: 8, 9, 10, 11
///   Neuroticismo:      12, 13, 14, 15
///   Abertura:          16, 17, 18, 19
///
/// Itens reversos (0-based): 1, 5, 9, 13, 17
///   Reversão: 6 - value  (escala 1–5 → rev = 6 - x)
struct IPIP20Result: Equatable, Codable {
    /// 20 respostas; índice 0-19, valores 1..5
    var responses: [Int]

    private func rev(_ v: Int) -> Double { Double(6 - v) }
    private func d(_ v: Int) -> Double  { Double(v) }

    /// Extroversão — média dos itens 0–3 (item 1 reverso)
    var extroversion: Double {
        [d(responses[0]), rev(responses[1]), d(responses[2]), d(responses[3])].reduce(0, +) / 4.0
    }

    /// Amabilidade — média dos itens 4–7 (item 5 reverso)
    var agreeableness: Double {
        [d(responses[4]), rev(responses[5]), d(responses[6]), d(responses[7])].reduce(0, +) / 4.0
    }

    /// Conscienciosidade — média dos itens 8–11 (item 9 reverso)
    var conscientiousness: Double {
        [d(responses[8]), rev(responses[9]), d(responses[10]), d(responses[11])].reduce(0, +) / 4.0
    }

    /// Neuroticismo — média dos itens 12–15 (item 13 reverso)
    var neuroticism: Double {
        [d(responses[12]), rev(responses[13]), d(responses[14]), d(responses[15])].reduce(0, +) / 4.0
    }

    /// Abertura — média dos itens 16–19 (item 17 reverso)
    var openness: Double {
        [d(responses[16]), rev(responses[17]), d(responses[18]), d(responses[19])].reduce(0, +) / 4.0
    }

    /// Converte escala 1–5 para 0–100
    private static func toPercent(_ v: Double) -> Double { (v - 1.0) / 4.0 * 100.0 }

    /// Scores normalizados 0–100 como BigFiveResult
    var normalized: BigFiveResult {
        BigFiveResult(
            extraversion:      IPIP20Result.toPercent(extroversion),
            agreeableness:     IPIP20Result.toPercent(agreeableness),
            conscientiousness: IPIP20Result.toPercent(conscientiousness),
            neuroticism:       IPIP20Result.toPercent(neuroticism),
            openness:          IPIP20Result.toPercent(openness),
            version:           "IPIP-20"
        )
    }
}

// MARK: - PVQ-21 (Schwartz Portrait Values Questionnaire — 21 itens)

/// Resultado do PVQ-21 — 21 respostas em escala 1–6.
///
/// Escala original: 1 = Muito parecido comigo → 6 = Não se parece nada comigo.
/// Score de valor = 7 - resposta  (inversão para que 6 = alto alinhamento).
///
/// Mapeamento dos 10 valores (0-based):
///   Conformidade:    [0, 10]
///   Tradição:        [1, 11]
///   Benevolência:    [2, 12]
///   Universalismo:   [3, 13, 19]
///   Autodireção:     [4, 14]
///   Estimulação:     [5, 15]
///   Hedonismo:       [6, 20]
///   Realização:      [7, 16]
///   Poder:           [8, 17]
///   Segurança:       [9, 18]
struct PVQ21Result: Equatable, Codable {
    /// 21 respostas; índice 0-20, valores 1..6
    var responses: [Int]

    private func inv(_ idx: Int) -> Double { Double(7 - responses[idx]) }
    private func mean(_ indices: [Int]) -> Double {
        indices.map { inv($0) }.reduce(0, +) / Double(indices.count)
    }

    var conformity:    Double { mean([0, 10]) }
    var tradition:     Double { mean([1, 11]) }
    var benevolence:   Double { mean([2, 12]) }
    var universalism:  Double { mean([3, 13, 19]) }
    var selfDirection: Double { mean([4, 14]) }
    var stimulation:   Double { mean([5, 15]) }
    var hedonism:      Double { mean([6, 20]) }
    var achievement:   Double { mean([7, 16]) }
    var power:         Double { mean([8, 17]) }
    var security:      Double { mean([9, 18]) }

    /// Top 3 valores com maior score
    var topValues: [SchwartzValue] {
        let ranked: [(SchwartzValue, Double)] = [
            (.conformity,    conformity),
            (.tradition,     tradition),
            (.benevolence,   benevolence),
            (.universalism,  universalism),
            (.freedom,       selfDirection),
            (.hedonism,      hedonism),
            (.achievement,   achievement),
            (.power,         power),
            (.security,      security)
        ].sorted { $0.1 > $1.1 }
        return Array(ranked.prefix(3).map { $0.0 })
    }
}

// MARK: - ECR-RS (Experiences in Close Relationships — Revised Short, 12 itens)

/// Resultado do ECR-RS-12 — 12 respostas em escala 1–7.
///
/// Ansiedade (anxiety):  itens 0–5  (6 itens, todos diretos nesta versão)
/// Evitação (avoidance): itens 6–11 (6 itens, todos diretos nesta versão)
///
/// Cutoffs:  ansiedade alta = score > 3.5 | evitação alta = score > 3.5
struct ECRRSResult: Equatable, Codable {
    /// 12 respostas; índice 0-11, valores 1..7
    var responses: [Int]

    /// Média dos 6 itens de ansiedade (itens 0–5)
    var anxietyScore: Double {
        responses[0..<6].map(Double.init).reduce(0, +) / 6.0
    }

    /// Média dos 6 itens de evitação (itens 6–11)
    var avoidanceScore: Double {
        responses[6..<12].map(Double.init).reduce(0, +) / 6.0
    }

    /// Classificação do estilo de apego baseada nos cutoffs (> 3.5 = alto)
    var attachmentStyle: AttachmentStyle {
        let highAnxiety   = anxietyScore   > 3.5
        let highAvoidance = avoidanceScore > 3.5
        switch (highAnxiety, highAvoidance) {
        case (false, false): return .secure
        case (true,  false): return .anxious
        case (false, true):  return .avoidant
        case (true,  true):  return .disorganized
        }
    }
}

enum AttachmentStyle: String, Equatable, Codable {
    case secure
    case anxious
    case avoidant
    case disorganized

    var displayName: String {
        switch self {
        case .secure:       return "Seguro"
        case .anxious:      return "Ansioso"
        case .avoidant:     return "Evitante"
        case .disorganized: return "Desorganizado"
        }
    }

    var description: String {
        switch self {
        case .secure:       return "Confortável com intimidade e independência"
        case .anxious:      return "Busca muita proximidade, teme abandono"
        case .avoidant:     return "Prefere independência, desconforto com intimidade"
        case .disorganized: return "Ambivalente entre proximidade e distância"
        }
    }

    var emoji: String {
        switch self {
        case .secure:       return "💚"
        case .anxious:      return "💛"
        case .avoidant:     return "🩶"
        case .disorganized: return "🔮"
        }
    }
}

// MARK: - Conflito Profundo (6 cenários)

struct DeepConflictResult: Equatable, Codable {
    /// Como resolve conflitos
    var resolutionStyle: ConflictResolutionStyle
    /// Como expressa emoções
    var emotionalExpression: EmotionalExpression
    /// Como repara após conflito
    var repairBehavior: RepairBehavior
    /// Quanto tempo precisa depois de uma briga
    var silencePeriod: SilencePeriod
    /// Como pede desculpa
    var apologyStyle: ApologyStyle
    /// Como recebe críticas
    var feedbackTolerance: FeedbackTolerance
}

enum ConflictResolutionStyle: String, Equatable, Codable, CaseIterable {
    case immediate   // "Prefiro resolver na hora, sem deixar acumular"
    case coolingOff  // "Preciso de tempo para processar antes de conversar"
    case avoidant    // "Evito conflito, prefiro ceder para manter a paz"
    case analytical  // "Gosto de entender a raiz do problema antes de resolver"

    var displayName: String {
        switch self {
        case .immediate:  return "Resolver na hora"
        case .coolingOff: return "Processar antes de conversar"
        case .avoidant:   return "Evitar conflito, ceder pela paz"
        case .analytical: return "Entender a raiz antes de resolver"
        }
    }
}

enum EmotionalExpression: String, Equatable, Codable, CaseIterable {
    case expressive  // "Expresso tudo que sinto, às vezes com intensidade"
    case moderate    // "Expresso, mas de forma controlada"
    case contained   // "Guardo para mim até estar seguro(a)"
    case written     // "Prefiro escrever para organizar antes de falar"

    var displayName: String {
        switch self {
        case .expressive: return "Expresso com intensidade"
        case .moderate:   return "Expresso de forma controlada"
        case .contained:  return "Guardo até estar seguro(a)"
        case .written:    return "Escrevo antes de falar"
        }
    }
}

enum RepairBehavior: String, Equatable, Codable, CaseIterable {
    case immediate  // "Já busco reconciliação logo depois"
    case gradual    // "Reaproximento aos poucos, no meu tempo"
    case waitOther  // "Espero o outro dar o primeiro passo"
    case gesture    // "Demonstro com um gesto (presente, carinho, ato)"

    var displayName: String {
        switch self {
        case .immediate:  return "Busco reconciliação imediatamente"
        case .gradual:    return "Reaproximento aos poucos"
        case .waitOther:  return "Espero o outro dar o primeiro passo"
        case .gesture:    return "Demonstro com gesto ou ato"
        }
    }
}

enum SilencePeriod: String, Equatable, Codable, CaseIterable {
    case none   // "Não preciso de silêncio"
    case hours  // "Algumas horas"
    case day    // "Pelo menos um dia"
    case days   // "Alguns dias"

    var displayName: String {
        switch self {
        case .none:  return "Não preciso de silêncio"
        case .hours: return "Algumas horas"
        case .day:   return "Pelo menos um dia"
        case .days:  return "Alguns dias"
        }
    }
}

enum ApologyStyle: String, Equatable, Codable, CaseIterable {
    case verbal    // "Digo claramente que errei"
    case action    // "Demonstro com atitudes"
    case both      // "Falo e mostro com ações"
    case difficult // "Tenho dificuldade em pedir desculpa"

    var displayName: String {
        switch self {
        case .verbal:    return "Digo claramente que errei"
        case .action:    return "Demonstro com atitudes"
        case .both:      return "Falo e mostro com ações"
        case .difficult: return "Tenho dificuldade em pedir desculpa"
        }
    }
}

enum FeedbackTolerance: String, Equatable, Codable, CaseIterable {
    case receptive   // "Recebo bem críticas, gosto de melhorar"
    case contextual  // "Depende de como é dado"
    case sensitive   // "Sinto muito, levo um tempo para processar"
    case resistant   // "Fico na defensiva inicialmente"

    var displayName: String {
        switch self {
        case .receptive:  return "Recebo bem, gosto de melhorar"
        case .contextual: return "Depende de como é dado"
        case .sensitive:  return "Sinto muito, preciso de tempo"
        case .resistant:  return "Fico na defensiva inicialmente"
        }
    }
}

// MARK: - Projeto de Vida (5 dimensões)

struct LifeProjectResult: Equatable, Codable {
    var childrenDesire:      ChildrenDesire
    var locationFlexibility: LocationFlexibility
    var careerPriority:      CareerPriority
    var financialApproach:   FinancialApproach
    var spiritualityRole:    SpiritualityRole
}

enum ChildrenDesire: String, Equatable, Codable, CaseIterable {
    case definitelyYes  // "Quero filhos, é prioridade"
    case openToIt       // "Aberto(a), se a relação caminhar bem"
    case undecided      // "Ainda não sei"
    case probablyNot    // "Provavelmente não"
    case definitelyNo   // "Não quero filhos"

    var displayName: String {
        switch self {
        case .definitelyYes: return "Quero filhos, é prioridade"
        case .openToIt:      return "Aberto(a), se a relação caminhar bem"
        case .undecided:     return "Ainda não sei"
        case .probablyNot:   return "Provavelmente não"
        case .definitelyNo:  return "Não quero filhos"
        }
    }
}

enum LocationFlexibility: String, Equatable, Codable, CaseIterable {
    case stayHere      // "Quero ficar na cidade onde estou"
    case openSameCity  // "Posso mudar dentro da mesma cidade"
    case openSameState // "Aberto(a) a mudar de estado"
    case openBrazil    // "Poderia morar em qualquer lugar do Brasil"
    case openWorld     // "Poderia morar em qualquer lugar do mundo"

    var displayName: String {
        switch self {
        case .stayHere:      return "Ficar na minha cidade"
        case .openSameCity:  return "Mudar dentro da mesma cidade"
        case .openSameState: return "Aberto(a) a mudar de estado"
        case .openBrazil:    return "Qualquer lugar do Brasil"
        case .openWorld:     return "Qualquer lugar do mundo"
        }
    }
}

enum CareerPriority: String, Equatable, Codable, CaseIterable {
    case veryHigh      // "Carreira é central na minha vida"
    case balanced      // "Equilíbrio entre carreira e vida pessoal"
    case lifeFirst     // "Vida pessoal vem primeiro"
    case transitioning // "Estou em transição de carreira"

    var displayName: String {
        switch self {
        case .veryHigh:      return "Carreira é central na minha vida"
        case .balanced:      return "Equilíbrio entre carreira e vida pessoal"
        case .lifeFirst:     return "Vida pessoal vem primeiro"
        case .transitioning: return "Estou em transição de carreira"
        }
    }
}

enum FinancialApproach: String, Equatable, Codable, CaseIterable {
    case saver    // "Poupo muito, me preocupo com segurança financeira"
    case balanced // "Equilíbrio entre poupar e aproveitar"
    case spender  // "Prefiro viver bem hoje do que economizar demais"
    case investor // "Invisto ativamente, penso em patrimônio"

    var displayName: String {
        switch self {
        case .saver:    return "Poupo muito, priorizo segurança financeira"
        case .balanced: return "Equilíbrio entre poupar e aproveitar"
        case .spender:  return "Prefiro viver bem hoje"
        case .investor: return "Invisto ativamente, penso em patrimônio"
        }
    }
}

enum SpiritualityRole: String, Equatable, Codable, CaseIterable {
    case central      // "Espiritualidade/religião é central na minha vida"
    case important    // "É importante, mas não define tudo"
    case personal     // "É algo muito pessoal, prefiro não misturar"
    case notImportant // "Não é importante para mim"

    var displayName: String {
        switch self {
        case .central:      return "Espiritualidade é central na minha vida"
        case .important:    return "É importante, mas não define tudo"
        case .personal:     return "É pessoal, prefiro não misturar"
        case .notImportant: return "Não é importante para mim"
        }
    }
}

// MARK: - Container do Modo Profundo

/// Agrega todos os módulos do Modo Profundo (Sprint 6).
/// Complementa UserQuestionnaire sem substituí-lo.
struct DeepModeQuestionnaire: Equatable, Codable {
    var userId: String
    var completedAt: Date?
    var ipip20: IPIP20Result?
    var pvq21: PVQ21Result?
    var ecrrs: ECRRSResult?
    var conflictDeep: DeepConflictResult?
    var lifeProject: LifeProjectResult?

    /// Retorna true se todos os 5 módulos foram respondidos
    var isComplete: Bool {
        ipip20 != nil && pvq21 != nil && ecrrs != nil &&
        conflictDeep != nil && lifeProject != nil
    }

    /// Percentual de conclusão em múltiplos de 20
    var completionPercentage: Int {
        let completed = [
            ipip20 != nil,
            pvq21 != nil,
            ecrrs != nil,
            conflictDeep != nil,
            lifeProject != nil
        ].filter { $0 }.count
        return completed * 20
    }
}

// MARK: - Self-Knowledge Models (Sprint 7)

// MARK: Eneagrama

enum EnneagramType: Int, Equatable, Codable, CaseIterable {
    case one = 1, two, three, four, five, six, seven, eight, nine

    var displayName: String {
        switch self {
        case .one:   return "O Perfeccionista"
        case .two:   return "O Prestativo"
        case .three: return "O Realizador"
        case .four:  return "O Individualista"
        case .five:  return "O Investigador"
        case .six:   return "O Leal"
        case .seven: return "O Entusiasta"
        case .eight: return "O Desafiador"
        case .nine:  return "O Pacificador"
        }
    }

    var emoji: String {
        switch self {
        case .one:   return "⚖️"
        case .two:   return "🤗"
        case .three: return "🏆"
        case .four:  return "🎨"
        case .five:  return "🔭"
        case .six:   return "🛡️"
        case .seven: return "🎉"
        case .eight: return "🦁"
        case .nine:  return "☮️"
        }
    }

    var shortDescription: String {
        switch self {
        case .one:   return "Princípios e melhoria contínua"
        case .two:   return "Cuidado e conexão com os outros"
        case .three: return "Conquistas e reconhecimento"
        case .four:  return "Autenticidade e profundidade emocional"
        case .five:  return "Conhecimento e independência"
        case .six:   return "Segurança e lealdade"
        case .seven: return "Entusiasmo e novas experiências"
        case .eight: return "Força e autonomia"
        case .nine:  return "Harmonia e paz interior"
        }
    }

    /// Compatibilidade natural entre tipos (simplificada)
    var naturalPartners: [EnneagramType] {
        switch self {
        case .one:   return [.seven, .nine, .two]
        case .two:   return [.eight, .four, .six]
        case .three: return [.six, .nine, .one]
        case .four:  return [.one, .five, .nine]
        case .five:  return [.four, .eight, .two]
        case .six:   return [.nine, .three, .two]
        case .seven: return [.one, .five, .four]
        case .eight: return [.two, .five, .nine]
        case .nine:  return [.three, .six, .one]
        }
    }
}

struct EnneagramResult: Equatable, Codable {
    /// 27 respostas: true = escolha A, false = escolha B
    var responses: [Bool]

    /// 27 pares (typeA, typeB). Cada tipo aparece em exatamente 6 pares.
    /// Pares removidos de C(9,2): (1,2)(2,3)(3,4)(4,5)(5,6)(6,7)(7,8)(8,9)(1,9)
    /// — formam um ciclo hamiltoniano, garantindo 8−2 = 6 aparições por tipo.
    static let itemMap: [(EnneagramType, EnneagramType)] = [
        (.one,   .three), // 0
        (.one,   .four),  // 1
        (.one,   .five),  // 2
        (.one,   .six),   // 3
        (.one,   .seven), // 4
        (.one,   .eight), // 5
        (.two,   .four),  // 6
        (.two,   .five),  // 7
        (.two,   .six),   // 8
        (.two,   .seven), // 9
        (.two,   .eight), // 10
        (.two,   .nine),  // 11
        (.three, .five),  // 12
        (.three, .six),   // 13
        (.three, .seven), // 14
        (.three, .eight), // 15
        (.three, .nine),  // 16
        (.four,  .six),   // 17
        (.four,  .seven), // 18
        (.four,  .eight), // 19
        (.four,  .nine),  // 20
        (.five,  .seven), // 21
        (.five,  .eight), // 22
        (.five,  .nine),  // 23
        (.six,   .eight), // 24
        (.six,   .nine),  // 25
        (.seven, .nine),  // 26
    ]
    // Distribuição verificada: cada tipo aparece em exatamente 6 pares.
    // tipo 1: índices 0–5   | tipo 2: índices 6–11
    // tipo 3: 0,12–16       | tipo 4: 1,6,17–20
    // tipo 5: 2,7,12,21–23  | tipo 6: 3,8,13,17,24,25
    // tipo 7: 4,9,14,18,21,26 | tipo 8: 5,10,15,19,22,24
    // tipo 9: 11,16,20,23,25,26

    var scores: [EnneagramType: Int] {
        var counts: [EnneagramType: Int] = [:]
        EnneagramType.allCases.forEach { counts[$0] = 0 }
        for (index, response) in responses.enumerated() {
            guard index < Self.itemMap.count else { break }
            let (typeA, typeB) = Self.itemMap[index]
            let chosen = response ? typeA : typeB
            counts[chosen, default: 0] += 1
        }
        return counts
    }

    var dominantType: EnneagramType {
        scores.max(by: { $0.value < $1.value })?.key ?? .nine
    }

    var topThree: [EnneagramType] {
        scores.sorted { $0.value > $1.value }.prefix(3).map(\.key)
    }
}

// MARK: Linguagens do Cuidado (Chapman)

enum LoveLanguage: String, Equatable, Codable, CaseIterable {
    case wordsOfAffirmation  // Palavras de afirmação
    case actsOfService       // Atos de serviço
    case receivingGifts      // Presentes
    case qualityTime         // Tempo de qualidade
    case physicalTouch       // Toque físico

    var displayName: String {
        switch self {
        case .wordsOfAffirmation: return "Palavras de Afirmação"
        case .actsOfService:      return "Atos de Serviço"
        case .receivingGifts:     return "Presentes"
        case .qualityTime:        return "Tempo de Qualidade"
        case .physicalTouch:      return "Toque Físico"
        }
    }

    var emoji: String {
        switch self {
        case .wordsOfAffirmation: return "💬"
        case .actsOfService:      return "🛠️"
        case .receivingGifts:     return "🎁"
        case .qualityTime:        return "⏰"
        case .physicalTouch:      return "🤝"
        }
    }

    var description: String {
        switch self {
        case .wordsOfAffirmation: return "Se sente amado(a) com elogios, incentivos e 'eu te amo'"
        case .actsOfService:      return "Se sente amado(a) quando o outro faz coisas por você"
        case .receivingGifts:     return "Se sente amado(a) com presentes e gestos tangíveis"
        case .qualityTime:        return "Se sente amado(a) com atenção plena e tempo juntos"
        case .physicalTouch:      return "Se sente amado(a) com abraços, carinhos e proximidade física"
        }
    }
}

struct LoveLanguageResult: Equatable, Codable {
    var responses: [Bool]   // count = 30, true=A, false=B

    static let allLanguages: [LoveLanguage] = LoveLanguage.allCases

    // 30 pares forçados (Chapman): C(5,2)=10 combinações × 3 repetições = 30 pares.
    // Cada linguagem aparece em exatamente 12 pares.
    static let itemMap: [(Int, Int)] = [
        (0,1),(0,2),(0,3),(0,4),
        (1,2),(1,3),(1,4),
        (2,3),(2,4),
        (3,4),
        (0,1),(0,2),(0,3),(0,4),
        (1,2),(1,3),(1,4),
        (2,3),(2,4),
        (3,4),
        (0,1),(0,2),(0,3),(0,4),
        (1,2),(1,3),(1,4),
        (2,3),(2,4),
        (3,4)
    ]

    var scores: [LoveLanguage: Int] {
        var counts: [LoveLanguage: Int] = [:]
        LoveLanguage.allCases.forEach { counts[$0] = 0 }
        for (index, response) in responses.enumerated() {
            guard index < Self.itemMap.count else { break }
            let (iA, iB) = Self.itemMap[index]
            let chosen = response ? Self.allLanguages[iA] : Self.allLanguages[iB]
            counts[chosen, default: 0] += 1
        }
        return counts
    }

    var primaryLanguage: LoveLanguage {
        scores.max(by: { $0.value < $1.value })?.key ?? .qualityTime
    }

    var secondaryLanguage: LoveLanguage {
        let sorted = scores.sorted { $0.value > $1.value }
        return sorted.count > 1 ? sorted[1].key : .wordsOfAffirmation
    }

    // Compatibilidade com outro resultado (0-100)
    func compatibility(with other: LoveLanguageResult) -> Int {
        let myPrimary = primaryLanguage
        let mySecondary = secondaryLanguage
        let theirPrimary = other.primaryLanguage
        let theirSecondary = other.secondaryLanguage

        if myPrimary == theirPrimary { return 100 }
        if myPrimary == theirSecondary || mySecondary == theirPrimary { return 70 }
        if mySecondary == theirSecondary { return 55 }
        return 35
    }
}

// MARK: Arquétipos FypMatch

enum FypArchetype: String, Equatable, Codable, CaseIterable {
    case explorer, creator, caregiver, ruler, sage, hero
    case rebel, lover, jester, innocent, magician, everyman

    var displayName: String {
        switch self {
        case .explorer:  return "O Explorador"
        case .creator:   return "O Criador"
        case .caregiver: return "O Cuidador"
        case .ruler:     return "O Governante"
        case .sage:      return "O Sábio"
        case .hero:      return "O Herói"
        case .rebel:     return "O Rebelde"
        case .lover:     return "O Amante"
        case .jester:    return "O Bobo da Corte"
        case .innocent:  return "O Inocente"
        case .magician:  return "O Mago"
        case .everyman:  return "O Cidadão Comum"
        }
    }

    var emoji: String {
        switch self {
        case .explorer:  return "🧭"
        case .creator:   return "🎨"
        case .caregiver: return "💛"
        case .ruler:     return "👑"
        case .sage:      return "📚"
        case .hero:      return "⚔️"
        case .rebel:     return "🔥"
        case .lover:     return "❤️"
        case .jester:    return "🎭"
        case .innocent:  return "🌸"
        case .magician:  return "✨"
        case .everyman:  return "🤝"
        }
    }
}

struct ArchetypeResult: Equatable, Codable {
    var responses: [Bool]   // count = 24, true=A, false=B

    static let allArchetypes: [FypArchetype] = FypArchetype.allCases

    // 24 pares forçados: 12 arquétipos × 4 aparições cada.
    // Distribuição verificada: cada arquétipo aparece exatamente 4 vezes.
    // 0=explorer,1=creator,2=caregiver,3=ruler,4=sage,5=hero
    // 6=rebel,7=lover,8=jester,9=innocent,10=magician,11=everyman
    static let itemMap: [(Int, Int)] = [
        (0,1),  // 0  explorer vs creator
        (0,2),  // 1  explorer vs caregiver
        (0,6),  // 2  explorer vs rebel
        (0,9),  // 3  explorer vs innocent
        (1,3),  // 4  creator vs ruler
        (1,7),  // 5  creator vs lover
        (1,10), // 6  creator vs magician
        (2,4),  // 7  caregiver vs sage
        (2,8),  // 8  caregiver vs jester
        (2,11), // 9  caregiver vs everyman
        (3,5),  // 10 ruler vs hero
        (3,8),  // 11 ruler vs jester
        (3,11), // 12 ruler vs everyman
        (4,6),  // 13 sage vs rebel
        (4,9),  // 14 sage vs innocent
        (4,11), // 15 sage vs everyman
        (5,7),  // 16 hero vs lover
        (5,10), // 17 hero vs magician
        (5,9),  // 18 hero vs innocent
        (6,7),  // 19 rebel vs lover
        (6,8),  // 20 rebel vs jester
        (7,11), // 21 lover vs everyman
        (8,10), // 22 jester vs magician
        (9,10)  // 23 innocent vs magician
    ]
    // Aparições por arquétipo (4 cada):
    // explorer(0):  0,1,2,3       | creator(1):   0,4,5,6
    // caregiver(2): 1,7,8,9       | ruler(3):     4,10,11,12
    // sage(4):      7,13,14,15    | hero(5):      10,16,17,18
    // rebel(6):     2,13,19,20    | lover(7):     5,16,19,21
    // jester(8):    8,11,20,22    | innocent(9):  3,14,18,23
    // magician(10): 6,17,22,23    | everyman(11): 9,12,15,21

    var scores: [FypArchetype: Int] {
        var counts: [FypArchetype: Int] = [:]
        FypArchetype.allCases.forEach { counts[$0] = 0 }
        for (index, response) in responses.enumerated() {
            guard index < Self.itemMap.count else { break }
            let (iA, iB) = Self.itemMap[index]
            let chosen = response ? Self.allArchetypes[iA] : Self.allArchetypes[iB]
            counts[chosen, default: 0] += 1
        }
        return counts
    }

    var dominantArchetype: FypArchetype {
        scores.max(by: { $0.value < $1.value })?.key ?? .explorer
    }

    var topThree: [FypArchetype] {
        scores.sorted { $0.value > $1.value }.prefix(3).map(\.key)
    }
}

// MARK: Container do Modo Autoconhecimento

struct SelfKnowledgeQuestionnaire: Equatable, Codable {
    var userId: String
    var completedAt: Date?
    var enneagram: EnneagramResult?
    var loveLanguage: LoveLanguageResult?
    var archetype: ArchetypeResult?

    var isEnneagramComplete: Bool { enneagram != nil }

    /// Percentual de conclusão: cada módulo vale 33 pontos (3 módulos × 33 ≈ 99;
    /// quando todos completos retorna 99 — use isFullyComplete para verificar 100%).
    var completionPercentage: Int {
        [enneagram != nil, loveLanguage != nil, archetype != nil]
            .filter { $0 }.count * 33
    }

    var isFullyComplete: Bool {
        enneagram != nil && loveLanguage != nil && archetype != nil
    }
}
