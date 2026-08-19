package com.example.shoptourr.data.media

import com.example.shoptourr.domain.model.MediaUploadIntent

/**
 * Upload strategy for receipt bytes.
 * v1: single PUT to a pre-signed URL. The URL addresses one object; slicing
 * [offsetBytes] would overwrite it with a truncated body, so resume always
 * re-sends the full bytes. tus / multipart comes later.
 */
interface ResumableMediaUploader {
    suspend fun upload(
        intent: MediaUploadIntent,
        bytes: ByteArray,
        offsetBytes: Long = 0L,
        onProgress: ((uploaded: Long, total: Long) -> Unit)? = null,
    ): Result<Unit>
}

class PresignedPutMediaUploader(
    private val put: suspend (uploadUrl: String, bytes: ByteArray, headers: Map<String, String>) -> Unit,
    private val checkpoints: UploadCheckpointStore? = null,
) : ResumableMediaUploader {
    override suspend fun upload(
        intent: MediaUploadIntent,
        bytes: ByteArray,
        offsetBytes: Long,
        onProgress: ((uploaded: Long, total: Long) -> Unit)?,
    ): Result<Unit> = runCatching {
        put(intent.uploadUrl, bytes, intent.requiredHeaders)
        onProgress?.invoke(bytes.size.toLong(), bytes.size.toLong())
        checkpoints?.clear(intent.mediaId)
        Unit
    }.onFailure {
        checkpoints?.save(intent.mediaId, 0L)
    }
}
