package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.repository.AuthRepository
import com.example.shoptourr.domain.validation.RegisterForm
import com.example.shoptourr.domain.validation.RegisterFormValidator
import com.example.shoptourr.domain.validation.toValidationError

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
        val trimmedName = displayName.trim()
        val trimmedEmail = email.trim()
        RegisterFormValidator.validate(RegisterForm(trimmedName, trimmedEmail, password, locale))
            .toValidationError()
            ?.let { return Result.failure(it) }

        return authRepository.register(
            displayName = trimmedName,
            email = trimmedEmail,
            password = password,
            locale = locale,
        ).also { result ->
            if (result.isSuccess) {
                registerPushDevice?.invoke()
            }
        }
    }
}
