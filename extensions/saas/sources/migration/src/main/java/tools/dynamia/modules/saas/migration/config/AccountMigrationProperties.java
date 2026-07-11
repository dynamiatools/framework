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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Tenant Mobility module.
 *
 * <p>All properties are prefixed with {@code dynamia.saas.migration}.
 *
 * <p>Example {@code application.properties}:
 * <pre>
 * dynamia.saas.migration.chunk-size=500
 * dynamia.saas.migration.output-directory=/var/data/saas-migration
 * dynamia.saas.migration.max-concurrent-jobs=5
 * dynamia.saas.migration.export-parallelism=4
 * dynamia.saas.migration.fail-on-entity-error=false
 * </pre>
 *
 * @author Mario Serrano Leones
 */
@ConfigurationProperties(prefix = "dynamia.saas.migration")
public class AccountMigrationProperties {

    /** Number of records read/written per pagination page. Default: 500. */
    private int chunkSize = 500;

    /**
     * Directory where export/backup files are stored.
     * Defaults to {@code ${java.io.tmpdir}/saas-migration}.
     */
    private String outputDirectory = System.getProperty("java.io.tmpdir") + "/saas-migration";

    /** Whether to compress output files with GZIP by default. Default: {@code false}. */
    private boolean compressionEnabled = false;

    /**
     * Maximum number of jobs that can be in RUNNING state simultaneously.
     * Additional jobs remain in PENDING and are started as running jobs finish.
     * Default: 5.
     */
    private int maxConcurrentJobs = 5;

    /**
     * Number of entity types exported concurrently during a single export job.
     * Each parallel slot opens its own {@code EntityManager}. Default: 4.
     */
    private int exportParallelism = 4;

    /**
     * If {@code true}, the import pipeline stops immediately when any entity
     * fails to persist. If {@code false}, errors are logged and the import
     * continues. Default: {@code false}.
     */
    private boolean failOnEntityError = false;

    // ─── Accessors ─────────────────────────────────────────────────────────────

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }

    public int getMaxConcurrentJobs() {
        return maxConcurrentJobs;
    }

    public void setMaxConcurrentJobs(int maxConcurrentJobs) {
        this.maxConcurrentJobs = maxConcurrentJobs;
    }

    public int getExportParallelism() {
        return exportParallelism;
    }

    public void setExportParallelism(int exportParallelism) {
        this.exportParallelism = exportParallelism;
    }

    public boolean isFailOnEntityError() {
        return failOnEntityError;
    }

    public void setFailOnEntityError(boolean failOnEntityError) {
        this.failOnEntityError = failOnEntityError;
    }
}

