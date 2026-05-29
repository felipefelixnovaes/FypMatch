// ProfileEditView.swift — FypMatch iOS

import SwiftUI
import PhotosUI

struct ProfileEditView: View {
    let user: User
    @Environment(\.dismiss) private var dismiss
    @State private var displayName: String
    @State private var bio: String
    @State private var age: Int
    @State private var occupation: String
    @State private var interests: [String]
    @State private var selectedPhotos: [PhotosPickerItem] = []
    @State private var isSaving = false

    init(user: User) {
        self.user = user
        _displayName = State(initialValue: user.displayName)
        _bio        = State(initialValue: user.bio)
        _age        = State(initialValue: user.age)
        _occupation = State(initialValue: user.occupation ?? "")
        _interests  = State(initialValue: user.interests)
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Fotos") {
                    PhotosPicker(selection: $selectedPhotos, maxSelectionCount: 6, matching: .images) {
                        HStack {
                            Image(systemName: "photo.on.rectangle.angled").foregroundColor(.fypPink)
                            Text("Gerenciar fotos (\(user.photos.count)/6)")
                        }
                    }
                }

                Section("Informações básicas") {
                    FypTextField(placeholder: "Nome", text: $displayName, icon: "person")
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Idade: \(age) anos").font(.subheadline).foregroundColor(.secondary)
                        Slider(value: Binding(get: { Double(age) }, set: { age = Int($0) }), in: 18...80, step: 1)
                            .accentColor(.fypPink)
                    }
                    .padding(14).background(Color(.secondarySystemBackground)).clipShape(RoundedRectangle(cornerRadius: 12))
                    FypTextField(placeholder: "Profissão (opcional)", text: $occupation, icon: "briefcase")
                }

                Section("Bio") {
                    TextEditor(text: $bio)
                        .frame(minHeight: 100)
                        .font(.subheadline)
                    Text("\(bio.count)/300").font(.caption).foregroundColor(.secondary).frame(maxWidth: .infinity, alignment: .trailing)
                }

                Section("Interesses") {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(interests, id: \.self) { interest in
                                HStack(spacing: 4) {
                                    Text(interest).font(.subheadline).foregroundColor(.fypPink)
                                    Button { interests.removeAll { $0 == interest } } label: {
                                        Image(systemName: "xmark.circle.fill").font(.caption).foregroundColor(.fypPink.opacity(0.7))
                                    }
                                }
                                .padding(.horizontal, 10).padding(.vertical, 6)
                                .background(Color.fypPink.opacity(0.1)).clipShape(Capsule())
                            }
                        }
                    }
                    .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))

                    // Add interest chips
                    let allInterests = ["Música", "Cinema", "Viagens", "Gastronomia", "Esportes", "Arte", "Tecnologia", "Natureza", "Leitura", "Yoga"]
                    FlowLayout(spacing: 8) {
                        ForEach(allInterests.filter { !interests.contains($0) }, id: \.self) { i in
                            Button(i) { if interests.count < 10 { interests.append(i) } }
                                .font(.subheadline).padding(.horizontal, 10).padding(.vertical, 6)
                                .background(Color(.secondarySystemBackground)).clipShape(Capsule())
                                .foregroundColor(.primary)
                        }
                    }
                    .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                }
            }
            .navigationTitle("Editar perfil")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cancelar") { dismiss() }.foregroundColor(.fypPink)
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Salvar") { saveProfile() }
                        .font(.headline).foregroundColor(.fypPink)
                        .disabled(isSaving)
                }
            }
            .overlay {
                if isSaving {
                    ZStack {
                        Color.black.opacity(0.3).ignoresSafeArea()
                        VStack(spacing: 12) {
                            ProgressView().tint(.white).scaleEffect(1.3)
                            Text("Salvando…").foregroundColor(.white).font(.subheadline)
                        }
                        .padding(24).background(.ultraThinMaterial).clipShape(RoundedRectangle(cornerRadius: 16))
                    }
                }
            }
        }
    }

    private func saveProfile() {
        isSaving = true
        Task {
            await FirebaseService.shared.updateProfile(
                displayName: displayName, bio: bio, age: age,
                occupation: occupation.isEmpty ? nil : occupation,
                interests: interests
            )
            isSaving = false
            dismiss()
        }
    }
}

extension FirebaseService {
    func updateProfile(displayName: String, bio: String, age: Int, occupation: String?, interests: [String]) async {
        // Firestore update
    }
}
