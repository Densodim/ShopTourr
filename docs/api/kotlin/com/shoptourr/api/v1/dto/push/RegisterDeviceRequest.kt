package com.shoptourr.api.v1.dto.push

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class RegisterDeviceRequest(
    @field:NotBlank
    @field:Size(max = 512)
    val token: String,

    @field:NotNull
    val platform: PushPlatform,

    @field:Size(max = 64)
    val appVersion: String? = null,

    @field:Size(max = 120)
    val deviceName: String? = null,
)
