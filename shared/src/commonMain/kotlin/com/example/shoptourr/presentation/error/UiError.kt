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
        title = "Нет сети",
        message = "Проверьте подключение к интернету",
        isRetryable = true,
    )
    AppError.Timeout -> UiError(
        title = "Таймаут",
        message = "Сервер не ответил вовремя",
        isRetryable = true,
    )
    AppError.Unauthorized -> UiError(
        title = "Сессия истекла",
        message = "Войдите в аккаунт снова",
        isRetryable = false,
        action = UiErrorAction.Logout,
    )
    AppError.NotFound -> UiError(
        title = "Не найдено",
        message = "Запрошенные данные отсутствуют",
        isRetryable = false,
    )
    AppError.Conflict -> UiError(
        title = "Конфликт",
        message = "Изменение конфликтует с данными на сервере",
        isRetryable = true,
    )
    AppError.DatabaseError -> UiError(
        title = "Ошибка хранения",
        message = "Не удалось сохранить данные",
        isRetryable = true,
    )
    is AppError.Server -> UiError(
        title = "Ошибка сервера",
        message = "Что-то пошло не так. Попробуйте позже",
        isRetryable = true,
    )
    is AppError.Api -> UiError(
        title = "Запрос не выполнен",
        message = message ?: "HTTP $code",
        isRetryable = code in 500..599,
    )
    is AppError.Validation -> UiError(
        title = "Проверьте поля",
        message = message ?: "Некорректные данные",
        isRetryable = false,
    )
    is AppError.Unknown -> UiError(
        title = "Что-то пошло не так",
        message = origin?.message ?: "Непредвиденная ошибка",
        isRetryable = true,
    )
}
