package com.example.shoptourr.analytics

import kotlin.random.Random
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Product analytics facade. Default is [NoOpAnalytics];
 * [QueuedAnalytics] buffers offline and flushes to PostHog/Firebase later.
 */
interface Analytics {
    suspend fun track(name: String, properties: Map<String, String> = emptyMap())
    fun identify(userId: String?)
    suspend fun flush()
}

object NoOpAnalytics : Analytics {
    override suspend fun track(name: String, properties: Map<String, String>) = Unit
    override fun identify(userId: String?) = Unit
    override suspend fun flush() = Unit
}

data class AnalyticsEvent(
    val id: String,
    val name: String,
    val properties: Map<String, String> = emptyMap(),
    val timestampEpochMs: Long,
)

interface AnalyticsEventQueue {
    suspend fun enqueue(event: AnalyticsEvent)
    suspend fun pending(): List<AnalyticsEvent>
    suspend fun removeAll(events: List<AnalyticsEvent>)
    suspend fun size(): Int
    suspend fun clearAll()
}

class InMemoryAnalyticsEventQueue(
    private val maxPending: Int = 500,
) : AnalyticsEventQueue {
    private val events = ArrayDeque<AnalyticsEvent>()
    private val mutex = Mutex()

    override suspend fun enqueue(event: AnalyticsEvent) = mutex.withLock {
        if (events.size >= maxPending) {
            events.removeFirst()
        }
        events.addLast(event)
    }

    override suspend fun pending(): List<AnalyticsEvent> = mutex.withLock { events.toList() }

    override suspend fun removeAll(events: List<AnalyticsEvent>) {
        mutex.withLock {
            val ids = events.map { it.id }.toSet()
            this.events.removeAll { it.id in ids }
        }
    }

    override suspend fun size(): Int = mutex.withLock { events.size }

    override suspend fun clearAll() = mutex.withLock {
        events.clear()
    }
}

interface AnalyticsSink {
    suspend fun send(events: List<AnalyticsEvent>): Result<Unit>
    fun identify(userId: String?)
}

object NoOpAnalyticsSink : AnalyticsSink {
    override suspend fun send(events: List<AnalyticsEvent>): Result<Unit> = Result.success(Unit)
    override fun identify(userId: String?) = Unit
}

class QueuedAnalytics(
    private val queue: AnalyticsEventQueue,
    private val sink: AnalyticsSink,
    private val isOnline: () -> Boolean,
    private val clock: () -> Long,
    private val idGenerator: () -> String = ::newAnalyticsEventId,
) : Analytics {
    private var pendingUserId: String? = null
    private var hasPendingIdentify: Boolean = false

    override suspend fun track(name: String, properties: Map<String, String>) {
        queue.enqueue(
            AnalyticsEvent(
                id = idGenerator(),
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

    suspend fun pendingCount(): Int = queue.size()
}

class RecordingAnalytics : Analytics {
    val events = mutableListOf<AnalyticsEvent>()
    var lastUserId: String? = null
        private set

    override suspend fun track(name: String, properties: Map<String, String>) {
        events += AnalyticsEvent(
            id = "rec-${events.size}",
            name = name,
            properties = properties,
            timestampEpochMs = 0L,
        )
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

internal val analyticsPropertiesJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

internal fun encodeAnalyticsProperties(properties: Map<String, String>): String =
    analyticsPropertiesJson.encodeToString(
        MapSerializer(String.serializer(), String.serializer()),
        properties,
    )

internal fun decodeAnalyticsProperties(json: String): Map<String, String> =
    if (json.isBlank()) {
        emptyMap()
    } else {
        analyticsPropertiesJson.decodeFromString(
            MapSerializer(String.serializer(), String.serializer()),
            json,
        )
    }

fun newAnalyticsEventId(): String =
    buildString(16) {
        repeat(16) { append(Random.nextInt(0, 16).toString(16)) }
    }
