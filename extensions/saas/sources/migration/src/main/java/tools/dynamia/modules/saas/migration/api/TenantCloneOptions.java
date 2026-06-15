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
 * Options for a clone operation (source tenant → target tenant, same system).
 *
 * @author Mario Serrano Leones
 */
public class TenantCloneOptions {

    /** ID of the account to clone data from. Required. */
    private Long sourceAccountId;

    /** ID of the (already existing) target account. Required. */
    private Long targetAccountId;

    /**
     * Strategy for handling IDs.
     * Defaults to {@link IdentityStrategy#REGENERATE_IDS} because clone typically
     * happens within the same database.
     */
    private IdentityStrategy identityStrategy = IdentityStrategy.REGENERATE_IDS;

    /** Records per page during export/import. Default: 500. */
    private int chunkSize = 500;

    /**
     * When {@code true}, entity errors are fatal. When {@code false}, they are
     * logged and the clone continues. Default: {@code false}.
     */
    private boolean failOnEntityError = false;

    // ─── Fluent builder ────────────────────────────────────────────────────────

    public TenantCloneOptions source(Long sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
        return this;
    }

    public TenantCloneOptions target(Long targetAccountId) {
        this.targetAccountId = targetAccountId;
        return this;
    }

    public TenantCloneOptions identityStrategy(IdentityStrategy identityStrategy) {
        this.identityStrategy = identityStrategy;
        return this;
    }

    public TenantCloneOptions chunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }

    // ─── Accessors ─────────────────────────────────────────────────────────────

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(Long sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public Long getTargetAccountId() {
        return targetAccountId;
    }

    public void setTargetAccountId(Long targetAccountId) {
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

