// AccessCodeService.swift — FypMatch iOS
// Serviço de validação e resgate de códigos de acesso beta

import Foundation
import SwiftUI
import FirebaseFirestore
import ComposableArchitecture

// MARK: - AccessCodeType

enum AccessCodeType: String, Codable, Equatable {
    case basic   = "BASIC"
    case premium = "PREMIUM"
    case vip     = "VIP"

    /// Nome exibido ao usuário
    var displayName: String {
        switch self {
        case .basic:   return "Básico"
        case .premium: return "Premium"
        case .vip:     return "VIP"
        }
    }

    /// Cor representativa do tipo de código
    var color: Color {
        switch self {
        case .basic:   return Color(.systemGray)
        case .premium: return Color.fypPink
        case .vip:     return Color(hex: "FFD700") // ouro
        }
    }
}

// MARK: - AccessCode

struct AccessCode: Codable, Identifiable, Equatable {
    let id: String
    let code: String
    let type: AccessCodeType
    let expiresAt: Date
    var isUsed: Bool
    var usedBy: String?
    var usedAt: Date?
    let createdAt: Date
}

// MARK: - AccessCodeError

enum AccessCodeError: LocalizedError, Equatable {
    case notFound
    case alreadyUsed
    case expired
    case firestoreError(String)

    var errorDescription: String? {
        switch self {
        case .notFound:
            return "Código de acesso não encontrado. Verifique e tente novamente."
        case .alreadyUsed:
            return "Este código já foi utilizado."
        case .expired:
            return "Este código de acesso expirou."
        case .firestoreError(let msg):
            return "Erro ao processar código: \(msg)"
        }
    }
}

// MARK: - AccessCodeService

@MainActor
class AccessCodeService {

    static let shared = AccessCodeService()
    private let db = Firestore.firestore()

    private init() {}

    // MARK: - Códigos pré-gerados para beta

    /// Códigos pré-gerados no formato FYPM-XXXX-XXXX para cada tipo
    static let preGeneratedCodes: [String: AccessCodeType] = [
        // Básico
        "FYPM-BETA-2024": .basic,
        "FYPM-BETA-ABCD": .basic,
        "FYPM-BETA-EFGH": .basic,
        "FYPM-BETA-IJKL": .basic,
        "FYPM-BETA-MNOP": .basic,
        "FYPM-BASI-0001": .basic,
        "FYPM-BASI-0002": .basic,
        // Premium
        "FYPM-PREM-2024": .premium,
        "FYPM-PREM-ABCD": .premium,
        "FYPM-PREM-EFGH": .premium,
        "FYPM-PREM-IJKL": .premium,
        "FYPM-PREM-MNOP": .premium,
        "FYPM-PREM-0001": .premium,
        "FYPM-PREM-0002": .premium,
        // VIP
        "FYPM-VIP1-2024": .vip,
        "FYPM-VIP2-2024": .vip,
        "FYPM-VIP3-2024": .vip,
        "FYPM-VIP1-ABCD": .vip,
        "FYPM-VIP2-ABCD": .vip,
        "FYPM-VIP3-ABCD": .vip,
    ]

    // MARK: - Validação

    /// Busca e valida o código no Firestore.
    /// Lança `AccessCodeError` se não encontrado, já usado ou expirado.
    func validateCode(_ code: String) async throws -> AccessCode {
        let normalised = code.uppercased().trimmingCharacters(in: .whitespaces)

        let snapshot = try await db
            .collection("access_codes")
            .whereField("code", isEqualTo: normalised)
            .limit(to: 1)
            .getDocuments()

        guard let doc = snapshot.documents.first else {
            throw AccessCodeError.notFound
        }

        let data = doc.data()

        let isUsed     = data["isUsed"] as? Bool ?? false
        let expiresTS  = data["expiresAt"] as? Timestamp ?? Timestamp(date: Date.distantPast)
        let createdTS  = data["createdAt"] as? Timestamp ?? Timestamp(date: Date())
        let typeRaw    = data["type"] as? String ?? ""
        let type       = AccessCodeType(rawValue: typeRaw) ?? .basic

        if isUsed { throw AccessCodeError.alreadyUsed }
        if expiresTS.dateValue() < Date() { throw AccessCodeError.expired }

        return AccessCode(
            id: doc.documentID,
            code: normalised,
            type: type,
            expiresAt: expiresTS.dateValue(),
            isUsed: isUsed,
            usedBy: data["usedBy"] as? String,
            usedAt: (data["usedAt"] as? Timestamp)?.dateValue(),
            createdAt: createdTS.dateValue()
        )
    }

    // MARK: - Resgate

    /// Valida e marca o código como usado, retornando o `AccessCodeType`.
    func redeemCode(_ code: String, userId: String) async throws -> AccessCodeType {
        let accessCode = try await validateCode(code)

        let updateData: [String: Any] = [
            "isUsed": true,
            "usedBy": userId,
            "usedAt": Timestamp(date: Date())
        ]

        try await db
            .collection("access_codes")
            .document(accessCode.id)
            .updateData(updateData)

        return accessCode.type
    }

    // MARK: - Carga inicial (desenvolvimento)

    /// Cria os códigos pré-gerados no Firestore. Executar apenas uma vez em ambiente de desenvolvimento.
    func createPreGeneratedCodes() async throws {
        let batch = db.batch()

        // Validade padrão: 1 ano a partir de agora
        let expiresAt = Calendar.current.date(byAdding: .year, value: 1, to: Date()) ?? Date()

        for (code, type) in Self.preGeneratedCodes {
            let ref = db.collection("access_codes").document()
            let data: [String: Any] = [
                "code": code,
                "type": type.rawValue,
                "isUsed": false,
                "expiresAt": Timestamp(date: expiresAt),
                "createdAt": Timestamp(date: Date())
            ]
            batch.setData(data, forDocument: ref)
        }

        try await batch.commit()
    }
}

// MARK: - TCA Dependency

private enum AccessCodeServiceKey: DependencyKey {
    static let liveValue = AccessCodeService.shared
}

extension DependencyValues {
    var accessCodeService: AccessCodeService {
        get { self[AccessCodeServiceKey.self] }
        set { self[AccessCodeServiceKey.self] = newValue }
    }
}
