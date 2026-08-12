package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.repository.AuthRepository

class RequestPasswordResetUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        val trimmed = email.trim()
        if (trimmed.isBlank() || !trimmed.contains('@')) {
            return Result.failure(AppError.Validation("email"))
        }
        return authRepository.requestPasswordReset(trimmed)
    }
}
