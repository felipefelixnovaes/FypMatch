import SwiftUI
import Firebase
import ComposableArchitecture

@main
struct FypMatchApp: App {
    
    // Initialize Firebase
    init() {
        FirebaseApp.configure()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView(
                store: Store(
                    initialState: AppFeature.State(),
                    reducer: {
                        AppFeature()
                    }
                )
            )
        }
    }
}

// MARK: - App Feature
struct AppFeature: Reducer {
    struct State: Equatable {
        var isAuthenticated = false
        var currentUser: User?
        var isLoading = false
    }
    
    enum Action: Equatable {
        case onAppear
        case authenticationChanged(Bool)
        case userLoaded(User?)
        case signOut
    }
    
    var body: some ReducerOf<Self> {
        Reduce { state, action in
            switch action {
            case .onAppear:
                state.isLoading = true
                return .run { send in
                    // Check authentication status
                    let isAuth = Auth.auth().currentUser != nil
                    await send(.authenticationChanged(isAuth))
                }
                
            case .authenticationChanged(let isAuthenticated):
                state.isAuthenticated = isAuthenticated
                state.isLoading = false
                return .none
                
            case .userLoaded(let user):
                state.currentUser = user
                return .none
                
            case .signOut:
                try? Auth.auth().signOut()
                state.isAuthenticated = false
                state.currentUser = nil
                return .none
            }
        }
    }
}

// MARK: - Content View
struct ContentView: View {
    let store: StoreOf<AppFeature>
    
    var body: some View {
        WithViewStore(self.store, observe: { $0 }) { viewStore in
            Group {
                if viewStore.isLoading {
                    LoadingView()
                } else if viewStore.isAuthenticated {
                    MainTabView()
                } else {
                    AuthenticationView()
                }
            }
            .onAppear {
                viewStore.send(.onAppear)
            }
        }
    }
}

// MARK: - Loading View
struct LoadingView: View {
    var body: some View {
        VStack(spacing: 20) {
            Image("fypmatch_logo")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 120, height: 120)
                .clipShape(RoundedRectangle(cornerRadius: 30))
                .shadow(radius: 10)
            
            Text("FypMatch")
                .font(.largeTitle)
                .fontWeight(.bold)
                .foregroundColor(.primary)
            
            ProgressView()
                .scaleEffect(1.5)
                .tint(.blue)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(.systemBackground))
    }
}

// MARK: - Authentication View
struct AuthenticationView: View {
    var body: some View {
        VStack(spacing: 30) {
            Spacer()
            
            // Logo and Title
            VStack(spacing: 16) {
                Image("fypmatch_logo")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 100, height: 100)
                    .clipShape(RoundedRectangle(cornerRadius: 25))
                
                Text("FypMatch")
                    .font(.system(size: 36, weight: .bold))
                    .foregroundColor(.primary)
                
                Text("Encontre conexões genuínas")
                    .font(.title3)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }
            
            Spacer()
            
            // Auth Buttons
            VStack(spacing: 16) {
                Button(action: {
                    // TODO: Implement Google Sign In
                }) {
                    HStack {
                        Image(systemName: "globe")
                            .font(.title2)
                        
                        Text("Continuar com Google")
                            .font(.headline)
                            .fontWeight(.semibold)
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 56)
                    .background(Color.blue)
                    .cornerRadius(28)
                }
                
                Button(action: {
                    // TODO: Implement Apple Sign In
                }) {
                    HStack {
                        Image(systemName: "applelogo")
                            .font(.title2)
                        
                        Text("Continuar com Apple")
                            .font(.headline)
                            .fontWeight(.semibold)
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 56)
                    .background(Color.black)
                    .cornerRadius(28)
                }
            }
            .padding(.horizontal, 32)
            
            Spacer()
            
            // Terms
            Text("Ao continuar, você aceita nossos Termos de Uso e Política de Privacidade")
                .font(.caption)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(
            LinearGradient(
                colors: [Color.blue.opacity(0.1), Color.purple.opacity(0.1)],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
    }
}

// MARK: - Main Tab View
struct MainTabView: View {
    var body: some View {
        TabView {
            DiscoveryView()
                .tabItem {
                    Image(systemName: "heart.fill")
                    Text("Descobrir")
                }
            
            MatchesView()
                .tabItem {
                    Image(systemName: "message.fill")
                    Text("Matches")
                }
            
            ProfileView()
                .tabItem {
                    Image(systemName: "person.fill")
                    Text("Perfil")
                }
        }
        .tint(.blue)
    }
}

// MARK: - Placeholder Views
struct DiscoveryView: View {
    var body: some View {
        NavigationView {
            VStack {
                Text("Discovery View")
                    .font(.title)
                Text("Swipe cards will be implemented here")
                    .foregroundColor(.secondary)
            }
            .navigationTitle("Descobrir")
        }
    }
}

struct MatchesView: View {
    var body: some View {
        NavigationView {
            VStack {
                Text("Matches & Chat")
                    .font(.title)
                Text("Your matches and conversations")
                    .foregroundColor(.secondary)
            }
            .navigationTitle("Matches")
        }
    }
}

struct ProfileView: View {
    var body: some View {
        NavigationView {
            VStack {
                Text("Profile View")
                    .font(.title)
                Text("User profile and settings")
                    .foregroundColor(.secondary)
            }
            .navigationTitle("Perfil")
        }
    }
}

// MARK: - User Model
struct User: Equatable, Codable {
    let id: String
    let email: String
    let displayName: String
    let photoURL: String?
    let createdAt: Date
    
    init(id: String, email: String, displayName: String, photoURL: String? = nil, createdAt: Date = Date()) {
        self.id = id
        self.email = email
        self.displayName = displayName
        self.photoURL = photoURL
        self.createdAt = createdAt
    }
}