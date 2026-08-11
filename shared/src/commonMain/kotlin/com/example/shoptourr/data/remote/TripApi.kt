package com.example.shoptourr.data.remote

import com.example.shoptourr.data.remote.dto.common.ExchangeRateDto
import com.example.shoptourr.data.remote.dto.trip.CreateTravelerRequest
import com.example.shoptourr.data.remote.dto.trip.CreateTripRequest
import com.example.shoptourr.data.remote.dto.trip.InviteTravelerRequest
import com.example.shoptourr.data.remote.dto.trip.TravelerDto
import com.example.shoptourr.data.remote.dto.trip.TripDto
import com.example.shoptourr.data.remote.dto.trip.TripInviteDto
import com.example.shoptourr.data.remote.dto.trip.UpdateTripRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
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
    private val root get() = baseUrl.trimEnd('/')

    suspend fun createTrip(request: CreateTripRequest, idempotencyKey: String): TripDto {
        val response: HttpResponse = client.post("$root/trips") {
            contentType(ContentType.Application.Json)
            header("Idempotency-Key", idempotencyKey)
            setBody(request)
        }
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun fetchTrip(tripId: String): TripDto {
        val response: HttpResponse = client.get("$root/trips/$tripId")
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun updateTrip(tripId: String, request: UpdateTripRequest): TripDto {
        val response: HttpResponse = client.patch("$root/trips/$tripId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun deleteTrip(tripId: String) {
        val response: HttpResponse = client.delete("$root/trips/$tripId")
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
    }

    suspend fun addTraveler(tripId: String, request: CreateTravelerRequest): TravelerDto {
        val response: HttpResponse = client.post("$root/trips/$tripId/travelers") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun inviteTraveler(tripId: String, request: InviteTravelerRequest): TripInviteDto {
        val response: HttpResponse = client.post("$root/trips/$tripId/invites") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun refreshExchangeRate(tripId: String): ExchangeRateDto {
        val response: HttpResponse = client.post("$root/trips/$tripId/exchange-rate/refresh")
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }
}
