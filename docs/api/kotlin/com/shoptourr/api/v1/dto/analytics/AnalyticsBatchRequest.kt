package com.shoptourr.api.v1.dto.analytics

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

/**
 * POST /me/analytics-events — client product events after consent.
 * First-party ingest (no PostHog/Firebase required for v1).
 */
data class AnalyticsBatchRequest(
    @field:NotEmpty
    @field:Valid
    val events: List<AnalyticsEventDto>,

    @field:Size(max = 64)
    val userId: String? = null,
)

data class AnalyticsEventDto(
    @field:NotBlank
    @field:Size(max = 64)
    val id: String,

    @field:NotBlank
    @field:Size(max = 120)
    val name: String,

    val properties: Map<String, String> = emptyMap(),

    @field:NotBlank
    val timestamp: String,
)
