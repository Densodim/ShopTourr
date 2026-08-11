package com.example.shoptourr.domain.push

import com.example.shoptourr.domain.model.PushPlatform

/**
 * Platform FCM/APNs token source. Returns null until the OS push SDK is ready.
 */
interface PushTokenProvider {
    val platform: PushPlatform
    val appVersion: String?
    val deviceName: String?
    suspend fun currentToken(): String?
}
