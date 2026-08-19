package com.example.shoptourr.ui.privacy

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

private val androidSecureScreenGate = SecureScreenGate()

@Composable
actual fun SecureScreenCapture(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(enabled) {
        if (!enabled) {
            return@DisposableEffect onDispose { }
        }
        val window = view.context.findActivity()?.window
        if (androidSecureScreenGate.acquire()) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose {
            if (androidSecureScreenGate.release()) {
                window.clearSecureFlag()
            }
        }
    }
}

private fun Window?.clearSecureFlag() {
    this?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
