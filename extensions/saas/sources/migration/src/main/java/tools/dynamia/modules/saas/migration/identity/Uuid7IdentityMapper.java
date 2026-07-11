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
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Identity mapper that assigns UUIDv7 values as primary keys for all imported entities.
 *
 * <p>UUIDv7 is a time-ordered UUID (RFC 9562). Each call to {@link #mapId} generates
 * a new UUID7 from the current millisecond timestamp plus random bits. Suitable for
 * entities whose {@code id} field is of type {@link UUID} or {@link String}.
 *
 * <p>Foreign-key references are resolved via the running {@code idMappings} table
 * ({@code originalId → uuid7}), identical to the {@code REGENERATE_IDS} strategy.
 *
 * @author Mario Serrano Leones
 */
public class Uuid7IdentityMapper implements IdentityMapper {

    @Override
    public Object mapId(Object originalId, Class<?> entityClass) {
        return generateUuid7();
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
        // Fallback: entity not in the export set (e.g. shared system-level entity)
        return originalRefId;
    }

    @Override
    public IdentityStrategy getStrategy() {
        return IdentityStrategy.UUID7;
    }

    /**
     * Generates a UUIDv7 (time-ordered) value per RFC 9562.
     *
     * <p>Layout (128 bits):
     * <ul>
     *   <li>Bits  0–47 : Unix timestamp in milliseconds</li>
     *   <li>Bits 48–51 : Version = 7</li>
     *   <li>Bits 52–63 : rand_a (12 random bits)</li>
     *   <li>Bits 64–65 : Variant = 0b10</li>
     *   <li>Bits 66–127: rand_b (62 random bits)</li>
     * </ul>
     */
    static UUID generateUuid7() {
        long now = System.currentTimeMillis();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        // MSB: 48-bit timestamp | 4-bit version (7) | 12-bit random (rand_a)
        long msb = (now << 16) | (7L << 12) | (rng.nextLong() & 0x0FFFL);
        // LSB: variant 0b10 in high 2 bits | 62-bit random (rand_b)
        long lsb = 0x8000000000000000L | (rng.nextLong() & 0x3FFFFFFFFFFFFFFFL);
        return new UUID(msb, lsb);
    }
}
