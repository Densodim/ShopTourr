package com.shoptourr.api.v1.dto.common

data class FieldErrorDto(
    val field: String,
    val code: String,
    val message: String,
)
