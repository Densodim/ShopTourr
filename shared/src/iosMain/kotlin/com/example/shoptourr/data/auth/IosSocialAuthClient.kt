package com.example.shoptourr.data.auth

import com.example.shoptourr.di.AppConfig
import com.example.shoptourr.domain.auth.OidcAuthorizeUrl
import com.example.shoptourr.domain.auth.Pkce
import com.example.shoptourr.domain.auth.SocialAuthClient
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.SocialCredentials
import com.example.shoptourr.domain.model.SocialProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun interface IosAppleSignIn {
    fun start(nonce: String, callback: (idToken: String?, displayName: String?, error: String?) -> Unit)
}

fun interface IosBrowserAuth {
    fun start(url: String, callbackScheme: String, callback: (redirect: String?, error: String?) -> Unit)
}

object IosSocialAuthBridge {
    var appleSignIn: IosAppleSignIn? = null
    var browserAuth: IosBrowserAuth? = null
}

fun registerIosSocialAuth(
    apple: IosAppleSignIn,
    browser: IosBrowserAuth,
) {
    IosSocialAuthBridge.appleSignIn = apple
    IosSocialAuthBridge.browserAuth = browser
}

class IosSocialAuthClient(
    private val config: AppConfig,
    private val googleTokens: GoogleOidcTokenExchanger,
) : SocialAuthClient {

    override suspend fun signIn(provider: SocialProvider, nonce: String): Result<SocialCredentials> =
        runCatching {
            when (provider) {
                SocialProvider.APPLE -> apple(nonce)
                SocialProvider.GOOGLE -> google(nonce)
            }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(if (it is AppError) it else AppError.Unknown(it)) },
        )

    private suspend fun apple(nonce: String): SocialCredentials {
        val apple = IosSocialAuthBridge.appleSignIn
            ?: throw AppError.Validation("Sign in with Apple is not wired.")
        return suspendCancellableCoroutine { continuation ->
            apple.start(nonce) { idToken, displayName, error ->
                when {
                    error == "cancelled" -> continuation.resumeWithException(AppError.Cancelled)
                    !error.isNullOrBlank() -> continuation.resumeWithException(AppError.Validation(error))
                    idToken.isNullOrBlank() -> continuation.resumeWithException(
                        AppError.Validation("Apple did not return an identity token."),
                    )
                    else -> continuation.resume(
                        SocialCredentials(SocialProvider.APPLE, idToken, displayName),
                    )
                }
            }
        }
    }

    private suspend fun google(nonce: String): SocialCredentials {
        val clientId = config.googleIosClientId.trim()
        if (clientId.isEmpty()) {
            throw AppError.Validation("Google Sign-In is not configured (GOOGLE_IOS_CLIENT_ID).")
        }
        val browser = IosSocialAuthBridge.browserAuth
            ?: throw AppError.Validation("Google Sign-In browser session is not wired.")
        val state = Pkce.nonce()
        val verifier = Pkce.verifier()
        val redirectUri = GoogleOidcTokenExchanger.REDIRECT_URI
        val url = OidcAuthorizeUrl.google(
            clientId = clientId,
            redirectUri = redirectUri,
            nonce = nonce,
            state = state,
            challenge = Pkce.challengeS256(verifier),
        )
        val redirectUrl = suspendCancellableCoroutine { continuation ->
            browser.start(url, "voyage") { result, error ->
                when {
                    error == "cancelled" -> continuation.resumeWithException(AppError.Cancelled)
                    !error.isNullOrBlank() -> continuation.resumeWithException(AppError.Validation(error))
                    result.isNullOrBlank() -> continuation.resumeWithException(AppError.Cancelled)
                    else -> continuation.resume(result)
                }
            }
        }
        val code = queryParam(redirectUrl, "code")
            ?: throw AppError.Validation("Google did not return an authorization code.")
        val returnedState = queryParam(redirectUrl, "state")
        if (returnedState != state) {
            throw AppError.Validation("Google Sign-In state mismatch.")
        }
        val idToken = googleTokens.exchange(clientId, redirectUri, code, verifier)
        return SocialCredentials(SocialProvider.GOOGLE, idToken)
    }

    private fun queryParam(uri: String, name: String): String? {
        val query = uri.substringAfter('?', missingDelimiterValue = "").substringBefore('#')
        return query.split('&').firstOrNull { it.startsWith("$name=") }
            ?.substringAfter('=')
            ?.takeIf { it.isNotBlank() }
    }
}
