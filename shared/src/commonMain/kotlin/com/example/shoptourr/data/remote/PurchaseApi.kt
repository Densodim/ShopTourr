package com.example.shoptourr.data.remote

import com.example.shoptourr.data.remote.dto.purchase.CreatePurchaseRequest
import com.example.shoptourr.data.remote.dto.purchase.PurchaseDto
import com.example.shoptourr.data.remote.dto.purchase.UpdatePurchaseRequest
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

class PurchaseApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    private val root get() = baseUrl.trimEnd('/')

    suspend fun createPurchase(
        tripId: String,
        request: CreatePurchaseRequest,
        idempotencyKey: String,
    ): PurchaseDto {
        val response: HttpResponse = client.post("$root/trips/$tripId/purchases") {
            contentType(ContentType.Application.Json)
            header("Idempotency-Key", idempotencyKey)
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            throw mapHttpStatus(response.status)
        }
        return response.body()
    }

    suspend fun fetchPurchase(tripId: String, purchaseId: String): PurchaseDto {
        val response: HttpResponse = client.get("$root/trips/$tripId/purchases/$purchaseId")
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun updatePurchase(
        tripId: String,
        purchaseId: String,
        request: UpdatePurchaseRequest,
    ): PurchaseDto {
        val response: HttpResponse = client.patch("$root/trips/$tripId/purchases/$purchaseId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun deletePurchase(tripId: String, purchaseId: String) {
        val response: HttpResponse = client.delete("$root/trips/$tripId/purchases/$purchaseId")
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
    }
}
