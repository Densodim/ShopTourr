package com.example.shoptourr.domain.model

data class User(
    val id: String,
    val displayName: String,
    val email: String,
    val locale: String,
)

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val accessExpiresIn: Long,
    val refreshExpiresIn: Long,
    val user: User,
)
