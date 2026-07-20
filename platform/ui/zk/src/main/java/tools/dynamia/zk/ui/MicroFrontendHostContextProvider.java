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
package tools.dynamia.zk.ui;

import java.util.Map;

/**
 * Supplies cross-cutting context values (tenant id, locale, current user, API base URL, auth
 * token, feature flags, etc.) that every {@link MicroFrontend} instance on the page automatically
 * receives, so ZUL authors don't have to wire the same values by hand on every
 * {@code <microfrontend>} tag.
 * <p>
 * Register an implementation as a Spring bean (e.g. annotate it with
 * {@code tools.dynamia.integration.sterotypes.Component}); every registered provider found via
 * {@link tools.dynamia.integration.Containers#findObjects(Class)} is called and merged (later
 * providers overriding earlier ones on key collisions) into the {@code dynamiaHost} prop of every
 * mounted microfrontend. The mounted bundle reads it like any other prop, e.g. {@code this.dynamiaHost}
 * in a custom element, or {@code props.dynamiaHost} in a mount-fn.
 * <p>
 * Not delivered when {@link MicroFrontend#getMode()} resolves to {@link MicroFrontend#MODE_AUTO}:
 * that mode has no prop channel at all, since the bundle self-mounts independently.
 *
 * Example:
 * <pre>{@code
 * @Component
 * public class TenantHostContextProvider implements MicroFrontendHostContextProvider {
 *     @Override
 *     public Map<String, Object> getHostContext() {
 *         Account account = AccountServiceAPI.getCurrentAccount();
 *         return Map.of(
 *             "tenantId", account.getId(),
 *             "locale", LocaleUtils.getCurrentLocale().toLanguageTag(),
 *             "apiBaseUrl", "/api"
 *         );
 *     }
 * }
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 */
public interface MicroFrontendHostContextProvider {

    /**
     * Returns context values to merge into the {@code dynamiaHost} prop of every mounted
     * microfrontend.
     *
     * @return context values, must be JSON-serializable; null is treated as empty
     */
    Map<String, Object> getHostContext();
}
