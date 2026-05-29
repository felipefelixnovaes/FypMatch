// PremiumView.swift — FypMatch iOS
// Port de PremiumScreen.kt

import SwiftUI

struct PremiumView: View {
    @State private var selectedPlan: PremiumPlan = .premium
    @State private var isPurchasing = false
    @State private var showSuccess = false
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // Header
                    VStack(spacing: 8) {
                        Image(systemName: "crown.fill")
                            .font(.system(size: 56))
                            .foregroundStyle(LinearGradient(colors: [Color(hex:"FFD700"), Color(hex:"FF8C00")], startPoint: .top, endPoint: .bottom))
                            .padding(.top, 24)
                        GradientText(text: "FypMatch Premium")
                        Text("Desbloqueie conexões reais sem limites")
                            .font(.subheadline).foregroundColor(.secondary).multilineTextAlignment(.center)
                    }

                    // Plan selector
                    HStack(spacing: 12) {
                        ForEach(PremiumPlan.allCases) { plan in
                            PlanCard(plan: plan, isSelected: selectedPlan == plan) {
                                withAnimation(.spring()) { selectedPlan = plan }
                            }
                        }
                    }
                    .padding(.horizontal, 16)

                    // Features list
                    VStack(spacing: 0) {
                        ForEach(selectedPlan.features) { feature in
                            FeatureRow(feature: feature)
                            if feature.id != selectedPlan.features.last?.id { Divider().padding(.leading, 52) }
                        }
                    }
                    .background(Color(.secondarySystemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .padding(.horizontal, 16)

                    // CTA
                    VStack(spacing: 10) {
                        GradientButton(
                            title: isPurchasing ? "Processando…" : "Assinar \(selectedPlan.displayName) — \(selectedPlan.price)",
                            isLoading: isPurchasing
                        ) { purchase() }
                        .padding(.horizontal, 16)

                        Text("Cancele quando quiser. Sem fidelidade.")
                            .font(.caption).foregroundColor(.secondary)

                        Button("Restaurar compras") {}
                            .font(.caption).foregroundColor(.fypPink)
                    }
                    .padding(.bottom, 32)
                }
            }
            .navigationTitle("Planos")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Fechar") { dismiss() }.foregroundColor(.fypPink)
                }
            }
        }
        .alert("Assinatura ativada! 🎉", isPresented: $showSuccess) {
            Button("Aproveitar") { dismiss() }
        } message: {
            Text("Bem-vindo ao \(selectedPlan.displayName)! Todos os recursos estão desbloqueados.")
        }
    }

    private func purchase() {
        isPurchasing = true
        Task {
            try? await Task.sleep(for: .seconds(2))
            isPurchasing = false
            showSuccess = true
        }
    }
}

// MARK: - PlanCard

struct PlanCard: View {
    let plan: PremiumPlan
    let isSelected: Bool
    let onSelect: () -> Void

    var body: some View {
        Button(action: onSelect) {
            VStack(spacing: 8) {
                if plan == .vip {
                    Text("POPULAR").font(.caption2.bold()).foregroundColor(.white)
                        .padding(.horizontal, 8).padding(.vertical, 3)
                        .background(LinearGradient(colors: [Color(hex:"FFD700"), Color(hex:"FF8C00")], startPoint: .leading, endPoint: .trailing))
                        .clipShape(Capsule())
                }
                Text(plan.displayName).font(.headline)
                    .foregroundColor(isSelected ? .white : .primary)
                Text(plan.price).font(.title3.bold())
                    .foregroundColor(isSelected ? .white : .fypPink)
                Text(plan.period).font(.caption)
                    .foregroundColor(isSelected ? .white.opacity(0.8) : .secondary)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .background(isSelected ? AnyShapeStyle(LinearGradient.fypGradient) : AnyShapeStyle(Color(.secondarySystemBackground)))
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(RoundedRectangle(cornerRadius: 14).stroke(isSelected ? Color.clear : Color.secondary.opacity(0.2), lineWidth: 1))
        }
    }
}

// MARK: - FeatureRow

struct FeatureRow: View {
    let feature: PlanFeature
    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: feature.icon)
                .font(.system(size: 18)).foregroundStyle(LinearGradient.fypGradient)
                .frame(width: 36, height: 36)
                .background(Color.fypPink.opacity(0.1)).clipShape(RoundedRectangle(cornerRadius: 8))
            VStack(alignment: .leading, spacing: 2) {
                Text(feature.title).font(.subheadline.bold())
                Text(feature.description).font(.caption).foregroundColor(.secondary)
            }
            Spacer()
        }
        .padding(.horizontal, 16).padding(.vertical, 12)
    }
}

// MARK: - Models

enum PremiumPlan: String, CaseIterable, Identifiable {
    case premium, vip
    var id: String { rawValue }
    var displayName: String { self == .premium ? "Premium" : "VIP" }
    var price: String { self == .premium ? "R$ 19,90" : "R$ 39,90" }
    var period: String { "/mês" }
    var features: [PlanFeature] {
        switch self {
        case .premium:
            return [
                PlanFeature(icon: "heart.fill", title: "100 curtidas por dia", description: "10x mais que o plano gratuito"),
                PlanFeature(icon: "star.circle.fill", title: "5 super curtidas por dia", description: "Destaque-se nos perfis"),
                PlanFeature(icon: "eye.fill", title: "Ver quem curtiu você", description: "Responda antes que passe o momento"),
                PlanFeature(icon: "bolt.fill", title: "1 boost por mês (30 min)", description: "Apareça para mais pessoas"),
                PlanFeature(icon: "brain.head.profile", title: "10 créditos IA por dia", description: "Sem precisar de anúncios"),
                PlanFeature(icon: "nosign", title: "Sem anúncios", description: "Experiência limpa e fluida"),
                PlanFeature(icon: "slider.horizontal.3", title: "Filtros avançados", description: "Refine seus matches com precisão"),
            ]
        case .vip:
            return [
                PlanFeature(icon: "infinity", title: "Curtidas ilimitadas", description: "Sem limite diário"),
                PlanFeature(icon: "star.fill", title: "Super curtidas ilimitadas", description: "Destaque-se em todos os perfis"),
                PlanFeature(icon: "bolt.circle.fill", title: "5 boosts/mês + Super Boost 2h", description: "Apareça em destaque por mais tempo"),
                PlanFeature(icon: "brain.head.profile", title: "25 créditos IA por dia", description: "2,5x mais que o Premium"),
                PlanFeature(icon: "arrow.up.circle.fill", title: "Prioridade no algoritmo", description: "Apareça primeiro nas descobertas"),
                PlanFeature(icon: "crown.fill", title: "Selo VIP exclusivo", description: "Destaque-se nos matches"),
                PlanFeature(icon: "chart.bar.fill", title: "Analytics avançado", description: "Insights detalhados do seu perfil"),
                PlanFeature(icon: "person.badge.shield.checkmark", title: "Suporte prioritário", description: "Resposta em até 2 horas"),
            ]
        }
    }
}

struct PlanFeature: Identifiable {
    let id = UUID()
    let icon: String
    let title: String
    let description: String
}
