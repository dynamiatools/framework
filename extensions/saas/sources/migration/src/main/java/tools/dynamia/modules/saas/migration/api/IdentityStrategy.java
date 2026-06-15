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
 * Controls how entity primary keys are handled during import.
 *
 * @author Mario Serrano Leones
 */
public enum IdentityStrategy {

    /**
     * Preserve original database IDs.
     * <p>
     * The imported entities will have the same primary keys as the source system.
     * This is safe when restoring to an empty target database.
     * It may cause constraint violations if the target DB already contains data.
     */
    KEEP_IDS,

    /**
     * Auto-generate new IDs for all imported entities.
     * <p>
     * JPA auto-generation is used for each entity. Foreign-key references are
     * resolved via the internal ID mapping table ({@code originalId → newId}).
     * This is the recommended strategy for cloning within the same database.
     */
    REGENERATE_IDS,

    /**
     * Assign UUIDv7 values as new IDs.
     * <p>
     * Planned for v3.
     */
    UUID7
}

