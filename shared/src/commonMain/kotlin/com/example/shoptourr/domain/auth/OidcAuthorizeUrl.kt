package com.example.shoptourr.domain.auth

import io.ktor.http.encodeURLParameter

object OidcAuthorizeUrl {
    fun google(
        clientId: String,
        redirectUri: String,
        nonce: String,
        state: String,
        challenge: String,
    ): String = build(
        GoogleOidcEndpoints.AUTHORIZE,
        "client_id" to clientId,
        "redirect_uri" to redirectUri,
        "response_type" to "code",
        "scope" to "openid email profile",
        "code_challenge" to challenge,
        "code_challenge_method" to "S256",
        "state" to state,
        "nonce" to nonce,
    )

    fun apple(
        clientId: String,
        redirectUri: String,
        nonce: String,
        state: String,
        challenge: String,
    ): String = build(
        GoogleOidcEndpoints.APPLE_AUTHORIZE,
        "client_id" to clientId,
        "redirect_uri" to redirectUri,
        "response_type" to "code id_token",
        "response_mode" to "fragment",
        "scope" to "name email",
        "code_challenge" to challenge,
        "code_challenge_method" to "S256",
        "state" to state,
        "nonce" to nonce,
    )

    private fun build(endpoint: String, vararg params: Pair<String, String>): String =
        params.joinToString("&", prefix = "$endpoint?") { (key, value) ->
            "${key.encodeURLParameter()}=${value.encodeURLParameter()}"
        }
}

private object GoogleOidcEndpoints {
    const val AUTHORIZE = "https://accounts.google.com/o/oauth2/v2/auth"
    const val APPLE_AUTHORIZE = "https://appleid.apple.com/auth/authorize"
}
