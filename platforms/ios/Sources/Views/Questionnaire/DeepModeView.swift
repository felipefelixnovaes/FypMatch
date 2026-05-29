// DeepModeView.swift — FypMatch iOS
// Modo Profundo — IPIP-20, PVQ-21, ECR-RS, Conflito Profundo, Projeto de Vida
// iOS Squad — Sprint 6

import SwiftUI
import ComposableArchitecture

// MARK: - Feature

@Reducer
struct DeepModeFeature {
    @ObservableState
    struct State: Equatable {
        var userId: String
        var currentStep: Int = 0  // 0..4
        let totalSteps: Int = 5

        // Respostas por módulo
        var ipip20Responses: [Int] = Array(repeating: 3, count: 20)   // default 3 (neutro)
        var pvq21Responses: [Int]  = Array(repeating: 3, count: 21)   // default 3
        var ecrrResponses: [Int]   = Array(repeating: 4, count: 12)   // default 4

        var conflictDeep: DeepConflictResult? = nil
        var lifeProject: LifeProjectResult? = nil

        var isSaving: Bool = false
        var isComplete: Bool = false
        var errorMessage: String? = nil

        // Campos individuais do passo 4 — Conflito
        var conflictResolutionStyle: ConflictResolutionStyle = .coolingOff
        var emotionalExpression: EmotionalExpression = .moderate
        var repairBehavior: RepairBehavior = .gradual
        var silencePeriod: SilencePeriod = .hours
        var apologyStyle: ApologyStyle = .both
        var feedbackTolerance: FeedbackTolerance = .contextual

        // Campos individuais do passo 5 — Projeto de Vida
        var childrenDesire: ChildrenDesire = .undecided
        var locationFlexibility: LocationFlexibility = .openSameCity
        var careerPriority: CareerPriority = .balanced
        var financialApproach: FinancialApproach = .balanced
        var spiritualityRole: SpiritualityRole = .personal

        var progressFraction: Double { Double(currentStep) / Double(totalSteps) }

        /// Preview do estilo de apego calculado em tempo real a partir das respostas atuais
        var currentAttachmentStyle: AttachmentStyle {
            ECRRSResult(responses: ecrrResponses).attachmentStyle
        }

        /// Top 3 valores PVQ calculados em tempo real
        var currentTopValues: [SchwartzValue] {
            PVQ21Result(responses: pvq21Responses).topValues
        }
    }

    enum Action: BindableAction, Equatable {
        case binding(BindingAction<State>)
        case nextStep
        case previousStep
        case updateIPIP20(index: Int, value: Int)
        case updatePVQ21(index: Int, value: Int)
        case updateECRRS(index: Int, value: Int)
        case save
        case saveResponse(TaskResult<Void>)
        case dismiss
    }

    @Dependency(\.questionnaireService) var questionnaireService
    @Dependency(\.dismiss) var dismiss

    var body: some ReducerOf<Self> {
        BindingReducer()
        Reduce { state, action in
            switch action {

            case .nextStep:
                if state.currentStep == 3 {
                    // Consolidar campos de conflito
                    state.conflictDeep = DeepConflictResult(
                        resolutionStyle: state.conflictResolutionStyle,
                        emotionalExpression: state.emotionalExpression,
                        repairBehavior: state.repairBehavior,
                        silencePeriod: state.silencePeriod,
                        apologyStyle: state.apologyStyle,
                        feedbackTolerance: state.feedbackTolerance
                    )
                }
                if state.currentStep == 4 {
                    // Consolidar projeto de vida e salvar
                    state.lifeProject = LifeProjectResult(
                        childrenDesire: state.childrenDesire,
                        locationFlexibility: state.locationFlexibility,
                        careerPriority: state.careerPriority,
                        financialApproach: state.financialApproach,
                        spiritualityRole: state.spiritualityRole
                    )
                    return .send(.save)
                }
                guard state.currentStep < state.totalSteps - 1 else { return .none }
                state.currentStep += 1
                return .none

            case .previousStep:
                guard state.currentStep > 0 else { return .none }
                state.currentStep -= 1
                return .none

            case let .updateIPIP20(index, value):
                state.ipip20Responses[index] = value
                return .none

            case let .updatePVQ21(index, value):
                state.pvq21Responses[index] = value
                return .none

            case let .updateECRRS(index, value):
                state.ecrrResponses[index] = value
                return .none

            case .save:
                state.isSaving = true
                let deepQ = DeepModeQuestionnaire(
                    userId: state.userId,
                    completedAt: Date(),
                    ipip20: IPIP20Result(responses: state.ipip20Responses),
                    pvq21: PVQ21Result(responses: state.pvq21Responses),
                    ecrrs: ECRRSResult(responses: state.ecrrResponses),
                    conflictDeep: state.conflictDeep,
                    lifeProject: state.lifeProject
                )
                return .run { send in
                    await send(.saveResponse(TaskResult {
                        try await questionnaireService.saveDeepMode(deepQ)
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

            case .binding:
                return .none
            }
        }
    }
}

// MARK: - DeepModeView (Wizard principal)

struct DeepModeView: View {
    @Bindable var store: StoreOf<DeepModeFeature>

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Header com progresso
                DeepModeHeaderView(store: store)

                // Conteúdo do passo atual via TabView
                TabView(selection: $store.currentStep) {
                    IPIP20StepView(store: store).tag(0)
                    PVQ21StepView(store: store).tag(1)
                    ECRRSStepView(store: store).tag(2)
                    ConflictDeepStepView(store: store).tag(3)
                    LifeProjectStepView(store: store).tag(4)
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
                .animation(.easeInOut, value: store.currentStep)

                // Botões de navegação
                DeepModeNavigationButtons(store: store)
            }
            .background(Color(.systemBackground))
            .navigationTitle("Modo Profundo")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Fechar") { store.send(.dismiss) }
                        .foregroundColor(.fypPink)
                }
            }
        }
        .fullScreenCover(isPresented: Binding(
            get: { store.isComplete },
            set: { _ in }
        )) {
            DeepModeCompleteView(store: store)
        }
    }
}

// MARK: - DeepModeHeaderView

private struct DeepModeHeaderView: View {
    let store: StoreOf<DeepModeFeature>

    private let stepLabels = ["Personalidade", "Valores", "Vínculo", "Conflito", "Projeto de Vida"]
    private let stepIcons  = ["brain.head.profile", "heart.fill", "link", "flame.fill", "map.fill"]

    var body: some View {
        VStack(spacing: 8) {
            ProgressView(value: store.progressFraction + (1.0 / Double(store.totalSteps)))
                .tint(.fypPink)
                .padding(.horizontal, 20)
                .animation(.easeInOut, value: store.currentStep)

            HStack(spacing: 6) {
                Image(systemName: stepIcons[safe: store.currentStep] ?? "questionmark")
                    .foregroundStyle(LinearGradient.fypGradient)
                    .font(.system(size: 14, weight: .semibold))
                Text(stepLabels[safe: store.currentStep] ?? "")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(LinearGradient.fypGradient)
                Spacer()
                Text("Passo \(store.currentStep + 1) de \(store.totalSteps)")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .padding(.horizontal, 20)
        }
        .padding(.top, 12)
        .padding(.bottom, 8)
    }
}

// MARK: - DeepModeNavigationButtons

private struct DeepModeNavigationButtons: View {
    let store: StoreOf<DeepModeFeature>

    var body: some View {
        VStack(spacing: 0) {
            if let erro = store.errorMessage {
                Text(erro)
                    .font(.caption)
                    .foregroundColor(.red)
                    .padding(.bottom, 6)
                    .padding(.horizontal, 20)
            }

            HStack(spacing: 12) {
                if store.currentStep > 0 {
                    Button {
                        withAnimation(.easeInOut) { store.send(.previousStep) }
                    } label: {
                        HStack(spacing: 6) {
                            Image(systemName: "chevron.left")
                            Text("Voltar")
                        }
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(.fypPink)
                        .frame(maxWidth: 120, minHeight: 50)
                        .background(
                            RoundedRectangle(cornerRadius: 14)
                                .stroke(Color.fypPink, lineWidth: 1.5)
                        )
                    }
                }

                let isLastStep = store.currentStep == store.totalSteps - 1
                GradientButton(
                    title: isLastStep ? (store.isSaving ? "Salvando..." : "Concluir") : "Próximo",
                    icon: isLastStep ? (store.isSaving ? nil : "checkmark") : "chevron.right",
                    isLoading: store.isSaving
                ) {
                    withAnimation(.easeInOut) { store.send(.nextStep) }
                }
                .disabled(store.isSaving)
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 24)
        }
    }
}

// MARK: - Passo 1: IPIP-20

private struct IPIP20StepView: View {
    let store: StoreOf<DeepModeFeature>

    private let fatores: [(nome: String, cor: Color, intervalo: Range<Int>)] = [
        ("Extroversão",       .blue,   0..<4),
        ("Amabilidade",       .green,  4..<8),
        ("Conscienciosidade", .orange, 8..<12),
        ("Neuroticismo",      .red,    12..<16),
        ("Abertura",          .purple, 16..<20)
    ]

    private let itens: [String] = [
        // Extroversão (0-3)
        "Sou o centro das atenções nas festas",
        "Falo pouco com desconhecidos",
        "Me sinto bem em ambientes sociais",
        "Gosto de estar rodeado de pessoas",
        // Amabilidade (4-7)
        "Me importo com os outros",
        "Frequentemente insulto as pessoas",
        "Sinto empatia pelas emoções dos outros",
        "Faço as pessoas se sentirem bem-vindas",
        // Conscienciosidade (8-11)
        "Sempre estou preparado(a)",
        "Deixo uma bagunça por onde passo",
        "Presto atenção nos detalhes",
        "Faço meu trabalho imediatamente",
        // Neuroticismo (12-15)
        "Fico estressado(a) facilmente",
        "Me preocupo com as coisas",
        "Mudo de humor com facilidade",
        "Me irrito facilmente",
        // Abertura (16-19)
        "Tenho uma imaginação viva",
        "Tenho dificuldade de entender ideias abstratas",
        "Uso um vocabulário rico",
        "Não gosto de poesia"
    ]

    // Itens reversos (0-based): 1, 5, 9, 17, 19
    private let itensReversos: Set<Int> = [1, 5, 9, 17, 19]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                deepStepHeader(
                    titulo: "Personalidade aprofundada",
                    instrucao: "Para cada afirmação, indique o quanto ela descreve você.",
                    icone: "brain.head.profile"
                )

                Text("1 = Discordo totalmente  •  5 = Concordo totalmente")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .padding(.horizontal, 2)

                ForEach(fatores, id: \.nome) { fator in
                    VStack(alignment: .leading, spacing: 10) {
                        // Cabeçalho do fator
                        HStack(spacing: 8) {
                            RoundedRectangle(cornerRadius: 3)
                                .fill(fator.cor.gradient)
                                .frame(width: 4, height: 18)
                            Text(fator.nome)
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(fator.cor)
                        }

                        ForEach(fator.intervalo, id: \.self) { idx in
                            IPIP20ItemCard(
                                numero: idx + 1,
                                texto: itens[idx],
                                reverso: itensReversos.contains(idx),
                                cor: fator.cor,
                                value: Binding(
                                    get: { store.ipip20Responses[idx] },
                                    set: { store.send(.updateIPIP20(index: idx, value: $0)) }
                                )
                            )
                        }
                    }
                    .padding(14)
                    .background(Color(.secondarySystemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
        }
    }
}

// MARK: - IPIP20ItemCard

private struct IPIP20ItemCard: View {
    let numero: Int
    let texto: String
    let reverso: Bool
    let cor: Color
    @Binding var value: Int

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .top, spacing: 8) {
                Text("\(numero).")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(cor)
                    .frame(width: 24, alignment: .leading)
                Text(texto)
                    .font(.system(size: 13))
                    .foregroundColor(.primary)
                    .fixedSize(horizontal: false, vertical: true)
                if reverso {
                    Text("(R)")
                        .font(.system(size: 10, weight: .medium))
                        .foregroundColor(.secondary)
                }
            }
            DiscreteSlider(range: 1...5, value: $value, labels: ("Discordo", "Concordo"))
        }
        .padding(.vertical, 6)
    }
}

// MARK: - Passo 2: PVQ-21

private struct PVQ21StepView: View {
    let store: StoreOf<DeepModeFeature>

    private struct PVQItem {
        let indice: Int
        let texto: String
        let valor: SchwartzValue
    }

    private let grupos: [(titulo: String, valor: SchwartzValue, itens: [Int])] = [
        ("Conformidade",  .conformity,    [0, 10]),
        ("Tradição",      .tradition,     [1, 11]),
        ("Benevolência",  .benevolence,   [2, 12]),
        ("Universalismo", .universalism,  [3, 13, 19]),
        ("Autodireção",   .freedom,       [4, 14]),
        ("Estimulação",   .hedonism,      [5, 15]),
        ("Hedonismo",     .hedonism,      [6, 20]),
        ("Realização",    .achievement,   [7, 16]),
        ("Poder",         .power,         [8, 17]),
        ("Segurança",     .security,      [9, 18])
    ]

    private let textos: [String] = [
        // Conformidade (0, 10)
        "É importante para ele(a) sempre se comportar corretamente, evitar fazer algo que as pessoas consideram errado.",
        // Tradição (1, 11)
        "Para ele(a), é importante seguir os costumes de família ou religião. É importante para ele(a) respeitar as tradições da sua cultura.",
        // Benevolência (2, 12)
        "Para ele(a) é muito importante ajudar as pessoas ao redor. Ele(a) quer cuidar do seu bem-estar.",
        // Universalismo (3, 13, 19)
        "Para ele(a) é importante que todas as pessoas do mundo sejam tratadas igualmente. Ele(a) acredita que todos devem ter as mesmas oportunidades na vida.",
        // Autodireção (4, 14)
        "Pensar em novas ideias e ser criativo(a) é importante para ele(a). Ele(a) gosta de fazer as coisas do seu jeito, de forma original.",
        // Estimulação (5, 15)
        "Ele(a) gosta de surpresas e está sempre buscando novas coisas para fazer. Ele(a) acha que é importante ter muitas coisas diferentes na vida.",
        // Hedonismo (6, 20)
        "Ele(a) busca toda oportunidade de se divertir. Para ele(a) é importante fazer coisas que dão prazer.",
        // Realização (7, 16)
        "Ser muito bem-sucedido(a) é importante para ele(a). Ele(a) espera que as pessoas reconheçam suas conquistas.",
        // Poder (8, 17)
        "Ter muito dinheiro é importante para ele(a). Ele(a) quer ser rico(a).",
        // Segurança (9, 18)
        "Viver em lugar seguro é importante para ele(a). Ele(a) evita qualquer coisa que possa colocar sua segurança em risco.",
        // Conformidade cont. (10)
        "É muito importante para ele(a) obedecer as leis e regulamentos. Ele(a) acredita que as pessoas devem cumprir as regras sempre.",
        // Tradição cont. (11)
        "É muito importante para ele(a) ser humilde e modesto(a). Ele(a) tenta não chamar atenção para si.",
        // Benevolência cont. (12)
        "É muito importante para ele(a) ser leal aos amigos. Ele(a) se dedica às pessoas próximas.",
        // Universalismo cont. (13)
        "Ouvir e entender pessoas diferentes de si é importante para ele(a). Mesmo quando discorda, ainda quer entendê-las.",
        // Autodireção cont. (14)
        "É importante para ele(a) tomar suas próprias decisões. Ele(a) gosta de ser livre para planejar e escolher suas atividades.",
        // Estimulação cont. (15)
        "Ele(a) busca aventura e gosta de correr riscos. Ele(a) quer ter uma vida emocionante.",
        // Realização cont. (16)
        "É importante para ele(a) ser respeitado(a). Ele(a) quer que as pessoas façam o que diz.",
        // Poder cont. (17)
        "Para ele(a) é importante ser admirado(a). Ele(a) quer que as pessoas admirem o que faz.",
        // Segurança cont. (18)
        "É importante para ele(a) que o país esteja seguro e estável. Ele(a) se preocupa com a ordem do país.",
        // Universalismo cont. (19)
        "Ele(a) acredita fortemente que as pessoas devem cuidar da natureza. Cuidar do meio ambiente é importante para ele(a).",
        // Hedonismo cont. (20)
        "Aproveitar os prazeres da vida é importante para ele(a). Ele(a) gosta de se mimar."
    ]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                deepStepHeader(
                    titulo: "Seus valores mais profundos",
                    instrucao: "Leia cada descrição e indique o quanto essa pessoa se parece com você.",
                    icone: "sparkles"
                )

                HStack {
                    Text("1 = Se parece muito comigo")
                        .font(.caption)
                        .foregroundColor(.fypPink)
                    Spacer()
                    Text("6 = Não se parece nada")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .padding(.horizontal, 2)

                // Top 3 valores em tempo real
                let topVals = store.currentTopValues
                if !topVals.isEmpty {
                    HStack(spacing: 8) {
                        Text("Seus top valores agora:")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        ForEach(topVals.prefix(3), id: \.rawValue) { v in
                            Text("\(v.emoji) \(v.displayName)")
                                .font(.caption.bold())
                                .foregroundColor(.fypPink)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color.fypPink.opacity(0.1))
                                .clipShape(Capsule())
                        }
                    }
                    .padding(.bottom, 4)
                }

                ForEach(Array(textos.enumerated()), id: \.offset) { idx, texto in
                    PVQ21ItemCard(
                        numero: idx + 1,
                        texto: texto,
                        value: Binding(
                            get: { store.pvq21Responses[idx] },
                            set: { store.send(.updatePVQ21(index: idx, value: $0)) }
                        )
                    )
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
        }
    }
}

// MARK: - PVQ21ItemCard

private struct PVQ21ItemCard: View {
    let numero: Int
    let texto: String
    @Binding var value: Int

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(alignment: .top, spacing: 8) {
                Text("\(numero).")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundStyle(LinearGradient.fypGradient)
                    .frame(width: 24, alignment: .leading)
                Text(texto)
                    .font(.system(size: 13))
                    .foregroundColor(.primary)
                    .fixedSize(horizontal: false, vertical: true)
            }

            // Segmented 1-6 em chips
            HStack(spacing: 6) {
                Text("Muito")
                    .font(.system(size: 10))
                    .foregroundColor(.fypPink)
                ForEach(1...6, id: \.self) { n in
                    Button {
                        withAnimation(.easeInOut(duration: 0.15)) { value = n }
                    } label: {
                        Text("\(n)")
                            .font(.system(size: 13, weight: value == n ? .bold : .regular))
                            .foregroundColor(value == n ? .white : .primary)
                            .frame(width: 34, height: 34)
                            .background(
                                value == n
                                    ? AnyShapeStyle(LinearGradient.fypGradient)
                                    : AnyShapeStyle(Color(.systemBackground))
                            )
                            .clipShape(Circle())
                            .overlay(
                                Circle().stroke(
                                    value == n ? Color.clear : Color(.systemGray4),
                                    lineWidth: 1
                                )
                            )
                    }
                    .buttonStyle(.plain)
                }
                Text("Nada")
                    .font(.system(size: 10))
                    .foregroundColor(.secondary)
            }
        }
        .padding(14)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}

// MARK: - Passo 3: ECR-RS

private struct ECRRSStepView: View {
    let store: StoreOf<DeepModeFeature>

    private let itens: [String] = [
        // Ansiedade (0-5)
        "Tenho medo de perder o amor do meu parceiro(a).",
        "Me preocupo muito com meus relacionamentos.",
        "Frequentemente me preocupo que meu parceiro(a) não queira ficar comigo.",
        "Quero ficar muito próximo(a) do meu parceiro(a), mas acabo afastando-o(a).",
        "Me preocupo com a possibilidade de ser abandonado(a).",
        "Minha insegurança nos relacionamentos me causa muita ansiedade.",
        // Evitação (6-11)
        "Prefiro não me apegar emocionalmente ao meu parceiro(a).",
        "Fico desconfortável ao me sentir próximo(a) de outra pessoa.",
        "Acho difícil permitir que meu parceiro(a) dependa de mim.",
        "Prefiro não compartilhar meus sentimentos profundos com meu parceiro(a).",
        "Me sinto desconfortável quando me abro com meu parceiro(a).",
        "Prefiro manter distância emocional mesmo em relacionamentos."
    ]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                deepStepHeader(
                    titulo: "Seu estilo de vínculo",
                    instrucao: "Pense em como você geralmente se sente em relacionamentos românticos.",
                    icone: "link"
                )

                Text("1 = Discordo totalmente  •  7 = Concordo totalmente")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .padding(.horizontal, 2)

                // Grupo Ansiedade
                VStack(alignment: .leading, spacing: 10) {
                    HStack(spacing: 8) {
                        RoundedRectangle(cornerRadius: 3)
                            .fill(Color.orange.gradient)
                            .frame(width: 4, height: 18)
                        Text("Ansiedade de apego")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.orange)
                    }

                    ForEach(0..<6, id: \.self) { idx in
                        ECRRSItemCard(
                            numero: idx + 1,
                            texto: itens[idx],
                            value: Binding(
                                get: { store.ecrrResponses[idx] },
                                set: { store.send(.updateECRRS(index: idx, value: $0)) }
                            )
                        )
                    }
                }
                .padding(14)
                .background(Color(.secondarySystemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 16))

                // Grupo Evitação
                VStack(alignment: .leading, spacing: 10) {
                    HStack(spacing: 8) {
                        RoundedRectangle(cornerRadius: 3)
                            .fill(Color.blue.gradient)
                            .frame(width: 4, height: 18)
                        Text("Evitação de apego")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.blue)
                    }

                    ForEach(6..<12, id: \.self) { idx in
                        ECRRSItemCard(
                            numero: idx + 1,
                            texto: itens[idx],
                            value: Binding(
                                get: { store.ecrrResponses[idx] },
                                set: { store.send(.updateECRRS(index: idx, value: $0)) }
                            )
                        )
                    }
                }
                .padding(14)
                .background(Color(.secondarySystemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 16))

                // Preview do estilo calculado
                let estilo = store.currentAttachmentStyle
                AttachmentStylePreviewCard(style: estilo)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
        }
    }
}

// MARK: - ECRRSItemCard

private struct ECRRSItemCard: View {
    let numero: Int
    let texto: String
    @Binding var value: Int

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .top, spacing: 8) {
                Text("\(numero).")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundStyle(LinearGradient.fypGradient)
                    .frame(width: 24, alignment: .leading)
                Text(texto)
                    .font(.system(size: 13))
                    .foregroundColor(.primary)
                    .fixedSize(horizontal: false, vertical: true)
            }
            DiscreteSlider(range: 1...7, value: $value, labels: ("Discordo", "Concordo"))
        }
        .padding(.vertical, 6)
    }
}

// MARK: - AttachmentStylePreviewCard

private struct AttachmentStylePreviewCard: View {
    let style: AttachmentStyle

    private var bgColor: Color {
        switch style {
        case .secure:       return .green
        case .anxious:      return .yellow
        case .avoidant:     return .gray
        case .disorganized: return .purple
        }
    }

    var body: some View {
        VStack(spacing: 10) {
            HStack(spacing: 10) {
                Text(style.emoji)
                    .font(.system(size: 28))
                VStack(alignment: .leading, spacing: 2) {
                    Text("Estilo de apego: \(style.displayName)")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.primary)
                    Text(style.description)
                        .font(.system(size: 12))
                        .foregroundColor(.secondary)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            Text("Este preview atualiza conforme você responde. Será calculado ao salvar.")
                .font(.caption2)
                .foregroundColor(.secondary)
        }
        .padding(14)
        .background(bgColor.opacity(0.1))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(bgColor.opacity(0.3), lineWidth: 1)
        )
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}

// MARK: - Passo 4: Conflito Profundo

private struct ConflictDeepStepView: View {
    @Bindable var store: StoreOf<DeepModeFeature>

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                deepStepHeader(
                    titulo: "Como você lida com conflitos",
                    instrucao: "Escolha a opção que melhor representa você em cada situação.",
                    icone: "flame.fill"
                )

                DeepPerguntaPills(
                    titulo: "Como você costuma lidar com conflitos?",
                    opcoes: ConflictResolutionStyle.allCases.map { ($0.displayName, $0) },
                    selecionado: $store.conflictResolutionStyle
                )

                DeepPerguntaPills(
                    titulo: "Como você expressa emoções difíceis?",
                    opcoes: EmotionalExpression.allCases.map { ($0.displayName, $0) },
                    selecionado: $store.emotionalExpression
                )

                DeepPerguntaPills(
                    titulo: "Depois de uma briga, você...",
                    opcoes: RepairBehavior.allCases.map { ($0.displayName, $0) },
                    selecionado: $store.repairBehavior
                )

                DeepPerguntaPills(
                    titulo: "Quanto tempo você precisa de espaço após um conflito?",
                    opcoes: SilencePeriod.allCases.map { ($0.displayName, $0) },
                    selecionado: $store.silencePeriod
                )

                DeepPerguntaPills(
                    titulo: "Quando você erra, prefere...",
                    opcoes: ApologyStyle.allCases.map { ($0.displayName, $0) },
                    selecionado: $store.apologyStyle
                )

                DeepPerguntaPills(
                    titulo: "Como você recebe críticas?",
                    opcoes: FeedbackTolerance.allCases.map { ($0.displayName, $0) },
                    selecionado: $store.feedbackTolerance
                )
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
        }
    }
}

// MARK: - Passo 5: Projeto de Vida

private struct LifeProjectStepView: View {
    @Bindable var store: StoreOf<DeepModeFeature>

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                deepStepHeader(
                    titulo: "Projeto de vida compartilhado",
                    instrucao: "Suas expectativas para o futuro em 5 dimensões importantes.",
                    icone: "map.fill"
                )

                DeepPerguntaPills(
                    titulo: "Sobre ter filhos:",
                    opcoes: ChildrenDesire.allCases.map { ($0.displayName, $0) },
                    selecionado: $store.childrenDesire
                )

                DeepPerguntaPills(
                    titulo: "Flexibilidade de morar em outro lugar:",
                    opcoes: LocationFlexibility.allCases.map { ($0.displayName, $0) },
                    selecionado: $store.locationFlexibility
                )

                DeepPerguntaPills(
                    titulo: "Carreira na sua vida:",
                    opcoes: CareerPriority.allCases.map { ($0.displayName, $0) },
                    selecionado: $store.careerPriority
                )

                DeepPerguntaPills(
                    titulo: "Relação com dinheiro:",
                    opcoes: FinancialApproach.allCases.map { ($0.displayName, $0) },
                    selecionado: $store.financialApproach
                )

                DeepPerguntaPills(
                    titulo: "Espiritualidade e religião:",
                    opcoes: SpiritualityRole.allCases.map { ($0.displayName, $0) },
                    selecionado: $store.spiritualityRole
                )
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
        }
    }
}

// MARK: - DeepPerguntaPills (Pills para enums — versão Deep Mode)

private struct DeepPerguntaPills<T: Equatable & Hashable>: View {
    let titulo: String
    let opcoes: [(String, T)]
    @Binding var selecionado: T

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(titulo)
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(.primary)

            VStack(spacing: 8) {
                ForEach(Array(opcoes.enumerated()), id: \.offset) { _, opcao in
                    Button {
                        withAnimation(.easeInOut(duration: 0.15)) {
                            selecionado = opcao.1
                        }
                    } label: {
                        HStack {
                            Text(opcao.0)
                                .font(.system(size: 13, weight: selecionado == opcao.1 ? .semibold : .regular))
                                .foregroundColor(selecionado == opcao.1 ? .white : .primary)
                            Spacer()
                            if selecionado == opcao.1 {
                                Image(systemName: "checkmark")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(.white)
                            }
                        }
                        .padding(.horizontal, 14)
                        .padding(.vertical, 11)
                        .background(
                            selecionado == opcao.1
                                ? AnyShapeStyle(LinearGradient.fypGradient)
                                : AnyShapeStyle(Color(.systemBackground))
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(
                                    selecionado == opcao.1 ? Color.clear : Color(.systemGray4),
                                    lineWidth: 1
                                )
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        .padding(14)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}

// MARK: - DiscreteSlider

struct DiscreteSlider: View {
    let range: ClosedRange<Int>
    @Binding var value: Int
    let labels: (String, String)?

    var body: some View {
        VStack(spacing: 4) {
            HStack(spacing: 4) {
                ForEach(range, id: \.self) { n in
                    Button {
                        withAnimation(.easeInOut(duration: 0.12)) { value = n }
                    } label: {
                        Text("\(n)")
                            .font(.system(size: 11, weight: value == n ? .bold : .regular))
                            .foregroundColor(value == n ? .white : .secondary)
                            .frame(maxWidth: .infinity)
                            .frame(height: 28)
                            .background(
                                value == n
                                    ? AnyShapeStyle(LinearGradient.fypGradient)
                                    : AnyShapeStyle(Color(.systemBackground))
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 6))
                            .overlay(
                                RoundedRectangle(cornerRadius: 6)
                                    .stroke(
                                        value == n ? Color.clear : Color(.systemGray5),
                                        lineWidth: 1
                                    )
                            )
                    }
                    .buttonStyle(.plain)
                }
            }

            if let labels = labels {
                HStack {
                    Text(labels.0)
                    Spacer()
                    Text(labels.1)
                }
                .font(.system(size: 9))
                .foregroundColor(.secondary)
            }
        }
    }
}

// MARK: - DeepModeCompleteView

struct DeepModeCompleteView: View {
    let store: StoreOf<DeepModeFeature>

    @State private var checkmarkVisible: Bool = false

    private var attachmentStyle: AttachmentStyle {
        store.currentAttachmentStyle
    }

    private var topValues: [SchwartzValue] {
        store.currentTopValues
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 28) {
                Spacer(minLength: 40)

                // Ícone animado com spring
                ZStack {
                    Circle()
                        .fill(LinearGradient.fypGradient)
                        .frame(width: 100, height: 100)
                        .shadow(color: .fypPink.opacity(0.4), radius: 18)

                    Image(systemName: "checkmark")
                        .font(.system(size: 44, weight: .bold))
                        .foregroundColor(.white)
                }
                .scaleEffect(checkmarkVisible ? 1.0 : 0.4)
                .animation(.spring(response: 0.5, dampingFraction: 0.55).delay(0.1), value: checkmarkVisible)

                VStack(spacing: 8) {
                    Text("Modo Profundo concluído! 🎉")
                        .font(.title2.bold())
                        .multilineTextAlignment(.center)
                    Text("Seu perfil de compatibilidade ficou muito mais preciso.")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                }

                // Card: Estilo de apego
                VStack(alignment: .leading, spacing: 10) {
                    Text("Seu estilo de apego")
                        .font(.caption)
                        .foregroundColor(.secondary)

                    HStack(spacing: 12) {
                        Text(attachmentStyle.emoji)
                            .font(.system(size: 36))
                        VStack(alignment: .leading, spacing: 4) {
                            Text(attachmentStyle.displayName)
                                .font(.system(size: 17, weight: .bold))
                            Text(attachmentStyle.description)
                                .font(.system(size: 13))
                                .foregroundColor(.secondary)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                }
                .padding(18)
                .background(Color(.secondarySystemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 16))
                .padding(.horizontal, 20)

                // Card: Top 3 valores PVQ
                VStack(alignment: .leading, spacing: 10) {
                    Text("Seus 3 valores principais")
                        .font(.caption)
                        .foregroundColor(.secondary)

                    ForEach(Array(topValues.prefix(3).enumerated()), id: \.offset) { idx, val in
                        HStack(spacing: 10) {
                            Text("\(idx + 1)")
                                .font(.system(size: 12, weight: .black))
                                .foregroundColor(.white)
                                .frame(width: 22, height: 22)
                                .background(LinearGradient.fypGradient)
                                .clipShape(Circle())

                            Text(val.emoji)
                                .font(.system(size: 18))

                            VStack(alignment: .leading, spacing: 1) {
                                Text(val.displayName)
                                    .font(.system(size: 14, weight: .semibold))
                                Text(val.description)
                                    .font(.system(size: 11))
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                }
                .padding(18)
                .background(Color(.secondarySystemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 16))
                .padding(.horizontal, 20)

                Text("Seus novos matches vão refletir isso.")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 20)

                GradientButton(title: "Continuar", icon: "arrow.right") {
                    store.send(.dismiss)
                }
                .padding(.horizontal, 20)

                Spacer(minLength: 40)
            }
        }
        .onAppear {
            withAnimation { checkmarkVisible = true }
        }
    }
}

// MARK: - Helpers privados

/// Cabeçalho padrão de cada passo do Deep Mode
private func deepStepHeader(titulo: String, instrucao: String, icone: String) -> some View {
    VStack(alignment: .leading, spacing: 6) {
        HStack(spacing: 8) {
            Image(systemName: icone)
                .foregroundStyle(LinearGradient.fypGradient)
                .font(.system(size: 20, weight: .semibold))
            Text(titulo)
                .font(.title3.bold())
        }
        Text(instrucao)
            .font(.subheadline)
            .foregroundColor(.secondary)
    }
}

extension Array {
    subscript(safe index: Int) -> Element? {
        guard indices.contains(index) else { return nil }
        return self[index]
    }
}

// MARK: - Preview

#if DEBUG
struct DeepModeView_Previews: PreviewProvider {
    static var previews: some View {
        DeepModeView(
            store: Store(
                initialState: DeepModeFeature.State(userId: "preview-user"),
                reducer: { DeepModeFeature() }
            )
        )
    }
}
#endif
