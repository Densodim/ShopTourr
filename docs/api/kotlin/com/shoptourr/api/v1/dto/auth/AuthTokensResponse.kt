package com.shoptourr.api.v1.dto.auth

data class AuthTokensResponse(
    val accessToken: String,
    /** Seconds until access expiry. */
    val accessExpiresIn: Long,
    val refreshToken: String,
    val refreshExpiresIn: Long,
    tokenType: String? = null,
    val user: AuthUserDto,
) {
    val tokenType: String = if (tokenType.isNullOrBlank()) "Bearer" else tokenType
}
