package com.example.shoptourr.data.remote.dto.wishlist

import com.example.shoptourr.data.remote.dto.common.MoneyDto
import kotlinx.serialization.Serializable

@Serializable
data class WishlistItemDto(
    val id: String,
    val name: String,
    val city: String,
    val targetPrice: MoneyDto,
    val iconEmoji: String? = null,
    val note: String? = null,
    val createdAt: String,
)

@Serializable
data class CreateWishlistItemRequest(
    val name: String,
    val city: String,
    val targetPrice: MoneyDto,
    val iconEmoji: String? = null,
    val note: String? = null,
)

@Serializable
data class UpdateWishlistItemRequest(
    val name: String? = null,
    val city: String? = null,
    val targetPrice: MoneyDto? = null,
    val iconEmoji: String? = null,
    val note: String? = null,
)

@Serializable
data class WishlistResponse(
    val items: List<WishlistItemDto>,
)
