package com.shoptourr.application;

import com.shoptourr.api.v1.dto.alert.AlertDtos.AlertSeverity;
import com.shoptourr.api.v1.dto.alert.AlertDtos.AlertType;
import com.shoptourr.api.v1.dto.alert.AlertDtos.BudgetAlertDto;
import com.shoptourr.api.v1.dto.alert.AlertDtos.TripAlertsResponse;
import com.shoptourr.api.v1.dto.purchase.PurchaseDtos.PurchaseCategory;
import com.shoptourr.domain.MoneyMath;
import com.shoptourr.domain.TripCalendar;
import com.shoptourr.infra.persistence.PurchaseEntity;
import com.shoptourr.infra.persistence.PurchaseRepository;
import com.shoptourr.infra.persistence.TripEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AlertsService {

    private final TripService trips;
    private final PurchaseRepository purchases;
    private final TripMapper mapper;
    private final Clock clock;

    public AlertsService(
            TripService trips,
            PurchaseRepository purchases,
            TripMapper mapper,
            Clock clock
    ) {
        this.trips = trips;
        this.purchases = purchases;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public TripAlertsResponse list(UUID userId, UUID tripId) {
        TripEntity trip = trips.require(userId, tripId);
        List<PurchaseEntity> items = purchases.findByTripIdAndDeletedAtIsNullOrderByPurchaseDateDescCreatedAtDesc(tripId);
        String currency = trip.getBudgetCurrency();
        BigDecimal spent = items.stream().map(PurchaseEntity::getGrossAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = trip.getBudgetAmount().subtract(spent);
        LocalDate today = LocalDate.now(clock);
        int daysLeft = Math.max((int) java.time.temporal.ChronoUnit.DAYS.between(today, trip.getEndDate()) + 1, 1);
        if (today.isAfter(trip.getEndDate())) {
            daysLeft = 1;
        }
        BigDecimal dailyRemaining = remaining.signum() <= 0
                ? BigDecimal.ZERO.setScale(2)
                : remaining.divide(BigDecimal.valueOf(daysLeft), 2, RoundingMode.HALF_UP);
        Instant now = Instant.now(clock);
        List<BudgetAlertDto> alerts = new ArrayList<>();
        if (remaining.signum() < 0) {
            alerts.add(alert(
                    tripId,
                    AlertType.BUDGET_EXCEEDED,
                    AlertSeverity.CRITICAL,
                    "alert_budget_exceeded_title",
                    "alert_budget_exceeded_body",
                    Map.of("spent", MoneyMath.format(spent)),
                    dailyRemaining,
                    null,
                    currency,
                    now
            ));
        } else if (trip.getBudgetAmount().signum() > 0
                && remaining.multiply(BigDecimal.TEN).compareTo(trip.getBudgetAmount()) < 0) {
            alerts.add(alert(
                    tripId,
                    AlertType.BUDGET_ALMOST_GONE,
                    AlertSeverity.WARNING,
                    "alert_budget_almost_gone_title",
                    "alert_budget_almost_gone_body",
                    Map.of("remaining", MoneyMath.format(remaining)),
                    dailyRemaining,
                    null,
                    currency,
                    now
            ));
        }
        EnumMap<PurchaseCategory, BigDecimal> byCat = new EnumMap<>(PurchaseCategory.class);
        for (PurchaseEntity item : items) {
            byCat.merge(item.getCategory(), item.getGrossAmount(), BigDecimal::add);
        }
        byCat.entrySet().stream()
                .filter(entry -> trip.getBudgetAmount().signum() > 0
                        && entry.getValue().multiply(BigDecimal.valueOf(100))
                        .divide(trip.getBudgetAmount(), 2, RoundingMode.HALF_UP)
                        .compareTo(new BigDecimal("40.00")) >= 0)
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> alerts.add(alert(
                        tripId,
                        AlertType.CATEGORY_OVERSPENT,
                        AlertSeverity.WARNING,
                        "alert_category_overspent_title",
                        "alert_category_overspent_body",
                        Map.of("category", entry.getKey().name()),
                        dailyRemaining,
                        entry.getKey(),
                        currency,
                        now
                )));
        int elapsed = Math.max(TripCalendar.dayCount(trip.getStartDate(), today.isAfter(trip.getEndDate()) ? trip.getEndDate() : today), 1);
        BigDecimal dailyAverage = spent.divide(BigDecimal.valueOf(elapsed), 2, RoundingMode.HALF_UP);
        if (remaining.signum() > 0 && dailyAverage.compareTo(dailyRemaining) > 0) {
            alerts.add(alert(
                    tripId,
                    AlertType.PACE_HIGH,
                    AlertSeverity.WARNING,
                    "alert_pace_high_title",
                    "alert_pace_high_body",
                    Map.of("days", String.valueOf(daysLeft)),
                    dailyRemaining,
                    null,
                    currency,
                    now
            ));
        }
        alerts.add(alert(
                tripId,
                AlertType.DAILY_ALLOWANCE,
                AlertSeverity.INFO,
                "alert_daily_allowance_title",
                "alert_daily_allowance_body",
                Map.of("amount", MoneyMath.format(dailyRemaining)),
                dailyRemaining,
                null,
                currency,
                now
        ));
        return new TripAlertsResponse(alerts);
    }

    private BudgetAlertDto alert(
            UUID tripId,
            AlertType type,
            AlertSeverity severity,
            String titleKey,
            String bodyKey,
            Map<String, String> params,
            BigDecimal dailyRemaining,
            PurchaseCategory category,
            String currency,
            Instant now
    ) {
        UUID id = UUID.nameUUIDFromBytes((tripId + ":" + type.name()).getBytes(StandardCharsets.UTF_8));
        return new BudgetAlertDto(
                id,
                type,
                severity,
                titleKey,
                bodyKey,
                params,
                mapper.money(dailyRemaining, currency),
                category,
                now,
                false
        );
    }
}
