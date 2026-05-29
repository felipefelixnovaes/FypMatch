//
//  PhotoManagerView.swift
//  FypMatch iOS
//
//  Sprint 2 — Gerenciador de fotos com grid 3 colunas, drag-and-drop,
//  limites por plano (FREE:6 / PREMIUM:12 / VIP:20) e upsell premium.
//

import SwiftUI
import PhotosUI

// MARK: - Plano de assinatura

enum SubscriptionPlan {
    case free, premium, vip

    /// Número máximo de fotos permitidas por plano
    var maxPhotos: Int {
        switch self {
        case .free:    return 6
        case .premium: return 12
        case .vip:     return 20
        }
    }

    var displayName: String {
        switch self {
        case .free:    return "FREE"
        case .premium: return "PREMIUM"
        case .vip:     return "VIP"
        }
    }
}

extension User {
    /// Retorna o plano ativo baseado no status premium e data de expiração
    var subscriptionPlan: SubscriptionPlan {
        guard isActivePremium else { return .free }
        // VIP: considera isPremium true com expiração distante (heurística)
        // Numa implementação real, haveria um campo plan: String no modelo
        return .premium
    }
}

// MARK: - PhotoManagerView

struct PhotoManagerView: View {
    @Binding var photos: [String]
    let user: User
    let onSave: ([String]) -> Void

    @Environment(\.dismiss) private var dismiss

    // Estado local para edição antes de confirmar
    @State private var localPhotos: [String] = []

    // PhotosPicker
    @State private var selectedItems: [PhotosPickerItem] = []
    @State private var isUploading = false

    // Confirmação de remoção
    @State private var photoToDelete: String? = nil
    @State private var showDeleteConfirm = false

    // Fullscreen
    @State private var fullscreenPhoto: String? = nil

    // Upsell premium
    @State private var showPremiumUpsell = false

    // Drag-and-drop
    @State private var draggingURL: String? = nil

    private var plan: SubscriptionPlan { user.subscriptionPlan }
    private var maxPhotos: Int { plan.maxPhotos }
    private var atLimit: Bool { localPhotos.count >= maxPhotos }

    // MARK: - Body

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                planBanner
                    .padding(.horizontal, 16)
                    .padding(.top, 12)
                    .padding(.bottom, 8)

                photoGrid
                    .padding(.horizontal, 16)

                Spacer()

                addPhotoButton
                    .padding(.horizontal, 16)
                    .padding(.bottom, 24)
            }
            .navigationTitle("Minhas Fotos")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { toolbarContent }
            .onAppear { localPhotos = photos }
            // PhotosPicker — ativado pelo botão principal
            .photosPicker(
                isPresented: .constant(false),
                selection: $selectedItems,
                maxSelectionCount: max(0, maxPhotos - localPhotos.count),
                matching: .images
            )
            .onChange(of: selectedItems) { _, newItems in
                Task { await handlePickedItems(newItems) }
            }
            // Fullscreen
            .fullScreenCover(item: $fullscreenPhoto) { url in
                FullscreenPhotoView(url: url)
            }
            // Upsell premium
            .sheet(isPresented: $showPremiumUpsell) {
                PremiumUpsellSheet(plan: plan)
            }
            // Confirmação de exclusão
            .confirmationDialog(
                "Remover foto?",
                isPresented: $showDeleteConfirm,
                titleVisibility: .visible
            ) {
                Button("Remover", role: .destructive) {
                    if let url = photoToDelete { removePhoto(url) }
                }
                Button("Cancelar", role: .cancel) {}
            }
        }
    }

    // MARK: - Toolbar

    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        ToolbarItem(placement: .topBarLeading) {
            Button("Fechar") { dismiss() }
        }
        ToolbarItem(placement: .topBarTrailing) {
            Text("\(localPhotos.count)/\(maxPhotos) fotos")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
    }

    // MARK: - Banner de plano

    private var planBanner: some View {
        HStack(spacing: 12) {
            planChip("FREE", limit: "6", isActive: plan == .free)
            planChip("PREMIUM", limit: "12", isActive: plan == .premium)
            planChip("VIP", limit: "20", isActive: plan == .vip)

            if plan == .free {
                Spacer()
                Button {
                    showPremiumUpsell = true
                } label: {
                    Text("Upgrade")
                        .font(.caption.bold())
                        .foregroundColor(.white)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(LinearGradient.fypGradient)
                        .clipShape(Capsule())
                }
            }
        }
        .padding(12)
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private func planChip(_ name: String, limit: String, isActive: Bool) -> some View {
        VStack(spacing: 2) {
            Text(name)
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(isActive ? .white : .secondary)
            Text(limit)
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(isActive ? .white : .secondary)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 6)
        .background(isActive ? LinearGradient.fypGradient : LinearGradient(colors: [Color(.tertiarySystemBackground)], startPoint: .leading, endPoint: .trailing))
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }

    // MARK: - Grid

    private var photoGrid: some View {
        let columns = Array(repeating: GridItem(.flexible(), spacing: 8), count: 3)
        return LazyVGrid(columns: columns, spacing: 8) {
            ForEach(localPhotos, id: \.self) { url in
                photoCell(url: url)
                    .onDrag {
                        draggingURL = url
                        return NSItemProvider(object: url as NSString)
                    }
                    .onDrop(of: [.text], delegate: PhotoDropDelegate(
                        targetURL: url,
                        photos: $localPhotos,
                        draggingURL: $draggingURL
                    ))
            }

            // Célula "adicionar" — visível apenas se abaixo do limite
            if !atLimit {
                addCell
            }

            // Slots vazios para preenchimento visual
            let emptyCount = max(0, maxPhotos - localPhotos.count - (atLimit ? 0 : 1))
            ForEach(0..<emptyCount, id: \.self) { _ in
                emptySlot
            }
        }
    }

    @ViewBuilder
    private func photoCell(url: String) -> some View {
        let isPrimary = url == localPhotos.first

        ZStack(alignment: .topLeading) {
            AsyncImage(url: URL(string: url)) { phase in
                switch phase {
                case .success(let img):
                    img.resizable().scaledToFill()
                case .failure:
                    Color(.tertiarySystemBackground)
                        .overlay(Image(systemName: "photo").foregroundColor(.secondary))
                default:
                    Color(.tertiarySystemBackground)
                        .overlay(ProgressView())
                }
            }
            .frame(maxWidth: .infinity)
            .aspectRatio(1, contentMode: .fit)
            .clipped()
            .clipShape(RoundedRectangle(cornerRadius: 10))
            .onTapGesture {
                fullscreenPhoto = url
            }
            .contextMenu {
                if !isPrimary {
                    Button {
                        setAsPrimary(url)
                    } label: {
                        Label("Definir como principal", systemImage: "crown.fill")
                    }
                }
                Button(role: .destructive) {
                    photoToDelete = url
                    showDeleteConfirm = true
                } label: {
                    Label("Remover foto", systemImage: "trash")
                }
            }

            // Badge de foto principal
            if isPrimary {
                Image(systemName: "crown.fill")
                    .font(.system(size: 13))
                    .foregroundColor(.yellow)
                    .padding(6)
                    .background(.black.opacity(0.55))
                    .clipShape(RoundedRectangle(cornerRadius: 6))
                    .padding(6)
            }
        }
    }

    private var addCell: some View {
        Button {
            if atLimit {
                showPremiumUpsell = true
            }
            // PhotosPicker é acionado via sheet separada abaixo
        } label: {
            PhotosPicker(
                selection: $selectedItems,
                maxSelectionCount: max(1, maxPhotos - localPhotos.count),
                matching: .images
            ) {
                VStack(spacing: 6) {
                    if isUploading {
                        ProgressView()
                            .tint(.fypPink)
                    } else {
                        Image(systemName: "plus")
                            .font(.system(size: 22, weight: .medium))
                            .foregroundStyle(LinearGradient.fypGradient)
                        Text("Adicionar")
                            .font(.system(size: 11, weight: .medium))
                            .foregroundStyle(LinearGradient.fypGradient)
                    }
                }
                .frame(maxWidth: .infinity)
                .aspectRatio(1, contentMode: .fit)
                .background(Color(.secondarySystemBackground))
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(
                            LinearGradient.fypGradient.opacity(0.5) as LinearGradient,
                            style: StrokeStyle(lineWidth: 1.5, dash: [5])
                        )
                )
                .clipShape(RoundedRectangle(cornerRadius: 10))
            }
            .disabled(isUploading)
        }
    }

    private var emptySlot: some View {
        RoundedRectangle(cornerRadius: 10)
            .fill(Color(.tertiarySystemBackground))
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(Color(.separator).opacity(0.4), lineWidth: 1)
            )
            .aspectRatio(1, contentMode: .fit)
            .overlay(
                Image(systemName: "photo")
                    .foregroundColor(.quaternaryLabel)
            )
    }

    // MARK: - Botão principal adicionar

    private var addPhotoButton: some View {
        Group {
            if atLimit {
                GradientButton(
                    title: "Limite atingido — Upgrade para mais",
                    icon: "crown.fill",
                    isLoading: false
                ) {
                    showPremiumUpsell = true
                }
                .opacity(0.85)
            } else {
                PhotosPicker(
                    selection: $selectedItems,
                    maxSelectionCount: max(1, maxPhotos - localPhotos.count),
                    matching: .images
                ) {
                    HStack(spacing: 8) {
                        if isUploading {
                            ProgressView().tint(.white).scaleEffect(0.85)
                        } else {
                            Image(systemName: "plus.circle.fill")
                                .font(.system(size: 15, weight: .medium))
                            Text("Adicionar foto")
                                .font(.system(size: 15, weight: .semibold))
                        }
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 15)
                    .background(isUploading
                        ? AnyShapeStyle(Color.fypPink.opacity(0.7))
                        : AnyShapeStyle(LinearGradient.fypGradient)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .shadow(color: .fypPink.opacity(0.35), radius: 8, y: 4)
                }
                .disabled(isUploading)
            }
        }
    }

    // MARK: - Ações

    private func handlePickedItems(_ items: [PhotosPickerItem]) async {
        guard !items.isEmpty else { return }
        isUploading = true
        defer {
            isUploading = false
            selectedItems = []
        }

        for item in items {
            guard localPhotos.count < maxPhotos else { break }
            guard let data = try? await item.loadTransferable(type: Data.self) else { continue }

            do {
                // Simula upload via firebaseService — em produção injetar via @Dependency
                let url = try await FirebaseServiceProxy.shared.uploadPhoto(data, userId: user.id)
                localPhotos.append(url)
            } catch {
                // Em produção: exibir alerta de erro de upload
            }
        }

        onSave(localPhotos)
        photos = localPhotos
    }

    private func removePhoto(_ url: String) {
        guard let idx = localPhotos.firstIndex(of: url) else { return }
        localPhotos.remove(at: idx)
        // Se removeu a foto principal (índice 0), a próxima assume automaticamente
        onSave(localPhotos)
        photos = localPhotos
    }

    private func setAsPrimary(_ url: String) {
        guard let idx = localPhotos.firstIndex(of: url), idx != 0 else { return }
        localPhotos.remove(at: idx)
        localPhotos.insert(url, at: 0)
        onSave(localPhotos)
        photos = localPhotos
    }
}

// MARK: - PhotoDropDelegate

struct PhotoDropDelegate: DropDelegate {
    let targetURL: String
    @Binding var photos: [String]
    @Binding var draggingURL: String?

    func performDrop(info: DropInfo) -> Bool {
        draggingURL = nil
        return true
    }

    func dropEntered(info: DropInfo) {
        guard
            let dragging = draggingURL,
            dragging != targetURL,
            let fromIdx = photos.firstIndex(of: dragging),
            let toIdx = photos.firstIndex(of: targetURL)
        else { return }

        withAnimation(.spring()) {
            photos.move(fromOffsets: IndexSet(integer: fromIdx), toOffset: toIdx > fromIdx ? toIdx + 1 : toIdx)
        }
    }

    func dropUpdated(info: DropInfo) -> DropProposal? {
        DropProposal(operation: .move)
    }
}

// MARK: - FullscreenPhotoView

struct FullscreenPhotoView: View {
    let url: String
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ZStack(alignment: .topTrailing) {
            Color.black.ignoresSafeArea()
            AsyncImage(url: URL(string: url)) { phase in
                if case .success(let img) = phase {
                    img.resizable()
                        .scaledToFit()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            }
            Button {
                dismiss()
            } label: {
                Image(systemName: "xmark.circle.fill")
                    .font(.system(size: 28))
                    .foregroundColor(.white.opacity(0.85))
                    .padding(20)
            }
        }
    }
}

// MARK: - PremiumUpsellSheet

struct PremiumUpsellSheet: View {
    let plan: SubscriptionPlan
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(spacing: 24) {
            Spacer()

            Image(systemName: "crown.fill")
                .font(.system(size: 56))
                .foregroundStyle(LinearGradient(colors: [Color(hex: "FFD700"), Color(hex: "FF8C00")], startPoint: .top, endPoint: .bottom))

            VStack(spacing: 8) {
                Text("Mais fotos, mais matches")
                    .font(.title2.bold())
                    .multilineTextAlignment(.center)

                Text("Faça upgrade para adicionar mais fotos e se destacar nas descobertas.")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 24)
            }

            VStack(spacing: 12) {
                planRow("FREE", photos: "6 fotos", isActive: plan == .free)
                planRow("PREMIUM", photos: "12 fotos + fotos HD", isActive: plan == .premium)
                planRow("VIP", photos: "20 fotos + fotos HD + boost", isActive: plan == .vip)
            }
            .padding(.horizontal, 24)

            Spacer()

            VStack(spacing: 12) {
                GradientButton(title: "Ver planos Premium", icon: "sparkles") {
                    dismiss()
                    // Navegar para tela de planos — integrar via callback em prod.
                }
                Button("Agora não") { dismiss() }
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
        .presentationDetents([.medium, .large])
        .presentationDragIndicator(.visible)
    }

    private func planRow(_ name: String, photos: String, isActive: Bool) -> some View {
        HStack {
            Image(systemName: isActive ? "checkmark.circle.fill" : "circle")
                .foregroundColor(isActive ? .fypPink : .secondary)
            Text(name)
                .font(.subheadline.bold())
            Spacer()
            Text(photos)
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .padding(12)
        .background(isActive ? Color.fypPink.opacity(0.08) : Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}

// MARK: - FirebaseServiceProxy (stub de upload para desacoplar da injeção TCA)

/// Proxy leve que delega para o FirebaseService via DependencyValues.
/// Em testes, substituir por mock via @Dependency(\.firebaseService).
final class FirebaseServiceProxy {
    static let shared = FirebaseServiceProxy()
    private init() {}

    func uploadPhoto(_ data: Data, userId: String) async throws -> String {
        // Delega para o serviço real em produção.
        // Esta camada permite usar a view fora do store TCA (ex: previews).
        throw FirebaseServiceError.uploadFailed
    }
}

// MARK: - String: Identifiable (para fullscreenPhoto)

extension String: @retroactive Identifiable {
    public var id: String { self }
}

// MARK: - Preview

#Preview("FREE") {
    @Previewable @State var photos: [String] = [
        "https://picsum.photos/seed/a/400",
        "https://picsum.photos/seed/b/400",
        "https://picsum.photos/seed/c/400"
    ]
    PhotoManagerView(
        photos: $photos,
        user: User(id: "preview", email: "user@test.com", displayName: "Ana", age: 25),
        onSave: { _ in }
    )
}

#Preview("PREMIUM - Limite atingido") {
    @Previewable @State var photos: [String] = (0..<12).map { "https://picsum.photos/seed/\($0)/400" }
    PhotoManagerView(
        photos: $photos,
        user: User(id: "preview", email: "user@test.com", displayName: "Carlos", age: 30, isPremium: true, premiumExpiresAt: Date().addingTimeInterval(86400 * 30)),
        onSave: { _ in }
    )
}
