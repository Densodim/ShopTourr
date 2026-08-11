package com.shoptourr.api.v1.dto.stats;

import com.shoptourr.api.v1.dto.common.CommonDtos.MoneyDto;
import com.shoptourr.api.v1.dto.purchase.PurchaseDtos.PurchaseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Stats screen — donut by category + by day. */
public final class StatsDtos {

    private StatsDtos() {}

    public record CategorySpendDto(
            PurchaseCategory category,
            MoneyDto amount,
            /** 0..1 share of total. */
            BigDecimal share,
            int purchaseCount
    ) {}

    public record DailySpendDto(
            LocalDate date,
            MoneyDto amount,
            int purchaseCount
    ) {}

    public record TripStatsDto(
            UUID tripId,
            MoneyDto totalSpent,
            MoneyDto budget,
            MoneyDto dailyAverage,
            MoneyDto remaining,
            boolean onBudget,
            /** Positive days early/late vs linear burn; null if no dates. */
            Integer paceDeltaDays,
            PurchaseCategory topCategory,
            List<CategorySpendDto> byCategory,
            List<DailySpendDto> byDay
    ) {}
}
