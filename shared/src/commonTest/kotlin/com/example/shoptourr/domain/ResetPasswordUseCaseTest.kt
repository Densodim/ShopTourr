package com.example.shoptourr.domain

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.usecase.ResetPasswordUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

private const val VALID_TOKEN = "0123456789abcdef0123"
private const val VALID_EMAIL = "mila@voyage.app"
private const val VALID_PASSWORD = "s3cret!"

class ResetPasswordUseCaseTest {

    @Test
    fun `rejects invalid email`() = runTest {
        val repo = FakeAuthRepository()
        val result = ResetPasswordUseCase(repo)("not-an-email", VALID_TOKEN, VALID_PASSWORD)
        assertTrue(result.isFailure)
        assertEquals("email", assertIs<AppError.Validation>(result.exceptionOrNull()).message)
        assertEquals(0, repo.resetPasswordCalls)
    }

    @Test
    fun `rejects a token shorter than the backend minimum`() = runTest {
        val repo = FakeAuthRepository()
        val result = ResetPasswordUseCase(repo)(VALID_EMAIL, "short-token", VALID_PASSWORD)
        assertTrue(result.isFailure)
        assertEquals("token", assertIs<AppError.Validation>(result.exceptionOrNull()).message)
        assertEquals(0, repo.resetPasswordCalls)
    }

    @Test
    fun `rejects a password shorter than six characters`() = runTest {
        val repo = FakeAuthRepository()
        val result = ResetPasswordUseCase(repo)(VALID_EMAIL, VALID_TOKEN, "12345")
        assertTrue(result.isFailure)
        assertEquals("newPassword", assertIs<AppError.Validation>(result.exceptionOrNull()).message)
        assertEquals(0, repo.resetPasswordCalls)
    }

    @Test
    fun `trims the email and token before delegating`() = runTest {
        val repo = FakeAuthRepository()
        val result = ResetPasswordUseCase(repo)("  $VALID_EMAIL  ", "  $VALID_TOKEN  ", VALID_PASSWORD)
        assertTrue(result.isSuccess)
        assertEquals(1, repo.resetPasswordCalls)
        assertEquals(VALID_EMAIL, repo.lastResetEmail)
        assertEquals(VALID_TOKEN, repo.lastResetToken)
        assertEquals(VALID_PASSWORD, repo.lastResetPassword)
    }

    @Test
    fun `surfaces a repository failure`() = runTest {
        val repo = FakeAuthRepository(error = AppError.Unauthorized)
        val result = ResetPasswordUseCase(repo)(VALID_EMAIL, VALID_TOKEN, VALID_PASSWORD)
        assertTrue(result.isFailure)
        assertIs<AppError.Unauthorized>(result.exceptionOrNull())
    }
}
