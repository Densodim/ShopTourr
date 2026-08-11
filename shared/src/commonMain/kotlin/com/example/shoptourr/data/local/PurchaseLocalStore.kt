package com.example.shoptourr.data.local

import com.example.shoptourr.domain.model.Purchase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface PurchaseLocalStore {
    suspend fun upsert(purchase: Purchase)
    suspend fun replaceId(oldId: String, purchase: Purchase)
    fun observeByTrip(tripId: String): Flow<List<Purchase>>
    fun getById(id: String): Purchase?
}

class InMemoryPurchaseLocalStore : PurchaseLocalStore {
    private val items = MutableStateFlow<Map<String, Purchase>>(emptyMap())

    override suspend fun upsert(purchase: Purchase) {
        items.update { it + (purchase.id to purchase) }
    }

    override suspend fun replaceId(oldId: String, purchase: Purchase) {
        items.update { current ->
            current - oldId + (purchase.id to purchase)
        }
    }

    override fun observeByTrip(tripId: String): Flow<List<Purchase>> =
        items.map { map -> map.values.filter { it.tripId == tripId }.sortedByDescending { it.purchaseDate } }

    override fun getById(id: String): Purchase? = items.value[id]
}
