package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.auth.Pkce
import com.example.shoptourr.domain.auth.SocialAuthClient
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.model.SocialProvider
import com.example.shoptourr.domain.repository.AuthRepository

class SocialLoginUseCase(
    private val socialAuth: SocialAuthClient,
    private val authRepository: AuthRepository,
    private val registerPushDevice: RegisterPushDeviceUseCase? = null,
    private val nonce: () -> String = { Pkce.nonce() },
) {
    suspend operator fun invoke(provider: SocialProvider): Result<AuthSession> {
        val nonceValue = nonce()
        val credentials = socialAuth.signIn(provider, nonceValue).fold(
            onSuccess = { it },
            onFailure = { return Result.failure(it.asAppError()) },
        )
        if (credentials.idToken.isBlank()) {
            return Result.failure(AppError.Validation("idToken"))
        }
        return authRepository.loginSocial(
            provider = provider,
            idToken = credentials.idToken,
            nonce = nonceValue,
            displayName = credentials.displayName,
        ).also { result ->
            if (result.isSuccess) {
                registerPushDevice?.invoke()
            }
        }
    }
}
