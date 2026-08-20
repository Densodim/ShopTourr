package com.example.shoptourr.data.remote

import com.example.shoptourr.data.remote.dto.analytics.AnalyticsBatchRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class AnalyticsApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    private val root get() = baseUrl.trimEnd('/')

    suspend fun ingest(request: AnalyticsBatchRequest) {
        val response: HttpResponse = client.post("$root/me/analytics-events") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
    }
}
