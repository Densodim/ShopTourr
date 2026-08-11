package com.example.shoptourr.domain.error

sealed class AppError(message: String? = null) : Exception(message) {
    data object Network : AppError("network")
    data object Timeout : AppError("timeout")
    data object Unauthorized : AppError("unauthorized")
    data object NotFound : AppError("not_found")
    data object Conflict : AppError("conflict")
    data object DatabaseError : AppError("database")
    data class Api(val code: Int, override val message: String?) : AppError(message)
    data class Server(val code: Int) : AppError("server:$code")
    data class Validation(override val message: String?) : AppError(message)
    /** Use [origin], never a field named cause — reserved by Throwable. */
    data class Unknown(val origin: Throwable? = null) : AppError(origin?.message)
}

fun Throwable.asAppError(): AppError =
    this as? AppError ?: AppError.Unknown(origin = this)

inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> =
    fold(onSuccess = { transform(it) }, onFailure = { Result.failure(it) })

inline fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> =
    fold(onSuccess = { this }, onFailure = { Result.failure(transform(it)) })

fun <T> Result<T>.mapAppError(): Result<T> =
    mapFailure { it.asAppError() }
