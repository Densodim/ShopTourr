package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.PushPlatform
import com.example.shoptourr.domain.push.NotificationPermissionGate
import com.example.shoptourr.domain.push.PushTokenProvider
import com.example.shoptourr.domain.usecase.RegisterPushDeviceUseCase
import com.example.shoptourr.fake.FakePushRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class RegisterPushDeviceUseCaseTest {

    @Test
    fun `registers when token is available`() = runTest {
        val repo = FakePushRepository()
        val useCase = RegisterPushDeviceUseCase(
            pushRepository = repo,
            tokenProvider = object : PushTokenProvider {
                override val platform = PushPlatform.ANDROID
                override val appVersion = "1.0.0"
                override val deviceName = "Pixel"
                override suspend fun currentToken() = "fcm-token-1"
            },
            notificationPermission = NotificationPermissionGate { true },
        )
        val device = useCase().getOrThrow()
        assertEquals("fcm-token-1", repo.lastDraft?.token)
        assertEquals(PushPlatform.ANDROID, repo.lastDraft?.platform)
        assertEquals("dev-1", device?.id)
        assertEquals(1, repo.registerCalls)
    }

    @Test
    fun `asks for notification permission before reading the push token`() = runTest {
        val order = mutableListOf<String>()
        val repo = FakePushRepository()
        val useCase = RegisterPushDeviceUseCase(
            pushRepository = repo,
            tokenProvider = object : PushTokenProvider {
                override val platform = PushPlatform.ANDROID
                override val appVersion = null
                override val deviceName = null
                override suspend fun currentToken(): String {
                    order += "token"
                    return "tok"
                }
            },
            notificationPermission = NotificationPermissionGate {
                order += "permission"
                true
            },
        )
        useCase().getOrThrow()
        assertEquals(listOf("permission", "token"), order)
        assertEquals(1, repo.registerCalls)
    }

    @Test
    fun `skips token and register when the user denies notifications`() = runTest {
        var tokenReads = 0
        val repo = FakePushRepository()
        val useCase = RegisterPushDeviceUseCase(
            pushRepository = repo,
            tokenProvider = object : PushTokenProvider {
                override val platform = PushPlatform.IOS
                override val appVersion = null
                override val deviceName = null
                override suspend fun currentToken(): String {
                    tokenReads += 1
                    return "should-not-read"
                }
            },
            notificationPermission = NotificationPermissionGate { false },
        )
        assertNull(useCase().getOrThrow())
        assertEquals(0, tokenReads)
        assertEquals(0, repo.registerCalls)
    }

    @Test
    fun `skips when token missing`() = runTest {
        val repo = FakePushRepository()
        val useCase = RegisterPushDeviceUseCase(
            pushRepository = repo,
            tokenProvider = object : PushTokenProvider {
                override val platform = PushPlatform.IOS
                override val appVersion = null
                override val deviceName = null
                override suspend fun currentToken() = null
            },
        )
        assertNull(useCase().getOrThrow())
        assertEquals(0, repo.registerCalls)
    }

    @Test
    fun `propagates repository failure`() = runTest {
        val repo = FakePushRepository(registerError = IllegalStateException("boom"))
        val useCase = RegisterPushDeviceUseCase(
            pushRepository = repo,
            tokenProvider = object : PushTokenProvider {
                override val platform = PushPlatform.ANDROID
                override val appVersion = null
                override val deviceName = null
                override suspend fun currentToken() = "tok"
            },
        )
        assertTrue(useCase().isFailure)
    }
}
