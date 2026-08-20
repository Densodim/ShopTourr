package com.example.shoptourr.data.remote

import com.example.shoptourr.data.remote.dto.auth.AuthTokensResponse
import com.example.shoptourr.data.remote.dto.auth.ForgotPasswordRequest
import com.example.shoptourr.data.remote.dto.auth.LoginRequest
import com.example.shoptourr.data.remote.dto.auth.LogoutRequest
import com.example.shoptourr.data.remote.dto.auth.RefreshTokenRequest
import com.example.shoptourr.data.remote.dto.auth.RegisterRequest
import com.example.shoptourr.data.remote.dto.auth.ResetPasswordRequest
import com.example.shoptourr.data.remote.dto.auth.SocialLoginRequest
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

    suspend fun loginSocial(request: SocialLoginRequest): AuthTokensResponse =
        post("$root/auth/oauth", request)

    suspend fun register(request: RegisterRequest): AuthTokensResponse =
        post("$root/auth/register", request)

    suspend fun forgotPassword(request: ForgotPasswordRequest) {
        val response: HttpResponse = client.post("$root/auth/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess() && response.status != HttpStatusCode.NoContent) {
            throw mapHttpStatus(response.status)
        }
    }

    suspend fun resetPassword(request: ResetPasswordRequest) {
        val response: HttpResponse = client.post("$root/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess() && response.status != HttpStatusCode.NoContent) {
            throw mapHttpStatus(response.status)
        }
    }

    suspend fun refresh(request: RefreshTokenRequest): AuthTokensResponse =
        post("$root/auth/refresh", request)

    suspend fun logout(request: LogoutRequest) {
        val response: HttpResponse = client.post("$root/auth/logout") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess() && response.status != HttpStatusCode.NoContent) {
            throw mapHttpStatus(response.status)
        }
    }

    private suspend inline fun <reified T : Any, reified R : Any> post(url: String, body: T): R {
        val response: HttpResponse = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        if (!response.status.isSuccess()) {
            throw mapHttpStatus(response.status)
        }
        return response.body()
    }
}
