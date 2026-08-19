package com.example.shoptourr.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class AnalyticsTest {

    @Test
    fun `recording track stores name and properties`() = runTest {
        val analytics = RecordingAnalytics()
        analytics.track("purchase_created", mapOf("trip_id" to "t1"))
        assertEquals(1, analytics.events.size)
        assertEquals("purchase_created", analytics.events.single().name)
        assertEquals("t1", analytics.events.single().properties["trip_id"])
    }

    @Test
    fun `noop does not throw`() = runTest {
        NoOpAnalytics.track("screen_view")
        NoOpAnalytics.identify("user-1")
        assertTrue(true)
    }

    @Test
    fun `queued analytics keeps events offline and flushes when online`() = runTest {
        val sink = RecordingAnalyticsSink()
        var online = false
        val analytics = QueuedAnalytics(
            queue = InMemoryAnalyticsEventQueue(),
            sink = sink,
            isOnline = { online },
            clock = { 1_700_000_000_000L },
            idGenerator = { "evt-1" },
            consent = { true },
        )

        analytics.track("home_opened", mapOf("tab" to "home"))
        analytics.flush()
        assertEquals(0, sink.batches.size)
        assertEquals(1, analytics.pendingCount())

        online = true
        analytics.flush()
        assertEquals(1, sink.batches.size)
        assertEquals("home_opened", sink.batches.single().single().name)
        assertEquals(0, analytics.pendingCount())
    }

    @Test
    fun `queued analytics retains events when sink fails`() = runTest {
        val sink = FailingAnalyticsSink()
        val analytics = QueuedAnalytics(
            queue = InMemoryAnalyticsEventQueue(),
            sink = sink,
            isOnline = { true },
            clock = { 42L },
            idGenerator = { "evt-fail" },
            consent = { true },
        )
        analytics.track("export_tapped")
        analytics.flush()
        assertEquals(1, analytics.pendingCount())
    }

    @Test
    fun `identify is forwarded to sink on flush`() = runTest {
        val sink = RecordingAnalyticsSink()
        val analytics = QueuedAnalytics(
            queue = InMemoryAnalyticsEventQueue(),
            sink = sink,
            isOnline = { true },
            clock = { 1L },
            consent = { true },
        )
        analytics.identify("u-9")
        analytics.flush()
        assertEquals("u-9", sink.lastUserId)
    }

    @Test
    fun `without consent events are not queued and identify is not sent`() = runTest {
        val sink = RecordingAnalyticsSink()
        val analytics = QueuedAnalytics(
            queue = InMemoryAnalyticsEventQueue(),
            sink = sink,
            isOnline = { true },
            clock = { 1L },
            idGenerator = { "evt-denied" },
            consent = { false },
        )
        analytics.track("home_opened")
        analytics.identify("u-9")
        analytics.flush()
        assertEquals(0, analytics.pendingCount())
        assertEquals(0, sink.batches.size)
        assertEquals(null, sink.lastUserId)
    }

    @Test
    fun `granting consent later forwards a pending identify`() = runTest {
        val sink = RecordingAnalyticsSink()
        var granted = false
        val analytics = QueuedAnalytics(
            queue = InMemoryAnalyticsEventQueue(),
            sink = sink,
            isOnline = { true },
            clock = { 1L },
            consent = { granted },
        )
        analytics.identify("u-9")
        analytics.flush()
        assertEquals(null, sink.lastUserId)
        granted = true
        analytics.flush()
        assertEquals("u-9", sink.lastUserId)
    }

    @Test
    fun `properties round-trip through json helpers`() {
        val encoded = encodeAnalyticsProperties(mapOf("a" to "1", "b" to "two"))
        assertEquals(mapOf("a" to "1", "b" to "two"), decodeAnalyticsProperties(encoded))
    }
}
