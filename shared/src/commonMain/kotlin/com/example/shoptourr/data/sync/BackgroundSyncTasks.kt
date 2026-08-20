package com.example.shoptourr.data.sync

import com.example.shoptourr.analytics.Analytics
import com.example.shoptourr.domain.usecase.DrainSyncOutboxUseCase
import com.example.shoptourr.domain.usecase.EvictLocalCacheUseCase
import org.koin.mp.KoinPlatform.getKoinOrNull

object BackgroundSyncTasks {
    suspend fun drainAndEvict(): Boolean {
        val koin = getKoinOrNull() ?: return false
        runCatching { koin.get<EvictLocalCacheUseCase>().invoke() }
        val drain = koin.get<DrainSyncOutboxUseCase>().invoke()
        runCatching { koin.get<Analytics>().flush() }
        return drain.getOrNull()?.failureCount == 0
    }
}
