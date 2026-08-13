package com.example.shoptourr.data.local

import com.example.shoptourr.db.VoyageDatabase
import com.example.shoptourr.domain.model.CacheRecord
import com.example.shoptourr.domain.model.PurchaseRetentionRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class SqlDelightLocalCacheInventory(
    private val db: VoyageDatabase,
) : LocalCacheInventory {
    override suspend fun tripCacheRecords(): List<CacheRecord> = withContext(Dispatchers.IO) {
        db.tripCacheEntityQueries.selectAll().executeAsList()
            .groupBy { it.trip_id }
            .map { (tripId, rows) ->
                CacheRecord(
                    id = tripId,
                    lastAccessEpochMs = rows.maxOf { it.updated_at_epoch_ms },
                    sizeBytes = rows.sumOf { it.payload_json.length.toLong() },
                )
            }
    }

    override suspend fun evictTripCache(tripIds: Set<String>) {
        withContext(Dispatchers.IO) {
            db.transaction {
                tripIds.forEach { tripId ->
                    db.tripCacheEntityQueries.deleteByTripId(tripId)
                }
            }
        }
    }

    override suspend fun purchaseRecordsByTrip(): Map<String, List<PurchaseRetentionRecord>> =
        withContext(Dispatchers.IO) {
            db.purchaseEntityQueries.selectAll().executeAsList()
                .groupBy { it.trip_id }
                .mapValues { (_, rows) ->
                    rows.map { row ->
                        PurchaseRetentionRecord(
                            id = row.id,
                            purchaseDate = row.purchase_date,
                            pendingSync = row.pending_sync == 1L,
                        )
                    }
                }
        }

    override suspend fun evictPurchases(ids: Set<String>) {
        withContext(Dispatchers.IO) {
            db.transaction {
                ids.forEach { id ->
                    db.purchaseEntityQueries.deleteById(id)
                }
            }
        }
    }
}
