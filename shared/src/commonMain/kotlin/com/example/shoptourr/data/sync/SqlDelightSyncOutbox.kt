package com.example.shoptourr.data.sync

import com.example.shoptourr.db.VoyageDatabase
import com.example.shoptourr.domain.error.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class SqlDelightSyncOutbox(
    private val db: VoyageDatabase,
    private val maxPending: Int = SyncOutboxPolicy.MAX_PENDING,
    private val maxFailures: Int = SyncOutboxPolicy.MAX_FAILURES,
) : SyncOutbox {

    override suspend fun enqueue(entry: SyncOutboxEntry) {
        withContext(Dispatchers.IO) {
            val existing = db.syncOutboxEntityQueries.selectPending()
                .executeAsList()
                .any { it.id == entry.id }
            if (!existing && pendingCountUnlocked() >= maxPending) {
                throw AppError.Validation("outbox_full")
            }
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
        db.syncOutboxEntityQueries.selectPending().executeAsList().map { it.toDomain() }
    }

    override suspend fun pendingCount(): Int = withContext(Dispatchers.IO) {
        pendingCountUnlocked()
    }

    override suspend fun markSuccess(id: String) {
        withContext(Dispatchers.IO) {
            db.syncOutboxEntityQueries.deleteById(id)
        }
    }

    override suspend fun markFailure(id: String, updatedAtEpochMs: Long) = withContext(Dispatchers.IO) {
        val current = db.syncOutboxEntityQueries.selectPending()
            .executeAsList()
            .firstOrNull { it.id == id }
            ?.toDomain()
            ?: return@withContext
        val failures = current.failureCount + 1
        db.syncOutboxEntityQueries.upsert(
            id = current.id,
            type = current.type.name,
            payload_json = current.payloadJson,
            idempotency_key = current.idempotencyKey,
            created_at_epoch_ms = current.createdAtEpochMs,
            updated_at_epoch_ms = updatedAtEpochMs,
            failure_count = failures.toLong(),
            status = if (failures >= maxFailures) {
                SyncOutboxStatus.FAILED.name
            } else {
                SyncOutboxStatus.PENDING.name
            },
        )
    }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            db.syncOutboxEntityQueries.deleteAll()
        }
    }

    private fun pendingCountUnlocked(): Int =
        db.syncOutboxEntityQueries.countPending().executeAsOne().toInt()

    private fun com.example.shoptourr.db.SyncOutboxEntity.toDomain(): SyncOutboxEntry =
        SyncOutboxEntry(
            id = id,
            type = SyncMutationType.valueOf(type),
            payloadJson = payload_json,
            idempotencyKey = idempotency_key,
            createdAtEpochMs = created_at_epoch_ms,
            updatedAtEpochMs = updated_at_epoch_ms,
            failureCount = failure_count.toInt(),
            status = SyncOutboxStatus.valueOf(status),
        )
}
