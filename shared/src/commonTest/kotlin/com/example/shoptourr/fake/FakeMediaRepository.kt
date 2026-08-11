package com.example.shoptourr.fake

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.MediaAsset
import com.example.shoptourr.domain.model.MediaPurpose
import com.example.shoptourr.domain.model.MediaStatus
import com.example.shoptourr.domain.model.MediaUploadIntent
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.ReceiptOcrResult
import com.example.shoptourr.domain.model.ReceiptUploadDraft
import com.example.shoptourr.domain.repository.MediaRepository

class FakeMediaRepository(
    private val createError: Throwable? = null,
    private val uploadError: Throwable? = null,
    private val confirmError: Throwable? = null,
    private val ocrError: Throwable? = null,
    private val ocr: ReceiptOcrResult = ReceiptOcrResult(
        mediaId = "media-1",
        suggestedName = "Pasteis de Belem",
        suggestedAmount = "4.50",
        suggestedPlace = "Belem",
        suggestedCategory = PurchaseCategory.FOOD,
        confidence = 0.92,
    ),
) : MediaRepository {
    var createCalls: Int = 0
        private set
    var uploadCalls: Int = 0
        private set
    var confirmCalls: Int = 0
        private set
    var ocrCalls: Int = 0
        private set
    var lastDraft: ReceiptUploadDraft? = null
        private set

    override suspend fun createReceiptUploadIntent(draft: ReceiptUploadDraft): Result<MediaUploadIntent> {
        createCalls += 1
        lastDraft = draft
        createError?.let { return Result.failure(it) }
        return Result.success(
            MediaUploadIntent(
                mediaId = "media-1",
                uploadUrl = "https://upload.example/media-1",
                requiredHeaders = mapOf("Content-Type" to draft.contentType),
                uploadExpiresAt = "2026-01-01T01:00:00Z",
                status = MediaStatus.PENDING_UPLOAD,
            ),
        )
    }

    override suspend fun uploadBytes(intent: MediaUploadIntent, bytes: ByteArray): Result<Unit> {
        uploadCalls += 1
        uploadError?.let { return Result.failure(it) }
        return Result.success(Unit)
    }

    override suspend fun confirmUpload(mediaId: String): Result<MediaAsset> {
        confirmCalls += 1
        confirmError?.let { return Result.failure(it) }
        return Result.success(
            MediaAsset(
                id = mediaId,
                purpose = MediaPurpose.RECEIPT,
                status = MediaStatus.READY,
                contentType = "image/jpeg",
                byteSize = 12,
                createdAt = "2026-01-01T00:00:00Z",
            ),
        )
    }

    override suspend fun getAsset(mediaId: String): Result<MediaAsset> =
        confirmUpload(mediaId)

    override suspend fun getOcr(mediaId: String): Result<ReceiptOcrResult> {
        ocrCalls += 1
        ocrError?.let { return Result.failure(it) }
        return Result.success(ocr.copy(mediaId = mediaId))
    }
}
