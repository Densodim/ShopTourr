package com.shoptourr.api.v1.dto.common

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal

/** FX snapshot stored on trip (rate = units of [quoteCurrency] per 1 [tripCurrency]). */
data class ExchangeRateDto(
    @field:NotBlank
    @field:Size(min = 3, max = 3)
    val tripCurrency: String,

    @field:NotBlank
    @field:Size(min = 3, max = 3)
    val quoteCurrency: String,

    @field:NotNull
    @field:DecimalMin("0.000001")
    val rate: BigDecimal,

    @field:NotNull
    val rateDate: String,

    val provider: String?,
)
