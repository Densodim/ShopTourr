package com.example.shoptourr.data

import com.example.shoptourr.data.remote.MediaApi
import com.example.shoptourr.data.remote.createVoyageHttpClient
import com.example.shoptourr.data.remote.dto.media.MediaAssetDto
import com.example.shoptourr.data.remote.dto.media.MediaPurpose
import com.example.shoptourr.data.remote.dto.media.MediaStatus
import com.example.shoptourr.data.remote.dto.media.MediaUploadIntentResponse
import com.example.shoptourr.data.repository.MediaRepositoryImpl
import com.example.shoptourr.domain.hash.ContentChecksum
import com.example.shoptourr.domain.model.MediaStatus as DomainMediaStatus
import com.example.shoptourr.domain.model.ReceiptUploadDraft
import com.example.shoptourr.domain.usecase.UploadReceiptUseCase
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MediaUploadIntentIntegrationTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; explicitNulls = false }

    @Test
    fun `upload receipt flows intent put confirm with checksum and idempotency`() = runTest {
        val bytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte())
        val checksum = "b".repeat(64)
        var intentPosted = 0
        var putPosted = 0
        var confirmPosted = 0
        var seenIdempotency: String? = null
        var putContentType: String? = null

        val apiEngine = MockEngine { request ->
            when {
                request.method == HttpMethod.Post &&
                    request.url.encodedPath.endsWith("/media/upload-intents") -> {
                    intentPosted += 1
                    seenIdempotency = request.headers["Idempotency-Key"]
                    val bodyText = (request.body as? TextContent)?.text.orEmpty()
                    assertTrue(bodyText.contains("\"byteSize\":${bytes.size}"))
                    assertTrue(bodyText.contains(checksum))
                    val response = MediaUploadIntentResponse(
                        mediaId = "media-42",
                        uploadUrl = "https://cdn.test/upload/media-42",
                        requiredHeaders = mapOf("Content-Type" to "image/jpeg"),
                        uploadExpiresAt = "2026-08-11T18:00:00Z",
                        status = MediaStatus.PENDING_UPLOAD,
                    )
                    respond(
                        content = ByteReadChannel(json.encodeToString(response)),
                        status = HttpStatusCode.Created,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
                request.method == HttpMethod.Post &&
                    request.url.encodedPath.endsWith("/media/media-42/confirm") -> {
                    confirmPosted += 1
                    val asset = MediaAssetDto(
                        id = "media-42",
                        purpose = MediaPurpose.RECEIPT,
                        status = MediaStatus.READY,
                        contentType = "image/jpeg",
                        byteSize = bytes.size.toLong(),
                        createdAt = "2026-08-11T17:00:00Z",
                    )
                    respond(
                        content = ByteReadChannel(json.encodeToString(asset)),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
                else -> error("Unexpected API call ${request.method} ${request.url}")
            }
        }
        val uploadEngine = MockEngine { request ->
            assertEquals(HttpMethod.Put, request.method)
            assertEquals("https://cdn.test/upload/media-42", request.url.toString())
            putPosted += 1
            putContentType = request.body.contentType?.toString()
                ?: request.headers[HttpHeaders.ContentType]
            respondOk()
        }

        val apiClient = createVoyageHttpClient(
            baseUrl = "https://api.test",
            engine = apiEngine,
            tokenProvider = { "token" },
        )
        val uploadClient = HttpClient(uploadEngine) { expectSuccess = false }
        val asset = UploadReceiptUseCase(
            mediaRepository = MediaRepositoryImpl(
                api = MediaApi(
                    client = apiClient,
                    uploadClient = uploadClient,
                    baseUrl = "https://api.test",
                ),
                idempotencyKey = { "idem-media-1" },
            ),
            checksum = ContentChecksum { checksum },
        )(
            ReceiptUploadDraft(contentType = "image/jpeg", bytes = bytes),
        ).getOrThrow()

        assertEquals("media-42", asset.id)
        assertEquals(DomainMediaStatus.READY, asset.status)
        assertEquals(1, intentPosted)
        assertEquals(1, putPosted)
        assertEquals(1, confirmPosted)
        assertEquals("idem-media-1", seenIdempotency)
        assertEquals("image/jpeg", putContentType)
    }
}
