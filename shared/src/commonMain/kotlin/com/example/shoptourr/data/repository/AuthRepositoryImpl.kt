package com.example.shoptourr.data.repository

import com.example.shoptourr.data.remote.dto.auth.AuthTokensResponse
import com.example.shoptourr.data.remote.dto.auth.LoginRequest
import com.example.shoptourr.data.remote.dto.auth.LogoutRequest
import com.example.shoptourr.data.remote.dto.auth.RefreshTokenRequest
import com.example.shoptourr.data.remote.dto.auth.RegisterRequest
import com.example.shoptourr.data.remote.AuthApi
import com.example.shoptourr.data.remote.mapHttpAppError
import com.example.shoptourr.data.settings.TokenStore
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.model.User
import com.example.shoptourr.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
) : AuthRepository {

    private var cachedUser: User? = null

    override suspend fun login(email: String, password: String, deviceName: String?): Result<AuthSession> =
        runCatching {
            api.login(LoginRequest(email = email, password = password, deviceName = deviceName)).toSession()
        }.mapHttpAppError()

    override suspend fun register(
        displayName: String,
        email: String,
        password: String,
        locale: String,
    ): Result<AuthSession> =
        runCatching {
            api.register(
                RegisterRequest(
                    displayName = displayName,
                    email = email,
                    password = password,
                    locale = locale,
                )
            ).toSession()
        }.mapHttpAppError()

    override suspend fun refresh(): Result<AuthSession> =
        runCatching {
            val refresh = tokenStore.refreshToken() ?: throw AppError.Unauthorized
            api.refresh(RefreshTokenRequest(refresh)).toSession()
        }.mapHttpAppError()

    override suspend fun logout(allSessions: Boolean): Result<Unit> =
        runCatching {
            api.logout(
                LogoutRequest(
                    refreshToken = tokenStore.refreshToken(),
                    allSessions = allSessions,
                )
            )
            tokenStore.clear()
            cachedUser = null
        }.mapHttpAppError()

    override fun currentUser(): User? = cachedUser

    override fun isLoggedIn(): Boolean = tokenStore.accessToken() != null

    private fun AuthTokensResponse.toSession(): AuthSession {
        tokenStore.saveTokens(accessToken, refreshToken)
        val domainUser = User(
            id = user.id,
            displayName = user.displayName,
            email = user.email,
            locale = user.locale,
        )
        cachedUser = domainUser
        return AuthSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessExpiresIn = accessExpiresIn,
            refreshExpiresIn = refreshExpiresIn,
            user = domainUser,
        )
    }
}
