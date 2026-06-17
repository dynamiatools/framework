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
package tools.dynamia.modules.saas.migration.services;

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.modules.saas.migration.api.AccountCloneOptions;
import tools.dynamia.modules.saas.migration.api.AccountExportOptions;
import tools.dynamia.modules.saas.migration.api.AccountImportOptions;
import tools.dynamia.modules.saas.migration.api.AccountMigrationService;
import tools.dynamia.modules.saas.migration.api.CancellationToken;
import tools.dynamia.modules.saas.migration.api.MigrationException;
import tools.dynamia.modules.saas.migration.api.MigrationProgressListener;
import tools.dynamia.modules.saas.migration.pipeline.ExportPipeline;
import tools.dynamia.modules.saas.migration.pipeline.ImportPipeline;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Default implementation of {@link AccountMigrationService}.
 *
 * <p>Delegates export to {@link ExportPipeline} and import to {@link ImportPipeline}.
 *
 * <p>Clone operations export to a temporary file on disk (instead of an in-memory buffer)
 * to handle tenants with hundreds of megabytes of data without risking OOM errors.
 * The temporary file is deleted once the import phase completes.
 *
 * @author Mario Serrano Leones
 */
@Service
public class AccountMigrationServiceImpl implements AccountMigrationService {

    private static final LoggingService log = LoggingService.get(AccountMigrationServiceImpl.class);

    private final ExportPipeline exportPipeline;
    private final ImportPipeline importPipeline;

    public AccountMigrationServiceImpl(ExportPipeline exportPipeline,
                                       ImportPipeline importPipeline) {
        this.exportPipeline = exportPipeline;
        this.importPipeline = importPipeline;
    }

    @Override
    public void exportTenant(Serializable accountId,
                             OutputStream output,
                             AccountExportOptions options,
                             MigrationProgressListener listener,
                             CancellationToken token) {
        log.info("[Migration] Starting export for accountId={}", accountId);
        exportPipeline.export(accountId, output, options, listener, token);
        log.info("[Migration] Export complete for accountId={}", accountId);
    }

    @Override
    public void importTenant(InputStream input,
                             AccountImportOptions options,
                             MigrationProgressListener listener,
                             CancellationToken token) {
        log.info("[Migration] Starting import for targetAccountId={}", options.getTargetAccountId());
        importPipeline.importTenant(input, options, listener, token);
        log.info("[Migration] Import complete for targetAccountId={}", options.getTargetAccountId());
    }

    @Override
    public void cloneTenant(AccountCloneOptions options,
                            MigrationProgressListener listener,
                            CancellationToken token) {
        Serializable source = options.getSourceAccountId();
        Serializable target = options.getTargetAccountId();
        log.info("[Migration] Starting clone {} → {}", source, target);

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("saas-clone-" + source + "-", ".zip");

            // ── Phase 1: Export to temp file ──────────────────────────────────
            AccountExportOptions exportOptions = new AccountExportOptions()
                    .chunkSize(options.getChunkSize())
                    .identityStrategy(options.getIdentityStrategy())
                    .label("clone-" + source + "->" + target);

            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(tempFile))) {
                exportPipeline.export(source, out, exportOptions, progress -> {
                    if (listener != null) listener.onProgress(progress);
                }, token);
            }

            if (token != null && token.isCancelled()) {
                log.info("[Migration] Clone cancelled after export phase");
                return;
            }

            log.debug("[Migration] Clone temp file size: {} bytes", Files.size(tempFile));

            // ── Phase 2: Import from temp file ────────────────────────────────
            AccountImportOptions importOptions = new AccountImportOptions()
                    .targetAccountId(target)
                    .chunkSize(options.getChunkSize())
                    .identityStrategy(options.getIdentityStrategy())
                    .failOnEntityError(options.isFailOnEntityError());

            try (InputStream in = new BufferedInputStream(Files.newInputStream(tempFile))) {
                importPipeline.importTenant(in, importOptions, listener, token);
            }

            log.info("[Migration] Clone complete {} → {}", source, target);

        } catch (IOException e) {
            throw new MigrationException("Clone operation failed", e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }
}
