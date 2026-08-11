package com.example.shoptourr.api.home

import com.example.shoptourr.api.common.MoneyDto
import com.example.shoptourr.api.trip.TripSummaryDto
import com.example.shoptourr.api.user.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class HomeResponse(
    val user: UserDto,
    val currentTrip: TripSummaryDto? = null,
    val upcoming: List<TripSummaryDto> = emptyList(),
    val archive: List<TripSummaryDto> = emptyList(),
    val allTimeSpent: MoneyDto,
    val unreadAlertCount: Int = 0,
)
