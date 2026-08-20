package com.example.shoptourr.data

import com.example.shoptourr.analytics.Analytics
import com.example.shoptourr.data.sync.SyncScheduler
import com.example.shoptourr.domain.model.SyncDrainResult
import com.example.shoptourr.domain.usecase.DrainSyncOutboxUseCase
import com.example.shoptourr.fake.FakeConnectivityMonitor
import com.example.shoptourr.fake.FakeSyncRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SyncSchedulerTest {

    @Test
    fun `drains when connectivity becomes online`() = runTest(UnconfinedTestDispatcher()) {
        val sync = FakeSyncRepository(Result.success(SyncDrainResult(1, 0)))
        val connectivity = FakeConnectivityMonitor(initiallyOnline = false)
        val scheduler = SyncScheduler(
            connectivity = connectivity,
            drainSyncOutbox = DrainSyncOutboxUseCase(sync),
        )
        scheduler.start(backgroundScope)
        assertEquals(0, sync.drainCalls)

        connectivity.setOnline(true)
        assertTrue(sync.drainCalls >= 1)

        scheduler.stop()
    }

    @Test
    fun `drains immediately when already online`() = runTest(UnconfinedTestDispatcher()) {
        val sync = FakeSyncRepository()
        val connectivity = FakeConnectivityMonitor(initiallyOnline = true)
        val scheduler = SyncScheduler(
            connectivity = connectivity,
            drainSyncOutbox = DrainSyncOutboxUseCase(sync),
        )
        scheduler.start(backgroundScope)
        advanceUntilIdle()
        assertEquals(1, sync.drainCalls)
        scheduler.stop()
    }

    @Test
    fun `flushes analytics when connectivity becomes online`() = runTest(UnconfinedTestDispatcher()) {
        val analytics = CountingAnalytics()
        val connectivity = FakeConnectivityMonitor(initiallyOnline = false)
        val scheduler = SyncScheduler(
            connectivity = connectivity,
            drainSyncOutbox = DrainSyncOutboxUseCase(FakeSyncRepository()),
            analytics = analytics,
        )
        scheduler.start(backgroundScope)
        assertEquals(0, analytics.flushCalls)

        connectivity.setOnline(true)
        assertTrue(analytics.flushCalls >= 1)

        scheduler.stop()
    }
}

private class CountingAnalytics : Analytics {
    var flushCalls: Int = 0

    override suspend fun track(name: String, properties: Map<String, String>) = Unit

    override fun identify(userId: String?) = Unit

    override suspend fun flush() {
        flushCalls += 1
    }
}
