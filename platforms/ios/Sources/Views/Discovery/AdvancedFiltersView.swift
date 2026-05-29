//
//  AdvancedFiltersView.swift
//  FypMatch iOS
//
//  Sprint 2 — Filtros avançados: básicos (todos) + premium-gated (PREMIUM/VIP).
//  Apresentada como sheet deslizante (.presentationDetents([.large])).
//

import SwiftUI

// MARK: - Modelo de Filtros

struct DiscoveryFilters: Equatable {

    // MARK: Básicos (todos os planos)
    var ageMin: Int = 18
    var ageMax: Int = 45
    var maxDistance: Int = 50
    var genderInterest: GenderInterest = .all

    // MARK: Avançados (premium)
    var heightMin: Int? = nil
    var heightMax: Int? = nil
    var hasChildren: ChildrenFilter = .any
    var smoking: LifestyleFilter = .any
    var drinking: DrinkingFilter = .any
    var religions: Set<String> = []
    var intentions: Set<String> = []
    var verifiedOnly: Bool = false
    var onlineOnly: Bool = false

    // MARK: - Enums internos

    enum ChildrenFilter: String, CaseIterable, Equatable {
        case has    = "Tem filhos"
        case hasNot = "Não tem"
        case any    = "Tanto faz"
    }

    enum LifestyleFilter: String, CaseIterable, Equatable {
        case yes      = "Sim"
        case socially = "Socialmente"
        case no       = "Não"
        case any      = "Tanto faz"
    }

    /// Filtro de bebida com opção "socialmente" (mapeado de DrinkingStatus)
    enum DrinkingFilter: String, CaseIterable, Equatable {
        case yes      = "Sim"
        case socially = "Socialmente"
        case no       = "Não"
        case any      = "Tanto faz"
    }

    // MARK: - Contagem de filtros ativos

    var activeCount: Int {
        var count = 0
        if ageMin != 18 || ageMax != 45  { count += 1 }
        if maxDistance != 50             { count += 1 }
        if genderInterest != .all        { count += 1 }
        if heightMin != nil || heightMax != nil { count += 1 }
        if hasChildren != .any           { count += 1 }
        if smoking != .any               { count += 1 }
        if drinking != .any              { count += 1 }
        if !religions.isEmpty            { count += 1 }
        if !intentions.isEmpty           { count += 1 }
        if verifiedOnly                  { count += 1 }
        if onlineOnly                    { count += 1 }
        return count
    }

    // MARK: - Reset

    static var defaults: DiscoveryFilters { DiscoveryFilters() }
}

// MARK: - Constantes de domínio

private let kReligions: [String] = [
    "Agnóstico", "Ateu", "Budista", "Católico",
    "Cristão", "Espírita", "Evangélico", "Hinduísta",
    "Judeu", "Muçulmano", "Umbandista", "Outro"
]

private let kIntentions: [String] = [
    "Relacionamento sério", "Namoro casual",
    "Amizade", "Algo novo", "Ainda não sei"
]

// MARK: - AdvancedFiltersView

struct AdvancedFiltersView: View {
    @Binding var filters: DiscoveryFilters
    let isPremium: Bool
    let onApply: (DiscoveryFilters) -> Void
    let onShowPremiumUpsell: () -> Void

    @Environment(\.dismiss) private var dismiss

    @State private var tempFilters: DiscoveryFilters

    init(
        filters: Binding<DiscoveryFilters>,
        isPremium: Bool,
        onApply: @escaping (DiscoveryFilters) -> Void,
        onShowPremiumUpsell: @escaping () -> Void
    ) {
        self._filters = filters
        self.isPremium = isPremium
        self.onApply = onApply
        self.onShowPremiumUpsell = onShowPremiumUpsell
        self._tempFilters = State(initialValue: filters.wrappedValue)
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {

                    // Banner premium para usuários FREE
                    if !isPremium {
                        premiumBanner
                            .padding(.horizontal, 16)
                            .padding(.top, 8)
                    }

                    // Seção básica
                    basicFiltersSection
                        .padding(.horizontal, 16)

                    // Seção avançada
                    advancedFiltersSection
                        .padding(.horizontal, 16)

                    // Espaço para o botão fixo
                    Spacer().frame(height: 80)
                }
                .padding(.vertical, 8)
            }
            .safeAreaInset(edge: .bottom) {
                applyButton
                    .padding(.horizontal, 16)
                    .padding(.bottom, 24)
                    .background(.regularMaterial)
            }
            .navigationTitle("Filtros Avançados")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { toolbarContent }
        }
        .presentationDetents([.large])
        .presentationDragIndicator(.visible)
    }

    // MARK: - Toolbar

    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        ToolbarItem(placement: .topBarLeading) {
            Button("Fechar") { dismiss() }
        }
        ToolbarItem(placement: .topBarTrailing) {
            if tempFilters.activeCount > 0 {
                Button("Limpar") {
                    withAnimation { tempFilters = .defaults }
                }
                .foregroundColor(.fypPink)
            }
        }
        ToolbarItem(placement: .principal) {
            VStack(spacing: 0) {
                Text("Filtros Avançados")
                    .font(.headline)
                if tempFilters.activeCount > 0 {
                    Text("\(tempFilters.activeCount) filtro\(tempFilters.activeCount == 1 ? "" : "s") ativo\(tempFilters.activeCount == 1 ? "" : "s")")
                        .font(.caption)
                        .foregroundColor(.fypPink)
                }
            }
        }
    }

    // MARK: - Banner premium (FREE)

    private var premiumBanner: some View {
        HStack(spacing: 14) {
            Image(systemName: "lock.fill")
                .font(.title2)
                .foregroundStyle(LinearGradient.fypGradient)

            VStack(alignment: .leading, spacing: 2) {
                Text("Filtros avançados são Premium")
                    .font(.subheadline.bold())
                Text("Faça upgrade para usar todos os filtros")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            Spacer()

            Button {
                onShowPremiumUpsell()
            } label: {
                Text("Upgrade")
                    .font(.caption.bold())
                    .foregroundColor(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(LinearGradient.fypGradient)
                    .clipShape(Capsule())
            }
        }
        .padding(14)
        .background(
            LinearGradient(
                colors: [Color.fypPink.opacity(0.08), Color.fypPurple.opacity(0.08)],
                startPoint: .leading, endPoint: .trailing
            )
        )
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(
                    LinearGradient(
                        colors: [Color.fypPink.opacity(0.3), Color.fypPurple.opacity(0.3)],
                        startPoint: .leading, endPoint: .trailing
                    ),
                    lineWidth: 1
                )
        )
    }

    // MARK: - Seção básica

    private var basicFiltersSection: some View {
        FilterCard(title: "Básicos", icon: "slider.horizontal.3", isPremium: false) {
            // Faixa etária
            ageRangeFilter

            Divider().padding(.vertical, 4)

            // Distância
            distanceFilter

            Divider().padding(.vertical, 4)

            // Interesse em
            genderInterestFilter
        }
    }

    // MARK: - Seção avançada

    private var advancedFiltersSection: some View {
        FilterCard(title: "Avançados", icon: "sparkles", isPremium: true) {

            // Altura
            heightFilter

            Divider().padding(.vertical, 4)

            // Filhos
            premiumRow(title: "Filhos") {
                SegmentedPickerRow(
                    options: DiscoveryFilters.ChildrenFilter.allCases,
                    selected: $tempFilters.hasChildren,
                    label: \.rawValue
                )
            }

            Divider().padding(.vertical, 4)

            // Fumante
            premiumRow(title: "Fumante") {
                SegmentedPickerRow(
                    options: DiscoveryFilters.LifestyleFilter.allCases,
                    selected: $tempFilters.smoking,
                    label: \.rawValue
                )
            }

            Divider().padding(.vertical, 4)

            // Bebida
            premiumRow(title: "Bebida") {
                SegmentedPickerRow(
                    options: DiscoveryFilters.DrinkingFilter.allCases,
                    selected: $tempFilters.drinking,
                    label: \.rawValue
                )
            }

            Divider().padding(.vertical, 4)

            // Religião
            premiumRow(title: "Religião") {
                MultiChipPicker(
                    options: kReligions,
                    selected: $tempFilters.religions
                )
            }

            Divider().padding(.vertical, 4)

            // Intenção
            premiumRow(title: "Intenção") {
                MultiChipPicker(
                    options: kIntentions,
                    selected: $tempFilters.intentions
                )
            }

            Divider().padding(.vertical, 4)

            // Verificado
            premiumRow(title: nil) {
                LockableToggleRow(
                    title: "Verificado ✓ apenas",
                    subtitle: "Exibe somente perfis verificados",
                    value: $tempFilters.verifiedOnly,
                    isLocked: !isPremium,
                    onTapLocked: onShowPremiumUpsell
                )
            }

            // Online agora
            premiumRow(title: nil) {
                LockableToggleRow(
                    title: "Online agora apenas",
                    subtitle: "Prioriza quem está ativo no momento",
                    value: $tempFilters.onlineOnly,
                    isLocked: !isPremium,
                    onTapLocked: onShowPremiumUpsell
                )
            }
        }
    }

    // MARK: - Filtros individuais básicos

    private var ageRangeFilter: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("Faixa etária")
                    .font(.subheadline.bold())
                Spacer()
                Text("\(tempFilters.ageMin) – \(tempFilters.ageMax) anos")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            // Slider mínimo
            HStack {
                Text("Min")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .frame(width: 28)
                Slider(
                    value: Binding(
                        get: { Double(tempFilters.ageMin) },
                        set: { tempFilters.ageMin = min(Int($0), tempFilters.ageMax - 1) }
                    ),
                    in: 18...60,
                    step: 1
                )
                .tint(.fypPink)
            }
            // Slider máximo
            HStack {
                Text("Max")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .frame(width: 28)
                Slider(
                    value: Binding(
                        get: { Double(tempFilters.ageMax) },
                        set: { tempFilters.ageMax = max(Int($0), tempFilters.ageMin + 1) }
                    ),
                    in: 18...80,
                    step: 1
                )
                .tint(.fypPurple)
            }
        }
    }

    private var distanceFilter: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("Distância")
                    .font(.subheadline.bold())
                Spacer()
                Text("até \(tempFilters.maxDistance) km")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            Slider(
                value: Binding(
                    get: { Double(tempFilters.maxDistance) },
                    set: { tempFilters.maxDistance = Int($0) }
                ),
                in: 1...200,
                step: 5
            )
            .tint(.fypPink)
        }
    }

    private var genderInterestFilter: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Interesse em")
                .font(.subheadline.bold())
            SegmentedPickerRow(
                options: GenderInterest.allCases,
                selected: $tempFilters.genderInterest,
                label: \.displayName
            )
        }
    }

    // MARK: - Filtro de altura (premium)

    private var heightFilter: some View {
        premiumRow(title: "Altura") {
            VStack(alignment: .leading, spacing: 8) {
                // Toggle para ativar filtro de altura
                Toggle(isOn: Binding(
                    get: { tempFilters.heightMin != nil },
                    set: { active in
                        if active {
                            tempFilters.heightMin = 155
                            tempFilters.heightMax = 185
                        } else {
                            tempFilters.heightMin = nil
                            tempFilters.heightMax = nil
                        }
                    }
                )) {
                    Text(tempFilters.heightMin == nil
                         ? "Qualquer altura"
                         : "\(tempFilters.heightMin ?? 150) – \(tempFilters.heightMax ?? 190) cm")
                        .font(.subheadline)
                        .foregroundColor(isPremium ? .primary : .secondary)
                }
                .tint(.fypPink)
                .disabled(!isPremium)

                if let hMin = tempFilters.heightMin, let hMax = tempFilters.heightMax, isPremium {
                    HStack {
                        Text("Min")
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .frame(width: 28)
                        Slider(
                            value: Binding(
                                get: { Double(hMin) },
                                set: { tempFilters.heightMin = min(Int($0), hMax - 1) }
                            ),
                            in: 140...220,
                            step: 1
                        )
                        .tint(.fypPink)
                    }
                    HStack {
                        Text("Max")
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .frame(width: 28)
                        Slider(
                            value: Binding(
                                get: { Double(hMax) },
                                set: { tempFilters.heightMax = max(Int($0), hMin + 1) }
                            ),
                            in: 140...220,
                            step: 1
                        )
                        .tint(.fypPurple)
                    }
                }
            }
        }
    }

    // MARK: - Wrapper de linha premium com cadeado

    @ViewBuilder
    private func premiumRow<Content: View>(
        title: String?,
        @ViewBuilder content: @escaping () -> Content
    ) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            if let title {
                HStack(spacing: 4) {
                    Text(title)
                        .font(.subheadline.bold())
                        .foregroundColor(isPremium ? .primary : .secondary)
                    if !isPremium {
                        Image(systemName: "lock.fill")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }
            content()
                .allowsHitTesting(isPremium)
                .opacity(isPremium ? 1 : 0.45)
                .overlay(
                    isPremium ? nil :
                        Color.clear
                            .contentShape(Rectangle())
                            .onTapGesture { onShowPremiumUpsell() }
                )
        }
    }

    // MARK: - Botão aplicar

    private var applyButton: some View {
        GradientButton(title: "Aplicar filtros") {
            filters = tempFilters
            onApply(tempFilters)
            dismiss()
        }
    }
}

// MARK: - Componentes auxiliares

/// Card de seção com título e badge premium opcional
struct FilterCard<Content: View>: View {
    let title: String
    let icon: String
    var isPremium: Bool = false
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack(spacing: 8) {
                Image(systemName: icon)
                    .foregroundStyle(
                        isPremium
                            ? LinearGradient(colors: [Color(hex: "FFD700"), Color(hex: "FF8C00")], startPoint: .leading, endPoint: .trailing)
                            : LinearGradient.fypGradient
                    )
                Text(title)
                    .font(.headline)

                if isPremium {
                    PremiumBadge(tier: .premium)
                }
            }

            content()
        }
        .padding(16)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}

/// Picker em linha de opções (segmented-style em chips)
struct SegmentedPickerRow<T: Hashable>: View {
    let options: [T]
    @Binding var selected: T
    let label: KeyPath<T, String>

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(options, id: \.hashValue) { option in
                    let isSelected = option == selected
                    Button {
                        withAnimation(.spring(duration: 0.2)) { selected = option }
                    } label: {
                        Text(option[keyPath: label])
                            .font(.system(size: 13, weight: isSelected ? .semibold : .regular))
                            .foregroundColor(isSelected ? .white : .primary)
                            .padding(.horizontal, 14)
                            .padding(.vertical, 8)
                            .background(
                                isSelected
                                    ? AnyShapeStyle(LinearGradient.fypGradient)
                                    : AnyShapeStyle(Color(.tertiarySystemBackground))
                            )
                            .clipShape(Capsule())
                            .overlay(
                                Capsule().stroke(
                                    isSelected ? Color.clear : Color(.separator),
                                    lineWidth: 1
                                )
                            )
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }
}

/// Multi-seleção em chips
struct MultiChipPicker: View {
    let options: [String]
    @Binding var selected: Set<String>

    var body: some View {
        FlowLayout(spacing: 8) {
            ForEach(options, id: \.self) { option in
                let isSelected = selected.contains(option)
                Button {
                    withAnimation(.spring(duration: 0.2)) {
                        if isSelected { selected.remove(option) }
                        else { selected.insert(option) }
                    }
                } label: {
                    Text(option)
                        .font(.system(size: 13, weight: isSelected ? .semibold : .regular))
                        .foregroundColor(isSelected ? .white : .primary)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 7)
                        .background(
                            isSelected
                                ? AnyShapeStyle(LinearGradient.fypGradient)
                                : AnyShapeStyle(Color(.tertiarySystemBackground))
                        )
                        .clipShape(Capsule())
                        .overlay(
                            Capsule().stroke(
                                isSelected ? Color.clear : Color(.separator),
                                lineWidth: 1
                            )
                        )
                }
                .buttonStyle(.plain)
            }
        }
    }
}

/// Toggle com suporte a cadeado (gating premium)
struct LockableToggleRow: View {
    let title: String
    let subtitle: String
    @Binding var value: Bool
    let isLocked: Bool
    let onTapLocked: () -> Void

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 4) {
                    Text(title)
                        .font(.subheadline)
                        .foregroundColor(isLocked ? .secondary : .primary)
                    if isLocked {
                        Image(systemName: "lock.fill")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                Text(subtitle)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            Spacer()
            if isLocked {
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(.secondary)
            } else {
                Toggle("", isOn: $value)
                    .labelsHidden()
                    .tint(.fypPink)
            }
        }
        .contentShape(Rectangle())
        .onTapGesture {
            if isLocked { onTapLocked() }
        }
    }
}

/// Layout em fluxo horizontal com quebra automática de linha
struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let maxWidth = proposal.width ?? .infinity
        var x: CGFloat = 0
        var y: CGFloat = 0
        var rowHeight: CGFloat = 0
        var totalHeight: CGFloat = 0

        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if x + size.width > maxWidth, x > 0 {
                y += rowHeight + spacing
                x = 0
                rowHeight = 0
                totalHeight = y
            }
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
        totalHeight += rowHeight
        return CGSize(width: maxWidth, height: totalHeight)
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
            view.place(at: CGPoint(x: x, y: y), proposal: .unspecified)
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
    }
}

// MARK: - Preview

#Preview("FREE") {
    @Previewable @State var filters = DiscoveryFilters()
    AdvancedFiltersView(
        filters: $filters,
        isPremium: false,
        onApply: { _ in },
        onShowPremiumUpsell: {}
    )
}

#Preview("PREMIUM") {
    @Previewable @State var filters = DiscoveryFilters()
    AdvancedFiltersView(
        filters: $filters,
        isPremium: true,
        onApply: { _ in },
        onShowPremiumUpsell: {}
    )
}
