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
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    val value = task.result?.takeIf { task.isSuccessful }
                    if (value != null) DevicePushTokenHolder.update(value)
                    if (cont.isActive) cont.resume(value)
                }
        }
    }
}
