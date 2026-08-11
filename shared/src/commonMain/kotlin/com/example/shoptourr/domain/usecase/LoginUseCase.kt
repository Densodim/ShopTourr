package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository,
    private val registerPushDevice: RegisterPushDeviceUseCase? = null,
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthSession> {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isEmpty()) {
            return Result.failure(AppError.Validation("email"))
        }
        if (password.length < 6) {
            return Result.failure(AppError.Validation("password"))
        }
        return authRepository.login(normalizedEmail, password)
            .also { result ->
                if (result.isSuccess) {
                    registerPushDevice?.invoke()
                }
            }
    }
}
