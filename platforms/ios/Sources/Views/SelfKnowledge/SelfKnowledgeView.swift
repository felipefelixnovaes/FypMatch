// SelfKnowledgeView.swift — FypMatch iOS
// Hub de Autoconhecimento — Eneagrama, Linguagem do Cuidado e Arquétipo

import SwiftUI
import ComposableArchitecture

// MARK: - TCA Feature

@Reducer
struct SelfKnowledgeFeature {
    @ObservableState
    struct State: Equatable {
        var userId: String
        var questionnaire: SelfKnowledgeQuestionnaire? = nil
        var isLoading: Bool = false
        var showEnneagram: Bool = false
        var showLoveLanguage: Bool = false
        var showArchetype: Bool = false
    }

    enum Action: Equatable {
        case onAppear
        case questionnaireLoaded(SelfKnowledgeQuestionnaire?)
        case startEnneagram
        case startLoveLanguage
        case startArchetype
        case dismiss
    }

    @Dependency(\.questionnaireService) var questionnaireService
    @Dependency(\.dismiss) var dismiss

    var body: some ReducerOf<Self> {
        Reduce { state, action in
            switch action {

            case .onAppear:
                state.isLoading = true
                let userId = state.userId
                return .run { send in
                    let q = try? await questionnaireService.loadSelfKnowledge(userId)
                    await send(.questionnaireLoaded(q))
                }

            case let .questionnaireLoaded(q):
                state.isLoading = false
                state.questionnaire = q
                return .none

            case .startEnneagram:
                state.showEnneagram = true
                return .none

            case .startLoveLanguage:
                state.showLoveLanguage = true
                return .none

            case .startArchetype:
                state.showArchetype = true
                return .none

            case .dismiss:
                return .run { _ in await dismiss() }
            }
        }
    }
}

// MARK: - SelfKnowledgeView

struct SelfKnowledgeView: View {
    @Bindable var store: StoreOf<SelfKnowledgeFeature>

    @State private var modulesAppeared = false

    var body: some View {
        NavigationStack {
            Group {
                if store.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    ScrollView {
                        VStack(spacing: 24) {
                            // Header
                            VStack(spacing: 6) {
                                Text("Seu Perfil de Autoconhecimento")
                                    .font(.title2.bold())
                                    .multilineTextAlignment(.center)

                                Text("Descubra seus padrões mais profundos e entenda como você se conecta com outras pessoas.")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                    .multilineTextAlignment(.center)
                                    .padding(.horizontal, 8)
                            }
                            .padding(.top, 8)
                            .padding(.horizontal, 20)

                            // Módulos
                            VStack(spacing: 16) {
                                let q = store.questionnaire

                                SelfKnowledgeModuleCard(
                                    sectionTitle: "ENEAGRAMA",
                                    emoji: q?.enneagram != nil
                                        ? q!.enneagram!.dominantType.emoji
                                        : nil,
                                    resultName: q?.enneagram != nil
                                        ? "Tipo \(q!.enneagram!.dominantType.rawValue) — \(q!.enneagram!.dominantType.displayName)"
                                        : nil,
                                    description: q?.enneagram != nil
                                        ? q!.enneagram!.dominantType.shortDescription
                                        : nil,
                                    isComplete: q?.enneagram != nil,
                                    onAction: { store.send(.startEnneagram) }
                                )
                                .opacity(modulesAppeared ? 1 : 0)
                                .offset(y: modulesAppeared ? 0 : 20)
                                .animation(.easeOut(duration: 0.4).delay(0 * 0.15), value: modulesAppeared)

                                SelfKnowledgeModuleCard(
                                    sectionTitle: "LINGUAGEM DO CUIDADO",
                                    emoji: q?.loveLanguage != nil
                                        ? q!.loveLanguage!.primaryLanguage.emoji
                                        : nil,
                                    resultName: q?.loveLanguage != nil
                                        ? q!.loveLanguage!.primaryLanguage.displayName
                                        : nil,
                                    description: q?.loveLanguage != nil
                                        ? q!.loveLanguage!.primaryLanguage.description
                                        : nil,
                                    isComplete: q?.loveLanguage != nil,
                                    onAction: { store.send(.startLoveLanguage) }
                                )
                                .opacity(modulesAppeared ? 1 : 0)
                                .offset(y: modulesAppeared ? 0 : 20)
                                .animation(.easeOut(duration: 0.4).delay(1 * 0.15), value: modulesAppeared)

                                SelfKnowledgeModuleCard(
                                    sectionTitle: "ARQUÉTIPO",
                                    emoji: q?.archetype != nil
                                        ? q!.archetype!.dominantArchetype.emoji
                                        : nil,
                                    resultName: q?.archetype != nil
                                        ? q!.archetype!.dominantArchetype.displayName
                                        : nil,
                                    description: nil,
                                    isComplete: q?.archetype != nil,
                                    onAction: { store.send(.startArchetype) }
                                )
                                .opacity(modulesAppeared ? 1 : 0)
                                .offset(y: modulesAppeared ? 0 : 20)
                                .animation(.easeOut(duration: 0.4).delay(2 * 0.15), value: modulesAppeared)
                            }
                            .padding(.horizontal, 20)

                            // CombinationCard — somente quando tudo completo
                            if let q = store.questionnaire, q.isFullyComplete,
                               let ennResult = q.enneagram,
                               let llResult = q.loveLanguage,
                               let arcResult = q.archetype {
                                CombinationCard(
                                    enneagramType: ennResult.dominantType,
                                    loveLanguage: llResult.primaryLanguage,
                                    archetype: arcResult.dominantArchetype
                                )
                                .padding(.horizontal, 20)
                                .opacity(modulesAppeared ? 1 : 0)
                                .animation(.easeIn(duration: 0.5).delay(0.6), value: modulesAppeared)
                            }

                            Spacer(minLength: 32)
                        }
                    }
                }
            }
            .navigationTitle("Autoconhecimento")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Fechar") { store.send(.dismiss) }
                        .foregroundColor(.fypPink)
                }
            }
        }
        .onAppear {
            store.send(.onAppear)
            withAnimation { modulesAppeared = true }
        }
        .sheet(isPresented: $store.showEnneagram, onDismiss: { store.send(.onAppear) }) {
            EnneagramView(
                store: Store(
                    initialState: EnneagramFeature.State(userId: store.userId)
                ) { EnneagramFeature() }
            )
        }
        .sheet(isPresented: $store.showLoveLanguage, onDismiss: { store.send(.onAppear) }) {
            LoveLanguageView(
                store: Store(
                    initialState: LoveLanguageFeature.State(userId: store.userId)
                ) { LoveLanguageFeature() }
            )
        }
        .sheet(isPresented: $store.showArchetype, onDismiss: { store.send(.onAppear) }) {
            ArchetypeView(
                store: Store(
                    initialState: ArchetypeFeature.State(userId: store.userId)
                ) { ArchetypeFeature() }
            )
        }
    }
}

// MARK: - SelfKnowledgeModuleCard

struct SelfKnowledgeModuleCard: View {
    let sectionTitle: String
    let emoji: String?
    let resultName: String?
    let description: String?
    let isComplete: Bool
    let onAction: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(sectionTitle)
                .font(.system(size: 11, weight: .bold))
                .foregroundColor(.secondary)
                .tracking(1.2)

            if isComplete, let emoji, let name = resultName {
                HStack(spacing: 16) {
                    Text(emoji)
                        .font(.system(size: 44))

                    VStack(alignment: .leading, spacing: 4) {
                        Text(name)
                            .font(.system(size: 15, weight: .bold))
                            .lineLimit(2)

                        if let desc = description {
                            Text(desc)
                                .font(.caption)
                                .foregroundColor(.secondary)
                                .lineLimit(3)
                        }
                    }
                }

                Button(action: onAction) {
                    Text("Refazer")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(.fypPink)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 7)
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(Color.fypPink, lineWidth: 1)
                        )
                }
            } else {
                HStack(spacing: 16) {
                    Text("?")
                        .font(.system(size: 44))
                        .foregroundColor(.secondary)

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Ainda não descoberto")
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(.secondary)

                        Text("Responda as perguntas para revelar seu perfil.")
                            .font(.caption)
                            .foregroundColor(Color(.tertiaryLabel))
                    }
                }

                GradientButton(title: "Descobrir →", isDisabled: false, action: onAction)
                    .frame(height: 44)
            }
        }
        .padding(18)
        .background(
            RoundedRectangle(cornerRadius: 18)
                .fill(Color(.systemBackground))
                .shadow(color: .black.opacity(0.07), radius: 10, y: 3)
        )
    }
}

// MARK: - CombinationCard

struct CombinationCard: View {
    let enneagramType: EnneagramType
    let loveLanguage: LoveLanguage
    let archetype: FypArchetype

    private var combinationPhrase: String {
        "Um(a) \(enneagramType.displayName.lowercased()) que se sente amado(a) com \(loveLanguage.displayName.lowercased()) e carrega o espírito de \(archetype.displayName.lowercased().replacingOccurrences(of: "o ", with: "").replacingOccurrences(of: "a ", with: ""))."
    }

    var body: some View {
        VStack(spacing: 16) {
            Text("Sua combinação única")
                .font(.system(size: 12, weight: .bold))
                .foregroundColor(.white.opacity(0.8))
                .tracking(1.0)

            HStack(spacing: 20) {
                Text(enneagramType.emoji)
                    .font(.largeTitle)
                Text(loveLanguage.emoji)
                    .font(.largeTitle)
                Text(archetype.emoji)
                    .font(.largeTitle)
            }

            Text(combinationPhrase)
                .font(.system(size: 15, weight: .medium))
                .foregroundColor(.white)
                .multilineTextAlignment(.center)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(24)
        .frame(maxWidth: .infinity)
        .background(
            LinearGradient(
                colors: [.fypPink, .fypPurple],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .clipShape(RoundedRectangle(cornerRadius: 20))
    }
}

// MARK: - Preview

#if DEBUG
struct SelfKnowledgeView_Previews: PreviewProvider {
    static var previews: some View {
        SelfKnowledgeView(
            store: Store(
                initialState: SelfKnowledgeFeature.State(userId: "preview-user")
            ) {
                SelfKnowledgeFeature()
            }
        )
    }
}

struct CombinationCard_Previews: PreviewProvider {
    static var previews: some View {
        CombinationCard(
            enneagramType: .four,
            loveLanguage: .qualityTime,
            archetype: .lover
        )
        .padding()
    }
}
#endif
