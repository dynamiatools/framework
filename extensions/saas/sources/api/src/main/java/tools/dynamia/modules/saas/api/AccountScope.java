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

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.AccountServiceAPI;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom Spring bean scope implementation for account-scoped beans in a multi-tenant SaaS environment.
 * <p>
 * This scope manages beans at the account level, ensuring that each tenant account has its own
 * isolated instances of account-scoped beans. This is essential for maintaining proper data
 * isolation and state management in multi-tenant applications.
 * <p>
 * Beans defined with this scope will be created once per account and reused for all requests
 * within that account's context. The scope automatically handles bean lifecycle and cleanup
 * when accounts are switched or removed.
 * <p>
 * Example usage in Spring configuration:
 * <pre>{@code
 * @Component
 * @Scope("account")
 * public class AccountSpecificService {
 *     // This bean will have one instance per account
 * }
 * }</pre>
 *
 * @author Mario Serrano Leones
 * @see org.springframework.beans.factory.config.Scope
 */
public class AccountScope implements Scope {

    private final Map<Long, Map<String, Object>> accountObjects = new ConcurrentHashMap<>();

    /**
     * Retrieves a bean instance from the account scope, creating it if necessary.
     * <p>
     * This method is called by Spring when resolving account-scoped beans.
     * It ensures that each account has its own isolated instance of the bean.
     *
     * @param name the name of the bean
     * @param objectFactory the factory to create the bean if it doesn't exist
     * @return the scoped bean instance, or null if no account context is available
     */
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        Object object = null;
        Long accountId = getAccountId();
        if (accountId != null) {
            object = getAccountObjects(accountId).get(name);

            if (object == null) {
                object = objectFactory.getObject();
                getAccountObjects(accountId).put(name, object);
            }
        }
        return object;
    }

    /**
     * Removes a bean instance from the current account's scope.
     * <p>
     * This method is called when explicitly removing a bean from the scope
     * or during account cleanup operations.
     *
     * @param name the name of the bean to remove
     * @return the removed bean instance, or null if not found or no account context
     */
    @Override
    public Object remove(String name) {
        Long accountId = getAccountId();
        if (accountId != null) {
            return getAccountObjects(accountId).remove(name);
        }
        return null;
    }

    /**
     * Registers a callback to be executed when a bean is destroyed.
     * <p>
     * This implementation currently does not support destruction callbacks.
     *
     * @param name the name of the bean
     * @param callback the callback to execute on destruction
     */
    @Override
    public void registerDestructionCallback(String name, Runnable callback) {

    }

    /**
     * Resolves a contextual object for the given key.
     * <p>
     * This implementation does not provide contextual objects.
     *
     * @param key the key to resolve
     * @return always returns null
     */
    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    /**
     * Returns a unique conversation ID for the current account scope.
     * <p>
     * The conversation ID is based on the current account ID and is used
     * by Spring to identify the scope context.
     *
     * @return a conversation ID in the format "account{accountId}", or null if no account context
     */
    @Override
    public String getConversationId() {
        Long accountId = getAccountId();
        if (accountId != null) {
            return "account" + accountId;
        }
        return null;
    }


    /**
     * Retrieves or creates the bean storage map for the specified account.
     *
     * @param accountId the account identifier
     * @return a map containing all beans for the account
     */
    private Map<String, Object> getAccountObjects(Long accountId) {
        Map<String, Object> objectMap = accountObjects.get(accountId);
        if (objectMap == null) {
            objectMap = new ConcurrentHashMap<>();
            accountObjects.put(accountId, objectMap);
        }
        return objectMap;
    }

    /**
     * Retrieves the current account ID from the AccountServiceAPI.
     *
     * @return the current account ID, or null if no account context is available
     */
    private Long getAccountId() {
        AccountServiceAPI accountServiceAPI = Containers.get().findObject(AccountServiceAPI.class);
        if (accountServiceAPI != null) {
            return accountServiceAPI.getCurrentAccountId();
        }
        return null;
    }
}
