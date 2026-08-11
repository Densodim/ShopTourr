package com.example.shoptourr.data.remote.dto.map

import com.example.shoptourr.data.remote.dto.common.MoneyDto
import kotlinx.serialization.Serializable

@Serializable
data class GeoPointDto(
    val lat: String,
    val lng: String,
)

@Serializable
data class RouteStopDto(
    val id: String,
    val title: String,
    val place: String? = null,
    val date: String? = null,
    val amountSpentHere: MoneyDto? = null,
    val point: GeoPointDto? = null,
    val orderIndex: Int,
)

@Serializable
data class TripRouteDto(
    val tripId: String,
    val stopCount: Int,
    val distanceMeters: String? = null,
    val stops: List<RouteStopDto>,
    val path: List<GeoPointDto> = emptyList(),
)
