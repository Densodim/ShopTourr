package com.shoptourr.api.v1.dto.taxfree;

import com.shoptourr.api.v1.dto.common.CommonDtos.MoneyDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Tax Free screen — eligibility against country thresholds (mock TAXFREE_INFO).
 */
public final class TaxFreeDtos {

    private TaxFreeDtos() {}

    public record TaxFreeRulesDto(
            String currency,
            MoneyDto minimumPurchase,
            /** Fraction e.g. 0.13 = 13% estimated refund. */
            BigDecimal estimatedRefundRate,
            String regionLabel
    ) {}

    public record TaxFreeEligibleItemDto(
            UUID purchaseId,
            String name,
            MoneyDto amount,
            MoneyDto estimatedRefund,
            boolean meetsMinimum
    ) {}

    public record TaxFreeSummaryDto(
            UUID tripId,
            TaxFreeRulesDto rules,
            int eligibleCount,
            MoneyDto eligibleTotal,
            MoneyDto estimatedRefundTotal,
            /** How much more spend needed to unlock next eligible item / form, if any. */
            MoneyDto remainingToMinimum,
            List<TaxFreeEligibleItemDto> items
    ) {}
}
