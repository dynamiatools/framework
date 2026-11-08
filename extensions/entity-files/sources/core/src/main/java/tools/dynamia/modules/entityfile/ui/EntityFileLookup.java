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
package tools.dynamia.modules.entityfile.ui;

import org.springframework.stereotype.Component;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.entityfile.EntityFileAccountProvider;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.domain.enums.EntityFileState;
import tools.dynamia.modules.entityfile.enums.EntityFileType;
import tools.dynamia.ui.EntityLookup;

import java.util.List;
import java.util.Optional;

/**
 * Lets the user pick a file already stored for the current account instead of uploading it again:
 * {@code UIChoices.pickEntity(EntityFileLookup.NAME, "File", file -> ...)}.
 *
 * <p>Only valid files (not directories) of the account given by {@link EntityFileAccountProvider} can be found or picked.
 * Without a provider, or when it has no account, nothing can be picked: the lookup fails closed.
 */
@Component
public class EntityFileLookup implements EntityLookup<EntityFile> {

    /** Name of the lookup. */
    public static final String NAME = "entity-files";

    private final CrudService crudService;

    /**
     * @param crudService used to query the files
     */
    public EntityFileLookup(CrudService crudService) {
        this.crudService = crudService;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public List<EntityFile> search(String text, int limit) {
        Long account = currentAccount();
        if (account == null) {
            return List.of();
        }
        var params = QueryParameters.with("accountId", QueryConditions.eq(account))
                .add("state", QueryConditions.eq(EntityFileState.VALID))
                .add("type", QueryConditions.notEq(EntityFileType.DIRECTORY))
                .orderBy("id", false)
                .setMaxResults(Math.max(1, Math.min(limit, MAX_LIMIT)));
        if (text != null && !text.isBlank()) {
            params.add("name", QueryConditions.like(text.trim()));
        }
        return crudService.find(EntityFile.class, params);
    }

    @Override
    public Optional<EntityFile> find(String key) {
        Long account = currentAccount();
        if (account == null || key == null || key.isBlank()) {
            return Optional.empty();
        }
        var found = crudService.findSingle(EntityFile.class, QueryParameters.with("uuid", QueryConditions.eq(key))
                .add("accountId", QueryConditions.eq(account))
                .add("state", QueryConditions.eq(EntityFileState.VALID))
                .add("type", QueryConditions.notEq(EntityFileType.DIRECTORY)));
        return Optional.ofNullable(found);
    }

    @Override
    public String key(EntityFile value) {
        return value.getUuid();
    }

    @Override
    public String label(EntityFile value) {
        return value.getName();
    }

    private static Long currentAccount() {
        EntityFileAccountProvider provider = Containers.get().findObject(EntityFileAccountProvider.class);
        Long account = provider == null ? null : provider.getAccountId();
        return account != null && account > 0 ? account : null;
    }
}
