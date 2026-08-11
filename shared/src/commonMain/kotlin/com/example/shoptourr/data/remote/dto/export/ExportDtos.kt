package com.example.shoptourr.data.remote.dto.export

enum class ExportFormat { PDF, CSV }

enum class ExportJobStatus { QUEUED, RUNNING, READY, FAILED, EXPIRED }

data class CreateExportRequest(
    val format: ExportFormat,
    val includeTaxFree: Boolean = true,
    val includeDiary: Boolean = false,
)

data class ExportJobDto(
    val id: String,
    val tripId: String,
    val format: ExportFormat,
    val status: ExportJobStatus,
    val downloadUrl: String? = null,
    val expiresAt: String? = null,
    val errorCode: String? = null,
    val createdAt: String,
    val finishedAt: String? = null,
)
