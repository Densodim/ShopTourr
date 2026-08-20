package com.example.shoptourr.data.media

import com.example.shoptourr.domain.model.MediaStatus
import com.example.shoptourr.domain.model.MediaUploadIntent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class TusOffsetMediaUploaderTest {

    @Test
    fun `uploads remaining chunks and clears the checkpoint`() = runTest {
        val sent = mutableListOf<Pair<Long, List<Byte>>>()
        val checkpoints = InMemoryUploadCheckpointStore()
        checkpoints.save("media-1", 2L)
        val uploader = TusOffsetMediaUploader(
            probeOffset = { 2L },
            patch = { _, offset, chunk ->
                sent += offset to chunk.toList()
                offset + chunk.size
            },
            checkpoints = checkpoints,
            chunkSize = 2,
        )

        uploader.upload(intent(), byteArrayOf(1, 2, 3, 4, 5)).getOrThrow()

        assertEquals(
            listOf(2L to listOf<Byte>(3, 4), 4L to listOf<Byte>(5)),
            sent,
        )
        assertEquals(0L, checkpoints.offsetBytes("media-1"))
    }

    @Test
    fun `failed chunk keeps the last acknowledged offset`() = runTest {
        val checkpoints = InMemoryUploadCheckpointStore()
        var calls = 0
        val uploader = TusOffsetMediaUploader(
            probeOffset = { 0L },
            patch = { _, offset, chunk ->
                calls += 1
                if (calls == 2) error("wifi")
                offset + chunk.size
            },
            checkpoints = checkpoints,
            chunkSize = 2,
        )

        val result = uploader.upload(intent(), byteArrayOf(1, 2, 3, 4))

        assertTrue(result.isFailure)
        assertEquals(2L, checkpoints.offsetBytes("media-1"))
    }

    @Test
    fun `uses the server offset when the local checkpoint is stale`() = runTest {
        val sentOffsets = mutableListOf<Long>()
        val checkpoints = InMemoryUploadCheckpointStore()
        checkpoints.save("media-1", 4L)
        val uploader = TusOffsetMediaUploader(
            probeOffset = { 2L },
            patch = { _, offset, chunk ->
                sentOffsets += offset
                offset + chunk.size
            },
            checkpoints = checkpoints,
            chunkSize = 8,
        )

        uploader.upload(intent(), byteArrayOf(1, 2, 3, 4)).getOrThrow()

        assertEquals(listOf(2L), sentOffsets)
    }

    private fun intent() = MediaUploadIntent(
        mediaId = "media-1",
        uploadUrl = "https://s3.example/put",
        requiredHeaders = emptyMap(),
        uploadExpiresAt = "2099-01-01T00:00:00Z",
        status = MediaStatus.PENDING_UPLOAD,
    )
}
