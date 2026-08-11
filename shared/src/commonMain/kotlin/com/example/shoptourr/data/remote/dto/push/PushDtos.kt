package com.example.shoptourr.data.remote.dto.push

import kotlinx.serialization.Serializable

@Serializable
enum class PushPlatformDto {
    ANDROID,
    IOS,
}

@Serializable
data class RegisterDeviceRequest(
    val token: String,
    val platform: PushPlatformDto,
    val appVersion: String? = null,
    val deviceName: String? = null,
)

@Serializable
data class DeviceDto(
    val id: String,
    val tokenFingerprint: String,
    val platform: PushPlatformDto,
    val appVersion: String? = null,
    val deviceName: String? = null,
    val createdAt: String,
    val lastSeenAt: String? = null,
)
