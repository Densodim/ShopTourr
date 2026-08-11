package com.example.shoptourr.api.diary

data class DiaryEntryDto(
    val id: String,
    val tripId: String,
    val entryDate: String,
    val mood: String,
    val text: String,
    val createdAt: String,
    val updatedAt: String,
)

data class CreateDiaryEntryRequest(
    val entryDate: String? = null,
    val mood: String,
    val text: String,
)

data class UpdateDiaryEntryRequest(
    val mood: String? = null,
    val text: String? = null,
)

data class DiaryDayGroupDto(
    val date: String,
    val labelKey: String? = null,
    val entries: List<DiaryEntryDto>,
)

data class TripDiaryResponse(
    val days: List<DiaryDayGroupDto>,
)
