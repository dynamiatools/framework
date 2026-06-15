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

import java.util.Map;

/**
 * SPI for controlling how entity primary keys are handled during import.
 *
 * <p>The default implementation is {@link tools.dynamia.modules.saas.migration.identity.RegenerateIdsIdentityMapper}
 * which assigns new JPA-generated IDs and resolves internal references via an
 * {@code idMappings} table ({@code entityClass.name → {originalId → newId}}).
 *
 * <p>Implement and register this interface as a Spring bean to override the default behaviour.
 *
 * @author Mario Serrano Leones
 */
public interface IdentityMapper {

    /**
     * Determines the ID to use when persisting an imported entity.
     *
     * @param originalId  the ID read from the export file; may be null
     * @param entityClass the JPA entity class being imported
     * @return the ID to assign before persisting, or {@code null} to let JPA auto-generate
     */
    Object mapId(Object originalId, Class<?> entityClass);

    /**
     * Resolves a foreign-key reference ID from the export file to the correct ID
     * in the target database, using the running ID mapping table.
     *
     * @param originalRefId the reference ID read from the export ({@code fieldName_ref_id})
     * @param refClass      the referenced entity class
     * @param idMappings    mutable map of {@code className → {originalId → newId}}; updated during import
     * @return the actual ID to use when creating the JPA reference proxy
     */
    Object resolveReferenceId(Object originalRefId, Class<?> refClass,
                               Map<String, Map<Object, Object>> idMappings);

    /** Returns the strategy implemented by this mapper. */
    IdentityStrategy getStrategy();
}

