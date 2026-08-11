package com.example.shoptourr.fake

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.BudgetAlert
import com.example.shoptourr.domain.model.CreateDiaryDraft
import com.example.shoptourr.domain.model.DiaryDayGroup
import com.example.shoptourr.domain.model.DiaryEntry
import com.example.shoptourr.domain.model.TaxFreeSummary
import com.example.shoptourr.domain.repository.AlertsRepository
import com.example.shoptourr.domain.repository.DiaryRepository
import com.example.shoptourr.domain.repository.TaxFreeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeDiaryRepository(
    private val mutateError: Throwable? = null,
) : DiaryRepository {
    private val entries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    var createCalls: Int = 0
        private set

    override fun observeDiary(tripId: String): Flow<List<DiaryDayGroup>> =
        entries.map { list ->
            list.filter { it.tripId == tripId }
                .groupBy { it.entryDate }
                .toSortedMap()
                .map { (date, dayEntries) -> DiaryDayGroup(date = date, entries = dayEntries) }
        }

    override suspend fun refresh(tripId: String): Result<Unit> = Result.success(Unit)

    override suspend fun create(tripId: String, draft: CreateDiaryDraft): Result<DiaryEntry> {
        mutateError?.let { return Result.failure(it) }
        createCalls += 1
        val entry = DiaryEntry(
            id = "d-$createCalls",
            tripId = tripId,
            entryDate = draft.entryDate ?: "2026-08-11",
            mood = draft.mood,
            text = draft.text,
            createdAt = "2026-08-11T00:00:00Z",
            updatedAt = "2026-08-11T00:00:00Z",
        )
        entries.update { it + entry }
        return Result.success(entry)
    }

    override suspend fun delete(tripId: String, entryId: String): Result<Unit> {
        mutateError?.let { return Result.failure(it) }
        if (entries.value.none { it.id == entryId && it.tripId == tripId }) {
            return Result.failure(AppError.NotFound)
        }
        entries.update { list -> list.filterNot { it.id == entryId } }
        return Result.success(Unit)
    }
}

class FakeTaxFreeRepository(
    initial: TaxFreeSummary? = null,
    private val refreshError: Throwable? = null,
) : TaxFreeRepository {
    private val state = MutableStateFlow(initial)
    var refreshCalls: Int = 0
        private set

    override fun observeSummary(tripId: String): Flow<TaxFreeSummary?> =
        state.map { summary -> summary?.takeIf { it.tripId == tripId } }

    override suspend fun refresh(tripId: String): Result<TaxFreeSummary> {
        refreshCalls += 1
        refreshError?.let { return Result.failure(it) }
        val current = state.value?.takeIf { it.tripId == tripId }
            ?: return Result.failure(AppError.NotFound)
        return Result.success(current)
    }
}

class FakeAlertsRepository(
    initial: List<BudgetAlert> = emptyList(),
    private val refreshError: Throwable? = null,
) : AlertsRepository {
    private val state = MutableStateFlow(initial)

    override fun observeAlerts(tripId: String): Flow<List<BudgetAlert>> = state.asStateFlow()

    override suspend fun refresh(tripId: String): Result<Unit> {
        refreshError?.let { return Result.failure(it) }
        return Result.success(Unit)
    }
}
