package com.shoptourr.api.v1.dto.export

import jakarta.validation.constraints.NotNull

data class CreateExportRequest(
    @field:NotNull
    val format: ExportFormat,

    /** Include Tax Free worksheet section (PDF). */
    val includeTaxFree: Boolean = false,

    val includeDiary: Boolean = false,
)
