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

package tools.dynamia.modules.saas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import tools.dynamia.domain.Transferable;
import tools.dynamia.domain.jpa.BaseEntity;
import tools.dynamia.modules.saas.api.dto.AccountLogDTO;

import java.time.LocalDateTime;

@Entity
@Table(name = "saas_logs")
public class AccountLog extends BaseEntity implements Transferable<AccountLogDTO> {


    @Column(name = "logDate")
    private LocalDateTime date = LocalDateTime.now();

    @ManyToOne
    @NotNull
    private Account account;
    private String ip;
    private String pathInfo;
    @Column(length = 1000)
    private String message;
    @Column(length = 2000)
    private String clientInfo;

    public AccountLog() {
    }

    public AccountLog(Account account, String message) {
        this.account = account;
        this.message = message;
    }

    public AccountLog(Account account, String ip, String message) {
        this.account = account;
        this.ip = ip;
        this.message = message;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        if (message != null && message.length() > 999) {
            this.message = message.substring(0, 999);
        }
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
    }
}
