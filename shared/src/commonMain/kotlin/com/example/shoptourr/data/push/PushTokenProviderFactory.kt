package com.example.shoptourr.data.push

import com.example.shoptourr.domain.model.PushPlatform
import com.example.shoptourr.domain.push.PushTokenProvider

/**
 * Default provider until FCM/APNs SDKs are wired.
 * Platforms may override via Koin with a real token source.
 */
class NoOpPushTokenProvider(
    override val platform: PushPlatform,
    override val appVersion: String? = null,
    override val deviceName: String? = null,
) : PushTokenProvider {
    override suspend fun currentToken(): String? = null
}

expect fun createDefaultPushTokenProvider(): PushTokenProvider
