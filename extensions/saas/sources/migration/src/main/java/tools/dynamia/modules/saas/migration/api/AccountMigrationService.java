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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * High-level service for executing tenant mobility operations synchronously.
 *
 * <p>This service is designed to be called from within a worker/job that is already
 * running in a background virtual thread. For async job management see
 * {@link AccountMigrationJobService}.
 *
 * <pre>{@code
 * // Direct usage (synchronous — blocks until complete):
 * mobilityService.exportTenant(42L, outputStream, new TenantExportOptions(), listener, token);
 *
 * // Preferred usage (async via job service):
 * jobService.createExportJob(42L, new TenantExportOptions());
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public interface AccountMigrationService {

    /**
     * Exports all tenant data for {@code accountId} to the given {@code output} stream.
     *
     * @param accountId account whose data will be exported
     * @param output    destination stream (may be wrapped in GZIP by the pipeline if configured)
     * @param options   export configuration
     * @param listener  optional progress callback; may be {@code null}
     * @param token     optional cancellation token; may be {@code null}
     */
    void exportTenant(Serializable accountId,
                      OutputStream output,
                      AccountExportOptions options,
                      MigrationProgressListener listener,
                      CancellationToken token);

    /**
     * Imports tenant data from an exported {@code input} stream.
     *
     * @param input    source stream (GZIP-encoded or plain JSON)
     * @param options  import configuration, including target account and identity strategy
     * @param listener optional progress callback; may be {@code null}
     * @param token    optional cancellation token; may be {@code null}
     */
    void importTenant(InputStream input,
                      AccountImportOptions options,
                      MigrationProgressListener listener,
                      CancellationToken token);

    /**
     * Clones a tenant within the same system by exporting to an in-memory buffer
     * and immediately importing to the target account.
     *
     * @param options  clone configuration (source/target accounts, identity strategy, etc.)
     * @param listener optional progress callback; may be {@code null}
     * @param token    optional cancellation token; may be {@code null}
     */
    void cloneTenant(AccountCloneOptions options,
                     MigrationProgressListener listener,
                     CancellationToken token);
}

