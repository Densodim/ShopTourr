package com.shoptourr.api.v1.dto.push

import java.time.Instant
import java.util.UUID

data class DeviceDto(
    val id: UUID,
    val tokenFingerprint: String,
    val platform: PushPlatform,
    val appVersion: String?,
    val deviceName: String?,
    val createdAt: Instant,
    val lastSeenAt: Instant?,
)
