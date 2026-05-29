// AdsView.swift — FypMatch iOS
// Tela para ganhar créditos IA assistindo anúncios

import SwiftUI
import ComposableArchitecture

struct AdsView: View {
    let userId: String
    @Environment(\.dismiss) private var dismiss

    @State private var info: AdsCreditsInfo = AdsCreditsInfo(
        adsWatchedToday: 0, maxAdsPerDay: 3,
        creditsEarnedToday: 0, creditsPerAd: 3
    )
    @State private var isWatching = false
    @State private var showSuccess = false
    @State private var errorMessage: String?

    @Dependency(\.adsService) var adsService

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 28) {
                    // Header
                    VStack(spacing: 12) {
                        Text("🎬")
                            .font(.system(size: 64))
                            .padding(.top, 24)

                        GradientText(text: "Ganhar Créditos IA")
                            .font(.title2.bold())

                        Text("Assista a um anúncio curto e ganhe 3 créditos\npara usar com seu conselheiro IA")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                    }

                    // Progresso do dia
                    VStack(spacing: 12) {
                        HStack {
                            Text("Anúncios hoje")
                                .font(.subheadline).foregroundColor(.secondary)
                            Spacer()
                            Text("\(info.adsWatchedToday)/\(info.maxAdsPerDay)")
                                .font(.subheadline.bold())
                                .foregroundColor(.fypPink)
                        }

                        ProgressView(value: Double(info.adsWatchedToday),
                                     total: Double(info.maxAdsPerDay))
                            .tint(.fypPink)
                            .scaleEffect(x: 1, y: 2.5, anchor: .center)

                        HStack {
                            Label("\(info.creditsEarnedToday) créditos ganhos hoje",
                                  systemImage: "brain.head.profile")
                                .font(.caption).foregroundColor(.fypPurple)
                            Spacer()
                        }
                    }
                    .padding(16)
                    .background(Color(.secondarySystemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                    // Estado de carregamento / sucesso / botão
                    if isWatching {
                        WatchingAdView()
                    } else if showSuccess {
                        SuccessCreditsView(credits: info.creditsPerAd) {
                            showSuccess = false
                        }
                    } else {
                        VStack(spacing: 16) {
                            GradientButton(
                                title: info.canWatchMore
                                    ? "Assistir Anúncio (+\(info.creditsPerAd) créditos)"
                                    : "Limite diário atingido ✓",
                                isLoading: false
                            ) {
                                Task { await watchAd() }
                            }
                            .disabled(!info.canWatchMore)
                            .opacity(info.canWatchMore ? 1.0 : 0.5)

                            if !info.canWatchMore {
                                Label("Volte amanhã para mais créditos",
                                      systemImage: "clock.fill")
                                    .font(.caption).foregroundColor(.secondary)
                            }
                        }
                    }

                    // Mensagem de erro
                    if let error = errorMessage {
                        HStack {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .foregroundColor(.orange)
                            Text(error).font(.subheadline).foregroundColor(.orange)
                        }
                        .padding(12)
                        .background(Color.orange.opacity(0.1))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                    }

                    Divider()

                    // Nota sobre planos
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Créditos por plano").font(.caption.bold()).foregroundColor(.secondary)
                        HStack(spacing: 0) {
                            PlanCreditsRow(plan: "Gratuito", credits: "Via anúncios",
                                           icon: "play.rectangle", color: .gray)
                            PlanCreditsRow(plan: "Premium", credits: "10/dia",
                                           icon: "crown", color: .fypPink)
                            PlanCreditsRow(plan: "VIP", credits: "25/dia",
                                           icon: "crown.fill", color: Color(hex: "FFD700"))
                        }
                    }
                    .padding(16)
                    .background(Color(.secondarySystemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .padding(.bottom, 32)
                }
                .padding(.horizontal, 20)
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Fechar") { dismiss() }.foregroundColor(.fypPink)
                }
            }
        }
        .onAppear {
            info = adsService.getCreditsInfo(userId)
        }
    }

    private func watchAd() async {
        isWatching = true
        errorMessage = nil
        do {
            _ = try await adsService.watchAd(userId)
            info = adsService.getCreditsInfo(userId)
            isWatching = false
            showSuccess = true
        } catch {
            isWatching = false
            errorMessage = error.localizedDescription
        }
    }
}

// MARK: - WatchingAdView

struct WatchingAdView: View {
    @State private var progress: Double = 0.0
    @State private var timer: Timer?

    var body: some View {
        VStack(spacing: 16) {
            ZStack {
                Circle()
                    .stroke(Color.secondary.opacity(0.2), lineWidth: 6)
                    .frame(width: 80, height: 80)
                Circle()
                    .trim(from: 0, to: progress)
                    .stroke(LinearGradient.fypGradient, style: StrokeStyle(lineWidth: 6, lineCap: .round))
                    .frame(width: 80, height: 80)
                    .rotationEffect(.degrees(-90))
                    .animation(.linear(duration: 0.1), value: progress)
                Text("\(Int((1 - progress) * 5) + 1)s")
                    .font(.headline.bold())
            }
            Text("Carregando anúncio...")
                .font(.subheadline).foregroundColor(.secondary)
            Text("Não feche esta tela")
                .font(.caption).foregroundColor(.secondary.opacity(0.6))
        }
        .padding(24)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .onAppear {
            progress = 0.0
            timer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { t in
                progress = min(1.0, progress + 0.02)
                if progress >= 1.0 { t.invalidate() }
            }
        }
        .onDisappear { timer?.invalidate() }
    }
}

// MARK: - SuccessCreditsView

struct SuccessCreditsView: View {
    let credits: Int
    let onDismiss: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 48))
                .foregroundColor(.green)
            Text("+\(credits) créditos adicionados!")
                .font(.title3.bold())
            Text("Use com seu conselheiro IA")
                .font(.subheadline).foregroundColor(.secondary)
            Button("Continuar") { onDismiss() }
                .font(.subheadline.bold())
                .foregroundColor(.fypPink)
        }
        .padding(24)
        .background(Color.green.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color.green.opacity(0.3), lineWidth: 1))
    }
}

// MARK: - PlanCreditsRow

struct PlanCreditsRow: View {
    let plan: String
    let credits: String
    let icon: String
    let color: Color

    var body: some View {
        VStack(spacing: 6) {
            Image(systemName: icon).font(.system(size: 18)).foregroundColor(color)
            Text(plan).font(.caption.bold())
            Text(credits).font(.caption2).foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
    }
}
