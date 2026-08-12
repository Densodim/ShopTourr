package com.shoptourr.api.v1.dto.common

import java.math.BigDecimal

data class VatBreakdownDto(
    val net: BigDecimal,
    val vat: BigDecimal,
    val gross: BigDecimal,
    val vatRatePercent: BigDecimal,
    val vatIncluded: Boolean,
)
