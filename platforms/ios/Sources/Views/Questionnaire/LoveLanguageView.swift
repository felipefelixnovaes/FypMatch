// LoveLanguageView.swift — FypMatch iOS
// Modo Autoconhecimento — Linguagem do Cuidado (30 pares forçados — Chapman)

import SwiftUI
import ComposableArchitecture

// MARK: - Conteúdo dos 30 itens (alinhado ao LoveLanguageResult.itemMap)

struct LoveLanguageItem {
    let optionA: String
    let optionB: String
}

enum LoveLanguageItems {
    static let items: [LoveLanguageItem] = [
        // Grupo 1 — pares 0-9 (primeiro ciclo C(5,2))
        // 0  (0,1): Palavras de Afirmação vs Atos de Serviço
        LoveLanguageItem(
            optionA: "Quando alguém me elogia sinceramente",
            optionB: "Quando alguém faz algo por mim sem eu pedir"
        ),
        // 1  (0,2): Palavras de Afirmação vs Presentes
        LoveLanguageItem(
            optionA: "Ouvir 'eu te amo' e palavras carinhosas",
            optionB: "Receber um presente pensado especialmente para mim"
        ),
        // 2  (0,3): Palavras de Afirmação vs Tempo de Qualidade
        LoveLanguageItem(
            optionA: "Uma conversa profunda e atenciosa",
            optionB: "Passar tempo de qualidade juntos, sem distrações"
        ),
        // 3  (0,4): Palavras de Afirmação vs Toque Físico
        LoveLanguageItem(
            optionA: "Mensagens e palavras de incentivo",
            optionB: "Um abraço longo ou carinho físico"
        ),
        // 4  (1,2): Atos de Serviço vs Presentes
        LoveLanguageItem(
            optionA: "Quando o outro resolve algo que me preocupava",
            optionB: "Ganhar algo que mostra que a pessoa pensa em mim"
        ),
        // 5  (1,3): Atos de Serviço vs Tempo de Qualidade
        LoveLanguageItem(
            optionA: "Alguém que ajuda nas tarefas do dia a dia",
            optionB: "Alguém que desliga o celular e fica presente"
        ),
        // 6  (1,4): Atos de Serviço vs Toque Físico
        LoveLanguageItem(
            optionA: "Quando o outro cuida de detalhes práticos",
            optionB: "Segurar a mão ou encostar no ombro"
        ),
        // 7  (2,3): Presentes vs Tempo de Qualidade
        LoveLanguageItem(
            optionA: "Um mimo surpresa, mesmo pequeno",
            optionB: "Um programa especial planejado para nós dois"
        ),
        // 8  (2,4): Presentes vs Toque Físico
        LoveLanguageItem(
            optionA: "Um presente que mostra que a pessoa me conhece",
            optionB: "Proximidade física e contato constante"
        ),
        // 9  (3,4): Tempo de Qualidade vs Toque Físico
        LoveLanguageItem(
            optionA: "Uma tarde inteira só nós dois, sem interrupções",
            optionB: "Trocar carinhos e estar próximo(a) fisicamente"
        ),

        // Grupo 2 — pares 10-19 (segundo ciclo C(5,2))
        // 10 (0,1): Palavras de Afirmação vs Atos de Serviço
        LoveLanguageItem(
            optionA: "Ser elogiado(a) na frente de outras pessoas",
            optionB: "Alguém que faz esforço para facilitar minha vida"
        ),
        // 11 (0,2): Palavras de Afirmação vs Presentes
        LoveLanguageItem(
            optionA: "Uma carta ou mensagem escrita com carinho",
            optionB: "Um presente que mostra atenção aos meus gostos"
        ),
        // 12 (0,3): Palavras de Afirmação vs Tempo de Qualidade
        LoveLanguageItem(
            optionA: "Alguém que me diz o quanto eu sou importante",
            optionB: "Um fim de semana juntos sem agenda lotada"
        ),
        // 13 (0,4): Palavras de Afirmação vs Toque Físico
        LoveLanguageItem(
            optionA: "Palavras de apoio quando estou mal",
            optionB: "Um colo ou presença física quando estou mal"
        ),
        // 14 (1,2): Atos de Serviço vs Presentes
        LoveLanguageItem(
            optionA: "Alguém que organiza algo complicado para mim",
            optionB: "Receber algo que eu queria há tempo"
        ),
        // 15 (1,3): Atos de Serviço vs Tempo de Qualidade
        LoveLanguageItem(
            optionA: "Quando o outro se antecipa e já resolveu algo",
            optionB: "Quando o outro cancela outros planos para estar comigo"
        ),
        // 16 (1,4): Atos de Serviço vs Toque Físico
        LoveLanguageItem(
            optionA: "Ser surpreendido(a) com uma tarefa já feita",
            optionB: "Massagem ou toque reconfortante após um dia difícil"
        ),
        // 17 (2,3): Presentes vs Tempo de Qualidade
        LoveLanguageItem(
            optionA: "Um presente simbólico de uma data especial",
            optionB: "Fazer juntos algo que sempre quis experienciar"
        ),
        // 18 (2,4): Presentes vs Toque Físico
        LoveLanguageItem(
            optionA: "Receber flores ou algo inesperado",
            optionB: "Ficar abraçado(a) enquanto assistimos algo"
        ),
        // 19 (3,4): Tempo de Qualidade vs Toque Físico
        LoveLanguageItem(
            optionA: "Jantarmos juntos sem celular nenhum na mesa",
            optionB: "Dormir abraçados ou ficar de mãos dadas"
        ),

        // Grupo 3 — pares 20-29 (terceiro ciclo C(5,2))
        // 20 (0,1): Palavras de Afirmação vs Atos de Serviço
        LoveLanguageItem(
            optionA: "'Estou orgulhoso(a) de você' dito com sinceridade",
            optionB: "Alguém que faz o jantar quando chego cansado(a)"
        ),
        // 21 (0,2): Palavras de Afirmação vs Presentes
        LoveLanguageItem(
            optionA: "Uma dedicatória escrita num livro ou cartão",
            optionB: "Um presente que custou tempo e cuidado"
        ),
        // 22 (0,3): Palavras de Afirmação vs Tempo de Qualidade
        LoveLanguageItem(
            optionA: "Ser reconhecido(a) verbalmente pelos meus esforços",
            optionB: "Passeios ou atividades que planejamos juntos"
        ),
        // 23 (0,4): Palavras de Afirmação vs Toque Físico
        LoveLanguageItem(
            optionA: "Mensagem de bom dia com palavras carinhosas",
            optionB: "Um beijo surpresa ou abraço no meio do dia"
        ),
        // 24 (1,2): Atos de Serviço vs Presentes
        LoveLanguageItem(
            optionA: "Alguém que cuida de mim quando estou doente",
            optionB: "Receber algo que lembra uma memória nossa"
        ),
        // 25 (1,3): Atos de Serviço vs Tempo de Qualidade
        LoveLanguageItem(
            optionA: "Quando o outro lembra de fazer algo que me ajuda",
            optionB: "Uma viagem curta ou experiência só para nós"
        ),
        // 26 (1,4): Atos de Serviço vs Toque Físico
        LoveLanguageItem(
            optionA: "Alguém que trata bem as pessoas que eu amo",
            optionB: "Carinho espontâneo sem motivo especial"
        ),
        // 27 (2,3): Presentes vs Tempo de Qualidade
        LoveLanguageItem(
            optionA: "Algo feito à mão com dedicação",
            optionB: "Um ritual semanal só nosso"
        ),
        // 28 (2,4): Presentes vs Toque Físico
        LoveLanguageItem(
            optionA: "Presente surpresa num dia comum",
            optionB: "Dormir colado(a) ou ficar juntinhos no sofá"
        ),
        // 29 (3,4): Tempo de Qualidade vs Toque Físico
        LoveLanguageItem(
            optionA: "Prioridade: me ver pessoalmente",
            optionB: "Prioridade: me tocar com carinho quando está perto"
        ),
    ]
}

// MARK: - TCA Feature

@Reducer
struct LoveLanguageFeature {
    @ObservableState
    struct State: Equatable {
        var userId: String
        var responses: [Bool?] = Array(repeating: nil, count: 30)
        var isSaving: Bool = false
        var isComplete: Bool = false
        var result: LoveLanguageResult? = nil
        var errorMessage: String? = nil

        var answeredCount: Int { responses.compactMap { $0 }.count }
        var progressFraction: Double { Double(answeredCount) / 30.0 }
        var allAnswered: Bool { answeredCount == 30 }
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
                guard index >= 0, index < 30 else { return .none }
                state.responses[index] = chooseA
                return .none

            case .save:
                guard state.allAnswered else { return .none }
                state.isSaving = true
                let boolResponses = state.responses.map { $0 ?? false }
                let result = LoveLanguageResult(responses: boolResponses)
                state.result = result
                let q = SelfKnowledgeQuestionnaire(
                    userId: state.userId,
                    completedAt: Date(),
                    loveLanguage: result
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

// MARK: - LoveLanguageView

struct LoveLanguageView: View {
    @Bindable var store: StoreOf<LoveLanguageFeature>

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                LoveLanguageHeaderView(
                    answeredCount: store.answeredCount,
                    progressFraction: store.progressFraction
                )

                ScrollView {
                    LazyVStack(spacing: 16) {
                        InstructionCard(
                            text: "Em cada par, escolha a opção que mais representa como você se sente amado(a) — não o que você faz pelos outros.",
                            icon: "heart.text.square"
                        )

                        ForEach(0..<30, id: \.self) { index in
                            LoveLanguageItemCard(
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
                                    ? "Ver minha linguagem 💬"
                                    : "Responda todas as perguntas (\(store.answeredCount)/30)",
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
            .navigationTitle("Linguagem do Cuidado")
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
                LoveLanguageResultView(result: result)
            }
        }
    }
}

// MARK: - LoveLanguageHeaderView

private struct LoveLanguageHeaderView: View {
    let answeredCount: Int
    let progressFraction: Double

    var body: some View {
        VStack(spacing: 6) {
            HStack(spacing: 8) {
                Image(systemName: "heart.text.square")
                    .foregroundStyle(LinearGradient.fypGradient)
                    .font(.system(size: 16, weight: .semibold))
                Text("Linguagem do Cuidado — Autoconhecimento")
                    .font(.subheadline.weight(.semibold))
            }
            .padding(.top, 12)

            ProgressView(value: progressFraction)
                .tint(.fypPink)
                .padding(.horizontal, 20)
                .animation(.easeInOut, value: progressFraction)

            Text("\(answeredCount) de 30 respondidas")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding(.bottom, 8)
    }
}

// MARK: - LoveLanguageItemCard

private struct LoveLanguageItemCard: View {
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
                LoveLanguageOptionButton(
                    label: "A",
                    text: LoveLanguageItems.items[index].optionA,
                    isSelected: response == true,
                    action: onChooseA
                )

                LoveLanguageOptionButton(
                    label: "B",
                    text: LoveLanguageItems.items[index].optionB,
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

// MARK: - LoveLanguageOptionButton

private struct LoveLanguageOptionButton: View {
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

// MARK: - LoveLanguageResultView

struct LoveLanguageResultView: View {
    let result: LoveLanguageResult
    @Environment(\.dismiss) private var dismiss

    @State private var appeared = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 28) {
                    Spacer(minLength: 32)

                    // Emoji da linguagem primária com animação spring
                    let primary = result.primaryLanguage
                    ZStack {
                        Circle()
                            .fill(LinearGradient.fypGradient)
                            .frame(width: 110, height: 110)
                            .shadow(color: .fypPink.opacity(0.4), radius: 20)

                        Text(primary.emoji)
                            .font(.system(size: 52))
                    }
                    .scaleEffect(appeared ? 1.0 : 0.4)
                    .animation(.spring(response: 0.5, dampingFraction: 0.55).delay(0.1), value: appeared)

                    // Nome e descrição
                    VStack(spacing: 8) {
                        Text(primary.displayName)
                            .font(.title2.bold())
                            .multilineTextAlignment(.center)

                        Text(primary.description)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 24)
                    }
                    .opacity(appeared ? 1 : 0)
                    .animation(.easeIn(duration: 0.4).delay(0.3), value: appeared)

                    // Barras de progresso para as 5 linguagens
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Suas linguagens por proporção")
                            .font(.subheadline.weight(.semibold))
                            .padding(.bottom, 4)

                        let scores = result.scores
                        let maxScore = scores.values.max() ?? 1

                        ForEach(LoveLanguage.allCases, id: \.rawValue) { language in
                            let score = scores[language] ?? 0
                            let fraction = maxScore > 0 ? Double(score) / Double(maxScore) : 0

                            HStack(spacing: 12) {
                                Text(language.emoji)
                                    .font(.title3)
                                    .frame(width: 30)

                                VStack(alignment: .leading, spacing: 4) {
                                    HStack {
                                        Text(language.displayName)
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
            .navigationTitle("Sua Linguagem")
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
struct LoveLanguageView_Previews: PreviewProvider {
    static var previews: some View {
        LoveLanguageView(
            store: Store(initialState: LoveLanguageFeature.State(userId: "preview-user")) {
                LoveLanguageFeature()
            }
        )
    }
}

struct LoveLanguageResultView_Previews: PreviewProvider {
    static var previews: some View {
        let responses = Array(repeating: true, count: 30)
        LoveLanguageResultView(result: LoveLanguageResult(responses: responses))
    }
}
#endif
