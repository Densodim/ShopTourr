package com.shoptourr.api.v1.dto.common

/**
 * RFC 7807 Problem Details (+ Voyage extensions `code`, `errors`, `requestId`).
 */
data class ProblemDetailDto(
    val type: String?,
    val title: String?,
    val status: Int,
    val detail: String?,
    val instance: String?,
    val code: String?,
    val errors: List<FieldErrorDto>?,
    val requestId: String?,
)
