package com.example.shoptourr.data.push

import com.example.shoptourr.domain.push.NotificationPermissionGate
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun interface IosNotificationPermission {
    fun ensure(callback: (granted: Boolean) -> Unit)
}

object IosNotificationPermissionBridge {
    var impl: IosNotificationPermission? = null
}

fun registerIosNotificationPermission(impl: IosNotificationPermission) {
    IosNotificationPermissionBridge.impl = impl
}

/** Swift cannot construct Kotlin fun interfaces; pass a closure instead. */
fun registerIosNotificationPermissionBlock(ensure: (callback: (Boolean) -> Unit) -> Unit) {
    IosNotificationPermissionBridge.impl = IosNotificationPermission { callback -> ensure(callback) }
}

class IosNotificationPermissionGate : NotificationPermissionGate {
    override suspend fun ensureGranted(): Boolean = suspendCancellableCoroutine { continuation ->
        val impl = IosNotificationPermissionBridge.impl
        if (impl == null) {
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }
        impl.ensure { granted ->
            if (continuation.isActive) continuation.resume(granted)
        }
    }
}
