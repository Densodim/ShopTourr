package com.example.shoptourr.data.local

import com.example.shoptourr.domain.model.WishlistItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface WishlistLocalStore {
    fun observe(): Flow<List<WishlistItem>>
    fun all(): List<WishlistItem>
    suspend fun replaceAll(items: List<WishlistItem>)
    suspend fun upsert(item: WishlistItem)
    suspend fun replaceId(oldId: String, item: WishlistItem)
    suspend fun remove(id: String)
}

class InMemoryWishlistLocalStore : WishlistLocalStore {
    private val items = MutableStateFlow<List<WishlistItem>>(emptyList())

    override fun observe(): Flow<List<WishlistItem>> = items.asStateFlow()
    override fun all(): List<WishlistItem> = items.value
    override suspend fun replaceAll(items: List<WishlistItem>) {
        this.items.value = items
    }
    override suspend fun upsert(item: WishlistItem) {
        items.value = items.value.filterNot { it.id == item.id } + item
    }
    override suspend fun replaceId(oldId: String, item: WishlistItem) {
        items.value = items.value.filterNot { it.id == oldId || it.id == item.id } + item
    }
    override suspend fun remove(id: String) {
        items.value = items.value.filterNot { it.id == id }
    }
}
