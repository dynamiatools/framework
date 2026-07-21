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
package tools.dynamia.modules.saas.migration.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.domain.jpa.SimpleEntity;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.saas.migration.api.AccountExportOptions;
import tools.dynamia.modules.saas.migration.api.AccountImportOptions;
import tools.dynamia.modules.saas.migration.api.MigrationProgress;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Persisted record of a tenant mobility operation (export, import, clone, backup, restore).
 *
 * <p>Each row tracks the full lifecycle: PENDING → RUNNING → COMPLETED / FAILED / CANCELLED.
 *
 * <p>This entity is NOT {@code AccountAware} intentionally — it is a system-level record
 * and must not be exported alongside tenant data.
 *
 * @author Mario Serrano Leones
 */
@Entity
@Table(name = "saas_migration_jobs", indexes = @Index(name = "idx_saas_migration_jobs_uuid", columnList = "uuid"))
@DynamicUpdate
public class AccountMigrationJob extends SimpleEntity {

    // ─── Identity ──────────────────────────────────────────────────────────────

    /**
     * Stable external identifier (URL-safe, used in REST paths).
     */
    @Column(nullable = false, unique = true, length = 64)
    private String uuid = StringUtils.randomString();

    // ─── Tenant references ─────────────────────────────────────────────────────

    /**
     * Source tenant account ID.
     */
    private String accountId;

    /**
     * Target tenant account ID (used for clone/restore operations).
     */
    private String targetAccountId;
    private String accountIdType;

    // ─── Classification ────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private AccountJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private AccountJobStatus status = AccountJobStatus.PENDING;

    // ─── Progress ──────────────────────────────────────────────────────────────

    /**
     * Completion percentage 0–100.
     */
    private int progress;
    private long records;

    @Column(length = 2000)
    private String progressMessage;

    // ─── Timestamps ────────────────────────────────────────────────────────────

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    // ─── Results & errors ──────────────────────────────────────────────────────

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Persisted result archive for EXPORT / BACKUP jobs.
     *
     * <p>Uploaded to {@link tools.dynamia.modules.entityfile.service.EntityFileService}
     * only after the export pipeline finishes successfully, so no intermediate ZIP is
     * ever left behind as a "final" artifact on local/container disk. Actual bytes live
     * wherever {@code EntityFileStorage} is configured (local safe directory, S3, Buckie, etc.).
     *
     * <p>Fetched eagerly (this is a single-row lookup, not a bulk listing) so
     * {@code getResultFile()} is always safe to call outside the loading transaction/session —
     * needed by {@code downloadResult()}, which runs after {@code findByUuid} returns.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "result_file_id")
    private EntityFile resultFile;

    /**
     * Serialized {@link AccountExportOptions}
     * or {@link AccountImportOptions} as JSON.
     */
    @Column(length = 4000)
    private String optionsJson;

    // ─── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Mark the job as started.
     */
    public void markRunning() {
        this.status = AccountJobStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * Mark the job as successfully completed.
     */
    public void markCompleted() {
        this.status = AccountJobStatus.COMPLETED;
        this.finishedAt = LocalDateTime.now();
        this.progress = 100;
    }

    /**
     * Mark the job as failed with an error message.
     */
    public void markFailed(String errorMessage) {
        this.status = AccountJobStatus.FAILED;
        this.finishedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    /**
     * Mark the job as cancelled.
     */
    public void markCancelled(String reason) {
        this.status = AccountJobStatus.CANCELLED;
        this.finishedAt = LocalDateTime.now();
        this.progressMessage = reason;
    }

    /**
     * Update running progress (0-100) and an optional human-readable message.
     */
    public void updateProgress(MigrationProgress progress) {

        if (progress.partial()) {
            this.records += progress.processedRecords();
        } else {
            this.progress = progress.percentage();
            this.progressMessage = StringUtils.truncate(progress.message(), 1999);
            this.records = progress.processedRecords();
        }

    }

    /**
     * Returns {@code true} if the job is in a terminal state.
     */
    public boolean isFinished() {
        return status == AccountJobStatus.COMPLETED
                || status == AccountJobStatus.FAILED
                || status == AccountJobStatus.CANCELLED;
    }

    // ─── Accessors ─────────────────────────────────────────────────────────────

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void accountId(Serializable accountId) {
        if (accountId != null) {
            this.accountId = String.valueOf(accountId);
            this.accountIdType = accountId.getClass().getName();
        } else {
            this.accountId = null;
            this.accountIdType = null;
        }
    }

    public String getTargetAccountId() {
        return targetAccountId;
    }

    public void setTargetAccountId(String targetAccountId) {
        this.targetAccountId = targetAccountId;
    }

    public void targetAccountId(Serializable targetAccountId) {
        if (targetAccountId != null) {
            this.targetAccountId = String.valueOf(targetAccountId);
        } else {
            this.targetAccountId = null;
        }
    }

    public String getAccountIdType() {
        return accountIdType;
    }

    public void setAccountIdType(String accountIdType) {
        this.accountIdType = accountIdType;
    }

    public AccountJobType getJobType() {
        return jobType;
    }

    public void setJobType(AccountJobType jobType) {
        this.jobType = jobType;
    }

    public AccountJobStatus getStatus() {
        return status;
    }

    public void setStatus(AccountJobStatus status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getProgressMessage() {
        return progressMessage;
    }

    public void setProgressMessage(String progressMessage) {
        this.progressMessage = progressMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public EntityFile getResultFile() {
        return resultFile;
    }

    public void setResultFile(EntityFile resultFile) {
        this.resultFile = resultFile;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public void setOptionsJson(String optionsJson) {
        this.optionsJson = optionsJson;
    }

    @Override
    public String toString() {
        return "TenantMobilityJob{uuid=" + uuid + ", type=" + jobType + ", status=" + status + "}";
    }

    public long getRecords() {
        return records;
    }

    public void setRecords(long records) {
        this.records = records;
    }
}

