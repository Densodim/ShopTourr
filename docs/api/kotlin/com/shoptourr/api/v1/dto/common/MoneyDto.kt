package com.shoptourr.api.v1.dto.common

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal

/** ISO-4217 money. JSON: {"amount":"96.50","currency":"EUR"} */
data class MoneyDto(
    @field:NotNull
    @field:DecimalMin(value = "0.00", inclusive = true)
    val amount: BigDecimal,

    @field:NotBlank
    @field:Size(min = 3, max = 3)
    @field:Pattern(regexp = "[A-Z]{3}")
    val currency: String,
)
