// ProfileView.swift — FypMatch iOS
// Port de ProfileScreen.kt + ProfileEditScreen.kt

import SwiftUI

struct ProfileView: View {
    @StateObject private var service = FirebaseService.shared
    @State private var showEdit = false
    @State private var showSettings = false

    var body: some View {
        NavigationStack {
            ScrollView {
                if let user = service.currentUser {
                    VStack(spacing: 0) {
                        // Header
                        profileHeader(user: user)

                        // Stats
                        statsRow(user: user)
                            .padding(.horizontal, 16).padding(.vertical, 12)

                        Divider()

                        // Bio
                        if !user.bio.isEmpty {
                            profileSection(title: "Sobre mim") {
                                Text(user.bio).font(.subheadline).foregroundColor(.primary)
                            }
                        }

                        // Interests
                        if !user.interests.isEmpty {
                            profileSection(title: "Interesses") {
                                FlowLayout(spacing: 8) {
                                    ForEach(user.interests, id: \.self) { interest in
                                        InterestChip(text: interest)
                                    }
                                }
                            }
                        }

                        // Details
                        profileSection(title: "Detalhes") {
                            VStack(spacing: 10) {
                                if let city = user.city { DetailRow(icon: "location.fill", text: city) }
                                if let occ = user.occupation { DetailRow(icon: "briefcase.fill", text: occ) }
                                if let edu = user.education { DetailRow(icon: "graduationcap.fill", text: edu) }
                                DetailRow(icon: "person.fill", text: user.gender.displayName)
                                DetailRow(icon: "heart.fill", text: user.genderInterest.displayName)
                            }
                        }

                        // Subscription
                        subscriptionCard(user: user)
                            .padding(.horizontal, 16).padding(.bottom, 32)
                    }
                } else {
                    ProgressView().tint(.fypPink).padding(.top, 100)
                }
            }
            .navigationTitle("Perfil")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { showEdit = true } label: {
                        Image(systemName: "pencil.circle.fill")
                            .font(.title3).foregroundStyle(LinearGradient.fypGradient)
                    }
                }
                ToolbarItem(placement: .topBarLeading) {
                    Button { showSettings = true } label: {
                        Image(systemName: "gearshape").foregroundColor(.fypPink)
                    }
                }
            }
        }
        .sheet(isPresented: $showEdit) {
            if let user = service.currentUser {
                ProfileEditView(user: user)
            }
        }
        .sheet(isPresented: $showSettings) {
            SettingsView()
        }
    }

    private func profileHeader(user: User) -> some View {
        ZStack(alignment: .bottom) {
            // Cover gradient
            LinearGradient.fypGradient.frame(height: 180)

            VStack(spacing: 10) {
                // Avatar
                ZStack(alignment: .bottomTrailing) {
                    AsyncImage(url: URL(string: user.primaryPhotoURL)) { img in
                        img.resizable().scaledToFill()
                    } placeholder: { LinearGradient.fypGradient }
                    .frame(width: 100, height: 100).clipShape(Circle())
                    .overlay(Circle().stroke(.white, lineWidth: 3))

                    if user.isVerified {
                        Image(systemName: "checkmark.seal.fill")
                            .font(.system(size: 22)).foregroundColor(.fypPink)
                            .background(Circle().fill(.white).frame(width: 24, height: 24))
                    }
                }
                .offset(y: 50)

                if user.isActivePremium {
                    PremiumBadge(tier: .premium).offset(y: 50)
                }
            }
            .padding(.bottom, 8)
        }
        .frame(height: 180)
        .padding(.bottom, 60)
        .overlay(alignment: .bottomLeading) {
            VStack(alignment: .leading, spacing: 2) {
                Text("\(user.displayName), \(user.age)").font(.title2.bold())
                if let city = user.city {
                    HStack(spacing: 4) {
                        Image(systemName: "location.fill").font(.caption).foregroundColor(.secondary)
                        Text(city).font(.subheadline).foregroundColor(.secondary)
                    }
                }
            }
            .padding(.horizontal, 16).padding(.bottom, 4)
        }
    }

    private func statsRow(user: User) -> some View {
        HStack {
            StatCell(value: "\(user.matchCount)", label: "Matches")
            Divider().frame(height: 40)
            StatCell(value: "\(user.likes)", label: "Curtidas")
            Divider().frame(height: 40)
            StatCell(value: "\(user.profileViews)", label: "Visualizações")
        }
        .padding(12)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }

    private func profileSection<Content: View>(title: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(title).font(.headline).padding(.horizontal, 16).padding(.top, 16)
            content().padding(.horizontal, 16).padding(.bottom, 4)
        }
    }

    private func subscriptionCard(user: User) -> some View {
        VStack(spacing: 12) {
            HStack {
                Image(systemName: user.isActivePremium ? "crown.fill" : "sparkles")
                    .foregroundStyle(LinearGradient.fypGradient)
                Text(user.isActivePremium ? "Plano Premium" : "Plano Gratuito")
                    .font(.headline)
                Spacer()
                if !user.isActivePremium {
                    Text("Upgrade").font(.subheadline.bold()).foregroundColor(.fypPink)
                }
            }
            if !user.isActivePremium {
                Text("Desbloqueie matches ilimitados, veja quem curtiu você e tenha acesso ao conselheiro IA todos os dias.")
                    .font(.caption).foregroundColor(.secondary)
                GradientButton(title: "Ver planos Premium") {}
            }
        }
        .padding(16)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

// MARK: - Supporting Views

struct StatCell: View {
    let value: String; let label: String
    var body: some View {
        VStack(spacing: 2) {
            Text(value).font(.title3.bold()).foregroundStyle(LinearGradient.fypGradient)
            Text(label).font(.caption).foregroundColor(.secondary)
        }.frame(maxWidth: .infinity)
    }
}

struct DetailRow: View {
    let icon: String; let text: String
    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: icon).foregroundColor(.fypPink).frame(width: 20)
            Text(text).font(.subheadline)
            Spacer()
        }
    }
}

struct InterestChip: View {
    let text: String
    var body: some View {
        Text(text).font(.subheadline)
            .padding(.horizontal, 12).padding(.vertical, 6)
            .background(Color.fypPink.opacity(0.1))
            .foregroundColor(.fypPink)
            .clipShape(Capsule())
    }
}

struct FlowLayout: Layout {
    var spacing: CGFloat = 8
    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let rows = computeRows(proposal: proposal, subviews: subviews)
        let height = rows.map { $0.map { $0.sizeThatFits(.unspecified).height }.max() ?? 0 }.reduce(0, +) + CGFloat(max(rows.count - 1, 0)) * spacing
        return CGSize(width: proposal.width ?? 0, height: height)
    }
    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var y = bounds.minY
        for row in computeRows(proposal: proposal, subviews: subviews) {
            var x = bounds.minX
            let rowHeight = row.map { $0.sizeThatFits(.unspecified).height }.max() ?? 0
            for view in row {
                let size = view.sizeThatFits(.unspecified)
                view.place(at: CGPoint(x: x, y: y), proposal: .unspecified)
                x += size.width + spacing
            }
            y += rowHeight + spacing
        }
    }
    private func computeRows(proposal: ProposedViewSize, subviews: Subviews) -> [[LayoutSubview]] {
        var rows: [[LayoutSubview]] = [[]]
        var x: CGFloat = 0
        let maxW = proposal.width ?? .infinity
        for view in subviews {
            let w = view.sizeThatFits(.unspecified).width
            if x + w > maxW && !rows[rows.count - 1].isEmpty { rows.append([]); x = 0 }
            rows[rows.count - 1].append(view)
            x += w + spacing
        }
        return rows
    }
}
