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
package tools.dynamia.modules.saas.migration.identity;

import tools.dynamia.modules.saas.migration.api.IdentityMapper;
import tools.dynamia.modules.saas.migration.api.IdentityStrategy;

import java.util.Map;

/**
 * Identity mapper that discards original IDs and lets JPA auto-generate new ones.
 *
 * <p>During import, each entity is persisted without a pre-set ID so the JPA
 * persistence provider assigns a fresh ID. After each {@code persist}, the mapping
 * {@code originalId → newId} is recorded and used to resolve foreign-key references
 * in subsequent entities.
 *
 * <p>This is the recommended strategy for cloning within the same database, where
 * duplicate IDs would cause constraint violations.
 *
 * @author Mario Serrano Leones
 */
public class RegenerateIdsIdentityMapper implements IdentityMapper {

    @Override
    public Object mapId(Object originalId, Class<?> entityClass) {
        // Return null → pipeline will clear the ID field and let JPA generate a new one
        return null;
    }

    @Override
    public Object resolveReferenceId(Object originalRefId, Class<?> refClass,
                                      Map<String, Map<Object, Object>> idMappings) {
        if (originalRefId == null) {
            return null;
        }
        Map<Object, Object> classMap = idMappings.get(refClass.getName());
        if (classMap != null) {
            Object mapped = classMap.get(originalRefId);
            if (mapped != null) {
                return mapped;
            }
        }
        // Fallback: return original (may happen for references to entities not in the export,
        // e.g., system-level entities shared across tenants)
        return originalRefId;
    }

    @Override
    public IdentityStrategy getStrategy() {
        return IdentityStrategy.REGENERATE_IDS;
    }
}

