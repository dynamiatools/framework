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

/**
 * Contributes extra state to an {@link ObjectsContext.Snapshot}: state that lives outside the objects container, such
 * as a value bound with a {@link ScopedValue}. Register an implementation as a bean; every capturer found in the
 * container is asked, on the thread that starts the async work, for a {@link Binding} that re-applies that state
 * inside the task.
 *
 * <pre>{@code
 * @Component
 * public class TenantCapturer implements ContextCapturer {
 *     public Binding capture() {
 *         Long tenant = Tenants.current();            // read on the calling thread
 *         return tenant == null ? null : new Binding() {
 *             public <T, X extends Throwable> T call(ScopedValue.CallableOp<? extends T, X> op) throws X {
 *                 return Tenants.callBoundTo(tenant, op); // applied inside the task
 *             }
 *         };
 *     }
 * }
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public interface ContextCapturer {

    /**
     * Captures the state of the calling thread.
     *
     * @return the binding to apply inside the task, or {@code null} when there is nothing to carry
     */
    Binding capture();

    /**
     * Re-applies captured state around a piece of work.
     */
    interface Binding {

        /**
         * Runs the operation with the captured state applied.
         *
         * @param op  the operation
         * @param <T> the result type
         * @param <X> the exception type the operation may throw
         * @return the result of the operation
         * @throws X whatever the operation throws
         */
        <T, X extends Throwable> T call(ScopedValue.CallableOp<? extends T, X> op) throws X;
    }
}
