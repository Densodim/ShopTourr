package com.example.shoptourr.data.platform

import com.example.shoptourr.domain.model.ClientPlatform
import com.example.shoptourr.domain.repository.AppBuildInfo

class StaticAppBuildInfo(
    override val platform: ClientPlatform,
    override val buildNumber: Int,
    override val isReleaseBuild: Boolean = false,
) : AppBuildInfo

expect fun createDefaultAppBuildInfo(): AppBuildInfo
