package com.example.shoptourr.domain.model

data class WishlistItem(
    val id: String,
    val name: String,
    val city: String,
    val targetPrice: Money,
    val iconEmoji: String? = null,
    val note: String? = null,
    val createdAt: String,
)

data class CreateWishlistDraft(
    val name: String,
    val city: String,
    val targetPrice: Money,
    val iconEmoji: String? = null,
    val note: String? = null,
)
