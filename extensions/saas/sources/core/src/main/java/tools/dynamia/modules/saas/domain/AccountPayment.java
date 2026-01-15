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
import tools.dynamia.commons.BigDecimalUtils;
import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.domain.OrderBy;
import tools.dynamia.domain.Transferable;
import tools.dynamia.domain.contraints.NotEmpty;
import tools.dynamia.domain.jpa.BaseEntity;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.dto.AccountPaymentDTO;
import tools.dynamia.modules.saas.domain.enums.ResellerComissionStatus;
import tools.dynamia.modules.saas.services.AccountService;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * @author Mario Serrano Leones
 */
@Entity
@Table(name = "saas_payments")
@OrderBy("creationDate")
public class AccountPayment extends BaseEntity implements Transferable<AccountPaymentDTO> {

    @ManyToOne
    @NotNull(message = "Select account")
    private Account account;
    @ManyToOne
    private AccountType type;
    @NotEmpty(message = "Entrer payment reference")
    private String reference;
    @Column(name = "realValue")
    private BigDecimal value;
    private BigDecimal paymentValue;
    private long users;
    private long activedUsers;
    @Column(length = 2000)
    private String description;
    @Column(length = 2000)
    private String paymentMethodDescription;
    private boolean finished = true;
    @ManyToOne
    @NotNull
    private AccountPaymentMethod paymentMethod;
    private BigDecimal resellerComission;
    private double comissionRate;
    private ResellerComissionStatus comissionStatus;
    @ManyToOne
    private AccountReseller reseller;
    private String couponCode;

    private boolean silent;
    private boolean invoiceRequired = true;
    @ManyToOne
    private AccountAdditionalService additionalService;
    private String externalReference;
    private String externalService;
    private boolean external;
    private String reference2;
    private String invoiceID;
    private String invoiceNumber;
    private String invoiceUUID;
    private String extra0;
    private String extra1;
    private String extra2;
    private String extra3;
    @Column(length = 400)
    private String paymentLink;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
        init();
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getPaymentValue() {
        return paymentValue;
    }

    public void setPaymentValue(BigDecimal paymentValue) {
        this.paymentValue = paymentValue;
    }

    public long getUsers() {
        return users;
    }

    public void setUsers(long users) {
        this.users = users;
    }

    public long getActivedUsers() {
        return activedUsers;
    }

    public void setActivedUsers(long activedUsers) {
        this.activedUsers = activedUsers;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPaymentMethodDescription() {
        return paymentMethodDescription;
    }

    public void setPaymentMethodDescription(String paymentMethodDescription) {
        this.paymentMethodDescription = paymentMethodDescription;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    private void init() {
        if (account != null) {
            paymentValue = Containers.get().findObject(AccountService.class).getPaymentValue(account);
            activedUsers = account.getActivedUsers();
            users = account.getUsers();
            type = account.getType();
            value = paymentValue;
            reseller = account.getReseller();
            if (account.getDiscount() != null && account.getDiscountExpire() != null && account.getDiscountExpire().after(new Date())) {
                description = "Discount: " + DecimalFormat.getCurrencyInstance().format(account.getDiscount()) + " - " + DateTimeUtils.formatDate(account.getDiscountExpire());
            }

            notifyChange("paymentValue", BigDecimal.ZERO, paymentValue);
            notifyChange("value", BigDecimal.ZERO, value);
        }
    }

    public AccountPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(AccountPaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public BigDecimal getResellerComission() {
        return resellerComission;
    }

    public void setResellerComission(BigDecimal resellerComission) {
        this.resellerComission = resellerComission;
    }

    public double getComissionRate() {
        return comissionRate;
    }

    public void setComissionRate(double comissionRate) {
        this.comissionRate = comissionRate;
    }

    public ResellerComissionStatus getComissionStatus() {
        return comissionStatus;
    }

    public void setComissionStatus(ResellerComissionStatus comissionStatus) {
        this.comissionStatus = comissionStatus;
    }


    public void computeComission() {
        if (paymentMethod != null && paymentMethod.isComissionable() && account != null && account.getReseller() != null) {
            comissionRate = account.getReseller().getComissionRate();
            comissionStatus = ResellerComissionStatus.PENDING;
            if (comissionRate > 0) {
                resellerComission = BigDecimalUtils.computePercent(value, comissionRate, false);
            } else {
                resellerComission = BigDecimal.ZERO;
            }
        }
    }

    @Override
    public String toString() {
        if (paymentMethod != null) {
            return getPaymentMethod().getName() + " - " + getReference() + ": " + DecimalFormat.getCurrencyInstance().format(getValue());
        } else {
            return DecimalFormat.getCurrencyInstance().format(getValue());
        }
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public boolean isInvoiceRequired() {
        return invoiceRequired;
    }

    public void setInvoiceRequired(boolean invoiceRequired) {
        this.invoiceRequired = invoiceRequired;
    }

    public AccountReseller getReseller() {
        return reseller;
    }

    public void setReseller(AccountReseller reseller) {
        this.reseller = reseller;
    }

    public AccountAdditionalService getAdditionalService() {
        return additionalService;
    }

    public void setAdditionalService(AccountAdditionalService additionalService) {
        this.additionalService = additionalService;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public String getExternalService() {
        return externalService;
    }

    public void setExternalService(String externalService) {
        this.externalService = externalService;
    }

    public String getReference2() {
        return reference2;
    }

    public void setReference2(String reference2) {
        this.reference2 = reference2;
    }

    public String getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(String invoiceID) {
        this.invoiceID = invoiceID;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getInvoiceUUID() {
        return invoiceUUID;
    }

    public void setInvoiceUUID(String invoiceUUID) {
        this.invoiceUUID = invoiceUUID;
    }

    public String getExtra0() {
        return extra0;
    }

    public void setExtra0(String extra0) {
        this.extra0 = extra0;
    }

    public String getExtra1() {
        return extra1;
    }

    public void setExtra1(String extra1) {
        this.extra1 = extra1;
    }

    public String getExtra2() {
        return extra2;
    }

    public void setExtra2(String extra2) {
        this.extra2 = extra2;
    }

    public String getExtra3() {
        return extra3;
    }

    public void setExtra3(String extra3) {
        this.extra3 = extra3;
    }

    public String getPaymentLink() {
        return paymentLink;
    }

    public void setPaymentLink(String paymentLink) {
        this.paymentLink = paymentLink;
    }

    public boolean isExternal() {
        return external;
    }

    /**
     * External payment do not update account balance or account last payment
     *
     * @param external
     */
    public void setExternal(boolean external) {
        this.external = external;
    }

    @Override
    public AccountPaymentDTO toDTO() {
        AccountPaymentDTO dto = DomainUtils.autoDataTransferObject(this, AccountPaymentDTO.class);
        if (paymentMethod != null) {
            dto.setPaymentMethod(paymentMethod.getName());
            dto.setPaymentMethodId(paymentMethod.getId());
        }

        if (additionalService != null) {
            dto.setAdditionalService(additionalService.getName());
            dto.setAdditionalServiceId(additionalService.getId());
        }

        if (type != null) {
            dto.setType(type.getName());
        }

        if (account != null) {
            dto.setAccount(account.getName());
            dto.setAccountId(account.getId());

        }

        if (reseller != null) {
            dto.setReseller(getReseller().getName());
            dto.setResellerId(getReseller().getId());
        }


        return dto;
    }
}
