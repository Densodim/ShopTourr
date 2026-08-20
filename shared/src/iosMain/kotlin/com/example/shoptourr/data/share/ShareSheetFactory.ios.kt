package com.example.shoptourr.data.share

import com.example.shoptourr.domain.share.ShareSheet
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

class IosShareSheet : ShareSheet {
    @OptIn(ExperimentalForeignApi::class)
    override fun shareText(text: String) {
        val root = keyViewController() ?: return
        val controller = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )
        controller.popoverPresentationController?.sourceView = root.view
        root.presentViewController(controller, animated = true, completion = null)
    }
}

private fun keyViewController(): UIViewController? {
    val window = keyWindow() ?: return null
    var current = window.rootViewController
    while (current?.presentedViewController != null) {
        current = current?.presentedViewController
    }
    return current
}

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

actual fun createShareSheet(): ShareSheet = IosShareSheet()
