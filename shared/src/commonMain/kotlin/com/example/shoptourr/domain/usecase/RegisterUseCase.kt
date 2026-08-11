package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.repository.AuthRepository

class RegisterUseCase(
    private val authRepository: AuthRepository,
    private val registerPushDevice: RegisterPushDeviceUseCase? = null,
) {
    suspend operator fun invoke(
        displayName: String,
        email: String,
        password: String,
        locale: String = "ru",
    ): Result<AuthSession> {
        if (displayName.isBlank()) return Result.failure(AppError.Validation("displayName"))
        if (email.isBlank() || !email.contains("@")) return Result.failure(AppError.Validation("email"))
        if (password.length < 8) return Result.failure(AppError.Validation("password"))
        return authRepository.register(
            displayName = displayName.trim(),
            email = email.trim(),
            password = password,
            locale = locale,
        ).also { result ->
            if (result.isSuccess) {
                registerPushDevice?.invoke()
            }
        }
    }
}
