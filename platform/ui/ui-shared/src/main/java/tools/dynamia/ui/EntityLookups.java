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
package tools.dynamia.ui;

import tools.dynamia.integration.Containers;

import java.util.Optional;

/**
 * Finds the {@link EntityLookup} beans by name.
 */
public final class EntityLookups {

    private EntityLookups() {
    }

    /**
     * Finds a lookup by name among the beans of the container.
     *
     * @param name the name given by {@link EntityLookup#name()}
     * @return the lookup, or empty when none has that name
     */
    @SuppressWarnings("unchecked")
    public static Optional<EntityLookup<Object>> find(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Containers.get().findObjects(EntityLookup.class).stream()
                .filter(lookup -> name.equals(lookup.name()))
                .map(lookup -> (EntityLookup<Object>) lookup)
                .findFirst();
    }
}
