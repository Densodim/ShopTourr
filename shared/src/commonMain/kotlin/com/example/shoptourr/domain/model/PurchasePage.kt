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
    }
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
}
