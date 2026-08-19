package com.example.shoptourr.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
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
import io.ktor.http.HttpMethod
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import com.example.shoptourr.i18n.VoyageI18n
import com.example.shoptourr.observability.NoOpObservability
import com.example.shoptourr.observability.Observability
import com.example.shoptourr.security.CertificatePinConfig
import kotlin.random.Random
import kotlinx.serialization.json.Json

/** Context7-verified: ContentNegotiation + kotlinx.serialization json(). */
fun voyageJson(): Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    explicitNulls = false
}

private const val REQUEST_ID_HEADER = "X-Request-Id"

fun newRequestId(): String =
    buildString(36) {
        repeat(8) { append(Random.nextInt(0, 16).toString(16)) }
        append('-')
        repeat(4) { append(Random.nextInt(0, 16).toString(16)) }
        append("-4")
        repeat(3) { append(Random.nextInt(0, 16).toString(16)) }
        append('-')
        append(listOf('8', '9', 'a', 'b').random())
        repeat(3) { append(Random.nextInt(0, 16).toString(16)) }
        append('-')
        repeat(12) { append(Random.nextInt(0, 16).toString(16)) }
    }

private fun requestIdPlugin(observability: Observability) = createClientPlugin("VoyageRequestId") {
    onRequest { request, _ ->
        val existing = request.headers[REQUEST_ID_HEADER]
        val requestId = if (existing.isNullOrBlank()) {
            val generated = newRequestId()
            request.headers.append(REQUEST_ID_HEADER, generated)
            generated
        } else {
            existing
        }
        observability.addBreadcrumb(
            message = "http.request",
            category = "http",
            data = mapOf(
                "request_id" to requestId,
                "method" to request.method.value,
                "url" to request.url.buildString(),
            ),
        )
    }
}

internal const val VOYAGE_REQUEST_TIMEOUT_MS = 15_000L
internal const val VOYAGE_CONNECT_TIMEOUT_MS = 10_000L
internal const val VOYAGE_SOCKET_TIMEOUT_MS = 15_000L
internal const val VOYAGE_HTTP_MAX_RETRIES = 3

fun createVoyageHttpClient(
    baseUrl: String,
    engine: HttpClientEngine,
    tokenProvider: () -> String?,
    refreshTokenProvider: () -> String? = { null },
    refreshTokens: (suspend () -> BearerTokens?)? = null,
    enableLogging: Boolean = false,
    observability: Observability = NoOpObservability,
    retryDelayMillis: Long? = null,
    acceptLanguage: () -> String = { VoyageI18n.currentLocale.tag },
): HttpClient = HttpClient(engine) {
    expectSuccess = false
    install(ContentNegotiation) {
        json(voyageJson())
    }
    install(requestIdPlugin(observability))
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
        header(HttpHeaders.AcceptLanguage, acceptLanguage())
    }
    // Retry before timeout so timed-out GET/HEAD can be retried (Ktor plugin order).
    installVoyageSafeRetry(retryDelayMillis)
    installVoyageHttpTimeouts()
}

internal fun HttpClientConfig<*>.installVoyageHttpTimeouts() {
    install(HttpTimeout) {
        requestTimeoutMillis = VOYAGE_REQUEST_TIMEOUT_MS
        connectTimeoutMillis = VOYAGE_CONNECT_TIMEOUT_MS
        socketTimeoutMillis = VOYAGE_SOCKET_TIMEOUT_MS
    }
}

internal fun HttpClientConfig<*>.installVoyageSafeRetry(retryDelayMillis: Long? = null) {
    install(HttpRequestRetry) {
        maxRetries = VOYAGE_HTTP_MAX_RETRIES
        retryIf { request, response ->
            request.method.isIdempotentSafe() && response.status.value in 500..599
        }
        retryOnExceptionIf { request, cause ->
            request.method.isIdempotentSafe() && cause.isRetryableTimeout()
        }
        val delayMs = retryDelayMillis
        if (delayMs != null) {
            delayMillis { delayMs }
        } else {
            exponentialDelay()
        }
    }
}

private fun HttpMethod.isIdempotentSafe(): Boolean =
    this == HttpMethod.Get || this == HttpMethod.Head

private fun Throwable.isRetryableTimeout(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        if (
            current is HttpRequestTimeoutException ||
            current is ConnectTimeoutException ||
            current is SocketTimeoutException
        ) {
            return true
        }
        current = current.cause
    }
    return false
}

/** Context7: expect/actual httpClient with OkHttp (Android) / Darwin (iOS). */
expect fun createPlatformHttpEngine(
    pinConfig: CertificatePinConfig = CertificatePinConfig.Empty,
    enforcePinning: Boolean = false,
): HttpClientEngine
