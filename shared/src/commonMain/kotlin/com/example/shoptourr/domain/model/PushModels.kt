package com.example.shoptourr.domain.model

enum class PushPlatform {
    ANDROID,
    IOS,
}

data class PushDevice(
    val id: String,
    val tokenFingerprint: String,
    val platform: PushPlatform,
    val appVersion: String? = null,
    val deviceName: String? = null,
    val createdAt: String,
    val lastSeenAt: String? = null,
)

data class RegisterDeviceDraft(
    val token: String,
    val platform: PushPlatform,
    val appVersion: String? = null,
    val deviceName: String? = null,
)
