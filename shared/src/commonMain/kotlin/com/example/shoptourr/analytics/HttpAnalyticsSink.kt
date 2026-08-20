package com.example.shoptourr.analytics

import com.example.shoptourr.data.remote.AnalyticsApi
import com.example.shoptourr.data.remote.dto.analytics.AnalyticsBatchRequest
import com.example.shoptourr.data.remote.dto.analytics.toIngestDto
import com.example.shoptourr.data.remote.mapHttpAppError

/**
 * First-party ingest sink (ch.10 metrics). Queued events leave the device via
 * `POST /me/analytics-events`; 5xx keeps the SQL queue for the next flush.
 */
class HttpAnalyticsSink(
    private val api: AnalyticsApi,
) : AnalyticsSink {
    private var userId: String? = null

    override suspend fun send(events: List<AnalyticsEvent>): Result<Unit> {
        if (events.isEmpty()) return Result.success(Unit)
        return runCatching {
            api.ingest(
                AnalyticsBatchRequest(
                    events = events.map { it.toIngestDto() },
                    userId = userId,
                ),
            )
        }.mapHttpAppError()
    }

    override fun identify(userId: String?) {
        this.userId = userId
    }
}
