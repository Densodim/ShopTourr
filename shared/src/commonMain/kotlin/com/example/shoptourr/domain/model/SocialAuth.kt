package com.example.shoptourr.domain.model

enum class SocialProvider {
    GOOGLE,
    APPLE,
}

data class SocialCredentials(
    val provider: SocialProvider,
    val idToken: String,
    val displayName: String? = null,
)
