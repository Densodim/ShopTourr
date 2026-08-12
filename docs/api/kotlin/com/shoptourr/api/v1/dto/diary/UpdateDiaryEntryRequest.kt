package com.shoptourr.api.v1.dto.diary

import jakarta.validation.constraints.Size

data class UpdateDiaryEntryRequest(
    @field:Size(min = 1, max = 8)
    val mood: String? = null,

    @field:Size(min = 1, max = 4000)
    val text: String? = null,
)
