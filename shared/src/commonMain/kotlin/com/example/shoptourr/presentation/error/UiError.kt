package com.example.shoptourr.presentation.error

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.i18n.AppLocale
import com.example.shoptourr.i18n.VoyageI18n

data class UiError(
    val titleKey: String,
    val messageKey: String,
    val isRetryable: Boolean,
    val action: UiErrorAction? = null,
    val messageOverride: String? = null,
) {
    val title: String get() = title(VoyageI18n.currentLocale)
    val message: String get() = message(VoyageI18n.currentLocale)

    fun title(locale: AppLocale): String = VoyageI18n.t(locale, titleKey)
    fun message(locale: AppLocale): String = messageOverride ?: VoyageI18n.t(locale, messageKey)
}

sealed interface UiErrorAction {
    data object Logout : UiErrorAction
    data class Navigate(val route: String) : UiErrorAction
}

fun AppError.toUiError(): UiError = when (this) {
    AppError.Network -> UiError(
        titleKey = "error_network_title",
        messageKey = "error_network_message",
        isRetryable = true,
    )
    AppError.Timeout -> UiError(
        titleKey = "error_timeout_title",
        messageKey = "error_timeout_message",
        isRetryable = true,
    )
    AppError.Unauthorized -> UiError(
        titleKey = "error_unauthorized_title",
        messageKey = "error_unauthorized_message",
        isRetryable = false,
        action = UiErrorAction.Logout,
    )
    AppError.NotFound -> UiError(
        titleKey = "error_not_found_title",
        messageKey = "error_not_found_message",
        isRetryable = false,
    )
    AppError.Conflict -> UiError(
        titleKey = "error_conflict_title",
        messageKey = "error_conflict_message",
        isRetryable = true,
    )
    AppError.DatabaseError -> UiError(
        titleKey = "error_storage_title",
        messageKey = "error_storage_message",
        isRetryable = true,
    )
    is AppError.Server -> UiError(
        titleKey = "error_server_title",
        messageKey = "error_server_message",
        isRetryable = true,
    )
    is AppError.Api -> UiError(
        titleKey = "error_api_title",
        messageKey = "error_api_message",
        messageOverride = message ?: "HTTP $code",
        isRetryable = code in 500..599,
    )
    is AppError.Validation -> UiError(
        titleKey = "error_validation_title",
        messageKey = "error_validation_message",
        messageOverride = message,
        isRetryable = false,
    )
    AppError.Cancelled -> UiError(
        titleKey = "error_validation_title",
        messageKey = "error_validation_message",
        messageOverride = "",
        isRetryable = false,
    )
    is AppError.Unknown -> UiError(
        titleKey = "error_unknown_title",
        messageKey = "error_unknown_message",
        messageOverride = origin?.message,
        isRetryable = true,
    )
}
