// DesignSystem.swift — FypMatch iOS
// Tokens, componentes reutilizáveis e extensões visuais

import SwiftUI

// MARK: - Color Tokens

extension Color {
    static let fypPink     = Color(hex: "E91E63")
    static let fypPurple   = Color(hex: "9C27B0")
    static let fypPinkDark = Color(hex: "AD1457")
    static let fypSurface  = Color(UIColor.systemBackground)

    init(hex: String) {
        let h = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: h).scanHexInt64(&int)
        self.init(
            red:   Double((int >> 16) & 0xFF) / 255,
            green: Double((int >> 8)  & 0xFF) / 255,
            blue:  Double( int        & 0xFF) / 255
        )
    }
}

// MARK: - Gradient

extension LinearGradient {
    static let fypGradient = LinearGradient(
        colors: [.fypPink, .fypPurple],
        startPoint: .leading, endPoint: .trailing
    )
    static let fypGradientVertical = LinearGradient(
        colors: [.fypPink, .fypPurple],
        startPoint: .top, endPoint: .bottom
    )
    static let cardOverlay = LinearGradient(
        colors: [.clear, .black.opacity(0.72)],
        startPoint: .top, endPoint: .bottom
    )
}

// MARK: - GradientButton

struct GradientButton: View {
    let title: String
    var icon: String? = nil
    var isFullWidth: Bool = true
    var isLoading: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                if isLoading {
                    ProgressView().tint(.white).scaleEffect(0.85)
                } else {
                    if let icon { Image(systemName: icon).font(.system(size: 15, weight: .medium)) }
                    Text(title).font(.system(size: 15, weight: .semibold))
                }
            }
            .foregroundColor(.white)
            .frame(maxWidth: isFullWidth ? .infinity : nil)
            .padding(.horizontal, 24)
            .padding(.vertical, 15)
            .background(isLoading ? AnyShapeStyle(Color.fypPink.opacity(0.7)) : AnyShapeStyle(LinearGradient.fypGradient))
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .shadow(color: .fypPink.opacity(0.35), radius: 8, y: 4)
        }
        .disabled(isLoading)
        .accessibilityLabel(title)
    }
}

// MARK: - OutlineButton

struct OutlineButton: View {
    let title: String
    var icon: String? = nil
    var isFullWidth: Bool = true
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                if let icon { Image(systemName: icon).font(.system(size: 15, weight: .medium)) }
                Text(title).font(.system(size: 15, weight: .semibold))
            }
            .foregroundStyle(LinearGradient.fypGradient)
            .frame(maxWidth: isFullWidth ? .infinity : nil)
            .padding(.horizontal, 24)
            .padding(.vertical, 14)
            .overlay(RoundedRectangle(cornerRadius: 14).stroke(LinearGradient.fypGradient, lineWidth: 1.5))
        }
        .accessibilityLabel(title)
    }
}

// MARK: - PremiumBadge

struct PremiumBadge: View {
    enum Tier { case premium, vip }
    let tier: Tier

    var body: some View {
        HStack(spacing: 4) {
            Image(systemName: tier == .vip ? "crown.fill" : "sparkles")
                .font(.system(size: 9))
            Text(tier == .vip ? "VIP" : "Premium")
                .font(.system(size: 10, weight: .bold))
        }
        .foregroundColor(.white)
        .padding(.horizontal, 9).padding(.vertical, 4)
        .background(tier == .vip
            ? LinearGradient(colors: [Color(hex: "FFD700"), Color(hex: "FF8C00")], startPoint: .leading, endPoint: .trailing)
            : LinearGradient.fypGradient
        )
        .clipShape(Capsule())
        .accessibilityLabel(tier == .vip ? "Usuário VIP" : "Usuário Premium")
    }
}

// MARK: - CompatibilityScoreBadge

struct CompatibilityBadge: View {
    let score: Int
    var body: some View {
        HStack(spacing: 4) {
            Image(systemName: "heart.fill").font(.system(size: 10)).foregroundColor(.fypPink)
            Text("\(score)% compatível").font(.system(size: 11, weight: .medium)).foregroundColor(.white)
        }
        .padding(.horizontal, 10).padding(.vertical, 4)
        .background(.ultraThinMaterial)
        .clipShape(Capsule())
    }
}

// MARK: - Avatar

struct UserAvatar: View {
    let url: String
    let size: CGFloat
    var showOnline: Bool = false

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            AsyncImage(url: URL(string: url)) { img in
                img.resizable().scaledToFill()
            } placeholder: {
                LinearGradient.fypGradient
                    .overlay(Image(systemName: "person.fill").font(.system(size: size * 0.4)).foregroundColor(.white.opacity(0.7)))
            }
            .frame(width: size, height: size)
            .clipShape(Circle())

            if showOnline {
                Circle().fill(Color.green).frame(width: size * 0.22, height: size * 0.22)
                    .overlay(Circle().stroke(Color.white, lineWidth: 2))
            }
        }
    }
}

// MARK: - FypTextField

struct FypTextField: View {
    let placeholder: String
    @Binding var text: String
    var isSecure: Bool = false
    var error: String? = nil
    var icon: String? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 10) {
                if let icon { Image(systemName: icon).foregroundColor(.fypPink).frame(width: 18) }
                Group {
                    if isSecure { SecureField(placeholder, text: $text) }
                    else { TextField(placeholder, text: $text) }
                }
                .font(.system(size: 15))
            }
            .padding(14)
            .background(Color(.secondarySystemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(error != nil ? Color.red : Color.clear, lineWidth: 1.5)
            )

            if let error {
                Text(error).font(.caption).foregroundColor(.red).padding(.leading, 4)
            }
        }
    }
}

// MARK: - GradientText

struct GradientText: View {
    let text: String
    var font: Font = .title2.bold()

    var body: some View {
        Text(text)
            .font(font)
            .foregroundStyle(LinearGradient.fypGradient)
    }
}

// MARK: - SectionHeader

struct SectionHeader: View {
    let title: String
    var action: (() -> Void)? = nil
    var actionLabel: String = "Ver tudo"

    var body: some View {
        HStack {
            Text(title).font(.headline).foregroundColor(.primary)
            Spacer()
            if let action {
                Button(actionLabel, action: action)
                    .font(.subheadline).foregroundColor(.fypPink)
            }
        }
    }
}
