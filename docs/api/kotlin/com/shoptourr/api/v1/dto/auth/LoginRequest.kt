package com.shoptourr.api.v1.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    @field:Size(min = 1, max = 128)
    val password: String,

    /** Optional device label for session list ("iPhone 15"). */
    @field:Size(max = 120)
    val deviceName: String? = null,
)
