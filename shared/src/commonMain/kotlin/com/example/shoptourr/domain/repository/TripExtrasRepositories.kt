package com.example.shoptourr.domain.repository

import com.example.shoptourr.domain.model.BudgetAlert
import com.example.shoptourr.domain.model.CreateDiaryDraft
import com.example.shoptourr.domain.model.DiaryDayGroup
import com.example.shoptourr.domain.model.DiaryEntry
import com.example.shoptourr.domain.model.TaxFreeSummary
import kotlinx.coroutines.flow.Flow

interface DiaryRepository {
    fun observeDiary(tripId: String): Flow<List<DiaryDayGroup>>
    suspend fun refresh(tripId: String): Result<Unit>
    suspend fun create(tripId: String, draft: CreateDiaryDraft): Result<DiaryEntry>
    suspend fun delete(tripId: String, entryId: String): Result<Unit>
}

interface TaxFreeRepository {
    fun observeSummary(tripId: String): Flow<TaxFreeSummary?>
    suspend fun refresh(tripId: String): Result<TaxFreeSummary>
}

interface AlertsRepository {
    fun observeAlerts(tripId: String): Flow<List<BudgetAlert>>
    suspend fun refresh(tripId: String): Result<Unit>
}
