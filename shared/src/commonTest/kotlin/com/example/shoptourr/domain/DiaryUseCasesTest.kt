package com.example.shoptourr.domain

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.CreateDiaryDraft
import com.example.shoptourr.domain.usecase.CreateDiaryEntryUseCase
import com.example.shoptourr.domain.usecase.DeleteDiaryEntryUseCase
import com.example.shoptourr.fake.FakeDiaryRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class DiaryUseCasesTest {

    @Test
    fun `create rejects blank mood and text`() = runTest {
        val repo = FakeDiaryRepository()
        assertEquals(
            AppError.Validation("mood"),
            CreateDiaryEntryUseCase(repo)("t1", CreateDiaryDraft(mood = " ", text = "hi"))
                .exceptionOrNull(),
        )
        assertEquals(
            AppError.Validation("text"),
            CreateDiaryEntryUseCase(repo)("t1", CreateDiaryDraft(mood = "good", text = " "))
                .exceptionOrNull(),
        )
        assertEquals(0, repo.createCalls)
    }

    @Test
    fun `create rejects punctuation-only mood`() = runTest {
        assertEquals(
            AppError.Validation("mood"),
            CreateDiaryEntryUseCase(FakeDiaryRepository())(
                "t1",
                CreateDiaryDraft(mood = "!!!!!!!!", text = "Walked the city"),
            ).exceptionOrNull(),
        )
    }

    @Test
    fun `create persists trimmed entry`() = runTest {
        val repo = FakeDiaryRepository()
        val entry = CreateDiaryEntryUseCase(repo)(
            "lisbon",
            CreateDiaryDraft(mood = "  happy  ", text = "  Pasteis  "),
        ).getOrThrow()
        assertEquals("happy", entry.mood)
        assertEquals("Pasteis", entry.text)
        assertEquals(1, repo.createCalls)
    }

    @Test
    fun `delete rejects blank ids`() = runTest {
        assertEquals(
            AppError.Validation("entryId"),
            DeleteDiaryEntryUseCase(FakeDiaryRepository())("t1", " ").exceptionOrNull(),
        )
    }
}
