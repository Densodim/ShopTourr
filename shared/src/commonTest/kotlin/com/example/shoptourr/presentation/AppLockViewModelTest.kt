package com.example.shoptourr.presentation

import com.example.shoptourr.domain.lock.AppLockStore
import com.example.shoptourr.domain.lock.BiometricAuthenticator
import com.example.shoptourr.domain.lock.BiometricAvailability
import com.example.shoptourr.domain.usecase.IsLoggedInUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import com.example.shoptourr.presentation.lock.AppLockIntent
import com.example.shoptourr.presentation.lock.AppLockViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AppLockViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `cold start with lock on covers the session`() = runTest {
        val vm = createVm(loggedIn = true, enabled = true)
        assertTrue(vm.state.value.locked)
        assertTrue(vm.state.value.enabled)
        vm.onCleared()
    }

    @Test
    fun `cold start while logged out stays unlocked`() = runTest {
        val vm = createVm(loggedIn = false, enabled = true)
        assertFalse(vm.state.value.locked)
        vm.onCleared()
    }

    @Test
    fun `backgrounding a logged-in session locks the app`() = runTest {
        val vm = createVm(loggedIn = true, enabled = true)
        vm.onIntent(AppLockIntent.Unlock)
        assertFalse(vm.state.value.locked)
        vm.onIntent(AppLockIntent.AppStopped)
        assertTrue(vm.state.value.locked)
        vm.onCleared()
    }

    @Test
    fun `backgrounding during the system prompt does not prompt twice`() = runTest {
        val gate = CompletableDeferred<Boolean>()
        val authenticator = FakeBiometricAuthenticator(gate = gate)
        val vm = createVm(loggedIn = true, enabled = true, authenticator = authenticator)
        vm.onIntent(AppLockIntent.Unlock)
        assertTrue(vm.state.value.authenticating)
        vm.onIntent(AppLockIntent.AppStopped)
        gate.complete(true)
        assertFalse(vm.state.value.locked)
        vm.onIntent(AppLockIntent.Bind)
        assertEquals(1, authenticator.authenticateCalls)
        vm.onCleared()
    }

    @Test
    fun `failed unlock leaves the overlay up`() = runTest {
        val authenticator = FakeBiometricAuthenticator(authenticateResult = false)
        val vm = createVm(loggedIn = true, enabled = true, authenticator = authenticator)
        vm.onIntent(AppLockIntent.Unlock)
        assertTrue(vm.state.value.locked)
        assertEquals(1, authenticator.authenticateCalls)
        vm.onCleared()
    }

    @Test
    fun `bind after a failed unlock does not spam the prompt`() = runTest {
        val authenticator = FakeBiometricAuthenticator(authenticateResult = false)
        val vm = createVm(loggedIn = true, enabled = true, authenticator = authenticator)
        vm.onIntent(AppLockIntent.Unlock)
        vm.onIntent(AppLockIntent.Bind)
        assertEquals(1, authenticator.authenticateCalls)
        assertTrue(vm.state.value.locked)
        vm.onCleared()
    }

    @Test
    fun `returning from background prompts once`() = runTest {
        val authenticator = FakeBiometricAuthenticator()
        val vm = createVm(loggedIn = true, enabled = true, authenticator = authenticator)
        vm.onIntent(AppLockIntent.Unlock)
        authenticator.authenticateCalls = 0
        vm.onIntent(AppLockIntent.AppStopped)
        vm.onIntent(AppLockIntent.Bind)
        assertEquals(1, authenticator.authenticateCalls)
        assertFalse(vm.state.value.locked)
        vm.onCleared()
    }

    @Test
    fun `enabling lock requires a successful prompt`() = runTest {
        val store = InMemoryAppLockStore()
        val authenticator = FakeBiometricAuthenticator()
        val vm = createVm(
            loggedIn = true,
            store = store,
            authenticator = authenticator,
        )
        vm.onIntent(AppLockIntent.SetEnabled(true))
        assertTrue(store.isEnabled())
        assertTrue(vm.state.value.enabled)
        assertFalse(vm.state.value.locked)
        assertEquals(1, authenticator.authenticateCalls)
        vm.onCleared()
    }

    @Test
    fun `enabling lock is refused when biometrics are missing`() = runTest {
        val store = InMemoryAppLockStore()
        val vm = createVm(
            loggedIn = true,
            store = store,
            authenticator = FakeBiometricAuthenticator(
                availability = BiometricAvailability.NOT_ENROLLED,
            ),
        )
        vm.onIntent(AppLockIntent.SetEnabled(true))
        assertFalse(store.isEnabled())
        assertFalse(vm.state.value.enabled)
        assertEquals("lock_not_enrolled", vm.state.value.error?.messageKey)
        vm.onCleared()
    }

    @Test
    fun `turning lock off clears the overlay`() = runTest {
        val store = InMemoryAppLockStore(enabled = true)
        val vm = createVm(loggedIn = true, store = store)
        assertTrue(vm.state.value.locked)
        vm.onIntent(AppLockIntent.SetEnabled(false))
        assertFalse(store.isEnabled())
        assertFalse(vm.state.value.locked)
        assertFalse(vm.state.value.enabled)
        vm.onCleared()
    }

    @Test
    fun `logout clears the overlay and keeps the preference`() = runTest {
        val store = InMemoryAppLockStore(enabled = true)
        val auth = FakeAuthRepository(loggedInOverride = true)
        val vm = createVm(auth = auth, store = store)
        assertTrue(vm.state.value.locked)
        auth.loggedInOverride = false
        vm.onIntent(AppLockIntent.Bind)
        assertFalse(vm.state.value.locked)
        assertTrue(store.isEnabled())
        vm.onCleared()
    }

    @Test
    fun `disabled lock does not lock on background`() = runTest {
        val vm = createVm(loggedIn = true, enabled = false)
        vm.onIntent(AppLockIntent.AppStopped)
        assertFalse(vm.state.value.locked)
        vm.onCleared()
    }

    private fun createVm(
        loggedIn: Boolean = true,
        enabled: Boolean = false,
        store: AppLockStore = InMemoryAppLockStore(enabled),
        authenticator: BiometricAuthenticator = FakeBiometricAuthenticator(),
        auth: FakeAuthRepository = FakeAuthRepository(loggedInOverride = loggedIn),
    ) = AppLockViewModel(
        store = store,
        authenticator = authenticator,
        isLoggedIn = IsLoggedInUseCase(auth),
    )
}

private class InMemoryAppLockStore(
    private var enabled: Boolean = false,
) : AppLockStore {
    override fun isEnabled(): Boolean = enabled
    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}

private class FakeBiometricAuthenticator(
    var availability: BiometricAvailability = BiometricAvailability.AVAILABLE,
    var authenticateResult: Boolean = true,
    private val gate: CompletableDeferred<Boolean>? = null,
) : BiometricAuthenticator {
    var authenticateCalls: Int = 0

    override suspend fun availability(): BiometricAvailability = availability

    override suspend fun authenticate(reason: String): Boolean {
        authenticateCalls += 1
        if (gate != null) return gate.await()
        return authenticateResult
    }
}
