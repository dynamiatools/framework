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
package tools.dynamia.modules.saas.migration.discovery;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.modules.saas.api.AccountAware;
import tools.dynamia.modules.saas.api.AccountExportIgnore;
import tools.dynamia.modules.saas.domain.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Discovers all JPA entity classes that participate in the Tenant Mobility export/import.
 *
 * <h3>Discovery algorithm</h3>
 * <ol>
 *   <li>Retrieves all managed entity types from {@link EntityManagerFactory#getMetamodel()}.</li>
 *   <li>Filters to classes that implement {@link AccountAware}.</li>
 *   <li>Excludes classes annotated with {@link AccountExportIgnore}.</li>
 *   <li>Always includes {@link Account} (the tenant root), regardless of the above filters.</li>
 * </ol>
 *
 * @author Mario Serrano Leones
 */
@Service
public class AccountEntityDiscovery {

    private static final Logger log = LoggerFactory.getLogger(AccountEntityDiscovery.class);

    private final EntityManagerFactory emf;

    public AccountEntityDiscovery(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Returns the list of entity classes that should be included in the export.
     * The list is not sorted; call
     * {@link tools.dynamia.modules.saas.migration.graph.EntityDependencyGraph#topologicalSort(List)}
     * to obtain the correct import order.
     */
    public List<Class<?>> discoverExportableEntities() {
        log.info("[Migration] Discovering exportable entity types from JPA metamodel");
        Set<EntityType<?>> managedTypes = emf.getMetamodel().getEntities();
        List<Class<?>> exportable = new ArrayList<>();

        // Always include Account as the tenant root
        exportable.add(Account.class);
        log.debug("[Migration] Always including: {}", Account.class.getName());

        for (EntityType<?> entityType : managedTypes) {
            Class<?> javaType = entityType.getJavaType();

            // Skip Account itself (already added above)
            if (Account.class.equals(javaType)) {
                continue;
            }

            // Skip entities not annotated as tenant-aware
            if (!AccountAware.class.isAssignableFrom(javaType)) {
                continue;
            }

            // Skip entities explicitly excluded from export
            if (javaType.isAnnotationPresent(AccountExportIgnore.class)) {
                log.debug("[Migration] Skipping @AccountExportIgnore entity: {}", javaType.getName());
                continue;
            }

            exportable.add(javaType);
            log.debug("[Migration] Discovered exportable entity: {}", javaType.getName());
        }

        log.info("[Migration] Discovered {} exportable entity types", exportable.size());
        return exportable;
    }
}

