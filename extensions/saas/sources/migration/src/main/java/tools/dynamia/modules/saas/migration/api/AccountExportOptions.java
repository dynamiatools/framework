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

import java.util.List;

/**
 * Options controlling a tenant export operation.
 *
 * @author Mario Serrano Leones
 */
public class AccountExportOptions {

    public static final int DEFAULT_CHUNK_SIZE = 1000;

    /**
     * Number of records to read from DB per pagination page. Default: 5000.
     */
    private int chunkSize = DEFAULT_CHUNK_SIZE;

    /**
     * No longer has any effect — the export always produces a ZIP archive.
     * Kept for API backward compatibility only.
     *
     * @deprecated Since format v3, the output is always a ZIP. This flag is ignored.
     */
    @Deprecated
    private boolean compressionEnabled = false;

    /**
     * Controls how IDs are represented in the exported file.
     */
    private IdentityStrategy identityStrategy = IdentityStrategy.KEEP_IDS;

    /**
     * Optional display name for this export (used in file names and job labels).
     */
    private String label;
    private List<String> entities;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public AccountExportOptions() {
    }

    public AccountExportOptions(IdentityStrategy identityStrategy) {
        this.identityStrategy = identityStrategy;
    }

    // ─── Fluent builder ────────────────────────────────────────────────────────

    public AccountExportOptions chunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }

    /**
     * @deprecated Since format v3, the output is always a ZIP. This method is a no-op.
     */
    @Deprecated
    public AccountExportOptions compressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
        return this;
    }

    public AccountExportOptions identityStrategy(IdentityStrategy identityStrategy) {
        this.identityStrategy = identityStrategy;
        return this;
    }

    public AccountExportOptions label(String label) {
        this.label = label;
        return this;
    }

    // ─── Accessors ─────────────────────────────────────────────────────────────

    public int getChunkSize() {
        if (chunkSize <= 0) {
            chunkSize = DEFAULT_CHUNK_SIZE;
        }
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    /** @deprecated Since format v3, always returns {@code false}. Ignored by the pipeline. */
    @Deprecated
    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    /** @deprecated Since format v3, the output is always a ZIP. This setter is a no-op. */
    @Deprecated
    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }

    public IdentityStrategy getIdentityStrategy() {
        if (identityStrategy == null) {
            identityStrategy = IdentityStrategy.KEEP_IDS;
        }
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

    public List<String> getEntities() {
        return entities;
    }

    public void setEntities(List<String> entities) {
        this.entities = entities;
    }
}

