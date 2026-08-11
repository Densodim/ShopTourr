package com.example.shoptourr.data.push

import com.example.shoptourr.domain.model.PushPlatform
import com.example.shoptourr.domain.push.PushTokenProvider
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

class ApnsPushTokenProvider(
    override val appVersion: String? =
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String,
    override val deviceName: String? = UIDevice.currentDevice.name,
) : PushTokenProvider {
    override val platform: PushPlatform = PushPlatform.IOS

    override suspend fun currentToken(): String? = DevicePushTokenHolder.token
}

actual fun createDefaultPushTokenProvider(): PushTokenProvider = ApnsPushTokenProvider()
