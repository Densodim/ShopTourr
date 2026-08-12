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
    ): Result<AuthSession> {
        error?.let { return Result.failure(it) }
        val user = User(
            id = session?.user?.id ?: "u-1",
            displayName = displayName,
            email = email,
            locale = locale,
        )
        val created = AuthSession(
            accessToken = session?.accessToken ?: "access",
            refreshToken = session?.refreshToken ?: "refresh",
            accessExpiresIn = session?.accessExpiresIn ?: 3600,
            refreshExpiresIn = session?.refreshExpiresIn ?: 86400,
            user = user,
        )
        session = created
        return Result.success(created)
    }

    var passwordResetCalls: Int = 0
        private set

    override suspend fun requestPasswordReset(email: String): Result<Unit> {
        passwordResetCalls += 1
        error?.let { return Result.failure(it) }
        return Result.success(Unit)
    }

    override suspend fun refresh(): Result<AuthSession> =
        session?.let { Result.success(it) } ?: Result.failure(AppError.Unauthorized)

    override suspend fun logout(allSessions: Boolean): Result<Unit> {
        session = null
        return Result.success(Unit)
    }

    override fun currentUser(): User? = session?.user

    override fun isLoggedIn(): Boolean = loggedInOverride ?: (session != null)
}
