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
package tools.dynamia.viewers;


/**
 * Observer that is notified whenever a {@link ViewDescriptor} is loaded or reloaded by the
 * {@link ViewDescriptorFactory}.
 *
 * <p>A {@code ViewDescriptorInterceptor} provides a hook point for cross-cutting concerns that
 * must react to descriptor registration events — for example, applying security policies,
 * injecting default parameters, or auditing which descriptors are available at runtime.</p>
 *
 * <p>Each interceptor declares a {@link ViewDescriptorTarget} that narrows the scope of
 * descriptors it is interested in (e.g., only {@code "form"} descriptors, or only descriptors
 * for a specific domain class). The factory will only invoke {@link #intercepted(ViewDescriptor)}
 * when the loaded descriptor matches the target.</p>
 *
 * <p>Implementations are typically registered as application beans and discovered automatically
 * during the descriptor loading phase triggered by
 * {@link ViewDescriptorFactory#loadViewDescriptors()}.</p>
 *
 * @see ViewDescriptorTarget
 * @see ViewDescriptorFactory#loadViewDescriptors()
 */
public interface ViewDescriptorInterceptor {

    /**
     * Returns the target specification that determines which descriptors this interceptor
     * applies to.
     *
     * <p>The factory uses the returned {@link ViewDescriptorTarget} to filter descriptors before
     * calling {@link #intercepted(ViewDescriptor)}. Return a target that matches {@code "*"} (or
     * the equivalent wildcard) to intercept all descriptors.</p>
     *
     * @return the interception target; never {@code null}
     */
    ViewDescriptorTarget getTarget();

    /**
     * Invoked by the framework when a {@link ViewDescriptor} that matches {@link #getTarget()}
     * is loaded or reloaded.
     *
     * <p>Implementations may freely read or mutate the descriptor at this point. Mutations are
     * permanent — changes are visible to all subsequent consumers of the descriptor.</p>
     *
     * @param viewDescriptor the descriptor that was just loaded; never {@code null}
     */
    void intercepted(ViewDescriptor viewDescriptor);

}
