package com.shoptourr.application;

import com.shoptourr.api.v1.dto.trip.TripDtos.CreateTravelerRequest;
import com.shoptourr.api.v1.dto.trip.TripDtos.CreateTripRequest;
import com.shoptourr.api.v1.dto.trip.TripDtos.TripDto;
import com.shoptourr.api.v1.dto.trip.TripDtos.TripListResponse;
import com.shoptourr.api.v1.dto.trip.TripDtos.TripStatus;
import com.shoptourr.api.v1.dto.trip.TripDtos.TripSummaryDto;
import com.shoptourr.api.v1.dto.trip.TripDtos.UpdateTripRequest;
import com.shoptourr.domain.ApiException;
import com.shoptourr.domain.CountryCatalog;
import com.shoptourr.domain.FxRates;
import com.shoptourr.domain.MoneyMath;
import com.shoptourr.domain.TripCalendar;
import com.shoptourr.infra.persistence.PurchaseEntity;
import com.shoptourr.infra.persistence.PurchaseRepository;
import com.shoptourr.infra.persistence.TravelerEntity;
import com.shoptourr.infra.persistence.TravelerRepository;
import com.shoptourr.infra.persistence.TripEntity;
import com.shoptourr.infra.persistence.TripRepository;
import com.shoptourr.infra.persistence.UserEntity;
import com.shoptourr.infra.persistence.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TripService {

    private final TripRepository trips;
    private final TravelerRepository travelers;
    private final PurchaseRepository purchases;
    private final UserRepository users;
    private final TripMapper mapper;
    private final Clock clock;

    public TripService(
            TripRepository trips,
            TravelerRepository travelers,
            PurchaseRepository purchases,
            UserRepository users,
            TripMapper mapper,
            Clock clock
    ) {
        this.trips = trips;
        this.travelers = travelers;
        this.purchases = purchases;
        this.users = users;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public TripListResponse list(UUID userId) {
        List<TripSummaryDto> active = new ArrayList<>();
        List<TripSummaryDto> upcoming = new ArrayList<>();
        List<TripSummaryDto> past = new ArrayList<>();
        for (TripEntity trip : trips.findByUserIdAndDeletedAtIsNullOrderByStartDateDesc(userId)) {
            TripSummaryDto summary = toSummary(trip);
            switch (summary.status()) {
                case ACTIVE -> active.add(summary);
                case UPCOMING -> upcoming.add(summary);
                case PAST, ARCHIVED -> past.add(summary);
            }
        }
        return new TripListResponse(active, upcoming, past);
    }

    @Transactional
    public TripDto create(UUID userId, CreateTripRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw ApiException.validation("endDate must be on or after startDate");
        }
        UserEntity user = users.findById(userId).orElseThrow(() -> ApiException.notFound("user not found"));
        String countryCode = request.countryCode() == null ? null : request.countryCode().toUpperCase(Locale.ROOT);
        BigDecimal vat = request.defaultVatRatePercent() != null
                ? request.defaultVatRatePercent()
                : CountryCatalog.vatRate(countryCode);
        String tripCurrency = request.budget().currency().toUpperCase(Locale.ROOT);
        String quote = request.quoteCurrency() == null ? user.getPreferredCurrency() : request.quoteCurrency();
        FxRates.Snapshot fx = FxRates.snapshot(tripCurrency, quote, LocalDate.now(clock));

        TripEntity trip = new TripEntity();
        trip.setUserId(userId);
        trip.setCity(request.city());
        trip.setCountry(request.country());
        trip.setCountryCode(countryCode);
        trip.setFlagEmoji(CountryCatalog.flagEmoji(countryCode));
        trip.setStartDate(request.startDate());
        trip.setEndDate(request.endDate());
        trip.setBudgetAmount(MoneyMath.scale(request.budget().amount()));
        trip.setBudgetCurrency(tripCurrency);
        trip.setDefaultVatRate(MoneyMath.scale(vat));
        trip.setFxTripCurrency(fx.tripCurrency());
        trip.setFxQuoteCurrency(fx.quoteCurrency());
        trip.setFxRate(fx.rate());
        trip.setFxRateDate(fx.rateDate());
        trip.setFxProvider(fx.provider());
        trip.setStatus(TripCalendar.resolve(request.startDate(), request.endDate(), LocalDate.now(clock), TripStatus.ACTIVE));
        trips.save(trip);

        if (request.travelers() == null || request.travelers().isEmpty()) {
            saveTraveler(trip.getId(), user.getDisplayName(), "#FFD84D", glyph(user.getDisplayName()), true);
        } else {
            boolean ownerAssigned = false;
            for (CreateTravelerRequest traveler : request.travelers()) {
                boolean owner = !ownerAssigned;
                ownerAssigned = true;
                saveTraveler(
                        trip.getId(),
                        traveler.name(),
                        traveler.colorHex(),
                        traveler.avatarGlyph() == null ? glyph(traveler.name()) : traveler.avatarGlyph(),
                        owner
                );
            }
        }
        return get(userId, trip.getId());
    }

    @Transactional(readOnly = true)
    public TripDto get(UUID userId, UUID tripId) {
        TripEntity trip = require(userId, tripId);
        return mapper.toDto(trip, travelers.findByTripIdOrderByCreatedAtAsc(tripId), spent(tripId), purchaseCount(tripId));
    }

    @Transactional
    public TripDto update(UUID userId, UUID tripId, UpdateTripRequest request) {
        TripEntity trip = require(userId, tripId);
        if (request.city() != null) trip.setCity(request.city());
        if (request.country() != null) trip.setCountry(request.country());
        if (request.countryCode() != null) {
            trip.setCountryCode(request.countryCode().toUpperCase(Locale.ROOT));
            trip.setFlagEmoji(CountryCatalog.flagEmoji(trip.getCountryCode()));
        }
        if (request.startDate() != null) trip.setStartDate(request.startDate());
        if (request.endDate() != null) trip.setEndDate(request.endDate());
        if (request.budget() != null) {
            trip.setBudgetAmount(MoneyMath.scale(request.budget().amount()));
            trip.setBudgetCurrency(request.budget().currency().toUpperCase(Locale.ROOT));
        }
        if (request.defaultVatRatePercent() != null) {
            trip.setDefaultVatRate(MoneyMath.scale(request.defaultVatRatePercent()));
        }
        if (trip.getEndDate().isBefore(trip.getStartDate())) {
            throw ApiException.validation("endDate must be on or after startDate");
        }
        TripStatus stored = request.status() == TripStatus.ARCHIVED
                ? TripStatus.ARCHIVED
                : (request.status() == null ? trip.getStatus() : request.status());
        if (stored != TripStatus.ARCHIVED) {
            stored = TripCalendar.resolve(trip.getStartDate(), trip.getEndDate(), LocalDate.now(clock), stored);
        }
        trip.setStatus(stored);
        return get(userId, tripId);
    }

    @Transactional
    public void delete(UUID userId, UUID tripId) {
        TripEntity trip = require(userId, tripId);
        trip.setDeletedAt(Instant.now(clock));
    }

    TripEntity require(UUID userId, UUID tripId) {
        return trips.findByIdAndUserIdAndDeletedAtIsNull(tripId, userId)
                .orElseThrow(() -> ApiException.notFound("trip not found"));
    }

    BigDecimal spent(UUID tripId) {
        return purchases.findByTripIdAndDeletedAtIsNullOrderByPurchaseDateDescCreatedAtDesc(tripId).stream()
                .map(PurchaseEntity::getGrossAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    int purchaseCount(UUID tripId) {
        return (int) purchases.countByTripIdAndDeletedAtIsNull(tripId);
    }

    TripSummaryDto toSummary(TripEntity trip) {
        return mapper.toSummary(trip, spent(trip.getId()), purchaseCount(trip.getId()));
    }

    private void saveTraveler(UUID tripId, String name, String colorHex, String glyph, boolean owner) {
        TravelerEntity traveler = new TravelerEntity();
        traveler.setTripId(tripId);
        traveler.setName(name);
        traveler.setColorHex(colorHex);
        traveler.setAvatarGlyph(glyph);
        traveler.setOwner(owner);
        travelers.save(traveler);
    }

    private static String glyph(String name) {
        String trimmed = name == null ? "" : name.trim();
        return trimmed.isEmpty() ? "V" : trimmed.substring(0, 1).toUpperCase(Locale.ROOT);
    }
}
