package com.shoptourr.api.v1.dto.diary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Diary screen. */
public final class DiaryDtos {

    private DiaryDtos() {}

    public record DiaryEntryDto(
            UUID id,
            UUID tripId,
            LocalDate entryDate,
            /** Emoji mood from mock set. */
            String mood,
            String text,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record CreateDiaryEntryRequest(
            /** Defaults to today if null. */
            LocalDate entryDate,
            @NotBlank @Size(min = 1, max = 8) String mood,
            @NotBlank @Size(min = 1, max = 4000) String text
    ) {}

    public record UpdateDiaryEntryRequest(
            @Size(min = 1, max = 8) String mood,
            @Size(min = 1, max = 4000) String text
    ) {}

    public record DiaryDayGroupDto(
            LocalDate date,
            String labelKey,
            List<DiaryEntryDto> entries
    ) {}

    public record TripDiaryResponse(
            List<DiaryDayGroupDto> days
    ) {}
}
