package com.shoptourr.api.v1.dto.auth

/**
 * POST /auth/forgot-password — always 204 (no user enumeration).
 */
data class ForgotPasswordRequest(
    val email: String,
)
