// SettingsView.swift — FypMatch iOS

import SwiftUI

struct SettingsView: View {
    @StateObject private var service = FirebaseService.shared
    @Environment(\.dismiss) private var dismiss
    @State private var showDeleteConfirm = false
    @State private var showLogoutConfirm = false
    @AppStorage("push_enabled") private var pushEnabled = true
    @AppStorage("show_online") private var showOnline = true
    @AppStorage("show_distance") private var showDistance = true

    var body: some View {
        NavigationStack {
            Form {
                // Account
                Section("Conta") {
                    if let user = service.currentUser {
                        HStack {
                            UserAvatar(url: user.primaryPhotoURL, size: 44)
                            VStack(alignment: .leading, spacing: 2) {
                                Text(user.displayName).font(.headline)
                                Text(user.email).font(.caption).foregroundColor(.secondary)
                            }
                            Spacer()
                            if user.isActivePremium { PremiumBadge(tier: .premium) }
                        }
                    }
                    NavigationLink("Ver planos Premium") { PremiumView() }
                        .foregroundColor(.fypPink)
                }

                // Notifications
                Section("Notificações") {
                    Toggle("Push notifications", isOn: $pushEnabled)
                        .tint(.fypPink)
                    Toggle("Notificar novos matches", isOn: .constant(true)).tint(.fypPink)
                    Toggle("Notificar mensagens", isOn: .constant(true)).tint(.fypPink)
                }

                // Privacy
                Section("Privacidade") {
                    Toggle("Mostrar status online", isOn: $showOnline).tint(.fypPink)
                    Toggle("Mostrar distância no perfil", isOn: $showDistance).tint(.fypPink)
                    NavigationLink("Bloquear / Denunciar") { Text("Usuários bloqueados") }
                }

                // Support
                Section("Suporte") {
                    Link("Central de ajuda", destination: URL(string: "https://fypmatch.com/help")!)
                    Link("Política de Privacidade", destination: URL(string: "https://fypmatch.com/privacy")!)
                    Link("Termos de Uso", destination: URL(string: "https://fypmatch.com/terms")!)
                    Button("Reportar um problema") {}
                        .foregroundColor(.fypPink)
                }

                // Account actions
                Section {
                    Button("Sair da conta") { showLogoutConfirm = true }
                        .foregroundColor(.orange)
                    Button("Excluir conta") { showDeleteConfirm = true }
                        .foregroundColor(.red)
                }
            }
            .navigationTitle("Configurações")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Fechar") { dismiss() }.foregroundColor(.fypPink)
                }
            }
        }
        .confirmationDialog("Sair da conta?", isPresented: $showLogoutConfirm) {
            Button("Sair", role: .destructive) {
                Task { try? service.signOut() }
            }
            Button("Cancelar", role: .cancel) {}
        }
        .confirmationDialog("Excluir conta permanentemente?", isPresented: $showDeleteConfirm) {
            Button("Excluir", role: .destructive) {}
            Button("Cancelar", role: .cancel) {}
        } message: {
            Text("Essa ação é irreversível. Todos os seus dados serão removidos.")
        }
    }
}
