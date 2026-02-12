
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.annotations.BatchSize;
import tools.dynamia.domain.jpa.SimpleEntity;
import tools.dynamia.domain.Transferable;
import tools.dynamia.domain.contraints.NotEmpty;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.modules.saas.api.dto.AccountTypeDTO;
import tools.dynamia.modules.saas.api.enums.AccountPeriodicity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mario Serrano Leones
 */
@Entity
@Table(name = "saas_account_types")
@BatchSize(size = 10)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountType extends SimpleEntity implements Transferable<AccountTypeDTO> {

    @NotNull
    @NotEmpty(message = "ingrese nombre del tipo de cuenta")
    private String name;
    private String description;
    private String internalDescription;
    private boolean active;
    private boolean publicType;
    private String publicName;
    @JsonIgnore
    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountTypeRestriction> restrictions = new ArrayList<>();
    @Enumerated(EnumType.ORDINAL)
    @NotNull
    private AccountPeriodicity periodicity = AccountPeriodicity.MONTHLY;
    private BigDecimal price;
    @Min(value = 1, message = "Enter valid max users")
    private int maxUsers = 1;
    private boolean allowAdditionalUsers;
    private BigDecimal additionalUserPrice;
    private boolean printingSupport;
    private int allowedOverdueDays = 5;
    private String reference;
    private boolean paymentRequired;

    private boolean secured;

    private boolean trial;
    private int trialDays;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    public boolean isAllowAdditionalUsers() {
        return allowAdditionalUsers;
    }

    public void setAllowAdditionalUsers(boolean allowAdditionalUsers) {
        this.allowAdditionalUsers = allowAdditionalUsers;
    }

    public BigDecimal getAdditionalUserPrice() {
        return additionalUserPrice;
    }

    public void setAdditionalUserPrice(BigDecimal additionalUserPrice) {
        this.additionalUserPrice = additionalUserPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInternalDescription() {
        return internalDescription;
    }

    public void setInternalDescription(String internalDescription) {
        this.internalDescription = internalDescription;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isPublicType() {
        return publicType;
    }

    public void setPublicType(boolean publicType) {
        this.publicType = publicType;
    }

    public List<AccountTypeRestriction> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(List<AccountTypeRestriction> restrictions) {
        this.restrictions = restrictions;
    }

    public AccountPeriodicity getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(AccountPeriodicity periodicity) {
        this.periodicity = periodicity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isPrintingSupport() {
        return printingSupport;
    }

    public void setPrintingSupport(boolean printingSupport) {
        this.printingSupport = printingSupport;
    }

    @Override
    public AccountTypeDTO toDTO() {
        return DomainUtils.autoDataTransferObject(this, AccountTypeDTO.class);
    }

    public int getAllowedOverdueDays() {
        return allowedOverdueDays;
    }

    public void setAllowedOverdueDays(int allowedOverdueDays) {
        this.allowedOverdueDays = allowedOverdueDays;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getPublicName() {
        if (publicName == null) {
            return getName();
        }
        return publicName;
    }

    public void setPublicName(String publicName) {
        this.publicName = publicName;
    }

    public boolean isPaymentRequired() {
        return paymentRequired;
    }

    public void setPaymentRequired(boolean paymentRequired) {
        this.paymentRequired = paymentRequired;
    }

    public boolean isAdmin() {
        return "admin".equals(name);
    }

    public boolean isSecured() {
        return secured;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }

    public boolean isTrial() {
        return trial;
    }

    public void setTrial(boolean trial) {
        this.trial = trial;
    }

    public int getTrialDays() {
        return trialDays;
    }

    public void setTrialDays(int trialDays) {
        this.trialDays = trialDays;
    }
}
