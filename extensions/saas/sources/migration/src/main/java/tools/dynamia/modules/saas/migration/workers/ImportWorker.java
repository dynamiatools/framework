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
package tools.dynamia.modules.saas.migration.workers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.dynamia.integration.scheduling.TaskWithResult;
import tools.dynamia.modules.saas.migration.api.CancellationToken;
import tools.dynamia.modules.saas.migration.api.MigrationProgressListener;
import tools.dynamia.modules.saas.migration.api.AccountImportOptions;
import tools.dynamia.modules.saas.migration.api.AccountMigrationService;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Background worker that executes a tenant import operation from a file on disk.
 *
 * <p>Submitted to {@code SchedulerUtil.runWithResult()} and runs on a virtual thread.
 * Returns {@code true} on success, {@code false} on cancellation, or throws
 * {@link RuntimeException} on failure.
 *
 * @author Mario Serrano Leones
 */
public class ImportWorker extends TaskWithResult<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(ImportWorker.class);

    private final Path inputFile;
    private final AccountImportOptions options;
    private final AccountMigrationService mobilityService;
    private final MigrationProgressListener progressListener;
    private final CancellationToken cancellationToken;

    public ImportWorker(Path inputFile,
                        AccountImportOptions options,
                        AccountMigrationService mobilityService,
                        MigrationProgressListener progressListener,
                        CancellationToken cancellationToken) {
        super("ImportWorker-account-" + options.getTargetAccountId());
        this.inputFile = inputFile;
        this.options = options;
        this.mobilityService = mobilityService;
        this.progressListener = progressListener;
        this.cancellationToken = cancellationToken;
    }

    @Override
    public Boolean doWorkWithResult() {
        log.info("[Migration/Worker] Starting IMPORT from {} → accountId={}",
                inputFile, options.getTargetAccountId());
        try (InputStream in = new BufferedInputStream(new FileInputStream(inputFile.toFile()))) {
            mobilityService.importTenant(in, options, progressListener, cancellationToken);
            if (cancellationToken != null && cancellationToken.isCancelled()) {
                log.info("[Migration/Worker] IMPORT cancelled");
                return false;
            }
            log.info("[Migration/Worker] IMPORT completed for accountId={}", options.getTargetAccountId());
            return true;
        } catch (Exception e) {
            log.error("[Migration/Worker] IMPORT failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}

