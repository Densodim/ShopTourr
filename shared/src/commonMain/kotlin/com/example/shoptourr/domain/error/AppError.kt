package com.example.shoptourr.domain.error

sealed class AppError(message: String? = null) : Exception(message) {
    data class Validation(val field: String) : AppError("validation:$field")
    data object Unauthorized : AppError("unauthorized")
    data object NotFound : AppError("not_found")
    data object Network : AppError("network")
    data object Conflict : AppError("conflict")
    data class Unknown(val causeMessage: String? = null) : AppError(causeMessage)
}
