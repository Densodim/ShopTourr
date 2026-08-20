package com.example.shoptourr.domain

import com.example.shoptourr.domain.auth.SocialAuthClient
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.model.SocialCredentials
import com.example.shoptourr.domain.model.SocialProvider
import com.example.shoptourr.domain.model.User
import com.example.shoptourr.domain.usecase.SocialLoginUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SocialLoginUseCaseTest {

    @Test
    fun `exchanges a google id token for a session`() = runTest {
        val repo = FakeAuthRepository(
            session = AuthSession(
                accessToken = "a",
                refreshToken = "r",
                accessExpiresIn = 900,
                refreshExpiresIn = 1000,
                user = User("u1", "Ada", "ada@voyage.app", "en"),
            ),
        )
        val social = FakeSocialAuthClient(
            SocialCredentials(SocialProvider.GOOGLE, "google-id-token", "Ada"),
        )
        val useCase = SocialLoginUseCase(social, repo, nonce = { "fixed-nonce" })

        val result = useCase(SocialProvider.GOOGLE)

        assertTrue(result.isSuccess)
        assertEquals(1, repo.socialLoginCalls)
        assertEquals(SocialProvider.GOOGLE, repo.lastSocialProvider)
        assertEquals("Ada", result.getOrThrow().user.displayName)
    }

    @Test
    fun `does not hit the api when the user cancels`() = runTest {
        val repo = FakeAuthRepository()
        val social = FakeSocialAuthClient(error = AppError.Cancelled)
        val result = SocialLoginUseCase(social, repo)(SocialProvider.APPLE)
        assertEquals(AppError.Cancelled, result.exceptionOrNull())
        assertEquals(0, repo.socialLoginCalls)
    }
}

private class FakeSocialAuthClient(
    private val credentials: SocialCredentials? = null,
    private val error: AppError? = null,
) : SocialAuthClient {
    override suspend fun signIn(provider: SocialProvider, nonce: String) =
        error?.let { Result.failure(it) }
            ?: Result.success(requireNotNull(credentials))
}
