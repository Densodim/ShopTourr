package com.example.shoptourr.domain.model

data class Traveler(
    val id: String,
    val name: String,
    val colorHex: String,
    val avatarGlyph: String,
    val isOwner: Boolean,
)

data class CreateTravelerDraft(
    val name: String,
    val colorHex: String = "#FFD84D",
    val avatarGlyph: String? = null,
)

data class ExchangeRate(
    val tripCurrency: String,
    val quoteCurrency: String,
    val rate: String,
    val rateDate: String,
    val provider: String? = null,
)

enum class TripInviteStatus { PENDING, ACCEPTED, DECLINED, EXPIRED }

data class TripInvite(
    val id: String,
    val tripId: String,
    val email: String,
    val status: TripInviteStatus,
    val createdAt: String,
    val expiresAt: String? = null,
)

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
    val exchangeRate: ExchangeRate? = null,
    val travelers: List<Traveler> = emptyList(),
) {
    companion object {
        fun toHomeSnapshot(userName: String, trips: List<TripSummary>): HomeSnapshot {
            val current = trips.firstOrNull { it.status == TripStatus.ACTIVE }
            return HomeSnapshot(
                userName = userName,
                currentTripCity = current?.city,
                upcomingCount = trips.count { it.status == TripStatus.UPCOMING },
                archiveCount = trips.count { it.status == TripStatus.PAST || it.status == TripStatus.ARCHIVED },
                currentTripId = current?.id,
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
    val quoteCurrency: String? = null,
    val travelers: List<CreateTravelerDraft> = emptyList(),
)

data class UpdateTripDraft(
    val city: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val budget: Money? = null,
    val defaultVatRatePercent: String? = null,
    val status: TripStatus? = null,
)
