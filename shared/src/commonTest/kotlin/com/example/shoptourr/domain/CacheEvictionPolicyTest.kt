package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.CacheEvictionPolicy
import com.example.shoptourr.domain.model.CacheRecord
import com.example.shoptourr.domain.model.PurchaseRetentionPolicy
import com.example.shoptourr.domain.model.PurchaseRetentionRecord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CacheEvictionPolicyTest {

    private val now = 1_700_000_000_000L
    private val ttl = CacheEvictionPolicy.TTL_MS

    @Test
    fun `keeps all when at or below min entries even if expired`() {
        val entries = listOf(
            CacheRecord("a", lastAccessEpochMs = now - ttl - 1),
            CacheRecord("b", lastAccessEpochMs = now - ttl - 2),
            CacheRecord("c", lastAccessEpochMs = now - ttl - 3),
        )
        assertTrue(CacheEvictionPolicy.idsToEvict(entries, nowEpochMs = now).isEmpty())
    }

    @Test
    fun `evicts ttl-expired beyond min`() {
        val entries = listOf(
            CacheRecord("fresh", lastAccessEpochMs = now),
            CacheRecord("fresh2", lastAccessEpochMs = now - 1_000),
            CacheRecord("fresh3", lastAccessEpochMs = now - 2_000),
            CacheRecord("stale", lastAccessEpochMs = now - ttl - 5),
        )
        assertEquals(setOf("stale"), CacheEvictionPolicy.idsToEvict(entries, nowEpochMs = now))
    }

    @Test
    fun `evicts lru when over max entries`() {
        val entries = (1..45).map { index ->
            CacheRecord(
                id = "t$index",
                lastAccessEpochMs = now - index * 1_000L,
            )
        }
        val evicted = CacheEvictionPolicy.idsToEvict(entries, nowEpochMs = now)
        assertEquals(5, evicted.size)
        assertTrue(evicted.containsAll(setOf("t41", "t42", "t43", "t44", "t45")))
        assertTrue("t1" !in evicted)
    }

    @Test
    fun `evicts lru when over max size but keeps min`() {
        val entries = listOf(
            CacheRecord("keep-new", now, sizeBytes = 10),
            CacheRecord("keep-mid", now - 1, sizeBytes = 10),
            CacheRecord("keep-old", now - 2, sizeBytes = 10),
            CacheRecord("drop", now - 3, sizeBytes = 200),
        )
        val evicted = CacheEvictionPolicy.idsToEvict(
            entries,
            nowEpochMs = now,
            maxSizeBytes = 50,
        )
        assertEquals(setOf("drop"), evicted)
    }
}

class PurchaseRetentionPolicyTest {

    @Test
    fun `keeps pending sync even when over max`() {
        val records = (1..55).map { index ->
            PurchaseRetentionRecord(
                id = "p$index",
                purchaseDate = "2026-08-${(index % 28 + 1).toString().padStart(2, '0')}",
                pendingSync = index == 1,
            )
        }
        val evicted = PurchaseRetentionPolicy.idsToEvict(records)
        assertTrue("p1" !in evicted)
        assertEquals(4, evicted.size)
    }

    @Test
    fun `does not evict when at or below max`() {
        val records = (1..40).map { index ->
            PurchaseRetentionRecord("p$index", "2026-08-01", pendingSync = false)
        }
        assertTrue(PurchaseRetentionPolicy.idsToEvict(records).isEmpty())
    }
}
