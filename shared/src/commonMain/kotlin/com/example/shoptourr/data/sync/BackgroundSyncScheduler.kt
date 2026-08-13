package com.example.shoptourr.data.sync

/**
 * Drain + cache eviction entry used by WorkManager / BGTaskScheduler.
 */
interface BackgroundSyncScheduler {
    fun schedule()
}

class NoOpBackgroundSyncScheduler : BackgroundSyncScheduler {
    override fun schedule() = Unit
}
