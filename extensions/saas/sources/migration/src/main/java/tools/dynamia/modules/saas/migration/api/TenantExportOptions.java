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
 * Options controlling a tenant export operation.
 *
 * @author Mario Serrano Leones
 */
public class TenantExportOptions {

    /** Number of records to read from DB per pagination page. Default: 500. */
    private int chunkSize = 500;

    /** When {@code true}, the output stream is wrapped in GZIP compression. */
    private boolean compressionEnabled = false;

    /** Controls how IDs are represented in the exported file. */
    private IdentityStrategy identityStrategy = IdentityStrategy.KEEP_IDS;

    /** Optional display name for this export (used in file names and job labels). */
    private String label;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public TenantExportOptions() {
    }

    public TenantExportOptions(IdentityStrategy identityStrategy) {
        this.identityStrategy = identityStrategy;
    }

    // ─── Fluent builder ────────────────────────────────────────────────────────

    public TenantExportOptions chunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }

    public TenantExportOptions compressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
        return this;
    }

    public TenantExportOptions identityStrategy(IdentityStrategy identityStrategy) {
        this.identityStrategy = identityStrategy;
        return this;
    }

    public TenantExportOptions label(String label) {
        this.label = label;
        return this;
    }

    // ─── Accessors ─────────────────────────────────────────────────────────────

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }

    public IdentityStrategy getIdentityStrategy() {
        return identityStrategy;
    }

    public void setIdentityStrategy(IdentityStrategy identityStrategy) {
        this.identityStrategy = identityStrategy;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}

