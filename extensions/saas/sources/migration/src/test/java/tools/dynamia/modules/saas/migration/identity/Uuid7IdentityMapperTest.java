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
import java.util.UUID;

public class Uuid7IdentityMapperTest {

    private Uuid7IdentityMapper mapper;

    @Before
    public void setUp() {
        mapper = new Uuid7IdentityMapper();
    }

    @Test
    public void strategyIsUuid7() {
        Assert.assertEquals(IdentityStrategy.UUID7, mapper.getStrategy());
    }

    @Test
    public void mapIdReturnsUuid() {
        Object id = mapper.mapId(1L, Object.class);
        Assert.assertNotNull(id);
        Assert.assertTrue(id instanceof UUID);
    }

    @Test
    public void mapIdReturnsDistinctValuesEachCall() {
        UUID a = (UUID) mapper.mapId(1L, Object.class);
        UUID b = (UUID) mapper.mapId(1L, Object.class);
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void mapIdIgnoresOriginalId() {
        // UUID7 strategy always generates a new ID regardless of the original
        Assert.assertNotEquals(mapper.mapId(42L, Object.class), 42L);
        Assert.assertNotNull(mapper.mapId(null, Object.class));
    }

    @Test
    public void resolveReferenceIdLookupsFromIdMappings() {
        UUID newId = UUID.randomUUID();
        Map<String, Map<Object, Object>> idMappings = new HashMap<>();
        idMappings.put(String.class.getName(), Map.of(10L, newId));

        Object resolved = mapper.resolveReferenceId(10L, String.class, idMappings);
        Assert.assertEquals(newId, resolved);
    }

    @Test
    public void resolveReferenceIdFallsBackToOriginalWhenNotMapped() {
        Map<String, Map<Object, Object>> idMappings = new HashMap<>();
        Object resolved = mapper.resolveReferenceId(77L, String.class, idMappings);
        Assert.assertEquals(77L, resolved);
    }

    @Test
    public void resolveReferenceIdWithNullReturnsNull() {
        Assert.assertNull(mapper.resolveReferenceId(null, String.class, new HashMap<>()));
    }

    // ── UUIDv7 structure tests ─────────────────────────────────────────────────

    @Test
    public void generatedUuidHasVersion7() {
        UUID uuid = Uuid7IdentityMapper.generateUuid7();
        Assert.assertEquals(7, uuid.version());
    }

    @Test
    public void generatedUuidHasVariant2() {
        UUID uuid = Uuid7IdentityMapper.generateUuid7();
        Assert.assertEquals(2, uuid.variant());
    }

    @Test
    public void generatedUuidsAreTimeOrdered() throws InterruptedException {
        UUID a = Uuid7IdentityMapper.generateUuid7();
        Thread.sleep(2);
        UUID b = Uuid7IdentityMapper.generateUuid7();
        // Higher timestamp → higher MSB → natural UUID ordering matches time order
        Assert.assertTrue(a.getMostSignificantBits() < b.getMostSignificantBits()
                || a.compareTo(b) < 0);
    }
}
