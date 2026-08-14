package com.shoptourr.api.web;

import com.shoptourr.api.v1.dto.wishlist.WishlistDtos.CreateWishlistItemRequest;
import com.shoptourr.api.v1.dto.wishlist.WishlistDtos.UpdateWishlistItemRequest;
import com.shoptourr.api.v1.dto.wishlist.WishlistDtos.WishlistItemDto;
import com.shoptourr.api.v1.dto.wishlist.WishlistDtos.WishlistResponse;
import com.shoptourr.application.IdempotencyService;
import com.shoptourr.application.WishlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/wishlist", version = "1")
public class WishlistController {

    private final WishlistService wishlist;
    private final IdempotencyService idempotency;

    public WishlistController(WishlistService wishlist, IdempotencyService idempotency) {
        this.wishlist = wishlist;
        this.idempotency = idempotency;
    }

    @GetMapping
    WishlistResponse list(Authentication authentication) {
        return wishlist.list(CurrentUser.id(authentication));
    }

    @PostMapping
    ResponseEntity<WishlistItemDto> create(
            @Valid @RequestBody CreateWishlistItemRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            Authentication authentication
    ) {
        UUID userId = CurrentUser.id(authentication);
        return idempotency.run(
                userId,
                idempotencyKey,
                "POST /api/wishlist",
                request,
                HttpStatus.CREATED.value(),
                WishlistItemDto.class,
                () -> wishlist.create(userId, request)
        );
    }

    @PatchMapping("/{itemId}")
    WishlistItemDto update(
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateWishlistItemRequest request,
            Authentication authentication
    ) {
        return wishlist.update(CurrentUser.id(authentication), itemId, request);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID itemId, Authentication authentication) {
        wishlist.delete(CurrentUser.id(authentication), itemId);
    }
}
