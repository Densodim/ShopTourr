package com.example.shoptourr.presentation.error

import com.example.shoptourr.domain.error.AppError

data class UiError(
    val title: String,
    val message: String,
    val isRetryable: Boolean,
    val action: UiErrorAction? = null,
)

sealed interface UiErrorAction {
    data object Logout : UiErrorAction
    data class Navigate(val route: String) : UiErrorAction
}

fun AppError.toUiError(): UiError = when (this) {
    AppError.Network -> UiError(
        title = "No Connection",
        message = "Check your internet connection",
        isRetryable = true,
    )
    AppError.Timeout -> UiError(
        title = "Timeout",
        message = "Server did not respond",
        isRetryable = true,
    )
    AppError.Unauthorized -> UiError(
        title = "Session Expired",
        message = "Please log in again",
        isRetryable = false,
        action = UiErrorAction.Logout,
    )
    AppError.NotFound -> UiError(
        title = "Not Found",
        message = "The requested content was not found",
        isRetryable = false,
    )
    AppError.Conflict -> UiError(
        title = "Conflict",
        message = "This change conflicts with server state",
        isRetryable = true,
    )
    AppError.DatabaseError -> UiError(
        title = "Storage Error",
        message = "Could not save data",
        isRetryable = true,
    )
    is AppError.Server -> UiError(
        title = "Server Error",
        message = "Something went wrong. Please try later.",
        isRetryable = true,
    )
    is AppError.Api -> UiError(
        title = "Request Failed",
        message = message ?: "HTTP $code",
        isRetryable = code in 500..599,
    )
    is AppError.Validation -> UiError(
        title = "Validation Error",
        message = message ?: "Invalid input",
        isRetryable = false,
    )
    is AppError.Unknown -> UiError(
        title = "Something went wrong",
        message = origin?.message ?: "An unexpected error occurred",
        isRetryable = true,
    )
}
