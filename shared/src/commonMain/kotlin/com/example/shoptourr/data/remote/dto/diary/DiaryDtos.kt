package com.example.shoptourr.data.remote.dto.diary

import kotlinx.serialization.Serializable

@Serializable
data class DiaryEntryDto(
    val id: String,
    val tripId: String,
    val entryDate: String,
    val mood: String,
    val text: String,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class CreateDiaryEntryRequest(
    val entryDate: String? = null,
    val mood: String,
    val text: String,
)

@Serializable
data class UpdateDiaryEntryRequest(
    val mood: String? = null,
    val text: String? = null,
)

@Serializable
data class DiaryDayGroupDto(
    val date: String,
    val labelKey: String? = null,
    val entries: List<DiaryEntryDto>,
)

@Serializable
data class TripDiaryResponse(
    val days: List<DiaryDayGroupDto>,
)
