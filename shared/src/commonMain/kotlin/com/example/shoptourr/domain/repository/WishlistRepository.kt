package com.example.shoptourr.domain.repository

import com.example.shoptourr.domain.model.CreateWishlistDraft
import com.example.shoptourr.domain.model.WishlistItem
import kotlinx.coroutines.flow.Flow

interface WishlistRepository {
    fun observeItems(): Flow<List<WishlistItem>>
    suspend fun refresh(): Result<Unit>
    suspend fun create(draft: CreateWishlistDraft): Result<WishlistItem>
    suspend fun delete(id: String): Result<Unit>
}
