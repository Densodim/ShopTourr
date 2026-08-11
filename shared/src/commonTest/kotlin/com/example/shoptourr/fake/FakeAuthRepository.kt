package com.example.shoptourr.fake

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.model.User
import com.example.shoptourr.domain.repository.AuthRepository

class FakeAuthRepository(
    var session: AuthSession? = null,
    var error: AppError? = null,
    private val loggedInOverride: Boolean? = null,
) : AuthRepository {
    var loginCalls: Int = 0
        private set

    override suspend fun login(email: String, password: String, deviceName: String?): Result<AuthSession> {
        loginCalls += 1
        error?.let { return Result.failure(it) }
        return session?.let { Result.success(it) }
            ?: Result.failure(AppError.Unauthorized)
    }

    override suspend fun register(
        displayName: String,
        email: String,
        password: String,
        locale: String,
    ): Result<AuthSession> = login(email, password)

    override suspend fun refresh(): Result<AuthSession> =
        session?.let { Result.success(it) } ?: Result.failure(AppError.Unauthorized)

    override suspend fun logout(allSessions: Boolean): Result<Unit> {
        session = null
        return Result.success(Unit)
    }

    override fun currentUser(): User? = session?.user

    override fun isLoggedIn(): Boolean = loggedInOverride ?: (session != null)
}
