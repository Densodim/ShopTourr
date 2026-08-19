package com.example.shoptourr.domain.model

data class PurchasePageRequest(
    val page: Int = 0,
    val size: Int = 50,
    val afterDate: String? = null,
    val afterId: String? = null,
) {
    init {
        require(page >= 0) { "page must be >= 0" }
        require(size in 1..100) { "size must be 1..100" }
        require((afterDate == null) == (afterId == null)) {
            "afterDate and afterId must be used together"
        }
    }

    val usesKeyset: Boolean get() = afterDate != null && afterId != null
}

object PurchasePageKeyset {
    fun slice(
        purchases: List<Purchase>,
        request: PurchasePageRequest,
    ): List<Purchase> {
        val sorted = purchases.sortedWith(
            compareByDescending<Purchase> { it.purchaseDate }.thenByDescending { it.id },
        )
        if (request.afterDate == null || request.afterId == null) {
            return sorted.drop(request.page * request.size).take(request.size)
        }
        val start = sorted.indexOfFirst {
            it.purchaseDate == request.afterDate && it.id == request.afterId
        }
        val from = if (start < 0) 0 else start + 1
        return sorted.drop(from).take(request.size)
    }

    /** Next keyset request after a fetched page, or null when that page was short. */
    fun nextRequest(page: List<Purchase>, size: Int): PurchasePageRequest? {
        if (page.size < size) return null
        val last = page.last()
        return PurchasePageRequest(
            page = 0,
            size = size,
            afterDate = last.purchaseDate,
            afterId = last.id,
        )
    }
}
