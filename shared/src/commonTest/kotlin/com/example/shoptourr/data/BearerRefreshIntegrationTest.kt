package com.example.shoptourr.data

import com.example.shoptourr.data.remote.createVoyageHttpClient
import com.example.shoptourr.data.settings.SettingsTokenStore
import com.russhwolf.settings.MapSettings
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.request.get
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

class BearerRefreshIntegrationTest {

    @Test
    fun `401 triggers refreshTokens and retries request`() = runTest {
        val tokenStore = SettingsTokenStore(MapSettings())
        tokenStore.saveTokens(accessToken = "expired", refreshToken = "refresh-1")
        var homeHits = 0
        var refreshCalls = 0

        val engine = MockEngine { request ->
            require(request.url.encodedPath.endsWith("/home"))
            homeHits += 1
            val auth = request.headers[HttpHeaders.Authorization]
            if (auth == "Bearer expired") {
                respond(
                    content = ByteReadChannel("""{"status":401,"title":"Unauthorized"}"""),
                    status = HttpStatusCode.Unauthorized,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            } else {
                assertEquals("Bearer fresh-access", auth)
                respond(
                    content = ByteReadChannel("""{"ok":true}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        }

        val client = createVoyageHttpClient(
            baseUrl = "https://api.test",
            engine = engine,
            tokenProvider = { tokenStore.accessToken() },
            refreshTokenProvider = { tokenStore.refreshToken() },
            refreshTokens = {
                refreshCalls += 1
                tokenStore.saveTokens("fresh-access", "refresh-2")
                BearerTokens("fresh-access", "refresh-2")
            },
        )

        val response = client.get("https://api.test/home")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(2, homeHits)
        assertEquals(1, refreshCalls)
        assertEquals("fresh-access", tokenStore.accessToken())
        assertTrue(tokenStore.refreshToken() == "refresh-2")
    }
}
