package com.example.shoptourr.data.remote

import com.example.shoptourr.observability.RecordingObservability
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
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

    @Test
    fun `http client breadcrumbs request id`() = runTest {
        val obs = RecordingObservability()
        val engine = MockEngine { respond("", status = HttpStatusCode.NoContent) }
        val client = createVoyageHttpClient(
            baseUrl = "https://api.test",
            engine = engine,
            tokenProvider = { null },
            observability = obs,
        )
        client.get("https://api.test/ping")
        assertEquals(1, obs.breadcrumbs.size)
        assertEquals("http", obs.breadcrumbs.single().category)
        assertTrue(obs.breadcrumbs.single().data["request_id"].orEmpty().isNotBlank())
    }
}
