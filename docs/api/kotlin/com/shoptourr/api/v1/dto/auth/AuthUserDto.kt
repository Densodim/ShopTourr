package com.shoptourr.api.v1.dto.auth

import java.time.Instant
import java.util.UUID

data class AuthUserDto(
    val id: UUID,
    val displayName: String,
    val email: String,
    val locale: String,
    val createdAt: Instant,
)
