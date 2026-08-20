package com.example.shoptourr.domain

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.MediaStatus
import com.example.shoptourr.domain.model.ReceiptUploadDraft
import com.example.shoptourr.domain.usecase.FetchReceiptOcrUseCase
import com.example.shoptourr.domain.usecase.RegisterUseCase
import com.example.shoptourr.domain.usecase.UploadReceiptUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import com.example.shoptourr.fake.FakeMediaRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class MediaRegisterUseCasesTest {

    @Test
    fun `upload receipt rejects empty bytes`() = runTest {
        assertEquals(
            AppError.Validation("bytes"),
            UploadReceiptUseCase(FakeMediaRepository())
                .invoke(ReceiptUploadDraft(contentType = "image/jpeg", bytes = ByteArray(0)))
                .exceptionOrNull(),
        )
    }

    @Test
    fun `upload receipt rejects a non image content type`() = runTest {
        assertEquals(
            AppError.Validation("contentType"),
            UploadReceiptUseCase(FakeMediaRepository())
                .invoke(ReceiptUploadDraft(contentType = "text/html", bytes = byteArrayOf(1)))
                .exceptionOrNull(),
        )
    }

    @Test
    fun `upload receipt runs intent upload confirm`() = runTest {
        val repo = FakeMediaRepository()
        val asset = UploadReceiptUseCase(repo)(
            ReceiptUploadDraft(contentType = "image/jpeg", bytes = byteArrayOf(1, 2, 3)),
        ).getOrThrow()
        assertEquals("media-1", asset.id)
        assertEquals(MediaStatus.READY, asset.status)
        assertEquals(1, repo.createCalls)
        assertEquals(1, repo.uploadCalls)
        assertEquals(1, repo.confirmCalls)
    }

    @Test
    fun `fetch ocr returns suggestions`() = runTest {
        val result = FetchReceiptOcrUseCase(FakeMediaRepository())("media-1").getOrThrow()
        assertEquals("Pasteis de Belem", result.suggestedName)
        assertEquals("4.50", result.suggestedAmount)
    }

    @Test
    fun `register rejects short password`() = runTest {
        assertEquals(
            AppError.Validation("password"),
            RegisterUseCase(FakeAuthRepository()).invoke("Ada", "ada@ex.com", "short").exceptionOrNull(),
        )
    }

    @Test
    fun `register succeeds`() = runTest {
        val session = RegisterUseCase(FakeAuthRepository())("Ada", "ada@ex.com", "password1").getOrThrow()
        assertEquals("ada@ex.com", session.user.email)
    }
}
