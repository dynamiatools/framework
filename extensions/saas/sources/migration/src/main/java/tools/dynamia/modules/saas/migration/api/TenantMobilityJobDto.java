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

import tools.dynamia.modules.saas.migration.domain.TenantJobStatus;
import tools.dynamia.modules.saas.migration.domain.TenantJobType;

import java.time.LocalDateTime;

/**
 * Read-only DTO representing the state of a {@link tools.dynamia.modules.saas.migration.domain.TenantMobilityJob}.
 * Returned by REST endpoints.
 *
 * @author Mario Serrano Leones
 */
public record TenantMobilityJobDto(
        Long id,
        String uuid,
        Long accountId,
        Long targetAccountId,
        TenantJobType jobType,
        TenantJobStatus status,
        int progress,
        String progressMessage,
        String errorMessage,
        String downloadUrl,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {

    /** Convenience: returns {@code true} when the job has reached a terminal state. */
    public boolean isFinished() {
        return status == TenantJobStatus.COMPLETED
                || status == TenantJobStatus.FAILED
                || status == TenantJobStatus.CANCELLED;
    }
}

