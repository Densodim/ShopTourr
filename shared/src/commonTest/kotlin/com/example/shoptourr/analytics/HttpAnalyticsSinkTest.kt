package com.example.shoptourr.analytics

import com.example.shoptourr.data.remote.AnalyticsApi
import com.example.shoptourr.data.remote.createVoyageHttpClient
import com.example.shoptourr.data.remote.dto.analytics.toIngestDto
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class HttpAnalyticsSinkTest {

    @Test
    fun `event dto uses iso-8601 timestamp`() {
        val dto = AnalyticsEvent(
            id = "e1",
            name = "home_opened",
            properties = mapOf("tab" to "home"),
            timestampEpochMs = 1_700_000_000_000L,
        ).toIngestDto()
        assertEquals("e1", dto.id)
        assertEquals("home_opened", dto.name)
        assertEquals("home", dto.properties["tab"])
        assertEquals("2023-11-14T22:13:20Z", dto.timestamp)
    }

    @Test
    fun `send posts the batch and identify user to me analytics-events`() = runTest {
        var method: HttpMethod? = null
        var path: String? = null
        var body = ""
        val engine = MockEngine { request ->
            method = request.method
            path = request.url.encodedPath
            body = request.body.toByteArray().decodeToString()
            respond("", status = HttpStatusCode.NoContent)
        }
        val sink = HttpAnalyticsSink(
            AnalyticsApi(
                createVoyageHttpClient("https://api.test", engine, { "t" }),
                "https://api.test",
            ),
        )
        sink.identify("user-9")
        val result = sink.send(
            listOf(
                AnalyticsEvent(
                    id = "e1",
                    name = "home_opened",
                    properties = mapOf("tab" to "home"),
                    timestampEpochMs = 1_700_000_000_000L,
                ),
            ),
        )
        assertTrue(result.isSuccess)
        assertEquals(HttpMethod.Post, method)
        assertTrue(path.orEmpty().endsWith("/me/analytics-events"))
        assertTrue(body.contains("home_opened"))
        assertTrue(body.contains("user-9"))
    }

    @Test
    fun `send keeps failure when the server rejects the batch`() = runTest {
        val engine = MockEngine { respond("", status = HttpStatusCode.InternalServerError) }
        val sink = HttpAnalyticsSink(
            AnalyticsApi(
                createVoyageHttpClient("https://api.test", engine, { "t" }),
                "https://api.test",
            ),
        )
        val result = sink.send(
            listOf(
                AnalyticsEvent(
                    id = "e2",
                    name = "export_tapped",
                    timestampEpochMs = 1L,
                ),
            ),
        )
        assertTrue(result.isFailure)
    }
}
