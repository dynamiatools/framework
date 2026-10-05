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
package tools.dynamia.integration.context;

import tools.dynamia.integration.CloneableThreadLocalObject;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.ObjectContainer;
import tools.dynamia.integration.ScopedValueObjectContainer;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.integration.ThreadLocalContextProvider;
import tools.dynamia.integration.ThreadLocalObjectAware;
import tools.dynamia.integration.ThreadLocalObjectContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Facade for "context variables": the objects that code running for a request or a task needs as the <em>current</em>
 * ones (current user, branch, account...). Application code reads and binds them here and does not care how they are
 * stored: internally it uses {@link ScopedValueObjectContainer} (a {@link ScopedValue}), and still reads the legacy
 * {@link ThreadLocalObjectContainer} while applications migrate.
 *
 * <h2>Read</h2>
 * {@link #get(Class)}, {@link #find(Class)}, {@link #require(Class)} and {@link #getAll(Class)} look in this order:
 * the objects bound with {@link #with(Object...)} (or by {@code SchedulerUtil}), the legacy thread-local container,
 * and the Spring {@link Containers}. Note that a Spring request or session scoped bean found in the last step may only
 * fail when it is used outside of a request.
 *
 * <h2>Bind</h2>
 * Binding is always by scope, never {@code set}/{@code clear}. Scopes <strong>add up</strong>: an inner scope sees the
 * objects of the outer one, and its own objects win for the same type. The binding disappears when the code ends,
 * also if it throws.
 * <pre>{@code
 * ObjectsContext.with(branch).run(() -> {
 *     ObjectsContext.with(cashbox).run(() -> {
 *         Branch b = ObjectsContext.get(Branch.class);   // still visible here
 *         Cashbox c = ObjectsContext.require(Cashbox.class);
 *     });
 * });
 * }</pre>
 *
 * <h2>Propagate to other threads</h2>
 * A scoped value is not inherited by new threads. {@link #capture()} takes an immutable {@link Snapshot} of the
 * context (including the state contributed by {@link ContextCapturer} beans) that re-applies it inside a task;
 * {@link #wrap(Executor)} does it for every task of an executor.
 * <pre>{@code
 * var snapshot = ObjectsContext.capture();
 * executor.submit(snapshot.wrap(() -> ObjectsContext.get(Branch.class)));
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public final class ObjectsContext {

    private static volatile boolean legacyBridge = true;

    private ObjectsContext() {
    }

    // ── Configuration ────────────────────────────────────────────────────────

    /**
     * Sets whether a {@link Snapshot} also fills the legacy {@link ThreadLocalObjectContainer} while it runs (and puts
     * back its previous value afterwards). It is on by default, because existing code still reads that container; turn
     * it off once nothing does.
     *
     * @param enabled true to keep filling the legacy container
     */
    public static void legacyThreadLocalBridge(boolean enabled) {
        legacyBridge = enabled;
    }

    /**
     * Tells whether snapshots also fill the legacy {@link ThreadLocalObjectContainer}.
     *
     * @return true if the legacy bridge is on
     */
    public static boolean isLegacyThreadLocalBridge() {
        return legacyBridge;
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    /**
     * Finds the current object of a type.
     *
     * @param type the object type
     * @param <T>  the type
     * @return the object, or {@code null} if there is none
     */
    public static <T> T get(Class<T> type) {
        ObjectContainer bound = ScopedValueObjectContainer.current();
        if (bound != null) {
            T found = bound.getObject(type);
            if (found != null) return found;
        }
        ObjectContainer legacy = ThreadLocalObjectContainer.get();
        if (legacy != null) {
            T found = legacy.getObject(type);
            if (found != null) return found;
        }
        return Containers.get().findObject(type);
    }

    /**
     * Finds the current object with a name and type.
     *
     * @param name the object name
     * @param type the object type
     * @param <T>  the type
     * @return the object, or {@code null} if there is none
     */
    public static <T> T get(String name, Class<T> type) {
        ObjectContainer bound = ScopedValueObjectContainer.current();
        if (bound != null) {
            T found = bound.getObject(name, type);
            if (found != null) return found;
        }
        ObjectContainer legacy = ThreadLocalObjectContainer.get();
        if (legacy != null) {
            T found = legacy.getObject(name, type);
            if (found != null) return found;
        }
        return Containers.get().findObject(name, type);
    }

    /**
     * Finds the current object of a type.
     *
     * @param type the object type
     * @param <T>  the type
     * @return the object, or empty if there is none
     */
    public static <T> Optional<T> find(Class<T> type) {
        return Optional.ofNullable(get(type));
    }

    /**
     * Finds the current object of a type, which must exist.
     *
     * @param type the object type
     * @param <T>  the type
     * @return the object
     * @throws ContextException if there is none
     */
    public static <T> T require(Class<T> type) {
        T found = get(type);
        if (found == null) {
            throw new ContextException("No " + type.getName() + " in the current context");
        }
        return found;
    }

    /**
     * Finds the current objects of a type: those of the first source that has any (bound, legacy, Spring).
     *
     * @param type the object type
     * @param <T>  the type
     * @return the objects, possibly empty
     */
    public static <T> Collection<T> getAll(Class<T> type) {
        ObjectContainer bound = ScopedValueObjectContainer.current();
        if (bound != null) {
            Collection<T> found = bound.getObjects(type);
            if (found != null && !found.isEmpty()) return found;
        }
        ObjectContainer legacy = ThreadLocalObjectContainer.get();
        if (legacy != null) {
            Collection<T> found = legacy.getObjects(type);
            if (found != null && !found.isEmpty()) return found;
        }
        return Containers.get().findObjects(type);
    }

    // ── Bind ─────────────────────────────────────────────────────────────────

    /**
     * Starts a scope that binds the given objects, on top of the current context.
     *
     * @param objects the objects to bind; {@code null}s are ignored
     * @return the scope, to run code in
     */
    public static Scope with(Object... objects) {
        var scope = new Scope();
        if (objects != null) {
            for (Object object : objects) {
                if (object != null) {
                    scope.own.addObject(object);
                }
            }
        }
        return scope;
    }

    /**
     * A set of objects to bind while some code runs. See {@link ObjectsContext#with(Object...)}.
     */
    public static final class Scope {

        private final SimpleObjectContainer own = new SimpleObjectContainer();

        private Scope() {
        }

        /**
         * Adds a named object to the scope.
         *
         * @param name   the name
         * @param object the object
         * @return this scope
         */
        public Scope add(String name, Object object) {
            own.addObject(name, object);
            return this;
        }

        /**
         * Runs the task with the scope bound.
         *
         * @param task the task
         */
        public void run(Runnable task) {
            ScopedValueObjectContainer.run(layer(), task);
        }

        /**
         * Runs the task with the scope bound and returns its result.
         *
         * @param task the task
         * @param <T>  the result type
         * @return the result
         */
        public <T> T get(Supplier<T> task) {
            return ScopedValueObjectContainer.call(layer(), task::get);
        }

        /**
         * Runs the task with the scope bound and returns its result. Use it for tasks that throw checked exceptions.
         *
         * @param task the task
         * @param <T>  the result type
         * @param <X>  the exception type the task may throw
         * @return the result
         * @throws X whatever the task throws
         */
        public <T, X extends Throwable> T call(ScopedValue.CallableOp<? extends T, X> task) throws X {
            return ScopedValueObjectContainer.call(layer(), task);
        }

        private ObjectContainer layer() {
            return new LayeredObjectContainer(ScopedValueObjectContainer.current(), own);
        }
    }

    // ── Propagate ────────────────────────────────────────────────────────────

    /**
     * Captures the current context so it can be re-applied in another thread.
     * <p>
     * What is captured: the bound objects, on top of the legacy thread-local container when it is initialized (the same
     * order as {@link #get(Class)}); or, when none are bound, the legacy thread-local container; or, when there is
     * neither, the beans that implement {@link ThreadLocalObjectAware}, cloned if they are
     * {@link CloneableThreadLocalObject}, and the values of the {@link ThreadLocalContextProvider} beans), plus the
     * binding of every {@link ContextCapturer} bean.
     *
     * @return the snapshot
     */
    @SuppressWarnings("deprecation")
    public static Snapshot capture() {
        var extra = new SimpleObjectContainer();
        ObjectContainer base = null;

        ObjectContainer bound = ScopedValueObjectContainer.current();
        if (bound != null) {
            ObjectContainer top = bound; // layered containers are immutable
            if (bound instanceof SimpleObjectContainer) {
                var copy = new SimpleObjectContainer();
                ScopedValueObjectContainer.copyTo(copy); // it is mutable: copy it
                top = copy;
            }
            if (ThreadLocalObjectContainer.isInitialized()) {
                // same order as get(): the bound objects win over the legacy thread-local container
                var legacy = new SimpleObjectContainer();
                ThreadLocalObjectContainer.copyTo(legacy);
                base = new LayeredObjectContainer(legacy, top);
            } else {
                base = top;
            }
        } else if (ThreadLocalObjectContainer.isInitialized()) {
            ThreadLocalObjectContainer.copyTo(extra);
        } else {
            collectSessionObjects(extra);
        }

        List<ContextCapturer.Binding> bindings = new ArrayList<>();
        Collection<ContextCapturer> capturers = Containers.get().findObjects(ContextCapturer.class);
        if (capturers != null) {
            for (ContextCapturer capturer : capturers) {
                ContextCapturer.Binding binding = capturer.capture();
                if (binding != null) {
                    bindings.add(binding);
                }
            }
        }
        return new Snapshot(new LayeredObjectContainer(base, extra), bindings);
    }

    /**
     * Wraps an executor so that every task it runs gets the context of the code that submitted it.
     *
     * @param executor the executor to wrap
     * @return the wrapping executor
     */
    public static Executor wrap(Executor executor) {
        return command -> executor.execute(capture().wrap(command));
    }

    @SuppressWarnings("deprecation")
    private static void collectSessionObjects(SimpleObjectContainer context) {
        Collection<ThreadLocalObjectAware> sessionBeans = Containers.get().findObjects(ThreadLocalObjectAware.class);
        if (sessionBeans != null) {
            sessionBeans.forEach(bean -> {
                if (bean instanceof CloneableThreadLocalObject cloneable) {
                    try {
                        context.addObject(cloneable.clone());
                    } catch (Exception e) {
                        context.addObject(bean); // fall back to the reference
                    }
                } else {
                    context.addObject(bean);
                }
            });
        }

        Collection<ThreadLocalContextProvider> providers = Containers.get().findObjects(ThreadLocalContextProvider.class);
        if (providers != null) {
            providers.forEach(provider -> {
                Map<String, Object> contextObjects = provider.getContextObjects();
                if (contextObjects != null) {
                    contextObjects.forEach(context::addObject);
                }
            });
        }
    }

    /**
     * An immutable capture of the context, to re-apply it in another thread. See {@link ObjectsContext#capture()}.
     */
    public static final class Snapshot {

        private final ObjectContainer container;
        private final List<ContextCapturer.Binding> bindings;

        private Snapshot(ObjectContainer container, List<ContextCapturer.Binding> bindings) {
            this.container = container;
            this.bindings = List.copyOf(bindings);
        }

        /**
         * Runs the task with the captured context applied.
         *
         * @param task the task
         */
        public void run(Runnable task) {
            call(() -> {
                task.run();
                return null;
            });
        }

        /**
         * Runs the task with the captured context applied and returns its result.
         *
         * @param task the task
         * @param <T>  the result type
         * @return the result
         */
        public <T> T get(Supplier<T> task) {
            return call(task::get);
        }

        /**
         * Runs the task with the captured context applied. Use it for tasks that throw checked exceptions.
         *
         * @param task the task
         * @param <T>  the result type
         * @param <X>  the exception type the task may throw
         * @return the result
         * @throws X whatever the task throws
         */
        public <T, X extends Throwable> T call(ScopedValue.CallableOp<? extends T, X> task) throws X {
            return bind(0, task);
        }

        /**
         * Returns a runnable that runs the task with the captured context applied. The supplier and callable variants
         * have their own names ({@link #wrapSupplier}, {@link #wrapCallable}) so lambdas are never ambiguous.
         *
         * @param task the task
         * @return the wrapping runnable
         */
        public Runnable wrap(Runnable task) {
            return () -> run(task);
        }

        /**
         * Returns a supplier that runs the task with the captured context applied.
         *
         * @param task the task
         * @param <T>  the result type
         * @return the wrapping supplier
         */
        public <T> Supplier<T> wrapSupplier(Supplier<T> task) {
            return () -> get(task);
        }

        /**
         * Returns a callable that runs the task with the captured context applied.
         *
         * @param task the task
         * @param <T>  the result type
         * @return the wrapping callable
         */
        public <T> Callable<T> wrapCallable(Callable<T> task) {
            return () -> call(task::call);
        }

        private <T, X extends Throwable> T bind(int index, ScopedValue.CallableOp<? extends T, X> task) throws X {
            if (index == bindings.size()) {
                return ScopedValueObjectContainer.call(container, () -> withLegacyContainer(task));
            }
            return bindings.get(index).call(() -> bind(index + 1, task));
        }

        private <T, X extends Throwable> T withLegacyContainer(ScopedValue.CallableOp<? extends T, X> task) throws X {
            if (!legacyBridge) {
                return task.call();
            }
            ObjectContainer previous = ThreadLocalObjectContainer.get();
            ThreadLocalObjectContainer.set(container);
            try {
                return task.call();
            } finally {
                if (previous != null) {
                    ThreadLocalObjectContainer.set(previous);
                } else {
                    ThreadLocalObjectContainer.clear();
                }
            }
        }
    }
}
