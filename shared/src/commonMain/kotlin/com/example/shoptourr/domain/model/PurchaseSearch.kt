package com.example.shoptourr.domain.model

object PurchaseSearch {
    fun filter(purchases: List<Purchase>, query: String): List<Purchase> {
        val needle = query.trim()
        if (needle.isEmpty()) return purchases
        return purchases.filter { matches(it, needle) }
    }

    fun matches(purchase: Purchase, query: String): Boolean {
        val needle = query.trim()
        if (needle.isEmpty()) return true
        return purchase.name.contains(needle, ignoreCase = true) ||
            purchase.place.orEmpty().contains(needle, ignoreCase = true)
    }
}
