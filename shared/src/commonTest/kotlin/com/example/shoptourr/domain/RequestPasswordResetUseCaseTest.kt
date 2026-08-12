package com.example.shoptourr.domain

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.usecase.RequestPasswordResetUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class RequestPasswordResetUseCaseTest {

    @Test
    fun `rejects invalid email`() = runTest {
        val useCase = RequestPasswordResetUseCase(FakeAuthRepository())
        val result = useCase("not-an-email")
        assertTrue(result.isFailure)
        assertIs<AppError.Validation>(result.exceptionOrNull())
    }

    @Test
    fun `delegates to repository`() = runTest {
        val repo = FakeAuthRepository()
        val useCase = RequestPasswordResetUseCase(repo)
        val result = useCase("mila@voyage.app")
        assertTrue(result.isSuccess)
        assertEquals(1, repo.passwordResetCalls)
    }
}
