package com.example.shoptourr.data.remote

import com.example.shoptourr.data.remote.dto.home.HomeResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class HomeApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun fetchHome(): HomeResponse {
        val response: HttpResponse = client.get("${baseUrl.trimEnd('/')}/home")
        if (!response.status.isSuccess()) {
            throw mapHttpStatus(response.status)
        }
        return response.body()
    }
}
