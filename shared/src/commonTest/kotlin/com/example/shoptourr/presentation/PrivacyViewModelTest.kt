package com.example.shoptourr.presentation

import app.cash.turbine.test
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.model.User
import com.example.shoptourr.domain.usecase.DeleteAccountUseCase
import com.example.shoptourr.domain.usecase.LogoutUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import com.example.shoptourr.fake.FakeUserRepository
import com.example.shoptourr.presentation.privacy.PrivacyIntent
import com.example.shoptourr.presentation.privacy.PrivacyUiEvent
import com.example.shoptourr.presentation.privacy.PrivacyViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class PrivacyViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun vm(
        users: FakeUserRepository = FakeUserRepository(),
        auth: FakeAuthRepository = FakeAuthRepository(
            session = AuthSession("a", "r", 1, 1, User("u1", "Mila", "m@v.app", "ru")),
        ),
    ) = PrivacyViewModel(DeleteAccountUseCase(users, LogoutUseCase(auth)))

    @Test
    fun `confirm delete emits account deleted`() = runTest {
        val users = FakeUserRepository()
        val viewModel = vm(users)
        viewModel.events.test {
            viewModel.onIntent(PrivacyIntent.RequestDeleteAccount)
            viewModel.onIntent(PrivacyIntent.ConfirmDeleteAccount)
            assertIs<PrivacyUiEvent.AccountDeleted>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(1, users.deleteAccountCalls)
        viewModel.onCleared()
    }

    @Test
    fun `failed delete stays on the privacy screen`() = runTest {
        val users = FakeUserRepository(deleteAccountError = AppError.Network)
        val viewModel = vm(users)
        viewModel.onIntent(PrivacyIntent.ConfirmDeleteAccount)
        assertTrue(viewModel.state.value.error != null)
        assertEquals(1, users.deleteAccountCalls)
        viewModel.onCleared()
    }
}
