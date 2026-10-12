
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.dynamia.modules.saas.AccountResolver;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.services.AccountService;
import tools.dynamia.web.util.HttpUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * @author Mario Serrano Leones
 */
@Component
public class HttpAccountResolver implements AccountResolver {

    @Autowired
    private AccountService service;

    private static final String ATTRIBUTE_SAAS_ACCOUNT = "saas_account";

    @Override
    public Account resolve() {

        try {
            Account account = null;
            HttpServletRequest request = getHttpRequest();
            if (request != null) {

                Long accountId = (Long) request.getAttribute(AccountServiceAPI.CURRENT_ACCOUNT_ID_ATTRIBUTE);
                if(accountId!=null){
                    account = service.getAccountById(accountId);
                }

                if (account == null) {
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        account = (Account) session.getAttribute(ATTRIBUTE_SAAS_ACCOUNT);
                        if (account == null) {
                            account = service.getAccount(request);
                            if (account != null) {
                                session.setAttribute(ATTRIBUTE_SAAS_ACCOUNT, account);
                            }
                        }
                    }
                }
            }

            return account;
        } catch (Exception e) {
            return null;
        }
    }

    protected HttpServletRequest getHttpRequest() {
        return HttpUtils.getCurrentRequest();
    }
}
