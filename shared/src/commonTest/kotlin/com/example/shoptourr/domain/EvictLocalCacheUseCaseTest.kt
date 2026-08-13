package com.example.shoptourr.domain

import com.example.shoptourr.data.local.InMemoryLocalCacheInventory
import com.example.shoptourr.domain.model.CacheEvictionPolicy
import com.example.shoptourr.domain.model.CacheRecord
import com.example.shoptourr.domain.model.PurchaseRetentionRecord
import com.example.shoptourr.domain.usecase.EvictLocalCacheUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class EvictLocalCacheUseCaseTest {

    @Test
    fun `evicts stale trip cache and extra purchases`() = runTest {
        val now = 2_000_000_000_000L
        val inventory = InMemoryLocalCacheInventory().apply {
            tripRecords += listOf(
                CacheRecord("fresh", now),
                CacheRecord("fresh2", now - 10),
                CacheRecord("fresh3", now - 20),
                CacheRecord("stale", now - CacheEvictionPolicy.TTL_MS - 1),
            )
            purchasesByTrip["lisbon"] = (1..55).map { index ->
                PurchaseRetentionRecord(
                    id = "p$index",
                    purchaseDate = "2026-08-${(index % 28 + 1).toString().padStart(2, '0')}",
                    pendingSync = false,
                )
            }.toMutableList()
        }
        val result = EvictLocalCacheUseCase(inventory, clock = { now })()
        assertEquals(setOf("stale"), result.evictedTripIds)
        assertEquals(setOf("stale"), inventory.evictedTripIds)
        assertEquals(5, result.evictedPurchaseIds.size)
        assertTrue(inventory.evictedPurchaseIds.isNotEmpty())
    }
}
