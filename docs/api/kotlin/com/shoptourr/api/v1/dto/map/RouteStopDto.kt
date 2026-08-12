package com.shoptourr.api.v1.dto.map

import com.shoptourr.api.v1.dto.common.MoneyDto
import java.time.LocalDate
import java.util.UUID

data class RouteStopDto(
    val id: UUID,
    val title: String,
    val place: String?,
    val date: LocalDate?,
    val amountSpentHere: MoneyDto?,
    val point: GeoPointDto?,
    val orderIndex: Int,
)
