package com.example.shoptourr.data.media

import com.example.shoptourr.domain.model.MediaUploadIntent

/**
 * Upload strategy for receipt bytes.
 * v1: single PUT to pre-signed URL.
 * Later: tus / multipart resume with [offsetBytes].
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
) : ResumableMediaUploader {
    override suspend fun upload(
        intent: MediaUploadIntent,
        bytes: ByteArray,
        offsetBytes: Long,
        onProgress: ((uploaded: Long, total: Long) -> Unit)?,
    ): Result<Unit> = runCatching {
        // Foundation: ignore offset for single PUT; resume requires tus or multipart APIs.
        val slice = if (offsetBytes <= 0L) bytes else bytes.copyOfRange(offsetBytes.toInt(), bytes.size)
        put(intent.uploadUrl, slice, intent.requiredHeaders)
        onProgress?.invoke(bytes.size.toLong(), bytes.size.toLong())
    }
}
