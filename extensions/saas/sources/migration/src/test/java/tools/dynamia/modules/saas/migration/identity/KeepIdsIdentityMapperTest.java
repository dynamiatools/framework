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

public class KeepIdsIdentityMapperTest {

    private KeepIdsIdentityMapper mapper;

    @Before
    public void setUp() {
        mapper = new KeepIdsIdentityMapper();
    }

    @Test
    public void strategyIsKeepIds() {
        Assert.assertEquals(IdentityStrategy.KEEP_IDS, mapper.getStrategy());
    }

    @Test
    public void mapIdReturnsOriginalId() {
        Assert.assertEquals(42L, mapper.mapId(42L, String.class));
        Assert.assertEquals("uuid-123", mapper.mapId("uuid-123", Object.class));
    }

    @Test
    public void mapIdWithNullReturnsNull() {
        Assert.assertNull(mapper.mapId(null, String.class));
    }

    @Test
    public void resolveReferenceIdReturnsOriginalRefIdIgnoringMap() {
        Map<String, Map<Object, Object>> idMappings = new HashMap<>();
        idMappings.put(String.class.getName(), Map.of(1L, 999L));

        // KEEP_IDS: the ref ID from the file is the correct ID in the target DB
        Object resolved = mapper.resolveReferenceId(1L, String.class, idMappings);
        Assert.assertEquals(1L, resolved);
    }

    @Test
    public void resolveReferenceIdWithNullRefIdReturnsNull() {
        Assert.assertNull(mapper.resolveReferenceId(null, String.class, new HashMap<>()));
    }
}
