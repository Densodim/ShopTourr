package com.example.shoptourr.data.remote

import com.example.shoptourr.data.remote.dto.trip.CreateTripRequest
import com.example.shoptourr.data.remote.dto.trip.TripDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class TripApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun createTrip(request: CreateTripRequest, idempotencyKey: String): TripDto {
        val response: HttpResponse = client.post("${baseUrl.trimEnd('/')}/trips") {
            contentType(ContentType.Application.Json)
            header("Idempotency-Key", idempotencyKey)
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            throw mapHttpStatus(response.status)
        }
        return response.body()
    }
}
