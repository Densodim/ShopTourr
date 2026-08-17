package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.repository.AuthRepository
import com.example.shoptourr.domain.validation.LoginForm
import com.example.shoptourr.domain.validation.LoginFormValidator
import com.example.shoptourr.domain.validation.toValidationError

class LoginUseCase(
    private val authRepository: AuthRepository,
    private val registerPushDevice: RegisterPushDeviceUseCase? = null,
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthSession> {
        val normalizedEmail = email.trim()
        LoginFormValidator.validate(LoginForm(normalizedEmail, password))
            .toValidationError()
            ?.let { return Result.failure(it) }

        return authRepository.login(normalizedEmail, password)
            .also { result ->
                if (result.isSuccess) {
                    registerPushDevice?.invoke()
                }
            }
    }
}
