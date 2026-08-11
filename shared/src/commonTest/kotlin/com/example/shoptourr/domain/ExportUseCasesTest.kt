package com.example.shoptourr.domain

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.CreateExportDraft
import com.example.shoptourr.domain.model.ExportFormat
import com.example.shoptourr.domain.model.ExportJob
import com.example.shoptourr.domain.model.ExportJobStatus
import com.example.shoptourr.domain.usecase.CreateExportUseCase
import com.example.shoptourr.domain.usecase.RefreshExportJobUseCase
import com.example.shoptourr.fake.FakeExportRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class ExportUseCasesTest {

    @Test
    fun `create export rejects blank trip id`() = runTest {
        assertEquals(
            AppError.Validation("tripId"),
            CreateExportUseCase(FakeExportRepository())
                .invoke(" ", CreateExportDraft(ExportFormat.PDF))
                .exceptionOrNull(),
        )
    }

    @Test
    fun `create export queues job`() = runTest {
        val repo = FakeExportRepository()
        val job = CreateExportUseCase(repo)(
            "lisbon",
            CreateExportDraft(format = ExportFormat.CSV, includeDiary = true),
        ).getOrThrow()
        assertEquals(ExportJobStatus.QUEUED, job.status)
        assertEquals(ExportFormat.CSV, job.format)
        assertEquals(1, repo.createCalls)
    }

    @Test
    fun `refresh export rejects blank id`() = runTest {
        assertEquals(
            AppError.Validation("exportId"),
            RefreshExportJobUseCase(FakeExportRepository()).invoke("").exceptionOrNull(),
        )
    }

    @Test
    fun `refresh export returns ready job`() = runTest {
        val ready = ExportJob(
            id = "e1",
            tripId = "lisbon",
            format = ExportFormat.PDF,
            status = ExportJobStatus.READY,
            downloadUrl = "https://cdn.example/export.pdf",
            createdAt = "2026-01-01T00:00:00Z",
            finishedAt = "2026-01-01T00:01:00Z",
        )
        val result = RefreshExportJobUseCase(
            FakeExportRepository(refreshSequence = listOf(ready)),
        )("e1").getOrThrow()
        assertTrue(result.isTerminal)
        assertEquals("https://cdn.example/export.pdf", result.downloadUrl)
    }
}
