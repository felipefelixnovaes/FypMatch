// WaitlistFormView.swift — FypMatch iOS
// Tela de inscrição na lista de espera — precede WaitlistView (confirmação)

import SwiftUI
import FirebaseFirestore

// MARK: - WaitlistFormView

struct WaitlistFormView: View {

    // MARK: Campos do formulário
    @State private var nomeCompleto: String = ""
    @State private var email: String = ""
    @State private var cidade: String = ""
    @State private var estadoSelecionado: String = "SP"
    @State private var idade: Double = 25
    @State private var generoSelecionado: String = "Homem"
    @State private var intencoesSelecionadas: Set<String> = []
    @State private var codigoConvite: String = ""

    // MARK: UI State
    @State private var isLoading: Bool = false
    @State private var erros: [String: String] = [:]
    @State private var errorGeral: String? = nil
    @State private var navegarParaConfirmacao: Bool = false
    @State private var posicaoNaFila: Int = 0

    // MARK: Ambiente
    @Environment(\.dismiss) private var dismiss

    // MARK: - Constantes

    private let generos = ["Homem", "Mulher", "Outro"]

    private let intencoes = [
        "Relacionamento sério",
        "Encontros casuais",
        "Amizade"
    ]

    private let estados = [
        "AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES",
        "GO", "MA", "MG", "MS", "MT", "PA", "PB", "PE",
        "PI", "PR", "RJ", "RN", "RO", "RR", "RS", "SC",
        "SE", "SP", "TO"
    ]

    // MARK: - Body

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {

                    // Header
                    headerSection

                    // Campos obrigatórios
                    VStack(spacing: 16) {
                        campoNome
                        campoEmail
                        HStack(spacing: 12) {
                            campoCidade
                            campoEstado
                        }
                        campoIdade
                    }
                    .padding(.horizontal, 20)

                    Divider().padding(.horizontal, 20)

                    // Preferências
                    VStack(spacing: 16) {
                        campoGenero
                        campoIntencoes
                    }
                    .padding(.horizontal, 20)

                    Divider().padding(.horizontal, 20)

                    // Código de convite (opcional)
                    campoCodigoConvite
                        .padding(.horizontal, 20)

                    // Erro geral
                    if let err = errorGeral {
                        Text(err)
                            .font(.caption)
                            .foregroundColor(.red)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 20)
                    }

                    // Botão principal
                    botaoSubmit
                        .padding(.horizontal, 20)
                        .padding(.bottom, 40)
                }
                .padding(.top, 8)
            }
            .navigationTitle("Entrar na lista")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Fechar") { dismiss() }
                        .foregroundColor(.fypPink)
                }
            }
        }
        // Navega para WaitlistView após sucesso
        .fullScreenCover(isPresented: $navegarParaConfirmacao) {
            WaitlistView(email: email, position: posicaoNaFila)
        }
    }

    // MARK: - Subviews

    private var headerSection: some View {
        VStack(spacing: 8) {
            Text("🎉")
                .font(.system(size: 48))
            Text("Junte-se ao FypMatch")
                .font(.title2.bold())
                .foregroundColor(.primary)
            Text("Seja um dos primeiros a experimentar conexões reais com IA inclusiva.")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 16)
        }
        .padding(.top, 16)
        .padding(.horizontal, 20)
    }

    private var campoNome: some View {
        FypTextField(
            placeholder: "Nome completo",
            text: $nomeCompleto,
            error: erros["nome"],
            icon: "person.fill"
        )
    }

    private var campoEmail: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 10) {
                Image(systemName: "envelope.fill")
                    .foregroundColor(.fypPink)
                    .frame(width: 18)
                TextField("Email", text: $email)
                    .keyboardType(.emailAddress)
                    .autocorrectionDisabled()
                    .textInputAutocapitalization(.never)
                    .font(.system(size: 15))
            }
            .padding(14)
            .background(Color(.secondarySystemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(erros["email"] != nil ? Color.red : Color.clear, lineWidth: 1.5)
            )

            if let err = erros["email"] {
                Text(err).font(.caption).foregroundColor(.red).padding(.leading, 4)
            }
        }
    }

    private var campoCidade: some View {
        FypTextField(
            placeholder: "Cidade",
            text: $cidade,
            error: erros["cidade"],
            icon: "mappin.circle.fill"
        )
    }

    private var campoEstado: some View {
        VStack(alignment: .leading, spacing: 4) {
            Menu {
                ForEach(estados, id: \.self) { uf in
                    Button(uf) { estadoSelecionado = uf }
                }
            } label: {
                HStack {
                    Text(estadoSelecionado)
                        .font(.system(size: 15))
                        .foregroundColor(.primary)
                    Spacer()
                    Image(systemName: "chevron.up.chevron.down")
                        .font(.system(size: 12))
                        .foregroundColor(.fypPink)
                }
                .padding(14)
                .background(Color(.secondarySystemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }
        }
        .frame(width: 80)
    }

    private var campoIdade: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Image(systemName: "calendar")
                    .foregroundColor(.fypPink)
                Text("\(Int(idade)) anos")
                    .font(.system(size: 15))
                Spacer()
            }

            Slider(value: $idade, in: 18...60, step: 1)
                .tint(.fypPink)
        }
        .padding(14)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private var campoGenero: some View {
        VStack(alignment: .leading, spacing: 8) {
            Label("Gênero", systemImage: "person.2.fill")
                .font(.subheadline.bold())
                .foregroundColor(.secondary)

            Picker("Gênero", selection: $generoSelecionado) {
                ForEach(generos, id: \.self) { g in
                    Text(g).tag(g)
                }
            }
            .pickerStyle(.segmented)
        }
    }

    private var campoIntencoes: some View {
        VStack(alignment: .leading, spacing: 10) {
            Label("O que você busca?", systemImage: "heart.text.square.fill")
                .font(.subheadline.bold())
                .foregroundColor(.secondary)

            if let err = erros["intencoes"] {
                Text(err).font(.caption).foregroundColor(.red)
            }

            // Pills multi-select
            FlowLayout(spacing: 8) {
                ForEach(intencoes, id: \.self) { intencao in
                    let selecionada = intencoesSelecionadas.contains(intencao)
                    Button {
                        if selecionada {
                            intencoesSelecionadas.remove(intencao)
                        } else {
                            intencoesSelecionadas.insert(intencao)
                        }
                    } label: {
                        Text(intencao)
                            .font(.subheadline)
                            .padding(.horizontal, 14)
                            .padding(.vertical, 8)
                            .background(selecionada
                                ? AnyShapeStyle(LinearGradient.fypGradient)
                                : AnyShapeStyle(Color(.secondarySystemBackground))
                            )
                            .foregroundColor(selecionada ? .white : .primary)
                            .clipShape(Capsule())
                            .overlay(
                                Capsule()
                                    .stroke(selecionada ? Color.clear : Color.fypPink.opacity(0.4), lineWidth: 1)
                            )
                    }
                }
            }
        }
    }

    private var campoCodigoConvite: some View {
        VStack(alignment: .leading, spacing: 4) {
            Label("Código de convite", systemImage: "star.fill")
                .font(.subheadline.bold())
                .foregroundColor(.secondary)

            TextField("Tem um código? Digite aqui", text: $codigoConvite)
                .font(.system(size: 15, design: .monospaced))
                .autocorrectionDisabled()
                .textInputAutocapitalization(.characters)
                .onChange(of: codigoConvite) { _, newValue in
                    codigoConvite = newValue.uppercased()
                }
                .padding(14)
                .background(Color(.secondarySystemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    private var botaoSubmit: some View {
        GradientButton(
            title: "Quero entrar na lista!",
            icon: "arrow.right.circle.fill",
            isLoading: isLoading
        ) {
            Task { await submeter() }
        }
    }

    // MARK: - Lógica de Submissão

    private func submeter() async {
        guard validarCampos() else { return }

        isLoading = true
        errorGeral = nil

        do {
            let db = Firestore.firestore()

            // Dados para salvar na collection "waitlist"
            var dados: [String: Any] = [
                "nomeCompleto": nomeCompleto.trimmingCharacters(in: .whitespaces),
                "email": email.lowercased().trimmingCharacters(in: .whitespaces),
                "cidade": cidade.trimmingCharacters(in: .whitespaces),
                "estado": estadoSelecionado,
                "idade": Int(idade),
                "genero": generoSelecionado,
                "intencoes": Array(intencoesSelecionadas),
                "criadoEm": Timestamp(date: Date())
            ]

            if !codigoConvite.trimmingCharacters(in: .whitespaces).isEmpty {
                dados["codigoConvite"] = codigoConvite.uppercased()
            }

            try await db.collection("waitlist").addDocument(data: dados)

            // Busca posição na fila (count da collection)
            let snapshot = try await db.collection("waitlist").getDocuments()
            posicaoNaFila = snapshot.documents.count

            isLoading = false
            navegarParaConfirmacao = true

        } catch {
            isLoading = false
            errorGeral = "Erro ao entrar na lista: \(error.localizedDescription)"
        }
    }

    /// Valida campos obrigatórios e retorna `true` se tudo estiver correto
    private func validarCampos() -> Bool {
        erros = [:]

        let nome = nomeCompleto.trimmingCharacters(in: .whitespaces)
        if nome.isEmpty {
            erros["nome"] = "Nome é obrigatório"
        } else if nome.count < 2 {
            erros["nome"] = "Nome deve ter pelo menos 2 caracteres"
        }

        let emailTrimmed = email.trimmingCharacters(in: .whitespaces)
        if emailTrimmed.isEmpty {
            erros["email"] = "Email é obrigatório"
        } else if !emailTrimmed.contains("@") || !emailTrimmed.contains(".") {
            erros["email"] = "Email inválido"
        }

        let cidadeTrimmed = cidade.trimmingCharacters(in: .whitespaces)
        if cidadeTrimmed.isEmpty {
            erros["cidade"] = "Cidade é obrigatória"
        }

        if intencoesSelecionadas.isEmpty {
            erros["intencoes"] = "Selecione pelo menos uma intenção"
        }

        return erros.isEmpty
    }
}

// MARK: - FlowLayout (Pills wrap)

/// Layout de fluxo para distribuir pills em múltiplas linhas
struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let containerWidth = proposal.width ?? 0
        var currentX: CGFloat = 0
        var currentY: CGFloat = 0
        var rowHeight: CGFloat = 0
        var totalHeight: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if currentX + size.width > containerWidth && currentX > 0 {
                currentX = 0
                currentY += rowHeight + spacing
                rowHeight = 0
            }
            currentX += size.width + spacing
            rowHeight = max(rowHeight, size.height)
            totalHeight = currentY + rowHeight
        }

        return CGSize(width: containerWidth, height: totalHeight)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var currentX = bounds.minX
        var currentY = bounds.minY
        var rowHeight: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if currentX + size.width > bounds.maxX && currentX > bounds.minX {
                currentX = bounds.minX
                currentY += rowHeight + spacing
                rowHeight = 0
            }
            subview.place(at: CGPoint(x: currentX, y: currentY), proposal: ProposedViewSize(size))
            currentX += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
    }
}

// MARK: - Preview

#Preview {
    WaitlistFormView()
}
