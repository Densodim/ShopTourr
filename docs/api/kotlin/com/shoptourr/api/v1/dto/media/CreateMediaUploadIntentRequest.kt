package com.shoptourr.api.v1.dto.media

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CreateMediaUploadIntentRequest(
    @field:NotNull
    val purpose: MediaPurpose,

    @field:NotBlank
    @field:Size(max = 128)
    val contentType: String,

    @field:Positive
    val byteSize: Long,

    /** Optional client checksum (sha256 hex). */
    @field:Size(min = 64, max = 64)
    val sha256Hex: String? = null,
)
