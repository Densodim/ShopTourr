package com.example.shoptourr.data.remote

import com.example.shoptourr.data.remote.dto.export.CreateExportRequest
import com.example.shoptourr.data.remote.dto.export.ExportJobDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class ExportApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    private val root get() = baseUrl.trimEnd('/')

    suspend fun create(tripId: String, request: CreateExportRequest): ExportJobDto {
        val response: HttpResponse = client.post("$root/trips/$tripId/exports") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess() && response.status != HttpStatusCode.Accepted) {
            throw mapHttpStatus(response.status)
        }
        return response.body()
    }

    suspend fun fetchJob(exportId: String): ExportJobDto {
        val response: HttpResponse = client.get("$root/exports/$exportId")
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }
}
