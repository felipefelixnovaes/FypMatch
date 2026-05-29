// ArchetypeView.swift — FypMatch iOS
// Modo Autoconhecimento — Arquétipo Fyp (24 pares forçados)

import SwiftUI
import ComposableArchitecture

// MARK: - Conteúdo dos 24 itens (alinhado ao ArchetypeResult.itemMap)
//
// Índices: 0=explorer, 1=creator, 2=caregiver, 3=ruler, 4=sage, 5=hero
//          6=rebel, 7=lover, 8=jester, 9=innocent, 10=magician, 11=everyman

struct ArchetypeItem {
    let optionA: String
    let optionB: String
}

enum ArchetypeItems {
    static let items: [ArchetypeItem] = [
        // 0  (0,1): Explorador vs Criador
        ArchetypeItem(
            optionA: "Prefiro explorar o desconhecido e descobrir coisas novas",
            optionB: "Prefiro criar algo do zero e dar forma às minhas ideias"
        ),
        // 1  (0,2): Explorador vs Cuidador
        ArchetypeItem(
            optionA: "Me realizo quando tenho liberdade para ir além das fronteiras",
            optionB: "Me realizo quando cuido e protejo quem está perto de mim"
        ),
        // 2  (0,6): Explorador vs Rebelde
        ArchetypeItem(
            optionA: "Busco o novo porque a aventura me move",
            optionB: "Questiono o que está estabelecido porque a mudança me move"
        ),
        // 3  (0,9): Explorador vs Inocente
        ArchetypeItem(
            optionA: "Saio em busca do que ainda não conheço",
            optionB: "Encontro beleza e gratidão no que já tenho"
        ),
        // 4  (1,3): Criador vs Governante
        ArchetypeItem(
            optionA: "Expresso meu valor criando coisas únicas e originais",
            optionB: "Expresso meu valor organizando e liderando com responsabilidade"
        ),
        // 5  (1,7): Criador vs Amante
        ArchetypeItem(
            optionA: "Me conecto com o mundo pela expressão criativa",
            optionB: "Me conecto com o mundo pela intensidade das relações e da beleza"
        ),
        // 6  (1,10): Criador vs Mago
        ArchetypeItem(
            optionA: "Construo algo novo com minhas próprias mãos e mente",
            optionB: "Transformo a realidade enxergando o que outros não enxergam"
        ),
        // 7  (2,4): Cuidador vs Sábio
        ArchetypeItem(
            optionA: "Protejo sendo gentil, presente e atencioso(a)",
            optionB: "Contribuo compartilhando conhecimento e compreensão profunda"
        ),
        // 8  (2,8): Cuidador vs Bobo da Corte
        ArchetypeItem(
            optionA: "Cuido do outro com empatia e dedicação",
            optionB: "Conecto o outro com leveza, humor e espontaneidade"
        ),
        // 9  (2,11): Cuidador vs Cidadão Comum
        ArchetypeItem(
            optionA: "Me importo profundamente com o bem-estar de cada pessoa",
            optionB: "Me identifico com as pessoas comuns e valorizo o pertencimento"
        ),
        // 10 (3,5): Governante vs Herói
        ArchetypeItem(
            optionA: "Assumo o controle e traço o caminho para todos",
            optionB: "Me lanço nos desafios mais difíceis com coragem e determinação"
        ),
        // 11 (3,8): Governante vs Bobo da Corte
        ArchetypeItem(
            optionA: "Prefiro estrutura, ordem e responsabilidade",
            optionB: "Prefiro leveza, improviso e alegria no que faço"
        ),
        // 12 (3,11): Governante vs Cidadão Comum
        ArchetypeItem(
            optionA: "Lidero com visão e assumo a responsabilidade pelas decisões",
            optionB: "Prefiro trabalhar junto, como parte do grupo, sem hierarquias"
        ),
        // 13 (4,6): Sábio vs Rebelde
        ArchetypeItem(
            optionA: "Reflito com profundidade antes de agir",
            optionB: "Ajo para romper o que está errado, mesmo sem certeza"
        ),
        // 14 (4,9): Sábio vs Inocente
        ArchetypeItem(
            optionA: "Busco verdade pelo pensamento crítico e análise",
            optionB: "Busco paz aceitando o que é com confiança e fé"
        ),
        // 15 (4,11): Sábio vs Cidadão Comum
        ArchetypeItem(
            optionA: "Me guio pela busca do conhecimento e da sabedoria",
            optionB: "Me guio pela solidez, praticidade e vínculos concretos"
        ),
        // 16 (5,7): Herói vs Amante
        ArchetypeItem(
            optionA: "Protejo quem amo sendo corajoso(a) e determinado(a)",
            optionB: "Protejo quem amo sendo apaixonado(a) e profundamente presente"
        ),
        // 17 (5,10): Herói vs Mago
        ArchetypeItem(
            optionA: "Enfrento os obstáculos de frente com força e perseverança",
            optionB: "Dissolvo os obstáculos com criatividade e visão transformadora"
        ),
        // 18 (5,9): Herói vs Inocente
        ArchetypeItem(
            optionA: "Escolho o caminho difícil quando sei que é o certo",
            optionB: "Confio que o bem prevalece e sigo com otimismo"
        ),
        // 19 (6,7): Rebelde vs Amante
        ArchetypeItem(
            optionA: "Questiono o que está estabelecido e provoco mudança",
            optionB: "Sigo o que o coração sente com paixão e entrega"
        ),
        // 20 (6,8): Rebelde vs Bobo da Corte
        ArchetypeItem(
            optionA: "Subverto as regras quando elas não fazem sentido",
            optionB: "Transformo a tensão em riso e leveza"
        ),
        // 21 (7,11): Amante vs Cidadão Comum
        ArchetypeItem(
            optionA: "Me movo pela intensidade, beleza e conexão profunda",
            optionB: "Me movo pela lealdade, autenticidade e vida simples"
        ),
        // 22 (8,10): Bobo da Corte vs Mago
        ArchetypeItem(
            optionA: "Respondo aos momentos pesados com humor e descontração",
            optionB: "Respondo aos momentos pesados com insight e transformação"
        ),
        // 23 (9,10): Inocente vs Mago
        ArchetypeItem(
            optionA: "Aceito a vida com gratidão e pureza de intenção",
            optionB: "Reescrevo a vida usando o poder da intenção e da visão"
        ),
    ]
}

// MARK: - TCA Feature

@Reducer
struct ArchetypeFeature {
    @ObservableState
    struct State: Equatable {
        var userId: String
        var responses: [Bool?] = Array(repeating: nil, count: 24)
        var isSaving: Bool = false
        var isComplete: Bool = false
        var result: ArchetypeResult? = nil
        var errorMessage: String? = nil

        var answeredCount: Int { responses.compactMap { $0 }.count }
        var progressFraction: Double { Double(answeredCount) / 24.0 }
        var allAnswered: Bool { answeredCount == 24 }
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
                guard index >= 0, index < 24 else { return .none }
                state.responses[index] = chooseA
                return .none

            case .save:
                guard state.allAnswered else { return .none }
                state.isSaving = true
                let boolResponses = state.responses.map { $0 ?? false }
                let result = ArchetypeResult(responses: boolResponses)
                state.result = result
                let q = SelfKnowledgeQuestionnaire(
                    userId: state.userId,
                    completedAt: Date(),
                    archetype: result
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

// MARK: - ArchetypeView

struct ArchetypeView: View {
    @Bindable var store: StoreOf<ArchetypeFeature>

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                ArchetypeHeaderView(
                    answeredCount: store.answeredCount,
                    progressFraction: store.progressFraction
                )

                ScrollView {
                    LazyVStack(spacing: 16) {
                        InstructionCard(
                            text: "Em cada par, escolha a opção que mais ressoa com quem você é — não com quem gostaria de ser.",
                            icon: "theatermasks"
                        )

                        ForEach(0..<24, id: \.self) { index in
                            ArchetypeItemCard(
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
                                    ? "Ver meu arquétipo ✨"
                                    : "Responda todas as perguntas (\(store.answeredCount)/24)",
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
            .navigationTitle("Arquétipo")
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
                ArchetypeResultView(result: result)
            }
        }
    }
}

// MARK: - ArchetypeHeaderView

private struct ArchetypeHeaderView: View {
    let answeredCount: Int
    let progressFraction: Double

    var body: some View {
        VStack(spacing: 6) {
            HStack(spacing: 8) {
                Image(systemName: "theatermasks")
                    .foregroundStyle(LinearGradient.fypGradient)
                    .font(.system(size: 16, weight: .semibold))
                Text("Arquétipo — Autoconhecimento")
                    .font(.subheadline.weight(.semibold))
            }
            .padding(.top, 12)

            ProgressView(value: progressFraction)
                .tint(.fypPink)
                .padding(.horizontal, 20)
                .animation(.easeInOut, value: progressFraction)

            Text("\(answeredCount) de 24 respondidas")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding(.bottom, 8)
    }
}

// MARK: - ArchetypeItemCard

private struct ArchetypeItemCard: View {
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
                ArchetypeOptionButton(
                    label: "A",
                    text: ArchetypeItems.items[index].optionA,
                    isSelected: response == true,
                    action: onChooseA
                )

                ArchetypeOptionButton(
                    label: "B",
                    text: ArchetypeItems.items[index].optionB,
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

// MARK: - ArchetypeOptionButton

private struct ArchetypeOptionButton: View {
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

// MARK: - ArchetypeResultView

struct ArchetypeResultView: View {
    let result: ArchetypeResult
    @Environment(\.dismiss) private var dismiss

    @State private var appeared = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 28) {
                    Spacer(minLength: 32)

                    // Emoji do arquétipo dominante com animação spring
                    let dominant = result.dominantArchetype
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

                    // Nome do arquétipo
                    Text(dominant.displayName)
                        .font(.title2.bold())
                        .multilineTextAlignment(.center)
                        .opacity(appeared ? 1 : 0)
                        .animation(.easeIn(duration: 0.4).delay(0.3), value: appeared)

                    // Top 3 como chips coloridos
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Seus 3 arquétipos mais presentes")
                            .font(.subheadline.weight(.semibold))
                            .padding(.bottom, 4)

                        HStack(spacing: 10) {
                            ForEach(Array(result.topThree.enumerated()), id: \.offset) { offset, archetype in
                                ArchetypeChip(
                                    archetype: archetype,
                                    rank: offset + 1
                                )
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
            .navigationTitle("Seu Arquétipo")
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

// MARK: - ArchetypeChip

private struct ArchetypeChip: View {
    let archetype: FypArchetype
    let rank: Int

    private var chipColor: Color {
        switch rank {
        case 1: return .fypPink
        case 2: return .fypPurple
        default: return Color(.systemGray3)
        }
    }

    var body: some View {
        HStack(spacing: 6) {
            Text(archetype.emoji)
                .font(.body)
            Text(archetype.displayName)
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(rank <= 2 ? .white : .primary)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(rank <= 2 ? chipColor : Color(.secondarySystemBackground))
        .clipShape(Capsule())
    }
}

// MARK: - Preview

#if DEBUG
struct ArchetypeView_Previews: PreviewProvider {
    static var previews: some View {
        ArchetypeView(
            store: Store(initialState: ArchetypeFeature.State(userId: "preview-user")) {
                ArchetypeFeature()
            }
        )
    }
}

struct ArchetypeResultView_Previews: PreviewProvider {
    static var previews: some View {
        let responses = Array(repeating: true, count: 24)
        ArchetypeResultView(result: ArchetypeResult(responses: responses))
    }
}
#endif
