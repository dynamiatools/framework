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

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.dynamia.modules.saas.migration.domain.AccountJobStatus;
import tools.dynamia.modules.saas.migration.domain.AccountJobType;
import tools.dynamia.modules.saas.migration.domain.AccountMigrationJob;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Read-only DTO representing the state of a {@link AccountMigrationJob}.
 * Returned by REST endpoints.
 *
 * @author Mario Serrano Leones
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountMigrationJobDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private  Long id;
    private  String uuid;
    private  Serializable accountId;
    private  Serializable targetAccountId;
    private  AccountJobType jobType;
    private  AccountJobStatus status;
    private  int progress;
    private  String progressMessage;
    private  long records;
    private  String errorMessage;
    private  String downloadUrl;
    private  LocalDateTime createdAt;
    private  LocalDateTime startedAt;
    private  LocalDateTime finishedAt;

    public AccountMigrationJobDto() {
    }

    public AccountMigrationJobDto(Long id,
                                  String uuid,
                                  Serializable accountId,
                                  Serializable targetAccountId,
                                  AccountJobType jobType,
                                  AccountJobStatus status,
                                  int progress,
                                  String progressMessage,
                                  long records,
                                  String errorMessage,
                                  String downloadUrl,
                                  LocalDateTime createdAt,
                                  LocalDateTime startedAt,
                                  LocalDateTime finishedAt) {
        this.id = id;
        this.uuid = uuid;
        this.accountId = accountId;
        this.targetAccountId = targetAccountId;
        this.jobType = jobType;
        this.status = status;
        this.progress = progress;
        this.progressMessage = progressMessage;
        this.records = records;
        this.errorMessage = errorMessage;
        this.downloadUrl = downloadUrl;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    /**
     * Convenience: returns {@code true} when the job has reached a terminal state.
     */
    public boolean isFinished() {
        return status == AccountJobStatus.COMPLETED
                || status == AccountJobStatus.FAILED
                || status == AccountJobStatus.CANCELLED;
    }

    public Duration getDuration() {
        if (startedAt != null && finishedAt != null) {
            return Duration.between(startedAt, finishedAt);
        }
        return null;
    }

    public Serializable getTargetAccountId() {
        return targetAccountId;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getProgress() {
        return progress;
    }

    public AccountJobStatus getStatus() {
        return status;
    }

    public AccountJobType getJobType() {
        return jobType;
    }

    public Serializable getAccountId() {
        return accountId;
    }

    public String getUuid() {
        return uuid;
    }

    public Long getId() {
        return id;
    }

    public String getProgressMessage() {
        return progressMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public long getRecords() {
        return records;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setAccountId(Serializable accountId) {
        this.accountId = accountId;
    }

    public void setTargetAccountId(Serializable targetAccountId) {
        this.targetAccountId = targetAccountId;
    }

    public void setJobType(AccountJobType jobType) {
        this.jobType = jobType;
    }

    public void setStatus(AccountJobStatus status) {
        this.status = status;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setProgressMessage(String progressMessage) {
        this.progressMessage = progressMessage;
    }

    public void setRecords(long records) {
        this.records = records;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    @Override
    public String toString() {
        return "AccountMigrationJobDto{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", accountId=" + accountId +
                ", targetAccountId=" + targetAccountId +
                ", jobType=" + jobType +
                ", status=" + status +
                ", progress=" + progress +
                ", progressMessage='" + progressMessage + '\'' +
                ", records=" + records +
                ", errorMessage='" + errorMessage + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", createdAt=" + createdAt +
                ", startedAt=" + startedAt +
                ", finishedAt=" + finishedAt +
                '}';
    }

}

