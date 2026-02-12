
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.CloneableThreadLocalObject;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.ThreadLocalObjectContainer;
import tools.dynamia.integration.sterotypes.Component;
import tools.dynamia.modules.saas.api.AccountException;
import tools.dynamia.modules.saas.api.dto.AccountDTO;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.services.AccountService;

import java.io.Serializable;
import java.time.ZoneId;
import java.util.Locale;

/**
 * @author Mario Serrano Leones
 */
@Component("accountSessionHolder")
@SessionScope
public class AccountSessionHolder implements Serializable, CloneableThreadLocalObject {


    @JsonIgnore
    private transient AccountService service;

    private Locale accountLocale;
    private ZoneId accountTimeZone;
    private Long currentId;
    private AccountDTO currentDTO;

    public AccountSessionHolder() {
        this.service = Containers.get().findObject(AccountService.class);
    }

    public AccountSessionHolder(AccountService service) {
        this.service = service;
    }

    public static AccountSessionHolder get() {
        AccountSessionHolder accountSessionHolder = null;
        try {
            accountSessionHolder = ThreadLocalObjectContainer.getObject(AccountSessionHolder.class);
        } catch (Exception e) {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                accountSessionHolder = (AccountSessionHolder) attributes.getAttribute("accountSessionHolder", RequestAttributes.SCOPE_SESSION);
            }
        }

        if (accountSessionHolder == null) {
            throw new AccountException("Cannot found instance of " + AccountSessionHolder.class + " in current thread.");
        }
        return accountSessionHolder;
    }

    public Account getCurrent() {
        return currentId != null ? getService().getAccountById(currentId) : null;
    }

    public void setCurrent(final Account account) {
        if (account != null) {
            try {
                DomainUtils.lookupCrudService().executeWithinTransaction(() -> {
                    var current = getService().getAccountById(account.getId());
                    loadAccountLocale(current);
                    loadAccountTimeZone(current);
                    currentId = current.getId();
                    currentDTO = current.toDTO();
                });
            } catch (Exception e) {
                //ignore
            }
        }
    }

    private void loadAccountTimeZone(Account current) {
        try {
            accountTimeZone = current.getTimeZone() != null ? ZoneId.of(current.getTimeZone()) : null;
        } catch (Exception e) {
            accountTimeZone = null;
        }
    }

    private void loadAccountLocale(Account current) {
        try {
            accountLocale = current.getLocale() != null ? Locale.forLanguageTag(current.getLocale()) : null;
        } catch (Exception e) {
            accountLocale = null;
        }
    }


    public AccountDTO toDTO() {
        return currentDTO;
    }

    public Long getId() {
        return currentId;
    }

    public Locale getAccountLocale() {
        if (accountLocale == null) {
            accountLocale = Locale.getDefault();
        }
        return accountLocale;
    }

    private AccountService getService() {
        if (service == null) {
            service = Containers.get().findObject(AccountService.class);
        }
        return service;
    }

    public ZoneId getAccountTimeZone() {
        if (accountTimeZone == null) {
            accountTimeZone = ZoneId.systemDefault();
        }
        return accountTimeZone;
    }

    public AccountSessionHolder clone() {
        AccountSessionHolder accountSessionHolder = new AccountSessionHolder(service);
        accountSessionHolder.currentId = this.currentId;
        accountSessionHolder.accountLocale = this.accountLocale;
        accountSessionHolder.accountTimeZone = this.accountTimeZone;
        accountSessionHolder.currentDTO = this.currentDTO;
        return accountSessionHolder;
    }


}
