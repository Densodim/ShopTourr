package com.example.shoptourr.domain.auth

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.SocialCredentials
import com.example.shoptourr.domain.model.SocialProvider

interface SocialAuthClient {
    suspend fun signIn(provider: SocialProvider, nonce: String): Result<SocialCredentials>
}

class UnavailableSocialAuthClient : SocialAuthClient {
    override suspend fun signIn(provider: SocialProvider, nonce: String): Result<SocialCredentials> =
        Result.failure(AppError.Validation("Social login is not available on this platform."))
}
