package com.example.shoptourr.data.remote

import com.example.shoptourr.data.remote.dto.media.ConfirmMediaUploadRequest
import com.example.shoptourr.data.remote.dto.media.CreateMediaUploadIntentRequest
import com.example.shoptourr.data.remote.dto.media.MediaAssetDto
import com.example.shoptourr.data.remote.dto.media.MediaUploadIntentResponse
import com.example.shoptourr.data.remote.dto.media.ReceiptOcrResultDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class MediaApi(
    private val client: HttpClient,
    private val uploadClient: HttpClient,
    private val baseUrl: String,
) {
    private val root get() = baseUrl.trimEnd('/')

    suspend fun createUploadIntent(
        request: CreateMediaUploadIntentRequest,
        idempotencyKey: String,
    ): MediaUploadIntentResponse {
        val response: HttpResponse = client.post("$root/media/upload-intents") {
            contentType(ContentType.Application.Json)
            header("Idempotency-Key", idempotencyKey)
            setBody(request)
        }
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun uploadBytes(
        uploadUrl: String,
        bytes: ByteArray,
        requiredHeaders: Map<String, String>,
    ) {
        val response: HttpResponse = uploadClient.put(uploadUrl) {
            requiredHeaders.forEach { (key, value) ->
                if (key.equals(HttpHeaders.ContentType, ignoreCase = true)) {
                    contentType(ContentType.parse(value))
                } else {
                    header(key, value)
                }
            }
            setBody(bytes)
        }
        if (!response.status.isSuccess() && response.status != HttpStatusCode.NoContent) {
            throw mapHttpStatus(response.status)
        }
    }

    suspend fun probeUploadOffset(uploadUrl: String): Long {
        val response: HttpResponse = uploadClient.head(uploadUrl) {
            header(TUS_RESUMABLE, TUS_VERSION)
        }
        if (!response.status.isSuccess() && response.status != HttpStatusCode.NoContent) {
            throw mapHttpStatus(response.status)
        }
        return response.headers[UPLOAD_OFFSET]?.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
    }

    suspend fun patchUpload(uploadUrl: String, offset: Long, chunk: ByteArray): Long {
        val response: HttpResponse = uploadClient.patch(uploadUrl) {
            header(TUS_RESUMABLE, TUS_VERSION)
            header(UPLOAD_OFFSET, offset.toString())
            contentType(ContentType.parse(TUS_OFFSET_CONTENT_TYPE))
            setBody(chunk)
        }
        if (!response.status.isSuccess() && response.status != HttpStatusCode.NoContent) {
            throw mapHttpStatus(response.status)
        }
        return response.headers[UPLOAD_OFFSET]?.toLongOrNull()
            ?: (offset + chunk.size)
    }

    suspend fun confirm(mediaId: String, request: ConfirmMediaUploadRequest = ConfirmMediaUploadRequest()): MediaAssetDto {
        val response: HttpResponse = client.post("$root/media/$mediaId/confirm") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun fetchAsset(mediaId: String): MediaAssetDto {
        val response: HttpResponse = client.get("$root/media/$mediaId")
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun fetchOcr(mediaId: String): ReceiptOcrResultDto {
        val response: HttpResponse = client.get("$root/media/$mediaId/ocr")
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    companion object {
        const val TUS_RESUMABLE = "Tus-Resumable"
        const val TUS_VERSION = "1.0.0"
        const val UPLOAD_OFFSET = "Upload-Offset"
        const val TUS_OFFSET_CONTENT_TYPE = "application/offset+octet-stream"
    }
}
