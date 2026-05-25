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

package tools.dynamia.modules.saas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.AbstractService;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.saas.api.AccountStatsList;
import tools.dynamia.modules.saas.api.dto.AccountDTO;
import tools.dynamia.modules.saas.api.enums.AccountPeriodicity;
import tools.dynamia.modules.saas.api.enums.AccountStatus;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.domain.AccountLog;
import tools.dynamia.modules.saas.jpa.AccountParameter;
import tools.dynamia.modules.saas.services.AccountService;
import tools.dynamia.web.util.HttpUtils;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/saas", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
public class AccountApiController extends AbstractService {

    private static final AccountDTO NO_ACCOUNT;

    static {
        NO_ACCOUNT = new AccountDTO();
        NO_ACCOUNT.setId(1L);
        NO_ACCOUNT.setStatus(AccountStatus.CANCELED);
        NO_ACCOUNT.setStatusDate(LocalDateTime.now());
        NO_ACCOUNT.setStatusDescription("Invalid License");
    }

    private final AccountService service;
    private final AccountServiceAPI serviceAPI;

    @Autowired
    public AccountApiController(AccountService service, AccountServiceAPI serviceAPI) {
        this.service = service;
        this.serviceAPI = serviceAPI;
    }

    @GetMapping("/account/{uuid}")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable("uuid") String uuid, HttpServletRequest request) {

        if (!isAuthorized(request) && !isSameAccount(uuid, request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Account account = getAccount(uuid);

        if (account != null) {
            newLog(uuid, request, account);
            AccountDTO accountDTO = account.toDTO();
            if (accountDTO.getRequiredInstanceUuid()) {
                String uuidhw = request.getParameter("uuid");
                if (!uuidhw.equalsIgnoreCase(accountDTO.getInstanceUuid())) {
                    accountDTO.setStatus(AccountStatus.NEW);
                    accountDTO.setStatusDescription("Licencia invalida");
                }
            }
            return ResponseEntity.ok(accountDTO);
        }


        return ResponseEntity.ok(NO_ACCOUNT);

    }

    @PostMapping("/account/{uuid}/stats")
    public ResponseEntity<Map<String, String>> updateStats(@PathVariable("uuid") String uuid, @RequestBody AccountStatsList stats, HttpServletRequest request) {

        if (!isAuthorized(request) && !isSameAccount(uuid, request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Account account = getAccount(uuid);
        if (account != null) {
            if (stats != null) {
                service.updateStats(account, stats.getData());
                return ResponseEntity.ok(Map.of("message", "DONE: Account stats updated"));
            } else {
                return ResponseEntity.ok(Map.of("message", "Invalid stats"));
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private Account getAccount(@PathVariable("uuid") String uuid) {
        if (uuid != null && !uuid.isEmpty()) {

            return crudService().findSingle(Account.class, QueryParameters.with("uuid", QueryConditions.eq(uuid)));
        } else {
            return null;
        }
    }

    @GetMapping("/account/{uuid}/parameter/{name}")
    public ResponseEntity<Map<String, String>> getAccountParameter(@PathVariable("uuid") String uuid, @PathVariable("name") String name, HttpServletRequest request) {

        if (!isAuthorized(request) && !isSameAccount(uuid, request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String defautlValue = request.getParameter("defaultValue");
        String value = null;
        Account account = getAccount(uuid);
        if (account != null) {
            AccountParameter parameter = crudService().findSingle(AccountParameter.class, QueryParameters.with("name", QueryConditions.eq(name)).add("accountId", account.getId()));
            if (parameter != null) {
                value = parameter.getValue();
            } else if (defautlValue != null) {
                value = defautlValue;
                crudService().executeWithinTransaction(() -> {
                    AccountParameter newParam = new AccountParameter();
                    newParam.setAccountId(account.getId());
                    newParam.setName(name);
                    newParam.setValue(defautlValue);
                    newParam.save();
                });
            }
        }
        if (value != null) {
            return ResponseEntity.ok(Map.of("parameter", name, "value", value));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private void newLog(String uuid, HttpServletRequest request, Account account) {
        try {
            AccountLog log = new AccountLog(account, HttpUtils.getIpFromRequest(request), "Remote account check");
            log.setPathInfo(request.getPathInfo());
            log.setClientInfo(request.getParameter("info"));
            crudService().create(log);
        } catch (Exception e) {

        }
    }

    public boolean isAuthorized(HttpServletRequest request) {
        if (serviceAPI.getSystemAccountId().equals(serviceAPI.getCurrentAccountId())) {
            return true;
        }

        return false;
    }

    public boolean isSameAccount(String uuid, HttpServletRequest request) {
        var subdomain = HttpUtils.getSubdomain(request);
        var currentAccount = service.getAccount(subdomain);
        return uuid.equals(currentAccount.getUuid());
    }
}
