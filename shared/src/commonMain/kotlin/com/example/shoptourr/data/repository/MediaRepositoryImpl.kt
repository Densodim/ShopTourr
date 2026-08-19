package com.example.shoptourr.data.repository

import com.example.shoptourr.data.media.PresignedPutMediaUploader
import com.example.shoptourr.data.media.ResumableMediaUploader
import com.example.shoptourr.data.remote.MediaApi
import com.example.shoptourr.data.remote.dto.media.CreateMediaUploadIntentRequest
import com.example.shoptourr.data.remote.dto.media.MediaAssetDto
import com.example.shoptourr.data.remote.dto.media.MediaPurpose as ApiMediaPurpose
import com.example.shoptourr.data.remote.dto.media.MediaStatus as ApiMediaStatus
import com.example.shoptourr.data.remote.dto.media.MediaUploadIntentResponse
import com.example.shoptourr.data.remote.dto.media.ReceiptOcrResultDto
import com.example.shoptourr.data.remote.mapHttpAppError
import com.example.shoptourr.domain.model.MediaAsset
import com.example.shoptourr.domain.model.MediaPurpose
import com.example.shoptourr.domain.model.MediaStatus
import com.example.shoptourr.domain.model.MediaUploadIntent
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.ReceiptOcrResult
import com.example.shoptourr.domain.model.ReceiptUploadDraft
import com.example.shoptourr.domain.repository.MediaRepository

class MediaRepositoryImpl(
    private val api: MediaApi,
    private val idempotencyKey: () -> String,
    private val uploader: ResumableMediaUploader = PresignedPutMediaUploader(
        put = { url, bytes, headers ->
            api.uploadBytes(uploadUrl = url, bytes = bytes, requiredHeaders = headers)
        },
    ),
) : MediaRepository {
    override suspend fun createReceiptUploadIntent(draft: ReceiptUploadDraft): Result<MediaUploadIntent> =
        runCatching {
            api.createUploadIntent(
                request = CreateMediaUploadIntentRequest(
                    purpose = ApiMediaPurpose.RECEIPT,
                    contentType = draft.contentType,
                    byteSize = draft.bytes.size.toLong(),
                    sha256Hex = draft.sha256Hex,
                ),
                idempotencyKey = idempotencyKey(),
            ).toDomain()
        }.mapHttpAppError()

    override suspend fun uploadBytes(intent: MediaUploadIntent, bytes: ByteArray): Result<Unit> =
        uploader.upload(intent = intent, bytes = bytes).mapHttpAppError()

    override suspend fun confirmUpload(mediaId: String): Result<MediaAsset> =
        runCatching { api.confirm(mediaId).toDomain() }.mapHttpAppError()

    override suspend fun getAsset(mediaId: String): Result<MediaAsset> =
        runCatching { api.fetchAsset(mediaId).toDomain() }.mapHttpAppError()

    override suspend fun getOcr(mediaId: String): Result<ReceiptOcrResult> =
        runCatching { api.fetchOcr(mediaId).toDomain() }.mapHttpAppError()
}

private fun MediaUploadIntentResponse.toDomain(): MediaUploadIntent =
    MediaUploadIntent(
        mediaId = mediaId,
        uploadUrl = uploadUrl,
        requiredHeaders = requiredHeaders,
        uploadExpiresAt = uploadExpiresAt,
        status = status.toDomain(),
    )

private fun MediaAssetDto.toDomain(): MediaAsset =
    MediaAsset(
        id = id,
        purpose = purpose.toDomain(),
        status = status.toDomain(),
        contentType = contentType,
        byteSize = byteSize,
        downloadUrl = downloadUrl,
        thumbnailUrl = thumbnailUrl,
        createdAt = createdAt,
    )

private fun ReceiptOcrResultDto.toDomain(): ReceiptOcrResult =
    ReceiptOcrResult(
        mediaId = mediaId,
        suggestedName = suggestedName,
        suggestedAmount = suggestedAmount,
        suggestedPlace = suggestedPlace,
        suggestedCategory = suggestedCategory?.let { raw ->
            runCatching { PurchaseCategory.valueOf(raw.uppercase()) }.getOrNull()
        },
        confidence = confidence,
    )

private fun ApiMediaPurpose.toDomain(): MediaPurpose = when (this) {
    ApiMediaPurpose.RECEIPT -> MediaPurpose.RECEIPT
    ApiMediaPurpose.AVATAR -> MediaPurpose.AVATAR
    ApiMediaPurpose.DIARY -> MediaPurpose.DIARY
    ApiMediaPurpose.EXPORT -> MediaPurpose.EXPORT
}

private fun ApiMediaStatus.toDomain(): MediaStatus = when (this) {
    ApiMediaStatus.PENDING_UPLOAD -> MediaStatus.PENDING_UPLOAD
    ApiMediaStatus.UPLOADED -> MediaStatus.UPLOADED
    ApiMediaStatus.PROCESSING -> MediaStatus.PROCESSING
    ApiMediaStatus.READY -> MediaStatus.READY
    ApiMediaStatus.FAILED -> MediaStatus.FAILED
}
