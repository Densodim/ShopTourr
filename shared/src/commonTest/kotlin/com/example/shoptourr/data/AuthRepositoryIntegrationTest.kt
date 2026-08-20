package com.example.shoptourr.data

import com.example.shoptourr.data.remote.dto.auth.AuthTokensResponse
import com.example.shoptourr.data.remote.dto.auth.AuthUserDto
import com.example.shoptourr.data.remote.AuthApi
import com.example.shoptourr.data.remote.createVoyageHttpClient
import com.example.shoptourr.data.repository.AuthRepositoryImpl
import com.example.shoptourr.data.settings.SettingsTokenStore
import com.russhwolf.settings.MapSettings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthRepositoryIntegrationTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Test
    fun `login stores tokens from api response`() = runTest {
        val engine = MockEngine { request ->
            assertTrue(request.url.encodedPath.endsWith("/auth/login"))
            val body = AuthTokensResponse(
                accessToken = "access-token",
                accessExpiresIn = 900,
                refreshToken = "refresh-token",
                refreshExpiresIn = 2_592_000,
                user = AuthUserDto(
                    id = "u1",
                    displayName = "Mila",
                    email = "mila@voyage.app",
                    locale = "ru",
                    createdAt = "2026-01-01T00:00:00Z",
                ),
            )
            respond(
                content = ByteReadChannel(json.encodeToString(body)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = createVoyageHttpClient(
            baseUrl = "https://api.test",
            engine = engine,
            tokenProvider = { null },
        )
        val tokenStore = SettingsTokenStore(MapSettings())
        val repo = AuthRepositoryImpl(AuthApi(client, "https://api.test"), tokenStore)

        val session = repo.login("mila@voyage.app", "secret1").getOrThrow()

        assertEquals("Mila", session.user.displayName)
        assertEquals("access-token", tokenStore.accessToken())
        assertEquals("refresh-token", tokenStore.refreshToken())
    }

    @Test
    fun `social login posts oauth and stores tokens`() = runTest {
        val engine = MockEngine { request ->
            assertTrue(request.url.encodedPath.endsWith("/auth/oauth"))
            val body = AuthTokensResponse(
                accessToken = "access-token",
                accessExpiresIn = 900,
                refreshToken = "refresh-token",
                refreshExpiresIn = 2_592_000,
                user = AuthUserDto(
                    id = "u1",
                    displayName = "Ada",
                    email = "ada@voyage.app",
                    locale = "en",
                    createdAt = "2026-01-01T00:00:00Z",
                ),
            )
            respond(
                content = ByteReadChannel(json.encodeToString(body)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = createVoyageHttpClient(
            baseUrl = "https://api.test",
            engine = engine,
            tokenProvider = { null },
        )
        val tokenStore = SettingsTokenStore(MapSettings())
        val repo = AuthRepositoryImpl(AuthApi(client, "https://api.test"), tokenStore)

        val session = repo.loginSocial(
            provider = com.example.shoptourr.domain.model.SocialProvider.GOOGLE,
            idToken = "google-id-token",
            nonce = "nonce",
        ).getOrThrow()

        assertEquals("Ada", session.user.displayName)
        assertEquals("access-token", tokenStore.accessToken())
    }

    @Test
    fun `logout clears tokens even when api fails`() = runTest {
        val engine = MockEngine { request ->
            require(request.url.encodedPath.endsWith("/auth/logout"))
            respond(
                content = ByteReadChannel("""{"title":"down"}"""),
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = createVoyageHttpClient(
            baseUrl = "https://api.test",
            engine = engine,
            tokenProvider = { "access-token" },
        )
        val tokenStore = SettingsTokenStore(MapSettings())
        tokenStore.saveTokens("access-token", "refresh-token")
        val repo = AuthRepositoryImpl(AuthApi(client, "https://api.test"), tokenStore)

        val result = repo.logout()

        assertTrue(result.isFailure)
        assertEquals(null, tokenStore.accessToken())
        assertEquals(null, tokenStore.refreshToken())
        assertTrue(!repo.isLoggedIn())
    }
}
