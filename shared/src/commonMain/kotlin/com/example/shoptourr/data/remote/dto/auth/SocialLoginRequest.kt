package com.example.shoptourr.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class SocialLoginRequest(
    val provider: String,
    val idToken: String,
    val nonce: String? = null,
    val displayName: String? = null,
    val deviceName: String? = null,
)
