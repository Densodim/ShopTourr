package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.model.ClientRemoteConfig
import com.example.shoptourr.domain.model.FeatureFlag
import com.example.shoptourr.domain.model.FeatureFlags
import com.example.shoptourr.domain.model.ForceUpdateDecision
import com.example.shoptourr.domain.model.ForceUpdateEvaluator
import com.example.shoptourr.domain.repository.AppBuildInfo
import com.example.shoptourr.domain.repository.ClientRemoteConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RefreshClientRemoteConfigUseCase(
    private val repository: ClientRemoteConfigRepository,
) {
    suspend operator fun invoke(): Result<ClientRemoteConfig> = repository.refresh()
}

class EvaluateForceUpdateUseCase(
    private val buildInfo: AppBuildInfo,
    private val repository: ClientRemoteConfigRepository,
) {
    suspend operator fun invoke(): Result<ForceUpdateDecision> {
        val config = repository.refresh().getOrElse { return Result.failure(it) }
        val action = ForceUpdateEvaluator.evaluate(
            platform = buildInfo.platform,
            currentBuild = buildInfo.buildNumber,
            config = config,
        )
        return Result.success(
            ForceUpdateDecision(
                action = action,
                storeUrl = config.storeUrl(buildInfo.platform),
                config = config,
            ),
        )
    }
}

class ObserveFeatureFlagUseCase(
    private val repository: ClientRemoteConfigRepository,
) {
    operator fun invoke(flag: FeatureFlag): Flow<Boolean> =
        repository.observe().map { config ->
            config?.flags?.isEnabled(flag) ?: FeatureFlags().isEnabled(flag)
        }
}
