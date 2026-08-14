package com.shoptourr.application;

import com.shoptourr.api.v1.dto.export.ExportDtos.CreateExportRequest;
import com.shoptourr.api.v1.dto.export.ExportDtos.ExportFormat;
import com.shoptourr.api.v1.dto.export.ExportDtos.ExportJobDto;
import com.shoptourr.api.v1.dto.export.ExportDtos.ExportJobStatus;
import com.shoptourr.domain.ApiException;
import com.shoptourr.infra.persistence.DiaryEntryEntity;
import com.shoptourr.infra.persistence.DiaryEntryRepository;
import com.shoptourr.infra.persistence.ExportJobEntity;
import com.shoptourr.infra.persistence.ExportJobRepository;
import com.shoptourr.infra.persistence.PurchaseEntity;
import com.shoptourr.infra.persistence.PurchaseRepository;
import com.shoptourr.infra.persistence.TripEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ExportService {

    private final TripService trips;
    private final PurchaseRepository purchases;
    private final DiaryEntryRepository diary;
    private final ExportJobRepository jobs;
    private final Clock clock;

    public ExportService(
            TripService trips,
            PurchaseRepository purchases,
            DiaryEntryRepository diary,
            ExportJobRepository jobs,
            Clock clock
    ) {
        this.trips = trips;
        this.purchases = purchases;
        this.diary = diary;
        this.jobs = jobs;
        this.clock = clock;
    }

    @Transactional
    public ExportJobDto create(UUID userId, UUID tripId, CreateExportRequest request) {
        TripEntity trip = trips.require(userId, tripId);
        Instant now = Instant.now(clock);
        ExportJobEntity job = new ExportJobEntity();
        job.setUserId(userId);
        job.setTripId(tripId);
        job.setFormat(request.format());
        job.setIncludeTaxFree(request.includeTaxFree());
        job.setIncludeDiary(request.includeDiary());
        job.setStatus(ExportJobStatus.READY);
        job.setContentType(request.format() == ExportFormat.CSV
                ? "text/csv"
                : MediaType.APPLICATION_PDF_VALUE);
        job.setContent(render(trip, request));
        job.setFinishedAt(now);
        job.setExpiresAt(now.plus(Duration.ofHours(24)));
        jobs.save(job);
        return toDto(job);
    }

    @Transactional(readOnly = true)
    public ExportJobDto get(UUID userId, UUID exportId) {
        return toDto(require(userId, exportId));
    }

    @Transactional(readOnly = true)
    public ExportJobEntity file(UUID userId, UUID exportId) {
        ExportJobEntity job = require(userId, exportId);
        if (job.getStatus() != ExportJobStatus.READY || job.getContent() == null) {
            throw ApiException.notFound("export is not ready");
        }
        if (job.getExpiresAt() != null && Instant.now(clock).isAfter(job.getExpiresAt())) {
            throw ApiException.notFound("export expired");
        }
        return job;
    }

    private ExportJobEntity require(UUID userId, UUID exportId) {
        return jobs.findByIdAndUserId(exportId, userId)
                .orElseThrow(() -> ApiException.notFound("export not found"));
    }

    private String render(TripEntity trip, CreateExportRequest request) {
        StringBuilder csv = new StringBuilder();
        csv.append("trip,city,country,date,name,category,amount,currency,place\n");
        List<PurchaseEntity> items = purchases.findByTripIdAndDeletedAtIsNullOrderByPurchaseDateDescCreatedAtDesc(trip.getId());
        for (PurchaseEntity item : items) {
            csv.append(trip.getId()).append(',')
                    .append(escape(trip.getCity())).append(',')
                    .append(escape(trip.getCountry())).append(',')
                    .append(item.getPurchaseDate()).append(',')
                    .append(escape(item.getName())).append(',')
                    .append(item.getCategory()).append(',')
                    .append(item.getGrossAmount().toPlainString()).append(',')
                    .append(item.getCurrency()).append(',')
                    .append(escape(item.getPlace() == null ? "" : item.getPlace()))
                    .append('\n');
        }
        if (request.includeDiary()) {
            csv.append("\ndiary_date,mood,text\n");
            for (DiaryEntryEntity entry : diary.findByTripIdOrderByEntryDateDescCreatedAtDesc(trip.getId())) {
                csv.append(entry.getEntryDate()).append(',')
                        .append(escape(entry.getMood())).append(',')
                        .append(escape(entry.getText()))
                        .append('\n');
            }
        }
        return csv.toString();
    }

    private ExportJobDto toDto(ExportJobEntity job) {
        String download = job.getStatus() == ExportJobStatus.READY
                ? ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/exports/{id}/file")
                .buildAndExpand(job.getId())
                .toUriString()
                : null;
        return new ExportJobDto(
                job.getId(),
                job.getTripId(),
                job.getFormat(),
                job.getStatus(),
                download,
                job.getExpiresAt(),
                job.getErrorCode(),
                job.getCreatedAt(),
                job.getFinishedAt()
        );
    }

    private static String escape(String raw) {
        if (raw.contains(",") || raw.contains("\"") || raw.contains("\n")) {
            return "\"" + raw.replace("\"", "\"\"") + "\"";
        }
        return raw;
    }
}
