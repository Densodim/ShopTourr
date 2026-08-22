package com.example.shoptourr.presentation

import com.example.shoptourr.domain.usecase.RequestPasswordResetUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import com.example.shoptourr.presentation.auth.ForgotPasswordIntent
import com.example.shoptourr.presentation.auth.ForgotPasswordViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `empty email puts the error on the email field`() = runTest {
        val vm = ForgotPasswordViewModel(RequestPasswordResetUseCase(FakeAuthRepository()))
        vm.onIntent(ForgotPasswordIntent.Submit)
        assertNull(vm.state.value.error)
        assertEquals("validation_email_required", vm.state.value.fieldErrors.email)
        vm.onCleared()
    }

    @Test
    fun `invalid email puts the error on the email field`() = runTest {
        val vm = ForgotPasswordViewModel(RequestPasswordResetUseCase(FakeAuthRepository()))
        vm.onIntent(ForgotPasswordIntent.EmailChanged("nope"))
        vm.onIntent(ForgotPasswordIntent.Submit)
        assertNull(vm.state.value.error)
        assertEquals("validation_email_invalid", vm.state.value.fieldErrors.email)
        vm.onCleared()
    }
}
