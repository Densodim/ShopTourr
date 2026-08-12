package com.shoptourr.api.v1.dto.media

import java.util.UUID

/** OCR assist (P2) — result attached when PROCESSING→READY. */
data class ReceiptOcrResultDto(
    val mediaId: UUID,
    val suggestedName: String?,
    val suggestedAmount: String?,
    val suggestedPlace: String?,
    val suggestedCategory: String?,
    val confidence: Double,
)
