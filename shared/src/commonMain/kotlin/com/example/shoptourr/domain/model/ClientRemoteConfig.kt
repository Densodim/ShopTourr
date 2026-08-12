package com.example.shoptourr.domain.model

enum class ClientPlatform {
    ANDROID,
    IOS,
}

enum class ForceUpdateAction {
    NONE,
    SOFT,
    HARD,
}

enum class FeatureFlag {
    EXPORT_PDF,
    OCR_ASSIST,
    NATIVE_MAPS,
}

data class FeatureFlags(
    val exportPdf: Boolean = true,
    val ocrAssist: Boolean = true,
    val nativeMaps: Boolean = false,
) {
    fun isEnabled(flag: FeatureFlag): Boolean = when (flag) {
        FeatureFlag.EXPORT_PDF -> exportPdf
        FeatureFlag.OCR_ASSIST -> ocrAssist
        FeatureFlag.NATIVE_MAPS -> nativeMaps
    }
}

/**
 * Remote client config from `GET /me/app-config`.
 * Named to avoid clashing with DI [com.example.shoptourr.di.AppConfig] (API base URL).
 */
data class ClientRemoteConfig(
    val minAndroidBuild: Int,
    val minIosBuild: Int,
    val softMinAndroidBuild: Int? = null,
    val softMinIosBuild: Int? = null,
    val flags: FeatureFlags = FeatureFlags(),
    val storeUrlAndroid: String? = null,
    val storeUrlIos: String? = null,
) {
    fun minBuild(platform: ClientPlatform): Int = when (platform) {
        ClientPlatform.ANDROID -> minAndroidBuild
        ClientPlatform.IOS -> minIosBuild
    }

    fun softMinBuild(platform: ClientPlatform): Int? = when (platform) {
        ClientPlatform.ANDROID -> softMinAndroidBuild
        ClientPlatform.IOS -> softMinIosBuild
    }

    fun storeUrl(platform: ClientPlatform): String? = when (platform) {
        ClientPlatform.ANDROID -> storeUrlAndroid
        ClientPlatform.IOS -> storeUrlIos
    }
}

object ForceUpdateEvaluator {
    fun evaluate(
        platform: ClientPlatform,
        currentBuild: Int,
        config: ClientRemoteConfig,
    ): ForceUpdateAction {
        val min = config.minBuild(platform)
        if (currentBuild < min) return ForceUpdateAction.HARD
        val softMin = config.softMinBuild(platform)
        if (softMin != null && currentBuild < softMin) return ForceUpdateAction.SOFT
        return ForceUpdateAction.NONE
    }
}
