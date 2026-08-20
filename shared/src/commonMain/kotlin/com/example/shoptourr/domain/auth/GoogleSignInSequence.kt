package com.example.shoptourr.domain.auth

/**
 * Google Credential Manager steps for an explicit "Sign in with Google" button.
 * Context7 `/websites/developers_google_identity`: returning-user one-tap, then
 * all accounts, then the branded Sign in with Google sheet.
 */
enum class GoogleSignInStep {
    OneTapAuthorized,
    AccountPicker,
    SignInButton,
}

enum class GoogleSignInRetry {
    NoCredential,
    Cancelled,
}

object GoogleSignInSequence {
    val first: GoogleSignInStep = GoogleSignInStep.OneTapAuthorized

    fun next(current: GoogleSignInStep, retry: GoogleSignInRetry): GoogleSignInStep? {
        if (retry != GoogleSignInRetry.NoCredential) return null
        return when (current) {
            GoogleSignInStep.OneTapAuthorized -> GoogleSignInStep.AccountPicker
            GoogleSignInStep.AccountPicker -> GoogleSignInStep.SignInButton
            GoogleSignInStep.SignInButton -> null
        }
    }
}
