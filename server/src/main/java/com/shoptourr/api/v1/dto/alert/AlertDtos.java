package com.shoptourr.api.v1.dto.alert;

import com.shoptourr.api.v1.dto.common.CommonDtos.MoneyDto;
import com.shoptourr.api.v1.dto.purchase.PurchaseDtos.PurchaseCategory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Budget / pace alerts screen. */
public final class AlertDtos {

    private AlertDtos() {}

    public enum AlertSeverity {
        INFO, WARNING, CRITICAL
    }

    public enum AlertType {
        PACE_HIGH,
        CATEGORY_OVERSPENT,
        BUDGET_ALMOST_GONE,
        BUDGET_EXCEEDED,
        DAILY_ALLOWANCE
    }

    public record BudgetAlertDto(
            UUID id,
            AlertType type,
            AlertSeverity severity,
            String titleKey,
            String bodyKey,
            /** Interpolation vars for client i18n, e.g. {"days":"2","category":"FOOD"}. */
            java.util.Map<String, String> params,
            MoneyDto dailyRemaining,
            PurchaseCategory category,
            Instant createdAt,
            boolean read
    ) {}

    public record TripAlertsResponse(
            List<BudgetAlertDto> alerts
    ) {}
}
