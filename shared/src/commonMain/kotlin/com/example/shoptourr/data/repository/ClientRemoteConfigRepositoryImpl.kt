package com.example.shoptourr.data.repository

import com.example.shoptourr.data.local.ClientRemoteConfigStore
import com.example.shoptourr.data.remote.UserApi
import com.example.shoptourr.data.remote.dto.user.ClientRemoteConfigDto
import com.example.shoptourr.data.remote.dto.user.FeatureFlagsDto
import com.example.shoptourr.data.remote.mapHttpAppError
import com.example.shoptourr.domain.model.ClientRemoteConfig
import com.example.shoptourr.domain.model.FeatureFlags
import com.example.shoptourr.domain.repository.ClientRemoteConfigRepository
import kotlinx.coroutines.flow.Flow

class ClientRemoteConfigRepositoryImpl(
    private val api: UserApi,
    private val localStore: ClientRemoteConfigStore,
) : ClientRemoteConfigRepository {

    override fun observe(): Flow<ClientRemoteConfig?> = localStore.observe()

    override suspend fun refresh(): Result<ClientRemoteConfig> =
        runCatching {
            val config = api.fetchAppConfig().toDomain()
            localStore.save(config)
            config
        }.mapHttpAppError()
}

internal fun ClientRemoteConfigDto.toDomain(): ClientRemoteConfig =
    ClientRemoteConfig(
        minAndroidBuild = minAndroidBuild,
        minIosBuild = minIosBuild,
        softMinAndroidBuild = softMinAndroidBuild,
        softMinIosBuild = softMinIosBuild,
        flags = flags.toDomain(),
        storeUrlAndroid = storeUrlAndroid,
        storeUrlIos = storeUrlIos,
    )

private fun FeatureFlagsDto.toDomain(): FeatureFlags =
    FeatureFlags(
        exportPdf = exportPdf,
        ocrAssist = ocrAssist,
        nativeMaps = nativeMaps,
    )
