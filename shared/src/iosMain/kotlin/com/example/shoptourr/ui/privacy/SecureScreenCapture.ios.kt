package com.example.shoptourr.ui.privacy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationWillResignActiveNotification
import platform.UIKit.UIColor
import platform.UIKit.UIScreen
import platform.UIKit.UIScreenCapturedDidChangeNotification
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIView
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObjectProtocol

private const val COVER_TAG = 0x53454352L // 'SECR'
private val iosSecureScreenGate = SecureScreenGate()
private val iosSecureScreenController = IosSecureScreenController()

@Composable
actual fun SecureScreenCapture(enabled: Boolean) {
    DisposableEffect(enabled) {
        if (!enabled) {
            return@DisposableEffect onDispose { }
        }
        if (iosSecureScreenGate.acquire()) {
            iosSecureScreenController.start()
        }
        onDispose {
            if (iosSecureScreenGate.release()) {
                iosSecureScreenController.stop()
            }
        }
    }
}

/**
 * Covers the key window while iOS is recording the screen or the app is in the
 * switcher. Consumer iOS cannot block the screenshot shutter the way FLAG_SECURE
 * does on Android; hiding recents + AirPlay is the equivalent we can enforce.
 */
private class IosSecureScreenController {
    private val center = NSNotificationCenter.defaultCenter
    private var observers: List<NSObjectProtocol> = emptyList()
    private var appInBackground: Boolean = false

    fun start() {
        syncCover()
        observers = listOf(
            observe(UIScreenCapturedDidChangeNotification) { syncCover() },
            observe(UIApplicationWillResignActiveNotification) {
                appInBackground = true
                syncCover()
            },
            observe(UIApplicationDidBecomeActiveNotification) {
                appInBackground = false
                syncCover()
            },
        )
    }

    fun stop() {
        observers.forEach { center.removeObserver(it) }
        observers = emptyList()
        appInBackground = false
        coverWindow(show = false)
    }

    private fun observe(
        name: String?,
        block: () -> Unit,
    ): NSObjectProtocol =
        center.addObserverForName(
            name = name,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { _ -> block() }

    private fun syncCover() {
        val hide = SecureScreenPolicy.hideSensitiveContent(
            enabled = true,
            screenCaptured = UIScreen.mainScreen.isCaptured(),
            appInBackground = appInBackground,
        )
        coverWindow(show = hide)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun coverWindow(show: Boolean) {
    val window = keyWindow() ?: return
    val existing = window.viewWithTag(COVER_TAG)
    if (!show) {
        existing?.removeFromSuperview()
        return
    }
    if (existing != null) {
        window.bringSubviewToFront(existing)
        return
    }
    val cover = UIView(frame = window.bounds)
    cover.tag = COVER_TAG
    cover.backgroundColor = UIColor.colorWithRed(
        red = 0xF1 / 255.0,
        green = 0xED / 255.0,
        blue = 0xE4 / 255.0,
        alpha = 1.0,
    )
    cover.autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
    window.addSubview(cover)
}

@OptIn(ExperimentalForeignApi::class)
private fun keyWindow(): UIWindow? {
    val app = UIApplication.sharedApplication
    val scenes = app.connectedScenes.mapNotNull { it as? UIWindowScene }
    val foreground = scenes.firstOrNull {
        it.activationState == UISceneActivationStateForegroundActive
    } ?: scenes.firstOrNull()
    val fromScene = foreground?.windows
        ?.mapNotNull { it as? UIWindow }
        ?.firstOrNull { it.isKeyWindow() }
    if (fromScene != null) return fromScene
    @Suppress("DEPRECATION")
    return app.windows.mapNotNull { it as? UIWindow }.firstOrNull { it.isKeyWindow() }
}
