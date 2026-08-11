package com.example.shoptourr.api.common

/**
 * Shared API models — keep field names/types aligned with
 * docs/api/spring/.../dto (Java records).
 *
 * Amounts are decimal strings on the wire ("96.50").
 * Instants/dates are ISO-8601 strings until kotlinx.datetime is wired.
 */
data class MoneyDto(
    val amount: String,
    val currency: String,
)

data class PageResponseDto<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean,
)

data class FieldErrorDto(
    val field: String,
    val code: String,
    val message: String,
)

data class ProblemDetailDto(
    val type: String? = null,
    val title: String? = null,
    val status: Int,
    val detail: String? = null,
    val instance: String? = null,
    val code: String? = null,
    val errors: List<FieldErrorDto>? = null,
    val requestId: String? = null,
)

data class ExchangeRateDto(
    val tripCurrency: String,
    val quoteCurrency: String,
    val rate: String,
    val rateDate: String,
    val provider: String? = null,
)

data class VatBreakdownDto(
    val net: String,
    val vat: String,
    val gross: String,
    val vatRatePercent: String,
    val vatIncluded: Boolean,
)
