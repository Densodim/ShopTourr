package com.shoptourr.api.v1.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank
    @field:Size(min = 2, max = 80)
    val displayName: String,

    @field:NotBlank
    @field:Email
    @field:Size(max = 254)
    val email: String,

    @field:NotBlank
    @field:Size(min = 6, max = 128)
    val password: String,

    /** Optional; default `ru`. */
    @field:Size(min = 2, max = 5)
    val locale: String? = null,
)
