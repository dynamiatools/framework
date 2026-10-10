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
package tools.dynamia.modules.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.ui.files.FlowPrincipal;

/**
 * Who owns a resume token or a transferred file: the authenticated user and, when the saas module is present, the account
 * of the current session. Without it everything would belong to the anonymous principal and any user could use another
 * user's references.
 */
@Component
public class SecurityFlowPrincipal implements FlowPrincipal {

    @Override
    public String subject() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            return FlowPrincipal.ANONYMOUS.subject();
        }
        return auth.getName();
    }

    @Override
    public String tenant() {
        AccountServiceAPI accounts = Containers.get().findObject(AccountServiceAPI.class);
        Long id = accounts == null ? null : accounts.getCurrentAccountId();
        return id == null ? null : String.valueOf(id);
    }
}
