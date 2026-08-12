package com.shoptourr.api.v1.dto.home

import com.shoptourr.api.v1.dto.common.MoneyDto
import com.shoptourr.api.v1.dto.trip.TripSummaryDto
import com.shoptourr.api.v1.dto.user.UserDto

/**
 * Home screen aggregate — one round-trip for first paint.
 * Prefer this over N separate calls on cold start.
 */
data class HomeResponse(
    val user: UserDto,
    val currentTrip: TripSummaryDto?,
    val upcoming: List<TripSummaryDto>,
    val archive: List<TripSummaryDto>,
    /** Sum of spent across active + past (in preferredCurrency). */
    val allTimeSpent: MoneyDto,
    val unreadAlertCount: Int,
)
