package com.shoptourr.api.v1.dto.common;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Shared wire types. Amounts are decimal strings on the wire; map with
 * {@code @JsonFormat(shape = STRING)} or a Jackson module for BigDecimal-as-string.
 */
public final class CommonDtos {

    private CommonDtos() {}

    /** ISO-4217 money. JSON: {"amount":"96.50","currency":"EUR"} */
    public record MoneyDto(
            @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal amount,
            @NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "[A-Z]{3}") String currency
    ) {}

    public record PageRequestDto(
            int page,
            int size,
            String sort
    ) {
        public PageRequestDto {
            if (page < 0) page = 0;
            if (size < 1) size = 20;
            if (size > 100) size = 100;
            if (sort == null || sort.isBlank()) sort = "createdAt,desc";
        }
    }

    public record PageResponseDto<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {}

    public record FieldErrorDto(
            String field,
            String code,
            String message
    ) {}

    /**
     * RFC 7807 Problem Details (+ Voyage extensions {@code code}, {@code errors}, {@code requestId}).
     */
    public record ProblemDetailDto(
            String type,
            String title,
            int status,
            String detail,
            String instance,
            String code,
            List<FieldErrorDto> errors,
            String requestId
    ) {}

    /** FX snapshot stored on trip (rate = units of {@code quoteCurrency} per 1 {@code tripCurrency}). */
    public record ExchangeRateDto(
            @NotBlank @Size(min = 3, max = 3) String tripCurrency,
            @NotBlank @Size(min = 3, max = 3) String quoteCurrency,
            @NotNull @DecimalMin("0.000001") BigDecimal rate,
            @NotNull String rateDate,
            String provider
    ) {}

    public record VatBreakdownDto(
            BigDecimal net,
            BigDecimal vat,
            BigDecimal gross,
            BigDecimal vatRatePercent,
            boolean vatIncluded
    ) {}
}
