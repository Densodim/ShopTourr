package com.example.shoptourr.data.local

import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface PurchaseLocalStore {
    suspend fun upsert(purchase: Purchase)
    suspend fun replaceId(oldId: String, purchase: Purchase)
    suspend fun remove(id: String)
    suspend fun clearAll()
    fun observeByTrip(tripId: String): Flow<List<Purchase>>
    fun getById(id: String): Purchase?
    fun searchByTrip(tripId: String, query: String): List<Purchase>
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

    override suspend fun remove(id: String) {
        items.update { it - id }
    }

    override suspend fun clearAll() {
        items.value = emptyMap()
    }

    override fun observeByTrip(tripId: String): Flow<List<Purchase>> =
        items.map { map -> map.values.filter { it.tripId == tripId }.sortedByDescending { it.purchaseDate } }

    override fun getById(id: String): Purchase? = items.value[id]

    override fun searchByTrip(tripId: String, query: String): List<Purchase> {
        val inTrip = items.value.values.filter { it.tripId == tripId }
        return PurchaseSearch.filter(inTrip, query)
    }
}
