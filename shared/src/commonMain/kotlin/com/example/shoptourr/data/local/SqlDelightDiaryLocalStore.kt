package com.example.shoptourr.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.shoptourr.db.DiaryEntity
import com.example.shoptourr.db.VoyageDatabase
import com.example.shoptourr.domain.model.DiaryDayGroup
import com.example.shoptourr.domain.model.DiaryEntry
import com.example.shoptourr.domain.model.FtsQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightDiaryLocalStore(
    private val db: VoyageDatabase,
) : DiaryLocalStore {

    override fun observe(tripId: String): Flow<List<DiaryDayGroup>> =
        db.diaryEntityQueries.selectByTrip(tripId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> group(rows.map { it.toDomain() }) }

    override fun search(tripId: String, query: String): List<DiaryEntry> {
        val match = FtsQuery.fromUserInput(query)
        if (match == null) {
            return db.diaryEntityQueries.selectByTrip(tripId).executeAsList().map { it.toDomain() }
        }
        val ftsHits = db.diaryFtsQueries.searchIdsByTrip(tripId, match).executeAsList()
            .mapNotNull { row ->
                val id = row.entry_id ?: return@mapNotNull null
                db.diaryEntityQueries.selectById(id).executeAsOneOrNull()?.toDomain()
            }
        if (ftsHits.isNotEmpty()) return ftsHits
        val needle = query.trim()
        return db.diaryEntityQueries.selectByTrip(tripId).executeAsList()
            .map { it.toDomain() }
            .filter { it.text.contains(needle, ignoreCase = true) }
    }

    override suspend fun replaceDays(tripId: String, days: List<DiaryDayGroup>) =
        withContext(Dispatchers.IO) {
            db.transaction {
                db.diaryFtsQueries.deleteByTrip(tripId)
                db.diaryEntityQueries.deleteByTrip(tripId)
                days.flatMap { it.entries }.forEach { upsertInternal(it) }
            }
        }

    override suspend fun upsertEntry(entry: DiaryEntry) = withContext(Dispatchers.IO) {
        upsertInternal(entry)
    }

    override suspend fun replaceId(oldId: String, entry: DiaryEntry) = withContext(Dispatchers.IO) {
        db.transaction {
            db.diaryFtsQueries.deleteByEntryId(oldId)
            db.diaryEntityQueries.deleteById(oldId)
            upsertInternal(entry)
        }
    }

    override suspend fun removeEntry(tripId: String, entryId: String) {
        withContext(Dispatchers.IO) {
            db.diaryFtsQueries.deleteByEntryId(entryId)
            db.diaryEntityQueries.deleteById(entryId)
        }
    }

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            db.diaryFtsQueries.deleteAll()
            db.diaryEntityQueries.deleteAll()
        }
    }

    private fun upsertInternal(entry: DiaryEntry) {
        db.diaryEntityQueries.upsert(
            id = entry.id,
            trip_id = entry.tripId,
            entry_date = entry.entryDate,
            mood = entry.mood,
            text = entry.text,
            created_at = entry.createdAt,
            updated_at = entry.updatedAt,
        )
        db.diaryFtsQueries.deleteByEntryId(entry.id)
        db.diaryFtsQueries.insert(
            entry_id = entry.id,
            trip_id = entry.tripId,
            text = entry.text,
        )
    }

    private fun DiaryEntity.toDomain(): DiaryEntry =
        DiaryEntry(
            id = id,
            tripId = trip_id,
            entryDate = entry_date,
            mood = mood,
            text = text,
            createdAt = created_at,
            updatedAt = updated_at,
        )

    private fun group(entries: List<DiaryEntry>): List<DiaryDayGroup> =
        entries.groupBy { it.entryDate }
            .entries
            .sortedByDescending { it.key }
            .map { entry -> DiaryDayGroup(date = entry.key, entries = entry.value) }
}
