package com.example.shoptourr.data.remote

import com.example.shoptourr.data.remote.dto.wishlist.CreateWishlistItemRequest
import com.example.shoptourr.data.remote.dto.wishlist.WishlistItemDto
import com.example.shoptourr.data.remote.dto.wishlist.WishlistResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class WishlistApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    private val root get() = baseUrl.trimEnd('/')

    suspend fun fetchWishlist(): WishlistResponse {
        val response: HttpResponse = client.get("$root/wishlist")
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun create(request: CreateWishlistItemRequest, idempotencyKey: String): WishlistItemDto {
        val response: HttpResponse = client.post("$root/wishlist") {
            contentType(ContentType.Application.Json)
            header("Idempotency-Key", idempotencyKey)
            setBody(request)
        }
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun delete(id: String) {
        val response: HttpResponse = client.delete("$root/wishlist/$id")
        if (!response.status.isSuccess() && response.status != HttpStatusCode.NoContent) {
            throw mapHttpStatus(response.status)
        }
    }
}
