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

    /** How to handle primary keys when persisting imported entities. */
    private IdentityStrategy identityStrategy = IdentityStrategy.REGENERATE_IDS;

    /** Number of entities to persist per transaction batch. Default: 500. */
    private int chunkSize = 500;

    /**
     * When {@code true}, the import fails immediately if any entity cannot be
     * persisted. When {@code false}, errors are logged and the import continues.
     */
    private boolean failOnEntityError = false;

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
}

