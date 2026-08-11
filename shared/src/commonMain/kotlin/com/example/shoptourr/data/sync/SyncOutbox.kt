package com.example.shoptourr.data.sync

import com.example.shoptourr.domain.error.AppError

enum class SyncMutationType {
    CREATE_PURCHASE,
    UPDATE_PURCHASE,
    DELETE_PURCHASE,
    CREATE_TRIP,
    UPDATE_TRIP,
    DELETE_TRIP,
    CREATE_DIARY,
    CREATE_WISHLIST,
}

enum class SyncOutboxStatus {
    PENDING,
    FAILED,
    CANCELED,
}

data class SyncOutboxEntry(
    val id: String,
    val type: SyncMutationType,
    val payloadJson: String,
    val idempotencyKey: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long = createdAtEpochMs,
    val failureCount: Int = 0,
    val status: SyncOutboxStatus = SyncOutboxStatus.PENDING,
)

object SyncOutboxPolicy {
    const val MAX_PENDING = 200
    const val MAX_FAILURES = 8

    fun backoffMs(failureCount: Int): Long {
        if (failureCount <= 0) return 0L
        val shift = (failureCount - 1).coerceAtMost(14)
        return (1_000L shl shift).coerceAtMost(15 * 60_000L)
    }

    fun isDue(entry: SyncOutboxEntry, nowEpochMs: Long): Boolean {
        if (entry.status != SyncOutboxStatus.PENDING) return false
        if (entry.failureCount <= 0) return true
        return nowEpochMs >= entry.updatedAtEpochMs + backoffMs(entry.failureCount)
    }
}

interface SyncOutbox {
    suspend fun enqueue(entry: SyncOutboxEntry)
    suspend fun pending(): List<SyncOutboxEntry>
    suspend fun pendingCount(): Int
    suspend fun markSuccess(id: String)
    suspend fun markFailure(id: String, updatedAtEpochMs: Long)
}

class InMemorySyncOutbox(
    private val maxPending: Int = SyncOutboxPolicy.MAX_PENDING,
    private val maxFailures: Int = SyncOutboxPolicy.MAX_FAILURES,
) : SyncOutbox {
    private val entries = linkedMapOf<String, SyncOutboxEntry>()

    override suspend fun enqueue(entry: SyncOutboxEntry) {
        val occupiesSlot = entries[entry.id] == null
        if (occupiesSlot && pendingCount() >= maxPending) {
            throw AppError.Validation("outbox_full")
        }
        entries[entry.id] = entry
    }

    override suspend fun pending(): List<SyncOutboxEntry> =
        entries.values
            .filter { it.status == SyncOutboxStatus.PENDING || it.status == SyncOutboxStatus.FAILED }
            .sortedBy { it.createdAtEpochMs }

    override suspend fun pendingCount(): Int =
        entries.values.count {
            it.status == SyncOutboxStatus.PENDING || it.status == SyncOutboxStatus.FAILED
        }

    override suspend fun markSuccess(id: String) {
        entries.remove(id)
    }

    override suspend fun markFailure(id: String, updatedAtEpochMs: Long) {
        val current = entries[id] ?: return
        val failures = current.failureCount + 1
        entries[id] = current.copy(
            failureCount = failures,
            updatedAtEpochMs = updatedAtEpochMs,
            status = if (failures >= maxFailures) {
                SyncOutboxStatus.FAILED
            } else {
                SyncOutboxStatus.PENDING
            },
        )
    }
}
