package com.shoptourr.api.v1.dto.export

import java.time.Instant
import java.util.UUID

data class ExportJobDto(
    val id: UUID,
    val tripId: UUID,
    val format: ExportFormat,
    val status: ExportJobStatus,
    val downloadUrl: String?,
    val expiresAt: Instant?,
    val errorCode: String?,
    val createdAt: Instant,
    val finishedAt: Instant?,
)
