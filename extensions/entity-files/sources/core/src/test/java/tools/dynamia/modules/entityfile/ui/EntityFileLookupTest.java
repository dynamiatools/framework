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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.modules.entityfile.EntityFileAccountProvider;
import tools.dynamia.modules.entityfile.domain.EntityFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EntityFileLookupTest {

    @AfterEach
    void uninstall() {
        Containers.get().removeAllContainers();
    }

    private static void account(Long id) {
        var beans = new SimpleObjectContainer("entity-file-lookup-test");
        beans.addObject((EntityFileAccountProvider) () -> id);
        Containers.get().installObjectContainer(beans);
    }

    @Test
    void withoutAnAccountNothingCanBeFoundOrPicked() {
        var crud = mock(CrudService.class);
        var lookup = new EntityFileLookup(crud);

        assertTrue(lookup.search("x", 10).isEmpty());
        assertTrue(lookup.find("some-uuid").isEmpty());
        verify(crud, never()).find(eq(EntityFile.class), any(QueryParameters.class));
        verify(crud, never()).findSingle(eq(EntityFile.class), any(QueryParameters.class));
    }

    @Test
    void anAccountOfZeroIsNotAnAccount() {
        account(0L);
        var crud = mock(CrudService.class);

        assertTrue(new EntityFileLookup(crud).search("", 10).isEmpty());
    }

    @Test
    void searchFiltersByTheCurrentAccountAndCapsThePage() {
        account(7L);
        var crud = mock(CrudService.class);
        var file = new EntityFile();
        when(crud.find(eq(EntityFile.class), any(QueryParameters.class))).thenReturn(List.of(file));

        var found = new EntityFileLookup(crud).search("inv", 100000);

        assertEquals(List.of(file), found);
        var captor = org.mockito.ArgumentCaptor.forClass(QueryParameters.class);
        verify(crud).find(eq(EntityFile.class), captor.capture());
        assertEquals(7L, ((tools.dynamia.domain.query.QueryCondition) captor.getValue().get("accountId")).getValue());
        assertEquals(tools.dynamia.ui.EntityLookup.MAX_LIMIT, captor.getValue().getMaxResults());
    }

    @Test
    void findLooksTheKeyUpInsideTheAccountOnly() {
        account(7L);
        var crud = mock(CrudService.class);
        var file = new EntityFile();
        when(crud.findSingle(eq(EntityFile.class), any(QueryParameters.class))).thenReturn(file);

        assertEquals(file, new EntityFileLookup(crud).find("abc").orElseThrow());
        var captor = org.mockito.ArgumentCaptor.forClass(QueryParameters.class);
        verify(crud).findSingle(eq(EntityFile.class), captor.capture());
        assertEquals(7L, ((tools.dynamia.domain.query.QueryCondition) captor.getValue().get("accountId")).getValue());
    }
}
