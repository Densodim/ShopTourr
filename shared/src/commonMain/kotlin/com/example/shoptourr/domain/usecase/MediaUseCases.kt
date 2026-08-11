package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.hash.ContentChecksum
import com.example.shoptourr.domain.model.MediaAsset
import com.example.shoptourr.domain.model.ReceiptOcrResult
import com.example.shoptourr.domain.model.ReceiptUploadDraft
import com.example.shoptourr.domain.repository.MediaRepository

class UploadReceiptUseCase(
    private val mediaRepository: MediaRepository,
    private val checksum: ContentChecksum = ContentChecksum { "" },
) {
    suspend operator fun invoke(draft: ReceiptUploadDraft): Result<MediaAsset> {
        if (draft.contentType.isBlank()) return Result.failure(AppError.Validation("contentType"))
        if (draft.bytes.isEmpty()) return Result.failure(AppError.Validation("bytes"))
        val prepared = if (draft.sha256Hex.isNullOrBlank()) {
            draft.copy(sha256Hex = checksum.sha256Hex(draft.bytes))
        } else {
            draft
        }
        val intent = mediaRepository.createReceiptUploadIntent(prepared).getOrElse { return Result.failure(it) }
        mediaRepository.uploadBytes(intent, prepared.bytes).getOrElse { return Result.failure(it) }
        return mediaRepository.confirmUpload(intent.mediaId)
    }
}

class FetchReceiptOcrUseCase(
    private val mediaRepository: MediaRepository,
) {
    suspend operator fun invoke(mediaId: String): Result<ReceiptOcrResult> {
        if (mediaId.isBlank()) return Result.failure(AppError.Validation("mediaId"))
        return mediaRepository.getOcr(mediaId)
    }
}
