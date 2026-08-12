package com.example.shoptourr.analytics

/**
 * Product analytics facade. Default is [NoOpAnalytics];
 * [QueuedAnalytics] buffers offline and flushes to PostHog/Firebase later.
 */
interface Analytics {
    fun track(name: String, properties: Map<String, String> = emptyMap())
    fun identify(userId: String?)
    suspend fun flush()
}

object NoOpAnalytics : Analytics {
    override fun track(name: String, properties: Map<String, String>) = Unit
    override fun identify(userId: String?) = Unit
    override suspend fun flush() = Unit
}

data class AnalyticsEvent(
    val name: String,
    val properties: Map<String, String> = emptyMap(),
    val timestampEpochMs: Long,
)

interface AnalyticsEventQueue {
    fun enqueue(event: AnalyticsEvent)
    fun pending(): List<AnalyticsEvent>
    fun removeAll(events: List<AnalyticsEvent>)
    fun size(): Int
}

class InMemoryAnalyticsEventQueue(
    private val maxPending: Int = 500,
) : AnalyticsEventQueue {
    private val events = ArrayDeque<AnalyticsEvent>()

    override fun enqueue(event: AnalyticsEvent) {
        if (events.size >= maxPending) {
            events.removeFirst()
        }
        events.addLast(event)
    }

    override fun pending(): List<AnalyticsEvent> = events.toList()

    override fun removeAll(events: List<AnalyticsEvent>) {
        this.events.removeAll(events.toSet())
    }

    override fun size(): Int = events.size
}

interface AnalyticsSink {
    suspend fun send(events: List<AnalyticsEvent>): Result<Unit>
    fun identify(userId: String?)
}

class QueuedAnalytics(
    private val queue: AnalyticsEventQueue,
    private val sink: AnalyticsSink,
    private val isOnline: () -> Boolean,
    private val clock: () -> Long,
) : Analytics {
    @Volatile
    private var pendingUserId: String? = null
    private var hasPendingIdentify: Boolean = false

    override fun track(name: String, properties: Map<String, String>) {
        queue.enqueue(
            AnalyticsEvent(
                name = name,
                properties = properties,
                timestampEpochMs = clock(),
            ),
        )
    }

    override fun identify(userId: String?) {
        pendingUserId = userId
        hasPendingIdentify = true
    }

    override suspend fun flush() {
        if (!isOnline()) return
        if (hasPendingIdentify) {
            sink.identify(pendingUserId)
            hasPendingIdentify = false
        }
        val batch = queue.pending()
        if (batch.isEmpty()) return
        val result = sink.send(batch)
        if (result.isSuccess) {
            queue.removeAll(batch)
        }
    }

    fun pendingCount(): Int = queue.size()
}

class RecordingAnalytics : Analytics {
    val events = mutableListOf<AnalyticsEvent>()
    var lastUserId: String? = null
        private set

    override fun track(name: String, properties: Map<String, String>) {
        events += AnalyticsEvent(name = name, properties = properties, timestampEpochMs = 0L)
    }

    override fun identify(userId: String?) {
        lastUserId = userId
    }

    override suspend fun flush() = Unit
}

class RecordingAnalyticsSink : AnalyticsSink {
    val batches = mutableListOf<List<AnalyticsEvent>>()
    var lastUserId: String? = null
        private set

    override suspend fun send(events: List<AnalyticsEvent>): Result<Unit> {
        batches += events
        return Result.success(Unit)
    }

    override fun identify(userId: String?) {
        lastUserId = userId
    }
}

class FailingAnalyticsSink : AnalyticsSink {
    override suspend fun send(events: List<AnalyticsEvent>): Result<Unit> =
        Result.failure(IllegalStateException("sink down"))

    override fun identify(userId: String?) = Unit
}
