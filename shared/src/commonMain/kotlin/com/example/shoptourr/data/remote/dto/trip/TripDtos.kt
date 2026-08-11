package com.example.shoptourr.data.remote.dto.trip

import com.example.shoptourr.data.remote.dto.common.ExchangeRateDto
import com.example.shoptourr.data.remote.dto.common.MoneyDto
import kotlinx.serialization.Serializable

@Serializable
enum class TripStatus { UPCOMING, ACTIVE, PAST, ARCHIVED }

@Serializable
data class TravelerDto(
    val id: String,
    val name: String,
    val colorHex: String,
    val avatarGlyph: String,
    val isOwner: Boolean,
)

@Serializable
data class CreateTravelerRequest(
    val name: String,
    val colorHex: String,
    val avatarGlyph: String? = null,
)

@Serializable
enum class TripInviteStatus { PENDING, ACCEPTED, DECLINED, EXPIRED }

@Serializable
data class InviteTravelerRequest(
    val email: String,
    val displayNameHint: String? = null,
)

@Serializable
data class TripInviteDto(
    val id: String,
    val tripId: String,
    val email: String,
    val status: TripInviteStatus,
    val createdAt: String,
    val expiresAt: String? = null,
)

@Serializable
data class TripDto(
    val id: String,
    val city: String,
    val country: String,
    val countryCode: String? = null,
    val flagEmoji: String? = null,
    val status: TripStatus,
    val startDate: String,
    val endDate: String,
    val datesLabel: String? = null,
    val budget: MoneyDto,
    val spent: MoneyDto,
    val remaining: MoneyDto,
    val purchaseCount: Int,
    val dayCount: Int,
    val currentDayNumber: Int? = null,
    val defaultVatRatePercent: String,
    val exchangeRate: ExchangeRateDto? = null,
    val travelers: List<TravelerDto> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class TripSummaryDto(
    val id: String,
    val city: String,
    val country: String,
    val flagEmoji: String? = null,
    val status: TripStatus,
    val startDate: String,
    val endDate: String,
    val datesLabel: String? = null,
    val budget: MoneyDto,
    val spent: MoneyDto,
    val purchaseCount: Int,
    val currentDayNumber: Int? = null,
    val dayCount: Int? = null,
)

@Serializable
data class CreateTripRequest(
    val city: String,
    val country: String,
    val countryCode: String? = null,
    val startDate: String,
    val endDate: String,
    val budget: MoneyDto,
    val defaultVatRatePercent: String? = null,
    val quoteCurrency: String? = null,
    val travelers: List<CreateTravelerRequest>? = null,
)

@Serializable
data class UpdateTripRequest(
    val city: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val budget: MoneyDto? = null,
    val defaultVatRatePercent: String? = null,
    val status: TripStatus? = null,
)

@Serializable
data class TripListResponse(
    val active: List<TripSummaryDto>,
    val upcoming: List<TripSummaryDto>,
    val past: List<TripSummaryDto>,
)
