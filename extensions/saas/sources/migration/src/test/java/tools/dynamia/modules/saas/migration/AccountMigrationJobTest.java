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

import org.junit.Assert;
import org.junit.Test;
import tools.dynamia.modules.saas.migration.api.MigrationProgress;
import tools.dynamia.modules.saas.migration.domain.AccountJobStatus;
import tools.dynamia.modules.saas.migration.domain.AccountMigrationJob;

public class AccountMigrationJobTest {

    @Test
    public void newJobIsInPendingStatus() {
        AccountMigrationJob job = new AccountMigrationJob();
        Assert.assertEquals(AccountJobStatus.PENDING, job.getStatus());
    }

    @Test
    public void newJobIsNotFinished() {
        Assert.assertFalse(new AccountMigrationJob().isFinished());
    }

    @Test
    public void newJobHasUuid() {
        AccountMigrationJob job = new AccountMigrationJob();
        Assert.assertNotNull(job.getUuid());
        Assert.assertFalse(job.getUuid().isEmpty());
    }

    @Test
    public void markRunningTransitionsToRunning() {
        AccountMigrationJob job = new AccountMigrationJob();
        job.markRunning();

        Assert.assertEquals(AccountJobStatus.RUNNING, job.getStatus());
        Assert.assertNotNull(job.getStartedAt());
        Assert.assertFalse(job.isFinished());
    }

    @Test
    public void markCompletedSetsProgressTo100AndFinishedAt() {
        AccountMigrationJob job = new AccountMigrationJob();
        job.markRunning();
        job.markCompleted();

        Assert.assertEquals(AccountJobStatus.COMPLETED, job.getStatus());
        Assert.assertEquals(100, job.getProgress());
        Assert.assertNotNull(job.getFinishedAt());
        Assert.assertTrue(job.isFinished());
    }

    @Test
    public void markFailedStoresMessage() {
        AccountMigrationJob job = new AccountMigrationJob();
        job.markRunning();
        job.markFailed("DB connection lost");

        Assert.assertEquals(AccountJobStatus.FAILED, job.getStatus());
        Assert.assertEquals("DB connection lost", job.getErrorMessage());
        Assert.assertNotNull(job.getFinishedAt());
        Assert.assertTrue(job.isFinished());
    }

    @Test
    public void markCancelledStoresReason() {
        AccountMigrationJob job = new AccountMigrationJob();
        job.markRunning();
        job.markCancelled("User requested cancellation");

        Assert.assertEquals(AccountJobStatus.CANCELLED, job.getStatus());
        Assert.assertEquals("User requested cancellation", job.getProgressMessage());
        Assert.assertNotNull(job.getFinishedAt());
        Assert.assertTrue(job.isFinished());
    }

    @Test
    public void updateProgressClampsTo0_100Range() {
        AccountMigrationJob job = new AccountMigrationJob();

        job.updateProgress(MigrationProgress.of(-5L, 0L, "below zero", 0));
        Assert.assertEquals(0, job.getProgress());

        job.updateProgress(MigrationProgress.of(130, 5, "above hundred", 0));
        Assert.assertEquals(100, job.getProgress());

        job.updateProgress(MigrationProgress.of(42, 100, "normal", 0));
        Assert.assertEquals(42, job.getProgress());
        Assert.assertEquals("normal", job.getProgressMessage());
    }

    @Test
    public void twoJobsHaveDifferentUuids() {
        AccountMigrationJob a = new AccountMigrationJob();
        AccountMigrationJob b = new AccountMigrationJob();
        Assert.assertNotEquals(a.getUuid(), b.getUuid());
    }
}
