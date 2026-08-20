package com.shoptourr.api.v1.dto.auth

data class SocialLoginRequest(
    val provider: String,
    val idToken: String,
    val nonce: String? = null,
    val displayName: String? = null,
    val deviceName: String? = null,
)
