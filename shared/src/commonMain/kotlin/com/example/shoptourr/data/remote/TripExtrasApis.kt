package com.example.shoptourr.data.remote

import com.example.shoptourr.data.remote.dto.alert.TripAlertsResponse
import com.example.shoptourr.data.remote.dto.diary.CreateDiaryEntryRequest
import com.example.shoptourr.data.remote.dto.diary.DiaryEntryDto
import com.example.shoptourr.data.remote.dto.diary.TripDiaryResponse
import com.example.shoptourr.data.remote.dto.taxfree.TaxFreeSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class DiaryApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    private val root get() = baseUrl.trimEnd('/')

    suspend fun fetchDiary(tripId: String): TripDiaryResponse {
        val response: HttpResponse = client.get("$root/trips/$tripId/diary")
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun create(tripId: String, request: CreateDiaryEntryRequest): DiaryEntryDto {
        val response: HttpResponse = client.post("$root/trips/$tripId/diary") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun delete(tripId: String, entryId: String) {
        val response: HttpResponse = client.delete("$root/trips/$tripId/diary/$entryId")
        if (!response.status.isSuccess() && response.status != HttpStatusCode.NoContent) {
            throw mapHttpStatus(response.status)
        }
    }
}

class TaxFreeApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun fetchSummary(tripId: String): TaxFreeSummaryDto {
        val response: HttpResponse = client.get("${baseUrl.trimEnd('/')}/trips/$tripId/tax-free")
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }
}

class AlertsApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun fetchAlerts(tripId: String): TripAlertsResponse {
        val response: HttpResponse = client.get("${baseUrl.trimEnd('/')}/trips/$tripId/alerts")
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }
}
