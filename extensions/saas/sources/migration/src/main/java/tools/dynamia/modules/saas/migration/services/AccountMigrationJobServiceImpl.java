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

import org.springframework.core.io.InputStreamSource;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.modules.saas.migration.api.MigrationProgressListener;
import tools.dynamia.navigation.Page;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.multipart.MultipartFile;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.scheduling.SchedulerUtil;
import tools.dynamia.integration.scheduling.TaskWithResult;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.modules.entityfile.StoredEntityFile;
import tools.dynamia.modules.entityfile.UploadedFileInfo;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.service.EntityFileService;
import tools.dynamia.modules.saas.migration.api.CancellationToken;
import tools.dynamia.modules.saas.migration.api.IdentityStrategy;
import tools.dynamia.modules.saas.migration.api.MigrationProgress;
import tools.dynamia.modules.saas.migration.api.AccountCloneOptions;
import tools.dynamia.modules.saas.migration.api.AccountExportOptions;
import tools.dynamia.modules.saas.migration.api.AccountImportOptions;
import tools.dynamia.modules.saas.migration.api.AccountMigrationJobDto;
import tools.dynamia.modules.saas.migration.api.AccountMigrationJobService;
import tools.dynamia.modules.saas.migration.api.AccountMigrationService;
import tools.dynamia.modules.saas.migration.config.AccountMigrationProperties;
import tools.dynamia.modules.saas.migration.domain.AccountJobStatus;
import tools.dynamia.modules.saas.migration.domain.AccountJobType;
import tools.dynamia.modules.saas.migration.domain.AccountMigrationJob;
import tools.dynamia.modules.saas.migration.workers.CloneWorker;
import tools.dynamia.modules.saas.migration.workers.ExportWorker;
import tools.dynamia.modules.saas.migration.workers.ImportWorker;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link AccountMigrationJobService}.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Persist {@link AccountMigrationJob} records in the database.</li>
 *   <li>Launch worker tasks via {@link SchedulerUtil#runWithResult(tools.dynamia.integration.scheduling.TaskWithResult)}
 *       on virtual threads.</li>
 *   <li>Update job status, progress and result path as the worker executes.</li>
 *   <li>Maintain an in-memory {@link CancellationToken} registry so running jobs can be cancelled.</li>
 * </ol>
 *
 * @author Mario Serrano Leones
 */
@Service
public class AccountMigrationJobServiceImpl implements AccountMigrationJobService {

    private static final LoggingService log = LoggingService.get(AccountMigrationJobServiceImpl.class);
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final long PROGRESS_THROTTLE_MS = 8000;

    /**
     * In-memory token registry: jobUuid → CancellationToken. Cleaned up when job finishes.
     */
    private final Map<String, CancellationToken> activeTokens = new ConcurrentHashMap<>();

    private final CrudService crudService;
    private final AccountMigrationService migrationService;
    private final AccountMigrationProperties properties;
    private final ObjectMapper objectMapper;
    private final EntityFileService entityFileService;
    private final Semaphore concurrencyLimit;

    public AccountMigrationJobServiceImpl(CrudService crudService,
                                          AccountMigrationService migrationService,
                                          AccountMigrationProperties properties,
                                          @Qualifier("migrationObjectMapper") ObjectMapper objectMapper,
                                          EntityFileService entityFileService) {
        this.crudService = crudService;
        this.migrationService = migrationService;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.entityFileService = entityFileService;
        this.concurrencyLimit = new Semaphore(Math.max(1, properties.getMaxConcurrentJobs()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Job creation
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public AccountMigrationJobDto createExportJob(Serializable accountId, AccountExportOptions options) {
        AccountMigrationJob job = createAndSaveJob(accountId, null, AccountJobType.EXPORT, options);
        launchExportJob(job, accountId, options);
        return toDto(job);
    }

    @Override
    public AccountMigrationJobDto createBackupJob(Serializable accountId) {
        AccountExportOptions options = new AccountExportOptions()
                .label("backup");
        AccountMigrationJob job = createAndSaveJob(accountId, null, AccountJobType.BACKUP, options);
        launchExportJob(job, accountId, options);
        return toDto(job);
    }

    @Override
    public AccountMigrationJobDto createImportJob(InputStreamSource file, AccountImportOptions options) {
        Path savedFile = saveUploadedFile(file, "import");
        AccountMigrationJob job = createAndSaveJob(options.getTargetAccountId(), null, AccountJobType.IMPORT, options);
        launchImportJob(job, savedFile, options);
        return toDto(job);
    }

    @Override
    public AccountMigrationJobDto createRestoreJob(Serializable accountId, MultipartFile file) {
        Path savedFile = saveUploadedFile(file, "restore");
        AccountImportOptions options = new AccountImportOptions()
                .targetAccountId(accountId)
                .identityStrategy(IdentityStrategy.KEEP_IDS);
        AccountMigrationJob job = createAndSaveJob(accountId, null, AccountJobType.RESTORE, options);
        launchImportJob(job, savedFile, options);
        return toDto(job);
    }

    @Override
    public AccountMigrationJobDto createCloneJob(AccountCloneOptions options) {
        AccountMigrationJob job = createAndSaveJob(
                options.getSourceAccountId(), options.getTargetAccountId(), AccountJobType.CLONE, options);
        launchCloneJob(job, options);
        return toDto(job);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Job query
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public AccountMigrationJobDto getJob(String jobUuid) {
        AccountMigrationJob job = findByUuid(jobUuid);
        return job != null ? toDto(job) : null;
    }

    @Override
    public AccountMigrationJob getJobEntity(String jobUuid) {
        return findByUuid(jobUuid);
    }

    @Override
    public List<AccountMigrationJobDto> listJobs(Serializable accountId) {
        QueryParameters qp = new QueryParameters()
                .orderBy("createdAt", false);
        if (accountId != null) {
            qp.add("accountId", accountId);
        }
        return crudService.find(AccountMigrationJob.class, qp)
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
        AccountMigrationJob job = findByUuid(jobUuid);
        if (job != null && !job.isFinished()) {
            crudService.executeWithinTransaction(() -> {
                AccountMigrationJob j = crudService.find(AccountMigrationJob.class, job.getId());
                if (j != null && !j.isFinished()) {
                    j.markCancelled("Cancellation requested");
                    crudService.update(j);
                }
            });
        }
    }

    @Override
    public StoredEntityFile downloadResult(String jobUuid) {
        AccountMigrationJob job = findByUuid(jobUuid);
        if (job == null || job.getResultFile() == null) {
            return null;
        }
        return entityFileService.download(job.getResultFile());
    }

    @Override
    public List<AccountMigrationJobDto> getLastJobs() {
        var jobs = crudService.findReadOnly(AccountMigrationJob.class, QueryParameters.with("status", QueryConditions.notEq(AccountJobStatus.DELETED))
                .setMaxResults(100)
                .orderBy("createdAt", false));
        return jobs.stream().map(this::toDto).toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Worker launchers
    // ─────────────────────────────────────────────────────────────────────────

    private void launchExportJob(AccountMigrationJob job, Serializable accountId, AccountExportOptions options) {
        CancellationToken token = CancellationToken.active();
        activeTokens.put(job.getUuid(), token);
        Path workFile = buildWorkFile(job);
        ExportWorker worker = new ExportWorker(
                accountId, workFile, options, migrationService,
                buildProgressListener(job), token);
        scheduleWorker(job, worker, workFile, null, token);
    }

    private void launchImportJob(AccountMigrationJob job, Path inputFile, AccountImportOptions options) {
        CancellationToken token = CancellationToken.active();
        activeTokens.put(job.getUuid(), token);
        ImportWorker worker = new ImportWorker(
                inputFile, options, migrationService,
                buildProgressListener(job), token);
        scheduleWorker(job, worker, null, inputFile, token);
    }

    private void launchCloneJob(AccountMigrationJob job, AccountCloneOptions options) {
        CancellationToken token = CancellationToken.active();
        activeTokens.put(job.getUuid(), token);
        CloneWorker worker = new CloneWorker(
                options, migrationService,
                buildProgressListener(job), token);
        scheduleWorker(job, worker, null, null, token);
    }

    /**
     * Submits {@code worker} to a virtual thread, enforcing the configured
     * {@link AccountMigrationProperties#getMaxConcurrentJobs()} limit via a semaphore.
     * Workers that cannot immediately acquire a slot park the virtual thread
     * (cheap) until a running job finishes.
     *
     * @param exportedZipFile local working ZIP written by an {@code ExportWorker}; on success it is
     *                        uploaded to {@link EntityFileService} and always deleted afterward (may be null)
     * @param cleanupPath     deleted after the job completes, never uploaded (uploaded import/restore
     *                        input files; may be null)
     */
    private void scheduleWorker(AccountMigrationJob job,
                                TaskWithResult<Boolean> worker,
                                Path exportedZipFile,
                                Path cleanupPath,
                                CancellationToken token) {
        SchedulerUtil.runWithResult(new TaskWithResult<Boolean>(worker.getName() + "#queued") {
            @Override
            public Boolean doWorkWithResult() {
                try {
                    concurrencyLimit.acquire();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted waiting for migration concurrency slot");
                }
                try {
                    return worker.doWorkWithResult();
                } finally {
                    concurrencyLimit.release();
                }
            }
        }).whenComplete((result, ex) -> {
            activeTokens.remove(job.getUuid());
            finalizeJob(job.getUuid(), ex, exportedZipFile, token);
            if (cleanupPath != null) {
                try {
                    Files.deleteIfExists(cleanupPath);
                } catch (IOException ignored) {
                }
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private AccountMigrationJob createAndSaveJob(Serializable accountId, Serializable targetAccountId,
                                                 AccountJobType type, Object options) {
        AccountMigrationJob job = new AccountMigrationJob();
        job.accountId(accountId);
        job.targetAccountId(targetAccountId);
        job.setJobType(type);
        job.setStatus(AccountJobStatus.PENDING);
        if (options != null) {
            try {
                job.setOptionsJson(objectMapper.writeValueAsString(options));
            } catch (Exception e) {
                log.debug("[Migration/Jobs] Could not serialize options for {} job: {}", type, e.getMessage());
            }
        }
        crudService.create(job);
        log.info("[Migration/Jobs] Created job {} type={} account={}", job.getUuid(), type, accountId);
        return job;
    }

    private void markRunning(String jobUuid) {
        crudService.executeWithinTransaction(() -> {
            AccountMigrationJob job = findByUuid(jobUuid);
            if (job != null) {
                job.markRunning();
                crudService.update(job);
            }
        });
    }

    /**
     * Finalizes a job after its worker completes.
     *
     * <p>On success, {@code exportedZipFile} (a local working file — never the durable artifact)
     * is uploaded to {@link EntityFileService} <strong>before</strong> the job status transitions
     * to COMPLETED, so a job never reports success without a persisted, downloadable result.
     * The local file is always deleted afterward, regardless of outcome, so nothing is left
     * behind on local/container disk.
     */
    private void finalizeJob(String jobUuid, Throwable ex, Path exportedZipFile, CancellationToken token) {
        boolean cancelled = token != null && token.isCancelled();
        EntityFile uploadedResult = null;
        String uploadError = null;

        if (ex == null && !cancelled && exportedZipFile != null) {
            try {
                uploadedResult = persistResultFile(jobUuid, exportedZipFile);
            } catch (Exception uploadEx) {
                uploadError = uploadEx.getMessage() != null ? uploadEx.getMessage() : uploadEx.getClass().getSimpleName();
                log.error("[Migration/Jobs] Job {} failed to persist result file: {}", jobUuid, uploadError, uploadEx);
            }
        }

        EntityFile finalResult = uploadedResult;
        String finalUploadError = uploadError;
        crudService.executeWithinTransaction(() -> {
            AccountMigrationJob job = findByUuid(jobUuid);
            if (job == null) return;

            if (ex != null) {
                job.markFailed(ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
                log.error("[Migration/Jobs] Job {} FAILED: {}", jobUuid, ex.getMessage());
            } else if (cancelled) {
                job.markCancelled(token.getReason());
                log.info("[Migration/Jobs] Job {} CANCELLED: {}", jobUuid, token.getReason());
            } else if (finalUploadError != null) {
                job.markFailed("Result file could not be persisted: " + finalUploadError);
            } else {
                job.markCompleted();
                if (finalResult != null) {
                    job.setResultFile(finalResult);
                }
                log.info("[Migration/Jobs] Job {} COMPLETED", jobUuid);
            }
            crudService.update(job);
        });

        if (exportedZipFile != null) {
            try {
                Files.deleteIfExists(exportedZipFile);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Uploads the finished export/backup ZIP to {@link EntityFileService}, associating it
     * with the {@link AccountMigrationJob} itself so it can be resolved later for download.
     */
    private EntityFile persistResultFile(String jobUuid, Path zipFile) throws IOException {
        AccountMigrationJob job = findByUuid(jobUuid);
        if (job == null) {
            throw new IOException("Job " + jobUuid + " no longer exists");
        }

        UploadedFileInfo fileInfo = new UploadedFileInfo(zipFile);
        fileInfo.setContentType("application/zip");
        try {
            fileInfo.setAccountId(Long.valueOf(job.getAccountId()));
        } catch (NumberFormatException | NullPointerException ignored) {
            // non-numeric/absent account id (e.g. UUID tenants) — storage falls back to the current account provider
        }

        return entityFileService.createEntityFile(fileInfo, job,
                "Account migration " + job.getJobType() + " result for account " + job.getAccountId());
    }

    private MigrationProgressListener buildProgressListener(AccountMigrationJob job) {
        AtomicBoolean started = new AtomicBoolean(false);
        AtomicLong lastPersistedAt = new AtomicLong(0);
        AtomicLong partialSum = new AtomicLong(0);

        return (MigrationProgress p) -> {
            boolean isFirst = started.compareAndSet(false, true);

            if (isFirst || !p.partial()) {
                try {
                    crudService.executeWithinTransaction(() -> {
                        AccountMigrationJob j = findByUuid(job.getUuid());
                        if (j != null && !j.isFinished()) {
                            Map<String, Object> fields = new HashMap<>();
                            if (isFirst) {
                                j.markRunning();
                                fields.put("status", j.getStatus());
                                fields.put("startedAt", j.getStartedAt());
                            }

                            fields.put("progress", p.percentage());
                            fields.put("progressMessage", p.message());
                            fields.put("records", p.processedRecords());
                            crudService.batchUpdate(AccountMigrationJob.class, fields, QueryParameters.with("id", job.getId()));
                        }
                    });
                } catch (Exception e) {
                    log.debug("[Migration/Jobs] Progress update error for {}: {}", job.getUuid(), e.getMessage());
                }
            } else {
                long now = System.currentTimeMillis();
                boolean throttleExpired = (now - lastPersistedAt.get()) >= PROGRESS_THROTTLE_MS;
                boolean isFinal = p.totalEntities() > 0 && p.processedEntities() >= p.totalEntities();
                partialSum.addAndGet(p.processedRecords());

                if (!throttleExpired && !isFinal) return;

                lastPersistedAt.set(now);
                try {
                    var sum = partialSum.getAndSet(0);
                    crudService.executeWithinTransaction(() -> {
                        crudService.execute("UPDATE AccountMigrationJob j SET j.records = j.records + :partial where j.id = :id", QueryParameters.with("id", job.getId())
                                .add("partial", sum));
                    });

                } catch (Exception e) {
                    log.debug("[Migration/Jobs] Progress update error for {}: {}", job.getUuid(), e.getMessage());
                }
            }
        };
    }

    /**
     * Builds the path of the local, transient working file an {@link ExportWorker} streams
     * the ZIP into while an export/backup job runs. This file is never the durable artifact —
     * once the pipeline finishes, {@link #finalizeJob} uploads it to {@link EntityFileService}
     * and deletes it, so it never lingers on local/container disk.
     */
    private Path buildWorkFile(AccountMigrationJob job) {
        String ts = LocalDateTime.now().format(FILE_TS);
        String fileName = "Account" + job.getAccountId() + "_" + ts + ".zip";
        try {
            Files.createDirectories(Paths.get(properties.getOutputDirectory()));
        } catch (IOException ignore) {

        }
        return Paths.get(properties.getOutputDirectory(), fileName);
    }

    private Path saveUploadedFile(InputStreamSource file, String prefix) {
        try {
            String ts = LocalDateTime.now().format(FILE_TS);
            Path dest = Paths.get(properties.getOutputDirectory(), prefix + "_upload_" + ts + ".zip");
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

    private AccountMigrationJob findByUuid(String uuid) {
        return crudService.findSingle(AccountMigrationJob.class,
                QueryParameters.with("uuid", QueryConditions.eq(uuid)));
    }

    private AccountMigrationJobDto toDto(AccountMigrationJob job) {
        String downloadUrl = null;
        if (job.getResultFile() != null) {
            downloadUrl = "/api/saas/migration/jobs/" + job.getUuid() + "/download";
        }
        return new AccountMigrationJobDto(
                job.getId(),
                job.getUuid(),
                job.getAccountId(),
                job.getTargetAccountId(),
                job.getJobType(),
                job.getStatus(),
                job.getProgress(),
                job.getProgressMessage(),
                job.getRecords(),
                job.getErrorMessage(),
                downloadUrl,
                job.getCreatedAt(),
                job.getStartedAt(),
                job.getFinishedAt()
        );
    }
}

