package com.example.shoptourr.domain

import com.example.shoptourr.domain.hash.ContentChecksum
import com.example.shoptourr.domain.model.ReceiptUploadDraft
import com.example.shoptourr.domain.usecase.UploadReceiptUseCase
import com.example.shoptourr.fake.FakeMediaRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest

class UploadReceiptChecksumTest {

    @Test
    fun `upload receipt fills sha256 when missing`() = runTest {
        val repo = FakeMediaRepository()
        UploadReceiptUseCase(
            mediaRepository = repo,
            checksum = ContentChecksum { bytes -> "hash-${bytes.size}" },
        )(
            ReceiptUploadDraft(contentType = "image/jpeg", bytes = byteArrayOf(1, 2, 3)),
        ).getOrThrow()

        assertEquals("hash-3", repo.lastDraft?.sha256Hex)
    }

    @Test
    fun `upload receipt keeps provided sha256`() = runTest {
        val repo = FakeMediaRepository()
        UploadReceiptUseCase(
            mediaRepository = repo,
            checksum = ContentChecksum { "ignored" },
        )(
            ReceiptUploadDraft(
                contentType = "image/jpeg",
                bytes = byteArrayOf(1, 2, 3),
                sha256Hex = "a".repeat(64),
            ),
        ).getOrThrow()

        assertEquals("a".repeat(64), repo.lastDraft?.sha256Hex)
        assertNotNull(repo.lastDraft?.sha256Hex)
    }
}
