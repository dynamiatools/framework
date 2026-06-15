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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.modules.saas.migration.api.CancellationToken;
import tools.dynamia.modules.saas.migration.api.MigrationProgressListener;
import tools.dynamia.modules.saas.migration.api.TenantCloneOptions;
import tools.dynamia.modules.saas.migration.api.TenantExportOptions;
import tools.dynamia.modules.saas.migration.api.TenantImportOptions;
import tools.dynamia.modules.saas.migration.api.TenantMobilityService;
import tools.dynamia.modules.saas.migration.pipeline.ExportPipeline;
import tools.dynamia.modules.saas.migration.pipeline.ImportPipeline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Default implementation of {@link TenantMobilityService}.
 *
 * <p>Delegates export to {@link ExportPipeline} and import to {@link ImportPipeline}.
 * For clone operations, the export is buffered in-memory ({@link ByteArrayOutputStream})
 * and then fed directly to the import pipeline — suitable for tenants with moderate
 * data volumes (< ~100 MB uncompressed). For massive tenants, prefer the
 * export-to-file + import-from-file job sequence.
 *
 * @author Mario Serrano Leones
 */
@Service
public class TenantMobilityServiceImpl implements TenantMobilityService {

    private static final Logger log = LoggerFactory.getLogger(TenantMobilityServiceImpl.class);

    private final ExportPipeline exportPipeline;
    private final ImportPipeline importPipeline;

    public TenantMobilityServiceImpl(ExportPipeline exportPipeline,
                                      ImportPipeline importPipeline) {
        this.exportPipeline = exportPipeline;
        this.importPipeline = importPipeline;
    }

    @Override
    public void exportTenant(Long accountId,
                              OutputStream output,
                              TenantExportOptions options,
                              MigrationProgressListener listener,
                              CancellationToken token) {
        log.info("[Migration] Starting export for accountId={}", accountId);
        exportPipeline.export(accountId, output, options, listener, token);
        log.info("[Migration] Export complete for accountId={}", accountId);
    }

    @Override
    public void importTenant(InputStream input,
                              TenantImportOptions options,
                              MigrationProgressListener listener,
                              CancellationToken token) {
        log.info("[Migration] Starting import for targetAccountId={}", options.getTargetAccountId());
        importPipeline.importTenant(input, options, listener, token);
        log.info("[Migration] Import complete for targetAccountId={}", options.getTargetAccountId());
    }

    @Override
    public void cloneTenant(TenantCloneOptions options,
                             MigrationProgressListener listener,
                             CancellationToken token) {
        Long source = options.getSourceAccountId();
        Long target = options.getTargetAccountId();
        log.info("[Migration] Starting clone {} → {}", source, target);

        // ── Phase 1: Export to memory buffer ───────────────────────────────
        TenantExportOptions exportOptions = new TenantExportOptions()
                .chunkSize(options.getChunkSize())
                .identityStrategy(options.getIdentityStrategy())
                .label("clone-" + source + "->" + target);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream(8 * 1024 * 1024); // 8 MB initial
        exportPipeline.export(source, buffer, exportOptions, progress -> {
            if (listener != null) {
                listener.onProgress(progress); // forward export progress
            }
        }, token);

        if (token != null && token.isCancelled()) {
            log.info("[Migration] Clone cancelled after export phase");
            return;
        }

        // ── Phase 2: Import from buffer ────────────────────────────────────
        TenantImportOptions importOptions = new TenantImportOptions()
                .targetAccountId(target)
                .chunkSize(options.getChunkSize())
                .identityStrategy(options.getIdentityStrategy())
                .failOnEntityError(options.isFailOnEntityError());

        byte[] exported = buffer.toByteArray();
        log.debug("[Migration] Clone buffer size: {} bytes", exported.length);

        importPipeline.importTenant(
                new ByteArrayInputStream(exported), importOptions, listener, token);

        log.info("[Migration] Clone complete {} → {}", source, target);
    }
}

