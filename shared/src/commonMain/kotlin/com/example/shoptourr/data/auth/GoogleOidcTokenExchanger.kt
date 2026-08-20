package com.example.shoptourr.data.auth

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GoogleOidcTokenExchanger(
    private val client: HttpClient,
) {
    suspend fun exchange(
        clientId: String,
        redirectUri: String,
        code: String,
        verifier: String,
    ): String {
        val text = client.submitForm(
            url = TOKEN_URL,
            formParameters = Parameters.build {
                append("code", code)
                append("client_id", clientId)
                append("redirect_uri", redirectUri)
                append("grant_type", "authorization_code")
                append("code_verifier", verifier)
            },
        ).bodyAsText()
        val response = json.decodeFromString(GoogleTokenResponse.serializer(), text)
        return response.idToken?.takeIf { it.isNotBlank() }
            ?: error(response.error ?: "Google token exchange failed")
    }

    @Serializable
    private data class GoogleTokenResponse(
        @SerialName("id_token") val idToken: String? = null,
        val error: String? = null,
    )

    companion object {
        const val TOKEN_URL = "https://oauth2.googleapis.com/token"
        const val REDIRECT_URI = "voyage://oauth"
        private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
    }
}
