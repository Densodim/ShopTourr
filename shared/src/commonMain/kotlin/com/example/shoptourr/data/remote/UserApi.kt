package com.example.shoptourr.data.remote

import com.example.shoptourr.data.remote.dto.user.ActivatePremiumRequest
import com.example.shoptourr.data.remote.dto.user.UpdatePreferencesRequest
import com.example.shoptourr.data.remote.dto.user.UpdateProfileRequest
import com.example.shoptourr.data.remote.dto.user.UserDto
import com.example.shoptourr.data.remote.dto.user.UserPreferencesDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class UserApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    private val root get() = baseUrl.trimEnd('/')

    suspend fun fetchMe(): UserDto {
        val response: HttpResponse = client.get("$root/me")
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun updateMe(request: UpdateProfileRequest): UserDto {
        val response: HttpResponse = client.patch("$root/me") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun fetchPreferences(): UserPreferencesDto {
        val response: HttpResponse = client.get("$root/me/preferences")
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun updatePreferences(request: UpdatePreferencesRequest): UserPreferencesDto {
        val response: HttpResponse = client.patch("$root/me/preferences") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }

    suspend fun activatePremium(request: ActivatePremiumRequest): UserDto {
        val response: HttpResponse = client.post("$root/me/premium/activate") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) throw mapHttpStatus(response.status)
        return response.body()
    }
}
