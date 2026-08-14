package com.shoptourr.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TaxFreeCalculator {

    private TaxFreeCalculator() {}

    public record Candidate(
            UUID purchaseId,
            String name,
            BigDecimal amount,
            boolean taxRefundEligible
    ) {}

    public record Line(
            UUID purchaseId,
            String name,
            BigDecimal amount,
            BigDecimal estimatedRefund,
            boolean meetsMinimum
    ) {}

    public record Summary(
            CountryCatalog.TaxFreeRules rules,
            List<Line> items,
            int eligibleCount,
            BigDecimal eligibleTotal,
            BigDecimal estimatedRefundTotal,
            BigDecimal remainingToMinimum
    ) {}

    public static Summary summarize(String countryCode, String currency, List<Candidate> purchases) {
        CountryCatalog.TaxFreeRules rules = CountryCatalog.taxFreeRules(countryCode, currency);
        List<Line> items = new ArrayList<>();
        int eligibleCount = 0;
        BigDecimal eligibleTotal = BigDecimal.ZERO.setScale(2);
        BigDecimal refundTotal = BigDecimal.ZERO.setScale(2);
        BigDecimal largestFlagged = BigDecimal.ZERO.setScale(2);
        for (Candidate purchase : purchases) {
            if (!purchase.taxRefundEligible()) {
                continue;
            }
            BigDecimal amount = MoneyMath.scale(purchase.amount());
            boolean meets = rules.available() && amount.compareTo(rules.minimumPurchase()) >= 0;
            BigDecimal refund = meets
                    ? amount.multiply(rules.estimatedRefundRate()).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2);
            items.add(new Line(purchase.purchaseId(), purchase.name(), amount, refund, meets));
            if (amount.compareTo(largestFlagged) > 0) {
                largestFlagged = amount;
            }
            if (meets) {
                eligibleCount++;
                eligibleTotal = eligibleTotal.add(amount);
                refundTotal = refundTotal.add(refund);
            }
        }
        BigDecimal remaining = null;
        if (rules.available() && eligibleCount == 0) {
            remaining = MoneyMath.scale(rules.minimumPurchase().subtract(largestFlagged).max(BigDecimal.ZERO));
        } else if (rules.available()) {
            remaining = BigDecimal.ZERO.setScale(2);
        }
        return new Summary(rules, items, eligibleCount, eligibleTotal, refundTotal, remaining);
    }
}
