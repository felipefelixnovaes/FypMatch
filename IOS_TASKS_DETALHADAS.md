# 📝 FypMatch iOS - Tasks Técnicas Detalhadas

## 🎯 **TAREFAS POR SPRINT (2 SEMANAS)**

---

## 🏗️ **SPRINT 1: FOUNDATION (Semanas 1-2)**

### **SEMANA 1: Project Setup**

#### **Task 1.1: Criar Projeto Xcode**
**Responsável:** iOS Developer  
**Estimativa:** 4 horas  
**Prioridade:** P0 (Crítica)

**Detalhes técnicos:**
```
- Xcode 15+ Project
- iOS Deployment Target: 15.0
- Bundle ID: com.ideiassertiva.FypMatch
- Team: Ideias Sertiva
- Language: Swift
- UI Framework: SwiftUI
- Core Data: No (using Firebase)
```

**Acceptance Criteria:**
- [ ] Projeto compila sem warnings
- [ ] App roda no simulador iOS 15+
- [ ] Bundle ID configurado corretamente
- [ ] Git repository initialized

**Definition of Done:**
- ✅ Projeto iOS funcional
- ✅ README.md atualizado
- ✅ Estrutura de pastas criada

---

#### **Task 1.2: Firebase iOS Setup**
**Responsável:** iOS Developer  
**Estimativa:** 6 horas  
**Prioridade:** P0 (Crítica)

**Detalhes técnicos:**
```swift
// Firebase configuration
- Firebase Console: Add iOS app
- GoogleService-Info.plist: Download and add
- SwiftPM: Firebase iOS SDK 10.20.0+
- Dependencies:
  * FirebaseAuth
  * FirebaseFirestore
  * FirebaseStorage
  * FirebaseMessaging
```

**Acceptance Criteria:**
- [ ] Firebase console configured
- [ ] GoogleService-Info.plist integrated
- [ ] Basic Firebase connection test
- [ ] No build errors

**Test:**
```swift
func testFirebaseConnection() {
    // Test Firestore connection
    let db = Firestore.firestore()
    XCTAssertNotNil(db)
}
```

---

#### **Task 1.3: Dependencies Setup**
**Responsável:** iOS Developer  
**Estimativa:** 3 horas  
**Prioridade:** P1 (Alta)

**Package.swift dependencies:**
```swift
dependencies: [
    .package(url: "https://github.com/firebase/firebase-ios-sdk", from: "10.20.0"),
    .package(url: "https://github.com/pointfreeco/swift-composable-architecture", from: "1.7.0"),
    .package(url: "https://github.com/onevcat/Kingfisher", from: "7.10.0"),
    .package(url: "https://github.com/Alamofire/Alamofire", from: "5.8.0")
]
```

**Acceptance Criteria:**
- [ ] All dependencies resolve
- [ ] No version conflicts
- [ ] Build time < 30 seconds
- [ ] Archive build successful

---

### **SEMANA 2: Core Models**

#### **Task 2.1: User Model Port**
**Responsável:** iOS Developer  
**Estimativa:** 12 horas  
**Prioridade:** P0 (Crítica)

**Arquivos para criar:**
```
Models/
├── User.swift              (NOVO)
├── UserProfile.swift       (NOVO)
├── UserPreferences.swift   (NOVO)
├── Enums/
│   ├── Gender.swift        (NOVO)
│   ├── Orientation.swift   (NOVO)
│   ├── Intention.swift     (NOVO)
│   ├── RelationshipStatus.swift (NOVO)
│   └── [10+ enum files]
```

**User.swift structure:**
```swift
struct User: Codable, Identifiable, Equatable {
    let id: String
    let email: String
    let displayName: String
    let photoUrl: String
    let profile: UserProfile
    let preferences: UserPreferences
    let subscription: SubscriptionStatus
    let accessLevel: AccessLevel
    let betaFlags: BetaFlags
    let createdAt: Date
    let lastActive: Date
    let isActive: Bool
    let waitlistData: WaitlistUser?
    let aiCredits: AiCredits
}
```

**Acceptance Criteria:**
- [ ] All enums implemented with display names
- [ ] Codable conformance working
- [ ] Unit tests for all models
- [ ] Extension methods ported

---

#### **Task 2.2: Chat Models**
**Responsável:** iOS Developer  
**Estimativa:** 6 horas  
**Prioridade:** P1 (Alta)

**Arquivos:**
```swift
// Message.swift
struct Message: Codable, Identifiable {
    let id: String
    let conversationId: String
    let senderId: String
    let receiverId: String
    let content: String
    let timestamp: Date
    let type: MessageType
    let isRead: Bool
}

// Conversation.swift
struct Conversation: Codable, Identifiable {
    let id: String
    let participants: [String]
    let lastMessage: Message?
    let lastUpdated: Date
    let isArchived: Bool
}
```

**Acceptance Criteria:**
- [ ] Message types enum complete
- [ ] Real-time compatibility ready
- [ ] Firestore mapping working
- [ ] Unit tests passing

---

#### **Task 2.3: Matching Models**
**Responsável:** iOS Developer  
**Estimativa:** 4 horas  
**Prioridade:** P1 (Alta)

**SwipeAction.swift:**
```swift
enum SwipeAction: String, CaseIterable, Codable {
    case like = "like"
    case pass = "pass"
    case superLike = "super_like"
    case report = "report"
    
    var icon: String {
        switch self {
        case .like: return "heart.fill"
        case .pass: return "xmark"
        case .superLike: return "star.fill"
        case .report: return "exclamationmark.triangle"
        }
    }
}
```

**Match.swift:**
```swift
struct Match: Codable, Identifiable {
    let id: String
    let user1Id: String
    let user2Id: String
    let timestamp: Date
    let isActive: Bool
    let compatibilityScore: Double
}
```

---

## 🔐 **SPRINT 2: AUTHENTICATION (Semanas 3-4)**

### **SEMANA 3: Auth System**

#### **Task 3.1: AuthFeature (TCA)**
**Responsável:** iOS Developer  
**Estimativa:** 10 horas  
**Prioridade:** P0 (Crítica)

**AuthFeature.swift:**
```swift
struct AuthFeature: Reducer {
    struct State: Equatable {
        var isAuthenticated = false
        var currentUser: User?
        var isLoading = false
        var errorMessage: String?
        var authMethod: AuthMethod = .none
    }
    
    enum Action: Equatable {
        case onAppear
        case googleSignInTapped
        case appleSignInTapped
        case signOut
        case authenticationResult(Result<User, AuthError>)
    }
    
    var body: some ReducerOf<Self> {
        Reduce { state, action in
            // Implementation
        }
    }
}
```

**Dependencies:**
- Firebase Auth integration
- Google Sign-In SDK
- Apple Sign-In framework

**Acceptance Criteria:**
- [ ] TCA state management working
- [ ] All authentication flows
- [ ] Error handling complete
- [ ] State persistence

---

#### **Task 3.2: Google Sign-In Integration**
**Responsável:** iOS Developer  
**Estimativa:** 8 horas  
**Prioridade:** P0 (Crítica)

**Technical setup:**
```swift
// GoogleService-Info.plist configuration
// URL schemes setup
// GoogleSignIn SDK integration

func signInWithGoogle() -> Effect<AuthFeature.Action> {
    .run { send in
        guard let presentingViewController = UIApplication.shared.windows.first?.rootViewController else {
            await send(.authenticationResult(.failure(.noViewController)))
            return
        }
        
        do {
            let result = try await GoogleSignIn.shared.signIn(withPresenting: presentingViewController)
            let credential = GoogleAuthProvider.credential(
                withIDToken: result.user.idToken!.tokenString,
                accessToken: result.user.accessToken.tokenString
            )
            let authResult = try await Auth.auth().signIn(with: credential)
            let user = try await UserService.createOrUpdateUser(from: authResult.user)
            await send(.authenticationResult(.success(user)))
        } catch {
            await send(.authenticationResult(.failure(.googleSignInFailed(error))))
        }
    }
}
```

**Acceptance Criteria:**
- [ ] Google Sign-In working
- [ ] Error handling complete
- [ ] User creation flow
- [ ] Token refresh handling

---

#### **Task 3.3: Apple Sign-In Integration**
**Responsável:** iOS Developer  
**Estimativa:** 6 horas  
**Prioridade:** P0 (Crítica)

**Apple Sign-In setup:**
```swift
import AuthenticationServices

func signInWithApple() -> Effect<AuthFeature.Action> {
    .run { send in
        let request = ASAuthorizationAppleIDProvider().createRequest()
        request.requestedScopes = [.fullName, .email]
        
        let controller = ASAuthorizationController(authorizationRequests: [request])
        // Implementation with delegate pattern
    }
}
```

**Requirements:**
- App Store Connect configuration
- Capabilities: Sign In with Apple
- Keychain entitlements
- Privacy manifest

---

### **SEMANA 4: Profile System**

#### **Task 4.1: ProfileFeature (TCA)**
**Responsável:** iOS Developer  
**Estimativa:** 12 horas  
**Prioridade:** P0 (Crítica)

**ProfileFeature.swift structure:**
```swift
struct ProfileFeature: Reducer {
    struct State: Equatable {
        var user: User?
        var isEditing = false
        var isLoading = false
        var photos: [PhotoUpload] = []
        var completionPercentage: Double = 0.0
    }
    
    enum Action: Equatable {
        case onAppear
        case editTapped
        case saveProfile
        case photoSelected(UIImage)
        case deletePhoto(String)
        case updateField(ProfileField, String)
    }
}
```

**Profile completion logic:**
```swift
func calculateCompletion(for user: User) -> Double {
    let requiredFields = [
        !user.profile.fullName.isEmpty,
        user.profile.age >= 18,
        !user.profile.bio.isEmpty,
        !user.profile.photos.isEmpty,
        user.profile.gender != .notSpecified,
        user.profile.orientation != .notSpecified
    ]
    
    let completed = requiredFields.filter { $0 }.count
    return Double(completed) / Double(requiredFields.count)
}
```

---

#### **Task 4.2: Photo Upload System**
**Responsável:** iOS Developer  
**Estimativa:** 10 horas  
**Prioridade:** P1 (Alta)

**PhotoUploadService.swift:**
```swift
class PhotoUploadService {
    func uploadPhoto(_ image: UIImage, for userId: String) async throws -> String {
        // Resize image to optimal size
        let resizedImage = image.resized(to: CGSize(width: 800, height: 800))
        
        // Convert to JPEG with compression
        guard let imageData = resizedImage.jpegData(compressionQuality: 0.8) else {
            throw PhotoUploadError.invalidImage
        }
        
        // Upload to Firebase Storage
        let storageRef = Storage.storage().reference()
        let photoRef = storageRef.child("users/\(userId)/photos/\(UUID().uuidString).jpg")
        
        let metadata = StorageMetadata()
        metadata.contentType = "image/jpeg"
        
        let _ = try await photoRef.putDataAsync(imageData, metadata: metadata)
        let downloadURL = try await photoRef.downloadURL()
        
        return downloadURL.absoluteString
    }
}
```

**Features:**
- Image picker integration
- Photo reordering
- Crop functionality
- Progress indicators
- Error handling

---

## 🎯 **SPRINT 3: DISCOVERY (Semanas 5-6)**

### **SEMANA 5: Swipe System**

#### **Task 5.1: SwipeCardsView Implementation**
**Responsável:** iOS Developer  
**Estimativa:** 16 horas  
**Prioridade:** P0 (Crítica)

**SwipeCardsView.swift:**
```swift
struct SwipeCardsView: View {
    let store: StoreOf<DiscoveryFeature>
    @State private var dragOffset = CGSize.zero
    @State private var rotation: Double = 0
    
    var body: some View {
        WithViewStore(store, observe: { $0 }) { viewStore in
            GeometryReader { geometry in
                ZStack {
                    ForEach(viewStore.userStack.indices.reversed(), id: \.self) { index in
                        if index < 3 { // Show only top 3 cards
                            UserCardView(user: viewStore.userStack[index])
                                .zIndex(Double(viewStore.userStack.count - index))
                                .offset(
                                    x: index == 0 ? dragOffset.width : 0,
                                    y: index == 0 ? dragOffset.height : CGFloat(index * 5)
                                )
                                .scaleEffect(index == 0 ? 1.0 : 1.0 - (CGFloat(index) * 0.05))
                                .rotationEffect(.degrees(index == 0 ? rotation : 0))
                                .gesture(
                                    index == 0 ? dragGesture(for: geometry) : nil
                                )
                        }
                    }
                }
            }
        }
    }
    
    private func dragGesture(for geometry: GeometryProxy) -> some Gesture {
        DragGesture()
            .onChanged { value in
                dragOffset = value.translation
                rotation = Double(value.translation.x / 20)
            }
            .onEnded { value in
                let threshold: CGFloat = 100
                let swipeDirection = determineSwipeDirection(
                    translation: value.translation,
                    threshold: threshold
                )
                
                if let direction = swipeDirection {
                    performSwipe(direction: direction)
                } else {
                    // Snap back to center
                    withAnimation(.spring()) {
                        dragOffset = .zero
                        rotation = 0
                    }
                }
            }
    }
}
```

**Gesture mechanics:**
- Drag gestures with spring animations
- Rotation based on drag direction
- Threshold-based swipe detection
- Stack management (show 3 cards)

---

#### **Task 5.2: UserCardView Component**
**Responsável:** iOS Developer  
**Estimativa:** 8 horas  
**Prioridade:** P1 (Alta)

**UserCardView.swift:**
```swift
struct UserCardView: View {
    let user: User
    @State private var currentPhotoIndex = 0
    
    var body: some View {
        VStack(spacing: 0) {
            // Photo carousel
            TabView(selection: $currentPhotoIndex) {
                ForEach(user.profile.photos.indices, id: \.self) { index in
                    AsyncImage(url: URL(string: user.profile.photos[index])) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } placeholder: {
                        ShimmerView()
                    }
                    .tag(index)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
            .frame(height: 400)
            .clipped()
            
            // User info
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    Text(user.profile.fullName)
                        .font(.title2)
                        .fontWeight(.bold)
                    
                    Text("\(user.profile.age)")
                        .font(.title2)
                        .foregroundColor(.secondary)
                    
                    Spacer()
                    
                    if user.subscription == .premium {
                        PremiumBadgeView()
                    }
                }
                
                if !user.profile.bio.isEmpty {
                    Text(user.profile.bio)
                        .font(.body)
                        .lineLimit(3)
                }
                
                HStack {
                    Label(user.profile.location.city, systemImage: "location")
                    Spacer()
                    Label("\(Int(user.distanceKm))km", systemImage: "arrow.trianglehead.2.slashˇ.clockwise")
                }
                .font(.caption)
                .foregroundColor(.secondary)
            }
            .padding()
        }
        .background(Color(.systemBackground))
        .cornerRadius(16)
        .shadow(radius: 8)
    }
}
```

---

### **SEMANA 6: Matching Logic**

#### **Task 6.1: MatchingService Implementation**
**Responsável:** iOS Developer  
**Estimativa:** 12 horas  
**Prioridade:** P0 (Crítica)

**MatchingService.swift:**
```swift
class MatchingService {
    private let firestore = Firestore.firestore()
    
    func processSwipe(
        currentUserId: String,
        targetUserId: String,
        action: SwipeAction
    ) async throws -> SwipeResult {
        
        // Record the swipe
        try await recordSwipe(
            from: currentUserId,
            to: targetUserId,
            action: action
        )
        
        // Check for mutual like (match)
        if action == .like {
            let isMatch = try await checkForMatch(
                user1: currentUserId,
                user2: targetUserId
            )
            
            if isMatch {
                let match = try await createMatch(
                    user1: currentUserId,
                    user2: targetUserId
                )
                
                // Send push notifications
                try await sendMatchNotifications(match: match)
                
                return .match(match)
            }
        }
        
        return .noMatch
    }
    
    private func checkForMatch(user1: String, user2: String) async throws -> Bool {
        let swipeQuery = firestore
            .collection("swipes")
            .whereField("fromUserId", isEqualTo: user2)
            .whereField("toUserId", isEqualTo: user1)
            .whereField("action", isEqualTo: SwipeAction.like.rawValue)
        
        let snapshot = try await swipeQuery.getDocuments()
        return !snapshot.documents.isEmpty
    }
}

enum SwipeResult {
    case match(Match)
    case noMatch
}
```

---

## 💬 **SPRINT 4: CHAT SYSTEM (Semanas 7-8)**

### **SEMANA 7: Real-time Chat**

#### **Task 7.1: ChatFeature (TCA)**
**Responsável:** iOS Developer  
**Estimativa:** 14 horas  
**Prioridade:** P0 (Crítica)

**ChatFeature.swift:**
```swift
struct ChatFeature: Reducer {
    struct State: Equatable {
        var conversation: Conversation?
        var messages: [Message] = []
        var newMessageText = ""
        var isLoading = false
        var isTyping = false
        var otherUser: User?
    }
    
    enum Action: Equatable {
        case onAppear
        case sendMessage
        case messageTextChanged(String)
        case messagesReceived([Message])
        case typingIndicatorChanged(Bool)
        case loadMoreMessages
    }
    
    var body: some ReducerOf<Self> {
        Reduce { state, action in
            switch action {
            case .onAppear:
                return .run { [conversationId = state.conversation?.id] send in
                    guard let conversationId = conversationId else { return }
                    
                    // Start listening to messages
                    for await messages in ChatService.shared.listenToMessages(conversationId: conversationId) {
                        await send(.messagesReceived(messages))
                    }
                }
                
            case .sendMessage:
                let messageText = state.newMessageText.trimmingCharacters(in: .whitespacesAndNewlines)
                guard !messageText.isEmpty else { return .none }
                
                state.newMessageText = ""
                
                return .run { [conversation = state.conversation] send in
                    guard let conversation = conversation else { return }
                    
                    try await ChatService.shared.sendMessage(
                        to: conversation.id,
                        content: messageText
                    )
                }
            }
        }
    }
}
```

**Real-time messaging:**
- Firestore real-time listeners
- Message ordering by timestamp
- Typing indicators
- Message status (sent, delivered, read)

---

#### **Task 7.2: ChatView Implementation**
**Responsável:** iOS Developer  
**Estimativa:** 10 horas  
**Prioridade:** P0 (Crítica)

**ChatView.swift:**
```swift
struct ChatView: View {
    let store: StoreOf<ChatFeature>
    @FocusState private var isInputFocused: Bool
    
    var body: some View {
        WithViewStore(store, observe: { $0 }) { viewStore in
            VStack(spacing: 0) {
                // Messages list
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(spacing: 8) {
                            ForEach(viewStore.messages) { message in
                                MessageBubbleView(
                                    message: message,
                                    isFromCurrentUser: message.senderId == AuthService.shared.currentUser?.id
                                )
                                .id(message.id)
                            }
                        }
                        .padding(.horizontal)
                    }
                    .onChange(of: viewStore.messages.count) { _ in
                        if let lastMessage = viewStore.messages.last {
                            proxy.scrollTo(lastMessage.id, anchor: .bottom)
                        }
                    }
                }
                
                // Input area
                HStack(spacing: 12) {
                    TextField("Mensagem...", text: viewStore.$newMessageText)
                        .textFieldStyle(.roundedBorder)
                        .focused($isInputFocused)
                        .onSubmit {
                            viewStore.send(.sendMessage)
                        }
                    
                    Button(action: {
                        viewStore.send(.sendMessage)
                    }) {
                        Image(systemName: "paperplane.fill")
                            .foregroundColor(viewStore.newMessageText.isEmpty ? .gray : .blue)
                    }
                    .disabled(viewStore.newMessageText.isEmpty)
                }
                .padding()
                .background(Color(.systemBackground))
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            viewStore.send(.onAppear)
        }
    }
}
```

---

### **SEMANA 8: Chat Features**

#### **Task 8.1: Push Notifications**
**Responsável:** iOS Developer  
**Estimativa:** 8 horas  
**Prioridade:** P1 (Alta)

**NotificationService.swift:**
```swift
class NotificationService: NSObject, UNUserNotificationCenterDelegate {
    static let shared = NotificationService()
    
    func requestPermission() async -> Bool {
        let center = UNUserNotificationCenter.current()
        
        do {
            let granted = try await center.requestAuthorization(options: [.alert, .sound, .badge])
            return granted
        } catch {
            print("Notification permission error: \(error)")
            return false
        }
    }
    
    func registerForRemoteNotifications() {
        DispatchQueue.main.async {
            UIApplication.shared.registerForRemoteNotifications()
        }
    }
    
    // Handle notification when app is in foreground
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound])
    }
    
    // Handle notification tap
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo
        
        // Deep link to chat if it's a message notification
        if let conversationId = userInfo["conversationId"] as? String {
            DeepLinkService.shared.navigateToChat(conversationId: conversationId)
        }
        
        completionHandler()
    }
}
```

**FCM Integration:**
- Firebase Messaging setup
- APNs certificate configuration
- Deep linking to conversations
- Badge count management

---

## 💎 **SPRINT 5: PREMIUM & LAUNCH (Semanas 9-10)**

### **SEMANA 9: Premium Features**

#### **Task 9.1: In-App Purchases**
**Responsável:** iOS Developer  
**Estimativa:** 12 horas  
**Prioridade:** P1 (Alta)

**StoreKitService.swift:**
```swift
import StoreKit

class StoreKitService: NSObject, ObservableObject {
    @Published var products: [Product] = []
    @Published var purchasedProducts: Set<String> = []
    
    private let productIdentifiers = [
        "com.ideiassertiva.fypmatch.premium.monthly",
        "com.ideiassertiva.fypmatch.premium.yearly",
        "com.ideiassertiva.fypmatch.vip.monthly"
    ]
    
    override init() {
        super.init()
        Task {
            await loadProducts()
            await checkPurchasedProducts()
        }
    }
    
    @MainActor
    func loadProducts() async {
        do {
            products = try await Product.products(for: productIdentifiers)
        } catch {
            print("Failed to load products: \(error)")
        }
    }
    
    func purchase(_ product: Product) async throws -> PurchaseResult {
        let result = try await product.purchase()
        
        switch result {
        case .success(let verification):
            switch verification {
            case .verified(let transaction):
                await handleVerifiedPurchase(transaction)
                await transaction.finish()
                return .success
            case .unverified:
                return .failure(.verificationFailed)
            }
        case .userCancelled:
            return .failure(.userCancelled)
        case .pending:
            return .pending
        @unknown default:
            return .failure(.unknown)
        }
    }
}
```

**Premium features:**
- Monthly/yearly subscriptions
- VIP tier
- Feature gating
- Receipt validation

---

#### **Task 9.2: HealthKit Integration**
**Responsável:** iOS Developer  
**Estimativa:** 8 horas  
**Prioridade:** P2 (Média)

**HealthKitService.swift:**
```swift
import HealthKit

class HealthKitService {
    private let healthStore = HKHealthStore()
    
    func requestPermission() async -> Bool {
        guard HKHealthStore.isHealthDataAvailable() else { return false }
        
        let readTypes: Set<HKObjectType> = [
            HKObjectType.quantityType(forIdentifier: .stepCount)!,
            HKObjectType.quantityType(forIdentifier: .activeEnergyBurned)!,
            HKObjectType.quantityType(forIdentifier: .distanceWalkingRunning)!
        ]
        
        do {
            try await healthStore.requestAuthorization(toShare: [], read: readTypes)
            return true
        } catch {
            print("HealthKit authorization failed: \(error)")
            return false
        }
    }
    
    func getActivityData() async throws -> HealthData {
        let calendar = Calendar.current
        let now = Date()
        let startOfDay = calendar.startOfDay(for: now)
        
        let steps = try await getStepCount(from: startOfDay, to: now)
        let calories = try await getActiveCalories(from: startOfDay, to: now)
        let distance = try await getWalkingDistance(from: startOfDay, to: now)
        
        return HealthData(
            steps: Int(steps),
            activeCalories: Int(calories),
            walkingDistance: distance
        )
    }
}

struct HealthData {
    let steps: Int
    let activeCalories: Int
    let walkingDistance: Double // in kilometers
    
    var activityScore: Double {
        // Calculate activity score based on WHO recommendations
        let stepScore = min(Double(steps) / 10000.0, 1.0) * 40
        let calorieScore = min(Double(activeCalories) / 500.0, 1.0) * 30
        let distanceScore = min(walkingDistance / 5.0, 1.0) * 30
        
        return stepScore + calorieScore + distanceScore
    }
}
```

---

### **SEMANA 10: iOS Features & Launch**

#### **Task 10.1: Siri Shortcuts**
**Responsável:** iOS Developer  
**Estimativa:** 6 horas  
**Prioridade:** P3 (Baixa)

**SiriShortcuts.swift:**
```swift
import Intents
import IntentsUI

class SiriShortcutsService {
    func setupShortcuts() {
        let openAppIntent = OpenAppIntent()
        openAppIntent.suggestedInvocationPhrase = "Abrir FypMatch"
        
        let shortcut = INShortcut(intent: openAppIntent)
        let relevantShortcut = INRelevantShortcut(shortcut: shortcut)
        relevantShortcut.shortcutRole = .action
        
        INRelevantShortcutStore.default.setRelevantShortcuts([relevantShortcut]) { error in
            if let error = error {
                print("Failed to set relevant shortcuts: \(error)")
            }
        }
    }
}

// Custom Intent
class OpenAppIntent: INIntent {
    // Implementation
}
```

---

#### **Task 10.2: iOS Widgets**
**Responsável:** iOS Developer  
**Estimativa:** 10 horas  
**Prioridade:** P2 (Média)

**MatchCounterWidget:**
```swift
import WidgetKit
import SwiftUI

struct MatchCounterWidget: Widget {
    let kind: String = "MatchCounterWidget"
    
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: MatchCounterProvider()) { entry in
            MatchCounterWidgetView(entry: entry)
        }
        .configurationDisplayName("FypMatch")
        .description("Veja seus matches do dia")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

struct MatchCounterWidgetView: View {
    var entry: MatchCounterProvider.Entry
    
    var body: some View {
        VStack {
            HStack {
                Image("fypmatch_logo")
                    .resizable()
                    .frame(width: 24, height: 24)
                
                Text("FypMatch")
                    .font(.headline)
                    .fontWeight(.bold)
                
                Spacer()
            }
            
            Spacer()
            
            VStack(spacing: 4) {
                Text("\(entry.matchCount)")
                    .font(.system(size: 32, weight: .bold))
                    .foregroundColor(.pink)
                
                Text("matches hoje")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
        }
        .padding()
        .background(Color(.systemBackground))
    }
}
```

---

#### **Task 10.3: App Store Preparation**
**Responsável:** iOS Developer + Marketing  
**Estimativa:** 16 horas  
**Prioridade:** P0 (Crítica)

**App Store Assets:**
```
App Store Materials/
├── Screenshots/
│   ├── iPhone 6.7"/
│   ├── iPhone 6.5"/
│   ├── iPhone 5.5"/
│   └── iPad Pro/
├── App Icon/
│   ├── 1024x1024.png
│   └── various sizes
├── Marketing Materials/
│   ├── App Description (PT-BR)
│   ├── Keywords
│   ├── Privacy Policy URL
│   └── Terms of Service URL
└── Metadata/
    ├── Version 1.0
    ├── Age Rating: 17+
    ├── Category: Social Networking
    └── Price: Free with IAP
```

**Privacy Manifest (PrivacyInfo.xcprivacy):**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>NSPrivacyCollectedDataTypes</key>
    <array>
        <dict>
            <key>NSPrivacyCollectedDataType</key>
            <string>NSPrivacyCollectedDataTypeLocation</string>
            <key>NSPrivacyCollectedDataTypeLinked</key>
            <true/>
            <key>NSPrivacyCollectedDataTypeTracking</key>
            <false/>
            <key>NSPrivacyCollectedDataTypePurposes</key>
            <array>
                <string>NSPrivacyCollectedDataTypePurposeAppFunctionality</string>
            </array>
        </dict>
    </array>
</dict>
</plist>
```

**TestFlight Setup:**
- Internal testing group
- External testing (100 users)
- App Review submission
- Version 1.0 release

---

## 📈 **METRICAS & KPIs**

### **Sprint 1 Success Metrics:**
- [ ] Build time < 30s
- [ ] 0 compiler warnings
- [ ] Firebase connection < 2s
- [ ] 15+ models implemented

### **Sprint 2 Success Metrics:**
- [ ] Login success rate > 95%
- [ ] Profile completion flow < 5 min
- [ ] 0 authentication errors in testing

### **Sprint 3 Success Metrics:**
- [ ] Swipe gesture response < 100ms
- [ ] Card animations 60fps
- [ ] Match detection < 1s

### **Sprint 4 Success Metrics:**
- [ ] Message delivery < 2s
- [ ] Real-time sync working
- [ ] Push notification delivery > 90%

### **Sprint 5 Success Metrics:**
- [ ] In-app purchase flow working
- [ ] App Store submission accepted
- [ ] TestFlight ready

**🎯 Com essas tasks detalhadas, o desenvolvedor iOS terá um guia completo para implementação!**