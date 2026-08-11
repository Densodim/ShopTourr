package com.example.shoptourr.data.remote.dto.map

import com.example.shoptourr.data.remote.dto.common.MoneyDto

data class GeoPointDto(
    val lat: String,
    val lng: String,
)

data class RouteStopDto(
    val id: String,
    val title: String,
    val place: String? = null,
    val date: String? = null,
    val amountSpentHere: MoneyDto? = null,
    val point: GeoPointDto? = null,
    val orderIndex: Int,
)

data class TripRouteDto(
    val tripId: String,
    val stopCount: Int,
    val distanceMeters: String? = null,
    val stops: List<RouteStopDto>,
    val path: List<GeoPointDto> = emptyList(),
)
