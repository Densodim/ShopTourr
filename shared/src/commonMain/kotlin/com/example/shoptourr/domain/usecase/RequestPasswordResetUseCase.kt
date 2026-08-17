package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.repository.AuthRepository
import com.example.shoptourr.domain.validation.ForgotPasswordForm
import com.example.shoptourr.domain.validation.ForgotPasswordFormValidator
import com.example.shoptourr.domain.validation.toValidationError

class RequestPasswordResetUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        val trimmed = email.trim()
        ForgotPasswordFormValidator.validate(ForgotPasswordForm(trimmed))
            .toValidationError()
            ?.let { return Result.failure(it) }

        return authRepository.requestPasswordReset(trimmed)
    }
}
