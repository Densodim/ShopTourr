package com.example.shoptourr.data.platform

import com.example.shoptourr.domain.model.ClientPlatform
import com.example.shoptourr.domain.repository.AppBuildInfo

actual fun createDefaultAppBuildInfo(): AppBuildInfo =
    StaticAppBuildInfo(platform = ClientPlatform.ANDROID, buildNumber = 1)
