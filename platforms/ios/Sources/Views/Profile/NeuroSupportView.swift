//
//  NeuroSupportView.swift
//  FypMatch iOS
//
//  Hub de suporte para neurodivergência — ferramentas de acessibilidade
//  e bem-estar para facilitar a experiência no app.
//

import SwiftUI

// MARK: - NeuroSupportView

struct NeuroSupportView: View {

    // MARK: - State

    @State private var mensagemAnalise: String = ""
    @State private var resultadoAnalise: AnaliseResultado? = nil
    @State private var analisandoMensagem = false
    @State private var acordionAnaliseAberto = false

    @State private var sinalSocialSelecionado: SinalSocial? = nil

    @State private var suporteExpandido: SuporteEmocional? = nil

    @State private var reduzirAnimacoes = false
    @State private var modoFoco = false

    @Environment(\.dismiss) private var dismiss

    // MARK: - Body

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    headerView
                    secaoAssistenteConversa
                    secaoDecodificadorPistas
                    secaoSuporteEmocional
                    secaoConfiguracoes
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 32)
            }
            .background(Color(.systemGroupedBackground))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Fechar") { dismiss() }
                        .foregroundStyle(Color(hex: "#E91E63"))
                }
            }
        }
        .sheet(item: $sinalSocialSelecionado) { sinal in
            SinalSocialDetailSheet(sinal: sinal)
        }
    }

    // MARK: - Header

    private var headerView: some View {
        ZStack {
            LinearGradient(
                colors: [Color(hex: "#E91E63"), Color(hex: "#9C27B0")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .clipShape(RoundedRectangle(cornerRadius: 20))

            VStack(spacing: 8) {
                Text("🧠")
                    .font(.system(size: 48))
                Text("Suporte Neuro")
                    .font(.title2.bold())
                    .foregroundStyle(.white)
                Text("Ferramentas para facilitar sua experiência")
                    .font(.subheadline)
                    .foregroundStyle(.white.opacity(0.88))
                    .multilineTextAlignment(.center)
            }
            .padding(.vertical, 28)
            .padding(.horizontal, 20)
        }
        .frame(maxWidth: .infinity)
    }

    // MARK: - Seção 1: Assistente de Conversa

    private var secaoAssistenteConversa: some View {
        cardContainer {
            VStack(alignment: .leading, spacing: 14) {
                Label("Sugestões de mensagem", systemImage: "bubble.left.and.bubble.right.fill")
                    .font(.headline)
                    .foregroundStyle(Color(hex: "#E91E63"))

                Text("Cole ou escreva uma mensagem para receber uma análise de tom e sugestões de melhoria.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                TextField("Digite sua mensagem para analisar...", text: $mensagemAnalise, axis: .vertical)
                    .lineLimit(3...6)
                    .padding(12)
                    .background(Color(.systemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                    .overlay(
                        RoundedRectangle(cornerRadius: 10)
                            .strokeBorder(Color(.separator), lineWidth: 1)
                    )

                Button(action: analisarMensagem) {
                    HStack {
                        if analisandoMensagem {
                            ProgressView()
                                .tint(.white)
                                .scaleEffect(0.8)
                        }
                        Text("Analisar")
                            .fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(mensagemAnalise.isEmpty
                        ? Color(.systemFill)
                        : LinearGradient(colors: [Color(hex: "#E91E63"), Color(hex: "#9C27B0")],
                                         startPoint: .leading, endPoint: .trailing))
                    .foregroundStyle(mensagemAnalise.isEmpty ? Color(.secondaryLabel) : .white)
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                }
                .disabled(mensagemAnalise.trimmingCharacters(in: .whitespaces).isEmpty || analisandoMensagem)
                .animation(.easeInOut(duration: 0.2), value: mensagemAnalise.isEmpty)

                if let resultado = resultadoAnalise {
                    accordionResultado(resultado)
                }
            }
        }
    }

    private func analisarMensagem() {
        guard !mensagemAnalise.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        analisandoMensagem = true
        resultadoAnalise = nil
        acordionAnaliseAberto = false

        // Simulação de análise local (sem chamada de rede)
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) {
            resultadoAnalise = AnaliseResultado.analisar(mensagem: mensagemAnalise)
            analisandoMensagem = false
            withAnimation { acordionAnaliseAberto = true }
        }
    }

    @ViewBuilder
    private func accordionResultado(_ resultado: AnaliseResultado) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            Button(action: { withAnimation(.spring(duration: 0.3)) { acordionAnaliseAberto.toggle() } }) {
                HStack {
                    Label("Ver análise", systemImage: "chart.bar.doc.horizontal")
                        .font(.subheadline.weight(.medium))
                        .foregroundStyle(Color(hex: "#9C27B0"))
                    Spacer()
                    Image(systemName: acordionAnaliseAberto ? "chevron.up" : "chevron.down")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                .padding(.vertical, 10)
            }

            if acordionAnaliseAberto {
                Divider()
                VStack(alignment: .leading, spacing: 10) {
                    infoRow(label: "Tom detectado", valor: resultado.tom, icone: "waveform")
                    infoRow(label: "Clareza", valor: resultado.clareza, icone: "checkmark.seal")
                    VStack(alignment: .leading, spacing: 6) {
                        Label("Sugestões de melhoria", systemImage: "lightbulb")
                            .font(.caption.weight(.medium))
                            .foregroundStyle(.secondary)
                        ForEach(resultado.sugestoes, id: \.self) { sugestao in
                            HStack(alignment: .top, spacing: 6) {
                                Text("•").foregroundStyle(Color(hex: "#E91E63"))
                                Text(sugestao).font(.caption).foregroundStyle(.primary)
                            }
                        }
                    }
                }
                .padding(.top, 10)
                .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(.horizontal, 12)
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 10))
        .overlay(RoundedRectangle(cornerRadius: 10).strokeBorder(Color(.separator), lineWidth: 1))
    }

    private func infoRow(label: String, valor: String, icone: String) -> some View {
        HStack(spacing: 8) {
            Image(systemName: icone)
                .font(.caption)
                .foregroundStyle(Color(hex: "#9C27B0"))
                .frame(width: 16)
            Text(label + ": ").font(.caption.weight(.medium)).foregroundStyle(.secondary)
            + Text(valor).font(.caption).foregroundStyle(.primary)
        }
    }

    // MARK: - Seção 2: Decodificador de Pistas Sociais

    private var secaoDecodificadorPistas: some View {
        cardContainer {
            VStack(alignment: .leading, spacing: 14) {
                Label("Entender sinais sociais", systemImage: "magnifyingglass.circle.fill")
                    .font(.headline)
                    .foregroundStyle(Color(hex: "#E91E63"))

                Text("Toque em um sinal para ver o significado, exemplos e como responder.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                ForEach(SinalSocial.todosSinais) { sinal in
                    Button(action: { sinalSocialSelecionado = sinal }) {
                        HStack(spacing: 12) {
                            Text(sinal.emoji)
                                .font(.title2)
                                .frame(width: 36, height: 36)
                                .background(Color(hex: "#9C27B0").opacity(0.12))
                                .clipShape(Circle())
                            VStack(alignment: .leading, spacing: 2) {
                                Text(sinal.titulo)
                                    .font(.subheadline.weight(.medium))
                                    .foregroundStyle(.primary)
                                Text(sinal.descricaoCurta)
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                    .lineLimit(1)
                            }
                            Spacer()
                            Image(systemName: "chevron.right")
                                .font(.caption)
                                .foregroundStyle(.tertiary)
                        }
                        .padding(.vertical, 8)
                    }
                    .buttonStyle(.plain)

                    if sinal.id != SinalSocial.todosSinais.last?.id {
                        Divider()
                    }
                }
            }
        }
    }

    // MARK: - Seção 3: Suporte Emocional

    private var secaoSuporteEmocional: some View {
        cardContainer {
            VStack(alignment: .leading, spacing: 14) {
                Label("Quando a ansiedade aparecer", systemImage: "heart.circle.fill")
                    .font(.headline)
                    .foregroundStyle(Color(hex: "#E91E63"))

                ForEach(SuporteEmocional.allCases) { item in
                    suporteCard(item)
                }
            }
        }
    }

    @ViewBuilder
    private func suporteCard(_ item: SuporteEmocional) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            Button(action: {
                withAnimation(.spring(duration: 0.35)) {
                    suporteExpandido = suporteExpandido == item ? nil : item
                }
            }) {
                HStack(spacing: 12) {
                    Text(item.emoji)
                        .font(.title3)
                        .frame(width: 40, height: 40)
                        .background(LinearGradient(
                            colors: [Color(hex: "#E91E63").opacity(0.15), Color(hex: "#9C27B0").opacity(0.15)],
                            startPoint: .topLeading, endPoint: .bottomTrailing))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                    VStack(alignment: .leading, spacing: 2) {
                        Text(item.titulo)
                            .font(.subheadline.weight(.semibold))
                            .foregroundStyle(.primary)
                        Text(item.descricaoCurta)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    Spacer()
                    Image(systemName: suporteExpandido == item ? "chevron.up" : "chevron.down")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                .padding(.vertical, 10)
            }
            .buttonStyle(.plain)

            if suporteExpandido == item {
                Divider()
                Text(item.conteudo)
                    .font(.subheadline)
                    .foregroundStyle(.primary)
                    .padding(.vertical, 12)
                    .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(.horizontal, 12)
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(Color(.separator), lineWidth: 1))
    }

    // MARK: - Seção 4: Configurações Rápidas

    private var secaoConfiguracoes: some View {
        cardContainer {
            VStack(alignment: .leading, spacing: 14) {
                Label("Configurações de acessibilidade", systemImage: "gearshape.fill")
                    .font(.headline)
                    .foregroundStyle(Color(hex: "#E91E63"))

                Toggle(isOn: $reduzirAnimacoes) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Reduzir animações")
                            .font(.subheadline.weight(.medium))
                        Text("Deixa transições mais suaves e rápidas")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
                .tint(Color(hex: "#E91E63"))
                .onChange(of: reduzirAnimacoes) { _, novoValor in
                    aplicarReduzirAnimacoes(novoValor)
                }

                Divider()

                Toggle(isOn: $modoFoco) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Modo foco")
                            .font(.subheadline.weight(.medium))
                        Text("Oculta elementos visuais não essenciais")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
                .tint(Color(hex: "#9C27B0"))

                Divider()

                NavigationLink(destination: NeuroProfileView()) {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Editar perfil neuro completo")
                                .font(.subheadline.weight(.medium))
                                .foregroundStyle(Color(hex: "#9C27B0"))
                            Text("Preferências de comunicação, sensibilidades e mais")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
            }
        }
    }

    private func aplicarReduzirAnimacoes(_ reduzir: Bool) {
        // Ajusta a velocidade das animações CALayer na janela ativa
        let velocidade: Float = reduzir ? 0.5 : 1.0
        for scene in UIApplication.shared.connectedScenes {
            guard let windowScene = scene as? UIWindowScene else { continue }
            for window in windowScene.windows {
                window.layer.speed = velocidade
            }
        }
    }

    // MARK: - Helpers

    @ViewBuilder
    private func cardContainer<Content: View>(@ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            content()
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

// MARK: - SinalSocialDetailSheet

struct SinalSocialDetailSheet: View {
    let sinal: SinalSocial
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    HStack(spacing: 16) {
                        Text(sinal.emoji)
                            .font(.system(size: 52))
                        VStack(alignment: .leading, spacing: 4) {
                            Text(sinal.titulo)
                                .font(.title3.bold())
                            Text(sinal.descricaoCurta)
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                    }
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(
                        LinearGradient(colors: [Color(hex: "#E91E63").opacity(0.1), Color(hex: "#9C27B0").opacity(0.1)],
                                       startPoint: .topLeading, endPoint: .bottomTrailing)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                    secaoDetalhe(titulo: "O que significa", conteudo: sinal.significado)
                    secaoDetalhe(titulo: "Exemplos comuns", conteudo: sinal.exemplos)
                    secaoDetalhe(titulo: "Como responder", conteudo: sinal.comoResponder)
                }
                .padding(16)
            }
            .background(Color(.systemGroupedBackground))
            .navigationTitle("Sinal Social")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Fechar") { dismiss() }
                        .foregroundStyle(Color(hex: "#E91E63"))
                }
            }
        }
    }

    private func secaoDetalhe(titulo: String, conteudo: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(titulo)
                .font(.headline)
                .foregroundStyle(Color(hex: "#9C27B0"))
            Text(conteudo)
                .font(.body)
                .foregroundStyle(.primary)
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

// MARK: - NeuroProfileView (placeholder)

struct NeuroProfileView: View {
    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                Text("🧩")
                    .font(.system(size: 60))
                    .padding(.top, 40)
                Text("Perfil Neuro Completo")
                    .font(.title2.bold())
                Text("Em breve você poderá configurar suas preferências de comunicação, sensibilidades sensoriais e muito mais.")
                    .font(.body)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
            }
        }
        .navigationTitle("Perfil Neuro")
        .navigationBarTitleDisplayMode(.inline)
    }
}

// MARK: - Modelos de Dados

struct AnaliseResultado {
    let tom: String
    let clareza: String
    let sugestoes: [String]

    static func analisar(mensagem: String) -> AnaliseResultado {
        let texto = mensagem.lowercased()

        let tom: String
        if texto.contains("?") && texto.count < 40 {
            tom = "Interrogativo / Direto"
        } else if texto.contains("!") {
            tom = "Entusiasmado / Expressivo"
        } else if ["oi", "olá", "ola", "hey"].contains(where: { texto.hasPrefix($0) }) {
            tom = "Informal / Amigável"
        } else if texto.count < 20 {
            tom = "Conciso / Neutro"
        } else {
            tom = "Conversacional"
        }

        let clareza: String
        if mensagem.count < 10 {
            clareza = "⚠️ Muito curta — pode ser ambígua"
        } else if mensagem.count > 200 {
            clareza = "⚠️ Muito longa — considere dividir"
        } else {
            clareza = "✅ Adequada"
        }

        var sugestoes: [String] = []
        if !texto.contains("por favor") && !texto.contains("obrigad") {
            sugestoes.append("Adicionar \"por favor\" pode tornar a mensagem mais gentil.")
        }
        if texto.count < 15 {
            sugestoes.append("Mensagens curtas podem ser mal interpretadas. Considere adicionar contexto.")
        }
        if !mensagem.hasSuffix("?") && !mensagem.hasSuffix(".") && !mensagem.hasSuffix("!") {
            sugestoes.append("Termine com pontuação para indicar o tom da mensagem.")
        }
        if sugestoes.isEmpty {
            sugestoes.append("Mensagem clara e com bom tom. Nenhuma sugestão adicional.")
        }

        return AnaliseResultado(tom: tom, clareza: clareza, sugestoes: sugestoes)
    }
}

struct SinalSocial: Identifiable {
    let id = UUID()
    let emoji: String
    let titulo: String
    let descricaoCurta: String
    let significado: String
    let exemplos: String
    let comoResponder: String

    static let todosSinais: [SinalSocial] = [
        SinalSocial(
            emoji: "😂",
            titulo: "Emojis em excesso",
            descricaoCurta: "Muitos emojis em sequência",
            significado: "Geralmente indica descontração, humor ou que a pessoa está muito animada. Pode também ser uma forma de suavizar uma mensagem direta.",
            exemplos: "\"Que saudade!! 😂😂😂🥰🔥\"\n\"Adorei demais isso 🎉🎊✨\"",
            comoResponder: "Responda no mesmo clima leve. Você não precisa usar tantos emojis — um ou dois já mostram que entendeu o tom animado."
        ),
        SinalSocial(
            emoji: "💬",
            titulo: "Padrão de resposta",
            descricaoCurta: "Sempre responde rápido (ou sempre demora)",
            significado: "Respostas rápidas geralmente indicam interesse e disponibilidade. Respostas demoradas podem significar que a pessoa está ocupada — não necessariamente desinteressada.",
            exemplos: "Sempre responde em menos de 1 minuto.\nDemora horas mas manda mensagens longas.",
            comoResponder: "Não tire conclusões apenas pela velocidade. Observe o conteúdo das respostas e se a conversa flui naturalmente."
        ),
        SinalSocial(
            emoji: "🤫",
            titulo: "Silêncio prolongado",
            descricaoCurta: "Ficou sem responder por horas ou dias",
            significado: "Pode indicar vida ocupada, sobrecarga emocional, ou que a pessoa está pensando em como responder. Raramente significa rejeição sem outros sinais.",
            exemplos: "Leu e não respondeu por 12 horas.\nDemorou 2 dias mas retomou a conversa.",
            comoResponder: "Espere um tempo razoável. Se precisar, mande uma mensagem leve sem cobrança: \"Tudo bem? Fico à disposição quando puder responder.\""
        ),
        SinalSocial(
            emoji: "📝",
            titulo: "Mensagens curtas",
            descricaoCurta: "Respostas de uma ou duas palavras",
            significado: "Pode significar que a pessoa está ocupada, que prefere conversar por voz/vídeo, ou que ainda não está 100% à vontade. Em alguns casos pode indicar desinteresse — observe o padrão ao longo do tempo.",
            exemplos: "\"Boa!\"\n\"Haha\"\n\"Verdade.\"",
            comoResponder: "Tente fazer perguntas abertas para incentivar respostas mais longas. Dê espaço e não force a conversa."
        ),
    ]
}

enum SuporteEmocional: String, CaseIterable, Identifiable {
    case tecnica54321 = "tecnica_54321"
    case respiracao = "respiracao"
    case afirmacoes = "afirmacoes"

    var id: String { rawValue }

    var emoji: String {
        switch self {
        case .tecnica54321: return "🖐️"
        case .respiracao: return "🌬️"
        case .afirmacoes: return "💛"
        }
    }

    var titulo: String {
        switch self {
        case .tecnica54321: return "Técnica 5-4-3-2-1"
        case .respiracao: return "Respiração guiada"
        case .afirmacoes: return "Afirmações positivas"
        }
    }

    var descricaoCurta: String {
        switch self {
        case .tecnica54321: return "Ancoragem pelos sentidos"
        case .respiracao: return "Acalme corpo e mente"
        case .afirmacoes: return "Palavras que acolhem"
        }
    }

    var conteudo: String {
        switch self {
        case .tecnica54321:
            return """
            Ancore-se no presente observando ao redor:

            👀 5 coisas que você pode VER agora.
            🤚 4 coisas que você pode TOCAR.
            👂 3 coisas que você pode OUVIR.
            👃 2 coisas que você pode CHEIRAR.
            👅 1 coisa que você pode PROVAR.

            Respire fundo entre cada etapa. Você está seguro/a.
            """
        case .respiracao:
            return """
            Respiração 4-7-8:

            1. Esvazie os pulmões completamente.
            2. Inspire pelo nariz contando até 4.
            3. Segure o ar contando até 7.
            4. Expire pela boca contando até 8.

            Repita 3 a 4 vezes. Essa técnica ativa o sistema nervoso parassimpático e reduz a ansiedade rapidamente.
            """
        case .afirmacoes:
            return """
            Leia em voz alta ou mentalmente:

            💛 "Eu mereço conexões genuínas."
            💛 "Meu jeito de ser é válido e valioso."
            💛 "Eu aprendo e melhoro a cada conversa."
            💛 "Não preciso ser perfeito/a para ser amado/a."
            💛 "Estou no meu ritmo e tudo bem."

            Repita a que mais ressoar com você agora.
            """
        }
    }
}

// MARK: - Color Extension (hex)

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r, g, b, a: Double
        switch hex.count {
        case 6:
            (r, g, b, a) = (Double((int >> 16) & 0xFF) / 255,
                            Double((int >> 8) & 0xFF) / 255,
                            Double(int & 0xFF) / 255,
                            1.0)
        case 8:
            (r, g, b, a) = (Double((int >> 24) & 0xFF) / 255,
                            Double((int >> 16) & 0xFF) / 255,
                            Double((int >> 8) & 0xFF) / 255,
                            Double(int & 0xFF) / 255)
        default:
            (r, g, b, a) = (1, 1, 1, 1)
        }
        self.init(red: r, green: g, blue: b, opacity: a)
    }
}

// MARK: - Preview

#Preview {
    NeuroSupportView()
}
