package com.example.shoptourr.data.local

import com.example.shoptourr.domain.model.CacheRecord
import com.example.shoptourr.domain.model.PurchaseRetentionRecord

interface LocalCacheInventory {
    suspend fun tripCacheRecords(): List<CacheRecord>
    suspend fun evictTripCache(tripIds: Set<String>)
    suspend fun purchaseRecordsByTrip(): Map<String, List<PurchaseRetentionRecord>>
    suspend fun evictPurchases(ids: Set<String>)
}

class InMemoryLocalCacheInventory : LocalCacheInventory {
    var tripRecords: MutableList<CacheRecord> = mutableListOf()
    var purchasesByTrip: MutableMap<String, MutableList<PurchaseRetentionRecord>> = mutableMapOf()
    val evictedTripIds = mutableSetOf<String>()
    val evictedPurchaseIds = mutableSetOf<String>()

    override suspend fun tripCacheRecords(): List<CacheRecord> = tripRecords.toList()

    override suspend fun evictTripCache(tripIds: Set<String>) {
        evictedTripIds += tripIds
        tripRecords.removeAll { it.id in tripIds }
    }

    override suspend fun purchaseRecordsByTrip(): Map<String, List<PurchaseRetentionRecord>> =
        purchasesByTrip.mapValues { it.value.toList() }

    override suspend fun evictPurchases(ids: Set<String>) {
        evictedPurchaseIds += ids
        purchasesByTrip.values.forEach { list ->
            list.removeAll { it.id in ids }
        }
    }
}
