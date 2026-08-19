package com.example.shoptourr.domain

import com.example.shoptourr.data.push.InMemoryRegisteredPushDeviceStore
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.model.User
import com.example.shoptourr.domain.session.RecordingAuthTokenCache
import com.example.shoptourr.domain.usecase.LogoutUseCase
import com.example.shoptourr.domain.usecase.UnregisterPushDeviceUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import com.example.shoptourr.fake.FakeLocalSessionStore
import com.example.shoptourr.fake.FakePushRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class LogoutUseCaseTest {

    private val session = AuthSession(
        accessToken = "a",
        refreshToken = "r",
        accessExpiresIn = 1,
        refreshExpiresIn = 1,
        user = User("u1", "Mila", "m@v.app", "ru"),
    )

    @Test
    fun `logout wipes local session after remote success`() = runTest {
        val auth = FakeAuthRepository(session = session)
        val local = FakeLocalSessionStore()

        LogoutUseCase(auth, local)().getOrThrow()

        assertEquals(1, local.clearCalls)
        assertTrue(!auth.isLoggedIn())
    }

    @Test
    fun `logout wipes local session even when remote logout fails`() = runTest {
        val auth = FakeAuthRepository(session = session, logoutError = AppError.Network)
        val local = FakeLocalSessionStore()

        val result = LogoutUseCase(auth, local)()

        assertTrue(result.isSuccess)
        assertEquals(1, local.clearCalls)
    }

    @Test
    fun `logout unregisters push then clears bearer cache while tokens still work`() = runTest {
        val auth = FakeAuthRepository(session = session)
        val local = FakeLocalSessionStore()
        val push = FakePushRepository()
        val devices = InMemoryRegisteredPushDeviceStore().apply { save("dev-1") }
        val cache = RecordingAuthTokenCache()

        LogoutUseCase(
            authRepository = auth,
            localSessionStore = local,
            unregisterPushDevice = UnregisterPushDeviceUseCase(push, devices),
            authTokenCache = cache,
        )().getOrThrow()

        assertEquals(1, push.unregisterCalls)
        assertEquals("dev-1", push.lastUnregisteredId)
        assertNull(devices.deviceId())
        assertEquals(1, cache.clearCalls)
        assertEquals(1, local.clearCalls)
        assertTrue(!auth.isLoggedIn())
    }

    @Test
    fun `logout still clears session when push unregister fails`() = runTest {
        val auth = FakeAuthRepository(session = session)
        val local = FakeLocalSessionStore()
        val push = FakePushRepository().apply { unregisterError = AppError.Network }
        val devices = InMemoryRegisteredPushDeviceStore().apply { save("dev-1") }
        val cache = RecordingAuthTokenCache()

        val result = LogoutUseCase(
            authRepository = auth,
            localSessionStore = local,
            unregisterPushDevice = UnregisterPushDeviceUseCase(push, devices),
            authTokenCache = cache,
        )()

        assertTrue(result.isSuccess)
        assertEquals(1, push.unregisterCalls)
        assertEquals("dev-1", devices.deviceId())
        assertEquals(1, cache.clearCalls)
        assertEquals(1, local.clearCalls)
    }
}
