package com.example.shoptourr.domain

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.ThemeMode
import com.example.shoptourr.domain.model.UpdatePreferencesDraft
import com.example.shoptourr.domain.model.UpdateProfileDraft
import com.example.shoptourr.domain.model.UserPreferences
import com.example.shoptourr.domain.model.UserProfile
import com.example.shoptourr.domain.model.UserStats
import com.example.shoptourr.domain.usecase.LogoutUseCase
import com.example.shoptourr.domain.usecase.UpdatePreferencesUseCase
import com.example.shoptourr.domain.usecase.UpdateProfileUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import com.example.shoptourr.fake.FakeUserRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class ProfileUseCasesTest {

    private val sampleProfile = UserProfile(
        id = "u1",
        displayName = "Mila",
        email = "mila@voyage.app",
        locale = "ru",
        preferredCurrency = "EUR",
        theme = ThemeMode.SYSTEM,
        pushNotificationsEnabled = true,
        memberSince = "2026-01-01",
        stats = UserStats(3, 2, 5),
    )

    @Test
    fun `update profile rejects blank display name`() = runTest {
        val repo = FakeUserRepository(profile = sampleProfile)
        val result = UpdateProfileUseCase(repo)(UpdateProfileDraft("  "))
        assertEquals(AppError.Validation("displayName"), result.exceptionOrNull())
        assertEquals(0, repo.updateProfileCalls)
    }

    @Test
    fun `update profile trims and persists`() = runTest {
        val repo = FakeUserRepository(profile = sampleProfile)
        val result = UpdateProfileUseCase(repo)(UpdateProfileDraft("  Nova  ")).getOrThrow()
        assertEquals("Nova", result.displayName)
        assertEquals(1, repo.updateProfileCalls)
    }

    @Test
    fun `update preferences validates currency`() = runTest {
        val repo = FakeUserRepository(
            preferences = UserPreferences("ru", "EUR", ThemeMode.DARK, true, true),
        )
        val result = UpdatePreferencesUseCase(repo)(
            UpdatePreferencesDraft(preferredCurrency = "EU"),
        )
        assertEquals(AppError.Validation("preferredCurrency"), result.exceptionOrNull())
    }

    @Test
    fun `update preferences applies theme and currency`() = runTest {
        val repo = FakeUserRepository(
            preferences = UserPreferences("ru", "EUR", ThemeMode.SYSTEM, true, false),
        )
        val result = UpdatePreferencesUseCase(repo)(
            UpdatePreferencesDraft(
                preferredCurrency = "USD",
                theme = ThemeMode.DARK,
                darkMode = true,
            ),
        ).getOrThrow()
        assertEquals("USD", result.preferredCurrency)
        assertEquals(ThemeMode.DARK, result.theme)
        assertTrue(result.darkMode)
    }

    @Test
    fun `logout clears auth session`() = runTest {
        val auth = FakeAuthRepository(
            session = com.example.shoptourr.domain.model.AuthSession(
                "a", "r", 1, 1,
                com.example.shoptourr.domain.model.User("u1", "Mila", "m@v.app", "ru"),
            )
        )
        LogoutUseCase(auth)().getOrThrow()
        assertNull(auth.session)
        assertTrue(!auth.isLoggedIn())
    }
}
