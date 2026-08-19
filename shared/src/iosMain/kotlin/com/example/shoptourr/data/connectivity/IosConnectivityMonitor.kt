package com.example.shoptourr.data.connectivity

import com.example.shoptourr.domain.connectivity.ConnectivityMonitor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.concurrent.Volatile
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_global_queue
import platform.posix.QOS_CLASS_UTILITY

/**
 * iOS connectivity via Network framework C API (NWPathMonitor under the hood).
 * Swift `NWPathMonitor` is not directly exposed to Kotlin/Native.
 */
@OptIn(ExperimentalForeignApi::class)
class IosConnectivityMonitor : ConnectivityMonitor {
    @Volatile
    private var lastOnline: Boolean = true

    override fun currentIsOnline(): Boolean = lastOnline

    override fun observeIsOnline(): Flow<Boolean> = callbackFlow {
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_update_handler(monitor) { path ->
            val online = path != null && nw_path_get_status(path) == nw_path_status_satisfied
            lastOnline = online
            trySend(online)
        }
        val queue = dispatch_get_global_queue(QOS_CLASS_UTILITY.toLong(), 0u)
        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_start(monitor)
        awaitClose {
            nw_path_monitor_cancel(monitor)
        }
    }.distinctUntilChanged()
}
