package com.shoptourr.api.v1.dto.wishlist;

import com.shoptourr.api.v1.dto.common.CommonDtos.MoneyDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Wishlist tab. */
public final class WishlistDtos {

    private WishlistDtos() {}

    public record WishlistItemDto(
            UUID id,
            String name,
            String city,
            MoneyDto targetPrice,
            String iconEmoji,
            String note,
            Instant createdAt
    ) {}

    public record CreateWishlistItemRequest(
            @NotBlank @Size(min = 1, max = 200) String name,
            @NotBlank @Size(min = 1, max = 120) String city,
            @NotNull @Valid MoneyDto targetPrice,
            @Size(max = 8) String iconEmoji,
            @Size(max = 500) String note
    ) {}

    public record UpdateWishlistItemRequest(
            @Size(min = 1, max = 200) String name,
            @Size(min = 1, max = 120) String city,
            @Valid MoneyDto targetPrice,
            @Size(max = 8) String iconEmoji,
            @Size(max = 500) String note
    ) {}

    public record WishlistResponse(
            List<WishlistItemDto> items
    ) {}
}
