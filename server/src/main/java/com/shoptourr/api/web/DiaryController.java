package com.shoptourr.api.web;

import com.shoptourr.api.v1.dto.diary.DiaryDtos.CreateDiaryEntryRequest;
import com.shoptourr.api.v1.dto.diary.DiaryDtos.DiaryEntryDto;
import com.shoptourr.api.v1.dto.diary.DiaryDtos.TripDiaryResponse;
import com.shoptourr.api.v1.dto.diary.DiaryDtos.UpdateDiaryEntryRequest;
import com.shoptourr.application.DiaryService;
import com.shoptourr.application.IdempotencyService;
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
@RequestMapping(path = "/api/trips/{tripId}/diary", version = "1")
public class DiaryController {

    private final DiaryService diary;
    private final IdempotencyService idempotency;

    public DiaryController(DiaryService diary, IdempotencyService idempotency) {
        this.diary = diary;
        this.idempotency = idempotency;
    }

    @GetMapping
    TripDiaryResponse list(@PathVariable UUID tripId, Authentication authentication) {
        return diary.list(CurrentUser.id(authentication), tripId);
    }

    @PostMapping
    ResponseEntity<DiaryEntryDto> create(
            @PathVariable UUID tripId,
            @Valid @RequestBody CreateDiaryEntryRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            Authentication authentication
    ) {
        UUID userId = CurrentUser.id(authentication);
        return idempotency.run(
                userId,
                idempotencyKey,
                "POST /api/trips/" + tripId + "/diary",
                request,
                HttpStatus.CREATED.value(),
                DiaryEntryDto.class,
                () -> diary.create(userId, tripId, request)
        );
    }

    @PatchMapping("/{entryId}")
    DiaryEntryDto update(
            @PathVariable UUID tripId,
            @PathVariable UUID entryId,
            @Valid @RequestBody UpdateDiaryEntryRequest request,
            Authentication authentication
    ) {
        return diary.update(CurrentUser.id(authentication), tripId, entryId, request);
    }

    @DeleteMapping("/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(
            @PathVariable UUID tripId,
            @PathVariable UUID entryId,
            Authentication authentication
    ) {
        diary.delete(CurrentUser.id(authentication), tripId, entryId);
    }
}
