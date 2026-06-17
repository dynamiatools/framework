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

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.integration.scheduling.TaskWithResult;
import tools.dynamia.modules.saas.migration.api.CancellationToken;
import tools.dynamia.modules.saas.migration.api.MigrationProgressListener;
import tools.dynamia.modules.saas.migration.api.AccountExportOptions;
import tools.dynamia.modules.saas.migration.api.AccountMigrationService;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * Background worker that executes a tenant export operation.
 *
 * <p>This task is submitted to {@code SchedulerUtil.runWithResult()} and runs
 * on a virtual thread. It calls {@link AccountMigrationService#exportTenant} and
 * writes the result to the file path provided by the job service.
 *
 * <p>Returns {@code true} on success, {@code false} on failure or cancellation.
 *
 * @author Mario Serrano Leones
 */
public class ExportWorker extends TaskWithResult<Boolean> {

    private static final LoggingService log = LoggingService.get(ExportWorker.class);

    private final Long accountId;
    private final Path outputFile;
    private final AccountExportOptions options;
    private final AccountMigrationService migrationService;
    private final MigrationProgressListener progressListener;
    private final CancellationToken cancellationToken;

    public ExportWorker(Long accountId,
                        Path outputFile,
                        AccountExportOptions options,
                        AccountMigrationService migrationService,
                        MigrationProgressListener progressListener,
                        CancellationToken cancellationToken) {
        super("ExportWorker-account-" + accountId);
        this.accountId = accountId;
        this.outputFile = outputFile;
        this.options = options;
        this.migrationService = migrationService;
        this.progressListener = progressListener;
        this.cancellationToken = cancellationToken;
    }

    @Override
    public Boolean doWorkWithResult() {
        log.info("[Migration/Worker] Starting EXPORT for accountId={} → {}", accountId, outputFile);
        try (OutputStream out = new FileOutputStream(outputFile.toFile())) {
            migrationService.exportTenant(accountId, out, options, progressListener, cancellationToken);
            if (cancellationToken != null && cancellationToken.isCancelled()) {
                log.info("[Migration/Worker] EXPORT cancelled for accountId={}", accountId);
                return false;
            }
            log.info("[Migration/Worker] EXPORT completed for accountId={}", accountId);
            return true;
        } catch (Exception e) {
            log.error("[Migration/Worker] EXPORT failed for accountId={}: {}", accountId, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}

