package com.example.shoptourr.api.trip

import com.example.shoptourr.api.common.ExchangeRateDto
import com.example.shoptourr.api.common.MoneyDto

enum class TripStatus { UPCOMING, ACTIVE, PAST, ARCHIVED }

data class TravelerDto(
    val id: String,
    val name: String,
    val colorHex: String,
    val avatarGlyph: String,
    val isOwner: Boolean,
)

data class CreateTravelerRequest(
    val name: String,
    val colorHex: String,
    val avatarGlyph: String? = null,
)

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

data class TripListResponse(
    val active: List<TripSummaryDto>,
    val upcoming: List<TripSummaryDto>,
    val past: List<TripSummaryDto>,
)
