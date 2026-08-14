package com.shoptourr.application;

import com.shoptourr.api.v1.dto.taxfree.TaxFreeDtos.TaxFreeEligibleItemDto;
import com.shoptourr.api.v1.dto.taxfree.TaxFreeDtos.TaxFreeRulesDto;
import com.shoptourr.api.v1.dto.taxfree.TaxFreeDtos.TaxFreeSummaryDto;
import com.shoptourr.domain.CountryCatalog;
import com.shoptourr.domain.TaxFreeCalculator;
import com.shoptourr.infra.persistence.PurchaseEntity;
import com.shoptourr.infra.persistence.PurchaseRepository;
import com.shoptourr.infra.persistence.TripEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TaxFreeService {

    private final TripService trips;
    private final PurchaseRepository purchases;
    private final TripMapper mapper;

    public TaxFreeService(TripService trips, PurchaseRepository purchases, TripMapper mapper) {
        this.trips = trips;
        this.purchases = purchases;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public TaxFreeSummaryDto summary(UUID userId, UUID tripId) {
        TripEntity trip = trips.require(userId, tripId);
        List<PurchaseEntity> items = purchases.findByTripIdAndDeletedAtIsNullOrderByPurchaseDateDescCreatedAtDesc(tripId);
        String currency = trip.getBudgetCurrency();
        TaxFreeCalculator.Summary summary = TaxFreeCalculator.summarize(
                trip.getCountryCode(),
                currency,
                items.stream()
                        .map(item -> new TaxFreeCalculator.Candidate(
                                item.getId(),
                                item.getName(),
                                item.getGrossAmount(),
                                item.isTaxRefundEligible()
                        ))
                        .toList()
        );
        CountryCatalog.TaxFreeRules rules = summary.rules();
        return new TaxFreeSummaryDto(
                trip.getId(),
                new TaxFreeRulesDto(
                        rules.currency(),
                        mapper.money(rules.minimumPurchase(), rules.currency()),
                        rules.estimatedRefundRate(),
                        rules.regionLabel()
                ),
                summary.eligibleCount(),
                mapper.money(summary.eligibleTotal(), currency),
                mapper.money(summary.estimatedRefundTotal(), currency),
                summary.remainingToMinimum() == null ? null : mapper.money(summary.remainingToMinimum(), currency),
                summary.items().stream()
                        .map(line -> new TaxFreeEligibleItemDto(
                                line.purchaseId(),
                                line.name(),
                                mapper.money(line.amount(), currency),
                                mapper.money(line.estimatedRefund(), currency),
                                line.meetsMinimum()
                        ))
                        .toList()
        );
    }
}
