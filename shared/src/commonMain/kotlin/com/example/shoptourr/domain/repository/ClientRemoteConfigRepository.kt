package com.example.shoptourr.domain.repository

import com.example.shoptourr.domain.model.ClientPlatform
import com.example.shoptourr.domain.model.ClientRemoteConfig
import kotlinx.coroutines.flow.Flow

interface AppBuildInfo {
    val platform: ClientPlatform
    val buildNumber: Int
}

interface ClientRemoteConfigRepository {
    fun observe(): Flow<ClientRemoteConfig?>
    suspend fun refresh(): Result<ClientRemoteConfig>
}
