package com.example.shoptourr.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Context7-verified: ContentNegotiation + kotlinx.serialization json(). */
fun voyageJson(): Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    explicitNulls = false
}

fun createVoyageHttpClient(
    baseUrl: String,
    engine: HttpClientEngine,
    tokenProvider: () -> String?,
    refreshTokenProvider: () -> String? = { null },
    refreshTokens: (suspend () -> BearerTokens?)? = null,
    enableLogging: Boolean = false,
): HttpClient = HttpClient(engine) {
    expectSuccess = false
    install(ContentNegotiation) {
        json(voyageJson())
    }
    if (enableLogging) {
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.HEADERS
        }
    }
    install(Auth) {
        bearer {
            loadTokens {
                val access = tokenProvider() ?: return@loadTokens null
                BearerTokens(access, refreshTokenProvider().orEmpty())
            }
            sendWithoutRequest { request ->
                val path = request.url.toString()
                !path.contains("/auth/login") &&
                    !path.contains("/auth/register") &&
                    !path.contains("/auth/refresh")
            }
            if (refreshTokens != null) {
                refreshTokens {
                    refreshTokens()
                }
            }
        }
    }
    // Context7 / KMP skill: DefaultRequest uses url.takeFrom + headers.append
    defaultRequest {
        url.takeFrom(baseUrl.trimEnd('/') + "/")
        header(HttpHeaders.Accept, ContentType.Application.Json.toString())
    }
    applyCommonTimeouts()
}

private fun HttpClientConfig<*>.applyCommonTimeouts() {
    // Engine-specific timeouts configured in platform engines.
}

/** Context7: expect/actual httpClient with OkHttp (Android) / Darwin (iOS). */
expect fun createPlatformHttpEngine(): HttpClientEngine
