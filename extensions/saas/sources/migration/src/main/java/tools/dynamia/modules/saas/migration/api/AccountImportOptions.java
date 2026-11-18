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

import java.io.Serializable;

/**
 * Options controlling a tenant import operation.
 *
 * @author Mario Serrano Leones
 */
public class AccountImportOptions {

    /**
     * Target account ID.
     * When {@code null}, the import will attempt to create a new account from
     * the {@code account} section of the export file.
     */
    private Serializable targetAccountId;

    /**
     * How to handle primary keys when persisting imported entities.
     */
    private IdentityStrategy identityStrategy = IdentityStrategy.REGENERATE_IDS;

    /**
     * Number of entities to persist per transaction batch. Default: 500.
     */
    private int chunkSize = AccountExportOptions.DEFAULT_CHUNK_SIZE;

    /**
     * When {@code true}, the import fails immediately if any entity cannot be
     * persisted. When {@code false}, errors are logged and the import continues.
     */
    private boolean failOnEntityError = false;

    /**
     * What to do with references to rows that were not cloned (for example a soft-deleted parent whose
     * children are alive). When {@code true} (default), an optional reference is left {@code null} and a
     * row with an unresolvable mandatory reference is skipped; the counts are logged and added to the
     * final progress message. When {@code false}, the original id is kept, which leaves the new row
     * pointing at a row of the source account (legacy behaviour).
     * References to entities that are not part of the export (shared system data) always keep their id.
     */
    private boolean skipOrphans = true;

    // ─── Fluent builder ────────────────────────────────────────────────────────

    public AccountImportOptions targetAccountId(Serializable targetAccountId) {
        this.targetAccountId = targetAccountId;
        return this;
    }

    public AccountImportOptions identityStrategy(IdentityStrategy identityStrategy) {
        this.identityStrategy = identityStrategy;
        return this;
    }

    public AccountImportOptions chunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }

    public AccountImportOptions failOnEntityError(boolean failOnEntityError) {
        this.failOnEntityError = failOnEntityError;
        return this;
    }

    public AccountImportOptions skipOrphans(boolean skipOrphans) {
        this.skipOrphans = skipOrphans;
        return this;
    }

    // ─── Accessors ─────────────────────────────────────────────────────────────

    public Serializable getTargetAccountId() {
        return targetAccountId;
    }

    public void setTargetAccountId(Serializable targetAccountId) {
        this.targetAccountId = targetAccountId;
    }

    public IdentityStrategy getIdentityStrategy() {
        return identityStrategy;
    }

    public void setIdentityStrategy(IdentityStrategy identityStrategy) {
        this.identityStrategy = identityStrategy;
    }

    public int getChunkSize() {
        if (chunkSize <= 0) {
            chunkSize = AccountExportOptions.DEFAULT_CHUNK_SIZE;
        }
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public boolean isFailOnEntityError() {
        return failOnEntityError;
    }

    public void setFailOnEntityError(boolean failOnEntityError) {
        this.failOnEntityError = failOnEntityError;
    }

    public boolean isSkipOrphans() {
        return skipOrphans;
    }

    public void setSkipOrphans(boolean skipOrphans) {
        this.skipOrphans = skipOrphans;
    }
}
