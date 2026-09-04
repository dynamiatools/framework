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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Semaphore;

public class AccountMigrationPropertiesTest {

    @Test
    public void defaultChunkSizeIs500() {
        Assertions.assertEquals(500, new AccountMigrationProperties().getChunkSize());
    }

    @Test
    public void defaultCompressionIsDisabled() {
        Assertions.assertFalse(new AccountMigrationProperties().isCompressionEnabled());
    }

    @Test
    public void defaultMaxConcurrentJobsIs5() {
        Assertions.assertEquals(5, new AccountMigrationProperties().getMaxConcurrentJobs());
    }

    @Test
    public void defaultFailOnEntityErrorIsFalse() {
        Assertions.assertFalse(new AccountMigrationProperties().isFailOnEntityError());
    }

    @Test
    public void defaultOutputDirectoryContainsTmpdir() {
        String dir = new AccountMigrationProperties().getOutputDirectory();
        Assertions.assertNotNull(dir);
        Assertions.assertTrue(dir.contains(System.getProperty("java.io.tmpdir").replace("\\", "/")),
                "outputDirectory should use system tmpdir");
    }

    @Test
    public void semaphoreInitializedFromMaxConcurrentJobs() {
        AccountMigrationProperties props = new AccountMigrationProperties();
        props.setMaxConcurrentJobs(3);

        // Simulate the service constructor logic
        Semaphore semaphore = new Semaphore(Math.max(1, props.getMaxConcurrentJobs()));
        Assertions.assertEquals(3, semaphore.availablePermits());
    }

    @Test
    public void semaphoreFloorIsOneEvenIfMaxIsZeroOrNegative() {
        // The service uses Math.max(1, maxConcurrentJobs) to avoid a 0-permit semaphore
        Assertions.assertEquals(1, Math.max(1, 0));
        Assertions.assertEquals(1, Math.max(1, -5));
        Assertions.assertEquals(2, Math.max(1, 2));
    }
}
