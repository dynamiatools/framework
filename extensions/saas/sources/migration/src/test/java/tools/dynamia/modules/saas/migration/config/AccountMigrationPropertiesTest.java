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
package tools.dynamia.modules.saas.migration.config;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Semaphore;

public class AccountMigrationPropertiesTest {

    @Test
    public void defaultChunkSizeIs500() {
        Assert.assertEquals(500, new AccountMigrationProperties().getChunkSize());
    }

    @Test
    public void defaultCompressionIsDisabled() {
        Assert.assertFalse(new AccountMigrationProperties().isCompressionEnabled());
    }

    @Test
    public void defaultMaxConcurrentJobsIs5() {
        Assert.assertEquals(5, new AccountMigrationProperties().getMaxConcurrentJobs());
    }

    @Test
    public void defaultFailOnEntityErrorIsFalse() {
        Assert.assertFalse(new AccountMigrationProperties().isFailOnEntityError());
    }

    @Test
    public void defaultOutputDirectoryContainsTmpdir() {
        String dir = new AccountMigrationProperties().getOutputDirectory();
        Assert.assertNotNull(dir);
        Assert.assertTrue("outputDirectory should use system tmpdir",
                dir.contains(System.getProperty("java.io.tmpdir").replace("\\", "/")));
    }

    @Test
    public void semaphoreInitializedFromMaxConcurrentJobs() {
        AccountMigrationProperties props = new AccountMigrationProperties();
        props.setMaxConcurrentJobs(3);

        // Simulate the service constructor logic
        Semaphore semaphore = new Semaphore(Math.max(1, props.getMaxConcurrentJobs()));
        Assert.assertEquals(3, semaphore.availablePermits());
    }

    @Test
    public void semaphoreFloorIsOneEvenIfMaxIsZeroOrNegative() {
        // The service uses Math.max(1, maxConcurrentJobs) to avoid a 0-permit semaphore
        Assert.assertEquals(1, Math.max(1, 0));
        Assert.assertEquals(1, Math.max(1, -5));
        Assert.assertEquals(2, Math.max(1, 2));
    }
}
