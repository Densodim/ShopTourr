package com.example.shoptourr.data.remote.dto.analytics

import com.example.shoptourr.analytics.AnalyticsEvent
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsEventDto(
    val id: String,
    val name: String,
    val properties: Map<String, String> = emptyMap(),
    val timestamp: String,
)

@Serializable
data class AnalyticsBatchRequest(
    val events: List<AnalyticsEventDto>,
    val userId: String? = null,
)

fun AnalyticsEvent.toIngestDto(): AnalyticsEventDto = AnalyticsEventDto(
    id = id,
    name = name,
    properties = properties,
    timestamp = Instant.fromEpochMilliseconds(timestampEpochMs).toString(),
)
