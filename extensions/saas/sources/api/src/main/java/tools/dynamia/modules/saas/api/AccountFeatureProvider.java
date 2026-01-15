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

package tools.dynamia.modules.saas.api;

/**
 * Provider interface for defining account features in a SaaS multi-tenant environment.
 * <p>
 * This interface allows modules to register features that can be enabled or disabled
 * per account. Features can be used to control access to specific functionality,
 * allowing different subscription tiers or custom feature sets for different accounts.
 * <p>
 * Implementations should be registered as Spring beans and will be automatically
 * discovered by the SaaS module to populate the available features list.
 * <p>
 * Example usage:
 * <pre>{@code
 * @Component
 * public class AdvancedReportsFeature implements AccountFeatureProvider {
 *     public String getId() {
 *         return "advanced-reports";
 *     }
 *
 *     public String getName() {
 *         return "Advanced Reports";
 *     }
 * }
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public interface AccountFeatureProvider {

    /**
     * Returns the unique identifier for this feature.
     * This ID is used to reference the feature programmatically throughout the application.
     *
     * @return the feature identifier (e.g., "advanced-reports", "api-access")
     */
    String getId();

    /**
     * Returns the human-readable name of this feature.
     * This name is typically displayed in the user interface for feature management.
     *
     * @return the feature display name
     */
    String getName();

}
