// MatchesView.swift — FypMatch iOS
// Port de MatchesScreen.kt

import SwiftUI

struct MatchesView: View {
    @StateObject private var viewModel = MatchesViewModel()

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.isLoading {
                    ProgressView().tint(.fypPink).scaleEffect(1.3)
                } else if viewModel.matches.isEmpty {
                    emptyView
                } else {
                    matchesList
                }
            }
            .navigationTitle("Matches")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Menu {
                        Button("Mais recentes") { viewModel.sortBy(.recent) }
                        Button("Compatibilidade") { viewModel.sortBy(.compatibility) }
                        Button("Não lidos primeiro") { viewModel.sortBy(.unread) }
                    } label: {
                        Image(systemName: "arrow.up.arrow.down.circle")
                            .foregroundColor(.fypPink)
                    }
                }
            }
        }
        .onAppear { viewModel.loadMatches() }
    }

    private var matchesList: some View {
        ScrollView {
            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                ForEach(viewModel.matches) { match in
                    NavigationLink {
                        ChatView(conversation: match.toConversation())
                    } label: {
                        MatchCard(match: match)
                    }
                }
            }
            .padding(16)
        }
    }

    private var emptyView: some View {
        VStack(spacing: 20) {
            Image(systemName: "heart.circle").font(.system(size: 72))
                .foregroundStyle(LinearGradient.fypGradient)
            Text("Sem matches ainda").font(.title3.bold())
            Text("Continue swipando — seus matches aparecerão aqui quando alguém curtir você de volta.")
                .font(.subheadline).foregroundColor(.secondary)
                .multilineTextAlignment(.center).padding(.horizontal, 32)
        }
    }
}

// MARK: - MatchCard

struct MatchCard: View {
    let match: Match

    var body: some View {
        ZStack(alignment: .bottom) {
            AsyncImage(url: URL(string: match.photoUrl ?? "")) { img in
                img.resizable().scaledToFill()
            } placeholder: {
                LinearGradient.fypGradient
            }
            .frame(height: 200)
            .clipped()

            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(match.userName).font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.white).lineLimit(1)
                    Spacer()
                    if match.hasUnread {
                        Circle().fill(Color.fypPink).frame(width: 8, height: 8)
                    }
                }
                if let score = match.compatibilityScore {
                    CompatibilityBadge(score: Int(score * 100))
                }
                Text(match.matchTimeAgo).font(.caption).foregroundColor(.white.opacity(0.75))
            }
            .padding(10)
            .background(LinearGradient.cardOverlay)
        }
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.12), radius: 8, y: 4)
        .accessibilityLabel("\(match.userName), \(match.hasUnread ? "nova mensagem" : "")")
    }
}

// MARK: - MatchesViewModel

@MainActor
class MatchesViewModel: ObservableObject {
    @Published var matches: [Match] = []
    @Published var isLoading = false

    enum SortOrder { case recent, compatibility, unread }

    func loadMatches() {
        isLoading = true
        FirebaseService.shared.loadMatches { [weak self] matches in
            self?.matches = matches
            self?.isLoading = false
        }
    }

    func sortBy(_ order: SortOrder) {
        switch order {
        case .recent:    matches.sort { $0.createdAt > $1.createdAt }
        case .compatibility: matches.sort { ($0.compatibilityScore ?? 0) > ($1.compatibilityScore ?? 0) }
        case .unread:    matches.sort { $0.hasUnread && !$1.hasUnread }
        }
    }
}

// MARK: - Match model extension

extension Match {
    func toConversation() -> Conversation {
        Conversation(
            id: id,
            otherUserId: userId,
            otherUserName: userName,
            otherUserPhotoURL: photoUrl ?? "",
            lastMessage: "",
            lastMessageTimestamp: createdAt,
            unreadCount: hasUnread ? 1 : 0,
            isOtherUserOnline: false
        )
    }
}

extension FirebaseService {
    func loadMatches(completion: @escaping ([Match]) -> Void) {
        completion([]) // replaced by Firestore implementation
    }
}
