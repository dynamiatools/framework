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

package tools.dynamia.modules.saas.ui;

import org.springframework.stereotype.Component;
import tools.dynamia.modules.saas.AccountContext;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.NavigationElement;

import java.io.Serializable;

/**
 * @author Mario Serrano Leones
 */
@Component
public class AccountNavigationRestriction implements tools.dynamia.navigation.NavigationRestriction, Serializable {

    /**
     * Check if navigation element is a/from module with access permission
     *
     * @param element
     * @return
     */
    @Override
    public Boolean allowAccess(NavigationElement element) {

        Account account = AccountContext.getCurrent().getAccount();
        if (account == null) {
            return false;
        }


        if (account.getProfile() != null) {

            var restrictions = account.getProfile().getRestrictions();

            var rest = restrictions.stream().filter(r -> r.getValue().equalsIgnoreCase(element.getVirtualPath())).findFirst();

            if (rest.isEmpty() && !(element instanceof Module)) {
                return null;
            }

            if (rest.isPresent()) {
                var accessControl = rest.get().getAccessControl();
                switch (accessControl) {
                    case ALLOWED:
                        return true;
                    case DENIED:
                        return false;
                    case DELEGATE:
                        return null;
                }
            }
        } else return account.getType().isAdmin();

        return false;
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
