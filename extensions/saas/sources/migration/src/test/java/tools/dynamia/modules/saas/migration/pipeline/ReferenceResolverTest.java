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
package tools.dynamia.modules.saas.migration.pipeline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.modules.saas.migration.identity.KeepIdsIdentityMapper;
import tools.dynamia.modules.saas.migration.identity.RegenerateIdsIdentityMapper;
import tools.dynamia.modules.saas.migration.pipeline.ReferenceResolver.Ref;
import tools.dynamia.modules.saas.migration.pipeline.ReferenceResolver.Resolution;
import tools.dynamia.modules.saas.migration.pipeline.ReferenceResolver.RowResult;
import tools.dynamia.modules.saas.migration.pipeline.ReferenceResolver.Status;
import tools.dynamia.modules.saas.migration.pipeline.ReferenceResolver.Verdict;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the decisions taken for references that are not mapped when a row is imported.
 */
public class ReferenceResolverTest {

    static class Parent {
    }

    static class SystemRow {
    }

    private Map<String, Map<Object, Object>> idMappings;

    @BeforeEach
    public void setUp() {
        idMappings = new HashMap<>();
    }

    private ReferenceResolver resolver(boolean skipOrphans, ReferenceResolver.SharedRowCheck check) {
        // Only Parent is part of the export; SystemRow is not.
        return new ReferenceResolver(new RegenerateIdsIdentityMapper(), Set.of(Parent.class.getName()),
                idMappings, check, skipOrphans);
    }

    private static Ref ref(Class<?> type, long id, boolean optional) {
        return new Ref("parent", type, id, optional);
    }

    @Test
    public void mappedParentIsRemappedToNewId() {
        idMappings.computeIfAbsent(Parent.class.getName(), k -> new HashMap<>()).put(10L, 99L);
        RowResult row = resolver(true, (c, i) -> false).resolveRow(List.of(ref(Parent.class, 10, false)), false, "Child");

        assertEquals(Verdict.PERSIST, row.verdict());
        assertEquals(99L, row.ids().get("parent"));
    }

    @Test
    public void softDeletedParentOptionalIsLeftNull() {
        ReferenceResolver r = resolver(true, (c, i) -> false);
        Ref optional = ref(Parent.class, 10, true);

        // First pass: parent not mapped yet, the reference becomes pending and the row is persisted.
        RowResult first = r.resolveRow(List.of(optional), false, "Child");
        assertEquals(Verdict.PERSIST, first.verdict());
        assertTrue(first.ids().isEmpty());
        assertEquals(List.of(optional), first.pending());

        // Second pass: the parent never appeared (soft-deleted) -> stays null and is counted.
        Resolution second = r.resolvePending(optional);
        assertEquals(Status.ORPHAN, second.status());
        assertNull(second.id());
        assertEquals(1, r.clearedReferences());
        assertEquals(0, r.skippedRows());
    }

    @Test
    public void softDeletedParentMandatoryRowIsSkippedAndCounted() {
        ReferenceResolver r = resolver(true, (c, i) -> false);
        List<Ref> refs = List.of(ref(Parent.class, 10, false));

        // Not final yet: the row is held, not dropped.
        assertEquals(Verdict.DEFER, r.resolveRow(refs, false, "Child").verdict());
        assertEquals(0, r.skippedRows());

        // Final phase: nothing maps the parent, the row is dropped.
        assertEquals(Verdict.SKIP, r.resolveRow(refs, true, "Child").verdict());
        assertEquals(1, r.skippedRows());
        assertEquals(1L, r.skippedByClass().get("Child"));
    }

    @Test
    public void cycleChildBeforeParentIsRemappedOnceParentExists() {
        ReferenceResolver r = resolver(true, (c, i) -> false);
        List<Ref> refs = List.of(ref(Parent.class, 10, false));

        // Child arrives first: held.
        assertEquals(Verdict.DEFER, r.resolveRow(refs, false, "Child").verdict());

        // Parent gets imported and mapped.
        idMappings.computeIfAbsent(Parent.class.getName(), k -> new HashMap<>()).put(10L, 77L);

        // Retry: the FK points to the new id, even in the final phase.
        RowResult retry = r.resolveRow(refs, true, "Child");
        assertEquals(Verdict.PERSIST, retry.verdict());
        assertEquals(77L, retry.ids().get("parent"));
        assertEquals(0, r.skippedRows());
    }

    @Test
    public void cycleOptionalReferenceIsPatchedWithNewId() {
        ReferenceResolver r = resolver(true, (c, i) -> false);
        Ref optional = ref(Parent.class, 10, true);

        assertEquals(1, r.resolveRow(List.of(optional), false, "Child").pending().size());
        idMappings.computeIfAbsent(Parent.class.getName(), k -> new HashMap<>()).put(10L, 55L);

        Resolution patch = r.resolvePending(optional);
        assertEquals(Status.RESOLVED, patch.status());
        assertEquals(55L, patch.id());
        assertEquals(0, r.clearedReferences());
    }

    @Test
    public void classOutsideTheExportKeepsOriginalId() {
        ReferenceResolver r = resolver(true, (c, i) -> false);

        RowResult row = r.resolveRow(List.of(ref(SystemRow.class, 5, false)), false, "Child");
        assertEquals(Verdict.PERSIST, row.verdict());
        assertEquals(5L, row.ids().get("parent"));
        assertTrue(row.pending().isEmpty());
    }

    @Test
    public void sharedRowOfExportedClassIsNotNulledNorSkipped() {
        // accountId = 0 rows are never exported but must keep their id.
        ReferenceResolver r = resolver(true, (c, i) -> Long.valueOf(3).equals(i));

        RowResult mandatory = r.resolveRow(List.of(ref(Parent.class, 3, false)), true, "Child");
        assertEquals(Verdict.PERSIST, mandatory.verdict());
        assertEquals(3L, mandatory.ids().get("parent"));

        Resolution optional = r.resolvePending(ref(Parent.class, 3, true));
        assertEquals(Status.RESOLVED, optional.status());
        assertEquals(3L, optional.id());
        assertEquals(0, r.skippedRows());
        assertEquals(0, r.clearedReferences());
    }

    @Test
    public void skipOrphansDisabledKeepsLegacyFallback() {
        ReferenceResolver r = resolver(false, (c, i) -> false);

        RowResult row = r.resolveRow(List.of(ref(Parent.class, 10, false)), true, "Child");
        assertEquals(Verdict.PERSIST, row.verdict());
        assertEquals(10L, row.ids().get("parent"));
        assertEquals(0, r.skippedRows());
    }

    @Test
    public void keepIdsStrategyNeverDefersOrDrops() {
        ReferenceResolver r = new ReferenceResolver(new KeepIdsIdentityMapper(), Set.of(Parent.class.getName()),
                idMappings, (c, i) -> false, true);

        RowResult row = r.resolveRow(List.of(ref(Parent.class, 10, false)), false, "Child");
        assertEquals(Verdict.PERSIST, row.verdict());
        assertEquals(10L, row.ids().get("parent"));
    }

    @Test
    public void sharedCheckIsCachedPerRow() {
        int[] calls = {0};
        ReferenceResolver r = resolver(true, (c, i) -> {
            calls[0]++;
            return false;
        });
        Ref ref = ref(Parent.class, 10, true);
        r.resolvePending(ref);
        r.resolvePending(ref);
        assertEquals(1, calls[0]);
    }
}
