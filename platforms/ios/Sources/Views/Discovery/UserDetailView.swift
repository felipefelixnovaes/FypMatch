//
//  UserDetailView.swift
//  FypMatch iOS
//
//  Tela de perfil completo exibida ao tocar em um card de discovery
//  Estrutura: carrossel de fotos 420pt + seções de perfil + bottom bar de swipe
//

import SwiftUI
import ComposableArchitecture

// MARK: - UserDetailView

struct UserDetailView: View {
    @Bindable var store: StoreOf<UserDetailFeature>
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottom) {
                ScrollView(showsIndicators: false) {
                    VStack(spacing: 0) {
                        // Carrossel de fotos
                        photoCarousel
                            .frame(height: 420)

                        // Seções de perfil
                        profileSections
                            .padding(.horizontal, 16)
                            .padding(.top, 16)
                            .padding(.bottom, 100) // espaço para o bottom bar
                    }
                }

                // Bottom bar fixo com ações de swipe
                swipeBottomBar
            }
            .ignoresSafeArea(edges: .top)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { toolbarContent }
            .sheet(isPresented: $store.isShowingPremiumUpsell) {
                PremiumUpsellSheet(reason: nil)
            }
            .alert("Denunciar perfil", isPresented: $store.isShowingReport, actions: {
                ForEach(reportReasons, id: \.self) { reason in
                    Button(reason) { store.send(.submitReport(reason: reason)) }
                }
                Button("Cancelar", role: .cancel) { store.send(.binding(.set(\.isShowingReport, false))) }
            }, message: {
                Text("Selecione o motivo da denúncia")
            })
            .alert("Bloquear \(store.user.displayName)?", isPresented: $store.isShowingBlock, actions: {
                Button("Bloquear", role: .destructive) { store.send(.confirmBlock) }
                Button("Cancelar", role: .cancel) { store.send(.binding(.set(\.isShowingBlock, false))) }
            }, message: {
                Text("Você não verá mais este perfil e ele não poderá entrar em contato com você.")
            })
            .alert(store.alertMessage ?? "", isPresented: Binding(
                get: { store.alertMessage != nil },
                set: { if !$0 { store.send(.dismissAlert) } }
            )) {
                Button("OK") { store.send(.dismissAlert) }
            }
            .onChange(of: store.swipeAction) { _, action in
                if action != nil { dismiss() }
            }
        }
        .onAppear { store.send(.onAppear) }
    }

    // MARK: - Carrossel de fotos

    private var photoCarousel: some View {
        ZStack(alignment: .bottom) {
            let photos = store.user.photos.isEmpty
                ? [store.user.primaryPhotoURL]
                : store.user.photos

            TabView(selection: Binding(
                get: { store.currentPhotoIndex },
                set: { store.send(.binding(.set(\.currentPhotoIndex, $0))) }
            )) {
                ForEach(Array(photos.enumerated()), id: \.offset) { index, url in
                    AsyncImage(url: URL(string: url)) { img in
                        img.resizable().scaledToFill()
                    } placeholder: {
                        LinearGradient.fypGradient
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .clipped()
                    .tag(index)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))

            // Gradiente inferior da foto
            LinearGradient(
                colors: [.clear, .black.opacity(0.45)],
                startPoint: .center,
                endPoint: .bottom
            )

            // Indicadores de foto
            if photos.count > 1 {
                HStack(spacing: 5) {
                    ForEach(photos.indices, id: \.self) { i in
                        Capsule()
                            .fill(i == store.currentPhotoIndex
                                  ? Color.white
                                  : Color.white.opacity(0.45))
                            .frame(
                                width: i == store.currentPhotoIndex ? 20 : 7,
                                height: 4
                            )
                            .animation(.spring(response: 0.3), value: store.currentPhotoIndex)
                    }
                }
                .padding(.bottom, 12)
            }

            // CompatibilityBadge overlay — canto inferior direito
            if let score = store.compatibilityScore {
                HStack {
                    Spacer()
                    CompatibilityBadge(score: Int(score))
                        .padding(.trailing, 14)
                        .padding(.bottom, store.user.photos.count > 1 ? 32 : 12)
                }
            }
        }
    }

    // MARK: - Seções do perfil

    private var profileSections: some View {
        VStack(alignment: .leading, spacing: 20) {
            // Nome, idade e badges
            headerSection

            // Localização
            if let city = store.user.city {
                profileInfoRow(icon: "location.fill", color: .fypPink, text: city)
            }

            // Bio
            if !store.user.bio.isEmpty {
                profileCard(title: "Sobre mim", systemIcon: "text.alignleft") {
                    Text(store.user.bio)
                        .font(.subheadline)
                        .foregroundColor(.primary.opacity(0.85))
                        .fixedSize(horizontal: false, vertical: true)
                }
            }

            // Interesses
            let allInterests = store.user.interests + store.user.hobbies
            if !allInterests.isEmpty {
                profileCard(title: "Interesses", systemIcon: "tag.fill") {
                    InterestsFlowView(interests: allInterests)
                }
            }

            // Informações pessoais
            personalInfoSection

            // Ocupação e educação
            if store.user.occupation != nil || store.user.education != nil {
                profileCard(title: "Carreira & Educação", systemIcon: "briefcase.fill") {
                    VStack(alignment: .leading, spacing: 8) {
                        if let occupation = store.user.occupation {
                            profileInfoRow(icon: "briefcase.fill", color: .fypPurple, text: occupation)
                        }
                        if let education = store.user.education {
                            profileInfoRow(icon: "graduationcap.fill", color: .fypPurple, text: education)
                        }
                    }
                }
            }

            // Intenções
            intentionsSection

            // Idiomas
            if !store.user.languages.isEmpty {
                profileCard(title: "Idiomas", systemIcon: "globe") {
                    InterestsFlowView(interests: store.user.languages)
                }
            }
        }
    }

    // MARK: - Header (nome, idade, verificado, premium)

    private var headerSection: some View {
        HStack(alignment: .center, spacing: 8) {
            VStack(alignment: .leading, spacing: 4) {
                HStack(alignment: .firstTextBaseline, spacing: 8) {
                    Text("\(store.user.displayName), \(store.user.age)")
                        .font(.system(size: 26, weight: .bold))
                    if store.user.isVerified {
                        Image(systemName: "checkmark.seal.fill")
                            .foregroundColor(.fypPink)
                            .font(.system(size: 18))
                    }
                }
                if store.user.isOnline {
                    HStack(spacing: 5) {
                        Circle().fill(Color.green).frame(width: 8, height: 8)
                        Text("Online agora").font(.caption).foregroundColor(.secondary)
                    }
                } else {
                    Text("Ativo recentemente").font(.caption).foregroundColor(.secondary)
                }
            }
            Spacer()
            if store.user.isActivePremium {
                PremiumBadge(tier: .premium)
            }
        }
    }

    // MARK: - Informações pessoais

    private var personalInfoSection: some View {
        let hasAnyInfo = store.user.height != nil
            || store.user.children != .no
            || store.user.smoking != .never
            || store.user.drinking != .never
            || store.user.religion != nil

        return Group {
            if hasAnyInfo {
                profileCard(title: "Informações", systemIcon: "person.fill") {
                    VStack(alignment: .leading, spacing: 8) {
                        if let height = store.user.height {
                            infoRow(label: "Altura", value: "\(height) cm")
                        }
                        if store.user.children != .no {
                            infoRow(label: "Filhos", value: store.user.children.displayName)
                        }
                        if let wants = store.user.wantsChildren {
                            infoRow(label: "Quer filhos", value: wants ? "Sim" : "Não")
                        }
                        if store.user.smoking != .never {
                            infoRow(label: "Fuma", value: store.user.smoking.displayName)
                        }
                        if store.user.drinking != .never {
                            infoRow(label: "Bebe", value: store.user.drinking.displayName)
                        }
                        if let religion = store.user.religion {
                            infoRow(label: "Religião", value: religion)
                        }
                    }
                }
            }
        }
    }

    // MARK: - Intenções de relacionamento

    private var intentionsSection: some View {
        let hasIntention = store.user.interestedInSeriousRelationship
            || store.user.interestedInCasualDating
            || store.user.interestedInFriendship

        return Group {
            if hasIntention {
                profileCard(title: "Procurando", systemIcon: "heart.fill") {
                    HStack(spacing: 8) {
                        if store.user.interestedInSeriousRelationship {
                            intentionChip(label: "Relacionamento sério", color: .fypPink)
                        }
                        if store.user.interestedInCasualDating {
                            intentionChip(label: "Dating casual", color: .fypPurple)
                        }
                        if store.user.interestedInFriendship {
                            intentionChip(label: "Amizade", color: .blue)
                        }
                    }
                    .flexibleWidth()
                }
            }
        }
    }

    // MARK: - Bottom Bar (PASS / SUPER LIKE / LIKE)

    private var swipeBottomBar: some View {
        VStack(spacing: 0) {
            Divider()
            HStack(spacing: 0) {
                Spacer()

                // PASS
                SwipeActionButton(
                    icon: "xmark",
                    label: "PASS",
                    gradient: LinearGradient(
                        colors: [Color(.systemGray3), Color(.systemGray4)],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    size: 64,
                    isLoading: store.isSendingSwipe
                ) {
                    store.send(.pass)
                }

                Spacer()

                // SUPER LIKE
                SwipeActionButton(
                    icon: "star.fill",
                    label: "SUPER",
                    gradient: LinearGradient(
                        colors: [Color(hex: "4FC3F7"), Color(hex: "FFD700")],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    size: 54,
                    isLoading: false
                ) {
                    store.send(.superLike)
                }

                Spacer()

                // LIKE
                SwipeActionButton(
                    icon: "heart.fill",
                    label: "LIKE",
                    gradient: LinearGradient.fypGradient,
                    size: 64,
                    isLoading: store.isSendingSwipe
                ) {
                    store.send(.like)
                }

                Spacer()
            }
            .padding(.vertical, 14)
            .background(.regularMaterial)
        }
    }

    // MARK: - Toolbar

    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        ToolbarItem(placement: .topBarLeading) {
            Button {
                dismiss()
            } label: {
                HStack(spacing: 4) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 16, weight: .semibold))
                    Text("Voltar")
                        .font(.subheadline)
                }
                .foregroundColor(.white)
                .shadow(radius: 4)
            }
        }

        ToolbarItem(placement: .principal) {
            Text("\(store.user.displayName), \(store.user.age)")
                .font(.headline)
                .foregroundColor(.white)
                .shadow(radius: 4)
        }

        ToolbarItem(placement: .topBarTrailing) {
            Menu {
                Button(role: .destructive) {
                    store.send(.showReport)
                } label: {
                    Label("Denunciar", systemImage: "exclamationmark.triangle.fill")
                }
                Button(role: .destructive) {
                    store.send(.showBlock)
                } label: {
                    Label("Bloquear", systemImage: "hand.raised.fill")
                }
            } label: {
                Image(systemName: "ellipsis.circle.fill")
                    .font(.system(size: 20))
                    .foregroundColor(.white)
                    .shadow(radius: 4)
            }
        }
    }

    // MARK: - Helpers de layout

    private func profileCard<Content: View>(
        title: String,
        systemIcon: String,
        @ViewBuilder content: () -> Content
    ) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 6) {
                Image(systemName: systemIcon)
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(.fypPink)
                Text(title)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.secondary)
            }
            content()
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }

    private func profileInfoRow(icon: String, color: Color, text: String) -> some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 13))
                .foregroundColor(color)
            Text(text)
                .font(.subheadline)
                .foregroundColor(.primary)
        }
    }

    private func infoRow(label: String, value: String) -> some View {
        HStack {
            Text(label)
                .font(.subheadline)
                .foregroundColor(.secondary)
            Spacer()
            Text(value)
                .font(.subheadline.weight(.medium))
                .foregroundColor(.primary)
        }
    }

    private func intentionChip(label: String, color: Color) -> some View {
        Text(label)
            .font(.caption.weight(.medium))
            .foregroundColor(color)
            .padding(.horizontal, 10)
            .padding(.vertical, 5)
            .background(color.opacity(0.12))
            .clipShape(Capsule())
    }

    // MARK: - Motivos de denúncia

    private let reportReasons = [
        "Fotos inadequadas",
        "Comportamento ofensivo",
        "Perfil falso",
        "Spam ou golpe",
        "Menor de idade",
        "Outro motivo"
    ]
}

// MARK: - SwipeActionButton

private struct SwipeActionButton: View {
    let icon: String
    let label: String
    let gradient: LinearGradient
    let size: CGFloat
    var isLoading: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 4) {
                ZStack {
                    Circle()
                        .fill(gradient)
                        .frame(width: size, height: size)
                        .shadow(color: .black.opacity(0.18), radius: 8, y: 4)

                    if isLoading {
                        ProgressView().tint(.white).scaleEffect(0.8)
                    } else {
                        Image(systemName: icon)
                            .font(.system(size: size * 0.38, weight: .bold))
                            .foregroundColor(.white)
                    }
                }
                Text(label)
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundColor(.secondary)
            }
        }
        .disabled(isLoading)
        .accessibilityLabel(label)
    }
}

// MARK: - InterestsFlowView (chips com wrap)

struct InterestsFlowView: View {
    let interests: [String]

    var body: some View {
        GeometryReader { geo in
            self.flowLayout(availableWidth: geo.size.width)
        }
        .frame(minHeight: 30)
    }

    private func flowLayout(availableWidth: CGFloat) -> some View {
        var lines: [[String]] = [[]]
        var currentLineWidth: CGFloat = 0
        let spacing: CGFloat = 8
        let chipPadding: CGFloat = 20 // horizontal padding de cada chip

        for interest in interests {
            let chipWidth = estimateChipWidth(text: interest, padding: chipPadding)
            if currentLineWidth + chipWidth + spacing > availableWidth && !lines[lines.count - 1].isEmpty {
                lines.append([interest])
                currentLineWidth = chipWidth
            } else {
                lines[lines.count - 1].append(interest)
                currentLineWidth += chipWidth + spacing
            }
        }

        return VStack(alignment: .leading, spacing: spacing) {
            ForEach(lines.indices, id: \.self) { lineIdx in
                HStack(spacing: spacing) {
                    ForEach(lines[lineIdx], id: \.self) { interest in
                        InterestChip(text: interest)
                    }
                }
            }
        }
    }

    private func estimateChipWidth(text: String, padding: CGFloat) -> CGFloat {
        let font = UIFont.systemFont(ofSize: 12, weight: .medium)
        let attrs = [NSAttributedString.Key.font: font]
        let size = (text as NSString).size(withAttributes: attrs)
        return size.width + padding + 8 // 8 = extra buffer
    }
}

private struct InterestChip: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.system(size: 12, weight: .medium))
            .foregroundStyle(LinearGradient.fypGradient)
            .padding(.horizontal, 10)
            .padding(.vertical, 5)
            .background(Color.fypPink.opacity(0.08))
            .overlay(
                Capsule().stroke(LinearGradient.fypGradient, lineWidth: 1)
            )
            .clipShape(Capsule())
    }
}

// MARK: - View extension para flexibleWidth

private extension View {
    func flexibleWidth() -> some View {
        self.frame(maxWidth: .infinity, alignment: .leading)
    }
}
