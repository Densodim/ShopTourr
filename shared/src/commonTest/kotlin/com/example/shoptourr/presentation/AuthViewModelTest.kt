package com.example.shoptourr.presentation

import app.cash.turbine.test
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.model.User
import com.example.shoptourr.domain.usecase.LoginUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import com.example.shoptourr.presentation.auth.AuthIntent
import com.example.shoptourr.presentation.auth.AuthUiEvent
import com.example.shoptourr.presentation.auth.AuthViewModel
import com.example.shoptourr.presentation.error.UiErrorAction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful login updates state and emits navigate home`() = runTest {
        val repo = FakeAuthRepository(
            session = AuthSession(
                accessToken = "a",
                refreshToken = "r",
                accessExpiresIn = 900,
                refreshExpiresIn = 1000,
                user = User("u1", "Mila", "mila@voyage.app", "ru"),
            )
        )
        val vm = AuthViewModel(LoginUseCase(repo))

        vm.events.test {
            vm.onIntent(AuthIntent.SubmitLogin(email = "mila@voyage.app", password = "secret1"))
            val event = awaitItem()
            assertIs<AuthUiEvent.NavigateHome>(event)
            cancelAndIgnoreRemainingEvents()
        }

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("Mila", state.user?.displayName)
        vm.onCleared()
    }

    @Test
    fun `failed login surfaces error`() = runTest {
        val repo = FakeAuthRepository(error = AppError.Unauthorized)
        val vm = AuthViewModel(LoginUseCase(repo))

        vm.onIntent(AuthIntent.SubmitLogin(email = "mila@voyage.app", password = "secret1"))

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertEquals("Session Expired", state.error?.title)
        assertEquals(UiErrorAction.Logout, state.error?.action)
        vm.onCleared()
    }
}
