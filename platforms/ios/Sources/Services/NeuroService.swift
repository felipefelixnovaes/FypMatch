// NeuroService.swift — FypMatch iOS
// Modelos de neurodiversidade e TCA dependency para Firestore

import Foundation
import FirebaseFirestore
import ComposableArchitecture

// MARK: - NeuroProfile

struct NeuroProfile: Codable, Equatable {
    var preferences: NeuroPreferences
    var accommodations: [AccommodationType]
    var neurodiversityTypes: [NeurodiversityType]
    var sensoryProfile: SensoryProfile

    init(
        preferences: NeuroPreferences = NeuroPreferences(),
        accommodations: [AccommodationType] = [],
        neurodiversityTypes: [NeurodiversityType] = [],
        sensoryProfile: SensoryProfile = SensoryProfile()
    ) {
        self.preferences = preferences
        self.accommodations = accommodations
        self.neurodiversityTypes = neurodiversityTypes
        self.sensoryProfile = sensoryProfile
    }
}

// MARK: - NeuroPreferences

struct NeuroPreferences: Codable, Equatable {
    var needsClearCommunication: Bool = false
    var prefersDirectness: Bool = false
    var sensitiveToCriticism: Bool = false
    var needsRoutine: Bool = false
    var prefersTextOverVoice: Bool = false
    var needsProcessingTime: Bool = false
    var prefersStructuredConversation: Bool = false
}

// MARK: - AccommodationType

enum AccommodationType: String, Codable, CaseIterable, Equatable {
    case simplifiedInterface = "simplified_interface"
    case reducedStimulation  = "reduced_stimulation"
    case clearInstructions   = "clear_instructions"
    case sensoryFiltering    = "sensory_filtering"
    case reducedAnimations   = "reduced_animations"

    var displayName: String {
        switch self {
        case .simplifiedInterface:  return "Interface Simplificada"
        case .reducedStimulation:   return "Estímulo Reduzido"
        case .clearInstructions:    return "Instruções Claras"
        case .sensoryFiltering:     return "Filtro Sensorial"
        case .reducedAnimations:    return "Animações Reduzidas"
        }
    }

    var description: String {
        switch self {
        case .simplifiedInterface:
            return "Menos elementos visuais, foco no essencial"
        case .reducedStimulation:
            return "Cores e sons mais suaves, menos distrações"
        case .clearInstructions:
            return "Mensagens diretas e passos bem definidos"
        case .sensoryFiltering:
            return "Controle sobre notificações e alertas sonoros"
        case .reducedAnimations:
            return "Transições mínimas para menos sobrecarga visual"
        }
    }

    var icon: String {
        switch self {
        case .simplifiedInterface:  return "rectangle.grid.1x2"
        case .reducedStimulation:   return "moon.stars"
        case .clearInstructions:    return "list.bullet.clipboard"
        case .sensoryFiltering:     return "ear.trianglebadge.exclamationmark"
        case .reducedAnimations:    return "hand.raised.slash"
        }
    }
}

// MARK: - NeurodiversityType

enum NeurodiversityType: String, Codable, CaseIterable, Equatable {
    case adhd             = "adhd"
    case autism           = "autism"
    case dyslexia         = "dyslexia"
    case anxiety          = "anxiety"
    case ocd              = "ocd"
    case bipolar          = "bipolar"
    case depression       = "depression"
    case sensoryProcessing = "sensory_processing"
    case other            = "other"

    var displayName: String {
        switch self {
        case .adhd:              return "TDAH"
        case .autism:            return "Autismo"
        case .dyslexia:          return "Dislexia"
        case .anxiety:           return "Ansiedade"
        case .ocd:               return "TOC"
        case .bipolar:           return "Bipolaridade"
        case .depression:        return "Depressão"
        case .sensoryProcessing: return "Processamento Sensorial"
        case .other:             return "Outro"
        }
    }

    var emoji: String {
        switch self {
        case .adhd:              return "⚡️"
        case .autism:            return "🌀"
        case .dyslexia:          return "📖"
        case .anxiety:           return "🌊"
        case .ocd:               return "🔄"
        case .bipolar:           return "🌗"
        case .depression:        return "🌧️"
        case .sensoryProcessing: return "🔊"
        case .other:             return "✨"
        }
    }
}

// MARK: - SensoryProfile

struct SensoryProfile: Codable, Equatable {
    var reduceBrightness: Bool  = false
    var reduceBusyness: Bool    = false
    var reduceAnimations: Bool  = false
    var increaseContrast: Bool  = false
}

// MARK: - NeuroService

struct NeuroService {
    var loadProfile: (_ userId: String) async throws -> NeuroProfile?
    var saveProfile: (_ userId: String, _ profile: NeuroProfile) async throws -> Void
}

// MARK: - Live Value

extension NeuroService {
    static let liveValue: NeuroService = {
        let db = Firestore.firestore()
        let collection = "neuroProfiles"

        return NeuroService(
            loadProfile: { userId in
                let doc = try await db.collection(collection).document(userId).getDocument()
                guard doc.exists, let data = doc.data() else { return nil }
                return try Firestore.Decoder().decode(NeuroProfile.self, from: data)
            },
            saveProfile: { userId, profile in
                let data = try Firestore.Encoder().encode(profile)
                try await db.collection(collection).document(userId).setData(data, merge: true)
            }
        )
    }()
}

// MARK: - Preview / Test Value

extension NeuroService {
    static let previewValue = NeuroService(
        loadProfile: { _ in
            NeuroProfile(
                preferences: NeuroPreferences(needsClearCommunication: true, prefersDirectness: true),
                accommodations: [.simplifiedInterface, .reducedAnimations],
                neurodiversityTypes: [.adhd, .anxiety],
                sensoryProfile: SensoryProfile(reduceAnimations: true)
            )
        },
        saveProfile: { _, _ in }
    )
}

// MARK: - TCA Dependency Key

private enum NeuroServiceKey: DependencyKey {
    static let liveValue: NeuroService = .liveValue
    static let previewValue: NeuroService = .previewValue
    static let testValue: NeuroService = NeuroService(
        loadProfile: { _ in nil },
        saveProfile: { _, _ in }
    )
}

extension DependencyValues {
    var neuroService: NeuroService {
        get { self[NeuroServiceKey.self] }
        set { self[NeuroServiceKey.self] = newValue }
    }
}
