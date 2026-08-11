package com.example.shoptourr.domain.model

enum class TripStatus {
    UPCOMING,
    ACTIVE,
    PAST,
    ARCHIVED,
}

data class TripSummary(
    val id: String,
    val city: String,
    val country: String,
    val status: TripStatus,
    val startDate: String,
    val endDate: String,
    val budget: Money,
    val spent: Money,
    val purchaseCount: Int,
    val flagEmoji: String? = null,
    val datesLabel: String? = null,
    val currentDayNumber: Int? = null,
    val dayCount: Int? = null,
) {
    companion object {
        fun toHomeSnapshot(userName: String, trips: List<TripSummary>): HomeSnapshot {
            val current = trips.firstOrNull { it.status == TripStatus.ACTIVE }
            return HomeSnapshot(
                userName = userName,
                currentTripCity = current?.city,
                upcomingCount = trips.count { it.status == TripStatus.UPCOMING },
                archiveCount = trips.count { it.status == TripStatus.PAST || it.status == TripStatus.ARCHIVED },
            )
        }
    }
}

data class CreateTripDraft(
    val city: String,
    val country: String,
    val startDate: String,
    val endDate: String,
    val budget: Money,
    val countryCode: String? = null,
    val defaultVatRatePercent: String? = null,
)
