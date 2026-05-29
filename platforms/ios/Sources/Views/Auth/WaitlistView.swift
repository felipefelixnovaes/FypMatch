// WaitlistView.swift — FypMatch iOS
// Tela de fila de espera para usuários em regiões com baixa densidade

import SwiftUI

struct WaitlistView: View {
    let email: String
    let position: Int

    @State private var animatePulse = false
    @State private var showShareSheet = false
    @State private var referralCode = ""
    @State private var referralCount = 0

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color(hex: "#E91E63"), Color(hex: "#9C27B0")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 32) {

                    // Header
                    VStack(spacing: 16) {
                        ZStack {
                            Circle()
                                .fill(Color.white.opacity(0.2))
                                .frame(width: 120, height: 120)
                                .scaleEffect(animatePulse ? 1.1 : 1.0)
                                .animation(.easeInOut(duration: 1.5).repeatForever(), value: animatePulse)

                            Text("💕")
                                .font(.system(size: 60))
                        }

                        Text("Você está na lista!")
                            .font(.largeTitle).bold()
                            .foregroundColor(.white)

                        Text("FypMatch está chegando à sua cidade")
                            .font(.title3)
                            .foregroundColor(.white.opacity(0.85))
                            .multilineTextAlignment(.center)
                    }
                    .padding(.top, 48)

                    // Posição na fila
                    VStack(spacing: 8) {
                        Text("Sua posição na fila")
                            .font(.subheadline)
                            .foregroundColor(.white.opacity(0.8))

                        Text("#\(position)")
                            .font(.system(size: 64, weight: .black))
                            .foregroundColor(.white)

                        Text("de \(position + Int.random(in: 1200...2400)) pessoas esperando")
                            .font(.caption)
                            .foregroundColor(.white.opacity(0.7))
                    }
                    .padding(24)
                    .background(Color.white.opacity(0.15))
                    .cornerRadius(20)
                    .padding(.horizontal, 24)

                    // Referral — pular fila
                    VStack(spacing: 16) {
                        Text("🚀 Pule a fila!")
                            .font(.title2).bold()
                            .foregroundColor(.white)

                        Text("Indique amigos e ganhe 100 posições por pessoa que se cadastrar")
                            .font(.body)
                            .foregroundColor(.white.opacity(0.85))
                            .multilineTextAlignment(.center)

                        // Código de referência
                        VStack(spacing: 8) {
                            Text("Seu código")
                                .font(.caption)
                                .foregroundColor(.white.opacity(0.7))

                            HStack {
                                Text(referralCode.isEmpty ? "FYPM-\(email.prefix(4).uppercased())" : referralCode)
                                    .font(.title3).bold()
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 20)
                                    .padding(.vertical, 12)
                                    .background(Color.white.opacity(0.2))
                                    .cornerRadius(12)

                                Button(action: copyCode) {
                                    Image(systemName: "doc.on.doc.fill")
                                        .foregroundColor(.white)
                                        .padding(12)
                                        .background(Color.white.opacity(0.2))
                                        .cornerRadius(12)
                                }
                            }
                        }

                        // Indicados
                        HStack(spacing: 20) {
                            VStack {
                                Text("\(referralCount)")
                                    .font(.title2).bold()
                                    .foregroundColor(.white)
                                Text("indicados")
                                    .font(.caption)
                                    .foregroundColor(.white.opacity(0.7))
                            }

                            Rectangle()
                                .fill(Color.white.opacity(0.3))
                                .frame(width: 1, height: 40)

                            VStack {
                                Text("\(referralCount * 100)")
                                    .font(.title2).bold()
                                    .foregroundColor(.white)
                                Text("posições puladas")
                                    .font(.caption)
                                    .foregroundColor(.white.opacity(0.7))
                            }
                        }
                        .padding(16)
                        .background(Color.white.opacity(0.1))
                        .cornerRadius(16)

                        // Botão compartilhar
                        Button(action: { showShareSheet = true }) {
                            HStack(spacing: 8) {
                                Image(systemName: "square.and.arrow.up")
                                Text("Convidar amigos")
                            }
                            .font(.headline)
                            .foregroundColor(Color(hex: "#E91E63"))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(Color.white)
                            .cornerRadius(16)
                        }
                    }
                    .padding(24)
                    .background(Color.white.opacity(0.1))
                    .cornerRadius(24)
                    .padding(.horizontal, 16)

                    // Perks ao entrar
                    VStack(alignment: .leading, spacing: 16) {
                        Text("🎁 Bônus por estar na lista")
                            .font(.title3).bold()
                            .foregroundColor(.white)

                        ForEach(perks, id: \.title) { perk in
                            HStack(spacing: 12) {
                                Text(perk.icon)
                                    .font(.title2)
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(perk.title)
                                        .font(.subheadline).bold()
                                        .foregroundColor(.white)
                                    Text(perk.description)
                                        .font(.caption)
                                        .foregroundColor(.white.opacity(0.75))
                                }
                            }
                        }
                    }
                    .padding(24)
                    .background(Color.white.opacity(0.1))
                    .cornerRadius(24)
                    .padding(.horizontal, 16)

                    // Email confirmação
                    Text("Você receberá um aviso em\n**\(email)**\nquando sua vez chegar")
                        .font(.footnote)
                        .foregroundColor(.white.opacity(0.7))
                        .multilineTextAlignment(.center)
                        .padding(.bottom, 40)
                }
            }
        }
        .onAppear { animatePulse = true }
        .sheet(isPresented: $showShareSheet) {
            ShareSheet(items: [shareText])
        }
    }

    // MARK: - Helpers

    private var shareText: String {
        let code = referralCode.isEmpty ? "FYPM-\(email.prefix(4).uppercased())" : referralCode
        return """
        🔥 Entrei na lista do FypMatch, o app de relacionamentos que usa IA pra conectar pessoas de verdade!

        Use meu código \(code) pra pular a fila:
        https://fypmatch.com/waitlist?ref=\(code)
        """
    }

    private func copyCode() {
        let code = referralCode.isEmpty ? "FYPM-\(email.prefix(4).uppercased())" : referralCode
        UIPasteboard.general.string = code
        // Feedback haptico
        let generator = UINotificationFeedbackGenerator()
        generator.notificationOccurred(.success)
    }

    private var perks: [(icon: String, title: String, description: String)] {
        [
            ("⭐️", "7 dias Premium grátis", "Ao entrar no app, você ganha 1 semana Premium"),
            ("💎", "50 Super Likes", "Para se destacar nos seus primeiros matches"),
            ("🤖", "100 créditos de IA", "Para o Conselheiro IA te ajudar nos matches"),
            ("🔒", "Perfil verificado", "Badge de verificado antecipado")
        ]
    }
}

// MARK: - ShareSheet (UIKit wrapper)

struct ShareSheet: UIViewControllerRepresentable {
    let items: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}

// MARK: - Preview

#Preview {
    WaitlistView(email: "joao@email.com", position: 347)
}
