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
package tools.dynamia.integration;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Holds an {@link ObjectContainer} for the code that is running, using a {@link ScopedValue} (final in Java 25).
 * It plays the same role as {@link ThreadLocalObjectContainer} (per-request or per-task objects, with a fallback to
 * {@link Containers}) but it is a separate class and does not replace it.
 * <p>
 * Unlike a {@code ThreadLocal}, the container is bound only while the code passed to {@link #run}, {@link #call} or
 * {@link #get} runs: there is no {@code set}/{@code clear} to forget, a nested binding shadows the outer one and the
 * outer one is back automatically (also when the code throws), and nothing can leak to later work on a pooled thread.
 * A scoped value is <strong>not inherited</strong> by threads started inside the scope (except those forked with
 * {@code StructuredTaskScope}), so code that hands work to an executor must bind the container again inside the task;
 * {@link tools.dynamia.integration.scheduling.SchedulerUtil} does it for the tasks it starts.
 *
 * <pre>{@code
 * var context = new SimpleObjectContainer();
 * context.addObject(currentUser);
 * ScopedValueObjectContainer.run(context, () -> {
 *     User user = ScopedValueObjectContainer.getObject(User.class); // the bound one, else the Spring context
 * });
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public final class ScopedValueObjectContainer {

    private static final ScopedValue<ObjectContainer> CONTAINER = ScopedValue.newInstance();

    private ScopedValueObjectContainer() {
    }

    /**
     * Runs the task with the container bound.
     *
     * @param container the container to bind
     * @param task      the task
     */
    public static void run(ObjectContainer container, Runnable task) {
        ScopedValue.where(CONTAINER, requireContainer(container)).run(task);
    }

    /**
     * Runs the task with the container bound and returns its result.
     *
     * @param container the container to bind
     * @param task      the task
     * @param <T>       the result type
     * @return the result of the task
     */
    public static <T> T get(ObjectContainer container, Supplier<T> task) {
        return ScopedValue.where(CONTAINER, requireContainer(container)).call(task::get);
    }

    /**
     * Runs the task with the container bound and returns its result. Use it for tasks that throw checked exceptions.
     *
     * @param container the container to bind
     * @param task      the task
     * @param <T>       the result type
     * @param <X>       the exception type the task may throw
     * @return the result of the task
     * @throws X whatever the task throws
     */
    public static <T, X extends Throwable> T call(ObjectContainer container, ScopedValue.CallableOp<? extends T, X> task) throws X {
        return ScopedValue.where(CONTAINER, requireContainer(container)).call(task);
    }

    /**
     * Returns the container bound in the running code.
     *
     * @return the container, or {@code null} when none is bound
     */
    public static ObjectContainer current() {
        return CONTAINER.isBound() ? CONTAINER.get() : null;
    }

    /**
     * Tells whether a container is bound in the running code.
     *
     * @return true if a container is bound
     */
    public static boolean isBound() {
        return CONTAINER.isBound();
    }

    /**
     * Finds an object by type in the bound container, falling back to {@link Containers}.
     *
     * @param clazz the object type
     * @param <T>   the type
     * @return the object, or {@code null} if neither has one
     */
    public static <T> T getObject(Class<T> clazz) {
        ObjectContainer bound = current();
        if (bound != null) {
            T obj = bound.getObject(clazz);
            if (obj != null) return obj;
        }
        return Containers.get().findObject(clazz);
    }

    /**
     * Finds an object by name and type in the bound container, falling back to {@link Containers}.
     *
     * @param name  the object name
     * @param clazz the object type
     * @param <T>   the type
     * @return the object, or {@code null} if neither has one
     */
    public static <T> T getObject(String name, Class<T> clazz) {
        ObjectContainer bound = current();
        if (bound != null) {
            T obj = bound.getObject(name, clazz);
            if (obj != null) return obj;
        }
        return Containers.get().findObject(name, clazz);
    }

    /**
     * Finds the objects of a type in the bound container, falling back to {@link Containers} when it has none.
     *
     * @param clazz the object type
     * @param <T>   the type
     * @return the objects found
     */
    public static <T> Collection<T> getObjects(Class<T> clazz) {
        ObjectContainer bound = current();
        if (bound != null) {
            Collection<T> objs = bound.getObjects(clazz);
            if (objs != null && !objs.isEmpty()) return objs;
        }
        return Containers.get().findObjects(clazz);
    }

    /**
     * Copies the objects of the bound container into the target, when the bound container is a
     * {@link SimpleObjectContainer}. Does nothing otherwise.
     *
     * @param target the container that receives the objects
     */
    public static void copyTo(SimpleObjectContainer target) {
        if (current() instanceof SimpleObjectContainer bound) {
            target.getObjects().putAll(bound.getObjects());
        }
    }

    private static ObjectContainer requireContainer(ObjectContainer container) {
        if (container == null) {
            throw new IllegalArgumentException("container is required");
        }
        return container;
    }
}
