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
import tools.dynamia.modules.saas.migration.api.AccountCloneOptions;
import tools.dynamia.modules.saas.migration.api.AccountMigrationService;

/**
 * Background worker that executes a tenant clone operation
 * (source account → target account, same system).
 *
 * <p>Uses an in-memory {@code PipedOutputStream / PipedInputStream} bridge so
 * the export and import pipelines run sequentially without writing to disk.
 * For very large datasets, consider using {@link ExportWorker} followed by
 * {@link ImportWorker} with a temporary file to avoid memory pressure.
 *
 * <p>Submitted to {@code SchedulerUtil.runWithResult()} and runs on a virtual thread.
 *
 * @author Mario Serrano Leones
 */
public class CloneWorker extends TaskWithResult<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(CloneWorker.class);

    private final AccountCloneOptions options;
    private final AccountMigrationService mobilityService;
    private final MigrationProgressListener progressListener;
    private final CancellationToken cancellationToken;

    public CloneWorker(AccountCloneOptions options,
                       AccountMigrationService mobilityService,
                       MigrationProgressListener progressListener,
                       CancellationToken cancellationToken) {
        super("CloneWorker-" + options.getSourceAccountId() + "->" + options.getTargetAccountId());
        this.options = options;
        this.mobilityService = mobilityService;
        this.progressListener = progressListener;
        this.cancellationToken = cancellationToken;
    }

    @Override
    public Boolean doWorkWithResult() {
        log.info("[Migration/Worker] Starting CLONE {} → {}",
                options.getSourceAccountId(), options.getTargetAccountId());
        try {
            mobilityService.cloneTenant(options, progressListener, cancellationToken);
            if (cancellationToken != null && cancellationToken.isCancelled()) {
                log.info("[Migration/Worker] CLONE cancelled");
                return false;
            }
            log.info("[Migration/Worker] CLONE completed {} → {}",
                    options.getSourceAccountId(), options.getTargetAccountId());
            return true;
        } catch (Exception e) {
            log.error("[Migration/Worker] CLONE failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}

