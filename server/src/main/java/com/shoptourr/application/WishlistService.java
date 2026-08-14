package com.shoptourr.application;

import com.shoptourr.api.v1.dto.wishlist.WishlistDtos.CreateWishlistItemRequest;
import com.shoptourr.api.v1.dto.wishlist.WishlistDtos.UpdateWishlistItemRequest;
import com.shoptourr.api.v1.dto.wishlist.WishlistDtos.WishlistItemDto;
import com.shoptourr.api.v1.dto.wishlist.WishlistDtos.WishlistResponse;
import com.shoptourr.domain.ApiException;
import com.shoptourr.infra.persistence.WishlistItemEntity;
import com.shoptourr.infra.persistence.WishlistItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class WishlistService {

    private final WishlistItemRepository items;
    private final TripMapper mapper;

    public WishlistService(WishlistItemRepository items, TripMapper mapper) {
        this.items = items;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public WishlistResponse list(UUID userId) {
        return new WishlistResponse(items.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toDto).toList());
    }

    @Transactional
    public WishlistItemDto create(UUID userId, CreateWishlistItemRequest request) {
        WishlistItemEntity entity = new WishlistItemEntity();
        entity.setUserId(userId);
        entity.setName(request.name());
        entity.setCity(request.city());
        entity.setTargetAmount(request.targetPrice().amount());
        entity.setCurrency(request.targetPrice().currency());
        entity.setIconEmoji(request.iconEmoji());
        entity.setNote(request.note());
        items.save(entity);
        return toDto(entity);
    }

    @Transactional
    public WishlistItemDto update(UUID userId, UUID itemId, UpdateWishlistItemRequest request) {
        WishlistItemEntity entity = items.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> ApiException.notFound("wishlist item not found"));
        if (request.name() != null) entity.setName(request.name());
        if (request.city() != null) entity.setCity(request.city());
        if (request.targetPrice() != null) {
            entity.setTargetAmount(request.targetPrice().amount());
            entity.setCurrency(request.targetPrice().currency());
        }
        if (request.iconEmoji() != null) entity.setIconEmoji(request.iconEmoji());
        if (request.note() != null) entity.setNote(request.note());
        return toDto(entity);
    }

    @Transactional
    public void delete(UUID userId, UUID itemId) {
        WishlistItemEntity entity = items.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> ApiException.notFound("wishlist item not found"));
        items.delete(entity);
    }

    private WishlistItemDto toDto(WishlistItemEntity entity) {
        return new WishlistItemDto(
                entity.getId(),
                entity.getName(),
                entity.getCity(),
                mapper.money(entity.getTargetAmount(), entity.getCurrency()),
                entity.getIconEmoji(),
                entity.getNote(),
                entity.getCreatedAt()
        );
    }
}
