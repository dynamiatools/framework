/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package tools.dynamia.modules.saas.migration.controllers;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tools.dynamia.modules.entityfile.StoredEntityFile;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.saas.migration.api.AccountCloneOptions;
import tools.dynamia.modules.saas.migration.api.AccountExportOptions;
import tools.dynamia.modules.saas.migration.api.AccountImportOptions;
import tools.dynamia.modules.saas.migration.api.AccountMigrationJobDto;
import tools.dynamia.modules.saas.migration.api.AccountMigrationJobService;

import java.util.List;
import java.util.Map;

/**
 * REST API for the Tenant Mobility module.
 *
 * <h3>Endpoints</h3>
 * <pre>
 * POST  /api/saas/migration/jobs/export/{accountId}         → start export job
 * POST  /api/saas/migration/jobs/import                      → start import job (multipart)
 * POST  /api/saas/migration/jobs/clone                       → start clone job
 * POST  /api/saas/migration/jobs/backup/{accountId}          → start backup job
 * POST  /api/saas/migration/jobs/restore/{accountId}         → start restore job (multipart)
 * GET   /api/saas/migration/jobs                             → list all jobs
 * GET   /api/saas/migration/jobs/{jobId}                     → get job status
 * POST  /api/saas/migration/jobs/{jobId}/cancel              → cancel a running job
 * GET   /api/saas/migration/jobs/{jobId}/download            → download result file
 * </pre>
 *
 * <p><strong>Note:</strong> Authorization is NOT enforced by this controller — the host
 * application is responsible for securing these endpoints (e.g., via Spring Security,
 * admin role checks, or API key filtering). These endpoints deal with raw tenant data
 * and should be restricted to system administrators only.
 *
 * @author Mario Serrano Leones
 */
@RestController
@RequestMapping(value = "/api/saas/migration", produces = MediaType.APPLICATION_JSON_VALUE)
public class AccountMigrationRestController {

    private final AccountMigrationJobService jobService;

    public AccountMigrationRestController(AccountMigrationJobService jobService) {
        this.jobService = jobService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Export
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Start an export job for the specified account.
     *
     * <p>Request body (optional JSON):
     * <pre>{@code
     * {
     *   "chunkSize": 500,
     *   "compressionEnabled": true,
     *   "identityStrategy": "KEEP_IDS"
     * }
     * }</pre>
     */
    @PostMapping("/jobs/export/{accountId}")
    public ResponseEntity<AccountMigrationJobDto> startExport(
            @PathVariable Long accountId,
            @RequestBody(required = false) AccountExportOptions options) {

        AccountExportOptions opts = options != null ? options : new AccountExportOptions();
        AccountMigrationJobDto job = jobService.createExportJob(accountId, opts);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(job);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Import
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Start an import job from an uploaded export file.
     *
     * <p>Form fields:
     * <ul>
     *   <li>{@code file} — the export JSON or JSON.GZ file (required)</li>
     *   <li>{@code targetAccountId} — target account ID (optional; null = create from file)</li>
     *   <li>{@code identityStrategy} — KEEP_IDS or REGENERATE_IDS (optional)</li>
     *   <li>{@code chunkSize} — records per transaction (optional)</li>
     * </ul>
     */
    @PostMapping(value = "/jobs/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AccountMigrationJobDto> startImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long targetAccountId,
            @RequestParam(required = false) String identityStrategy,
            @RequestParam(required = false, defaultValue = "0") int chunkSize) {

        AccountImportOptions options = new AccountImportOptions()
                .targetAccountId(targetAccountId)
                .chunkSize(chunkSize > 0 ? chunkSize : 500);

        if (identityStrategy != null) {
            try {
                options.setIdentityStrategy(
                        tools.dynamia.modules.saas.migration.api.IdentityStrategy.valueOf(identityStrategy));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        AccountMigrationJobDto job = jobService.createImportJob(file, options);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(job);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Clone
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Start a clone job (source tenant → target tenant, same system).
     *
     * <p>Request body:
     * <pre>{@code
     * {
     *   "sourceAccountId": 42,
     *   "targetAccountId": 99,
     *   "identityStrategy": "REGENERATE_IDS",
     *   "chunkSize": 500
     * }
     * }</pre>
     */
    @PostMapping("/jobs/clone")
    public ResponseEntity<AccountMigrationJobDto> startClone(
            @RequestBody AccountCloneOptions options) {

        if (options.getSourceAccountId() == null || options.getTargetAccountId() == null) {
            return ResponseEntity.badRequest().build();
        }
        AccountMigrationJobDto job = jobService.createCloneJob(options);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(job);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Backup / Restore
    // ─────────────────────────────────────────────────────────────────────────

    /** Start a backup job (export with BACKUP type label and compression). */
    @PostMapping("/jobs/backup/{accountId}")
    public ResponseEntity<AccountMigrationJobDto> startBackup(@PathVariable Long accountId) {
        AccountMigrationJobDto job = jobService.createBackupJob(accountId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(job);
    }

    /** Start a restore job from an uploaded export file. */
    @PostMapping(value = "/jobs/restore/{accountId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AccountMigrationJobDto> startRestore(
            @PathVariable Long accountId,
            @RequestParam("file") MultipartFile file) {

        AccountMigrationJobDto job = jobService.createRestoreJob(accountId, file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(job);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Job management
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * List all jobs. Pass {@code ?accountId=X} to filter by account.
     */
    @GetMapping("/jobs")
    public ResponseEntity<List<AccountMigrationJobDto>> listJobs(
            @RequestParam(required = false) Long accountId) {
        return ResponseEntity.ok(jobService.listJobs(accountId));
    }

    /** Get the current status of a job by its UUID. */
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<AccountMigrationJobDto> getJob(@PathVariable String jobId) {
        AccountMigrationJobDto job = jobService.getJob(jobId);
        if (job == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(job);
    }

    /** Request cancellation of a running job. Idempotent. */
    @PostMapping("/jobs/{jobId}/cancel")
    public ResponseEntity<Map<String, String>> cancelJob(@PathVariable String jobId) {
        AccountMigrationJobDto job = jobService.getJob(jobId);
        if (job == null) return ResponseEntity.notFound().build();
        jobService.cancelJob(jobId);
        return ResponseEntity.ok(Map.of("message", "Cancellation requested for job " + jobId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // File download
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Download the result file of a completed EXPORT or BACKUP job.
     * The file is streamed from wherever {@code EntityFileStorage} is configured
     * (local safe directory, S3, Buckie, etc.) — never from a raw local/container path.
     * Returns 404 if the job is not found, not completed, or has no result file.
     */
    @GetMapping("/jobs/{jobId}/download")
    public ResponseEntity<Resource> downloadResult(@PathVariable String jobId) {
        StoredEntityFile stored = jobService.downloadResult(jobId);
        if (stored == null) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = stored.toResource();
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        EntityFile entityFile = stored.getEntityFile();
        String contentType = entityFile.getContentType() != null ? entityFile.getContentType() : "application/zip";

        ResponseEntity.BodyBuilder response = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + entityFile.getName() + "\"");
        if (entityFile.getSize() != null) {
            response.header(HttpHeaders.CONTENT_LENGTH, String.valueOf(entityFile.getSize()));
        }
        return response.body(resource);
    }
}

