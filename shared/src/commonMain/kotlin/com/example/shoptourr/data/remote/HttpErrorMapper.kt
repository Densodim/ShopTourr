package com.example.shoptourr.data.remote

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.error.mapFailure
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.util.network.UnresolvedAddressException

fun Throwable.toHttpAppError(): AppError = when (this) {
    is AppError -> this
    is ClientRequestException -> when (response.status) {
        HttpStatusCode.Unauthorized -> AppError.Unauthorized
        HttpStatusCode.NotFound -> AppError.NotFound
        HttpStatusCode.Conflict -> AppError.Conflict
        HttpStatusCode.UnprocessableEntity -> AppError.Validation(message)
        else -> AppError.Api(response.status.value, message)
    }
    is ServerResponseException -> AppError.Server(response.status.value)
    is HttpRequestTimeoutException -> AppError.Timeout
    is UnresolvedAddressException -> AppError.Network
    else -> if (isNetworkFailure()) AppError.Network else AppError.Unknown(origin = this)
}

fun mapHttpStatus(status: HttpStatusCode, detail: String? = null): AppError = when (status) {
    HttpStatusCode.Unauthorized -> AppError.Unauthorized
    HttpStatusCode.NotFound -> AppError.NotFound
    HttpStatusCode.Conflict -> AppError.Conflict
    HttpStatusCode.UnprocessableEntity -> AppError.Validation(detail)
    else -> if (status.value in 500..599) {
        AppError.Server(status.value)
    } else {
        AppError.Api(status.value, detail ?: "HTTP ${status.value}")
    }
}

fun <T> Result<T>.mapHttpAppError(): Result<T> =
    mapFailure { it.toHttpAppError() }

private fun Throwable.isNetworkFailure(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        val name = current::class.simpleName.orEmpty()
        if (
            name.contains("IOException", ignoreCase = true) ||
            name.contains("ConnectException", ignoreCase = true) ||
            name.contains("SocketTimeout", ignoreCase = true) ||
            name.contains("UnknownHost", ignoreCase = true)
        ) {
            return true
        }
        current = current.cause
    }
    return false
}
