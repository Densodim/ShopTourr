package com.example.shoptourr.analytics

import com.example.shoptourr.db.VoyageDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class SqlDelightAnalyticsEventQueue(
    private val db: VoyageDatabase,
    private val maxPending: Int = 500,
) : AnalyticsEventQueue {

    override suspend fun enqueue(event: AnalyticsEvent) = withContext(Dispatchers.IO) {
        db.analyticsEventEntityQueries.insert(
            id = event.id,
            name = event.name,
            properties_json = encodeAnalyticsProperties(event.properties),
            timestamp_epoch_ms = event.timestampEpochMs,
        )
        val overflow = pendingCountUnlocked() - maxPending
        if (overflow > 0) {
            db.analyticsEventEntityQueries.selectOldestIds(overflow.toLong())
                .executeAsList()
                .forEach { id -> db.analyticsEventEntityQueries.deleteById(id) }
        }
    }

    override suspend fun pending(): List<AnalyticsEvent> = withContext(Dispatchers.IO) {
        db.analyticsEventEntityQueries.selectPending().executeAsList().map { it.toDomain() }
    }

    override suspend fun removeAll(events: List<AnalyticsEvent>) = withContext(Dispatchers.IO) {
        events.forEach { db.analyticsEventEntityQueries.deleteById(it.id) }
    }

    override suspend fun size(): Int = withContext(Dispatchers.IO) {
        pendingCountUnlocked()
    }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            db.analyticsEventEntityQueries.deleteAll()
        }
    }

    private fun pendingCountUnlocked(): Int =
        db.analyticsEventEntityQueries.countPending().executeAsOne().toInt()

    private fun com.example.shoptourr.db.AnalyticsEventEntity.toDomain(): AnalyticsEvent =
        AnalyticsEvent(
            id = id,
            name = name,
            properties = decodeAnalyticsProperties(properties_json),
            timestampEpochMs = timestamp_epoch_ms,
        )
}
