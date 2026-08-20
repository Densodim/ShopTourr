package com.example.shoptourr.data.push

import android.Manifest
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.example.shoptourr.data.auth.AndroidAuthHost
import com.example.shoptourr.domain.push.NotificationPermissionGate
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object AndroidNotificationPermissionHost {
    @Volatile
    var launcher: ((String) -> Unit)? = null

    @Volatile
    private var pending: ((Boolean) -> Unit)? = null

    fun complete(granted: Boolean) {
        val callback = pending
        pending = null
        callback?.invoke(granted)
    }

    suspend fun requestPostNotifications(): Boolean = suspendCancellableCoroutine { continuation ->
        val launch = launcher
        if (launch == null) {
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }
        pending = { granted ->
            if (continuation.isActive) continuation.resume(granted)
        }
        continuation.invokeOnCancellation { pending = null }
        launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

class AndroidNotificationPermissionGate : NotificationPermissionGate {
    override suspend fun ensureGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        val activity = AndroidAuthHost.currentActivity() ?: return false
        val granted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return true
        return AndroidNotificationPermissionHost.requestPostNotifications()
    }
}

actual fun createNotificationPermissionGate(): NotificationPermissionGate =
    AndroidNotificationPermissionGate()
