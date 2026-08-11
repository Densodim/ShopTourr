package com.example.shoptourr.data.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
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

@Serializable
data class FieldErrorDto(
    val field: String,
    val code: String,
    val message: String,
)

@Serializable
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

@Serializable
data class ExchangeRateDto(
    val tripCurrency: String,
    val quoteCurrency: String,
    val rate: String,
    val rateDate: String,
    val provider: String? = null,
)

@Serializable
data class VatBreakdownDto(
    val net: String,
    val vat: String,
    val gross: String,
    val vatRatePercent: String,
    val vatIncluded: Boolean,
)
