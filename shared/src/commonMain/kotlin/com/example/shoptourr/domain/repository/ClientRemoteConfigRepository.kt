package com.example.shoptourr.domain.repository

import com.example.shoptourr.domain.model.ClientPlatform
import com.example.shoptourr.domain.model.ClientRemoteConfig
import kotlinx.coroutines.flow.Flow

interface AppBuildInfo {
    val platform: ClientPlatform
    val buildNumber: Int
    /** When true, certificate pinning may enforce (if pins are configured). */
    val isReleaseBuild: Boolean get() = false
}

interface ClientRemoteConfigRepository {
    fun observe(): Flow<ClientRemoteConfig?>
    suspend fun refresh(): Result<ClientRemoteConfig>
}
