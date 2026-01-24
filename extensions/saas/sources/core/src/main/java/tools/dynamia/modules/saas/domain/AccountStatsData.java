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
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.domain.Descriptor;
import tools.dynamia.domain.OrderBy;
import tools.dynamia.domain.jpa.SimpleEntity;
import tools.dynamia.modules.saas.api.AccountStats;

import java.time.LocalDateTime;

@Entity
@Table(name = "saas_stats")
@OrderBy("module")
@Descriptor(fields = {"module", "name","quantity", "value",  "description", "creationDate", "lastUpdate"})
public class AccountStatsData extends SimpleEntity {

    @ManyToOne
    private Account account;
    private LocalDateTime creationDate = LocalDateTime.now();
    private LocalDateTime lastUpdate;

    private String name;
    @Column(length = 1000)
    private String value;
    @Column(length = 1000)
    private String description;
    private long quantity;
    private String module;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public void load(AccountStats stats) {
        this.lastUpdate = LocalDateTime.now();
        ObjectOperations.setupBean(this, stats);
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }
}
