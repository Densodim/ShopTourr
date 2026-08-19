package com.example.shoptourr.data.media

import com.example.shoptourr.domain.model.MediaStatus
import com.example.shoptourr.domain.model.MediaUploadIntent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class PresignedPutMediaUploaderTest {

    @Test
    fun `single put always sends the full object even when an offset is supplied`() = runTest {
        val sent = mutableListOf<ByteArray>()
        val checkpoints = InMemoryUploadCheckpointStore()
        val uploader = PresignedPutMediaUploader(
            put = { _, bytes, _ -> sent += bytes },
            checkpoints = checkpoints,
        )
        val bytes = byteArrayOf(1, 2, 3, 4)

        uploader.upload(intent(), bytes, offsetBytes = 2L).getOrThrow()

        assertEquals(1, sent.size)
        assertEquals(bytes.toList(), sent.single().toList())
        assertEquals(0L, checkpoints.offsetBytes("media-1"))
    }

    @Test
    fun `failed put keeps a zero checkpoint so the next try is a full object`() = runTest {
        val checkpoints = InMemoryUploadCheckpointStore()
        val uploader = PresignedPutMediaUploader(
            put = { _, _, _ -> error("s3") },
            checkpoints = checkpoints,
        )

        val result = uploader.upload(intent(), byteArrayOf(1, 2, 3))

        assertTrue(result.isFailure)
        assertEquals(0L, checkpoints.offsetBytes("media-1"))
    }

    private fun intent() = MediaUploadIntent(
        mediaId = "media-1",
        uploadUrl = "https://s3.example/put",
        requiredHeaders = emptyMap(),
        uploadExpiresAt = "2099-01-01T00:00:00Z",
        status = MediaStatus.PENDING_UPLOAD,
    )
}
