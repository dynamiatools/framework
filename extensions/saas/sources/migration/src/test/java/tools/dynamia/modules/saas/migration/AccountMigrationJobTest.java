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
package tools.dynamia.modules.saas.migration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.dynamia.modules.saas.migration.api.MigrationProgress;
import tools.dynamia.modules.saas.migration.domain.AccountJobStatus;
import tools.dynamia.modules.saas.migration.domain.AccountMigrationJob;

public class AccountMigrationJobTest {

    @Test
    public void newJobIsInPendingStatus() {
        AccountMigrationJob job = new AccountMigrationJob();
        Assertions.assertEquals(AccountJobStatus.PENDING, job.getStatus());
    }

    @Test
    public void newJobIsNotFinished() {
        Assertions.assertFalse(new AccountMigrationJob().isFinished());
    }

    @Test
    public void newJobHasUuid() {
        AccountMigrationJob job = new AccountMigrationJob();
        Assertions.assertNotNull(job.getUuid());
        Assertions.assertFalse(job.getUuid().isEmpty());
    }

    @Test
    public void markRunningTransitionsToRunning() {
        AccountMigrationJob job = new AccountMigrationJob();
        job.markRunning();

        Assertions.assertEquals(AccountJobStatus.RUNNING, job.getStatus());
        Assertions.assertNotNull(job.getStartedAt());
        Assertions.assertFalse(job.isFinished());
    }

    @Test
    public void markCompletedSetsProgressTo100AndFinishedAt() {
        AccountMigrationJob job = new AccountMigrationJob();
        job.markRunning();
        job.markCompleted();

        Assertions.assertEquals(AccountJobStatus.COMPLETED, job.getStatus());
        Assertions.assertEquals(100, job.getProgress());
        Assertions.assertNotNull(job.getFinishedAt());
        Assertions.assertTrue(job.isFinished());
    }

    @Test
    public void markFailedStoresMessage() {
        AccountMigrationJob job = new AccountMigrationJob();
        job.markRunning();
        job.markFailed("DB connection lost");

        Assertions.assertEquals(AccountJobStatus.FAILED, job.getStatus());
        Assertions.assertEquals("DB connection lost", job.getErrorMessage());
        Assertions.assertNotNull(job.getFinishedAt());
        Assertions.assertTrue(job.isFinished());
    }

    @Test
    public void markCancelledStoresReason() {
        AccountMigrationJob job = new AccountMigrationJob();
        job.markRunning();
        job.markCancelled("User requested cancellation");

        Assertions.assertEquals(AccountJobStatus.CANCELLED, job.getStatus());
        Assertions.assertEquals("User requested cancellation", job.getProgressMessage());
        Assertions.assertNotNull(job.getFinishedAt());
        Assertions.assertTrue(job.isFinished());
    }

    @Test
    public void updateProgressClampsTo0_100Range() {
        AccountMigrationJob job = new AccountMigrationJob();

        job.updateProgress(MigrationProgress.of(-5L, 0L, "below zero", 0));
        Assertions.assertEquals(0, job.getProgress());

        job.updateProgress(MigrationProgress.of(130, 5, "above hundred", 0));
        Assertions.assertEquals(100, job.getProgress());

        job.updateProgress(MigrationProgress.of(42, 100, "normal", 0));
        Assertions.assertEquals(42, job.getProgress());
        Assertions.assertEquals("normal", job.getProgressMessage());
    }

    @Test
    public void twoJobsHaveDifferentUuids() {
        AccountMigrationJob a = new AccountMigrationJob();
        AccountMigrationJob b = new AccountMigrationJob();
        Assertions.assertNotEquals(a.getUuid(), b.getUuid());
    }
}
