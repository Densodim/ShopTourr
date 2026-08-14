package com.shoptourr.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Static FX table for v1: units of quote currency per 1 unit of trip currency.
 */
public final class FxRates {

    private static final Map<String, BigDecimal> TO_RUB = Map.of(
            "EUR", new BigDecimal("98.50"),
            "USD", new BigDecimal("90.00"),
            "GBP", new BigDecimal("115.00"),
            "JPY", new BigDecimal("0.620000"),
            "NOK", new BigDecimal("8.90"),
            "RUB", BigDecimal.ONE
    );

    private FxRates() {}

    public record Snapshot(
            String tripCurrency,
            String quoteCurrency,
            BigDecimal rate,
            LocalDate rateDate,
            String provider
    ) {}

    public static Snapshot snapshot(String tripCurrency, String quoteCurrency, LocalDate date) {
        String trip = tripCurrency.toUpperCase();
        String quote = quoteCurrency.toUpperCase();
        if (trip.equals(quote)) {
            return new Snapshot(trip, quote, BigDecimal.ONE, date, "identity");
        }
        BigDecimal tripToRub = TO_RUB.getOrDefault(trip, BigDecimal.ONE);
        BigDecimal quoteToRub = TO_RUB.getOrDefault(quote, BigDecimal.ONE);
        BigDecimal rate = tripToRub.divide(quoteToRub, 8, java.math.RoundingMode.HALF_UP);
        return new Snapshot(trip, quote, rate, date, "static");
    }
}
