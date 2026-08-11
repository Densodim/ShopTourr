package com.example.shoptourr.domain

import com.example.shoptourr.domain.hash.ContentChecksum
import com.example.shoptourr.domain.media.CompressedReceipt
import com.example.shoptourr.domain.media.ReceiptImageCompressor
import com.example.shoptourr.domain.model.ReceiptUploadDraft
import com.example.shoptourr.domain.usecase.UploadReceiptUseCase
import com.example.shoptourr.fake.FakeMediaRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class UploadReceiptCompressionTest {

    @Test
    fun `upload receipt compresses bytes before checksum and intent`() = runTest {
        val repo = FakeMediaRepository()
        val original = ByteArray(2_000) { 7 }
        val compressed = byteArrayOf(1, 2, 3)

        UploadReceiptUseCase(
            mediaRepository = repo,
            checksum = ContentChecksum { bytes -> "hash-${bytes.size}" },
            compressor = ReceiptImageCompressor { bytes, _ ->
                assertTrue(bytes.contentEquals(original))
                CompressedReceipt(bytes = compressed, contentType = "image/jpeg")
            },
        )(
            ReceiptUploadDraft(contentType = "image/png", bytes = original),
        ).getOrThrow()

        assertEquals("image/jpeg", repo.lastDraft?.contentType)
        assertTrue(repo.lastDraft!!.bytes.contentEquals(compressed))
        assertEquals("hash-3", repo.lastDraft?.sha256Hex)
    }

    @Test
    fun `upload receipt keeps provided sha256 after compression`() = runTest {
        val repo = FakeMediaRepository()
        UploadReceiptUseCase(
            mediaRepository = repo,
            checksum = ContentChecksum { "ignored" },
            compressor = ReceiptImageCompressor { bytes, contentType ->
                CompressedReceipt(bytes = byteArrayOf(9), contentType = contentType)
            },
        )(
            ReceiptUploadDraft(
                contentType = "image/jpeg",
                bytes = byteArrayOf(1, 2, 3, 4),
                sha256Hex = "c".repeat(64),
            ),
        ).getOrThrow()

        assertEquals("c".repeat(64), repo.lastDraft?.sha256Hex)
        assertEquals(1, repo.lastDraft!!.bytes.size)
    }
}
