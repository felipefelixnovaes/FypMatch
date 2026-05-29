// QuestionnaireService.swift — FypMatch iOS
// Serviço de questionários de compatibilidade multi-camada

import Foundation
import ComposableArchitecture
import FirebaseFirestore

// MARK: - Modelos inline (TODO: mover para QuestionnaireModels.swift quando o Architecture Squad criar)

// MARK: Schwartz Values

enum SchwartzValue: String, Codable, CaseIterable, Equatable {
    case seguranca      = "seguranca"
    case liberdade      = "liberdade"
    case familia        = "familia"
    case realizacao     = "realizacao"
    case tradicao       = "tradicao"
    case hedonismo      = "hedonismo"
    case benevolencia   = "benevolencia"
    case universalismo  = "universalismo"
    case poder          = "poder"
    case conformidade   = "conformidade"

    var displayName: String {
        switch self {
        case .seguranca:    return "Segurança"
        case .liberdade:    return "Liberdade"
        case .familia:      return "Família"
        case .realizacao:   return "Realização"
        case .tradicao:     return "Tradição"
        case .hedonismo:    return "Hedonismo"
        case .benevolencia: return "Benevolência"
        case .universalismo:return "Universalismo"
        case .poder:        return "Poder"
        case .conformidade: return "Conformidade"
        }
    }

    var emoji: String {
        switch self {
        case .seguranca:    return "🔒"
        case .liberdade:    return "🕊️"
        case .familia:      return "👨‍👩‍👧"
        case .realizacao:   return "🏆"
        case .tradicao:     return "🌿"
        case .hedonismo:    return "🎉"
        case .benevolencia: return "💙"
        case .universalismo:return "🌍"
        case .poder:        return "💎"
        case .conformidade: return "📏"
        }
    }
}

// MARK: Big Five

struct BigFiveResult: Codable, Equatable {
    let openness: Int           // 1-7
    let conscientiousness: Int  // 1-7
    let extraversion: Int       // 1-7
    let agreeableness: Int      // 1-7
    let neuroticism: Int        // 1-7
}

// MARK: Values Result

struct ValuesResult: Codable, Equatable {
    let top3: [SchwartzValue]   // exatamente 3 valores em ordem de prioridade
}

// MARK: Communication Style

enum ConflictStyle: String, Codable, CaseIterable, Equatable {
    case conversa    = "conversa"    // Conversar na hora
    case espera      = "espera"      // Esperar esfriar
    case afasta      = "afasta"      // Me afastar

    // Complementares: conversa+espera (70), espera+afasta (70)
    // Opostos: conversa+afasta (30)
    func compatibility(with other: ConflictStyle) -> Int {
        if self == other { return 100 }
        let pair = Set([self, other])
        if pair == Set([.conversa, .espera]) || pair == Set([.espera, .afasta]) { return 70 }
        return 30
    }
}

enum MessagingFrequency: String, Codable, CaseIterable, Equatable {
    case alta      = "alta"       // Gosto de frequência
    case contextual = "contextual" // Depende do contexto
    case baixa     = "baixa"      // Prefiro espaço

    var level: Int {
        switch self { case .alta: return 2; case .contextual: return 1; case .baixa: return 0 }
    }

    func compatibility(with other: MessagingFrequency) -> Int {
        let diff = abs(self.level - other.level)
        switch diff { case 0: return 100; case 1: return 65; default: return 30 }
    }
}

enum AnxietyWhenSilent: String, Codable, CaseIterable, Equatable {
    case normal   = "normal"
    case umPouco  = "umPouco"
    case muito    = "muito"
}

enum ConversationDepth: String, Codable, CaseIterable, Equatable {
    case direta   = "direta"
    case profunda = "profunda"
    case misto    = "misto"
}

enum ConflictMedium: String, Codable, CaseIterable, Equatable {
    case naHora    = "naHora"
    case depoisEsfria = "depoisEsfria"
    case porEscrito = "porEscrito"
}

struct CommunicationResult: Codable, Equatable {
    let conflictStyle: ConflictStyle
    let messagingFrequency: MessagingFrequency
    let anxietyWhenSilent: AnxietyWhenSilent
    let conversationDepth: ConversationDepth
    let conflictMedium: ConflictMedium
}

// MARK: Routine Style

enum EnergySource: String, Codable, CaseIterable, Equatable {
    case introvertido  = "introvertido"
    case extrovertido  = "extrovertido"
}

enum WeekendStyle: String, Codable, CaseIterable, Equatable {
    case emCasa    = "emCasa"
    case equilibrio = "equilibrio"
    case sairExplorar = "sairExplorar"

    var level: Int {
        switch self { case .emCasa: return 0; case .equilibrio: return 1; case .sairExplorar: return 2 }
    }

    func compatibility(with other: WeekendStyle) -> Int {
        if self == other { return 100 }
        let diff = abs(self.level - other.level)
        return diff == 1 ? 70 : 40
    }
}

enum RoutineVsSpontaneous: String, Codable, CaseIterable, Equatable {
    case rotina      = "rotina"
    case espontaneo  = "espontaneo"
    case depende     = "depende"
}

enum WorkLifeBalance: String, Codable, CaseIterable, Equatable {
    case altaAmbi    = "altaAmbi"
    case equilibrio  = "equilibrio"
    case qualidadeVida = "qualidadeVida"
}

enum HomeNoise: String, Codable, CaseIterable, Equatable {
    case silencio    = "silencio"
    case umPoucoSom  = "umPoucoSom"
    case qualquer    = "qualquer"
}

struct RoutineResult: Codable, Equatable {
    let energySource: EnergySource
    let weekendStyle: WeekendStyle
    let routineVsSpontaneous: RoutineVsSpontaneous
    let workLifeBalance: WorkLifeBalance
    let homeNoise: HomeNoise
}

// MARK: Deal Breakers

enum DealBreaker: String, Codable, CaseIterable, Equatable {
    case fumar                  = "fumar"
    case beberExcessivamente    = "beberExcessivamente"
    case naoQuerFilhos          = "naoQuerFilhos"
    case querFilhos             = "querFilhos"
    case religioesIncompatíveis = "religioesIncompativeis"
    case politicamenteOposto    = "politicamenteOposto"
    case relacionamentoAberto   = "relacionamentoAberto"
    case violenciaAgressividade = "violenciaAgressividade"
    case infidelidade           = "infidelidade"
    case ritmosMuitoDiferentes  = "ritmosMuitoDiferentes"

    var displayName: String {
        switch self {
        case .fumar:                  return "Fumar"
        case .beberExcessivamente:    return "Beber excessivamente"
        case .naoQuerFilhos:          return "Não quer filhos jamais"
        case .querFilhos:             return "Quer filhos (obrigatório)"
        case .religioesIncompatíveis: return "Religiões incompatíveis"
        case .politicamenteOposto:    return "Politicamente muito oposto(a)"
        case .relacionamentoAberto:   return "Relacionamento aberto"
        case .violenciaAgressividade: return "Violência/agressividade"
        case .infidelidade:           return "Infidelidade"
        case .ritmosMuitoDiferentes:  return "Ritmos de vida muito diferentes"
        }
    }

    var emoji: String {
        switch self {
        case .fumar:                  return "🚬"
        case .beberExcessivamente:    return "🍺"
        case .naoQuerFilhos:          return "👶"
        case .querFilhos:             return "👶"
        case .religioesIncompatíveis: return "⛪"
        case .politicamenteOposto:    return "🗳️"
        case .relacionamentoAberto:   return "💞"
        case .violenciaAgressividade: return "👊"
        case .infidelidade:           return "💔"
        case .ritmosMuitoDiferentes:  return "⚡"
        }
    }
}

// MARK: - UserQuestionnaire (modelo principal)

struct UserQuestionnaire: Codable, Equatable {
    let userId: String
    var bigFive: BigFiveResult?
    var values: ValuesResult?
    var communication: CommunicationResult?
    var routine: RoutineResult?
    var dealBreakers: [DealBreaker]
    var completedAt: Date?

    init(userId: String) {
        self.userId = userId
        self.dealBreakers = []
    }

    /// Percentual de preenchimento (0-100)
    var completionPercent: Int {
        var count = 0
        if bigFive != nil        { count += 1 }
        if values != nil         { count += 1 }
        if communication != nil  { count += 1 }
        if routine != nil        { count += 1 }
        count += 1 // deal-breakers sempre preenchidos (pode ser vazio)
        return count * 20
    }
}

// MARK: - QuestionnaireCompatibility

struct QuestionnaireCompatibility: Equatable {
    let overall: Int                    // 0-100
    let layer1Score: Int                // valores+comunicação+vínculo (50%)
    let layer2Score: Int                // BigFive+rotina (35%)
    let layer3Score: Int                // hobbies+leve (15%)
    let dealBreakerConflict: Bool       // true = filtro obrigatório falhou
    let explanation: String             // texto gerado em pt-BR
    let highlights: [String]            // pontos positivos
    let differences: [String]           // diferenças a conversar
}

// MARK: - QuestionnaireService

struct QuestionnaireService {
    var loadQuestionnaire: (_ userId: String) async throws -> UserQuestionnaire?
    var saveQuestionnaire: (_ userId: String, _ q: UserQuestionnaire) async throws -> Void
    var calculateCompatibility: (_ a: UserQuestionnaire, _ b: UserQuestionnaire) -> QuestionnaireCompatibility

    // MARK: Deep Mode

    /// Salva o Modo Profundo no Firestore (coleção "deepQuestionnaires")
    var saveDeepMode: (_ q: DeepModeQuestionnaire) async throws -> Void

    /// Carrega o Modo Profundo do Firestore para um userId
    var loadDeepMode: (_ userId: String) async throws -> DeepModeQuestionnaire?

    /// Calcula compatibilidade enriquecida quando ambos têm Modo Profundo.
    /// Se apenas um tiver, usa dados de quem tem + proxy de quem não tem.
    /// Quando skA/skB forem fornecidos, inclui Camada 3 de Self-Knowledge (15%).
    var calculateCompatibilityFull: (
        _ quickA: UserQuestionnaire, _ quickB: UserQuestionnaire,
        _ deepA: DeepModeQuestionnaire?, _ deepB: DeepModeQuestionnaire?,
        _ skA: SelfKnowledgeQuestionnaire?, _ skB: SelfKnowledgeQuestionnaire?
    ) -> QuestionnaireCompatibility

    // MARK: Sprint 7a — Self-Knowledge (Eneagrama)

    /// Salva o questionário de autoconhecimento no Firestore (coleção "selfKnowledge").
    /// Usa merge: true para preservar outros campos do documento.
    var saveSelfKnowledge: (_ q: SelfKnowledgeQuestionnaire) async throws -> Void

    /// Carrega o questionário de autoconhecimento do Firestore para um userId.
    var loadSelfKnowledge: (_ userId: String) async throws -> SelfKnowledgeQuestionnaire?

    // MARK: Sprint 7b — Self-Knowledge compatibility (Camada 3 — 0-100)

    /// Calcula a compatibilidade de autoconhecimento entre dois usuários.
    /// Eneagrama: 20% | Linguagem do Cuidado: 40% | Arquétipo: 40%
    /// Retorna 60 (neutro) quando qualquer módulo estiver ausente em ambos.
    var calculateSelfKnowledgeCompatibility: (
        _ qA: SelfKnowledgeQuestionnaire?,
        _ qB: SelfKnowledgeQuestionnaire?
    ) -> Int
}

// MARK: - Lógica de compatibilidade multi-camada

private func computeCompatibility(_ a: UserQuestionnaire, _ b: UserQuestionnaire) -> QuestionnaireCompatibility {

    // ── Camada 0: Deal-breakers (filtro obrigatório) ─────────────────────────
    let dbA = Set(a.dealBreakers)
    let dbB = Set(b.dealBreakers)

    // Verifica conflito cruzado
    // "naoQuerFilhos" de A vs "querFilhos" de B (e vice-versa) são conflitantes
    let childConflict = (dbA.contains(.naoQuerFilhos) && dbB.contains(.querFilhos))
                      || (dbA.contains(.querFilhos) && dbB.contains(.naoQuerFilhos))
    let hasCommonDB = !dbA.intersection(dbB).isEmpty || childConflict

    if hasCommonDB {
        return QuestionnaireCompatibility(
            overall: 0,
            layer1Score: 0,
            layer2Score: 0,
            layer3Score: 0,
            dealBreakerConflict: true,
            explanation: "Há um ou mais pontos inaceitáveis em conflito entre vocês. Uma conexão saudável exige alinhamento nesses fundamentos.",
            highlights: [],
            differences: ["Deal-breaker identificado — incompatibilidade de base"]
        )
    }

    // ── Camada 1a: Valores (peso 25%) ────────────────────────────────────────
    let valuesScore: Int
    var highlights: [String] = []
    var differences: [String] = []

    if let vA = a.values, let vB = b.values {
        let setA = Set(vA.top3)
        let setB = Set(vB.top3)
        let intersection = setA.intersection(setB).count
        let union = setA.union(setB).count
        valuesScore = union > 0 ? Int(Double(intersection) / Double(union) * 100) : 50

        if valuesScore >= 70 {
            let shared = setA.intersection(setB).map { $0.displayName }.joined(separator: ", ")
            highlights.append("Alta compatibilidade de valores: \(shared)")
        } else if valuesScore < 40 {
            differences.append("Prioridades de vida diferentes — vale explorar o que cada um busca")
        }
    } else {
        valuesScore = 50
    }

    // ── Camada 1b: Comunicação (peso 10%) ────────────────────────────────────
    let commScore: Int
    if let cA = a.communication, let cB = b.communication {
        let s1 = cA.conflictStyle.compatibility(with: cB.conflictStyle)
        let s2 = cA.messagingFrequency.compatibility(with: cB.messagingFrequency)

        // anxietyWhenSilent: mesmos = 100, diferença de 1 nível = 65, 2 níveis = 30
        let anxLevels: [AnxietyWhenSilent: Int] = [.normal: 0, .umPouco: 1, .muito: 2]
        let anxDiff = abs((anxLevels[cA.anxietyWhenSilent] ?? 0) - (anxLevels[cB.anxietyWhenSilent] ?? 0))
        let s3 = anxDiff == 0 ? 100 : (anxDiff == 1 ? 65 : 30)

        // conversationDepth: mesmos = 100, misto com qualquer = 70, opostos = 40
        let s4: Int
        if cA.conversationDepth == cB.conversationDepth { s4 = 100 }
        else if cA.conversationDepth == .misto || cB.conversationDepth == .misto { s4 = 70 }
        else { s4 = 40 }

        // conflictMedium: mesmos = 100, diferença = 60
        let s5 = cA.conflictMedium == cB.conflictMedium ? 100 : 60

        commScore = (s1 + s2 + s3 + s4 + s5) / 5

        if s1 < 50 {
            differences.append("Estilos de comunicação em conflito — um prefere conversar na hora, o outro prefere esfriar")
        }
        if s2 >= 80 {
            highlights.append("Ritmo de mensagens alinhado")
        }
    } else {
        commScore = 50
    }

    // ── Camada 1 total (layer1Score): valores 25% + comunicação 10% = 35% do total
    // Normalizamos como porcentagem interna aqui e depois aplicamos os pesos
    let layer1Raw = Int(Double(valuesScore) * (25.0 / 35.0) + Double(commScore) * (10.0 / 35.0))
    let layer1Score = layer1Raw

    // ── Camada 2a: Big Five (peso 20%) ───────────────────────────────────────
    let bigFiveScore: Int
    if let bA = a.bigFive, let bB = b.bigFive {
        // Neuroticismo: baixo+baixo=100, alto+alto=60, alto+baixo=40
        let neuroScore: Int
        let neuroThreshold = 4
        switch (bA.neuroticism >= neuroThreshold, bB.neuroticism >= neuroThreshold) {
        case (false, false): neuroScore = 100
        case (true, true):   neuroScore = 60
        default:             neuroScore = 40
        }

        // Conscienciosidade: similar é melhor (distância normalizada)
        let consScore = Int((1.0 - abs(Double(bA.conscientiousness) - Double(bB.conscientiousness)) / 6.0) * 100)

        // Extroversão: distância normalizada
        let extraScore = Int((1.0 - abs(Double(bA.extraversion) - Double(bB.extraversion)) / 6.0) * 100)

        // Amabilidade: similar é melhor
        let agreeScore = Int((1.0 - abs(Double(bA.agreeableness) - Double(bB.agreeableness)) / 6.0) * 100)

        // Abertura: similar é bom; divergência também pode ser interessante
        let openScore = Int((1.0 - abs(Double(bA.openness) - Double(bB.openness)) / 6.0) * 100)

        bigFiveScore = (neuroScore + consScore + extraScore + agreeScore + openScore) / 5

        if neuroScore < 50 {
            differences.append("Um de vocês tende a ser mais ansioso — requer atenção emocional mútua")
        }
        if consScore >= 80 {
            highlights.append("Organização e responsabilidade muito alinhadas")
        }
    } else {
        bigFiveScore = 50
    }

    // ── Camada 2b: Rotina (peso 15%) ─────────────────────────────────────────
    let routineScore: Int
    if let rA = a.routine, let rB = b.routine {
        // energySource
        let energyScore: Int
        switch (rA.energySource, rB.energySource) {
        case (.introvertido, .introvertido): energyScore = 100
        case (.extrovertido, .extrovertido): energyScore = 90
        default: energyScore = 50
        }

        // weekendStyle
        let weekScore = rA.weekendStyle.compatibility(with: rB.weekendStyle)

        // routineVsSpontaneous: mesmos = 100, depende+qualquer = 70, opostos = 50
        let rvsScore: Int
        if rA.routineVsSpontaneous == rB.routineVsSpontaneous { rvsScore = 100 }
        else if rA.routineVsSpontaneous == .depende || rB.routineVsSpontaneous == .depende { rvsScore = 70 }
        else { rvsScore = 50 }

        // workLifeBalance: mesmos = 100, adjacentes = 70, opostos = 40
        let wlbLevels: [WorkLifeBalance: Int] = [.altaAmbi: 2, .equilibrio: 1, .qualidadeVida: 0]
        let wlbDiff = abs((wlbLevels[rA.workLifeBalance] ?? 1) - (wlbLevels[rB.workLifeBalance] ?? 1))
        let wlbScore = wlbDiff == 0 ? 100 : (wlbDiff == 1 ? 70 : 40)

        // homeNoise: mesmos = 100, adjacentes = 70, opostos = 40
        let noiseLevels: [HomeNoise: Int] = [.silencio: 0, .umPoucoSom: 1, .qualquer: 2]
        let noiseDiff = abs((noiseLevels[rA.homeNoise] ?? 1) - (noiseLevels[rB.homeNoise] ?? 1))
        let noiseScore = noiseDiff == 0 ? 100 : (noiseDiff == 1 ? 70 : 40)

        routineScore = (energyScore + weekScore + rvsScore + wlbScore + noiseScore) / 5

        if energyScore < 60 {
            differences.append("Ritmos sociais diferentes: um se recarrega no silêncio, o outro nas pessoas")
        }
        if weekScore >= 80 {
            highlights.append("Estilos de fim de semana compatíveis")
        }
    } else {
        routineScore = 50
    }

    // ── Camada 2 total
    let layer2Score = Int(Double(bigFiveScore) * (20.0 / 35.0) + Double(routineScore) * (15.0 / 35.0))

    // ── Camada 3 — hobbies/leve (15%) — placeholder até integração com User
    let layer3Score = 60 // neutro quando não há dados extras

    // ── Score final ponderado ─────────────────────────────────────────────────
    // Camada 1: 35%, Camada 2: 35% (Big Five 20% + rotina 15%), Camada 3: 15%
    // Ajuste: valores(25) + comm(10) + bigFive(20) + rotina(15) + layer3(15) = 85%
    // O 15% restante vem do layer3 fixo
    let overall = min(100, max(0,
        Int(Double(valuesScore) * 0.25
          + Double(commScore)   * 0.10
          + Double(bigFiveScore)* 0.20
          + Double(routineScore)* 0.15
          + Double(layer3Score) * 0.15)
    ))

    // ── Texto explicativo ─────────────────────────────────────────────────────
    let explanation = buildExplanation(
        overall: overall,
        valuesScore: valuesScore,
        commScore: commScore,
        bigFiveScore: bigFiveScore,
        routineScore: routineScore,
        highlights: highlights,
        differences: differences
    )

    return QuestionnaireCompatibility(
        overall: overall,
        layer1Score: layer1Score,
        layer2Score: layer2Score,
        layer3Score: layer3Score,
        dealBreakerConflict: false,
        explanation: explanation,
        highlights: highlights,
        differences: differences
    )
}

private func buildExplanation(
    overall: Int,
    valuesScore: Int,
    commScore: Int,
    bigFiveScore: Int,
    routineScore: Int,
    highlights: [String],
    differences: [String]
) -> String {

    var parts: [String] = []

    // Abertura baseada no score geral
    switch overall {
    case 80...100:
        parts.append("Vocês têm uma compatibilidade muito forte —")
    case 60..<80:
        parts.append("Há uma boa base entre vocês —")
    case 40..<60:
        parts.append("Existem pontos em comum, mas também diferenças relevantes —")
    default:
        parts.append("A compatibilidade é baixa —")
    }

    // Destaque de valores
    if valuesScore >= 70 {
        parts.append("os valores que guiam a vida de vocês estão muito alinhados.")
    } else if valuesScore < 40 {
        parts.append("as prioridades de vida de vocês divergem bastante.")
    } else {
        parts.append("alguns valores são compartilhados, outros bem diferentes.")
    }

    // Comunicação
    if commScore >= 70 {
        parts.append("O estilo de comunicação é compatível, o que ajuda muito no dia a dia.")
    } else if commScore < 45 {
        parts.append("Os estilos de comunicação são bem distintos — requer atenção e paciência.")
    }

    // Rotina
    if routineScore >= 70 {
        parts.append("O ritmo de vida de vocês combina bem.")
    } else if routineScore < 40 {
        parts.append("Os ritmos de vida são bem diferentes — isso merece uma conversa honesta.")
    }

    // Diferenças
    if let firstDiff = differences.first {
        parts.append("\(firstDiff).")
    }

    return parts.joined(separator: " ")
}

// MARK: - Compatibilidade enriquecida com Modo Profundo

/// Compatibilidade ECR-RS: matriz de estilo de apego → score 0-100
private func ecrrsCompatibility(_ a: AttachmentStyle, _ b: AttachmentStyle) -> Int {
    if a == b {
        switch a {
        case .secure:       return 100
        case .anxious:      return 50
        case .avoidant:     return 55
        case .disorganized: return 40
        }
    }
    let pair = Set([a, b])
    if pair == Set([.secure, .anxious])   { return 70 }
    if pair == Set([.secure, .avoidant])  { return 65 }
    if pair.contains(.disorganized)       { return 40 }
    // anxious + avoidant
    return 45
}

/// Distância euclidiana normalizada entre dois perfis Big Five expressos em 0–100 por fator.
/// Aceita 5 pares (extraversão, amabilidade, conscienciosidade, neuroticismo, abertura).
private func bigFiveDistance5(
    _ a: (ext: Double, agr: Double, con: Double, neu: Double, ope: Double),
    _ b: (ext: Double, agr: Double, con: Double, neu: Double, ope: Double)
) -> Int {
    let pairs: [(Double, Double)] = [
        (a.ext, b.ext), (a.agr, b.agr), (a.con, b.con), (a.neu, b.neu), (a.ope, b.ope)
    ]
    let sumSq = pairs.reduce(0.0) { acc, p in acc + pow(p.0 - p.1, 2) }
    let maxDist = sqrt(5.0 * 10000.0)  // sqrt(5 * 100^2)
    let dist = sqrt(sumSq)
    return Int((1.0 - dist / maxDist) * 100.0)
}

/// Converte IPIP20Result para tupla de scores normalizados 0–100
/// Usa as propriedades computadas diretamente de IPIP20Result (escala 1–5 → 0–100)
private func ipip20ToTuple(_ r: IPIP20Result) -> (Double, Double, Double, Double, Double) {
    func toPercent(_ v: Double) -> Double { (v - 1.0) / 4.0 * 100.0 }
    return (
        toPercent(r.extroversion),
        toPercent(r.agreeableness),
        toPercent(r.conscientiousness),
        toPercent(r.neuroticism),
        toPercent(r.openness)
    )
}

/// Converte BigFiveResult inline (escala 1–7, Int) para tupla 0–100
private func inlineBigFiveToTuple(o: Int, c: Int, e: Int, a: Int, n: Int) -> (Double, Double, Double, Double, Double) {
    func norm(_ v: Int) -> Double { (Double(v) - 1.0) / 6.0 * 100.0 }
    return (norm(e), norm(a), norm(c), norm(n), norm(o))
}

/// Versão completa da compatibilidade quando Modo Profundo e/ou Self-Knowledge estão disponíveis.
/// Quando skA/skB forem fornecidos, inclui Camada 3 de Self-Knowledge com peso de 15%,
/// reduzindo os demais pesos proporcionalmente (× 0.85).
private func computeCompatibilityFull(
    quick qA: UserQuestionnaire, _ qB: UserQuestionnaire,
    deep dA: DeepModeQuestionnaire?, _ dB: DeepModeQuestionnaire?,
    sk skA: SelfKnowledgeQuestionnaire? = nil, _ skB: SelfKnowledgeQuestionnaire? = nil
) -> QuestionnaireCompatibility {

    // Delegamos a lógica de deal-breakers e camadas básicas ao motor existente
    var base = computeCompatibility(qA, qB)

    // Se nenhum dos dois tem Modo Profundo, retorna compatibilidade base
    guard dA != nil || dB != nil else { return base }

    var highlights = base.highlights
    var differences = base.differences

    // ── Camada 1 enriquecida: Apego ECR-RS (15%) ─────────────────────────────
    let attachScore: Int
    if let da = dA, let db = dB,
       let ecrA = da.ecrrs, let ecrB = db.ecrrs {
        // Ambos têm ECR-RS: usa o score real
        attachScore = ecrrsCompatibility(ecrA.attachmentStyle, ecrB.attachmentStyle)

        if attachScore >= 80 {
            highlights.append("Estilos de apego muito compatíveis (\(ecrA.attachmentStyle.displayName) + \(ecrB.attachmentStyle.displayName))")
        } else if attachScore < 50 {
            differences.append("Estilos de apego divergentes (\(ecrA.attachmentStyle.displayName) × \(ecrB.attachmentStyle.displayName)) — requer comunicação consciente")
        }
    } else if let da = dA, let ecrA = da.ecrrs {
        // Só A tem ECR-RS: estima com proxy de ansiedade do Modo Rápido
        let proxyScore = ecrA.attachmentStyle == .secure ? 70 : 55
        attachScore = proxyScore
    } else if let db = dB, let ecrB = db.ecrrs {
        let proxyScore = ecrB.attachmentStyle == .secure ? 70 : 55
        attachScore = proxyScore
    } else {
        attachScore = 60 // proxy neutro
    }

    // ── Camada 2 enriquecida: Personalidade IPIP-20 (20%) ────────────────────
    // Usa bigFiveDistance5() com tuplas para evitar ambiguidade de tipo BigFiveResult
    let bigFiveDeepScore: Int
    if let da = dA, let db = dB,
       let ipA = da.ipip20, let ipB = db.ipip20 {
        bigFiveDeepScore = bigFiveDistance5(ipip20ToTuple(ipA), ipip20ToTuple(ipB))

        if bigFiveDeepScore >= 80 {
            highlights.append("Perfis de personalidade (IPIP-20) muito próximos")
        } else if bigFiveDeepScore < 45 {
            differences.append("Perfis de personalidade com diferenças marcadas — complementaridade possível")
        }
    } else if let da = dA, let ipA = da.ipip20 {
        // Usa IPIP-20 de A vs BigFive inline do Modo Rápido de B (se disponível)
        if let bfB = qB.bigFive {
            let tupleB = inlineBigFiveToTuple(o: bfB.openness, c: bfB.conscientiousness,
                                              e: bfB.extraversion, a: bfB.agreeableness, n: bfB.neuroticism)
            bigFiveDeepScore = bigFiveDistance5(ipip20ToTuple(ipA), tupleB)
        } else {
            bigFiveDeepScore = base.layer2Score
        }
    } else if let db = dB, let ipB = db.ipip20 {
        if let bfA = qA.bigFive {
            let tupleA = inlineBigFiveToTuple(o: bfA.openness, c: bfA.conscientiousness,
                                              e: bfA.extraversion, a: bfA.agreeableness, n: bfA.neuroticism)
            bigFiveDeepScore = bigFiveDistance5(tupleA, ipip20ToTuple(ipB))
        } else {
            bigFiveDeepScore = base.layer2Score
        }
    } else {
        bigFiveDeepScore = base.layer2Score
    }

    // ── Recalcula score final com os dados enriquecidos ───────────────────────
    // Pesos ajustados quando Deep Mode disponível:
    // valores(25%) + comunicação(10%) + apego(15%) + personalidade(20%) + rotina(15%) + layer3(15%) = 100%
    let valoresScore: Int
    if let vA = qA.values, let vB = qB.values {
        // Modelo inline usa .top3
        let setA = Set(vA.top3)
        let setB = Set(vB.top3)
        let inter = setA.intersection(setB).count
        let union = setA.union(setB).count
        valoresScore = union > 0 ? Int(Double(inter) / Double(union) * 100) : 50
    } else if let da = dA, let db = dB,
              let pvqA = da.pvq21, let pvqB = db.pvq21 {
        // Usa PVQ-21 quando disponível (SchwartzValue do QuestionnaireModels)
        let topA = pvqA.topValues
        let topB = pvqB.topValues
        let inter = Set(topA).intersection(Set(topB)).count
        let union = Set(topA).union(Set(topB)).count
        valoresScore = union > 0 ? Int(Double(inter) / Double(union) * 100) : 50
    } else {
        valoresScore = base.layer1Score
    }

    let commScore: Int
    if let cA = qA.communication, let cB = qB.communication {
        let s1 = cA.conflictStyle.compatibility(with: cB.conflictStyle)
        let s2 = cA.messagingFrequency.compatibility(with: cB.messagingFrequency)
        commScore = (s1 + s2) / 2
    } else {
        commScore = 60
    }

    let routineScore: Int
    if let rA = qA.routine, let rB = qB.routine {
        let energyScore = rA.energySource == rB.energySource ? 90 : 50
        let weekScore: Int
        switch (rA.weekendStyle, rB.weekendStyle) {
        case let (x, y) where x == y: weekScore = 100
        default: weekScore = 65
        }
        routineScore = (energyScore + weekScore) / 2
    } else {
        routineScore = base.layer2Score
    }

    let layer3Score = 60

    // Camada 3 — Self-Knowledge (15% quando disponível, reduzindo outras camadas 15% proporcionalmente)
    // Pesos base sem SK: valores(25) comm(10) attach(15) bf(20) rotina(15) layer3(15) = 100%
    // Com SK: cada peso base × 0.85, SK = 15%
    let hasSelfKnowledge = skA != nil || skB != nil
    let skScore = hasSelfKnowledge ? computeSelfKnowledgeCompatibility(skA, skB) : layer3Score

    let newOverall: Int
    if hasSelfKnowledge {
        newOverall = min(100, max(0,
            Int(Double(valoresScore)    * 0.2125
              + Double(commScore)       * 0.085
              + Double(attachScore)     * 0.1275
              + Double(bigFiveDeepScore)* 0.17
              + Double(routineScore)    * 0.1275
              + Double(layer3Score)     * 0.1275
              + Double(skScore)         * 0.15)
        ))
    } else {
        newOverall = min(100, max(0,
            Int(Double(valoresScore)    * 0.25
              + Double(commScore)       * 0.10
              + Double(attachScore)     * 0.15
              + Double(bigFiveDeepScore)* 0.20
              + Double(routineScore)    * 0.15
              + Double(layer3Score)     * 0.15)
        ))
    }

    // Deal-breaker detectado → mantém zero
    guard !base.dealBreakerConflict else { return base }

    let layer1New = Int(Double(valoresScore) * (25.0 / 35.0) + Double(commScore) * (10.0 / 35.0))
    let layer2New = Int(Double(bigFiveDeepScore) * (20.0 / 35.0) + Double(routineScore) * (15.0 / 35.0))

    let explanation = buildExplanation(
        overall: newOverall,
        valuesScore: valoresScore,
        commScore: commScore,
        bigFiveScore: bigFiveDeepScore,
        routineScore: routineScore,
        highlights: highlights,
        differences: differences
    )

    return QuestionnaireCompatibility(
        overall: newOverall,
        layer1Score: layer1New,
        layer2Score: layer2New,
        layer3Score: layer3Score,
        dealBreakerConflict: false,
        explanation: explanation,
        highlights: highlights,
        differences: differences
    )
}

// MARK: - Compatibilidade de Autoconhecimento (Sprint 7b)

/// Calcula o score de compatibilidade de autoconhecimento (0-100).
/// Pesos: Eneagrama 20% | Linguagem do Cuidado 40% | Arquétipo 40%
private func computeSelfKnowledgeCompatibility(
    _ qA: SelfKnowledgeQuestionnaire?,
    _ qB: SelfKnowledgeQuestionnaire?
) -> Int {
    guard let qA, let qB else { return 60 }

    // Eneagrama (20%)
    let ennScore: Int
    if let eA = qA.enneagram, let eB = qB.enneagram {
        let tA = eA.dominantType
        let tB = eB.dominantType
        ennScore = (tA.naturalPartners.contains(tB) || tB.naturalPartners.contains(tA)) ? 100
                 : tA == tB ? 70 : 50
    } else {
        ennScore = 60
    }

    // Linguagem do Cuidado (40%)
    let langScore: Int
    if let lA = qA.loveLanguage, let lB = qB.loveLanguage {
        langScore = lA.compatibility(with: lB)
    } else {
        langScore = 60
    }

    // Arquétipo (40%)
    let archetypeScore: Int
    if let aA = qA.archetype, let aB = qB.archetype {
        archetypeScore = aA.dominantArchetype == aB.dominantArchetype ? 100 : 60
    } else {
        archetypeScore = 60
    }

    return Int(Double(ennScore) * 0.20
             + Double(langScore) * 0.40
             + Double(archetypeScore) * 0.40)
}

// MARK: - Live Value (Firestore)

extension QuestionnaireService: DependencyKey {

    static let liveValue: QuestionnaireService = {
        let decoder: JSONDecoder = {
            let d = JSONDecoder()
            d.dateDecodingStrategy = .millisecondsSince1970
            return d
        }()
        let encoder: JSONEncoder = {
            let e = JSONEncoder()
            e.dateEncodingStrategy = .millisecondsSince1970
            return e
        }()

        return QuestionnaireService(
            loadQuestionnaire: { userId in
                let db = Firestore.firestore()
                let doc = try await db.collection("questionnaires").document(userId).getDocument()
                guard doc.exists, let data = doc.data() else { return nil }
                let jsonData = try JSONSerialization.data(withJSONObject: data)
                return try decoder.decode(UserQuestionnaire.self, from: jsonData)
            },
            saveQuestionnaire: { userId, questionnaire in
                let db = Firestore.firestore()
                let data = try encoder.encode(questionnaire)
                let dict = try JSONSerialization.jsonObject(with: data) as? [String: Any] ?? [:]
                try await db.collection("questionnaires").document(userId).setData(dict, merge: true)
            },
            calculateCompatibility: { a, b in
                computeCompatibility(a, b)
            },
            saveDeepMode: { questionnaire in
                let db = Firestore.firestore()
                let data = try encoder.encode(questionnaire)
                let dict = try JSONSerialization.jsonObject(with: data) as? [String: Any] ?? [:]
                try await db.collection("deepQuestionnaires").document(questionnaire.userId).setData(dict, merge: true)
            },
            loadDeepMode: { userId in
                let db = Firestore.firestore()
                let doc = try await db.collection("deepQuestionnaires").document(userId).getDocument()
                guard doc.exists, let data = doc.data() else { return nil }
                let jsonData = try JSONSerialization.data(withJSONObject: data)
                return try decoder.decode(DeepModeQuestionnaire.self, from: jsonData)
            },
            calculateCompatibilityFull: { qA, qB, dA, dB, skA, skB in
                computeCompatibilityFull(quick: qA, qB, deep: dA, dB, sk: skA, skB)
            },
            saveSelfKnowledge: { questionnaire in
                let db = Firestore.firestore()
                let data = try encoder.encode(questionnaire)
                let dict = try JSONSerialization.jsonObject(with: data) as? [String: Any] ?? [:]
                try await db.collection("selfKnowledge").document(questionnaire.userId).setData(dict, merge: true)
            },
            loadSelfKnowledge: { userId in
                let db = Firestore.firestore()
                let doc = try await db.collection("selfKnowledge").document(userId).getDocument()
                guard doc.exists, let data = doc.data() else { return nil }
                let jsonData = try JSONSerialization.data(withJSONObject: data)
                return try decoder.decode(SelfKnowledgeQuestionnaire.self, from: jsonData)
            },
            calculateSelfKnowledgeCompatibility: { qA, qB in
                computeSelfKnowledgeCompatibility(qA, qB)
            }
        )
    }()

    static let testValue = QuestionnaireService(
        loadQuestionnaire: { _ in nil },
        saveQuestionnaire: { _, _ in },
        calculateCompatibility: { _, _ in
            QuestionnaireCompatibility(
                overall: 75,
                layer1Score: 70,
                layer2Score: 80,
                layer3Score: 60,
                dealBreakerConflict: false,
                explanation: "Compatibilidade de teste.",
                highlights: ["Valores alinhados"],
                differences: []
            )
        },
        saveDeepMode: { _ in },
        loadDeepMode: { _ in nil },
        calculateCompatibilityFull: { _, _, _, _, _, _ in
            QuestionnaireCompatibility(
                overall: 82,
                layer1Score: 75,
                layer2Score: 85,
                layer3Score: 60,
                dealBreakerConflict: false,
                explanation: "Compatibilidade enriquecida de teste.",
                highlights: ["Estilos de apego compatíveis", "Valores alinhados"],
                differences: []
            )
        },
        saveSelfKnowledge: { _ in },
        loadSelfKnowledge: { _ in nil },
        calculateSelfKnowledgeCompatibility: { _, _ in 75 }
    )
}

extension DependencyValues {
    var questionnaireService: QuestionnaireService {
        get { self[QuestionnaireService.self] }
        set { self[QuestionnaireService.self] = newValue }
    }
}
