package com.example.shoptourr.domain.model

/**
 * Hybrid cache eviction (mobile-system-design / pagination ch.6):
 * TTL + LRU, never dropping below [minEntries].
 */
data class CacheRecord(
    val id: String,
    val lastAccessEpochMs: Long,
    val sizeBytes: Long = 1L,
)

object CacheEvictionPolicy {
    const val TTL_MS: Long = 15L * 24 * 60 * 60 * 1000
    const val MIN_ENTRIES: Int = 3
    const val MAX_ENTRIES: Int = 40
    const val MAX_SIZE_BYTES: Long = 150L * 1024 * 1024

    fun idsToEvict(
        entries: List<CacheRecord>,
        nowEpochMs: Long,
        ttlMs: Long = TTL_MS,
        minEntries: Int = MIN_ENTRIES,
        maxEntries: Int = MAX_ENTRIES,
        maxSizeBytes: Long = MAX_SIZE_BYTES,
    ): Set<String> {
        if (entries.size <= minEntries) return emptySet()
        val remaining = entries.toMutableList()
        val evict = linkedSetOf<String>()

        fun canEvict(): Boolean = remaining.size > minEntries

        remaining
            .filter { nowEpochMs - it.lastAccessEpochMs > ttlMs }
            .sortedBy { it.lastAccessEpochMs }
            .forEach { record ->
                if (canEvict()) {
                    evict += record.id
                    remaining.removeAll { it.id == record.id }
                }
            }

        while (remaining.size > maxEntries && canEvict()) {
            val oldest = remaining.minBy { it.lastAccessEpochMs }
            evict += oldest.id
            remaining.removeAll { it.id == oldest.id }
        }

        while (remaining.sumOf { it.sizeBytes } > maxSizeBytes && canEvict()) {
            val oldest = remaining.minBy { it.lastAccessEpochMs }
            evict += oldest.id
            remaining.removeAll { it.id == oldest.id }
        }
        return evict
    }
}

data class PurchaseRetentionRecord(
    val id: String,
    val purchaseDate: String,
    val pendingSync: Boolean,
)

object PurchaseRetentionPolicy {
    const val MAX_PER_TRIP: Int = 50
    const val MIN_PER_TRIP: Int = 20

    fun idsToEvict(
        records: List<PurchaseRetentionRecord>,
        maxPerTrip: Int = MAX_PER_TRIP,
        minPerTrip: Int = MIN_PER_TRIP,
    ): Set<String> {
        val keepable = records
            .filterNot { it.pendingSync }
            .sortedWith(
                compareByDescending<PurchaseRetentionRecord> { it.purchaseDate }
                    .thenByDescending { it.id },
            )
        if (keepable.size <= minPerTrip || keepable.size <= maxPerTrip) return emptySet()
        return keepable.drop(maxPerTrip).map { it.id }.toSet()
    }
}
