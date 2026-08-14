package com.shoptourr.application;

import com.shoptourr.api.v1.dto.purchase.PurchaseDtos.PurchaseCategory;
import com.shoptourr.api.v1.dto.stats.StatsDtos.CategorySpendDto;
import com.shoptourr.api.v1.dto.stats.StatsDtos.DailySpendDto;
import com.shoptourr.api.v1.dto.stats.StatsDtos.TripStatsDto;
import com.shoptourr.domain.MoneyMath;
import com.shoptourr.domain.TripCalendar;
import com.shoptourr.infra.persistence.PurchaseEntity;
import com.shoptourr.infra.persistence.PurchaseRepository;
import com.shoptourr.infra.persistence.TripEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class StatsService {

    private final TripService trips;
    private final PurchaseRepository purchases;
    private final TripMapper mapper;

    public StatsService(TripService trips, PurchaseRepository purchases, TripMapper mapper) {
        this.trips = trips;
        this.purchases = purchases;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public TripStatsDto stats(UUID userId, UUID tripId) {
        TripEntity trip = trips.require(userId, tripId);
        List<PurchaseEntity> items = purchases.findByTripIdAndDeletedAtIsNullOrderByPurchaseDateDescCreatedAtDesc(tripId);
        String currency = trip.getBudgetCurrency();
        BigDecimal total = items.stream().map(PurchaseEntity::getGrossAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        int days = Math.max(TripCalendar.dayCount(trip.getStartDate(), trip.getEndDate()), 1);
        BigDecimal dailyAverage = total.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);
        BigDecimal remaining = trip.getBudgetAmount().subtract(total);

        EnumMap<PurchaseCategory, BigDecimal> byCat = new EnumMap<>(PurchaseCategory.class);
        EnumMap<PurchaseCategory, Integer> catCount = new EnumMap<>(PurchaseCategory.class);
        LinkedHashMap<LocalDate, BigDecimal> byDay = new LinkedHashMap<>();
        LinkedHashMap<LocalDate, Integer> dayCount = new LinkedHashMap<>();
        for (PurchaseEntity item : items) {
            byCat.merge(item.getCategory(), item.getGrossAmount(), BigDecimal::add);
            catCount.merge(item.getCategory(), 1, Integer::sum);
            byDay.merge(item.getPurchaseDate(), item.getGrossAmount(), BigDecimal::add);
            dayCount.merge(item.getPurchaseDate(), 1, Integer::sum);
        }
        PurchaseCategory top = byCat.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        List<CategorySpendDto> categories = new ArrayList<>();
        for (PurchaseCategory category : PurchaseCategory.values()) {
            BigDecimal amount = byCat.getOrDefault(category, BigDecimal.ZERO);
            if (amount.signum() == 0) {
                continue;
            }
            BigDecimal share = total.signum() == 0
                    ? BigDecimal.ZERO
                    : amount.divide(total, 4, RoundingMode.HALF_UP);
            categories.add(new CategorySpendDto(
                    category,
                    mapper.money(amount, currency),
                    share,
                    catCount.getOrDefault(category, 0)
            ));
        }
        categories.sort(Comparator.comparing((CategorySpendDto dto) -> dto.amount().amount()).reversed());
        List<DailySpendDto> daily = byDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DailySpendDto(
                        entry.getKey(),
                        mapper.money(entry.getValue(), currency),
                        dayCount.getOrDefault(entry.getKey(), 0)
                ))
                .toList();
        return new TripStatsDto(
                trip.getId(),
                mapper.money(total, currency),
                mapper.money(trip.getBudgetAmount(), currency),
                mapper.money(dailyAverage, currency),
                mapper.money(remaining, currency),
                remaining.signum() >= 0,
                null,
                top,
                categories,
                daily
        );
    }
}
