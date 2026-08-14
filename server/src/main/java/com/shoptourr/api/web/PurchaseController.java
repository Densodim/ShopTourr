package com.shoptourr.api.web;

import com.shoptourr.api.v1.dto.purchase.PurchaseDtos.CreatePurchaseRequest;
import com.shoptourr.api.v1.dto.purchase.PurchaseDtos.PurchaseDto;
import com.shoptourr.api.v1.dto.purchase.PurchaseDtos.TripPurchasesResponse;
import com.shoptourr.api.v1.dto.purchase.PurchaseDtos.UpdatePurchaseRequest;
import com.shoptourr.application.IdempotencyService;
import com.shoptourr.application.PurchaseService;
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
@RequestMapping(path = "/api/trips/{tripId}/purchases", version = "1")
public class PurchaseController {

    private final PurchaseService purchases;
    private final IdempotencyService idempotency;

    public PurchaseController(PurchaseService purchases, IdempotencyService idempotency) {
        this.purchases = purchases;
        this.idempotency = idempotency;
    }

    @GetMapping
    TripPurchasesResponse list(@PathVariable UUID tripId, Authentication authentication) {
        return purchases.list(CurrentUser.id(authentication), tripId);
    }

    @PostMapping
    ResponseEntity<PurchaseDto> create(
            @PathVariable UUID tripId,
            @Valid @RequestBody CreatePurchaseRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            Authentication authentication
    ) {
        UUID userId = CurrentUser.id(authentication);
        return idempotency.run(
                userId,
                idempotencyKey,
                "POST /api/trips/" + tripId + "/purchases",
                request,
                HttpStatus.CREATED.value(),
                PurchaseDto.class,
                () -> purchases.create(userId, tripId, request)
        );
    }

    @GetMapping("/{purchaseId}")
    PurchaseDto get(
            @PathVariable UUID tripId,
            @PathVariable UUID purchaseId,
            Authentication authentication
    ) {
        return purchases.get(CurrentUser.id(authentication), tripId, purchaseId);
    }

    @PatchMapping("/{purchaseId}")
    PurchaseDto update(
            @PathVariable UUID tripId,
            @PathVariable UUID purchaseId,
            @Valid @RequestBody UpdatePurchaseRequest request,
            Authentication authentication
    ) {
        return purchases.update(CurrentUser.id(authentication), tripId, purchaseId, request);
    }

    @DeleteMapping("/{purchaseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(
            @PathVariable UUID tripId,
            @PathVariable UUID purchaseId,
            Authentication authentication
    ) {
        purchases.delete(CurrentUser.id(authentication), tripId, purchaseId);
    }
}
