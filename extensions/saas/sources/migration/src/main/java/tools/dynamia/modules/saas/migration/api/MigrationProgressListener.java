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

/**
 * SPI callback for receiving progress updates during a migration pipeline execution.
 *
 * <p>Implementations are typically provided by the job service to persist progress
 * in the {@code TenantMobilityJob} entity.
 *
 * @author Mario Serrano Leones
 */
@FunctionalInterface
public interface MigrationProgressListener {

    /**
     * Called by the pipeline whenever significant progress has been made.
     *
     * @param progress current progress snapshot
     */
    void onProgress(MigrationProgress progress);
}

