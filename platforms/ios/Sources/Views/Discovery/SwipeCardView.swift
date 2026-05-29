// SwipeCardView.swift — FypMatch iOS
// Card de swipe com DragGesture — design system aplicado

import SwiftUI

struct SwipeCardView: View {
    let user: User
    let onSwipe: (SwipeDirection) -> Void

    @State private var offset: CGSize = .zero
    @State private var rotation: Double = 0
    @State private var currentPhotoIndex = 0

    private let swipeThreshold: CGFloat = 100
    private let cardWidth = UIScreen.main.bounds.width - 32

    var body: some View {
        ZStack(alignment: .bottom) {
            // Photos
            ZStack {
                AsyncImage(url: URL(string: user.photos.indices.contains(currentPhotoIndex)
                    ? user.photos[currentPhotoIndex] : user.primaryPhotoURL)) { img in
                    img.resizable().scaledToFill()
                } placeholder: {
                    LinearGradient.fypGradient
                }
                .frame(width: cardWidth, height: 500)
                .clipped()

                // Photo navigation tap zones
                HStack(spacing: 0) {
                    Color.clear.contentShape(Rectangle())
                        .onTapGesture { prevPhoto() }
                    Color.clear.contentShape(Rectangle())
                        .onTapGesture { nextPhoto() }
                }

                // Photo dots
                if user.photos.count > 1 {
                    VStack {
                        HStack(spacing: 4) {
                            ForEach(user.photos.indices, id: \.self) { i in
                                Capsule()
                                    .fill(i == currentPhotoIndex ? Color.white : Color.white.opacity(0.5))
                                    .frame(width: i == currentPhotoIndex ? 20 : 6, height: 4)
                                    .animation(.spring(), value: currentPhotoIndex)
                            }
                        }
                        .padding(.top, 12)
                        Spacer()
                    }
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: 20))

            // Info overlay
            VStack(alignment: .leading, spacing: 6) {
                HStack(alignment: .firstTextBaseline, spacing: 8) {
                    Text("\(user.displayName), \(user.age)")
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(.white)
                    if user.isVerified {
                        Image(systemName: "checkmark.seal.fill")
                            .foregroundColor(.fypPink).font(.system(size: 16))
                    }
                    if user.isActivePremium { PremiumBadge(tier: .premium) }
                }
                if let city = user.city {
                    HStack(spacing: 4) {
                        Image(systemName: "location.fill").font(.caption).foregroundColor(.white.opacity(0.8))
                        Text(city).font(.subheadline).foregroundColor(.white.opacity(0.9))
                    }
                }
                if !user.bio.isEmpty {
                    Text(user.bio).font(.subheadline).foregroundColor(.white.opacity(0.85)).lineLimit(2)
                }
                HStack(spacing: 8) {
                    CompatibilityBadge(score: Int(user.compatibilityScore))
                    ForEach(user.interests.prefix(2), id: \.self) { interest in
                        Text(interest).font(.caption).foregroundColor(.white)
                            .padding(.horizontal, 8).padding(.vertical, 3)
                            .background(.ultraThinMaterial).clipShape(Capsule())
                    }
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(LinearGradient.cardOverlay)
            .clipShape(UnevenRoundedRectangle(
                topLeadingRadius: 0, bottomLeadingRadius: 20,
                bottomTrailingRadius: 20, topTrailingRadius: 0
            ))

            // Swipe indicators
            HStack {
                LikeIndicator().opacity(offset.width > 30 ? min(Double(offset.width / 80), 1) : 0)
                Spacer()
                PassIndicator().opacity(offset.width < -30 ? min(Double(-offset.width / 80), 1) : 0)
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 200)
        }
        .frame(width: cardWidth, height: 500)
        .shadow(color: shadowColor, radius: abs(offset.width) > 20 ? 20 : 10, y: 8)
        .offset(offset)
        .rotationEffect(.degrees(rotation))
        .gesture(dragGesture)
        .animation(.interactiveSpring(response: 0.35, dampingFraction: 0.7), value: offset)
    }

    // MARK: - Gestures

    private var dragGesture: some Gesture {
        DragGesture()
            .onChanged { value in
                offset = value.translation
                rotation = Double(value.translation.width / 22)
            }
            .onEnded { value in
                let width = value.translation.width
                if width > swipeThreshold {
                    flyOut(direction: .right)
                } else if width < -swipeThreshold {
                    flyOut(direction: .left)
                } else {
                    withAnimation(.spring()) { offset = .zero; rotation = 0 }
                }
            }
    }

    private func flyOut(direction: SwipeDirection) {
        let xOffset: CGFloat = direction == .right ? 600 : -600
        withAnimation(.easeIn(duration: 0.25)) {
            offset = CGSize(width: xOffset, height: 0)
            rotation = direction == .right ? 18 : -18
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
            onSwipe(direction)
        }
    }

    private var shadowColor: Color {
        if offset.width > 30 { return .fypPink.opacity(0.4) }
        if offset.width < -30 { return .gray.opacity(0.3) }
        return .black.opacity(0.15)
    }

    private func nextPhoto() {
        guard currentPhotoIndex < user.photos.count - 1 else { return }
        withAnimation { currentPhotoIndex += 1 }
    }
    private func prevPhoto() {
        guard currentPhotoIndex > 0 else { return }
        withAnimation { currentPhotoIndex -= 1 }
    }
}

// MARK: - Indicators

private struct LikeIndicator: View {
    var body: some View {
        Text("CURTIR")
            .font(.system(size: 28, weight: .black))
            .foregroundColor(.green)
            .padding(.horizontal, 12).padding(.vertical, 6)
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.green, lineWidth: 3))
            .rotationEffect(.degrees(-20))
    }
}

private struct PassIndicator: View {
    var body: some View {
        Text("PASSAR")
            .font(.system(size: 28, weight: .black))
            .foregroundColor(.red)
            .padding(.horizontal, 12).padding(.vertical, 6)
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.red, lineWidth: 3))
            .rotationEffect(.degrees(20))
    }
}

enum SwipeDirection { case left, right }
