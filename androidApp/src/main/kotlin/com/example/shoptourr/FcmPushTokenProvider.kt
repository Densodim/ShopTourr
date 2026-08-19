package com.example.shoptourr

import android.content.Context
import com.example.shoptourr.data.push.DevicePushTokenHolder
import com.example.shoptourr.domain.model.PushPlatform
import com.example.shoptourr.domain.push.PushTokenProvider
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class FcmPushTokenProvider(
    private val context: Context,
    override val appVersion: String? = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull(),
    override val deviceName: String? = android.os.Build.MODEL,
) : PushTokenProvider {
    override val platform: PushPlatform = PushPlatform.ANDROID

    override suspend fun currentToken(): String? {
        DevicePushTokenHolder.token?.let { return it }
        return suspendCancellableCoroutine { cont ->
            // `getInstance()` and `task.result` both throw when Firebase is not
            // configured — a debug build ships a placeholder key — and the listener
            // runs on the main thread, so an escaping throw kills the process right
            // after sign-in. Push is best-effort: no token simply means no push.
            val messaging = runCatching { FirebaseMessaging.getInstance() }.getOrNull()
            if (messaging == null) {
                cont.resume(null)
                return@suspendCancellableCoroutine
            }
            messaging.token.addOnCompleteListener { task ->
                val value = if (task.isSuccessful) {
                    runCatching { task.result }.getOrNull()
                } else {
                    null
                }
                if (value != null) DevicePushTokenHolder.update(value)
                if (cont.isActive) cont.resume(value)
            }
        }
    }
}
