// NeuroProfileView.swift — FypMatch iOS
// Questionário de neurodiversidade em 5 etapas com NavigationStack + progresso linear

import SwiftUI
import FirebaseAuth

// MARK: - Etapas

private enum Etapa: Int, CaseIterable {
    case bemeVindo    = 0
    case identificacao = 1
    case comunicacao  = 2
    case acomodacoes  = 3
    case resumo       = 4

    var progresso: Double {
        switch self {
        case .bemeVindo:     return 0.2
        case .identificacao: return 0.4
        case .comunicacao:   return 0.6
        case .acomodacoes:   return 0.8
        case .resumo:        return 1.0
        }
    }
}

// MARK: - NeuroProfileView

struct NeuroProfileView: View {

    // MARK: — Init

    /// `userId` do usuário autenticado. Se nil, tenta ler do Auth.
    var userId: String?
    /// Chamado quando o usuário finaliza ou pula.
    var onDismiss: (() -> Void)?

    // MARK: — State local

    @State private var etapaAtual: Etapa = .bemeVindo
    @State private var profile = NeuroProfile()
    @State private var isSaving = false
    @State private var saveError: String?
    @State private var mostrarErro = false

    // MARK: — View

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                barraProgresso
                conteudoEtapa
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
            .navigationBarHidden(true)
            .background(Color(.systemBackground))
        }
        .alert("Erro ao salvar", isPresented: $mostrarErro, presenting: saveError) { _ in
            Button("OK", role: .cancel) {}
        } message: { erro in
            Text(erro)
        }
    }

    // MARK: — Barra de Progresso

    private var barraProgresso: some View {
        VStack(spacing: 0) {
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    Rectangle()
                        .fill(Color(.systemGray5))
                        .frame(height: 4)
                    Rectangle()
                        .fill(LinearGradient.fypGradient)
                        .frame(width: geo.size.width * etapaAtual.progresso, height: 4)
                        .animation(.easeInOut(duration: 0.35), value: etapaAtual)
                }
            }
            .frame(height: 4)
        }
        .padding(.top, safeAreaTop)
    }

    // MARK: — Conteúdo por Etapa

    @ViewBuilder
    private var conteudoEtapa: some View {
        switch etapaAtual {
        case .bemeVindo:     EtapaBemVindo(onComecar: avancar, onPular: onDismiss)
        case .identificacao: EtapaIdentificacao(selecionados: $profile.neurodiversityTypes, onProximo: avancar)
        case .comunicacao:   EtapaComunicacao(preferencias: $profile.preferences, onProximo: avancar)
        case .acomodacoes:   EtapaAcomodacoes(selecionadas: $profile.accommodations, onProximo: avancar)
        case .resumo:        EtapaResumo(
            profile: profile,
            isSaving: isSaving,
            onSalvar: salvar
        )
        }
    }

    // MARK: — Ações

    private func avancar() {
        let todos = Etapa.allCases
        guard let idx = todos.firstIndex(of: etapaAtual), idx + 1 < todos.count else { return }
        withAnimation(.easeInOut(duration: 0.3)) {
            etapaAtual = todos[idx + 1]
        }
    }

    private func salvar() {
        let uid = userId ?? Auth.auth().currentUser?.uid ?? ""
        guard !uid.isEmpty else {
            saveError = "Usuário não identificado."
            mostrarErro = true
            return
        }
        isSaving = true
        Task {
            do {
                try await NeuroService.liveValue.saveProfile(uid, profile)
                await MainActor.run {
                    isSaving = false
                    onDismiss?()
                }
            } catch {
                await MainActor.run {
                    isSaving = false
                    saveError = error.localizedDescription
                    mostrarErro = true
                }
            }
        }
    }

    // MARK: — Helper

    private var safeAreaTop: CGFloat {
        (UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first?.windows.first?.safeAreaInsets.top) ?? 0
    }
}

// MARK: - Etapa 1 — Bem-vindo

private struct EtapaBemVindo: View {
    let onComecar: () -> Void
    let onPular: (() -> Void)?

    var body: some View {
        ScrollView {
            VStack(spacing: 32) {
                Spacer(minLength: 40)

                // Ícone gradiente
                ZStack {
                    Circle()
                        .fill(LinearGradient.fypGradient)
                        .frame(width: 120, height: 120)
                        .shadow(color: .fypPink.opacity(0.35), radius: 20, y: 8)
                    Text("🧠")
                        .font(.system(size: 56))
                }

                // Título + descrição
                VStack(spacing: 14) {
                    GradientText(text: "Perfil de Neurodiversidade", font: .title.bold())
                        .multilineTextAlignment(.center)

                    Text("Este espaço é seu. Compartilhe o quanto quiser — todas as informações são privadas e usadas apenas para tornar sua experiência melhor.")
                        .font(.body)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .lineSpacing(4)
                        .padding(.horizontal, 8)
                }

                // Benefícios
                VStack(spacing: 12) {
                    BeneficioRow(icon: "lock.shield.fill", texto: "Totalmente privado")
                    BeneficioRow(icon: "heart.fill",       texto: "Matches mais compatíveis")
                    BeneficioRow(icon: "slider.horizontal.3", texto: "Experiência personalizada")
                }
                .padding(.horizontal, 4)

                Spacer(minLength: 24)

                VStack(spacing: 12) {
                    GradientButton(title: "Começar") { onComecar() }

                    if let pular = onPular {
                        Button("Pular por agora") { pular() }
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                }
            }
            .padding(.horizontal, 28)
            .padding(.bottom, 40)
        }
    }
}

private struct BeneficioRow: View {
    let icon: String
    let texto: String
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 16, weight: .semibold))
                .foregroundStyle(LinearGradient.fypGradient)
                .frame(width: 24)
            Text(texto)
                .font(.subheadline)
                .foregroundColor(.primary)
            Spacer()
        }
        .padding(14)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

// MARK: - Etapa 2 — Identificação

private struct EtapaIdentificacao: View {
    @Binding var selecionados: [NeurodiversityType]
    let onProximo: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                EtapaHeader(
                    titulo: "Como você se identifica?",
                    subtitulo: "Fique à vontade para selecionar mais de um. Nenhuma seleção também é válida."
                )

                LazyVGrid(
                    columns: [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())],
                    spacing: 12
                ) {
                    ForEach(NeurodiversityType.allCases, id: \.self) { tipo in
                        NeuroDiversityChip(
                            tipo: tipo,
                            selecionado: selecionados.contains(tipo)
                        ) {
                            toggleTipo(tipo)
                        }
                    }
                }

                Spacer(minLength: 24)
                GradientButton(title: "Próximo →") { onProximo() }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 40)
        }
    }

    private func toggleTipo(_ tipo: NeurodiversityType) {
        withAnimation(.spring(response: 0.25, dampingFraction: 0.7)) {
            if let idx = selecionados.firstIndex(of: tipo) {
                selecionados.remove(at: idx)
            } else {
                selecionados.append(tipo)
            }
        }
    }
}

private struct NeuroDiversityChip: View {
    let tipo: NeurodiversityType
    let selecionado: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 6) {
                Text(tipo.emoji)
                    .font(.system(size: 28))
                Text(tipo.displayName)
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(selecionado ? .white : .primary)
                    .multilineTextAlignment(.center)
                    .lineLimit(2)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .padding(.horizontal, 6)
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
            .shadow(color: selecionado ? Color.fypPink.opacity(0.3) : .clear, radius: 6, y: 3)
        }
        .buttonStyle(.plain)
        .accessibilityLabel("\(tipo.displayName), \(selecionado ? "selecionado" : "não selecionado")")
    }
}

// MARK: - Etapa 3 — Preferências de Comunicação

private struct EtapaComunicacao: View {
    @Binding var preferencias: NeuroPreferences
    let onProximo: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                EtapaHeader(
                    titulo: "Como você prefere se comunicar?",
                    subtitulo: "Essas preferências ajudam o FypMatch a apresentar pessoas mais compatíveis com você."
                )

                VStack(spacing: 0) {
                    PreferenciaToggleRow(
                        icon: "arrow.forward.circle.fill",
                        titulo: "Comunicação direta",
                        descricao: "Prefiro mensagens diretas e sem rodeios",
                        valor: $preferencias.prefersDirectness
                    )
                    Divider().padding(.leading, 56)

                    PreferenciaToggleRow(
                        icon: "bubble.left.and.bubble.right.fill",
                        titulo: "Comunicação clara",
                        descricao: "Preciso de mensagens objetivas e bem estruturadas",
                        valor: $preferencias.needsClearCommunication
                    )
                    Divider().padding(.leading, 56)

                    PreferenciaToggleRow(
                        icon: "textformat.size",
                        titulo: "Prefiro texto",
                        descricao: "Texto é mais confortável do que mensagens de voz",
                        valor: $preferencias.prefersTextOverVoice
                    )
                    Divider().padding(.leading, 56)

                    PreferenciaToggleRow(
                        icon: "heart.slash",
                        titulo: "Sensível a críticas",
                        descricao: "Críticas diretas me afetam com mais intensidade",
                        valor: $preferencias.sensitiveToCriticism
                    )
                    Divider().padding(.leading, 56)

                    PreferenciaToggleRow(
                        icon: "calendar.badge.clock",
                        titulo: "Preciso de rotina",
                        descricao: "Imprevistos me geram ansiedade — prefiro previsibilidade",
                        valor: $preferencias.needsRoutine
                    )
                    Divider().padding(.leading, 56)

                    PreferenciaToggleRow(
                        icon: "hourglass",
                        titulo: "Preciso de tempo para processar",
                        descricao: "Prefiro pensar antes de responder, sem pressão",
                        valor: $preferencias.needsProcessingTime
                    )
                    Divider().padding(.leading, 56)

                    PreferenciaToggleRow(
                        icon: "list.number",
                        titulo: "Conversa estruturada",
                        descricao: "Prefiro conversas com começo, meio e fim bem definidos",
                        valor: $preferencias.prefersStructuredConversation
                    )
                }
                .background(Color(.secondarySystemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 16))

                Spacer(minLength: 24)
                GradientButton(title: "Próximo →") { onProximo() }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 40)
        }
    }
}

private struct PreferenciaToggleRow: View {
    let icon: String
    let titulo: String
    let descricao: String
    @Binding var valor: Bool

    var body: some View {
        Toggle(isOn: $valor) {
            HStack(spacing: 14) {
                Image(systemName: icon)
                    .font(.system(size: 18, weight: .medium))
                    .foregroundStyle(LinearGradient.fypGradient)
                    .frame(width: 26)
                VStack(alignment: .leading, spacing: 2) {
                    Text(titulo)
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.primary)
                    Text(descricao)
                        .font(.system(size: 12))
                        .foregroundColor(.secondary)
                }
            }
        }
        .tint(.fypPink)
        .padding(.vertical, 12)
        .padding(.horizontal, 16)
    }
}

// MARK: - Etapa 4 — Acomodações de Interface

private struct EtapaAcomodacoes: View {
    @Binding var selecionadas: [AccommodationType]
    let onProximo: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                EtapaHeader(
                    titulo: "Como posso te ajudar?",
                    subtitulo: "Selecione as acomodações que tornam sua experiência mais confortável."
                )

                VStack(spacing: 12) {
                    ForEach(AccommodationType.allCases, id: \.self) { tipo in
                        AcomodacaoCard(
                            tipo: tipo,
                            selecionado: selecionadas.contains(tipo)
                        ) {
                            toggleAcomodacao(tipo)
                        }
                    }
                }

                Spacer(minLength: 24)
                GradientButton(title: "Próximo →") { onProximo() }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 40)
        }
    }

    private func toggleAcomodacao(_ tipo: AccommodationType) {
        withAnimation(.spring(response: 0.25, dampingFraction: 0.7)) {
            if let idx = selecionadas.firstIndex(of: tipo) {
                selecionadas.remove(at: idx)
            } else {
                selecionadas.append(tipo)
            }
        }
    }
}

private struct AcomodacaoCard: View {
    let tipo: AccommodationType
    let selecionado: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 16) {
                // Ícone
                ZStack {
                    RoundedRectangle(cornerRadius: 10)
                        .fill(
                            selecionado
                                ? AnyShapeStyle(LinearGradient.fypGradient)
                                : AnyShapeStyle(Color(.systemGray5))
                        )
                        .frame(width: 46, height: 46)
                    Image(systemName: tipo.icon)
                        .font(.system(size: 20, weight: .medium))
                        .foregroundColor(selecionado ? .white : .fypPink)
                }

                // Texto
                VStack(alignment: .leading, spacing: 3) {
                    Text(tipo.displayName)
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.primary)
                    Text(tipo.description)
                        .font(.system(size: 12))
                        .foregroundColor(.secondary)
                        .lineLimit(2)
                }

                Spacer()

                // Checkmark
                Image(systemName: selecionado ? "checkmark.circle.fill" : "circle")
                    .font(.system(size: 22))
                    .foregroundStyle(
                        selecionado
                            ? AnyShapeStyle(LinearGradient.fypGradient)
                            : AnyShapeStyle(Color(.systemGray3))
                    )
            }
            .padding(16)
            .background(Color(.secondarySystemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(
                        selecionado ? Color.fypPink.opacity(0.5) : Color.clear,
                        lineWidth: 1.5
                    )
            )
            .shadow(color: selecionado ? Color.fypPink.opacity(0.15) : .clear, radius: 6, y: 3)
        }
        .buttonStyle(.plain)
        .accessibilityLabel("\(tipo.displayName), \(selecionado ? "selecionado" : "não selecionado")")
    }
}

// MARK: - Etapa 5 — Resumo e Salvar

private struct EtapaResumo: View {
    let profile: NeuroProfile
    let isSaving: Bool
    let onSalvar: () -> Void

    private var totalPreferencias: Int {
        let p = profile.preferences
        return [
            p.needsClearCommunication, p.prefersDirectness, p.sensitiveToCriticism,
            p.needsRoutine, p.prefersTextOverVoice, p.needsProcessingTime,
            p.prefersStructuredConversation
        ].filter { $0 }.count
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 32) {
                Spacer(minLength: 32)

                // Ícone de sucesso
                ZStack {
                    Circle()
                        .fill(LinearGradient.fypGradient)
                        .frame(width: 100, height: 100)
                        .shadow(color: .fypPink.opacity(0.35), radius: 16, y: 6)
                    Text("✅")
                        .font(.system(size: 44))
                }

                // Título
                VStack(spacing: 8) {
                    GradientText(text: "Perfil configurado!", font: .title2.bold())
                    Text("\(totalPreferencias) preferência\(totalPreferencias == 1 ? "" : "s") selecionada\(totalPreferencias == 1 ? "" : "s") • \(profile.accommodations.count) acomodaç\(profile.accommodations.count == 1 ? "ão" : "ões")")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }

                // Resumo — neurodiversidade
                if !profile.neurodiversityTypes.isEmpty {
                    ResumoCard(titulo: "Você se identifica como") {
                        FlowLayout(spacing: 8) {
                            ForEach(profile.neurodiversityTypes, id: \.self) { tipo in
                                HStack(spacing: 4) {
                                    Text(tipo.emoji).font(.system(size: 13))
                                    Text(tipo.displayName).font(.system(size: 12, weight: .medium))
                                }
                                .padding(.horizontal, 10).padding(.vertical, 5)
                                .background(Color(.systemGray6))
                                .clipShape(Capsule())
                            }
                        }
                    }
                }

                // Resumo — acomodações
                if !profile.accommodations.isEmpty {
                    ResumoCard(titulo: "Acomodações ativas") {
                        VStack(alignment: .leading, spacing: 8) {
                            ForEach(profile.accommodations, id: \.self) { tipo in
                                HStack(spacing: 10) {
                                    Image(systemName: tipo.icon)
                                        .font(.system(size: 13))
                                        .foregroundStyle(LinearGradient.fypGradient)
                                    Text(tipo.displayName)
                                        .font(.system(size: 13))
                                        .foregroundColor(.primary)
                                }
                            }
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }
                }

                Spacer(minLength: 8)

                GradientButton(title: "Salvar e Continuar", icon: "checkmark", isLoading: isSaving) {
                    onSalvar()
                }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 40)
        }
    }
}

private struct ResumoCard<Content: View>: View {
    let titulo: String
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(titulo)
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(.secondary)
                .textCase(.uppercase)
                .tracking(0.5)
            content
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}

// MARK: - Componentes Compartilhados

private struct EtapaHeader: View {
    let titulo: String
    var subtitulo: String? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            GradientText(text: titulo, font: .title2.bold())
                .fixedSize(horizontal: false, vertical: true)
            if let sub = subtitulo {
                Text(sub)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .lineSpacing(3)
            }
        }
        .padding(.top, 24)
    }
}

// MARK: - FlowLayout (chips quebra-linha)

private struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let maxWidth = proposal.width ?? .infinity
        var height: CGFloat = 0
        var rowWidth: CGFloat = 0
        var rowHeight: CGFloat = 0

        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if rowWidth + size.width > maxWidth, rowWidth > 0 {
                height += rowHeight + spacing
                rowWidth = 0
                rowHeight = 0
            }
            rowWidth += size.width + spacing
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
            if x + size.width > bounds.maxX, x > bounds.minX {
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

// MARK: - Preview

#Preview {
    NeuroProfileView(userId: "preview-user") {
        print("Dispensado")
    }
}
