package com.shoptourr.application;

import com.shoptourr.api.v1.dto.diary.DiaryDtos.CreateDiaryEntryRequest;
import com.shoptourr.api.v1.dto.diary.DiaryDtos.DiaryDayGroupDto;
import com.shoptourr.api.v1.dto.diary.DiaryDtos.DiaryEntryDto;
import com.shoptourr.api.v1.dto.diary.DiaryDtos.TripDiaryResponse;
import com.shoptourr.api.v1.dto.diary.DiaryDtos.UpdateDiaryEntryRequest;
import com.shoptourr.domain.ApiException;
import com.shoptourr.infra.persistence.DiaryEntryEntity;
import com.shoptourr.infra.persistence.DiaryEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DiaryService {

    private final TripService trips;
    private final DiaryEntryRepository entries;
    private final Clock clock;

    public DiaryService(TripService trips, DiaryEntryRepository entries, Clock clock) {
        this.trips = trips;
        this.entries = entries;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public TripDiaryResponse list(UUID userId, UUID tripId) {
        trips.require(userId, tripId);
        LocalDate today = LocalDate.now(clock);
        LinkedHashMap<LocalDate, List<DiaryEntryDto>> grouped = new LinkedHashMap<>();
        for (DiaryEntryEntity entry : entries.findByTripIdOrderByEntryDateDescCreatedAtDesc(tripId)) {
            grouped.computeIfAbsent(entry.getEntryDate(), key -> new ArrayList<>()).add(toDto(entry));
        }
        List<DiaryDayGroupDto> days = grouped.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, List<DiaryEntryDto>>comparingByKey(Comparator.reverseOrder()))
                .map(entry -> new DiaryDayGroupDto(entry.getKey(), labelKey(entry.getKey(), today), entry.getValue()))
                .toList();
        return new TripDiaryResponse(days);
    }

    @Transactional
    public DiaryEntryDto create(UUID userId, UUID tripId, CreateDiaryEntryRequest request) {
        trips.require(userId, tripId);
        DiaryEntryEntity entity = new DiaryEntryEntity();
        entity.setTripId(tripId);
        entity.setEntryDate(request.entryDate() == null ? LocalDate.now(clock) : request.entryDate());
        entity.setMood(request.mood());
        entity.setText(request.text());
        entries.save(entity);
        return toDto(entity);
    }

    @Transactional
    public DiaryEntryDto update(UUID userId, UUID tripId, UUID entryId, UpdateDiaryEntryRequest request) {
        trips.require(userId, tripId);
        DiaryEntryEntity entity = entries.findByIdAndTripId(entryId, tripId)
                .orElseThrow(() -> ApiException.notFound("diary entry not found"));
        if (request.mood() != null) entity.setMood(request.mood());
        if (request.text() != null) entity.setText(request.text());
        return toDto(entity);
    }

    @Transactional
    public void delete(UUID userId, UUID tripId, UUID entryId) {
        trips.require(userId, tripId);
        DiaryEntryEntity entity = entries.findByIdAndTripId(entryId, tripId)
                .orElseThrow(() -> ApiException.notFound("diary entry not found"));
        entries.delete(entity);
    }

    private static DiaryEntryDto toDto(DiaryEntryEntity entity) {
        return new DiaryEntryDto(
                entity.getId(),
                entity.getTripId(),
                entity.getEntryDate(),
                entity.getMood(),
                entity.getText(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static String labelKey(LocalDate date, LocalDate today) {
        if (date.equals(today)) return "TODAY";
        if (date.equals(today.minusDays(1))) return "YESTERDAY";
        return null;
    }
}
