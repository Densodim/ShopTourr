package com.shoptourr.api.v1.dto.purchase

import com.shoptourr.api.v1.dto.common.MoneyDto
import java.util.UUID

data class SplitShareDto(
    val travelerId: UUID,
    val travelerName: String,
    val share: MoneyDto,
)
