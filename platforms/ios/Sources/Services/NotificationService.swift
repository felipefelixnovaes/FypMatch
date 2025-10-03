//
//  NotificationService.swift
//  FypMatch iOS
//
//  Serviço de notificações push e locais
//  Gerencia Firebase Cloud Messaging e notificações locais
//

import Foundation
import UserNotifications
import FirebaseMessaging
import Combine

/// Serviço para gerenciamento de notificações do app
@MainActor
class NotificationService: NSObject, ObservableObject {
    
    // MARK: - Singleton
    static let shared = NotificationService()
    
    // MARK: - Properties
    @Published var isAuthorized = false
    @Published var fcmToken: String?
    @Published var notificationError: String?
    
    private let userNotificationCenter = UNUserNotificationCenter.current()
    
    // MARK: - Initialization
    override private init() {
        super.init()
        setupNotifications()
    }
    
    // MARK: - Setup
    
    private func setupNotifications() {
        userNotificationCenter.delegate = self
        
        // Configure Firebase Messaging
        Messaging.messaging().delegate = self
        
        // Check current authorization status
        Task {
            await checkAuthorizationStatus()
        }
    }
    
    // MARK: - Permission Management
    
    /// Request notification permissions
    func requestPermission() async throws {
        do {
            let granted = try await userNotificationCenter.requestAuthorization(
                options: [.alert, .badge, .sound]
            )
            
            isAuthorized = granted
            
            if granted {
                // Register for remote notifications
                await UIApplication.shared.registerForRemoteNotifications()
            }
            
        } catch {
            notificationError = error.localizedDescription
            throw NotificationError.permissionDenied
        }
    }
    
    /// Check current authorization status
    func checkAuthorizationStatus() async {
        let settings = await userNotificationCenter.notificationSettings()
        isAuthorized = settings.authorizationStatus == .authorized
    }
    
    // MARK: - FCM Token Management
    
    /// Get current FCM token
    func getCurrentToken() async throws -> String {
        return try await withCheckedThrowingContinuation { continuation in
            Messaging.messaging().token { token, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let token = token {
                    continuation.resume(returning: token)
                } else {
                    continuation.resume(throwing: NotificationError.tokenNotAvailable)
                }
            }
        }
    }
    
    /// Update FCM token in Firebase
    func updateFCMToken(for userId: String) async throws {
        guard let token = fcmToken else {
            throw NotificationError.tokenNotAvailable
        }
        
        // Update token in Firestore
        try await FirebaseService.shared.updateUserDeviceToken(userId: userId, token: token)
    }
    
    /// Delete FCM token
    func deleteFCMToken() async throws {
        try await Messaging.messaging().deleteToken()
        fcmToken = nil
    }
    
    // MARK: - Local Notifications
    
    /// Schedule a local notification
    func scheduleLocalNotification(
        title: String,
        body: String,
        badge: Int? = nil,
        delay: TimeInterval = 0,
        identifier: String = UUID().uuidString,
        userInfo: [String: Any] = [:]
    ) async throws {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = .default
        content.userInfo = userInfo
        
        if let badge = badge {
            content.badge = NSNumber(value: badge)
        }
        
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: max(delay, 1), repeats: false)
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)
        
        try await userNotificationCenter.add(request)
    }
    
    /// Schedule new match notification
    func scheduleMatchNotification(matchedUserName: String, matchId: String) async throws {
        try await scheduleLocalNotification(
            title: "🎉 Novo Match!",
            body: "Você deu match com \(matchedUserName)!",
            badge: 1,
            identifier: "match_\(matchId)",
            userInfo: [
                "type": "new_match",
                "matchId": matchId
            ]
        )
    }
    
    /// Schedule new message notification
    func scheduleMessageNotification(senderName: String, messagePreview: String, conversationId: String) async throws {
        try await scheduleLocalNotification(
            title: senderName,
            body: messagePreview,
            badge: 1,
            identifier: "message_\(UUID().uuidString)",
            userInfo: [
                "type": "new_message",
                "conversationId": conversationId
            ]
        )
    }
    
    /// Cancel specific notification
    func cancelNotification(identifier: String) {
        userNotificationCenter.removePendingNotificationRequests(withIdentifiers: [identifier])
        userNotificationCenter.removeDeliveredNotifications(withIdentifiers: [identifier])
    }
    
    /// Cancel all notifications
    func cancelAllNotifications() {
        userNotificationCenter.removeAllPendingNotificationRequests()
        userNotificationCenter.removeAllDeliveredNotifications()
    }
    
    // MARK: - Badge Management
    
    /// Update app badge count
    func updateBadgeCount(_ count: Int) {
        Task {
            await UIApplication.shared.setApplicationIconBadgeNumber(count)
        }
    }
    
    /// Clear app badge
    func clearBadge() {
        updateBadgeCount(0)
    }
    
    // MARK: - Topic Subscription (FCM)
    
    /// Subscribe to FCM topic
    func subscribeToTopic(_ topic: String) async throws {
        try await Messaging.messaging().subscribe(toTopic: topic)
    }
    
    /// Unsubscribe from FCM topic
    func unsubscribeFromTopic(_ topic: String) async throws {
        try await Messaging.messaging().unsubscribe(fromTopic: topic)
    }
    
    /// Subscribe to user-specific topics
    func subscribeToUserTopics(userId: String) async throws {
        try await subscribeToTopic("user_\(userId)")
        try await subscribeToTopic("all_users")
    }
    
    /// Unsubscribe from all topics
    func unsubscribeFromAllTopics(userId: String) async throws {
        try await unsubscribeFromTopic("user_\(userId)")
        try await unsubscribeFromTopic("all_users")
    }
    
    // MARK: - Notification Settings
    
    /// Get current notification settings
    func getNotificationSettings() async -> UNNotificationSettings {
        return await userNotificationCenter.notificationSettings()
    }
    
    /// Check if notifications are enabled
    func areNotificationsEnabled() async -> Bool {
        let settings = await getNotificationSettings()
        return settings.authorizationStatus == .authorized
    }
    
    // MARK: - Handle Notification Actions
    
    /// Handle notification tap
    func handleNotificationTap(userInfo: [AnyHashable: Any]) {
        guard let notificationType = userInfo["type"] as? String else { return }
        
        switch notificationType {
        case "new_match":
            if let matchId = userInfo["matchId"] as? String {
                // Navigate to match detail
                NotificationCenter.default.post(
                    name: .navigateToMatch,
                    object: matchId
                )
            }
            
        case "new_message":
            if let conversationId = userInfo["conversationId"] as? String {
                // Navigate to chat
                NotificationCenter.default.post(
                    name: .navigateToChat,
                    object: conversationId
                )
            }
            
        case "profile_view":
            if let userId = userInfo["userId"] as? String {
                // Navigate to profile
                NotificationCenter.default.post(
                    name: .navigateToProfile,
                    object: userId
                )
            }
            
        default:
            break
        }
    }
}

// MARK: - UNUserNotificationCenterDelegate

extension NotificationService: UNUserNotificationCenterDelegate {
    
    /// Handle notification when app is in foreground
    nonisolated func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // Show notification even when app is in foreground
        completionHandler([.banner, .sound, .badge])
    }
    
    /// Handle notification tap
    nonisolated func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo
        
        Task { @MainActor in
            self.handleNotificationTap(userInfo: userInfo)
        }
        
        completionHandler()
    }
}

// MARK: - MessagingDelegate

extension NotificationService: MessagingDelegate {
    
    /// Handle FCM token refresh
    nonisolated func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        Task { @MainActor in
            self.fcmToken = fcmToken
            
            // Update token in Firebase if user is logged in
            if let userId = FirebaseService.shared.currentUser?.id,
               let token = fcmToken {
                try? await FirebaseService.shared.updateUserDeviceToken(userId: userId, token: token)
            }
        }
    }
}

// MARK: - Firebase Service Extension

extension FirebaseService {
    /// Update user device token
    func updateUserDeviceToken(userId: String, token: String) async throws {
        try await db.collection("users").document(userId).updateData([
            "deviceToken": token,
            "updatedAt": Date()
        ])
    }
}

// MARK: - Errors

enum NotificationError: Error, LocalizedError {
    case permissionDenied
    case tokenNotAvailable
    case schedulingFailed
    
    var errorDescription: String? {
        switch self {
        case .permissionDenied:
            return "Permissão de notificação negada"
        case .tokenNotAvailable:
            return "Token FCM não disponível"
        case .schedulingFailed:
            return "Falha ao agendar notificação"
        }
    }
}

// MARK: - Notification Names

extension Notification.Name {
    static let navigateToMatch = Notification.Name("navigateToMatch")
    static let navigateToChat = Notification.Name("navigateToChat")
    static let navigateToProfile = Notification.Name("navigateToProfile")
}

// MARK: - UIApplication Extension

extension UIApplication {
    @MainActor
    func registerForRemoteNotifications() {
        UIApplication.shared.registerForRemoteNotifications()
    }
    
    @MainActor
    func setApplicationIconBadgeNumber(_ count: Int) {
        UIApplication.shared.applicationIconBadgeNumber = count
    }
}
