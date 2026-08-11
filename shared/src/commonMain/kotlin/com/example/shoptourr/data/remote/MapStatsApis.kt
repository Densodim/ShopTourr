package com.example.shoptourr.data.remote

import com.example.shoptourr.data.remote.dto.map.TripRouteDto
import com.example.shoptourr.data.remote.dto.stats.TripStatsDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class RouteApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun fetchRoute(tripId: String): TripRouteDto {
        val response: HttpResponse = client.get("${baseUrl.trimEnd('/')}/trips/$tripId/route")
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }
}

class StatsApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun fetchStats(tripId: String): TripStatsDto {
        val response: HttpResponse = client.get("${baseUrl.trimEnd('/')}/trips/$tripId/stats")
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }
}
