package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.repository.AuthRepository
import com.example.shoptourr.domain.validation.ResetPasswordForm
import com.example.shoptourr.domain.validation.ResetPasswordFormValidator
import com.example.shoptourr.domain.validation.toValidationError

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
        ResetPasswordFormValidator.validate(ResetPasswordForm(trimmedEmail, trimmedToken, newPassword))
            .toValidationError()
            ?.let { return Result.failure(it) }

        return authRepository.resetPassword(trimmedEmail, trimmedToken, newPassword)
    }
}
