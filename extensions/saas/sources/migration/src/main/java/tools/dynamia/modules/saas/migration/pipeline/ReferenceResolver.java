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

import tools.dynamia.modules.saas.migration.api.IdentityMapper;
import tools.dynamia.modules.saas.migration.api.IdentityStrategy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Decides what to do with a foreign-key reference read from an export while importing.
 *
 * <p>The plain {@link IdentityMapper#resolveReferenceId} falls back to the <em>original</em> id when the
 * parent has not been mapped yet. That is right for entities that are not part of the export (shared,
 * system-level rows) but wrong in two cases that left dangling references to the source account:
 * <ul>
 *   <li>the parent row was not exported (for example it is soft-deleted: negative {@code accountId}),
 *       but its children were;</li>
 *   <li>entities in a dependency cycle: the child is imported before its parent exists in the mapping.</li>
 * </ul>
 *
 * <p>This class keeps the original fallback only for classes that are <strong>not</strong> in the export
 * (and for rows owned by nobody, {@code accountId = 0}). For classes that are in the export it answers:
 * <ul>
 *   <li>{@link Status#RESOLVED}: the parent is mapped (or legitimately shared), use the returned id;</li>
 *   <li>{@link Status#PENDING}: not mapped yet, it may still be imported later — retry in the second pass;</li>
 *   <li>{@link Status#ORPHAN}: final phase and the parent was never cloned.</li>
 * </ul>
 *
 * <p>The class has no persistence dependencies so it can be unit tested; the (rare) check of whether an
 * unmapped row is shared is delegated to a {@link SharedRowCheck}. Not thread-safe: one instance per import.
 *
 * @author Mario Serrano Leones
 */
final class ReferenceResolver {

    /**
     * Tells whether an original row of an exported class is shared and must keep its original id
     * (for example {@code accountId = 0}) instead of being treated as an orphan.
     */
    @FunctionalInterface
    interface SharedRowCheck {
        boolean isShared(Class<?> refClass, Object originalId);
    }

    enum Status {RESOLVED, PENDING, ORPHAN}

    enum Verdict {PERSIST, DEFER, SKIP}

    /**
     * A foreign key found in a row.
     *
     * @param attribute  the entity attribute holding the reference
     * @param refClass   referenced entity class
     * @param originalId id of the referenced row in the source data
     * @param optional   whether the attribute accepts {@code null}
     */
    record Ref(String attribute, Class<?> refClass, Object originalId, boolean optional) {
    }

    record Resolution(Status status, Object id) {
    }

    /**
     * Result of resolving all the references of one row.
     *
     * @param verdict {@link Verdict#PERSIST} persist the row with {@code ids}; {@link Verdict#DEFER} retry later
     *                (a mandatory parent is not mapped yet); {@link Verdict#SKIP} drop the row (mandatory
     *                parent was never cloned)
     * @param ids     attribute name to the id to reference, for the references that are resolved
     * @param pending optional references left null for now, to be patched in the second pass
     * @param cleared optional references that stay null because their parent was never cloned
     */
    record RowResult(Verdict verdict, Map<String, Object> ids, List<Ref> pending, List<Ref> cleared) {
    }

    private final IdentityMapper mapper;
    private final Set<String> exportedClasses;
    private final Map<String, Map<Object, Object>> idMappings;
    private final SharedRowCheck sharedCheck;
    private final boolean skipOrphans;
    private final boolean tracking;
    private final Map<String, Boolean> sharedCache = new HashMap<>();

    private long skippedRows;
    private long clearedReferences;
    private final Map<String, Long> skippedByClass = new LinkedHashMap<>();

    /**
     * @param mapper          identity mapper, used for the legacy fallback
     * @param exportedClasses names of the entity classes present in the export
     * @param idMappings      the live {@code className -> {originalId -> newId}} table of the import
     * @param sharedCheck     decides whether an unmapped row of an exported class is shared
     * @param skipOrphans     when {@code false}, references that cannot be resolved keep the original id (legacy)
     */
    ReferenceResolver(IdentityMapper mapper,
                      Set<String> exportedClasses,
                      Map<String, Map<Object, Object>> idMappings,
                      SharedRowCheck sharedCheck,
                      boolean skipOrphans) {
        this.mapper = mapper;
        this.exportedClasses = exportedClasses == null ? Set.of() : new HashSet<>(exportedClasses);
        this.idMappings = idMappings;
        this.sharedCheck = sharedCheck;
        this.skipOrphans = skipOrphans;
        // KEEP_IDS preserves ids, so the original id is always the right one.
        this.tracking = !this.exportedClasses.isEmpty()
                && mapper.getStrategy() != IdentityStrategy.KEEP_IDS;
    }

    /**
     * Resolves one reference.
     *
     * @param finalPhase {@code true} once every entity (and every retry) has been imported: from then on a
     *                   missing parent will never appear, so {@link Status#PENDING} is not returned
     */
    Resolution resolve(Ref ref, boolean finalPhase) {
        Object originalId = ref.originalId();
        Map<Object, Object> classMap = idMappings.get(ref.refClass().getName());
        if (classMap != null) {
            Object mapped = classMap.get(originalId);
            if (mapped != null) {
                return new Resolution(Status.RESOLVED, mapped);
            }
        }

        if (!tracking || !exportedClasses.contains(ref.refClass().getName())) {
            // Not part of the export: shared/system entity, keep the original id.
            return new Resolution(Status.RESOLVED,
                    mapper.resolveReferenceId(originalId, ref.refClass(), idMappings));
        }

        if (!finalPhase) {
            return new Resolution(Status.PENDING, null);
        }

        if (!skipOrphans || isShared(ref.refClass(), originalId)) {
            return new Resolution(Status.RESOLVED, originalId);
        }
        return new Resolution(Status.ORPHAN, null);
    }

    /**
     * Resolves all references of a row and decides whether to persist, defer or skip it.
     */
    RowResult resolveRow(List<Ref> refs, boolean finalPhase, String entityName) {
        Map<String, Object> ids = new LinkedHashMap<>();
        List<Ref> pending = new ArrayList<>();
        List<Ref> cleared = new ArrayList<>();
        boolean defer = false;
        boolean skip = false;

        for (Ref ref : refs) {
            Resolution r = resolve(ref, finalPhase);
            switch (r.status()) {
                case RESOLVED -> ids.put(ref.attribute(), r.id());
                case PENDING -> {
                    if (ref.optional()) {
                        pending.add(ref);
                    } else {
                        defer = true;
                    }
                }
                case ORPHAN -> {
                    if (ref.optional()) {
                        cleared.add(ref);
                    } else {
                        skip = true;
                    }
                }
            }
        }

        if (skip) {
            skippedRows++;
            skippedByClass.merge(entityName, 1L, Long::sum);
            return new RowResult(Verdict.SKIP, ids, List.of(), List.of());
        }
        if (defer) {
            return new RowResult(Verdict.DEFER, ids, List.of(), List.of());
        }
        clearedReferences += cleared.size();
        return new RowResult(Verdict.PERSIST, ids, pending, cleared);
    }

    /**
     * Final resolution of an optional reference that was left null in the first pass. Counts it as cleared
     * when its parent was never cloned.
     */
    Resolution resolvePending(Ref ref) {
        Resolution r = resolve(ref, true);
        if (r.status() == Status.ORPHAN) {
            clearedReferences++;
        }
        return r;
    }

    /** Rows dropped because a mandatory parent was never cloned. */
    long skippedRows() {
        return skippedRows;
    }

    /** Optional references left null because their parent was never cloned. */
    long clearedReferences() {
        return clearedReferences;
    }

    /** Dropped rows by entity name. */
    Map<String, Long> skippedByClass() {
        return skippedByClass;
    }

    private boolean isShared(Class<?> refClass, Object originalId) {
        return sharedCache.computeIfAbsent(refClass.getName() + "#" + originalId,
                k -> sharedCheck != null && sharedCheck.isShared(refClass, originalId));
    }
}
