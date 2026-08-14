package com.shoptourr.api.web;

import com.shoptourr.api.v1.dto.alert.AlertDtos.TripAlertsResponse;
import com.shoptourr.api.v1.dto.export.ExportDtos.CreateExportRequest;
import com.shoptourr.api.v1.dto.export.ExportDtos.ExportJobDto;
import com.shoptourr.api.v1.dto.map.MapDtos.TripRouteDto;
import com.shoptourr.api.v1.dto.stats.StatsDtos.TripStatsDto;
import com.shoptourr.api.v1.dto.taxfree.TaxFreeDtos.TaxFreeSummaryDto;
import com.shoptourr.api.v1.dto.trip.TripDtos.CreateTripRequest;
import com.shoptourr.api.v1.dto.trip.TripDtos.TripDto;
import com.shoptourr.api.v1.dto.trip.TripDtos.TripListResponse;
import com.shoptourr.api.v1.dto.trip.TripDtos.UpdateTripRequest;
import com.shoptourr.application.AlertsService;
import com.shoptourr.application.ExportService;
import com.shoptourr.application.IdempotencyService;
import com.shoptourr.application.RouteService;
import com.shoptourr.application.StatsService;
import com.shoptourr.application.TaxFreeService;
import com.shoptourr.application.TripService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/trips", version = "1")
public class TripController {

    private final TripService trips;
    private final StatsService stats;
    private final TaxFreeService taxFree;
    private final AlertsService alerts;
    private final RouteService routes;
    private final ExportService exports;
    private final IdempotencyService idempotency;

    public TripController(
            TripService trips,
            StatsService stats,
            TaxFreeService taxFree,
            AlertsService alerts,
            RouteService routes,
            ExportService exports,
            IdempotencyService idempotency
    ) {
        this.trips = trips;
        this.stats = stats;
        this.taxFree = taxFree;
        this.alerts = alerts;
        this.routes = routes;
        this.exports = exports;
        this.idempotency = idempotency;
    }

    @GetMapping
    TripListResponse list(Authentication authentication) {
        return trips.list(CurrentUser.id(authentication));
    }

    @PostMapping
    ResponseEntity<TripDto> create(
            @Valid @RequestBody CreateTripRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            Authentication authentication
    ) {
        UUID userId = CurrentUser.id(authentication);
        return idempotency.run(
                userId,
                idempotencyKey,
                "POST /api/trips",
                request,
                HttpStatus.CREATED.value(),
                TripDto.class,
                () -> trips.create(userId, request)
        );
    }

    @GetMapping("/{tripId}")
    TripDto get(@PathVariable UUID tripId, Authentication authentication) {
        return trips.get(CurrentUser.id(authentication), tripId);
    }

    @PatchMapping("/{tripId}")
    TripDto update(
            @PathVariable UUID tripId,
            @Valid @RequestBody UpdateTripRequest request,
            Authentication authentication
    ) {
        return trips.update(CurrentUser.id(authentication), tripId, request);
    }

    @DeleteMapping("/{tripId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID tripId, Authentication authentication) {
        trips.delete(CurrentUser.id(authentication), tripId);
    }

    @GetMapping("/{tripId}/stats")
    TripStatsDto stats(@PathVariable UUID tripId, Authentication authentication) {
        return stats.stats(CurrentUser.id(authentication), tripId);
    }

    @GetMapping("/{tripId}/tax-free")
    TaxFreeSummaryDto taxFree(@PathVariable UUID tripId, Authentication authentication) {
        return taxFree.summary(CurrentUser.id(authentication), tripId);
    }

    @GetMapping("/{tripId}/alerts")
    TripAlertsResponse alerts(@PathVariable UUID tripId, Authentication authentication) {
        return alerts.list(CurrentUser.id(authentication), tripId);
    }

    @GetMapping("/{tripId}/route")
    TripRouteDto route(@PathVariable UUID tripId, Authentication authentication) {
        return routes.route(CurrentUser.id(authentication), tripId);
    }

    @PostMapping("/{tripId}/exports")
    ResponseEntity<ExportJobDto> createExport(
            @PathVariable UUID tripId,
            @Valid @RequestBody CreateExportRequest request,
            Authentication authentication
    ) {
        ExportJobDto job = exports.create(CurrentUser.id(authentication), tripId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(job);
    }
}
