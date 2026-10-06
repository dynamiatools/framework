/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.domain;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Describes which associations of an entity must be initialized while the persistence context is still open, so the
 * entity can be used after the CrudService returns it (no Open Persistence In View).
 * <p>
 * A plan is made of explicit property <em>paths</em> (a path walks through collections: {@code "lines.subs"}) and,
 * optionally, "all collections up to a depth". Plans are immutable; use {@link #and(LoadPlan)} to combine them.
 *
 * @see InitializeOnLoad
 */
public final class LoadPlan {

    /**
     * A plan that initializes nothing.
     */
    public static final LoadPlan EMPTY = new LoadPlan(Collections.emptySet(), false, 0);

    private static final Map<Class<?>, LoadPlan> ANNOTATED = new ConcurrentHashMap<>();

    private final Set<String> paths;
    private final boolean allCollections;
    private final int depth;

    private LoadPlan(Set<String> paths, boolean allCollections, int depth) {
        this.paths = paths;
        this.allCollections = allCollections;
        this.depth = depth;
    }

    /**
     * Creates a plan from property paths. Blank paths are ignored.
     */
    public static LoadPlan of(String... paths) {
        if (paths == null || paths.length == 0) {
            return EMPTY;
        }
        Set<String> clean = new LinkedHashSet<>();
        for (String path : paths) {
            if (path != null && !path.isBlank()) {
                clean.add(path.trim());
            }
        }
        return clean.isEmpty() ? EMPTY : new LoadPlan(Collections.unmodifiableSet(clean), false, 0);
    }

    /**
     * Creates a plan that initializes every to-many association, nested up to {@code depth} levels.
     */
    public static LoadPlan allCollections(int depth) {
        return depth <= 0 ? EMPTY : new LoadPlan(Collections.emptySet(), true, depth);
    }

    /**
     * Returns the plan declared with {@link InitializeOnLoad} on the entity class (or a superclass) and on its fields
     * and getters, or {@link #EMPTY} when nothing is annotated.
     */
    public static LoadPlan annotatedOf(Class<?> type) {
        if (type == null) {
            return EMPTY;
        }
        return ANNOTATED.computeIfAbsent(type, LoadPlan::readAnnotation);
    }

    private static LoadPlan readAnnotation(Class<?> type) {
        LoadPlan plan = EMPTY;

        InitializeOnLoad ann = type.getAnnotation(InitializeOnLoad.class);
        if (ann != null) {
            plan = of(ann.value());
            if (ann.allCollections()) {
                plan = plan.and(allCollections(ann.depth()));
            }
        }

        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                plan = plan.and(fromMember(field.getAnnotation(InitializeOnLoad.class), field.getName()));
            }
            for (Method method : c.getDeclaredMethods()) {
                String property = propertyName(method);
                if (property != null) {
                    plan = plan.and(fromMember(method.getAnnotation(InitializeOnLoad.class), property));
                }
            }
        }
        return plan;
    }

    /**
     * The plan of a field or getter annotation: the property itself plus its relative paths.
     */
    private static LoadPlan fromMember(InitializeOnLoad ann, String property) {
        if (ann == null) {
            return EMPTY;
        }
        Set<String> paths = new LinkedHashSet<>();
        paths.add(property);
        for (String sub : ann.value()) {
            if (sub != null && !sub.isBlank()) {
                paths.add(property + "." + sub.trim());
            }
        }
        return new LoadPlan(Collections.unmodifiableSet(paths), false, 0);
    }

    private static String propertyName(Method method) {
        if (method.getParameterCount() != 0 || !method.isAnnotationPresent(InitializeOnLoad.class)) {
            return null;
        }
        String name = method.getName();
        String property = null;
        if (name.startsWith("get") && name.length() > 3) {
            property = name.substring(3);
        } else if (name.startsWith("is") && name.length() > 2) {
            property = name.substring(2);
        }
        return property == null ? null : Character.toLowerCase(property.charAt(0)) + property.substring(1);
    }

    /**
     * Combines this plan with another one.
     */
    public LoadPlan and(LoadPlan other) {
        if (other == null || other.isEmpty()) {
            return this;
        }
        if (isEmpty()) {
            return other;
        }
        Set<String> merged = new LinkedHashSet<>(paths);
        merged.addAll(other.paths);
        return new LoadPlan(Collections.unmodifiableSet(merged), allCollections || other.allCollections,
                Math.max(depth, other.depth));
    }

    /**
     * Combines this plan with more paths.
     */
    public LoadPlan and(String... morePaths) {
        return and(of(morePaths));
    }

    public boolean isEmpty() {
        return paths.isEmpty() && !allCollections;
    }

    /**
     * The explicit property paths.
     */
    public Set<String> getPaths() {
        return paths;
    }

    /**
     * Whether every to-many association must be initialized.
     */
    public boolean isAllCollections() {
        return allCollections;
    }

    /**
     * The nesting level used when {@link #isAllCollections()} is true.
     */
    public int getDepth() {
        return depth;
    }

    @Override
    public String toString() {
        return "LoadPlan" + (allCollections ? "[all collections, depth " + depth + "]" : "") + Arrays.toString(paths.toArray());
    }
}
