package com.example.shoptourr.data.auth

import android.net.Uri
import androidx.browser.auth.AuthTabIntent
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.shoptourr.di.AppConfig
import com.example.shoptourr.domain.auth.OidcAuthorizeUrl
import com.example.shoptourr.domain.auth.Pkce
import com.example.shoptourr.domain.auth.SocialAuthClient
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.SocialCredentials
import com.example.shoptourr.domain.model.SocialProvider
import com.example.shoptourr.domain.auth.GoogleSignInRetry
import com.example.shoptourr.domain.auth.GoogleSignInSequence
import com.example.shoptourr.domain.auth.GoogleSignInStep
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import android.content.Context

class AndroidSocialAuthClient(
    private val context: Context,
    private val config: AppConfig,
) : SocialAuthClient {

    override suspend fun signIn(provider: SocialProvider, nonce: String): Result<SocialCredentials> =
        runCatching {
            when (provider) {
                SocialProvider.GOOGLE -> google(nonce)
                SocialProvider.APPLE -> apple(nonce)
            }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(mapError(it)) },
        )

    private suspend fun google(nonce: String): SocialCredentials {
        val serverClientId = config.googleWebClientId.trim()
        if (serverClientId.isEmpty()) {
            throw AppError.Validation("Google Sign-In is not configured (GOOGLE_WEB_CLIENT_ID).")
        }
        val activity = AndroidAuthHost.currentActivity()
            ?: throw AppError.Validation("Google Sign-In needs an activity.")
        var step: GoogleSignInStep? = GoogleSignInSequence.first
        var lastMissing: Throwable? = null
        while (step != null) {
            try {
                return requestGoogle(activity, serverClientId, nonce, step)
            } catch (cancelled: GetCredentialCancellationException) {
                throw cancelled
            } catch (missing: NoCredentialException) {
                lastMissing = missing
                step = GoogleSignInSequence.next(step, GoogleSignInRetry.NoCredential)
            }
        }
        throw lastMissing ?: AppError.Validation("No Google account available on this device.")
    }

    private suspend fun requestGoogle(
        activity: android.app.Activity,
        serverClientId: String,
        nonce: String,
        step: GoogleSignInStep,
    ): SocialCredentials {
        val option = when (step) {
            GoogleSignInStep.OneTapAuthorized -> GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setAutoSelectEnabled(true)
                .setServerClientId(serverClientId)
                .setNonce(nonce)
                .build()
            GoogleSignInStep.AccountPicker -> GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setNonce(nonce)
                .build()
            GoogleSignInStep.SignInButton -> GetSignInWithGoogleOption.Builder(serverClientId)
                .setNonce(nonce)
                .build()
        }
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        val response = CredentialManager.create(activity).getCredential(activity, request)
        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val google = GoogleIdTokenCredential.createFrom(credential.data)
            return SocialCredentials(
                provider = SocialProvider.GOOGLE,
                idToken = google.idToken,
                displayName = google.displayName,
            )
        }
        throw AppError.Validation("Google Sign-In returned an unexpected credential.")
    }

    private suspend fun apple(nonce: String): SocialCredentials {
        val servicesId = config.appleServicesId.trim()
        if (servicesId.isEmpty()) {
            throw AppError.Validation("Sign in with Apple is not configured (APPLE_SERVICES_ID).")
        }
        val launcher = AndroidAuthHost.authTabLauncher
            ?: throw AppError.Validation("Sign in with Apple needs Auth Tab.")
        val activity = AndroidAuthHost.currentActivity()
            ?: throw AppError.Validation("Sign in with Apple needs an activity.")
        val state = Pkce.nonce()
        val verifier = Pkce.verifier()
        val url = OidcAuthorizeUrl.apple(
            clientId = servicesId,
            redirectUri = GoogleOidcTokenExchanger.REDIRECT_URI,
            nonce = nonce,
            state = state,
            challenge = Pkce.challengeS256(verifier),
        )
        val redirect = awaitAuthTab(activity, launcher, url) ?: throw AppError.Cancelled
        val idToken = fragmentOrQuery(redirect, "id_token")
            ?: throw AppError.Validation("Apple did not return an identity token.")
        val returnedState = fragmentOrQuery(redirect, "state")
        if (returnedState != state) {
            throw AppError.Validation("Apple Sign-In state mismatch.")
        }
        return SocialCredentials(provider = SocialProvider.APPLE, idToken = idToken)
    }

    private suspend fun awaitAuthTab(
        activity: android.app.Activity,
        launcher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>,
        url: String,
    ): String? = suspendCancellableCoroutine { continuation ->
        pendingAuth = { uri, cancelled ->
            pendingAuth = null
            if (cancelled) {
                continuation.resume(null)
            } else {
                continuation.resume(uri)
            }
        }
        AuthTabIntent.Builder().build().launch(launcher, Uri.parse(url), "voyage")
        continuation.invokeOnCancellation { pendingAuth = null }
        activity.hashCode()
    }

    private fun mapError(throwable: Throwable): Throwable = when (throwable) {
        is AppError -> throwable
        is GetCredentialCancellationException -> AppError.Cancelled
        is NoCredentialException -> AppError.Validation("No Google account available on this device.")
        else -> throwable
    }

    private fun fragmentOrQuery(uri: String, name: String): String? {
        val hash = uri.substringAfter('#', missingDelimiterValue = "")
        val query = uri.substringAfter('?', missingDelimiterValue = "").substringBefore('#')
        val source = if (hash.contains("$name=")) hash else query
        return source.split('&').firstOrNull { it.startsWith("$name=") }
            ?.substringAfter('=')
            ?.takeIf { it.isNotBlank() }
    }

    companion object {
        @Volatile
        var pendingAuth: ((String?, Boolean) -> Unit)? = null

        fun complete(uri: String?) {
            val callback = pendingAuth
            pendingAuth = null
            callback?.invoke(uri, uri.isNullOrBlank())
        }

        fun cancel() {
            val callback = pendingAuth
            pendingAuth = null
            callback?.invoke(null, true)
        }
    }
}
