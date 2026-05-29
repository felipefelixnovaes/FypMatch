// EnneagramView.swift — FypMatch iOS
// Modo Autoconhecimento — Eneagrama (27 pares forçados)

import SwiftUI
import ComposableArchitecture

// MARK: - Modelos inline (TODO: mover para QuestionnaireModels.swift quando Architecture Squad concluir)

enum EnneagramType: Int, CaseIterable, Equatable, Codable {
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
        case .two:   return "💛"
        case .three: return "🏆"
        case .four:  return "🎭"
        case .five:  return "🔭"
        case .six:   return "🛡️"
        case .seven: return "🎉"
        case .eight: return "🦁"
        case .nine:  return "☮️"
        }
    }

    var shortDescription: String {
        switch self {
        case .one:   return "Principista, ético e autoexigente. Busca a perfeição e teme o erro."
        case .two:   return "Caloroso, prestativo e generoso. Busca ser amado sendo necessário."
        case .three: return "Ambicioso, adaptável e orientado a resultados. Busca sucesso e reconhecimento."
        case .four:  return "Introspectivo, sensível e autêntico. Busca identidade e profundidade emocional."
        case .five:  return "Analítico, reservado e curioso. Busca conhecimento e independência."
        case .six:   return "Responsável, fiel e cuidadoso. Busca segurança e confiança."
        case .seven: return "Espontâneo, versátil e otimista. Busca experiências e evita a dor."
        case .eight: return "Assertivo, decidido e protetor. Busca controle e não tolera injustiça."
        case .nine:  return "Receptivo, paciente e harmonioso. Busca paz e evita conflitos."
        }
    }
}

struct EnneagramResult: Equatable, Codable {
    var responses: [Bool]

    // 27 pares: escolha A pontua o primeiro tipo, B pontua o segundo
    // Cada tipo aparece exatamente 6 vezes
    // Pares (sem (2,3) para balancear; (7,9) fecha os 27)
    static let itemMap: [(EnneagramType, EnneagramType)] = [
        (.one, .two),    // 0
        (.one, .three),  // 1
        (.one, .four),   // 2
        (.one, .five),   // 3
        (.one, .six),    // 4
        (.one, .seven),  // 5
        (.two, .four),   // 6
        (.two, .five),   // 7
        (.two, .six),    // 8
        (.two, .eight),  // 9
        (.two, .nine),   // 10
        (.three, .five), // 11
        (.three, .six),  // 12
        (.three, .seven),// 13
        (.three, .eight),// 14
        (.three, .nine), // 15
        (.four, .six),   // 16
        (.four, .seven), // 17
        (.four, .eight), // 18
        (.four, .nine),  // 19
        (.five, .seven), // 20
        (.five, .eight), // 21
        (.five, .nine),  // 22
        (.six, .eight),  // 23
        (.six, .nine),   // 24
        (.seven, .eight),// 25
        (.seven, .nine), // 26
    ]

    var scores: [EnneagramType: Int] {
        var result: [EnneagramType: Int] = [:]
        EnneagramType.allCases.forEach { result[$0] = 0 }
        for (idx, chooseA) in responses.enumerated() {
            guard idx < Self.itemMap.count else { continue }
            let pair = Self.itemMap[idx]
            if chooseA {
                result[pair.0, default: 0] += 1
            } else {
                result[pair.1, default: 0] += 1
            }
        }
        return result
    }

    var dominantType: EnneagramType {
        scores.max(by: { $0.value < $1.value })?.key ?? .nine
    }

    var topThree: [EnneagramType] {
        scores.sorted { $0.value > $1.value }.prefix(3).map(\.key)
    }
}

struct SelfKnowledgeQuestionnaire: Equatable, Codable {
    var userId: String
    var completedAt: Date?
    var enneagram: EnneagramResult?
}

// MARK: - Conteúdo dos 27 itens (alinhado ao itemMap)

struct EnneagramItem {
    let optionA: String
    let optionB: String
}

enum EnneagramItems {
    static let items: [EnneagramItem] = [
        // 0  (1,2)
        EnneagramItem(optionA: "Prefiro fazer as coisas do jeito certo",
                      optionB: "Prefiro ajudar as pessoas ao meu redor"),
        // 1  (1,3)
        EnneagramItem(optionA: "Valorizo a integridade acima do sucesso",
                      optionB: "Valorizo o sucesso e ser reconhecido"),
        // 2  (1,4)
        EnneagramItem(optionA: "Sigo princípios mesmo que seja difícil",
                      optionB: "Expresso o que sinto com profundidade"),
        // 3  (1,5)
        EnneagramItem(optionA: "Tenho padrões altos para tudo que faço",
                      optionB: "Prefiro entender antes de agir"),
        // 4  (1,6)
        EnneagramItem(optionA: "Corrijo erros, nos outros e em mim",
                      optionB: "Penso muito nos riscos antes de decidir"),
        // 5  (1,7)
        EnneagramItem(optionA: "Evito excessos e mantenho a disciplina",
                      optionB: "Busco variedade e novas experiências"),
        // 6  (2,4)
        EnneagramItem(optionA: "Sinto satisfação em ser necessário(a)",
                      optionB: "Sinto que sou diferente da maioria"),
        // 7  (2,5)
        EnneagramItem(optionA: "Me aproximo das pessoas com afeto",
                      optionB: "Me afasto para pensar com clareza"),
        // 8  (2,6)
        EnneagramItem(optionA: "Dou muito de mim nas relações",
                      optionB: "Sou leal e sigo quem confio"),
        // 9  (2,8)
        EnneagramItem(optionA: "Cuido dos outros com carinho",
                      optionB: "Protejo quem amo com firmeza"),
        // 10 (2,9)
        EnneagramItem(optionA: "Antecipo o que os outros precisam",
                      optionB: "Evito conflitos e mantenho a paz"),
        // 11 (3,5)
        EnneagramItem(optionA: "Gosto de ser visto(a) como competente",
                      optionB: "Prefiro ser reconhecido(a) pela inteligência"),
        // 12 (3,6)
        EnneagramItem(optionA: "Foco em resultados e conquistas",
                      optionB: "Foco em segurança e lealdade"),
        // 13 (3,7)
        EnneagramItem(optionA: "Trabalho duro para alcançar meus objetivos",
                      optionB: "Busco diversão e fuga da rotina"),
        // 14 (3,8)
        EnneagramItem(optionA: "Mostro meu valor pelo que conquisto",
                      optionB: "Mostro meu valor pela minha força"),
        // 15 (3,9)
        EnneagramItem(optionA: "Adapto minha imagem ao contexto",
                      optionB: "Me adapto para manter harmonia"),
        // 16 (4,6)
        EnneagramItem(optionA: "Prefiro autenticidade a segurança",
                      optionB: "Prefiro segurança a correr riscos"),
        // 17 (4,7)
        EnneagramItem(optionA: "Mergulho em emoções profundas",
                      optionB: "Fujo de emoções pesadas com leveza"),
        // 18 (4,8)
        EnneagramItem(optionA: "Me expresso mesmo sendo vulnerável",
                      optionB: "Me expresso mesmo sendo confrontador(a)"),
        // 19 (4,9)
        EnneagramItem(optionA: "Aceito minha singularidade",
                      optionB: "Aceito as diferenças dos outros"),
        // 20 (5,7)
        EnneagramItem(optionA: "Preciso de silêncio para recarregar",
                      optionB: "Preciso de estímulo para recarregar"),
        // 21 (5,8)
        EnneagramItem(optionA: "Ganho poder pelo conhecimento",
                      optionB: "Ganho poder pela ação direta"),
        // 22 (5,9)
        EnneagramItem(optionA: "Me retiro para processar o mundo",
                      optionB: "Me retiro para manter a paz interna"),
        // 23 (6,8)
        EnneagramItem(optionA: "Busco segurança nos vínculos",
                      optionB: "Busco segurança na força própria"),
        // 24 (6,9)
        EnneagramItem(optionA: "Me preocupo com o que pode dar errado",
                      optionB: "Confio que as coisas vão se resolver"),
        // 25 (7,8)
        EnneagramItem(optionA: "Quero liberdade e novas experiências",
                      optionB: "Quero controle e autonomia"),
        // 26 (7,9)
        EnneagramItem(optionA: "Me lanço em aventuras e fujo da monotonia",
                      optionB: "Fluo com a vida e aceito o que vem"),
    ]
}

// MARK: - TCA Feature

@Reducer
struct EnneagramFeature {
    @ObservableState
    struct State: Equatable {
        var userId: String
        var responses: [Bool?] = Array(repeating: nil, count: 27)
        var isSaving: Bool = false
        var isComplete: Bool = false
        var result: EnneagramResult? = nil
        var errorMessage: String? = nil

        var answeredCount: Int { responses.compactMap { $0 }.count }
        var progressFraction: Double { Double(answeredCount) / 27.0 }
        var allAnswered: Bool { answeredCount == 27 }
    }

    enum Action: Equatable {
        case respond(index: Int, chooseA: Bool)
        case save
        case saveResponse(TaskResult<Bool>)
        case dismiss
    }

    @Dependency(\.questionnaireService) var questionnaireService
    @Dependency(\.dismiss) var dismiss

    var body: some ReducerOf<Self> {
        Reduce { state, action in
            switch action {

            case let .respond(index, chooseA):
                guard index >= 0, index < 27 else { return .none }
                state.responses[index] = chooseA
                return .none

            case .save:
                guard state.allAnswered else { return .none }
                state.isSaving = true
                let boolResponses = state.responses.map { $0 ?? false }
                let result = EnneagramResult(responses: boolResponses)
                state.result = result
                let q = SelfKnowledgeQuestionnaire(
                    userId: state.userId,
                    completedAt: Date(),
                    enneagram: result
                )
                return .run { send in
                    await send(.saveResponse(TaskResult {
                        try await questionnaireService.saveSelfKnowledge(q)
                        return true
                    }))
                }

            case .saveResponse(.success):
                state.isSaving = false
                state.isComplete = true
                return .none

            case let .saveResponse(.failure(error)):
                state.isSaving = false
                state.errorMessage = error.localizedDescription
                return .none

            case .dismiss:
                return .run { _ in await dismiss() }
            }
        }
    }
}

// MARK: - EnneagramView

struct EnneagramView: View {
    @Bindable var store: StoreOf<EnneagramFeature>

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                EnneagramHeaderView(
                    answeredCount: store.answeredCount,
                    progressFraction: store.progressFraction
                )

                ScrollView {
                    LazyVStack(spacing: 16) {
                        InstructionCard(
                            text: "Em cada par, escolha a opção que mais se parece com você — não a que você acha que deveria ser.",
                            icon: "brain"
                        )

                        ForEach(0..<27, id: \.self) { index in
                            EnneagramItemCard(
                                index: index,
                                response: store.responses[index],
                                onChooseA: { store.send(.respond(index: index, chooseA: true)) },
                                onChooseB: { store.send(.respond(index: index, chooseA: false)) }
                            )
                        }
                    }
                    .padding()
                }

                VStack(spacing: 0) {
                    Divider()

                    Group {
                        if store.isSaving {
                            ProgressView()
                                .padding()
                        } else {
                            GradientButton(
                                title: store.allAnswered
                                    ? "Ver meu tipo ✨"
                                    : "Responda todas as perguntas (\(store.answeredCount)/27)",
                                isDisabled: !store.allAnswered
                            ) {
                                store.send(.save)
                            }
                            .opacity(store.allAnswered ? 1.0 : 0.5)
                            .disabled(!store.allAnswered)
                        }
                    }
                    .padding()

                    if let errorMessage = store.errorMessage {
                        Text(errorMessage)
                            .font(.caption)
                            .foregroundColor(.red)
                            .padding(.bottom, 8)
                    }
                }
            }
            .navigationTitle("Eneagrama")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Fechar") { store.send(.dismiss) }
                        .foregroundColor(.fypPink)
                }
            }
        }
        .sheet(isPresented: .constant(store.isComplete)) {
            if let result = store.result {
                EnneagramResultView(result: result)
            }
        }
    }
}

// MARK: - EnneagramHeaderView

private struct EnneagramHeaderView: View {
    let answeredCount: Int
    let progressFraction: Double

    var body: some View {
        VStack(spacing: 6) {
            HStack(spacing: 8) {
                Image(systemName: "brain")
                    .foregroundStyle(LinearGradient.fypGradient)
                    .font(.system(size: 16, weight: .semibold))
                Text("Eneagrama — Autoconhecimento")
                    .font(.subheadline.weight(.semibold))
            }
            .padding(.top, 12)

            ProgressView(value: progressFraction)
                .tint(.fypPink)
                .padding(.horizontal, 20)
                .animation(.easeInOut, value: progressFraction)

            Text("\(answeredCount) de 27 respondidas")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding(.bottom, 8)
    }
}

// MARK: - InstructionCard

private struct InstructionCard: View {
    let text: String
    let icon: String

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: icon)
                .foregroundStyle(LinearGradient.fypGradient)
                .font(.system(size: 18, weight: .semibold))

            Text(text)
                .font(.subheadline)
                .foregroundColor(.secondary)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 14)
                .fill(Color(.secondarySystemBackground))
        )
    }
}

// MARK: - EnneagramItemCard

private struct EnneagramItemCard: View {
    let index: Int
    let response: Bool?
    let onChooseA: () -> Void
    let onChooseB: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Pergunta \(index + 1)")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Spacer()
                if response != nil {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.fypPink)
                        .font(.caption)
                }
            }

            HStack(spacing: 12) {
                EnneagramOptionButton(
                    label: "A",
                    text: EnneagramItems.items[index].optionA,
                    isSelected: response == true,
                    action: onChooseA
                )

                EnneagramOptionButton(
                    label: "B",
                    text: EnneagramItems.items[index].optionB,
                    isSelected: response == false,
                    action: onChooseB
                )
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(Color(.systemBackground))
                .shadow(color: .black.opacity(0.06), radius: 8, y: 2)
        )
    }
}

// MARK: - EnneagramOptionButton

private struct EnneagramOptionButton: View {
    let label: String
    let text: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(alignment: .leading, spacing: 8) {
                Text(label)
                    .font(.system(size: 12, weight: .black))
                    .foregroundColor(isSelected ? .white : .fypPink)
                    .frame(width: 24, height: 24)
                    .background(
                        isSelected
                            ? AnyShapeStyle(Color.white.opacity(0.25))
                            : AnyShapeStyle(Color.fypPink.opacity(0.08))
                    )
                    .clipShape(Circle())

                Text(text)
                    .font(.system(size: 13, weight: isSelected ? .semibold : .regular))
                    .foregroundColor(isSelected ? .white : .primary)
                    .fixedSize(horizontal: false, vertical: true)
                    .multilineTextAlignment(.leading)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(12)
            .background(
                isSelected
                    ? AnyShapeStyle(LinearGradient.fypGradient)
                    : AnyShapeStyle(Color(.secondarySystemBackground))
            )
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isSelected ? Color.clear : Color(.systemGray4), lineWidth: 1)
            )
            .scaleEffect(isSelected ? 1.02 : 1.0)
            .animation(.easeInOut(duration: 0.2), value: isSelected)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - EnneagramResultView

struct EnneagramResultView: View {
    let result: EnneagramResult
    @Environment(\.dismiss) private var dismiss

    @State private var appeared = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 28) {
                    Spacer(minLength: 32)

                    // Emoji do tipo dominante com animação spring
                    let dominant = result.dominantType
                    ZStack {
                        Circle()
                            .fill(LinearGradient.fypGradient)
                            .frame(width: 110, height: 110)
                            .shadow(color: .fypPink.opacity(0.4), radius: 20)

                        Text(dominant.emoji)
                            .font(.system(size: 52))
                    }
                    .scaleEffect(appeared ? 1.0 : 0.4)
                    .animation(.spring(response: 0.5, dampingFraction: 0.55).delay(0.1), value: appeared)

                    // Nome e descrição
                    VStack(spacing: 8) {
                        Text("Tipo \(dominant.rawValue) — \(dominant.displayName)")
                            .font(.title2.bold())
                            .multilineTextAlignment(.center)

                        Text(dominant.shortDescription)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 24)
                    }
                    .opacity(appeared ? 1 : 0)
                    .animation(.easeIn(duration: 0.4).delay(0.3), value: appeared)

                    // Top 3 com barras de progresso
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Seus tipos mais presentes")
                            .font(.subheadline.weight(.semibold))
                            .padding(.bottom, 4)

                        let scores = result.scores
                        let maxScore = scores.values.max() ?? 1

                        ForEach(result.topThree, id: \.rawValue) { type in
                            let score = scores[type] ?? 0
                            let fraction = maxScore > 0 ? Double(score) / Double(maxScore) : 0

                            HStack(spacing: 12) {
                                Text(type.emoji)
                                    .font(.title3)
                                    .frame(width: 30)

                                VStack(alignment: .leading, spacing: 4) {
                                    HStack {
                                        Text("Tipo \(type.rawValue) — \(type.displayName)")
                                            .font(.system(size: 13, weight: .medium))
                                        Spacer()
                                        Text("\(score) pts")
                                            .font(.caption)
                                            .foregroundColor(.secondary)
                                    }
                                    ProgressView(value: fraction)
                                        .tint(.fypPink)
                                        .animation(.easeInOut(duration: 0.6), value: fraction)
                                }
                            }
                        }
                    }
                    .padding(20)
                    .background(Color(.secondarySystemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 18))
                    .padding(.horizontal, 20)
                    .opacity(appeared ? 1 : 0)
                    .animation(.easeIn(duration: 0.4).delay(0.5), value: appeared)

                    GradientButton(title: "Entendido!") {
                        dismiss()
                    }
                    .padding(.horizontal, 20)
                    .opacity(appeared ? 1 : 0)
                    .animation(.easeIn(duration: 0.4).delay(0.65), value: appeared)

                    Spacer(minLength: 32)
                }
            }
            .navigationTitle("Seu Tipo")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Fechar") { dismiss() }
                        .foregroundColor(.fypPink)
                }
            }
        }
        .onAppear { appeared = true }
    }
}

// MARK: - Preview

#if DEBUG
struct EnneagramView_Previews: PreviewProvider {
    static var previews: some View {
        EnneagramView(
            store: Store(initialState: EnneagramFeature.State(userId: "preview-user")) {
                EnneagramFeature()
            }
        )
    }
}

struct EnneagramResultView_Previews: PreviewProvider {
    static var previews: some View {
        let responses = Array(repeating: true, count: 27)
        EnneagramResultView(result: EnneagramResult(responses: responses))
    }
}
#endif
