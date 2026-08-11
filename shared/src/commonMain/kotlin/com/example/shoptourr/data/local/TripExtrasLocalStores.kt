package com.example.shoptourr.data.local

import com.example.shoptourr.domain.model.BudgetAlert
import com.example.shoptourr.domain.model.DiaryDayGroup
import com.example.shoptourr.domain.model.DiaryEntry
import com.example.shoptourr.domain.model.TaxFreeSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

interface DiaryLocalStore {
    fun observe(tripId: String): Flow<List<DiaryDayGroup>>
    suspend fun replaceDays(tripId: String, days: List<DiaryDayGroup>)
    suspend fun upsertEntry(entry: DiaryEntry)
    suspend fun removeEntry(tripId: String, entryId: String)
}

class InMemoryDiaryLocalStore : DiaryLocalStore {
    private val byTrip = MutableStateFlow<Map<String, List<DiaryDayGroup>>>(emptyMap())

    override fun observe(tripId: String): Flow<List<DiaryDayGroup>> =
        byTrip.map { it[tripId].orEmpty() }

    override suspend fun replaceDays(tripId: String, days: List<DiaryDayGroup>) {
        byTrip.value = byTrip.value + (tripId to days)
    }

    override suspend fun upsertEntry(entry: DiaryEntry) {
        val existing = byTrip.value[entry.tripId].orEmpty()
            .flatMap { it.entries }
            .filterNot { it.id == entry.id } + entry
        byTrip.value = byTrip.value + (entry.tripId to group(existing))
    }

    override suspend fun removeEntry(tripId: String, entryId: String) {
        val existing = byTrip.value[tripId].orEmpty()
            .flatMap { it.entries }
            .filterNot { it.id == entryId }
        byTrip.value = byTrip.value + (tripId to group(existing))
    }

    private fun group(entries: List<DiaryEntry>): List<DiaryDayGroup> =
        entries.groupBy { it.entryDate }
            .toSortedMap()
            .map { (date, dayEntries) -> DiaryDayGroup(date = date, entries = dayEntries) }
}

interface TaxFreeLocalStore {
    fun observe(tripId: String): Flow<TaxFreeSummary?>
    fun save(summary: TaxFreeSummary)
}

class InMemoryTaxFreeLocalStore : TaxFreeLocalStore {
    private val byTrip = MutableStateFlow<Map<String, TaxFreeSummary>>(emptyMap())

    override fun observe(tripId: String): Flow<TaxFreeSummary?> =
        byTrip.map { it[tripId] }

    override fun save(summary: TaxFreeSummary) {
        byTrip.value = byTrip.value + (summary.tripId to summary)
    }
}

interface AlertsLocalStore {
    fun observe(tripId: String): Flow<List<BudgetAlert>>
    fun replaceAll(tripId: String, alerts: List<BudgetAlert>)
}

class InMemoryAlertsLocalStore : AlertsLocalStore {
    private val byTrip = MutableStateFlow<Map<String, List<BudgetAlert>>>(emptyMap())

    override fun observe(tripId: String): Flow<List<BudgetAlert>> =
        byTrip.map { it[tripId].orEmpty() }

    override fun replaceAll(tripId: String, alerts: List<BudgetAlert>) {
        byTrip.value = byTrip.value + (tripId to alerts)
    }
}
