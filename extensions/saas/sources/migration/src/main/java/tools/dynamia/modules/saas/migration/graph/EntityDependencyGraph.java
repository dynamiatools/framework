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
package tools.dynamia.modules.saas.migration.graph;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.dynamia.integration.sterotypes.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Builds a directed dependency graph of JPA entity classes and produces a
 * topologically sorted import order (parents before children).
 *
 * <h3>Graph construction</h3>
 * For each entity {@code E} in the input set, every {@code @ManyToOne} /
 * {@code @OneToOne} attribute that references another entity {@code P} in the
 * same set creates a directed edge {@code P → E} (P must be imported before E).
 *
 * <h3>Topological sort</h3>
 * Kahn's BFS algorithm is used.  If a cycle is detected (unlikely with
 * well-formed JPA models), the remaining nodes are appended in their original
 * discovery order so the pipeline can still proceed.
 *
 * @author Mario Serrano Leones
 */
@Service
public class EntityDependencyGraph {

    private static final Logger log = LoggerFactory.getLogger(EntityDependencyGraph.class);

    private final EntityManagerFactory emf;

    public EntityDependencyGraph(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Returns the input list sorted so that every entity appears after all
     * entities it references (i.e., safe insert order).
     *
     * @param entityClasses the set of entity classes to sort
     * @return a new list in topological order
     */
    public List<Class<?>> topologicalSort(List<Class<?>> entityClasses) {
        if (entityClasses == null || entityClasses.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Class<?>> classSet = new HashSet<>(entityClasses);

        // adjacency: node → set of nodes that depend on it  (predecessors of edges)
        Map<Class<?>, Set<Class<?>>> dependents = new HashMap<>();
        // in-degree: how many entities this entity depends on (within our set)
        Map<Class<?>, Integer> inDegree = new HashMap<>();

        for (Class<?> clazz : entityClasses) {
            dependents.putIfAbsent(clazz, new HashSet<>());
            inDegree.putIfAbsent(clazz, 0);
        }

        // Build graph from JPA metamodel
        for (Class<?> child : entityClasses) {
            try {
                EntityType<?> entityType = emf.getMetamodel().entity(child);
                for (SingularAttribute<?, ?> attr : entityType.getSingularAttributes()) {
                    PersistentAttributeType pt = attr.getPersistentAttributeType();
                    if (pt == PersistentAttributeType.MANY_TO_ONE
                            || pt == PersistentAttributeType.ONE_TO_ONE) {
                        Class<?> parent = attr.getJavaType();
                        if (classSet.contains(parent) && !parent.equals(child)) {
                            // child depends on parent → edge: parent → child
                            dependents.get(parent).add(child);
                            inDegree.merge(child, 1, Integer::sum);
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                // Entity not in metamodel; skip
                log.warn("[Migration] Entity not found in JPA metamodel, skipping graph analysis: {}", child.getName());
            }
        }

        // Kahn's BFS topological sort
        Queue<Class<?>> queue = new ArrayDeque<>();
        for (Class<?> clazz : entityClasses) {
            if (inDegree.get(clazz) == 0) {
                queue.add(clazz);
            }
        }

        List<Class<?>> sorted = new ArrayList<>(entityClasses.size());
        Set<Class<?>> visited = new HashSet<>();

        while (!queue.isEmpty()) {
            Class<?> current = queue.poll();
            sorted.add(current);
            visited.add(current);

            for (Class<?> dependent : dependents.get(current)) {
                int newDegree = inDegree.merge(dependent, -1, Integer::sum);
                if (newDegree == 0) {
                    queue.add(dependent);
                }
            }
        }

        // Cycle fallback: append remaining unvisited nodes
        if (sorted.size() < entityClasses.size()) {
            log.warn("[Migration] Dependency graph has cycles; appending {} unresolved entities in original order",
                    entityClasses.size() - sorted.size());
            for (Class<?> clazz : entityClasses) {
                if (!visited.contains(clazz)) {
                    sorted.add(clazz);
                }
            }
        }

        log.debug("[Migration] Topological sort result: {}",
                sorted.stream().map(Class::getSimpleName).toList());
        return sorted;
    }
}

