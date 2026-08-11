package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.model.TripDetail
import com.example.shoptourr.domain.repository.PurchaseRepository
import com.example.shoptourr.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveTripDetailUseCase(
    private val tripRepository: TripRepository,
    private val purchaseRepository: PurchaseRepository,
) {
    operator fun invoke(tripId: String): Flow<TripDetail?> =
        combine(
            tripRepository.observeTrip(tripId),
            purchaseRepository.observeByTrip(tripId),
        ) { trip, purchases ->
            trip?.let { TripDetail(trip = it, purchases = purchases) }
        }
}
