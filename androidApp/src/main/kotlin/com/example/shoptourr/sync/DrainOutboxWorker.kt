package com.example.shoptourr.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.shoptourr.data.sync.BackgroundSyncScheduler
import com.example.shoptourr.data.sync.BackgroundSyncTasks
import java.util.concurrent.TimeUnit

class DrainOutboxWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val ok = BackgroundSyncTasks.drainAndEvict()
        return if (ok) Result.success() else Result.retry()
    }
}

class AndroidBackgroundSyncScheduler(
    private val context: Context,
) : BackgroundSyncScheduler {
    override fun schedule() {
        val request = PeriodicWorkRequestBuilder<DrainOutboxWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private companion object {
        const val UNIQUE_WORK = "voyage-outbox-drain"
    }
}
