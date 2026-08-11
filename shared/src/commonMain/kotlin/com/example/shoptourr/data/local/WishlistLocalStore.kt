package com.example.shoptourr.data.local

import com.example.shoptourr.domain.model.WishlistItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface WishlistLocalStore {
    fun observe(): Flow<List<WishlistItem>>
    fun all(): List<WishlistItem>
    fun replaceAll(items: List<WishlistItem>)
    fun upsert(item: WishlistItem)
    fun remove(id: String)
}

class InMemoryWishlistLocalStore : WishlistLocalStore {
    private val items = MutableStateFlow<List<WishlistItem>>(emptyList())

    override fun observe(): Flow<List<WishlistItem>> = items.asStateFlow()
    override fun all(): List<WishlistItem> = items.value
    override fun replaceAll(items: List<WishlistItem>) {
        this.items.value = items
    }
    override fun upsert(item: WishlistItem) {
        items.value = items.value.filterNot { it.id == item.id } + item
    }
    override fun remove(id: String) {
        items.value = items.value.filterNot { it.id == id }
    }
}
