package com.example.shoptourr.data.remote

import com.example.shoptourr.api.home.HomeResponse
import com.example.shoptourr.domain.error.AppError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

class HomeApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun fetchHome(): HomeResponse {
        val response: HttpResponse = client.get("${baseUrl.trimEnd('/')}/home")
        if (!response.status.isSuccess()) {
            throw when (response.status) {
                HttpStatusCode.Unauthorized -> AppError.Unauthorized
                HttpStatusCode.NotFound -> AppError.NotFound
                else -> AppError.Unknown("HTTP ${response.status.value}")
            }
        }
        return response.body()
    }
}
