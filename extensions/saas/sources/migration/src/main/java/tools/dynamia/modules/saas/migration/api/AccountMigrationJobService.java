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
package tools.dynamia.modules.saas.migration.api;

import org.springframework.web.multipart.MultipartFile;
import tools.dynamia.modules.saas.migration.domain.AccountMigrationJob;

import java.util.List;

/**
 * Async job management service for tenant mobility operations.
 *
 * <p>Each method creates a {@link AccountMigrationJob} record, launches the operation
 * as a background virtual thread via {@code SchedulerUtil.runWithResult()}, and returns
 * the job DTO immediately (non-blocking).
 *
 * <pre>{@code
 * TenantMobilityJobDto job = jobService.createExportJob(42L, new TenantExportOptions());
 * // poll status:
 * TenantMobilityJobDto status = jobService.getJob(job.uuid());
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public interface AccountMigrationJobService {

    /**
     * Starts an async export job for the given account.
     *
     * @param accountId ID of the account to export
     * @param options   export configuration
     * @return the newly created (PENDING) job
     */
    AccountMigrationJobDto createExportJob(Long accountId, AccountExportOptions options);

    /**
     * Starts an async import job from an uploaded file.
     *
     * @param file    multipart upload containing the export JSON (or .json.gz)
     * @param options import configuration (target account, identity strategy, etc.)
     * @return the newly created (PENDING) job
     */
    AccountMigrationJobDto createImportJob(MultipartFile file, AccountImportOptions options);

    /**
     * Starts an async clone job (source tenant → target tenant, same system).
     *
     * @param options clone configuration
     * @return the newly created (PENDING) job
     */
    AccountMigrationJobDto createCloneJob(AccountCloneOptions options);

    /**
     * Starts an async backup job (semantically equivalent to export with BACKUP type label).
     *
     * @param accountId ID of the account to back up
     * @return the newly created (PENDING) job
     */
    AccountMigrationJobDto createBackupJob(Long accountId);

    /**
     * Starts an async restore job from an uploaded file
     * (semantically equivalent to import with RESTORE type label).
     *
     * @param accountId target account to restore into
     * @param file      multipart upload
     * @return the newly created (PENDING) job
     */
    AccountMigrationJobDto createRestoreJob(Long accountId, MultipartFile file);

    /**
     * Returns the current state of the job identified by {@code jobUuid}.
     *
     * @param jobUuid UUID of the job
     * @return job DTO or {@code null} if not found
     */
    AccountMigrationJobDto getJob(String jobUuid);

    /**
     * Returns the raw {@link AccountMigrationJob} entity for internal use (e.g. file download).
     *
     * @param jobUuid UUID of the job
     * @return entity or {@code null}
     */
    AccountMigrationJob getJobEntity(String jobUuid);

    /**
     * Lists all known jobs, optionally filtered by account.
     *
     * @param accountId filter by account; pass {@code null} to return all jobs
     * @return list of jobs ordered by creation date descending
     */
    List<AccountMigrationJobDto> listJobs(Long accountId);

    /**
     * Requests cooperative cancellation of a running job.
     * <p>
     * The pipeline will stop at the next chunk boundary. If the job is already
     * finished (COMPLETED/FAILED/CANCELLED), this is a no-op.
     *
     * @param jobUuid UUID of the job to cancel
     */
    void cancelJob(String jobUuid);
}

