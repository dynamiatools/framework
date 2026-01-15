
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
 * Interface for entities or components that belong to a specific account in a multi-tenant SaaS environment.
 * <p>
 * Implementing this interface allows objects to be associated with an account, enabling proper data isolation
 * and tenant-specific operations. This is a fundamental interface for multi-tenancy support, ensuring that
 * each entity knows which account it belongs to.
 * <p>
 * Example usage:
 * <pre>{@code
 * @Entity
 * public class Customer implements AccountAware {
 *     @Column
 *     private Long accountId;
 *
 *     public Long getAccountId() {
 *         return accountId;
 *     }
 *
 *     public void setAccountId(Long accountId) {
 *         this.accountId = accountId;
 *     }
 * }
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public interface AccountAware {

    /**
     * Returns the unique identifier of the account this entity belongs to.
     *
     * @return the account ID
     */
    Long getAccountId();

    /**
     * Sets the account this entity belongs to.
     *
     * @param account the account ID to assign to this entity
     */
    void setAccountId(Long account);

}
