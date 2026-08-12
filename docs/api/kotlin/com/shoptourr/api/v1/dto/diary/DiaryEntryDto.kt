package com.shoptourr.api.v1.dto.diary

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class DiaryEntryDto(
    val id: UUID,
    val tripId: UUID,
    val entryDate: LocalDate,
    /** Emoji mood from mock set. */
    val mood: String,
    val text: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
