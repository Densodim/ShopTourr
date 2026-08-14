package com.shoptourr.api.web;

import com.shoptourr.api.v1.dto.export.ExportDtos.ExportJobDto;
import com.shoptourr.application.ExportService;
import com.shoptourr.infra.persistence.ExportJobEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/exports", version = "1")
public class ExportController {

    private final ExportService exports;

    public ExportController(ExportService exports) {
        this.exports = exports;
    }

    @GetMapping("/{exportId}")
    ExportJobDto get(@PathVariable UUID exportId, Authentication authentication) {
        return exports.get(CurrentUser.id(authentication), exportId);
    }

    @GetMapping("/{exportId}/file")
    ResponseEntity<byte[]> file(@PathVariable UUID exportId, Authentication authentication) {
        ExportJobEntity job = exports.file(CurrentUser.id(authentication), exportId);
        String filename = "voyage-export-" + job.getId() + (job.getFormat().name().equals("CSV") ? ".csv" : ".pdf");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(job.getContentType()))
                .body(job.getContent().getBytes(StandardCharsets.UTF_8));
    }
}
