package com.example.shoptourr.presentation

import app.cash.turbine.test
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.usecase.ResetPasswordUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import com.example.shoptourr.presentation.auth.ResetPasswordIntent
import com.example.shoptourr.presentation.auth.ResetPasswordUiEvent
import com.example.shoptourr.presentation.auth.ResetPasswordViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

private const val TOKEN = "0123456789abcdef0123"

@OptIn(ExperimentalCoroutinesApi::class)
class ResetPasswordViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repo: FakeAuthRepository) =
        ResetPasswordViewModel(ResetPasswordUseCase(repo))

    @Test
    fun `prefills the email carried over from the request step`() {
        val vm = viewModel(FakeAuthRepository())
        vm.onIntent(ResetPasswordIntent.Prefill(email = "mila@voyage.app", token = TOKEN))
        assertEquals("mila@voyage.app", vm.state.value.email)
        assertEquals(TOKEN, vm.state.value.token)
    }

    @Test
    fun `submitting a valid form marks the reset done`() = runTest {
        val repo = FakeAuthRepository()
        val vm = viewModel(repo)
        vm.onIntent(ResetPasswordIntent.EmailChanged("mila@voyage.app"))
        vm.onIntent(ResetPasswordIntent.TokenChanged(TOKEN))
        vm.onIntent(ResetPasswordIntent.PasswordChanged("s3cret!"))
        vm.onIntent(ResetPasswordIntent.Submit)

        assertTrue(vm.state.value.done)
        assertFalse(vm.state.value.isLoading)
        assertEquals(1, repo.resetPasswordCalls)
    }

    @Test
    fun `a rejected token surfaces an error and leaves the form open`() = runTest {
        val repo = FakeAuthRepository()
        val vm = viewModel(repo)
        vm.onIntent(ResetPasswordIntent.EmailChanged("mila@voyage.app"))
        vm.onIntent(ResetPasswordIntent.TokenChanged("too-short"))
        vm.onIntent(ResetPasswordIntent.PasswordChanged("s3cret!"))
        vm.onIntent(ResetPasswordIntent.Submit)

        assertFalse(vm.state.value.done)
        assertNull(vm.state.value.error)
        assertEquals("validation_token_invalid", vm.state.value.fieldErrors.token)
        assertEquals(0, repo.resetPasswordCalls)
    }

    @Test
    fun `a server failure surfaces an error`() = runTest {
        val repo = FakeAuthRepository(error = AppError.Unauthorized)
        val vm = viewModel(repo)
        vm.onIntent(ResetPasswordIntent.EmailChanged("mila@voyage.app"))
        vm.onIntent(ResetPasswordIntent.TokenChanged(TOKEN))
        vm.onIntent(ResetPasswordIntent.PasswordChanged("s3cret!"))
        vm.onIntent(ResetPasswordIntent.Submit)

        assertFalse(vm.state.value.done)
        assertNotNull(vm.state.value.error)
    }

    @Test
    fun `editing a field clears a previous error`() = runTest {
        val repo = FakeAuthRepository(error = AppError.Unauthorized)
        val vm = viewModel(repo)
        vm.onIntent(ResetPasswordIntent.EmailChanged("mila@voyage.app"))
        vm.onIntent(ResetPasswordIntent.TokenChanged(TOKEN))
        vm.onIntent(ResetPasswordIntent.PasswordChanged("s3cret!"))
        vm.onIntent(ResetPasswordIntent.Submit)
        assertNotNull(vm.state.value.error)

        vm.onIntent(ResetPasswordIntent.PasswordChanged("another1"))
        assertEquals(null, vm.state.value.error)
    }

    @Test
    fun `finishing navigates on to sign in`() = runTest {
        val vm = viewModel(FakeAuthRepository())
        vm.events.test {
            vm.onIntent(ResetPasswordIntent.Finish)
            assertIs<ResetPasswordUiEvent.NavigateToSignIn>(awaitItem())
        }
    }
}
