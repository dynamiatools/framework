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
 * Identity mapper that preserves the original primary keys from the export file.
 *
 * <p>Suitable for restoring a backup to a completely empty target database where
 * ID conflicts are not expected.  If the target database already contains rows
 * with the same IDs, constraint violations will occur.
 *
 * @author Mario Serrano Leones
 */
public class KeepIdsIdentityMapper implements IdentityMapper {

    @Override
    public Object mapId(Object originalId, Class<?> entityClass) {
        // Return the original ID — tell the pipeline to set it before persisting
        return originalId;
    }

    @Override
    public Object resolveReferenceId(Object originalRefId, Class<?> refClass,
                                      Map<String, Map<Object, Object>> idMappings) {
        // IDs are unchanged, so the reference ID from the file is already correct
        return originalRefId;
    }

    @Override
    public IdentityStrategy getStrategy() {
        return IdentityStrategy.KEEP_IDS;
    }
}

