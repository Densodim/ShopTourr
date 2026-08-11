package com.example.shoptourr.data.push

/**
 * Cross-platform holder for the latest OS push token.
 * Android writes FCM tokens; iOS writes APNs device tokens from the app delegate.
 */
object DevicePushTokenHolder {
    private var cached: String? = null

    val token: String?
        get() = cached

    fun update(token: String?) {
        cached = token?.trim()?.ifEmpty { null }
    }
}
