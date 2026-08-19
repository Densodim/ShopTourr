package com.example.shoptourr.ui.privacy

/**
 * When to cover receipt UI: iOS cannot set FLAG_SECURE, so we hide content during
 * AirPlay/screen recording and while the app is in the switcher.
 */
object SecureScreenPolicy {
    fun hideSensitiveContent(
        enabled: Boolean,
        screenCaptured: Boolean,
        appInBackground: Boolean,
    ): Boolean = enabled && (screenCaptured || appInBackground)
}

/**
 * Nested [SecureScreenCapture] calls (form + receipt image) share one window flag
 * / overlay. The first acquire turns protection on; the last release turns it off.
 */
class SecureScreenGate {
    private var holders: Int = 0

    fun acquire(): Boolean {
        holders += 1
        return holders == 1
    }

    fun release(): Boolean {
        if (holders == 0) return false
        holders -= 1
        return holders == 0
    }
}
