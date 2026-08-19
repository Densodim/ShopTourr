package com.example.shoptourr.data.remote

import com.example.shoptourr.domain.session.AuthTokenCache
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider

/** Context7 /ktor.io: BearerAuthProvider.clearToken() drops the in-memory credential cache. */
class KtorAuthTokenCache(
    private val client: HttpClient,
) : AuthTokenCache {
    override fun clear() {
        client.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
