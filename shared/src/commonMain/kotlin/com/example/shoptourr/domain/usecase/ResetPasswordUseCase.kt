package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.repository.AuthRepository

/**
 * Completes a password reset started by [RequestPasswordResetUseCase].
 * Bounds mirror the server contract, so an obviously malformed token or
 * password is rejected before a round trip.
 */
class ResetPasswordUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        token: String,
        newPassword: String,
    ): Result<Unit> {
        val trimmedEmail = email.trim()
        val trimmedToken = token.trim()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains('@')) {
            return Result.failure(AppError.Validation("email"))
        }
        if (trimmedToken.length !in TOKEN_LENGTH) {
            return Result.failure(AppError.Validation("token"))
        }
        if (newPassword.length !in PASSWORD_LENGTH) {
            return Result.failure(AppError.Validation("newPassword"))
        }
        return authRepository.resetPassword(trimmedEmail, trimmedToken, newPassword)
    }

    private companion object {
        val TOKEN_LENGTH = 16..128
        val PASSWORD_LENGTH = 6..128
    }
}
