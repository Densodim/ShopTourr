package com.shoptourr.api.v1.dto.trip

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/** Invite a real account by email onto a shared trip (P3). */
data class InviteTravelerRequest(
    @field:NotBlank
    @field:Size(max = 254)
    val email: String,

    @field:Size(min = 1, max = 60)
    val displayNameHint: String? = null,
)
