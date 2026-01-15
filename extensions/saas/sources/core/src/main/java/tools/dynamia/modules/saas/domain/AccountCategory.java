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

import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.BatchSize;
import tools.dynamia.domain.Descriptor;
import tools.dynamia.domain.jpa.SimpleEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "saas_categories")
@Descriptor(fields = {"name", "defaultAccountType", "defaultAccountProfile"})
@BatchSize(size = 20)
public class AccountCategory extends SimpleEntity {

    @NotNull
    @Column(unique = true)
    private String name;
    @ManyToOne
    private AccountType defaultAccountType;
    @ManyToOne
    private AccountProfile defaultAccountProfile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccountType getDefaultAccountType() {
        return defaultAccountType;
    }

    public void setDefaultAccountType(AccountType defaultAccountType) {
        this.defaultAccountType = defaultAccountType;
    }

    public AccountProfile getDefaultAccountProfile() {
        return defaultAccountProfile;
    }

    public void setDefaultAccountProfile(AccountProfile defaultAccountProfile) {
        this.defaultAccountProfile = defaultAccountProfile;
    }

    @Override
    public String toString() {
        return name;
    }
}
