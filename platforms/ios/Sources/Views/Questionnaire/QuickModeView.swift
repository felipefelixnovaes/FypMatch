// QuickModeView.swift — FypMatch iOS
// Questionário de compatibilidade — wizard animado de 5 etapas

import SwiftUI
import ComposableArchitecture

// MARK: - QuickModeView (Wizard principal)

struct QuickModeView: View {
    let userId: String
    @Environment(\.dismiss) private var dismiss

    // Estado do wizard
    @State private var etapa: Int = 1
    @State private var animating: Bool = false

    // Etapa 1 — TIPI (10 sliders)
    @State private var tipiRespostas: [Int: Double] = [:]

    // Etapa 2 — Valores (top 3)
    @State private var valoresSelecionados: [SchwartzValue] = []

    // Etapa 3 — Comunicação
    @State private var conflictStyle: ConflictStyle?
    @State private var messagingFreq: MessagingFrequency?
    @State private var anxietyWhenSilent: AnxietyWhenSilent?
    @State private var conversationDepth: ConversationDepth?
    @State private var conflictMedium: ConflictMedium?

    // Etapa 4 — Rotina
    @State private var weekendStyle: WeekendStyle?
    @State private var routineVsSpontaneous: RoutineVsSpontaneous?
    @State private var energySource: EnergySource?
    @State private var workLifeBalance: WorkLifeBalance?
    @State private var homeNoise: HomeNoise?

    // Etapa 5 — Deal-breakers
    @State private var dealBreakers: Set<DealBreaker> = []

    // Estado de conclusão
    @State private var salvando: Bool = false
    @State private var concluido: Bool = false
    @State private var erroMsg: String? = nil
    @State private var questionarioFinal: UserQuestionnaire? = nil

    private var podeProsseguir: Bool {
        switch etapa {
        case 1: return tipiRespostas.count == 10
        case 2: return valoresSelecionados.count == 3
        case 3: return conflictStyle != nil && messagingFreq != nil
                    && anxietyWhenSilent != nil && conversationDepth != nil
                    && conflictMedium != nil
        case 4: return weekendStyle != nil && routineVsSpontaneous != nil
                    && energySource != nil && workLifeBalance != nil
                    && homeNoise != nil
        case 5: return true // deal-breakers são opcionais
        default: return false
        }
    }

    var body: some View {
        NavigationStack {
            ZStack {
                if concluido, let q = questionarioFinal {
                    ConclusaoView(questionnaire: q, dismiss: { dismiss() })
                        .transition(.asymmetric(
                            insertion: .move(edge: .trailing).combined(with: .opacity),
                            removal: .opacity
                        ))
                } else {
                    wizardBody
                }
            }
            .animation(.easeInOut(duration: 0.35), value: concluido)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    if !concluido {
                        Button("Fechar") { dismiss() }
                            .foregroundColor(.fypPink)
                    }
                }
            }
        }
    }

    // MARK: - Corpo do wizard

    private var wizardBody: some View {
        VStack(spacing: 0) {
            // ── Barra de progresso
            VStack(spacing: 6) {
                ProgressView(value: Double(etapa), total: 5.0)
                    .tint(.fypPink)
                    .padding(.horizontal, 20)
                    .animation(.easeInOut, value: etapa)

                Text("Etapa \(etapa) de 5")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .padding(.top, 12)
            .padding(.bottom, 8)

            // ── Conteúdo animado
            ScrollView {
                ZStack {
                    switch etapa {
                    case 1: etapa1View.transition(transicao)
                    case 2: etapa2View.transition(transicao)
                    case 3: etapa3View.transition(transicao)
                    case 4: etapa4View.transition(transicao)
                    case 5: etapa5View.transition(transicao)
                    default: EmptyView()
                    }
                }
                .animation(.easeInOut(duration: 0.3), value: etapa)
                .padding(.horizontal, 20)
                .padding(.top, 8)
                .padding(.bottom, 24)
            }

            // ── Botões de navegação
            HStack(spacing: 12) {
                if etapa > 1 {
                    OutlineButton(title: "Voltar", icon: "chevron.left", isFullWidth: false) {
                        withAnimation(.easeInOut) { etapa -= 1 }
                    }
                    .frame(maxWidth: 120)
                }

                if etapa < 5 {
                    GradientButton(title: "Próximo", icon: "chevron.right") {
                        guard podeProsseguir else { return }
                        withAnimation(.easeInOut) { etapa += 1 }
                    }
                    .opacity(podeProsseguir ? 1 : 0.45)
                    .disabled(!podeProsseguir)
                } else {
                    GradientButton(
                        title: salvando ? "Salvando..." : "Concluir",
                        icon: salvando ? nil : "checkmark",
                        isLoading: salvando
                    ) {
                        Task { await concluir() }
                    }
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 24)

            if let erro = erroMsg {
                Text(erro)
                    .font(.caption)
                    .foregroundColor(.red)
                    .padding(.bottom, 8)
            }
        }
    }

    private var transicao: AnyTransition {
        .asymmetric(
            insertion: .move(edge: .trailing).combined(with: .opacity),
            removal: .move(edge: .leading).combined(with: .opacity)
        )
    }

    // MARK: - Etapa 1: TIPI-10

    private var etapa1View: some View {
        VStack(alignment: .leading, spacing: 16) {
            etapaHeader(
                titulo: "Personalidade",
                instrucao: "Para cada afirmação, indique o quanto ela descreve você",
                icone: "person.fill"
            )

            ForEach(tipiItems, id: \.numero) { item in
                TIPIItemCard(
                    item: item,
                    valor: Binding(
                        get: { tipiRespostas[item.numero] ?? 4.0 },
                        set: { tipiRespostas[item.numero] = $0 }
                    )
                )
            }
        }
    }

    // MARK: - Etapa 2: Valores Schwartz

    private var etapa2View: some View {
        VStack(alignment: .leading, spacing: 16) {
            etapaHeader(
                titulo: "Seus Valores",
                instrucao: "Selecione seus 3 valores mais importantes (em ordem de prioridade)",
                icone: "heart.fill"
            )

            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                ForEach(SchwartzValue.allCases, id: \.rawValue) { valor in
                    ValorCard(
                        valor: valor,
                        ordem: valoresSelecionados.firstIndex(of: valor).map { $0 + 1 },
                        onTap: {
                            toggleValor(valor)
                        }
                    )
                }
            }

            if valoresSelecionados.count < 3 {
                Text("Selecione \(3 - valoresSelecionados.count) mais")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
            }
        }
    }

    // MARK: - Etapa 3: Comunicação

    private var etapa3View: some View {
        VStack(alignment: .leading, spacing: 20) {
            etapaHeader(
                titulo: "Comunicação",
                instrucao: "Como você funciona dentro de um relacionamento?",
                icone: "bubble.left.and.bubble.right.fill"
            )

            PerguntaPills(
                titulo: "Quando estou chateado(a), prefiro...",
                opcoes: [
                    ("Conversar na hora", ConflictStyle.conversa),
                    ("Esperar esfriar", ConflictStyle.espera),
                    ("Me afastar", ConflictStyle.afasta)
                ],
                selecionado: $conflictStyle
            )

            PerguntaPills(
                titulo: "Mensagens ao longo do dia...",
                opcoes: [
                    ("Gosto de frequência", MessagingFrequency.alta),
                    ("Depende do contexto", MessagingFrequency.contextual),
                    ("Prefiro espaço", MessagingFrequency.baixa)
                ],
                selecionado: $messagingFreq
            )

            PerguntaPills(
                titulo: "Quando alguém some por horas...",
                opcoes: [
                    ("Normal", AnxietyWhenSilent.normal),
                    ("Fico um pouco ansioso(a)", AnxietyWhenSilent.umPouco),
                    ("Me preocupo muito", AnxietyWhenSilent.muito)
                ],
                selecionado: $anxietyWhenSilent
            )

            PerguntaPills(
                titulo: "Prefiro conversas...",
                opcoes: [
                    ("Diretas e objetivas", ConversationDepth.direta),
                    ("Longas e profundas", ConversationDepth.profunda),
                    ("Misto", ConversationDepth.misto)
                ],
                selecionado: $conversationDepth
            )

            PerguntaPills(
                titulo: "Conflito prefiro resolver...",
                opcoes: [
                    ("Na hora", ConflictMedium.naHora),
                    ("Depois que esfria", ConflictMedium.depoisEsfria),
                    ("Por escrito antes", ConflictMedium.porEscrito)
                ],
                selecionado: $conflictMedium
            )
        }
    }

    // MARK: - Etapa 4: Rotina e energia

    private var etapa4View: some View {
        VStack(alignment: .leading, spacing: 20) {
            etapaHeader(
                titulo: "Ritmo de Vida",
                instrucao: "Como é o seu ritmo de vida?",
                icone: "sun.max.fill"
            )

            PerguntaPills(
                titulo: "Fim de semana ideal",
                opcoes: [
                    ("Em casa 🏠", WeekendStyle.emCasa),
                    ("Equilíbrio ⚖️", WeekendStyle.equilibrio),
                    ("Sair e explorar 🗺️", WeekendStyle.sairExplorar)
                ],
                selecionado: $weekendStyle
            )

            PerguntaPills(
                titulo: "Sou mais...",
                opcoes: [
                    ("Pessoa de rotina 📅", RoutineVsSpontaneous.rotina),
                    ("Espontâneo(a) 🎲", RoutineVsSpontaneous.espontaneo),
                    ("Depende", RoutineVsSpontaneous.depende)
                ],
                selecionado: $routineVsSpontaneous
            )

            PerguntaPills(
                titulo: "Me recarrego...",
                opcoes: [
                    ("Sozinho(a), preciso de silêncio 🤫", EnergySource.introvertido),
                    ("Com as pessoas 🎊", EnergySource.extrovertido)
                ],
                selecionado: $energySource
            )

            PerguntaPills(
                titulo: "Trabalho e vida...",
                opcoes: [
                    ("Alta ambição 🚀", WorkLifeBalance.altaAmbi),
                    ("Equilíbrio ⚖️", WorkLifeBalance.equilibrio),
                    ("Qualidade de vida primeiro 🌻", WorkLifeBalance.qualidadeVida)
                ],
                selecionado: $workLifeBalance
            )

            PerguntaPills(
                titulo: "Em casa preciso de...",
                opcoes: [
                    ("Silêncio 🤫", HomeNoise.silencio),
                    ("Um pouco de som 🎵", HomeNoise.umPoucoSom),
                    ("Qualquer ambiente", HomeNoise.qualquer)
                ],
                selecionado: $homeNoise
            )
        }
    }

    // MARK: - Etapa 5: Deal-breakers

    private var etapa5View: some View {
        VStack(alignment: .leading, spacing: 16) {
            etapaHeader(
                titulo: "Limites",
                instrucao: "O que seria inaceitável para você em um parceiro(a)? (selecione quantos quiser)",
                icone: "exclamationmark.triangle.fill"
            )

            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                ForEach(DealBreaker.allCases, id: \.rawValue) { db in
                    DealBreakerChip(
                        dealBreaker: db,
                        selecionado: dealBreakers.contains(db),
                        onTap: { toggleDealBreaker(db) }
                    )
                }
            }

            if dealBreakers.isEmpty {
                Text("Nenhum selecionado — você pode prosseguir assim mesmo")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)
                    .padding(.top, 4)
            }
        }
    }

    // MARK: - Helpers de cabeçalho

    private func etapaHeader(titulo: String, instrucao: String, icone: String) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 8) {
                Image(systemName: icone)
                    .foregroundStyle(LinearGradient.fypGradient)
                    .font(.system(size: 18, weight: .semibold))
                Text(titulo)
                    .font(.title3.bold())
            }
            Text(instrucao)
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .padding(.bottom, 4)
    }

    // MARK: - Toggle helpers

    private func toggleValor(_ valor: SchwartzValue) {
        withAnimation(.easeInOut(duration: 0.2)) {
            if let idx = valoresSelecionados.firstIndex(of: valor) {
                valoresSelecionados.remove(at: idx)
            } else if valoresSelecionados.count < 3 {
                valoresSelecionados.append(valor)
            }
        }
    }

    private func toggleDealBreaker(_ db: DealBreaker) {
        withAnimation(.easeInOut(duration: 0.15)) {
            if dealBreakers.contains(db) {
                dealBreakers.remove(db)
            } else {
                dealBreakers.insert(db)
            }
        }
    }

    // MARK: - Conclusão

    private func buildQuestionnaire() -> UserQuestionnaire {
        var q = UserQuestionnaire(userId: userId)

        // Big Five a partir do TIPI
        q.bigFive = calcularBigFive()

        // Valores
        if valoresSelecionados.count == 3 {
            q.values = ValuesResult(top3: valoresSelecionados)
        }

        // Comunicação
        if let cs = conflictStyle, let mf = messagingFreq,
           let aws = anxietyWhenSilent, let cd = conversationDepth,
           let cm = conflictMedium {
            q.communication = CommunicationResult(
                conflictStyle: cs,
                messagingFrequency: mf,
                anxietyWhenSilent: aws,
                conversationDepth: cd,
                conflictMedium: cm
            )
        }

        // Rotina
        if let ws = weekendStyle, let rvs = routineVsSpontaneous,
           let es = energySource, let wlb = workLifeBalance,
           let hn = homeNoise {
            q.routine = RoutineResult(
                energySource: es,
                weekendStyle: ws,
                routineVsSpontaneous: rvs,
                workLifeBalance: wlb,
                homeNoise: hn
            )
        }

        q.dealBreakers = Array(dealBreakers)
        q.completedAt = Date()
        return q
    }

    /// Calcula BigFiveResult a partir das respostas TIPI com reversão dos itens marcados
    private func calcularBigFive() -> BigFiveResult? {
        guard tipiRespostas.count == 10 else { return nil }

        func resp(_ n: Int, reverso: Bool = false) -> Int {
            let val = Int((tipiRespostas[n] ?? 4.0).rounded())
            return reverso ? (8 - val) : val
        }

        // TIPI scoring:
        // Extraversão:       itens 1 (direto) e 6 (reverso)
        // Amabilidade:       itens 7 (direto) e 2 (reverso)
        // Conscienciosidade: itens 3 (direto) e 8 (reverso)
        // Neuroticismo:      itens 4 (direto) e 9 (reverso)
        // Abertura:          itens 5 (direto) e 10 (reverso)

        let extraversion     = (resp(1) + resp(6, reverso: true)) / 2
        let agreeableness    = (resp(7) + resp(2, reverso: true)) / 2
        let conscientiousness = (resp(3) + resp(8, reverso: true)) / 2
        let neuroticism      = (resp(4) + resp(9, reverso: true)) / 2
        let openness         = (resp(5) + resp(10, reverso: true)) / 2

        return BigFiveResult(
            openness: openness,
            conscientiousness: conscientiousness,
            extraversion: extraversion,
            agreeableness: agreeableness,
            neuroticism: neuroticism
        )
    }

    private func concluir() async {
        guard !salvando else { return }
        salvando = true
        erroMsg = nil

        let q = buildQuestionnaire()

        do {
            try await QuestionnaireService.liveValue.saveQuestionnaire(userId, q)
            withAnimation(.easeInOut) {
                questionarioFinal = q
                concluido = true
            }
        } catch {
            erroMsg = "Erro ao salvar: \(error.localizedDescription)"
        }

        salvando = false
    }
}

// MARK: - TIPI Item Model

private struct TIPIItem {
    let numero: Int
    let texto: String
    let reverso: Bool
}

private let tipiItems: [TIPIItem] = [
    TIPIItem(numero: 1,  texto: "Extrovertido(a) e entusiasmado(a)",             reverso: false),
    TIPIItem(numero: 2,  texto: "Crítico(a) e conflituoso(a)",                    reverso: true),
    TIPIItem(numero: 3,  texto: "Confiável e autodisciplinado(a)",                reverso: false),
    TIPIItem(numero: 4,  texto: "Ansioso(a) e facilmente perturbado(a)",          reverso: false),
    TIPIItem(numero: 5,  texto: "Aberto(a) a novas experiências, curioso(a)",     reverso: false),
    TIPIItem(numero: 6,  texto: "Reservado(a) e quieto(a)",                       reverso: true),
    TIPIItem(numero: 7,  texto: "Simpático(a) e afetuoso(a)",                     reverso: false),
    TIPIItem(numero: 8,  texto: "Desorganizado(a) e descuidado(a)",               reverso: true),
    TIPIItem(numero: 9,  texto: "Calmo(a) e emocionalmente estável",              reverso: true),
    TIPIItem(numero: 10, texto: "Convencional e pouco criativo(a)",               reverso: true),
]

// MARK: - TIPIItemCard

private struct TIPIItemCard: View {
    let item: TIPIItem
    @Binding var valor: Double

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(alignment: .top, spacing: 10) {
                Text("\(item.numero).")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundStyle(LinearGradient.fypGradient)
                    .frame(width: 22, alignment: .leading)

                Text(item.texto)
                    .font(.system(size: 14))
                    .foregroundColor(.primary)
                    .fixedSize(horizontal: false, vertical: true)
            }

            VStack(spacing: 4) {
                Slider(value: $valor, in: 1...7, step: 1)
                    .tint(.fypPink)

                HStack {
                    Text("Discordo")
                    Spacer()
                    Text("\(Int(valor.rounded()))")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(.fypPink)
                    Spacer()
                    Text("Concordo")
                }
                .font(.caption2)
                .foregroundColor(.secondary)
            }
        }
        .padding(14)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}

// MARK: - ValorCard

private struct ValorCard: View {
    let valor: SchwartzValue
    let ordem: Int?
    let onTap: () -> Void

    var selecionado: Bool { ordem != nil }

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 6) {
                ZStack(alignment: .topTrailing) {
                    Text(valor.emoji)
                        .font(.system(size: 28))
                        .frame(maxWidth: .infinity)

                    if let o = ordem {
                        Text("\(o)")
                            .font(.system(size: 11, weight: .black))
                            .foregroundColor(.white)
                            .frame(width: 20, height: 20)
                            .background(Color.fypPink)
                            .clipShape(Circle())
                    }
                }
                Text(valor.displayName)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(selecionado ? .white : .primary)
                    .multilineTextAlignment(.center)
            }
            .padding(.vertical, 14)
            .padding(.horizontal, 8)
            .frame(maxWidth: .infinity)
            .background(
                selecionado
                    ? AnyShapeStyle(LinearGradient.fypGradient)
                    : AnyShapeStyle(Color(.secondarySystemBackground))
            )
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(selecionado ? Color.clear : Color(.systemGray4), lineWidth: 1)
            )
            .scaleEffect(selecionado ? 1.03 : 1.0)
            .animation(.spring(response: 0.3, dampingFraction: 0.6), value: selecionado)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - PerguntaPills (genérico)

private struct PerguntaPills<T: Equatable & Hashable>: View {
    let titulo: String
    let opcoes: [(String, T)]
    @Binding var selecionado: T?

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(titulo)
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(.primary)

            FlowLayout(spacing: 8) {
                ForEach(Array(opcoes.enumerated()), id: \.offset) { _, opcao in
                    PillButton(
                        titulo: opcao.0,
                        selecionado: selecionado == opcao.1
                    ) {
                        withAnimation(.easeInOut(duration: 0.15)) {
                            selecionado = opcao.1
                        }
                    }
                }
            }
        }
        .padding(14)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}

// MARK: - PillButton

private struct PillButton: View {
    let titulo: String
    let selecionado: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            Text(titulo)
                .font(.system(size: 13, weight: selecionado ? .semibold : .regular))
                .foregroundColor(selecionado ? .white : .primary)
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
                .background(
                    selecionado
                        ? AnyShapeStyle(LinearGradient.fypGradient)
                        : AnyShapeStyle(Color(.systemBackground))
                )
                .clipShape(Capsule())
                .overlay(
                    Capsule()
                        .stroke(selecionado ? Color.clear : Color(.systemGray4), lineWidth: 1)
                )
        }
        .buttonStyle(.plain)
    }
}

// MARK: - DealBreakerChip

private struct DealBreakerChip: View {
    let dealBreaker: DealBreaker
    let selecionado: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 6) {
                Text(dealBreaker.emoji)
                    .font(.system(size: 14))
                Text(dealBreaker.displayName)
                    .font(.system(size: 12, weight: selecionado ? .semibold : .regular))
                    .multilineTextAlignment(.leading)
                    .fixedSize(horizontal: false, vertical: true)
            }
            .foregroundColor(selecionado ? .white : .primary)
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(
                selecionado
                    ? AnyShapeStyle(Color.red.opacity(0.85))
                    : AnyShapeStyle(Color(.secondarySystemBackground))
            )
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(selecionado ? Color.red : Color(.systemGray4), lineWidth: 1)
            )
            .scaleEffect(selecionado ? 1.02 : 1.0)
            .animation(.spring(response: 0.25, dampingFraction: 0.7), value: selecionado)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - FlowLayout (quebra automática de pills)

private struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let maxWidth = proposal.width ?? .infinity
        var height: CGFloat = 0
        var x: CGFloat = 0
        var rowHeight: CGFloat = 0

        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if x + size.width > maxWidth && x > 0 {
                height += rowHeight + spacing
                x = 0
                rowHeight = 0
            }
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
        height += rowHeight
        return CGSize(width: maxWidth, height: height)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x = bounds.minX
        var y = bounds.minY
        var rowHeight: CGFloat = 0

        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if x + size.width > bounds.maxX && x > bounds.minX {
                y += rowHeight + spacing
                x = bounds.minX
                rowHeight = 0
            }
            view.place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(size))
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
    }
}

// MARK: - ConclusaoView

private struct ConclusaoView: View {
    let questionnaire: UserQuestionnaire
    let dismiss: () -> Void

    @State private var confettiVisible: Bool = false
    @State private var particulas: [ParticulaConfetti] = []

    var body: some View {
        ZStack {
            ScrollView {
                VStack(spacing: 28) {
                    Spacer(minLength: 40)

                    // Ícone animado
                    ZStack {
                        Circle()
                            .fill(LinearGradient.fypGradient)
                            .frame(width: 96, height: 96)
                            .shadow(color: .fypPink.opacity(0.4), radius: 16)

                        Image(systemName: "checkmark")
                            .font(.system(size: 40, weight: .bold))
                            .foregroundColor(.white)
                    }
                    .scaleEffect(confettiVisible ? 1.0 : 0.6)
                    .animation(.spring(response: 0.5, dampingFraction: 0.6).delay(0.1), value: confettiVisible)

                    VStack(spacing: 8) {
                        Text("Perfil Completo! 🎉")
                            .font(.title2.bold())
                        Text("Seu questionário foi salvo com sucesso.")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                    }

                    // Score de completude
                    VStack(spacing: 6) {
                        Text("Perfil de compatibilidade")
                            .font(.caption)
                            .foregroundColor(.secondary)

                        Text("\(questionnaire.completionPercent)% completo")
                            .font(.system(size: 36, weight: .black))
                            .foregroundStyle(LinearGradient.fypGradient)

                        ProgressView(value: Double(questionnaire.completionPercent), total: 100)
                            .tint(.fypPink)
                            .frame(maxWidth: 200)
                    }
                    .padding(20)
                    .background(Color(.secondarySystemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 18))

                    Text("Quanto mais completo seu perfil, mais precisos serão os matches!")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 20)

                    GradientButton(title: "Ver meus matches", icon: "heart.fill") {
                        dismiss()
                    }
                    .padding(.horizontal, 20)

                    Spacer(minLength: 40)
                }
            }

            // Confetti simples
            if confettiVisible {
                ForEach(particulas) { p in
                    Circle()
                        .fill(p.cor)
                        .frame(width: p.tamanho, height: p.tamanho)
                        .position(p.posicaoFinal)
                        .opacity(p.opacidade)
                }
            }
        }
        .onAppear {
            gerarParticulas()
            withAnimation { confettiVisible = true }
        }
    }

    private func gerarParticulas() {
        let cores: [Color] = [.fypPink, .fypPurple, .yellow, .green, .orange, .blue]
        particulas = (0..<40).map { _ in
            ParticulaConfetti(
                cor: cores.randomElement() ?? .fypPink,
                tamanho: CGFloat.random(in: 6...14),
                posicaoFinal: CGPoint(
                    x: CGFloat.random(in: 30...350),
                    y: CGFloat.random(in: 60...700)
                ),
                opacidade: Double.random(in: 0.6...1.0)
            )
        }
    }
}

private struct ParticulaConfetti: Identifiable {
    let id = UUID()
    let cor: Color
    let tamanho: CGFloat
    let posicaoFinal: CGPoint
    let opacidade: Double
}

// MARK: - Preview

#if DEBUG
struct QuickModeView_Previews: PreviewProvider {
    static var previews: some View {
        QuickModeView(userId: "preview-user")
    }
}
#endif
