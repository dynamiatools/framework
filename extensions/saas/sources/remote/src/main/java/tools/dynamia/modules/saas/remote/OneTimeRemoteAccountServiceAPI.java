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

package tools.dynamia.modules.saas.remote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.modules.saas.api.dto.AccountDTO;
import tools.dynamia.modules.saas.api.enums.AccountStatus;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class OneTimeRemoteAccountServiceAPI extends RemoteAccountServiceAPI {

    private static final String ACCOUNT_ID = "accountId";


    private int maxUsers = 10;

    public OneTimeRemoteAccountServiceAPI(String serverUrl, String accountUuid) {
        super(serverUrl, accountUuid);
    }

    public OneTimeRemoteAccountServiceAPI(String serverUrl, String accountUuid, Long defaultID) {
        super(serverUrl, accountUuid, defaultID);
    }

    @Override
    public AccountStatus getAccountStatus(Long accountId) {
        checkAccountInfo();
        if (accountDTO != null) {
            return accountDTO.getStatus();
        } else {
            return AccountStatus.CANCELED;
        }
    }


    @Override
    protected void checkAccountInfo() {
        if (accountDTO == null) {
            syncAccountInfo();
        }
    }


    @Override
    protected void syncAccountInfo() {
        String hwid = RemoteAccountServiceAPI.getHardwareUUID();
        String key = StringUtils.hash(hwid + "$" + accountUuid, "md5") + "_ERP";
        String key2 = key + ".";
        Preferences pref = Preferences.userRoot();

        String accountJson = pref.get(key, null);

        long time = pref.getLong(key2, 0);

        if (time == 0) {
            accountJson = null;
        }
        if (time > 0 && accountJson != null) {
            try {
                Date ns = DateTimeUtils.createDate(time);
                if (DateTimeUtils.monthsBetween(ns, new Date()) >= 6) {
                    pref.putLong(key2, System.currentTimeMillis());
                    accountJson = null;
                }
            } catch (Exception e) {
                accountJson = null;
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        if (accountJson != null && !accountJson.isEmpty()) {
            try {
                accountJson = new String(Base64.getDecoder().decode(accountJson));
                accountDTO = mapper.readValue(accountJson, AccountDTO.class);
                if (accountDTO != null) {
                    accountDTO.setMaxUsers(maxUsers);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (accountDTO == null) {
            loadAccountFromServer();
            if (accountDTO != null) {
                try {
                    accountJson = mapper.writeValueAsString(accountDTO);
                    accountJson = Base64.getEncoder().encodeToString(accountJson.getBytes());
                    pref.put(key, accountJson);
                    pref.putLong(key2, System.currentTimeMillis());
                    pref.sync();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                } catch (BackingStoreException e) {
                    e.printStackTrace();
                }
            }
        }

        if (accountDTO == null) {
            accountDTO = RemoteAccountServiceAPI.tempAccountInfo(defaultID);
            accountDTO.setStatus(AccountStatus.NEW);
            accountDTO.setStatusDescription("Licencia no valida");
        }
    }

    private void loadAccountFromServer() {
        String url = getAdminURL() + "?uuid=" + getHardwareUUID();
        RestTemplate rest = new RestTemplate();

        try {
            accountDTO = rest.getForObject(url, AccountDTO.class);

            if (accountDTO != null && accountDTO.getStatus() != AccountStatus.ACTIVE) {
                accountDTO = null;
                return;
            }


            if (defaultID != null) {
                accountDTO.setId(defaultID);
            }
        } catch (RestClientException e) {
            e.printStackTrace();
        }
    }


    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }
}
