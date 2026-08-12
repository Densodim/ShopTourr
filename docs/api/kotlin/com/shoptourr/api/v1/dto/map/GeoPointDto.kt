package com.shoptourr.api.v1.dto.map

import java.math.BigDecimal

data class GeoPointDto(
    val lat: BigDecimal,
    val lng: BigDecimal,
)
