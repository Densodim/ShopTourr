package com.example.shoptourr.data.remote

import com.example.shoptourr.data.remote.dto.purchase.CreatePurchaseRequest
import com.example.shoptourr.data.remote.dto.purchase.PurchaseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
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
            throw mapHttpStatus(response.status)
        }
        return response.body()
    }
}
