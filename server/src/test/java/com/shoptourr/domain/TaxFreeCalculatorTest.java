package com.shoptourr.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TaxFreeCalculatorTest {

    @Test
    void portugalFlagsPurchasesAboveMinimum() {
        UUID above = UUID.randomUUID();
        UUID below = UUID.randomUUID();
        TaxFreeCalculator.Summary summary = TaxFreeCalculator.summarize(
                "PT",
                "EUR",
                List.of(
                        new TaxFreeCalculator.Candidate(above, "Watch", new BigDecimal("80.00"), true),
                        new TaxFreeCalculator.Candidate(below, "Snack", new BigDecimal("12.00"), true),
                        new TaxFreeCalculator.Candidate(UUID.randomUUID(), "Ignored", new BigDecimal("200.00"), false)
                )
        );

        assertThat(summary.rules().available()).isTrue();
        assertThat(summary.rules().minimumPurchase()).isEqualByComparingTo("50.00");
        assertThat(summary.eligibleCount()).isEqualTo(1);
        assertThat(summary.eligibleTotal()).isEqualByComparingTo("80.00");
        assertThat(summary.estimatedRefundTotal()).isEqualByComparingTo("10.40");
        assertThat(summary.remainingToMinimum()).isEqualByComparingTo("0.00");
        assertThat(summary.items()).hasSize(2);
        TaxFreeCalculator.Line watch = summary.items().stream()
                .filter(line -> line.purchaseId().equals(above))
                .findFirst()
                .orElseThrow();
        assertThat(watch.meetsMinimum()).isTrue();
        assertThat(watch.estimatedRefund()).isEqualByComparingTo("10.40");
    }

    @Test
    void remainingToMinimumWhenNothingQualifies() {
        TaxFreeCalculator.Summary summary = TaxFreeCalculator.summarize(
                "PT",
                "EUR",
                List.of(new TaxFreeCalculator.Candidate(UUID.randomUUID(), "Mug", new BigDecimal("20.00"), true))
        );
        assertThat(summary.eligibleCount()).isZero();
        assertThat(summary.remainingToMinimum()).isEqualByComparingTo("30.00");
    }

    @Test
    void unitedStatesHasNoScheme() {
        TaxFreeCalculator.Summary summary = TaxFreeCalculator.summarize(
                "US",
                "USD",
                List.of(new TaxFreeCalculator.Candidate(UUID.randomUUID(), "Bag", new BigDecimal("400.00"), true))
        );
        assertThat(summary.rules().available()).isFalse();
        assertThat(summary.eligibleCount()).isZero();
        assertThat(summary.remainingToMinimum()).isNull();
    }
}
