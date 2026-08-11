package com.example.shoptourr.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest

class RequestIdPluginTest {

    @Test
    fun `http client attaches unique X-Request-Id per call`() = runTest {
        val seen = mutableListOf<String?>()
        val engine = MockEngine { request ->
            seen += request.headers["X-Request-Id"]
            respond("", status = HttpStatusCode.NoContent)
        }
        val client = createVoyageHttpClient(
            baseUrl = "https://api.test",
            engine = engine,
            tokenProvider = { null },
        )
        client.get("https://api.test/ping")
        client.get("https://api.test/ping")
        assertEquals(2, seen.size)
        assertNotNull(seen[0])
        assertNotNull(seen[1])
        assertNotEquals(seen[0], seen[1])
    }
}
