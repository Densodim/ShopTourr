package com.shoptourr.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VatCalculatorTest {

    @Test
    void vatIncludedSplitsGrossIntoNetAndVat() {
        VatCalculator.Breakdown result = VatCalculator.breakdown(
                new BigDecimal("23.00"), new BigDecimal("23"), true);

        assertThat(result.net()).isEqualByComparingTo("18.70");
        assertThat(result.vat()).isEqualByComparingTo("4.30");
        assertThat(result.gross()).isEqualByComparingTo("23.00");
        assertThat(result.vatIncluded()).isTrue();
    }

    @Test
    void vatExcludedAddsVatOnTopOfNet() {
        VatCalculator.Breakdown result = VatCalculator.breakdown(
                new BigDecimal("100.00"), new BigDecimal("23"), false);

        assertThat(result.net()).isEqualByComparingTo("100.00");
        assertThat(result.vat()).isEqualByComparingTo("23.00");
        assertThat(result.gross()).isEqualByComparingTo("123.00");
        assertThat(result.vatIncluded()).isFalse();
    }

    @Test
    void zeroVatKeepsAmountUnchanged() {
        VatCalculator.Breakdown included = VatCalculator.breakdown(
                new BigDecimal("10.00"), BigDecimal.ZERO, true);
        assertThat(included.net()).isEqualByComparingTo("10.00");
        assertThat(included.vat()).isEqualByComparingTo("0.00");

        VatCalculator.Breakdown excluded = VatCalculator.breakdown(
                new BigDecimal("10.00"), BigDecimal.ZERO, false);
        assertThat(excluded.gross()).isEqualByComparingTo("10.00");
        assertThat(excluded.vat()).isEqualByComparingTo("0.00");
    }

    @Test
    void rejectsNegativeRate() {
        assertThatThrownBy(() -> VatCalculator.breakdown(
                new BigDecimal("10.00"), new BigDecimal("-1"), true))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
