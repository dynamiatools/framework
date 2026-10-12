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
package tools.dynamia.ui.files;

import tools.dynamia.integration.Containers;

/**
 * Who a flow or a transferred file belongs to: the authenticated user and the tenant. Resume tokens and
 * {@link TransferStore} entries are bound to it, so another user, or the same user in another tenant, cannot use them.
 * <p>
 * Security and multi-tenancy modules register an implementation as a bean; without one everything belongs to
 * {@link #ANONYMOUS}.
 */
public interface FlowPrincipal {

    /** The principal of code with no security and no tenant. */
    FlowPrincipal ANONYMOUS = new FlowPrincipal() {
        @Override
        public String subject() {
            return "anonymous";
        }

        @Override
        public String tenant() {
            return null;
        }
    };

    /** @return the user identifier, never null */
    String subject();

    /** @return the tenant identifier, or null when there is none */
    String tenant();

    /**
     * @return the principal of the code running now: the first {@code FlowPrincipal} bean, else {@link #ANONYMOUS}
     */
    static FlowPrincipal current() {
        FlowPrincipal found = Containers.get().findObject(FlowPrincipal.class);
        return found != null ? found : ANONYMOUS;
    }

    /**
     * @param other another principal
     * @return whether both have the same user and tenant
     */
    default boolean sameAs(FlowPrincipal other) {
        return other != null && java.util.Objects.equals(subject(), other.subject())
                && java.util.Objects.equals(tenant(), other.tenant());
    }
}
