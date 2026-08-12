package com.shoptourr.api.v1.dto.map

import java.math.BigDecimal
import java.util.UUID

data class TripRouteDto(
    val tripId: UUID,
    val stopCount: Int,
    /** Meters; null if unknown. */
    val distanceMeters: BigDecimal?,
    val stops: List<RouteStopDto>,
    /** Ordered polyline if available. */
    val path: List<GeoPointDto>?,
)
