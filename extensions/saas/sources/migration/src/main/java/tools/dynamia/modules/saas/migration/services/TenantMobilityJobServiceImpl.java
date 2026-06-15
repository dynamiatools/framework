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
package tools.dynamia.modules.saas.migration.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.scheduling.SchedulerUtil;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.modules.saas.migration.api.CancellationToken;
import tools.dynamia.modules.saas.migration.api.IdentityStrategy;
import tools.dynamia.modules.saas.migration.api.MigrationProgress;
import tools.dynamia.modules.saas.migration.api.TenantCloneOptions;
import tools.dynamia.modules.saas.migration.api.TenantExportOptions;
import tools.dynamia.modules.saas.migration.api.TenantImportOptions;
import tools.dynamia.modules.saas.migration.api.TenantMobilityJobDto;
import tools.dynamia.modules.saas.migration.api.TenantMobilityJobService;
import tools.dynamia.modules.saas.migration.api.TenantMobilityService;
import tools.dynamia.modules.saas.migration.config.TenantMigrationProperties;
import tools.dynamia.modules.saas.migration.domain.TenantJobStatus;
import tools.dynamia.modules.saas.migration.domain.TenantJobType;
import tools.dynamia.modules.saas.migration.domain.TenantMobilityJob;
import tools.dynamia.modules.saas.migration.workers.CloneWorker;
import tools.dynamia.modules.saas.migration.workers.ExportWorker;
import tools.dynamia.modules.saas.migration.workers.ImportWorker;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link TenantMobilityJobService}.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Persist {@link TenantMobilityJob} records in the database.</li>
 *   <li>Launch worker tasks via {@link SchedulerUtil#runWithResult(tools.dynamia.integration.scheduling.TaskWithResult)}
 *       on virtual threads.</li>
 *   <li>Update job status, progress and result path as the worker executes.</li>
 *   <li>Maintain an in-memory {@link CancellationToken} registry so running jobs can be cancelled.</li>
 * </ol>
 *
 * @author Mario Serrano Leones
 */
@Service
public class TenantMobilityJobServiceImpl implements TenantMobilityJobService {

    private static final Logger log = LoggerFactory.getLogger(TenantMobilityJobServiceImpl.class);
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    /** In-memory token registry: jobUuid → CancellationToken. Cleaned up when job finishes. */
    private final Map<String, CancellationToken> activeTokens = new ConcurrentHashMap<>();

    private final CrudService crudService;
    private final TenantMobilityService mobilityService;
    private final TenantMigrationProperties properties;

    public TenantMobilityJobServiceImpl(CrudService crudService,
                                         TenantMobilityService mobilityService,
                                         TenantMigrationProperties properties) {
        this.crudService = crudService;
        this.mobilityService = mobilityService;
        this.properties = properties;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Job creation
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public TenantMobilityJobDto createExportJob(Long accountId, TenantExportOptions options) {
        TenantMobilityJob job = createAndSaveJob(accountId, null, TenantJobType.EXPORT);
        launchExportJob(job, accountId, options);
        return toDto(job);
    }

    @Override
    public TenantMobilityJobDto createBackupJob(Long accountId) {
        TenantExportOptions options = new TenantExportOptions()
                .compressionEnabled(properties.isCompressionEnabled())
                .label("backup");
        TenantMobilityJob job = createAndSaveJob(accountId, null, TenantJobType.BACKUP);
        launchExportJob(job, accountId, options);
        return toDto(job);
    }

    @Override
    public TenantMobilityJobDto createImportJob(MultipartFile file, TenantImportOptions options) {
        Path savedFile = saveUploadedFile(file, "import");
        TenantMobilityJob job = createAndSaveJob(options.getTargetAccountId(), null, TenantJobType.IMPORT);
        launchImportJob(job, savedFile, options);
        return toDto(job);
    }

    @Override
    public TenantMobilityJobDto createRestoreJob(Long accountId, MultipartFile file) {
        Path savedFile = saveUploadedFile(file, "restore");
        TenantImportOptions options = new TenantImportOptions()
                .targetAccountId(accountId)
                .identityStrategy(IdentityStrategy.KEEP_IDS);
        TenantMobilityJob job = createAndSaveJob(accountId, null, TenantJobType.RESTORE);
        launchImportJob(job, savedFile, options);
        return toDto(job);
    }

    @Override
    public TenantMobilityJobDto createCloneJob(TenantCloneOptions options) {
        TenantMobilityJob job = createAndSaveJob(
                options.getSourceAccountId(), options.getTargetAccountId(), TenantJobType.CLONE);
        launchCloneJob(job, options);
        return toDto(job);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Job query
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public TenantMobilityJobDto getJob(String jobUuid) {
        TenantMobilityJob job = findByUuid(jobUuid);
        return job != null ? toDto(job) : null;
    }

    @Override
    public TenantMobilityJob getJobEntity(String jobUuid) {
        return findByUuid(jobUuid);
    }

    @Override
    public List<TenantMobilityJobDto> listJobs(Long accountId) {
        QueryParameters qp = new QueryParameters()
                .orderBy("createdAt", false);
        if (accountId != null) {
            qp.add("accountId", accountId);
        }
        return crudService.find(TenantMobilityJob.class, qp)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelJob(String jobUuid) {
        CancellationToken token = activeTokens.get(jobUuid);
        if (token != null) {
            token.cancel("Cancelled by user request");
            log.info("[Migration/Jobs] Cancellation requested for job {}", jobUuid);
        } else {
            log.warn("[Migration/Jobs] No active token found for job {} (already finished?)", jobUuid);
        }
        // Optimistically update status in DB
        TenantMobilityJob job = findByUuid(jobUuid);
        if (job != null && !job.isFinished()) {
            crudService.executeWithinTransaction(() -> {
                TenantMobilityJob j = crudService.find(TenantMobilityJob.class, job.getId());
                if (j != null && !j.isFinished()) {
                    j.markCancelled("Cancellation requested");
                    crudService.update(j);
                }
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Worker launchers
    // ─────────────────────────────────────────────────────────────────────────

    private void launchExportJob(TenantMobilityJob job, Long accountId, TenantExportOptions options) {
        CancellationToken token = CancellationToken.active();
        activeTokens.put(job.getUuid(), token);

        Path outputFile = buildOutputPath(job, options.isCompressionEnabled());

        ExportWorker worker = new ExportWorker(
                accountId, outputFile, options, mobilityService,
                buildProgressListener(job), token);

        SchedulerUtil.runWithResult(worker).whenComplete((result, ex) -> {
            activeTokens.remove(job.getUuid());
            finalizeJob(job.getUuid(), ex, outputFile, token);
        });
    }

    private void launchImportJob(TenantMobilityJob job, Path inputFile, TenantImportOptions options) {
        CancellationToken token = CancellationToken.active();
        activeTokens.put(job.getUuid(), token);

        ImportWorker worker = new ImportWorker(
                inputFile, options, mobilityService,
                buildProgressListener(job), token);

        SchedulerUtil.runWithResult(worker).whenComplete((result, ex) -> {
            activeTokens.remove(job.getUuid());
            finalizeJob(job.getUuid(), ex, null, token);
            // Clean up uploaded file after import
            try {
                Files.deleteIfExists(inputFile);
            } catch (IOException ignored) {
            }
        });
    }

    private void launchCloneJob(TenantMobilityJob job, TenantCloneOptions options) {
        CancellationToken token = CancellationToken.active();
        activeTokens.put(job.getUuid(), token);

        CloneWorker worker = new CloneWorker(
                options, mobilityService,
                buildProgressListener(job), token);

        SchedulerUtil.runWithResult(worker).whenComplete((result, ex) -> {
            activeTokens.remove(job.getUuid());
            finalizeJob(job.getUuid(), ex, null, token);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private TenantMobilityJob createAndSaveJob(Long accountId, Long targetAccountId, TenantJobType type) {
        TenantMobilityJob job = new TenantMobilityJob();
        job.setAccountId(accountId);
        job.setTargetAccountId(targetAccountId);
        job.setJobType(type);
        job.setStatus(TenantJobStatus.PENDING);
        crudService.create(job);
        log.info("[Migration/Jobs] Created job {} type={} account={}", job.getUuid(), type, accountId);
        return job;
    }

    private void markRunning(String jobUuid) {
        crudService.executeWithinTransaction(() -> {
            TenantMobilityJob job = findByUuid(jobUuid);
            if (job != null) {
                job.markRunning();
                crudService.update(job);
            }
        });
    }

    private void finalizeJob(String jobUuid, Throwable ex, Path resultFile, CancellationToken token) {
        crudService.executeWithinTransaction(() -> {
            TenantMobilityJob job = findByUuid(jobUuid);
            if (job == null) return;

            if (ex != null) {
                job.markFailed(ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
                log.error("[Migration/Jobs] Job {} FAILED: {}", jobUuid, ex.getMessage());
            } else if (token != null && token.isCancelled()) {
                job.markCancelled(token.getReason());
                log.info("[Migration/Jobs] Job {} CANCELLED: {}", jobUuid, token.getReason());
            } else {
                job.markCompleted();
                if (resultFile != null) {
                    job.setResultPath(resultFile.toAbsolutePath().toString());
                }
                log.info("[Migration/Jobs] Job {} COMPLETED", jobUuid);
            }
            crudService.update(job);
        });
    }

    private tools.dynamia.modules.saas.migration.api.MigrationProgressListener buildProgressListener(
            TenantMobilityJob job) {
        // Mark the job as RUNNING on first progress event, then persist progress updates
        final boolean[] started = {false};
        return (MigrationProgress p) -> {
            if (!started[0]) {
                started[0] = true;
                markRunning(job.getUuid());
            }
            try {
                crudService.executeWithinTransaction(() -> {
                    TenantMobilityJob j = findByUuid(job.getUuid());
                    if (j != null && !j.isFinished()) {
                        j.updateProgress(p.percentage() >= 0 ? p.percentage() : j.getProgress(),
                                p.message());
                        crudService.update(j);
                    }
                });
            } catch (Exception e) {
                log.debug("[Migration/Jobs] Progress update error for {}: {}", job.getUuid(), e.getMessage());
            }
        };
    }

    private Path buildOutputPath(TenantMobilityJob job, boolean compressed) {
        String ts = LocalDateTime.now().format(FILE_TS);
        String fileName = "saas_export_" + job.getAccountId() + "_" + ts
                + (compressed ? ".json.gz" : ".json");
        return Paths.get(properties.getOutputDirectory(), fileName);
    }

    private Path saveUploadedFile(MultipartFile file, String prefix) {
        try {
            String ts = LocalDateTime.now().format(FILE_TS);
            String ext = file.getOriginalFilename() != null
                    && file.getOriginalFilename().endsWith(".gz") ? ".json.gz" : ".json";
            Path dest = Paths.get(properties.getOutputDirectory(), prefix + "_upload_" + ts + ext);
            Files.createDirectories(dest.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
            return dest;
        } catch (IOException e) {
            throw new tools.dynamia.modules.saas.migration.api.MigrationException(
                    "Failed to save uploaded file", e);
        }
    }

    private TenantMobilityJob findByUuid(String uuid) {
        return crudService.findSingle(TenantMobilityJob.class,
                QueryParameters.with("uuid", uuid));
    }

    private TenantMobilityJobDto toDto(TenantMobilityJob job) {
        String downloadUrl = null;
        if (job.getResultPath() != null) {
            downloadUrl = "/api/saas/migration/jobs/" + job.getUuid() + "/download";
        }
        return new TenantMobilityJobDto(
                job.getId(),
                job.getUuid(),
                job.getAccountId(),
                job.getTargetAccountId(),
                job.getJobType(),
                job.getStatus(),
                job.getProgress(),
                job.getProgressMessage(),
                job.getErrorMessage(),
                downloadUrl,
                job.getCreatedAt(),
                job.getStartedAt(),
                job.getFinishedAt()
        );
    }
}

