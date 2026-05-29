// NotificationService.swift — FypMatch iOS
// Push notifications via FCM + APNs

import Foundation
import FirebaseMessaging
import UserNotifications
import UIKit

@MainActor
class NotificationService: NSObject, ObservableObject, UNUserNotificationCenterDelegate, MessagingDelegate {
    static let shared = NotificationService()

    @Published var fcmToken: String?
    @Published var pendingDeepLink: DeepLink?

    // MARK: - Setup

    func setup() {
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        requestPermission()
    }

    func requestPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
            guard granted else { return }
            DispatchQueue.main.async {
                UIApplication.shared.registerForRemoteNotifications()
            }
        }
    }

    // MARK: - FCM Token

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        self.fcmToken = fcmToken
        guard let token = fcmToken, let userId = FirebaseService.shared.currentUser?.id else { return }
        // Save token to Firestore
        let db = Firestore.firestore()
        db.collection("users").document(userId).updateData(["deviceToken": token])
    }

    // MARK: - Notification handling

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                 willPresent notification: UNNotification,
                                 withCompletionHandler handler: @escaping (UNNotificationPresentationOptions) -> Void) {
        handler([.banner, .badge, .sound])
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                 didReceive response: UNNotificationResponse,
                                 withCompletionHandler handler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo
        handleDeepLink(from: userInfo)
        handler()
    }

    // MARK: - Deep Links

    private func handleDeepLink(from userInfo: [AnyHashable: Any]) {
        guard let type = userInfo["type"] as? String else { return }
        switch type {
        case "new_match":
            if let matchId = userInfo["matchId"] as? String {
                pendingDeepLink = .match(id: matchId)
            }
        case "new_message":
            if let conversationId = userInfo["conversationId"] as? String {
                pendingDeepLink = .conversation(id: conversationId)
            }
        case "boost_expiring":
            pendingDeepLink = .discovery
        default:
            break
        }
    }

    // MARK: - Schedule Local Notifications (for retention)

    func scheduleRetentionNotification(type: RetentionNotificationType, afterSeconds: TimeInterval) {
        let content = UNMutableNotificationContent()
        content.sound = .default
        switch type {
        case .matchPending(let name):
            content.title = "💕 Novo match!"
            content.body = "\(name) curtiu você de volta. Diga oi!"
            content.badge = 1
        case .inactivity(let count):
            content.title = "👀 \(count) pessoas te estão esperando"
            content.body = "Você tem potenciais matches que ainda não viu."
        case .creditsEmpty:
            content.title = "🤖 Seus créditos acabaram"
            content.body = "Assista a um anúncio e ganhe 3 créditos grátis."
        case .trialOffer:
            content.title = "✨ Experimente o Premium grátis"
            content.body = "Veja quem curtiu você. 7 dias grátis agora."
        }
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: afterSeconds, repeats: false)
        let request = UNNotificationRequest(identifier: type.identifier, content: content, trigger: trigger)
        UNUserNotificationCenter.current().add(request)
    }
}

// MARK: - Models

enum DeepLink {
    case match(id: String)
    case conversation(id: String)
    case discovery
    case premium
}

enum RetentionNotificationType {
    case matchPending(name: String)
    case inactivity(count: Int)
    case creditsEmpty
    case trialOffer

    var identifier: String {
        switch self {
        case .matchPending: return "match_pending"
        case .inactivity:   return "inactivity"
        case .creditsEmpty: return "credits_empty"
        case .trialOffer:   return "trial_offer"
        }
    }
}

// Firestore import needed in this file
import FirebaseFirestore
