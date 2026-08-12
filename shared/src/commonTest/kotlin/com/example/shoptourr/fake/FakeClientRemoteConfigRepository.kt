package com.example.shoptourr.fake

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.ClientRemoteConfig
import com.example.shoptourr.domain.repository.ClientRemoteConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeClientRemoteConfigRepository(
    config: ClientRemoteConfig? = null,
    var error: AppError? = null,
) : ClientRemoteConfigRepository {
    private val state = MutableStateFlow(config)
    var refreshCalls: Int = 0
        private set

    override fun observe(): Flow<ClientRemoteConfig?> = state

    override suspend fun refresh(): Result<ClientRemoteConfig> {
        refreshCalls += 1
        error?.let { return Result.failure(it) }
        val value = state.value ?: return Result.failure(AppError.NotFound)
        return Result.success(value)
    }

    fun seed(config: ClientRemoteConfig?) {
        state.value = config
    }
}
