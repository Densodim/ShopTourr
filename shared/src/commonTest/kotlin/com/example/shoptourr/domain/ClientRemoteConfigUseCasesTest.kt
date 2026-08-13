package com.example.shoptourr.domain

import app.cash.turbine.test
import com.example.shoptourr.domain.model.ClientPlatform
import com.example.shoptourr.domain.model.ClientRemoteConfig
import com.example.shoptourr.domain.model.FeatureFlags
import com.example.shoptourr.domain.model.ForceUpdateAction
import com.example.shoptourr.domain.repository.AppBuildInfo
import com.example.shoptourr.domain.repository.ClientRemoteConfigRepository
import com.example.shoptourr.domain.usecase.EvaluateForceUpdateUseCase
import com.example.shoptourr.domain.usecase.ObserveFeatureFlagUseCase
import com.example.shoptourr.domain.usecase.RefreshClientRemoteConfigUseCase
import com.example.shoptourr.domain.model.FeatureFlag
import com.example.shoptourr.fake.FakeClientRemoteConfigRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class EvaluateForceUpdateUseCaseTest {

    @Test
    fun `returns hard decision from refreshed config`() = runTest {
        val repo = FakeClientRemoteConfigRepository(
            config = ClientRemoteConfig(
                minAndroidBuild = 30,
                minIosBuild = 30,
                softMinAndroidBuild = 40,
                flags = FeatureFlags(),
                storeUrlAndroid = "https://play.google.com/voyage",
            ),
        )
        val useCase = EvaluateForceUpdateUseCase(
            buildInfo = FixedAppBuildInfo(ClientPlatform.ANDROID, 10),
            repository = repo,
        )
        val decision = useCase().getOrThrow()
        assertEquals(ForceUpdateAction.HARD, decision.action)
        assertEquals("https://play.google.com/voyage", decision.storeUrl)
        assertEquals(1, repo.refreshCalls)
    }
}

class RefreshClientRemoteConfigUseCaseTest {

    @Test
    fun `refresh delegates to repository`() = runTest {
        val repo = FakeClientRemoteConfigRepository(
            config = ClientRemoteConfig(minAndroidBuild = 1, minIosBuild = 1),
        )
        val useCase = RefreshClientRemoteConfigUseCase(repo)
        assertTrue(useCase().isSuccess)
        assertEquals(1, repo.refreshCalls)
    }
}

class ObserveFeatureFlagUseCaseTest {

    @Test
    fun `emits flag from cached config`() = runTest {
        val repo = FakeClientRemoteConfigRepository(
            config = ClientRemoteConfig(
                minAndroidBuild = 1,
                minIosBuild = 1,
                flags = FeatureFlags(nativeMaps = true),
            ),
        )
        ObserveFeatureFlagUseCase(repo).invoke(FeatureFlag.NATIVE_MAPS).test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `defaults to FeatureFlags when config missing`() = runTest {
        val repo = FakeClientRemoteConfigRepository(config = null)
        ObserveFeatureFlagUseCase(repo).invoke(FeatureFlag.OCR_ASSIST).test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        ObserveFeatureFlagUseCase(repo).invoke(FeatureFlag.NATIVE_MAPS).test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private class FixedAppBuildInfo(
    override val platform: ClientPlatform,
    override val buildNumber: Int,
) : AppBuildInfo
