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

package tools.dynamia.modules.saas;

import tools.dynamia.commons.SimpleCache;
import tools.dynamia.modules.saas.domain.Account;

/**
 * Basic simple cache for account status. Internaylly use {@link SimpleCache} class
 */
public class AccountStatusCache {

    private static final SimpleCache<Long, Boolean> statusCache = new SimpleCache<>();

    public static boolean isStatusChanged(Account account) {
        if (account.getId() == null) {
            return false;
        }
        return Boolean.TRUE.equals(statusCache.remove(account.getId()));
    }

    public static void statusChanged(Account account) {
        if (account.getId() != null) {
            statusCache.add(account.getId(), true);
        }
    }
}
