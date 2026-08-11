package com.example.shoptourr.data.sync

enum class SyncMutationType {
    CREATE_PURCHASE,
    UPDATE_PURCHASE,
    DELETE_PURCHASE,
    CREATE_TRIP,
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

interface SyncOutbox {
    suspend fun enqueue(entry: SyncOutboxEntry)
    suspend fun pending(): List<SyncOutboxEntry>
    suspend fun markSuccess(id: String)
    suspend fun markFailure(id: String, updatedAtEpochMs: Long)
}

class InMemorySyncOutbox : SyncOutbox {
    private val entries = linkedMapOf<String, SyncOutboxEntry>()

    override suspend fun enqueue(entry: SyncOutboxEntry) {
        entries[entry.id] = entry
    }

    override suspend fun pending(): List<SyncOutboxEntry> =
        entries.values
            .filter { it.status == SyncOutboxStatus.PENDING || it.status == SyncOutboxStatus.FAILED }
            .sortedBy { it.createdAtEpochMs }

    override suspend fun markSuccess(id: String) {
        entries.remove(id)
    }

    override suspend fun markFailure(id: String, updatedAtEpochMs: Long) {
        val current = entries[id] ?: return
        entries[id] = current.copy(
            failureCount = current.failureCount + 1,
            updatedAtEpochMs = updatedAtEpochMs,
            status = SyncOutboxStatus.PENDING,
        )
    }
}
