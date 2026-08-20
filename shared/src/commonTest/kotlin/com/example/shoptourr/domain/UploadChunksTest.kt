package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.UploadChunks
import com.example.shoptourr.domain.model.UploadResumeOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UploadChunksTest {

    @Test
    fun `empty remaining range at the end of the file`() {
        val bytes = byteArrayOf(1, 2, 3, 4)
        assertTrue(UploadChunks.slice(bytes, offset = 4L).isEmpty())
    }

    @Test
    fun `first chunk is at most the configured size`() {
        val bytes = ByteArray(10) { it.toByte() }
        val chunk = UploadChunks.slice(bytes, offset = 0L, chunkSize = 4)
        assertEquals(listOf<Byte>(0, 1, 2, 3), chunk.toList())
    }

    @Test
    fun `last chunk is the remainder`() {
        val bytes = byteArrayOf(1, 2, 3, 4, 5)
        val chunk = UploadChunks.slice(bytes, offset = 4L, chunkSize = 4)
        assertEquals(listOf<Byte>(5), chunk.toList())
    }

    @Test
    fun `server offset wins over a stale local checkpoint`() {
        assertEquals(256L, UploadResumeOffset.resolve(checkpoint = 512L, serverOffset = 256L))
        assertEquals(128L, UploadResumeOffset.resolve(checkpoint = 0L, serverOffset = 128L))
        assertEquals(64L, UploadResumeOffset.resolve(checkpoint = 64L, serverOffset = null))
    }
}
