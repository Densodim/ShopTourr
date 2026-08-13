package com.example.shoptourr.domain.usecase

import com.example.shoptourr.data.local.LocalCacheInventory
import com.example.shoptourr.domain.model.CacheEvictionPolicy
import com.example.shoptourr.domain.model.PurchaseRetentionPolicy

class EvictLocalCacheUseCase(
    private val inventory: LocalCacheInventory,
    private val clock: () -> Long,
) {
    data class Result(
        val evictedTripIds: Set<String>,
        val evictedPurchaseIds: Set<String>,
    )

    suspend operator fun invoke(): Result {
        val now = clock()
        val tripIds = CacheEvictionPolicy.idsToEvict(
            entries = inventory.tripCacheRecords(),
            nowEpochMs = now,
        )
        if (tripIds.isNotEmpty()) {
            inventory.evictTripCache(tripIds)
        }
        val purchaseIds = inventory.purchaseRecordsByTrip()
            .values
            .flatMap { records -> PurchaseRetentionPolicy.idsToEvict(records) }
            .toSet()
        if (purchaseIds.isNotEmpty()) {
            inventory.evictPurchases(purchaseIds)
        }
        return Result(evictedTripIds = tripIds, evictedPurchaseIds = purchaseIds)
    }
}
