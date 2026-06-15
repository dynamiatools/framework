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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tools.dynamia.modules.saas.migration.api.IdentityStrategy;

import java.util.HashMap;
import java.util.Map;

public class RegenerateIdsIdentityMapperTest {

    private RegenerateIdsIdentityMapper mapper;

    @Before
    public void setUp() {
        mapper = new RegenerateIdsIdentityMapper();
    }

    @Test
    public void strategyIsRegenerateIds() {
        Assert.assertEquals(IdentityStrategy.REGENERATE_IDS, mapper.getStrategy());
    }

    @Test
    public void mapIdAlwaysReturnsNull() {
        Assert.assertNull(mapper.mapId(1L, String.class));
        Assert.assertNull(mapper.mapId(99999L, Object.class));
        Assert.assertNull(mapper.mapId(null, String.class));
    }

    @Test
    public void resolveReferenceIdLookupsFromIdMappings() {
        Map<String, Map<Object, Object>> idMappings = new HashMap<>();
        idMappings.put(String.class.getName(), Map.of(10L, 501L));

        Object resolved = mapper.resolveReferenceId(10L, String.class, idMappings);
        Assert.assertEquals(501L, resolved);
    }

    @Test
    public void resolveReferenceIdFallsBackToOriginalWhenNotMapped() {
        // Entity not in idMappings (e.g. system-level shared entity)
        Map<String, Map<Object, Object>> idMappings = new HashMap<>();

        Object resolved = mapper.resolveReferenceId(77L, String.class, idMappings);
        Assert.assertEquals(77L, resolved);
    }

    @Test
    public void resolveReferenceIdWithNullRefIdReturnsNull() {
        Assert.assertNull(mapper.resolveReferenceId(null, String.class, new HashMap<>()));
    }

    @Test
    public void resolveReferenceIdFallsBackWhenClassKeyExistsButIdMissing() {
        // Class is in the map but this specific originalId isn't recorded yet
        Map<String, Map<Object, Object>> idMappings = new HashMap<>();
        Map<Object, Object> classMap = new HashMap<>();
        classMap.put(1L, 100L);
        idMappings.put(String.class.getName(), classMap);

        // originalRefId=99 not in classMap → fallback to original
        Object resolved = mapper.resolveReferenceId(99L, String.class, idMappings);
        Assert.assertEquals(99L, resolved);
    }
}
