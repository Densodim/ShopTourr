package com.example.shoptourr.domain

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.model.User
import com.example.shoptourr.domain.repository.AuthRepository
import com.example.shoptourr.domain.usecase.LoginUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class LoginUseCaseTest {

    @Test
    fun `login success returns session`() = runTest {
        val repo = FakeAuthRepository(
            session = AuthSession(
                accessToken = "access",
                refreshToken = "refresh",
                accessExpiresIn = 900,
                refreshExpiresIn = 2_592_000,
                user = User(
                    id = "u1",
                    displayName = "Mila",
                    email = "mila@voyage.app",
                    locale = "ru",
                ),
            )
        )
        val useCase = LoginUseCase(repo)

        val result = useCase(email = "mila@voyage.app", password = "secret1")

        assertTrue(result.isSuccess)
        assertEquals("Mila", result.getOrThrow().user.displayName)
        assertEquals(1, repo.loginCalls)
    }

    @Test
    fun `blank email fails validation without hitting repository`() = runTest {
        val repo = FakeAuthRepository()
        val useCase = LoginUseCase(repo)

        val result = useCase(email = " ", password = "secret1")

        assertTrue(result.isFailure)
        assertEquals(AppError.Validation("email"), result.exceptionOrNull())
        assertEquals(0, repo.loginCalls)
    }

    @Test
    fun `short password fails validation`() = runTest {
        val repo = FakeAuthRepository()
        val result = LoginUseCase(repo)(email = "a@b.co", password = "123")
        assertEquals(AppError.Validation("password"), result.exceptionOrNull())
    }
}
