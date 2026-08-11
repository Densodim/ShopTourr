package com.example.shoptourr.data.sync

import com.example.shoptourr.db.VoyageDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class SqlDelightSyncOutbox(
    private val db: VoyageDatabase,
) : SyncOutbox {

    override suspend fun enqueue(entry: SyncOutboxEntry) {
        withContext(Dispatchers.IO) {
            db.syncOutboxEntityQueries.upsert(
                id = entry.id,
                type = entry.type.name,
                payload_json = entry.payloadJson,
                idempotency_key = entry.idempotencyKey,
                created_at_epoch_ms = entry.createdAtEpochMs,
                updated_at_epoch_ms = entry.updatedAtEpochMs,
                failure_count = entry.failureCount.toLong(),
                status = entry.status.name,
            )
        }
    }

    override suspend fun pending(): List<SyncOutboxEntry> = withContext(Dispatchers.IO) {
        db.syncOutboxEntityQueries.selectPending().executeAsList().map { row ->
            SyncOutboxEntry(
                id = row.id,
                type = SyncMutationType.valueOf(row.type),
                payloadJson = row.payload_json,
                idempotencyKey = row.idempotency_key,
                createdAtEpochMs = row.created_at_epoch_ms,
                updatedAtEpochMs = row.updated_at_epoch_ms,
                failureCount = row.failure_count.toInt(),
                status = SyncOutboxStatus.valueOf(row.status),
            )
        }
    }

    override suspend fun markSuccess(id: String) {
        withContext(Dispatchers.IO) {
            db.syncOutboxEntityQueries.deleteById(id)
        }
    }

    override suspend fun markFailure(id: String, updatedAtEpochMs: Long) = withContext(Dispatchers.IO) {
        val current = pending().firstOrNull { it.id == id } ?: return@withContext
        enqueue(
            current.copy(
                failureCount = current.failureCount + 1,
                updatedAtEpochMs = updatedAtEpochMs,
                status = SyncOutboxStatus.PENDING,
            )
        )
    }
}
