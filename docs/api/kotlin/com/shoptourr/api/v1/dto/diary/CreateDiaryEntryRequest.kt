package com.shoptourr.api.v1.dto.diary

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class CreateDiaryEntryRequest(
    /** Defaults to today if null. */
    val entryDate: LocalDate? = null,

    @field:NotBlank
    @field:Size(min = 1, max = 8)
    val mood: String,

    @field:NotBlank
    @field:Size(min = 1, max = 4000)
    val text: String,
)
