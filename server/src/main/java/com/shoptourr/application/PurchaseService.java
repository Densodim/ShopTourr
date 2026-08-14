package com.shoptourr.application;

import com.shoptourr.api.v1.dto.common.CommonDtos.MoneyDto;
import com.shoptourr.api.v1.dto.purchase.PurchaseDtos.CreatePurchaseRequest;
import com.shoptourr.api.v1.dto.purchase.PurchaseDtos.PurchaseDayGroupDto;
import com.shoptourr.api.v1.dto.purchase.PurchaseDtos.PurchaseDto;
import com.shoptourr.api.v1.dto.purchase.PurchaseDtos.TripPurchasesResponse;
import com.shoptourr.api.v1.dto.purchase.PurchaseDtos.UpdatePurchaseRequest;
import com.shoptourr.domain.ApiException;
import com.shoptourr.domain.MoneyMath;
import com.shoptourr.domain.VatCalculator;
import com.shoptourr.infra.persistence.PurchaseEntity;
import com.shoptourr.infra.persistence.PurchaseRepository;
import com.shoptourr.infra.persistence.PurchaseSplitEntity;
import com.shoptourr.infra.persistence.PurchaseSplitRepository;
import com.shoptourr.infra.persistence.TravelerEntity;
import com.shoptourr.infra.persistence.TravelerRepository;
import com.shoptourr.infra.persistence.TripEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PurchaseService {

    private final TripService trips;
    private final PurchaseRepository purchases;
    private final PurchaseSplitRepository splits;
    private final TravelerRepository travelers;
    private final TripMapper mapper;
    private final Clock clock;

    public PurchaseService(
            TripService trips,
            PurchaseRepository purchases,
            PurchaseSplitRepository splits,
            TravelerRepository travelers,
            TripMapper mapper,
            Clock clock
    ) {
        this.trips = trips;
        this.purchases = purchases;
        this.splits = splits;
        this.travelers = travelers;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public TripPurchasesResponse list(UUID userId, UUID tripId) {
        TripEntity trip = trips.require(userId, tripId);
        List<PurchaseEntity> items = purchases.findByTripIdAndDeletedAtIsNullOrderByPurchaseDateDescCreatedAtDesc(tripId);
        List<TravelerEntity> tripTravelers = travelers.findByTripIdOrderByCreatedAtAsc(tripId);
        UUID ownerId = ownerId(tripTravelers);
        Map<UUID, TravelerEntity> index = mapper.travelerIndex(tripTravelers);
        LinkedHashMap<LocalDate, List<PurchaseDto>> grouped = new LinkedHashMap<>();
        BigDecimal spent = BigDecimal.ZERO;
        for (PurchaseEntity item : items) {
            spent = spent.add(item.getGrossAmount());
            PurchaseDto dto = mapper.toPurchase(item, splits.findByPurchaseId(item.getId()), index, trip, ownerId);
            grouped.computeIfAbsent(item.getPurchaseDate(), key -> new ArrayList<>()).add(dto);
        }
        LocalDate today = LocalDate.now(clock);
        List<PurchaseDayGroupDto> days = grouped.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, List<PurchaseDto>>comparingByKey(Comparator.reverseOrder()))
                .map(entry -> {
                    BigDecimal dayTotal = entry.getValue().stream()
                            .map(dto -> dto.amount().amount())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new PurchaseDayGroupDto(
                            entry.getKey(),
                            labelKey(entry.getKey(), today),
                            mapper.money(dayTotal, trip.getBudgetCurrency()),
                            entry.getValue()
                    );
                })
                .toList();
        MoneyDto budget = mapper.money(trip.getBudgetAmount(), trip.getBudgetCurrency());
        MoneyDto spentTotal = mapper.money(spent, trip.getBudgetCurrency());
        return new TripPurchasesResponse(
                spentTotal,
                budget,
                mapper.money(trip.getBudgetAmount().subtract(spent), trip.getBudgetCurrency()),
                days
        );
    }

    @Transactional
    public PurchaseDto create(UUID userId, UUID tripId, CreatePurchaseRequest request) {
        TripEntity trip = trips.require(userId, tripId);
        if (!trip.getBudgetCurrency().equalsIgnoreCase(request.amount().currency())) {
            throw ApiException.validation("purchase currency must match trip currency");
        }
        PurchaseEntity entity = new PurchaseEntity();
        entity.setTripId(tripId);
        applyCreate(entity, trip, request);
        purchases.save(entity);
        replaceSplits(entity, trip, request.splitWithTravelerIds());
        return get(userId, tripId, entity.getId());
    }

    @Transactional(readOnly = true)
    public PurchaseDto get(UUID userId, UUID tripId, UUID purchaseId) {
        TripEntity trip = trips.require(userId, tripId);
        PurchaseEntity purchase = purchases.findByIdAndTripIdAndDeletedAtIsNull(purchaseId, tripId)
                .orElseThrow(() -> ApiException.notFound("purchase not found"));
        List<TravelerEntity> tripTravelers = travelers.findByTripIdOrderByCreatedAtAsc(tripId);
        return mapper.toPurchase(
                purchase,
                splits.findByPurchaseId(purchaseId),
                mapper.travelerIndex(tripTravelers),
                trip,
                ownerId(tripTravelers)
        );
    }

    @Transactional
    public PurchaseDto update(UUID userId, UUID tripId, UUID purchaseId, UpdatePurchaseRequest request) {
        TripEntity trip = trips.require(userId, tripId);
        PurchaseEntity purchase = purchases.findByIdAndTripIdAndDeletedAtIsNull(purchaseId, tripId)
                .orElseThrow(() -> ApiException.notFound("purchase not found"));
        if (request.name() != null) purchase.setName(request.name());
        if (request.category() != null) purchase.setCategory(request.category());
        if (request.place() != null) purchase.setPlace(request.place());
        if (request.purchaseDate() != null) purchase.setPurchaseDate(request.purchaseDate());
        if (request.purchaseTime() != null) purchase.setPurchaseTime(request.purchaseTime());
        if (request.receiptMediaId() != null) purchase.setReceiptMediaId(request.receiptMediaId());
        if (request.taxRefundEligible() != null) purchase.setTaxRefundEligible(request.taxRefundEligible());
        boolean vatIncluded = request.vatIncluded() == null ? purchase.isVatIncluded() : request.vatIncluded();
        BigDecimal vatRate = request.vatRatePercent() == null ? purchase.getVatRate() : request.vatRatePercent();
        BigDecimal amount = request.amount() == null ? purchase.getGrossAmount() : request.amount().amount();
        String currency = request.amount() == null ? purchase.getCurrency() : request.amount().currency();
        if (request.amount() != null && !trip.getBudgetCurrency().equalsIgnoreCase(currency)) {
            throw ApiException.validation("purchase currency must match trip currency");
        }
        if (request.amount() != null || request.vatIncluded() != null || request.vatRatePercent() != null) {
            VatCalculator.Breakdown vat = VatCalculator.breakdown(amount, vatRate, vatIncluded);
            purchase.setCurrency(trip.getBudgetCurrency());
            purchase.setGrossAmount(vat.gross());
            purchase.setNetAmount(vat.net());
            purchase.setVatAmount(vat.vat());
            purchase.setVatRate(MoneyMath.scale(vatRate));
            purchase.setVatIncluded(vatIncluded);
        }
        if (request.splitWithTravelerIds() != null) {
            replaceSplits(purchase, trip, request.splitWithTravelerIds());
        }
        return get(userId, tripId, purchaseId);
    }

    @Transactional
    public void delete(UUID userId, UUID tripId, UUID purchaseId) {
        trips.require(userId, tripId);
        PurchaseEntity purchase = purchases.findByIdAndTripIdAndDeletedAtIsNull(purchaseId, tripId)
                .orElseThrow(() -> ApiException.notFound("purchase not found"));
        purchase.setDeletedAt(Instant.now(clock));
    }

    private void applyCreate(PurchaseEntity entity, TripEntity trip, CreatePurchaseRequest request) {
        BigDecimal vatRate = request.vatRatePercent() == null ? trip.getDefaultVatRate() : request.vatRatePercent();
        VatCalculator.Breakdown vat = VatCalculator.breakdown(request.amount().amount(), vatRate, request.vatIncluded());
        entity.setName(request.name());
        entity.setCategory(request.category());
        entity.setCurrency(trip.getBudgetCurrency());
        entity.setGrossAmount(vat.gross());
        entity.setNetAmount(vat.net());
        entity.setVatAmount(vat.vat());
        entity.setVatRate(MoneyMath.scale(vatRate));
        entity.setVatIncluded(request.vatIncluded());
        entity.setTaxRefundEligible(request.taxRefundEligible());
        entity.setPlace(request.place());
        entity.setPurchaseDate(request.purchaseDate() == null ? LocalDate.now(clock) : request.purchaseDate());
        entity.setPurchaseTime(request.purchaseTime() == null ? LocalTime.now(clock) : request.purchaseTime());
        entity.setReceiptMediaId(request.receiptMediaId());
    }

    private void replaceSplits(PurchaseEntity purchase, TripEntity trip, List<UUID> travelerIds) {
        splits.deleteByPurchaseId(purchase.getId());
        List<TravelerEntity> tripTravelers = travelers.findByTripIdOrderByCreatedAtAsc(trip.getId());
        List<UUID> ids = travelerIds == null || travelerIds.isEmpty()
                ? List.of(ownerId(tripTravelers))
                : travelerIds;
        for (UUID id : ids) {
            boolean exists = tripTravelers.stream().anyMatch(t -> t.getId().equals(id));
            if (!exists) {
                throw ApiException.validation("unknown traveler in split");
            }
        }
        List<BigDecimal> shares = MoneyMath.splitEqually(purchase.getGrossAmount(), ids.size());
        for (int i = 0; i < ids.size(); i++) {
            PurchaseSplitEntity split = new PurchaseSplitEntity();
            split.setPurchaseId(purchase.getId());
            split.setTravelerId(ids.get(i));
            split.setShareAmount(shares.get(i));
            splits.save(split);
        }
    }

    private static UUID ownerId(List<TravelerEntity> tripTravelers) {
        return tripTravelers.stream()
                .filter(TravelerEntity::isOwner)
                .map(TravelerEntity::getId)
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("trip owner traveler missing"));
    }

    private static String labelKey(LocalDate date, LocalDate today) {
        if (date.equals(today)) return "TODAY";
        if (date.equals(today.minusDays(1))) return "YESTERDAY";
        return null;
    }
}
