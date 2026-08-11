package com.example.shoptourr.data

import com.example.shoptourr.data.local.InMemoryUserLocalStore
import com.example.shoptourr.data.remote.UserApi
import com.example.shoptourr.data.remote.createVoyageHttpClient
import com.example.shoptourr.data.remote.dto.user.ThemePreference
import com.example.shoptourr.data.remote.dto.user.UserDto
import com.example.shoptourr.data.remote.dto.user.UserPreferencesDto
import com.example.shoptourr.data.remote.dto.user.UserStatsDto
import com.example.shoptourr.data.remote.voyageJson
import com.example.shoptourr.data.repository.UserRepositoryImpl
import com.example.shoptourr.domain.model.ThemeMode
import com.example.shoptourr.domain.model.UpdatePreferencesDraft
import com.example.shoptourr.domain.model.UpdateProfileDraft
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString

class UserRepositoryIntegrationTest {

    private val json = voyageJson()

    private fun sampleUserDto(name: String = "Mila") = UserDto(
        id = "u1",
        displayName = name,
        email = "mila@voyage.app",
        locale = "ru",
        preferredCurrency = "EUR",
        theme = ThemePreference.SYSTEM,
        pushNotificationsEnabled = true,
        memberSince = "2026-01-01",
        stats = UserStatsDto(2, 1, 4),
    )

    private fun samplePrefsDto(currency: String = "EUR") = UserPreferencesDto(
        locale = "ru",
        preferredCurrency = currency,
        theme = ThemePreference.DARK,
        pushNotificationsEnabled = false,
        darkMode = true,
    )

    @Test
    fun `refresh profile caches locally`() = runTest {
        val engine = MockEngine { request ->
            require(request.url.toString().contains("/me"))
            respond(
                content = ByteReadChannel(json.encodeToString(sampleUserDto())),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val store = InMemoryUserLocalStore()
        val repo = UserRepositoryImpl(
            api = UserApi(createVoyageHttpClient("https://api.test", engine, { "t" }), "https://api.test"),
            localStore = store,
        )

        val profile = repo.refreshProfile().getOrThrow()
        assertEquals("Mila", profile.displayName)
        assertEquals("Mila", store.observeProfile().first()?.displayName)
    }

    @Test
    fun `update preferences caches locally`() = runTest {
        val engine = MockEngine { request ->
            require(request.url.toString().contains("/me/preferences"))
            respond(
                content = ByteReadChannel(json.encodeToString(samplePrefsDto("USD"))),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val store = InMemoryUserLocalStore()
        val repo = UserRepositoryImpl(
            api = UserApi(createVoyageHttpClient("https://api.test", engine, { "t" }), "https://api.test"),
            localStore = store,
        )

        val prefs = repo.updatePreferences(
            UpdatePreferencesDraft(preferredCurrency = "USD", theme = ThemeMode.DARK),
        ).getOrThrow()
        assertEquals("USD", prefs.preferredCurrency)
        assertEquals(ThemeMode.DARK, store.observePreferences().first()?.theme)
    }

    @Test
    fun `update profile caches locally`() = runTest {
        val engine = MockEngine {
            respond(
                content = ByteReadChannel(json.encodeToString(sampleUserDto("Nova"))),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val store = InMemoryUserLocalStore()
        val repo = UserRepositoryImpl(
            api = UserApi(createVoyageHttpClient("https://api.test", engine, { "t" }), "https://api.test"),
            localStore = store,
        )
        val profile = repo.updateProfile(UpdateProfileDraft("Nova")).getOrThrow()
        assertEquals("Nova", profile.displayName)
        assertEquals("Nova", store.profile()?.displayName)
    }
}
