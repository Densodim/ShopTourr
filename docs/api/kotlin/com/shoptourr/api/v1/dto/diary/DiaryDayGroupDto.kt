package com.shoptourr.api.v1.dto.diary

import java.time.LocalDate

data class DiaryDayGroupDto(
    val date: LocalDate,
    val labelKey: String?,
    val entries: List<DiaryEntryDto>,
)
