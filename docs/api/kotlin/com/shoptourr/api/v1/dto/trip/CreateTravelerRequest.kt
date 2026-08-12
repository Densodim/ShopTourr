package com.shoptourr.api.v1.dto.trip

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateTravelerRequest(
    @field:NotBlank
    @field:Size(min = 1, max = 60)
    val name: String,

    @field:NotBlank
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    val colorHex: String,

    @field:Size(min = 1, max = 2)
    val avatarGlyph: String? = null,
)
