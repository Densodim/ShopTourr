package com.example.shoptourr.data.media

import com.example.shoptourr.domain.model.MediaUploadIntent
import com.example.shoptourr.domain.model.UploadChunks
import com.example.shoptourr.domain.model.UploadResumeOffset

/**
 * tus-lite upload: probe [Upload-Offset] then PATCH remaining chunks.
 * Local checkpoints survive process death; the server offset wins when reachable.
 */
interface ResumableMediaUploader {
    suspend fun upload(
        intent: MediaUploadIntent,
        bytes: ByteArray,
        offsetBytes: Long = 0L,
        onProgress: ((uploaded: Long, total: Long) -> Unit)? = null,
    ): Result<Unit>
}

class TusOffsetMediaUploader(
    private val probeOffset: suspend (uploadUrl: String) -> Long,
    private val patch: suspend (uploadUrl: String, offset: Long, chunk: ByteArray) -> Long,
    private val checkpoints: UploadCheckpointStore? = null,
    private val chunkSize: Int = UploadChunks.DEFAULT_SIZE,
) : ResumableMediaUploader {
    override suspend fun upload(
        intent: MediaUploadIntent,
        bytes: ByteArray,
        offsetBytes: Long,
        onProgress: ((uploaded: Long, total: Long) -> Unit)?,
    ): Result<Unit> = runCatching {
        val total = bytes.size.toLong()
        val checkpoint = maxOf(offsetBytes, checkpoints?.offsetBytes(intent.mediaId) ?: 0L)
        val serverOffset = runCatching { probeOffset(intent.uploadUrl) }.getOrNull()
        var offset = UploadResumeOffset.resolve(checkpoint, serverOffset).coerceAtMost(total)
        onProgress?.invoke(offset, total)
        while (offset < total) {
            val chunk = UploadChunks.slice(bytes, offset, chunkSize)
            if (chunk.isEmpty()) break
            val next = patch(intent.uploadUrl, offset, chunk)
            if (next <= offset) error("upload offset did not advance")
            offset = next
            checkpoints?.save(intent.mediaId, offset)
            onProgress?.invoke(offset, total)
        }
        checkpoints?.clear(intent.mediaId)
    }
}
