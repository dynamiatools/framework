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

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import tools.dynamia.domain.contraints.NotEmpty;
import tools.dynamia.domain.jpa.BaseEntity;
import tools.dynamia.domain.jpa.ContactInfo;
import tools.dynamia.domain.util.DomainUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "saas_resellers")
@BatchSize(size = 20)
public class AccountReseller extends BaseEntity {


    @NotEmpty
    private String name;
    private String identification;
    private String idType;
    private ContactInfo contactInfo = new ContactInfo();
    private boolean enabled;
    private Long externalId;
    @ManyToOne
    private Account mainAccount;
    private double comissionRate;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "reseller")
    private List<AccountResellerAgent> agents = new ArrayList<>();
    @Temporal(TemporalType.DATE)
    private Date startDate = new Date();
    @Temporal(TemporalType.DATE)
    private Date endDate = null;
    @Column(length = 1000)
    private String comments;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public ContactInfo getContactInfo() {
        if (contactInfo == null) {
            contactInfo = new ContactInfo();
        }
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public Account getMainAccount() {
        return mainAccount;
    }

    public void setMainAccount(Account mainAccount) {
        this.mainAccount = mainAccount;
    }

    public double getComissionRate() {
        return comissionRate;
    }

    public void setComissionRate(double comissionRate) {
        this.comissionRate = comissionRate;
    }

    public List<AccountResellerAgent> getAgents() {
        return agents;
    }

    public void setAgents(List<AccountResellerAgent> agents) {
        this.agents = agents;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return name;
    }

}
