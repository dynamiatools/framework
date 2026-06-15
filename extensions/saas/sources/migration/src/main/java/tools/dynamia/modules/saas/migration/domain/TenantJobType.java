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
package tools.dynamia.modules.saas.migration.domain;

/**
 * Type of a {@link TenantMobilityJob}.
 *
 * @author Mario Serrano Leones
 */
public enum TenantJobType {

    /** Export all tenant data to a JSON/GZIP file. */
    EXPORT,

    /** Import tenant data from a previously exported file. */
    IMPORT,

    /** Clone a tenant within the same system (source → target accountId). */
    CLONE,

    /** Export tagged as a backup (semantically identical to EXPORT). */
    BACKUP,

    /** Import that replaces existing tenant data (semantically identical to IMPORT). */
    RESTORE,

    /**
     * Cross-environment migration: export locally + push to a remote endpoint.
     * <em>Planned for v2.</em>
     */
    MIGRATE
}

