package com.example.shoptourr.data.remote

import com.example.shoptourr.api.auth.AuthTokensResponse
import com.example.shoptourr.api.auth.LoginRequest
import com.example.shoptourr.api.auth.LogoutRequest
import com.example.shoptourr.api.auth.RefreshTokenRequest
import com.example.shoptourr.api.auth.RegisterRequest
import com.example.shoptourr.domain.error.AppError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class AuthApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    private val root get() = baseUrl.trimEnd('/')

    suspend fun login(request: LoginRequest): AuthTokensResponse =
        post("$root/auth/login", request)

    suspend fun register(request: RegisterRequest): AuthTokensResponse =
        post("$root/auth/register", request)

    suspend fun refresh(request: RefreshTokenRequest): AuthTokensResponse =
        post("$root/auth/refresh", request)

    suspend fun logout(request: LogoutRequest) {
        val response: HttpResponse = client.post("$root/auth/logout") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess() && response.status != HttpStatusCode.NoContent) {
            throw mapStatus(response.status)
        }
    }

    private suspend inline fun <reified T : Any, reified R : Any> post(url: String, body: T): R {
        val response: HttpResponse = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        if (!response.status.isSuccess()) {
            throw mapStatus(response.status)
        }
        return response.body()
    }

    private fun mapStatus(status: HttpStatusCode): AppError = when (status) {
        HttpStatusCode.Unauthorized -> AppError.Unauthorized
        HttpStatusCode.NotFound -> AppError.NotFound
        HttpStatusCode.Conflict -> AppError.Conflict
        else -> AppError.Unknown("HTTP ${status.value}")
    }
}
