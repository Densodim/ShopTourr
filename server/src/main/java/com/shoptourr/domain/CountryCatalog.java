package com.shoptourr.domain;

import java.math.BigDecimal;
import java.util.Map;

public final class CountryCatalog {

    private record CountryMeta(
            BigDecimal vatRate,
            String flagEmoji,
            BigDecimal taxFreeMinimum,
            BigDecimal taxFreeRefundRate,
            String taxFreeRegion
    ) {}

    public record TaxFreeRules(
            String currency,
            BigDecimal minimumPurchase,
            BigDecimal estimatedRefundRate,
            String regionLabel,
            boolean available
    ) {}

    private static final Map<String, CountryMeta> BY_CODE = Map.ofEntries(
            Map.entry("PT", new CountryMeta(new BigDecimal("23"), "🇵🇹", new BigDecimal("50.00"), new BigDecimal("0.13"), "EU")),
            Map.entry("ES", new CountryMeta(new BigDecimal("21"), "🇪🇸", new BigDecimal("90.00"), new BigDecimal("0.13"), "EU")),
            Map.entry("FR", new CountryMeta(new BigDecimal("20"), "🇫🇷", new BigDecimal("100.00"), new BigDecimal("0.12"), "EU")),
            Map.entry("DE", new CountryMeta(new BigDecimal("19"), "🇩🇪", new BigDecimal("50.00"), new BigDecimal("0.13"), "EU")),
            Map.entry("IT", new CountryMeta(new BigDecimal("22"), "🇮🇹", new BigDecimal("70.00"), new BigDecimal("0.12"), "EU")),
            Map.entry("JP", new CountryMeta(new BigDecimal("10"), "🇯🇵", new BigDecimal("5000.00"), new BigDecimal("0.08"), "Japan")),
            Map.entry("NO", new CountryMeta(new BigDecimal("25"), "🇳🇴", new BigDecimal("280.00"), new BigDecimal("0.12"), "Norway")),
            Map.entry("GB", new CountryMeta(new BigDecimal("20"), "🇬🇧", new BigDecimal("30.00"), new BigDecimal("0.16"), "UK")),
            Map.entry("US", new CountryMeta(BigDecimal.ZERO, "🇺🇸", null, BigDecimal.ZERO, "Not available")),
            Map.entry("RU", new CountryMeta(new BigDecimal("20"), "🇷🇺", null, BigDecimal.ZERO, "Not available"))
    );

    private CountryCatalog() {}

    public static BigDecimal vatRate(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return BigDecimal.ZERO;
        }
        CountryMeta meta = BY_CODE.get(countryCode.toUpperCase());
        return meta == null ? BigDecimal.ZERO : meta.vatRate();
    }

    public static String flagEmoji(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return null;
        }
        CountryMeta meta = BY_CODE.get(countryCode.toUpperCase());
        return meta == null ? null : meta.flagEmoji();
    }

    public static TaxFreeRules taxFreeRules(String countryCode, String currency) {
        String iso = currency == null || currency.isBlank() ? "EUR" : currency.toUpperCase();
        if (countryCode == null || countryCode.isBlank()) {
            return unavailable(iso);
        }
        CountryMeta meta = BY_CODE.get(countryCode.toUpperCase());
        if (meta == null || meta.taxFreeMinimum() == null) {
            return unavailable(iso);
        }
        return new TaxFreeRules(
                iso,
                MoneyMath.scale(meta.taxFreeMinimum()),
                meta.taxFreeRefundRate(),
                meta.taxFreeRegion(),
                true
        );
    }

    private static TaxFreeRules unavailable(String currency) {
        return new TaxFreeRules(currency, MoneyMath.scale(BigDecimal.ZERO), BigDecimal.ZERO, "Not available", false);
    }
}
