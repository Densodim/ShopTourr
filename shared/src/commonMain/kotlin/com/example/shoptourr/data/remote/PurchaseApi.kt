package com.example.shoptourr.data.remote

import com.example.shoptourr.api.purchase.CreatePurchaseRequest
import com.example.shoptourr.api.purchase.PurchaseDto
import com.example.shoptourr.domain.error.AppError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class PurchaseApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun createPurchase(
        tripId: String,
        request: CreatePurchaseRequest,
        idempotencyKey: String,
    ): PurchaseDto {
        val response: HttpResponse = client.post("${baseUrl.trimEnd('/')}/trips/$tripId/purchases") {
            contentType(ContentType.Application.Json)
            header("Idempotency-Key", idempotencyKey)
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            throw when (response.status) {
                HttpStatusCode.Unauthorized -> AppError.Unauthorized
                HttpStatusCode.Conflict -> AppError.Conflict
                HttpStatusCode.NotFound -> AppError.NotFound
                else -> AppError.Unknown("HTTP ${response.status.value}")
            }
        }
        return response.body()
    }
}
