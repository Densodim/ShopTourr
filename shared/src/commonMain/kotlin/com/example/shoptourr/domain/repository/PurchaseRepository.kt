package com.example.shoptourr.domain.repository

import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseDraft
import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {
    suspend fun create(tripId: String, draft: PurchaseDraft): Result<Purchase>
    fun observeByTrip(tripId: String): Flow<List<Purchase>>
}
