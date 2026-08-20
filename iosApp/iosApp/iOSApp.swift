import SwiftUI
import UIKit
import Foundation
import UserNotifications
import BackgroundTasks
import Shared
import Sentry

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    static let syncTaskId = "com.shoptourr.sync"

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        BGTaskScheduler.shared.register(forTaskWithIdentifier: Self.syncTaskId, using: nil) { task in
            Self.handleSync(task: task)
        }
        Self.scheduleSync()
        Self.configureSentry()
        IosSocialAuthCoordinator.shared.configure()
        Self.configurePushPermission()
        return true
    }

    static func configurePushPermission() {
        IosNotificationPermissionKt.registerIosNotificationPermission(
            impl: IosNotificationPermission { callback in
                let center = UNUserNotificationCenter.current()
                center.getNotificationSettings { settings in
                    switch settings.authorizationStatus {
                    case .authorized, .provisional, .ephemeral:
                        DispatchQueue.main.async {
                            UIApplication.shared.registerForRemoteNotifications()
                        }
                        callback(true)
                    case .denied:
                        callback(false)
                    default:
                        center.requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
                            if granted {
                                DispatchQueue.main.async {
                                    UIApplication.shared.registerForRemoteNotifications()
                                }
                            }
                            callback(granted)
                        }
                    }
                }
            }
        )
    }

    static func configureSentry() {
        let plist = (Bundle.main.object(forInfoDictionaryKey: "SENTRY_DSN") as? String)?
            .trimmingCharacters(in: .whitespacesAndNewlines)
        let env = ProcessInfo.processInfo.environment["SENTRY_DSN"]?
            .trimmingCharacters(in: .whitespacesAndNewlines)
        guard let dsn = [plist, env].compactMap({ $0 }).first(where: { !$0.isEmpty }) else {
            return
        }
        SentrySDK.start { options in
            options.dsn = dsn
        }
        IosSentryBridge.shared.exceptionCapture = IosSentryExceptionCapture { message, stack in
            SentrySDK.capture(message: message) { scope in
                scope.setExtra(value: stack, key: "stack")
            }
        }
        IosSentryBridge.shared.breadcrumbCapture = IosSentryBreadcrumbCapture { message, category in
            let crumb = Breadcrumb()
            crumb.message = message
            crumb.category = category
            SentrySDK.addBreadcrumb(crumb)
        }
        IosSentryBridge.shared.tagSetter = IosSentryTagSetter { key, value in
            SentrySDK.configureScope { scope in
                scope.setTag(value: value, key: key)
            }
        }
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
                .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { activity in
                    if let url = activity.webpageURL {
                        DeepLinkIntakeKt.offerPendingDeepLinkUri(uri: url.absoluteString)
                    }
                }
        }
    }
}
