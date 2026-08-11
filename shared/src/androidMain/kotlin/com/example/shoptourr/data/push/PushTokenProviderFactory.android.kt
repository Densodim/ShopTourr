package com.example.shoptourr.data.push

import com.example.shoptourr.domain.model.PushPlatform
import com.example.shoptourr.domain.push.PushTokenProvider

actual fun createDefaultPushTokenProvider(): PushTokenProvider =
    NoOpPushTokenProvider(platform = PushPlatform.ANDROID, appVersion = "android")
