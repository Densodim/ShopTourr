package com.shoptourr.api.v1.dto.map;

import com.shoptourr.api.v1.dto.common.CommonDtos.MoneyDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Map / route screen. v1 may return stops derived from purchase places
 * (geocoded server-side); lat/lng nullable until geocode succeeds.
 */
public final class MapDtos {

    private MapDtos() {}

    public record GeoPointDto(
            BigDecimal lat,
            BigDecimal lng
    ) {}

    public record RouteStopDto(
            UUID id,
            String title,
            String place,
            LocalDate date,
            MoneyDto amountSpentHere,
            GeoPointDto point,
            int orderIndex
    ) {}

    public record TripRouteDto(
            UUID tripId,
            int stopCount,
            /** Meters; null if unknown. */
            BigDecimal distanceMeters,
            List<RouteStopDto> stops,
            /** Ordered polyline if available. */
            List<GeoPointDto> path
    ) {}
}
