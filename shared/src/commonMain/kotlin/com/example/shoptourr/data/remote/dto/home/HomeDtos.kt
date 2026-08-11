package com.example.shoptourr.data.remote.dto.home

import com.example.shoptourr.data.remote.dto.common.MoneyDto
import com.example.shoptourr.data.remote.dto.trip.TripSummaryDto
import com.example.shoptourr.data.remote.dto.user.UserDto
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
