package com.example.shoptourr.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class HttpRetryTimeoutTest {

    @Test
    fun `retries GET on 500 until success`() = runTest {
        var attempts = 0
        val engine = MockEngine {
            attempts++
            if (attempts < 3) {
                respond("", status = HttpStatusCode.InternalServerError)
            } else {
                respond("{}", status = HttpStatusCode.OK)
            }
        }
        val client = createVoyageHttpClient(
            baseUrl = "https://api.test",
            engine = engine,
            tokenProvider = { null },
            retryDelayMillis = 0,
        )
        val response = client.get("https://api.test/home")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(3, attempts)
    }

    @Test
    fun `retries HEAD on 503`() = runTest {
        var attempts = 0
        val engine = MockEngine {
            attempts++
            if (attempts == 1) {
                respond("", status = HttpStatusCode.ServiceUnavailable)
            } else {
                respond("", status = HttpStatusCode.NoContent)
            }
        }
        val client = createVoyageHttpClient(
            baseUrl = "https://api.test",
            engine = engine,
            tokenProvider = { null },
            retryDelayMillis = 0,
        )
        val response = client.head("https://api.test/health")
        assertEquals(HttpStatusCode.NoContent, response.status)
        assertEquals(2, attempts)
    }

    @Test
    fun `does not retry POST on 500`() = runTest {
        var attempts = 0
        val engine = MockEngine {
            attempts++
            respond("", status = HttpStatusCode.InternalServerError)
        }
        val client = createVoyageHttpClient(
            baseUrl = "https://api.test",
            engine = engine,
            tokenProvider = { null },
            retryDelayMillis = 0,
        )
        val response = client.post("https://api.test/purchases")
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertEquals(1, attempts)
    }
}
