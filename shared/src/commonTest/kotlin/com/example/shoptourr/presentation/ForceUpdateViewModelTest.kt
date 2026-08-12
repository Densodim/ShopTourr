package com.example.shoptourr.presentation

import app.cash.turbine.test
import com.example.shoptourr.domain.model.ClientPlatform
import com.example.shoptourr.domain.model.ClientRemoteConfig
import com.example.shoptourr.domain.model.ForceUpdateAction
import com.example.shoptourr.domain.repository.AppBuildInfo
import com.example.shoptourr.domain.usecase.EvaluateForceUpdateUseCase
import com.example.shoptourr.fake.FakeClientRemoteConfigRepository
import com.example.shoptourr.presentation.forceupdate.ForceUpdateIntent
import com.example.shoptourr.presentation.forceupdate.ForceUpdateViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ForceUpdateViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads hard update from config`() = runTest {
        val repo = FakeClientRemoteConfigRepository(
            config = ClientRemoteConfig(
                minAndroidBuild = 50,
                minIosBuild = 50,
                storeUrlAndroid = "https://play.google.com/voyage",
            ),
        )
        val vm = ForceUpdateViewModel(
            evaluateForceUpdate = EvaluateForceUpdateUseCase(
                buildInfo = object : AppBuildInfo {
                    override val platform = ClientPlatform.ANDROID
                    override val buildNumber = 10
                },
                repository = repo,
            ),
        )
        vm.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(ForceUpdateAction.HARD, state.action)
            assertEquals("https://play.google.com/voyage", state.storeUrl)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismiss soft clears action`() = runTest {
        val repo = FakeClientRemoteConfigRepository(
            config = ClientRemoteConfig(
                minAndroidBuild = 10,
                minIosBuild = 10,
                softMinAndroidBuild = 30,
            ),
        )
        val vm = ForceUpdateViewModel(
            evaluateForceUpdate = EvaluateForceUpdateUseCase(
                buildInfo = object : AppBuildInfo {
                    override val platform = ClientPlatform.ANDROID
                    override val buildNumber = 20
                },
                repository = repo,
            ),
        )
        vm.onIntent(ForceUpdateIntent.DismissSoft)
        assertEquals(ForceUpdateAction.NONE, vm.state.value.action)
    }
}
