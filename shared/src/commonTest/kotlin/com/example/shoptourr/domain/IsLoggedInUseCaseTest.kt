package com.example.shoptourr.domain

import com.example.shoptourr.domain.usecase.IsLoggedInUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsLoggedInUseCaseTest {

    @Test
    fun `delegates to auth repository`() {
        val repo = FakeAuthRepository(loggedInOverride = true)
        assertTrue(IsLoggedInUseCase(repo)())

        val loggedOut = FakeAuthRepository(loggedInOverride = false)
        assertFalse(IsLoggedInUseCase(loggedOut)())
    }
}
