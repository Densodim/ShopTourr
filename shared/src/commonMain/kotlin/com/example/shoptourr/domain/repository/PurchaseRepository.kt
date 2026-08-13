package com.example.shoptourr.domain.repository

import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseDraft
import com.example.shoptourr.domain.model.PurchasePageRequest
import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {
    suspend fun create(tripId: String, draft: PurchaseDraft): Result<Purchase>
    suspend fun update(tripId: String, purchaseId: String, draft: PurchaseDraft): Result<Purchase>
    suspend fun delete(tripId: String, purchaseId: String): Result<Unit>
    fun observeByTrip(tripId: String): Flow<List<Purchase>>
    suspend fun refreshPage(
        tripId: String,
        request: PurchasePageRequest = PurchasePageRequest(),
    ): Result<List<Purchase>>
}
