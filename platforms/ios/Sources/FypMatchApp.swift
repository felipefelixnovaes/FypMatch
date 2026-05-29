// FypMatchApp.swift — FypMatch iOS
// Entry point — gerado pelo @fullstack-chief

import SwiftUI
import Firebase
import FirebaseMessaging
import ComposableArchitecture

@main
struct FypMatchApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            AppView()
                .onOpenURL { url in
                    guard let components = URLComponents(url: url, resolvingAgainstBaseURL: true) else { return }
                    switch components.host {
                    case "match":
                        let id = components.queryItems?.first(where: { $0.name == "id" })?.value ?? ""
                        NotificationService.shared.pendingDeepLink = .match(id: id)
                    case "conversation":
                        let id = components.queryItems?.first(where: { $0.name == "id" })?.value ?? ""
                        NotificationService.shared.pendingDeepLink = .conversation(id: id)
                    default: break
                    }
                }
        }
    }
}

// MARK: - AppDelegate
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        FirebaseApp.configure()
        NotificationService.shared.setup()
        UITabBar.appearance().tintColor = UIColor(red: 0.914, green: 0.118, blue: 0.388, alpha: 1)
        UINavigationBar.appearance().tintColor = UIColor(red: 0.914, green: 0.118, blue: 0.388, alpha: 1)
        return true
    }
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }
}
