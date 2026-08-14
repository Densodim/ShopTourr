package com.shoptourr.application;

import com.shoptourr.api.v1.dto.common.CommonDtos.ExchangeRateDto;
import com.shoptourr.api.v1.dto.common.CommonDtos.MoneyDto;
import com.shoptourr.api.v1.dto.common.CommonDtos.VatBreakdownDto;
import com.shoptourr.api.v1.dto.purchase.PurchaseDtos.PurchaseDto;
import com.shoptourr.api.v1.dto.purchase.PurchaseDtos.SplitShareDto;
import com.shoptourr.api.v1.dto.trip.TripDtos.TravelerDto;
import com.shoptourr.api.v1.dto.trip.TripDtos.TripDto;
import com.shoptourr.api.v1.dto.trip.TripDtos.TripStatus;
import com.shoptourr.api.v1.dto.trip.TripDtos.TripSummaryDto;
import com.shoptourr.domain.MoneyMath;
import com.shoptourr.domain.TripCalendar;
import com.shoptourr.infra.persistence.PurchaseEntity;
import com.shoptourr.infra.persistence.PurchaseSplitEntity;
import com.shoptourr.infra.persistence.TravelerEntity;
import com.shoptourr.infra.persistence.TripEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TripMapper {

    private final Clock clock;

    public TripMapper(Clock clock) {
        this.clock = clock;
    }

    public TripStatus liveStatus(TripEntity trip) {
        return TripCalendar.resolve(trip.getStartDate(), trip.getEndDate(), LocalDate.now(clock), trip.getStatus());
    }

    public MoneyDto money(BigDecimal amount, String currency) {
        return new MoneyDto(MoneyMath.scale(amount == null ? BigDecimal.ZERO : amount), currency);
    }

    public TripSummaryDto toSummary(TripEntity trip, BigDecimal spent, int purchaseCount) {
        TripStatus status = liveStatus(trip);
        String currency = trip.getBudgetCurrency();
        return new TripSummaryDto(
                trip.getId(),
                trip.getCity(),
                trip.getCountry(),
                trip.getFlagEmoji(),
                status,
                trip.getStartDate(),
                trip.getEndDate(),
                TripCalendar.datesLabel(trip.getStartDate(), trip.getEndDate()),
                money(trip.getBudgetAmount(), currency),
                money(spent, currency),
                purchaseCount,
                TripCalendar.currentDayNumber(trip.getStartDate(), trip.getEndDate(), LocalDate.now(clock), status),
                TripCalendar.dayCount(trip.getStartDate(), trip.getEndDate())
        );
    }

    public TripDto toDto(
            TripEntity trip,
            List<TravelerEntity> travelers,
            BigDecimal spent,
            int purchaseCount
    ) {
        TripStatus status = liveStatus(trip);
        String currency = trip.getBudgetCurrency();
        BigDecimal remaining = trip.getBudgetAmount().subtract(spent);
        ExchangeRateDto fx = trip.getFxRate() == null ? null : new ExchangeRateDto(
                trip.getFxTripCurrency(),
                trip.getFxQuoteCurrency(),
                trip.getFxRate(),
                trip.getFxRateDate().toString(),
                trip.getFxProvider()
        );
        return new TripDto(
                trip.getId(),
                trip.getCity(),
                trip.getCountry(),
                trip.getCountryCode(),
                trip.getFlagEmoji(),
                status,
                trip.getStartDate(),
                trip.getEndDate(),
                TripCalendar.datesLabel(trip.getStartDate(), trip.getEndDate()),
                money(trip.getBudgetAmount(), currency),
                money(spent, currency),
                money(remaining, currency),
                purchaseCount,
                TripCalendar.dayCount(trip.getStartDate(), trip.getEndDate()),
                TripCalendar.currentDayNumber(trip.getStartDate(), trip.getEndDate(), LocalDate.now(clock), status),
                MoneyMath.scale(trip.getDefaultVatRate()),
                fx,
                travelers.stream().map(this::toTraveler).toList(),
                trip.getCreatedAt(),
                trip.getUpdatedAt()
        );
    }

    public TravelerDto toTraveler(TravelerEntity traveler) {
        return new TravelerDto(
                traveler.getId(),
                traveler.getName(),
                traveler.getColorHex(),
                traveler.getAvatarGlyph(),
                traveler.isOwner()
        );
    }

    public PurchaseDto toPurchase(
            PurchaseEntity purchase,
            List<PurchaseSplitEntity> splits,
            Map<UUID, TravelerEntity> travelers,
            TripEntity trip,
            UUID ownerTravelerId
    ) {
        String currency = purchase.getCurrency();
        List<SplitShareDto> shareDtos = splits.stream()
                .map(split -> {
                    TravelerEntity traveler = travelers.get(split.getTravelerId());
                    return new SplitShareDto(
                            split.getTravelerId(),
                            traveler == null ? "" : traveler.getName(),
                            money(split.getShareAmount(), currency)
                    );
                })
                .toList();
        List<UUID> splitIds = splits.stream().map(PurchaseSplitEntity::getTravelerId).toList();
        BigDecimal yourShare = splits.isEmpty()
                ? purchase.getGrossAmount()
                : splits.stream()
                .filter(s -> s.getTravelerId().equals(ownerTravelerId))
                .map(PurchaseSplitEntity::getShareAmount)
                .findFirst()
                .orElse(purchase.getGrossAmount());
        MoneyDto quote = null;
        if (trip.getFxRate() != null) {
            quote = money(purchase.getGrossAmount().multiply(trip.getFxRate()), trip.getFxQuoteCurrency());
        }
        return new PurchaseDto(
                purchase.getId(),
                purchase.getTripId(),
                purchase.getName(),
                purchase.getCategory(),
                money(purchase.getGrossAmount(), currency),
                new VatBreakdownDto(
                        purchase.getNetAmount(),
                        purchase.getVatAmount(),
                        purchase.getGrossAmount(),
                        MoneyMath.scale(purchase.getVatRate()),
                        purchase.isVatIncluded()
                ),
                purchase.isTaxRefundEligible(),
                purchase.getPlace(),
                purchase.getPurchaseDate(),
                purchase.getPurchaseTime(),
                purchase.getReceiptMediaId(),
                null,
                splitIds,
                shareDtos,
                money(yourShare, currency),
                quote,
                purchase.getCreatedAt(),
                purchase.getUpdatedAt()
        );
    }

    public Map<UUID, TravelerEntity> travelerIndex(List<TravelerEntity> travelers) {
        return travelers.stream().collect(Collectors.toMap(TravelerEntity::getId, t -> t));
    }
}
