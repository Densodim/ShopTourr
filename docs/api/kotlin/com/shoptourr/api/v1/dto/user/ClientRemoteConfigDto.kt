package com.shoptourr.api.v1.dto.user

/**
 * GET /me/app-config response mirror.
 */
data class FeatureFlagsDto(
    val exportPdf: Boolean = true,
    val ocrAssist: Boolean = true,
    val nativeMaps: Boolean = false,
)

data class ClientRemoteConfigDto(
    val minAndroidBuild: Int,
    val minIosBuild: Int,
    val softMinAndroidBuild: Int? = null,
    val softMinIosBuild: Int? = null,
    val flags: FeatureFlagsDto = FeatureFlagsDto(),
    val storeUrlAndroid: String? = null,
    val storeUrlIos: String? = null,
)
