package com.example.shoptourr.data.local

import com.example.shoptourr.domain.model.ExportJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

interface ExportLocalStore {
    fun observe(tripId: String): Flow<ExportJob?>
    suspend fun save(job: ExportJob)
}

class InMemoryExportLocalStore : ExportLocalStore {
    private val byTrip = MutableStateFlow<Map<String, ExportJob>>(emptyMap())

    override fun observe(tripId: String): Flow<ExportJob?> = byTrip.map { it[tripId] }

    override suspend fun save(job: ExportJob) {
        byTrip.value = byTrip.value + (job.tripId to job)
    }
}
