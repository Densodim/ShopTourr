package com.example.shoptourr.presentation

import app.cash.turbine.test
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.model.User
import com.example.shoptourr.domain.usecase.LoginUseCase
import com.example.shoptourr.domain.usecase.RegisterUseCase
import com.example.shoptourr.domain.usecase.SocialLoginUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import com.example.shoptourr.domain.auth.UnavailableSocialAuthClient
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

    private fun vm(repo: FakeAuthRepository) =
        AuthViewModel(
            LoginUseCase(repo),
            RegisterUseCase(repo),
            SocialLoginUseCase(UnavailableSocialAuthClient(), repo),
        )

    @Test
    fun `successful login updates state and emits navigate home`() = runTest {
        val repo = FakeAuthRepository(
            session = AuthSession(
                accessToken = "a",
                refreshToken = "r",
                accessExpiresIn = 900,
                refreshExpiresIn = 1000,
                user = User("u1", "Mila", "mila@voyage.app", "ru"),
            ),
        )
        val viewModel = vm(repo)

        viewModel.events.test {
            viewModel.onIntent(AuthIntent.EmailChanged("mila@voyage.app"))
            viewModel.onIntent(AuthIntent.PasswordChanged("secret1"))
            viewModel.onIntent(AuthIntent.Submit)
            val event = awaitItem()
            assertIs<AuthUiEvent.NavigateHome>(event)
            cancelAndIgnoreRemainingEvents()
        }

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("Mila", state.user?.displayName)
        viewModel.onCleared()
    }

    @Test
    fun `empty register submit puts the error on the name field`() = runTest {
        val viewModel = vm(FakeAuthRepository())
        viewModel.onIntent(AuthIntent.SetRegisterMode(true))
        viewModel.onIntent(AuthIntent.Submit)
        val state = viewModel.state.value
        assertNull(state.error)
        assertEquals("validation_person_name_required", state.fieldErrors.displayName)
        viewModel.onCleared()
    }

    @Test
    fun `empty login submit puts the error on the email field`() = runTest {
        val viewModel = vm(FakeAuthRepository())
        viewModel.onIntent(AuthIntent.Submit)
        val state = viewModel.state.value
        assertNull(state.error)
        assertEquals("validation_email_required", state.fieldErrors.email)
        assertNull(state.fieldErrors.password)
        viewModel.onCleared()
    }

    @Test
    fun `invalid email puts the error on the email field`() = runTest {
        val viewModel = vm(FakeAuthRepository())
        viewModel.onIntent(AuthIntent.EmailChanged("not-an-email"))
        viewModel.onIntent(AuthIntent.PasswordChanged("secret1"))
        viewModel.onIntent(AuthIntent.Submit)
        val state = viewModel.state.value
        assertNull(state.error)
        assertEquals("validation_email_invalid", state.fieldErrors.email)
        viewModel.onCleared()
    }

    @Test
    fun `short register password puts the error on the password field`() = runTest {
        val viewModel = vm(FakeAuthRepository())
        viewModel.onIntent(AuthIntent.SetRegisterMode(true))
        viewModel.onIntent(AuthIntent.DisplayNameChanged("Ada"))
        viewModel.onIntent(AuthIntent.EmailChanged("ada@voyage.app"))
        viewModel.onIntent(AuthIntent.PasswordChanged("short"))
        viewModel.onIntent(AuthIntent.Submit)
        val state = viewModel.state.value
        assertNull(state.error)
        assertEquals("validation_password_short", state.fieldErrors.password)
        viewModel.onCleared()
    }

    @Test
    fun `failed login surfaces error`() = runTest {
        val repo = FakeAuthRepository(error = AppError.Unauthorized)
        val viewModel = vm(repo)

        viewModel.onIntent(AuthIntent.EmailChanged("mila@voyage.app"))
        viewModel.onIntent(AuthIntent.PasswordChanged("secret1"))
        viewModel.onIntent(AuthIntent.Submit)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Сессия истекла", state.error?.title)
        assertEquals(UiErrorAction.Logout, state.error?.action)
        viewModel.onCleared()
    }

    @Test
    fun `register mode creates account`() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = vm(repo)
        viewModel.onIntent(AuthIntent.ToggleMode)
        assertTrue(viewModel.state.value.isRegisterMode)
        viewModel.onIntent(AuthIntent.DisplayNameChanged("Ada"))
        viewModel.onIntent(AuthIntent.EmailChanged("ada@voyage.app"))
        viewModel.onIntent(AuthIntent.PasswordChanged("password1"))
        viewModel.events.test {
            viewModel.onIntent(AuthIntent.Submit)
            assertIs<AuthUiEvent.NavigateHome>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals("Ada", viewModel.state.value.user?.displayName)
        viewModel.onCleared()
    }

    @Test
    fun `set register mode opens signup from welcome`() = runTest {
        val viewModel = vm(FakeAuthRepository())
        assertFalse(viewModel.state.value.isRegisterMode)
        viewModel.onIntent(AuthIntent.SetRegisterMode(true))
        assertTrue(viewModel.state.value.isRegisterMode)
        viewModel.onIntent(AuthIntent.SetRegisterMode(false))
        assertFalse(viewModel.state.value.isRegisterMode)
        viewModel.onCleared()
    }

    @Test
    fun `cancelled social sign in does not surface an error`() = runTest {
        val repo = FakeAuthRepository()
        val social = object : com.example.shoptourr.domain.auth.SocialAuthClient {
            override suspend fun signIn(
                provider: com.example.shoptourr.domain.model.SocialProvider,
                nonce: String,
            ) = Result.failure<com.example.shoptourr.domain.model.SocialCredentials>(AppError.Cancelled)
        }
        val viewModel = AuthViewModel(
            LoginUseCase(repo),
            RegisterUseCase(repo),
            SocialLoginUseCase(social, repo),
        )
        viewModel.onIntent(
            AuthIntent.SocialSignIn(com.example.shoptourr.domain.model.SocialProvider.GOOGLE),
        )
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(null, viewModel.state.value.error)
        viewModel.onCleared()
    }
}
