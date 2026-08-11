package com.example.shoptourr.domain.model

enum class ExportFormat { PDF, CSV }

enum class ExportJobStatus { QUEUED, RUNNING, READY, FAILED, EXPIRED }

data class CreateExportDraft(
    val format: ExportFormat,
    val includeTaxFree: Boolean = true,
    val includeDiary: Boolean = false,
)

data class ExportJob(
    val id: String,
    val tripId: String,
    val format: ExportFormat,
    val status: ExportJobStatus,
    val downloadUrl: String? = null,
    val expiresAt: String? = null,
    val errorCode: String? = null,
    val createdAt: String,
    val finishedAt: String? = null,
) {
    val isTerminal: Boolean
        get() = status == ExportJobStatus.READY ||
            status == ExportJobStatus.FAILED ||
            status == ExportJobStatus.EXPIRED

    val isInProgress: Boolean
        get() = status == ExportJobStatus.QUEUED || status == ExportJobStatus.RUNNING
}
