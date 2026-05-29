// AffiliateView.swift — FypMatch iOS
// Tela do Programa de Afiliados — cadastro, dashboard e saque

import SwiftUI

struct AffiliateView: View {
    @StateObject private var viewModel = AffiliateViewModel()

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.isLoading {
                    ProgressView("Carregando...")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let affiliate = viewModel.affiliate {
                    AffiliateDashboardView(
                        affiliate: affiliate,
                        referrals: viewModel.referrals,
                        payoutAmount: $viewModel.payoutAmount,
                        isRequestingPayout: viewModel.isRequestingPayout,
                        payoutMessage: viewModel.payoutMessage,
                        onRequestPayout: { viewModel.requestPayout() },
                        onCopyCode: { viewModel.copyCode(affiliate.code) },
                        onCopyLink: { viewModel.copyLink(affiliate.code) },
                        onLoadReferrals: { await viewModel.loadReferrals(affiliateId: affiliate.id) }
                    )
                } else {
                    AffiliateRegisterView(
                        name: $viewModel.registerName,
                        email: $viewModel.registerEmail,
                        isLoading: viewModel.isRegistering,
                        errorMessage: viewModel.errorMessage,
                        onRegister: { await viewModel.register() }
                    )
                }
            }
            .navigationTitle("Programa de Afiliados")
            .navigationBarTitleDisplayMode(.large)
        }
        .task { await viewModel.loadAffiliate() }
        .alert("Erro", isPresented: $viewModel.showError) {
            Button("OK") { viewModel.showError = false }
        } message: {
            Text(viewModel.errorMessage ?? "Ocorreu um erro inesperado.")
        }
    }
}

// MARK: - Register View

private struct AffiliateRegisterView: View {
    @Binding var name: String
    @Binding var email: String
    let isLoading: Bool
    let errorMessage: String?
    let onRegister: () async -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 28) {
                // Header gradient
                VStack(spacing: 12) {
                    ZStack {
                        LinearGradient.fypGradient
                            .frame(height: 160)
                            .clipShape(RoundedRectangle(cornerRadius: 20))

                        VStack(spacing: 8) {
                            Text("🤝").font(.system(size: 52))
                            Text("Programa de Afiliados")
                                .font(.title2.bold())
                                .foregroundColor(.white)
                        }
                    }

                    Text("Ganhe dinheiro indicando o FypMatch")
                        .font(.headline)
                        .foregroundStyle(LinearGradient.fypGradient)
                        .multilineTextAlignment(.center)
                }

                // Benefícios
                VStack(spacing: 12) {
                    BenefitRow(icon: "sparkles", text: "20% de comissão no 1º mês Premium (R$ 9,99/mês)")
                    BenefitRow(icon: "crown.fill", text: "25% de comissão no 1º mês VIP (R$ 19,99/mês)")
                    BenefitRow(icon: "banknote.fill", text: "Saque mínimo de R$ 50,00 via Pix")
                    BenefitRow(icon: "chart.line.uptrend.xyaxis", text: "Dashboard com suas indicações em tempo real")
                    BenefitRow(icon: "link", text: "Link personalizado para compartilhar")
                }
                .padding(16)
                .background(Color(.secondarySystemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 16))

                // Formulário
                VStack(spacing: 16) {
                    FypTextField(
                        placeholder: "Seu nome completo",
                        text: $name,
                        icon: "person"
                    )

                    FypTextField(
                        placeholder: "Seu email",
                        text: $email,
                        icon: "envelope"
                    )
                    .keyboardType(.emailAddress)
                    .autocapitalization(.none)
                }

                if let error = errorMessage {
                    HStack {
                        Image(systemName: "exclamationmark.triangle.fill").foregroundColor(.orange)
                        Text(error).font(.subheadline).foregroundColor(.orange)
                    }
                    .padding(12)
                    .background(Color.orange.opacity(0.1))
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                }

                GradientButton(
                    title: "Quero ser afiliado!",
                    icon: "hand.raised.fill",
                    isLoading: isLoading
                ) {
                    Task { await onRegister() }
                }
                .disabled(name.isEmpty || email.isEmpty || isLoading)
                .opacity((name.isEmpty || email.isEmpty) ? 0.5 : 1)

                Text("Ao se cadastrar, você concorda com os termos do Programa de Afiliados FypMatch.")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }
            .padding(20)
        }
    }
}

private struct BenefitRow: View {
    let icon: String
    let text: String

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 16, weight: .semibold))
                .foregroundStyle(LinearGradient.fypGradient)
                .frame(width: 28)
            Text(text)
                .font(.subheadline)
                .foregroundColor(.primary)
            Spacer()
        }
    }
}

// MARK: - Dashboard View

private struct AffiliateDashboardView: View {
    let affiliate: Affiliate
    let referrals: [Referral]
    @Binding var payoutAmount: String
    let isRequestingPayout: Bool
    let payoutMessage: String?
    let onRequestPayout: () -> Void
    let onCopyCode: () -> Void
    let onCopyLink: () -> Void
    let onLoadReferrals: () async -> Void

    @State private var codeCopied = false
    @State private var linkCopied = false

    private let currencyFormatter: NumberFormatter = {
        let f = NumberFormatter()
        f.numberStyle = .currency
        f.locale = Locale(identifier: "pt_BR")
        return f
    }()

    private func formatted(_ value: Double) -> String {
        currencyFormatter.string(from: NSNumber(value: value)) ?? "R$ 0,00"
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Header
                ZStack {
                    LinearGradient.fypGradient
                        .frame(height: 120)
                        .clipShape(RoundedRectangle(cornerRadius: 16))

                    VStack(spacing: 4) {
                        Text("🤝").font(.system(size: 36))
                        Text("Olá, \(affiliate.name.components(separatedBy: " ").first ?? affiliate.name)!")
                            .font(.title3.bold())
                            .foregroundColor(.white)
                        Text("Seu painel de afiliado")
                            .font(.subheadline)
                            .foregroundColor(.white.opacity(0.85))
                    }
                }

                // Stats — 4 cards em grade 2x2
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                    StatCard(
                        title: "Total de indicações",
                        value: "\(affiliate.stats.totalReferrals)",
                        icon: "person.2.fill",
                        color: .fypPink
                    )
                    StatCard(
                        title: "Ganhos pendentes",
                        value: formatted(affiliate.stats.pendingEarnings),
                        icon: "clock.fill",
                        color: .orange
                    )
                    StatCard(
                        title: "Ganhos totais",
                        value: formatted(affiliate.stats.totalEarnings),
                        icon: "banknote.fill",
                        color: .green
                    )
                    StatCard(
                        title: "Conversão",
                        value: String(format: "%.1f%%", affiliate.stats.conversionRate * 100),
                        icon: "chart.bar.fill",
                        color: .fypPurple
                    )
                }

                // Código de afiliado
                VStack(alignment: .leading, spacing: 10) {
                    Text("Seu código de afiliado")
                        .font(.headline)

                    HStack {
                        Text(affiliate.code)
                            .font(.system(size: 18, weight: .bold, design: .monospaced))
                            .foregroundStyle(LinearGradient.fypGradient)
                            .frame(maxWidth: .infinity, alignment: .leading)

                        Button {
                            onCopyCode()
                            codeCopied = true
                            DispatchQueue.main.asyncAfter(deadline: .now() + 2) { codeCopied = false }
                        } label: {
                            Label(codeCopied ? "Copiado!" : "Copiar", systemImage: codeCopied ? "checkmark" : "doc.on.doc")
                                .font(.subheadline.bold())
                                .foregroundColor(codeCopied ? .green : .fypPink)
                        }
                    }
                    .padding(14)
                    .background(Color(.secondarySystemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                // Link de indicação
                VStack(alignment: .leading, spacing: 10) {
                    Text("Link de indicação")
                        .font(.headline)

                    let link = "https://fypmatch.app/ref/\(affiliate.code)"

                    HStack(spacing: 10) {
                        Text(link)
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                            .truncationMode(.middle)
                            .frame(maxWidth: .infinity, alignment: .leading)

                        Button {
                            onCopyLink()
                            linkCopied = true
                            DispatchQueue.main.asyncAfter(deadline: .now() + 2) { linkCopied = false }
                        } label: {
                            Image(systemName: linkCopied ? "checkmark" : "doc.on.doc")
                                .foregroundColor(linkCopied ? .green : .fypPink)
                        }

                        if let url = URL(string: link) {
                            ShareLink(item: url) {
                                Image(systemName: "square.and.arrow.up")
                                    .foregroundColor(.fypPurple)
                            }
                        }
                    }
                    .padding(12)
                    .background(Color(.secondarySystemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                // Indicações recentes
                if !referrals.isEmpty {
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Indicações recentes")
                            .font(.headline)

                        ForEach(referrals.prefix(5)) { referral in
                            ReferralRow(referral: referral, formatter: currencyFormatter)
                        }
                    }
                }

                // Solicitar saque
                VStack(alignment: .leading, spacing: 12) {
                    Text("Solicitar Saque")
                        .font(.headline)

                    Text("Mínimo: R$ 50,00 • Pagamento via Pix")
                        .font(.caption)
                        .foregroundColor(.secondary)

                    if affiliate.stats.pendingEarnings < 50 {
                        HStack(spacing: 8) {
                            Image(systemName: "info.circle").foregroundColor(.orange)
                            Text("Você precisa de pelo menos R$ 50,00 em ganhos pendentes para solicitar o saque.")
                                .font(.caption)
                                .foregroundColor(.orange)
                        }
                        .padding(10)
                        .background(Color.orange.opacity(0.1))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                    } else {
                        FypTextField(
                            placeholder: "Valor do saque (ex: 50,00)",
                            text: $payoutAmount,
                            icon: "brazilianrealsign"
                        )
                        .keyboardType(.decimalPad)

                        if let msg = payoutMessage {
                            Text(msg)
                                .font(.subheadline)
                                .foregroundColor(msg.contains("sucesso") ? .green : .orange)
                                .padding(.horizontal, 4)
                        }

                        GradientButton(
                            title: "Solicitar Saque",
                            icon: "banknote",
                            isLoading: isRequestingPayout
                        ) { onRequestPayout() }
                        .disabled(payoutAmount.isEmpty || isRequestingPayout)
                        .opacity(payoutAmount.isEmpty ? 0.5 : 1)
                    }
                }
                .padding(16)
                .background(Color(.secondarySystemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 16))
            }
            .padding(16)
        }
        .task { await onLoadReferrals() }
    }
}

private struct StatCard: View {
    let title: String
    let value: String
    let icon: String
    let color: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Image(systemName: icon)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(color)
                Spacer()
            }
            Text(value)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.primary)
                .minimumScaleFactor(0.7)
                .lineLimit(1)
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
                .lineLimit(2)
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}

private struct ReferralRow: View {
    let referral: Referral
    let formatter: NumberFormatter

    private var statusColor: Color {
        switch referral.status {
        case .pending:    return .orange
        case .confirmed:  return .blue
        case .paid:       return .green
        case .cancelled:  return .red
        }
    }

    private var statusLabel: String {
        switch referral.status {
        case .pending:    return "Pendente"
        case .confirmed:  return "Confirmado"
        case .paid:       return "Pago"
        case .cancelled:  return "Cancelado"
        }
    }

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 3) {
                Text(referral.referredUserName)
                    .font(.subheadline.bold())
                if let plan = referral.planPurchased {
                    Text(plan.capitalized)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            Spacer()
            VStack(alignment: .trailing, spacing: 3) {
                Text(formatter.string(from: NSNumber(value: referral.commissionAmount)) ?? "R$ 0,00")
                    .font(.subheadline.bold())
                    .foregroundStyle(LinearGradient.fypGradient)
                Text(statusLabel)
                    .font(.caption.bold())
                    .foregroundColor(statusColor)
            }
        }
        .padding(12)
        .background(Color(.tertiarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}

// MARK: - ViewModel

@MainActor
class AffiliateViewModel: ObservableObject {
    @Published var affiliate: Affiliate?
    @Published var referrals: [Referral] = []
    @Published var isLoading = false
    @Published var isRegistering = false
    @Published var isRequestingPayout = false
    @Published var errorMessage: String?
    @Published var showError = false
    @Published var payoutAmount = ""
    @Published var payoutMessage: String?

    // Form
    @Published var registerName = ""
    @Published var registerEmail = ""

    private let service = AffiliateService.liveValue

    func loadAffiliate() async {
        guard let userId = FirebaseService.shared.currentUser?.id else { return }
        isLoading = true
        defer { isLoading = false }
        do {
            affiliate = try await service.getAffiliate(userId)
        } catch {
            // Sem afiliado — mostrar tela de cadastro
            affiliate = nil
        }
    }

    func loadReferrals(affiliateId: String) async {
        do {
            referrals = try await service.getReferrals(affiliateId)
        } catch {
            referrals = []
        }
    }

    func register() async {
        guard let userId = FirebaseService.shared.currentUser?.id else { return }
        guard !registerName.isEmpty, !registerEmail.isEmpty else {
            errorMessage = "Preencha todos os campos."
            return
        }
        isRegistering = true
        defer { isRegistering = false }
        do {
            affiliate = try await service.registerAffiliate(userId, registerName, registerEmail)
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
            showError = true
        }
    }

    func requestPayout() {
        guard let affiliate = affiliate else { return }
        let normalized = payoutAmount.replacingOccurrences(of: ",", with: ".")
        guard let amount = Double(normalized), amount >= 50 else {
            payoutMessage = "Valor mínimo de R$ 50,00."
            return
        }
        guard amount <= affiliate.stats.pendingEarnings else {
            payoutMessage = "Saldo pendente insuficiente."
            return
        }
        isRequestingPayout = true
        Task {
            defer { isRequestingPayout = false }
            do {
                _ = try await service.requestPayout(affiliate.id, amount)
                payoutMessage = "Solicitação enviada com sucesso! Processamos em até 3 dias úteis."
                payoutAmount = ""
            } catch {
                payoutMessage = error.localizedDescription
            }
        }
    }

    func copyCode(_ code: String) {
        UIPasteboard.general.string = code
    }

    func copyLink(_ code: String) {
        UIPasteboard.general.string = service.generateLink(code)
    }
}
