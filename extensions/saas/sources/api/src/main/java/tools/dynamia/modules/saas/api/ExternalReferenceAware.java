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
 * Interface for entities that maintain references to external systems or identifiers.
 * <p>
 * This interface is useful when integrating with third-party services, payment gateways,
 * or external APIs where entities need to store external identifiers for synchronization
 * or cross-referencing purposes.
 * <p>
 * Common use cases include:
 * <ul>
 *   <li>Storing Stripe customer IDs</li>
 *   <li>Maintaining external CRM references</li>
 *   <li>Tracking third-party service subscription IDs</li>
 *   <li>Linking to external accounting systems</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * @Entity
 * public class Account implements ExternalReferenceAware {
 *     @Column
 *     private String externalRef;
 *
 *     public String getExternalRef() {
 *         return externalRef;
 *     }
 *
 *     public void setExternalRef(String externalRef) {
 *         this.externalRef = externalRef;
 *     }
 * }
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public interface ExternalReferenceAware {

    /**
     * Returns the external reference identifier.
     * <p>
     * This typically contains an ID or reference code from an external system.
     *
     * @return the external reference, or null if not set
     */
    String getExternalRef();

    /**
     * Sets the external reference identifier.
     *
     * @param externalRef the external reference to assign to this entity
     */
    void setExternalRef(String externalRef);
}
