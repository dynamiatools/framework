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

import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.dynamia.domain.jpa.SimpleEntity;
import tools.dynamia.domain.contraints.NotEmpty;
import tools.dynamia.modules.saas.domain.enums.AccessControl;

@Entity
@Table(name = "saas_profiles_restrictions")
public class AccountProfileRestriction extends SimpleEntity {

    @ManyToOne
    @JsonIgnore
    private AccountProfile profile;

    @NotEmpty
    private String name;
    private String type;
    @Column(length = 1000)
    private String value;
    private AccessControl accessControl = AccessControl.ALLOWED;

    public AccountProfileRestriction() {
        // TODO Auto-generated constructor stub
    }

    public AccountProfileRestriction(String name, String type, String value) {
        super();
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AccountProfile getProfile() {
        return profile;
    }

    public void setProfile(AccountProfile profile) {
        this.profile = profile;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%s (%s %s)", name, type,getAccessControl());
    }

    public AccessControl getAccessControl() {
        if (accessControl == null) {
            accessControl = AccessControl.ALLOWED;
        }
        return accessControl;
    }

    public void setAccessControl(AccessControl access) {
        this.accessControl = access;
    }
}
