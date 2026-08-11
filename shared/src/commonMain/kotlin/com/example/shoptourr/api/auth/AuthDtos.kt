package com.example.shoptourr.api.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val displayName: String,
    val email: String,
    val password: String,
    val locale: String? = "ru",
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val deviceName: String? = null,
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
)

@Serializable
data class LogoutRequest(
    val refreshToken: String? = null,
    val allSessions: Boolean = false,
)

@Serializable
data class AuthUserDto(
    val id: String,
    val displayName: String,
    val email: String,
    val locale: String,
    val createdAt: String,
)

@Serializable
data class AuthTokensResponse(
    val accessToken: String,
    val accessExpiresIn: Long,
    val refreshToken: String,
    val refreshExpiresIn: Long,
    val tokenType: String = "Bearer",
    val user: AuthUserDto,
)
