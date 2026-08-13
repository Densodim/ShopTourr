package com.example.shoptourr.data.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val iosBackgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

fun drainOutboxFromBackground(onComplete: (Boolean) -> Unit) {
    iosBackgroundScope.launch {
        onComplete(BackgroundSyncTasks.drainAndEvict())
    }
}
