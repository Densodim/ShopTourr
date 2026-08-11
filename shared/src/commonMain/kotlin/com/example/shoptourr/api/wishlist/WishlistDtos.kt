package com.example.shoptourr.api.wishlist

import com.example.shoptourr.api.common.MoneyDto

data class WishlistItemDto(
    val id: String,
    val name: String,
    val city: String,
    val targetPrice: MoneyDto,
    val iconEmoji: String? = null,
    val note: String? = null,
    val createdAt: String,
)

data class CreateWishlistItemRequest(
    val name: String,
    val city: String,
    val targetPrice: MoneyDto,
    val iconEmoji: String? = null,
    val note: String? = null,
)

data class UpdateWishlistItemRequest(
    val name: String? = null,
    val city: String? = null,
    val targetPrice: MoneyDto? = null,
    val iconEmoji: String? = null,
    val note: String? = null,
)

data class WishlistResponse(
    val items: List<WishlistItemDto>,
)
