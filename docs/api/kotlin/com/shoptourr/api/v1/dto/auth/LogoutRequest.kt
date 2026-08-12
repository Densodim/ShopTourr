package com.shoptourr.api.v1.dto.auth

data class LogoutRequest(
    /** If null — revoke current session only; if present — that refresh token. */
    val refreshToken: String? = null,
    /** If true — revoke all sessions for user. */
    val allSessions: Boolean = false,
)
