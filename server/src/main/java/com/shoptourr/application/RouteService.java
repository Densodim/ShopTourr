package com.shoptourr.application;

import com.shoptourr.api.v1.dto.map.MapDtos.RouteStopDto;
import com.shoptourr.api.v1.dto.map.MapDtos.TripRouteDto;
import com.shoptourr.infra.persistence.PurchaseEntity;
import com.shoptourr.infra.persistence.PurchaseRepository;
import com.shoptourr.infra.persistence.TripEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RouteService {

    private final TripService trips;
    private final PurchaseRepository purchases;
    private final TripMapper mapper;

    public RouteService(TripService trips, PurchaseRepository purchases, TripMapper mapper) {
        this.trips = trips;
        this.purchases = purchases;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public TripRouteDto route(UUID userId, UUID tripId) {
        TripEntity trip = trips.require(userId, tripId);
        List<PurchaseEntity> items = purchases.findByTripIdAndDeletedAtIsNullOrderByPurchaseDateDescCreatedAtDesc(tripId);
        LinkedHashMap<String, StopAcc> grouped = new LinkedHashMap<>();
        for (PurchaseEntity item : items) {
            if (item.getPlace() == null || item.getPlace().isBlank()) {
                continue;
            }
            String key = item.getPlace().trim();
            grouped.computeIfAbsent(key, ignored -> new StopAcc(item.getPurchaseDate()))
                    .add(item.getGrossAmount(), item.getPurchaseDate());
        }
        List<Map.Entry<String, StopAcc>> ordered = new ArrayList<>(grouped.entrySet());
        ordered.sort(Comparator.comparing((Map.Entry<String, StopAcc> entry) -> entry.getValue().firstDate)
                .thenComparing(Map.Entry::getKey));
        List<RouteStopDto> stops = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, StopAcc> entry : ordered) {
            StopAcc acc = entry.getValue();
            UUID stopId = UUID.nameUUIDFromBytes((tripId + ":" + entry.getKey()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            stops.add(new RouteStopDto(
                    stopId,
                    entry.getKey(),
                    entry.getKey(),
                    acc.firstDate,
                    mapper.money(acc.spent, trip.getBudgetCurrency()),
                    null,
                    index++
            ));
        }
        return new TripRouteDto(trip.getId(), stops.size(), null, stops, List.of());
    }

    private static final class StopAcc {
        private BigDecimal spent = BigDecimal.ZERO.setScale(2);
        private LocalDate firstDate;

        private StopAcc(LocalDate firstDate) {
            this.firstDate = firstDate;
        }

        private void add(BigDecimal amount, LocalDate date) {
            spent = spent.add(amount);
            if (date.isBefore(firstDate)) {
                firstDate = date;
            }
        }
    }
}
