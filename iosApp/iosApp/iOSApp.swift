import SwiftUI
import UIKit
import UserNotifications
import BackgroundTasks
import Shared

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    static let syncTaskId = "com.shoptourr.sync"

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
            guard granted else { return }
            DispatchQueue.main.async {
                application.registerForRemoteNotifications()
            }
        }
        BGTaskScheduler.shared.register(forTaskWithIdentifier: Self.syncTaskId, using: nil) { task in
            Self.handleSync(task: task)
        }
        Self.scheduleSync()
        return true
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        let token = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        DevicePushTokenHolder.shared.update(token: token)
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        DevicePushTokenHolder.shared.update(token: nil)
    }

    static func handleSync(task: BGTask) {
        scheduleSync()
        IosBackgroundSyncKt.drainOutboxFromBackground { success in
            task.setTaskCompleted(success: success.boolValue)
        }
        task.expirationHandler = {
            task.setTaskCompleted(success: false)
        }
    }

    static func scheduleSync() {
        let request = BGAppRefreshTaskRequest(identifier: syncTaskId)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 15 * 60)
        try? BGTaskScheduler.shared.submit(request)
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    DeepLinkIntakeKt.offerPendingDeepLinkUri(uri: url.absoluteString)
                }
        }
    }
}
