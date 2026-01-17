
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

package tools.dynamia.modules.saas.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.dynamia.modules.saas.api.enums.AccountStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Mario Serrano Leones
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDTO implements Serializable {


    private Long id;
    private String name;
    private String identification;
    private String idType;
    private String subdomain;
    private String customDomain;
    private String email;
    private AccountTypeDTO type = new AccountTypeDTO();

    private LocalDate expirationDate;
    private AccountStatus status = AccountStatus.NEW;

    private LocalDateTime statusDate;
    private String statusDescription;

    private LocalDateTime creationDate;
    private boolean defaultAccount;
    private String skin;
    private String logo;
    private String locale;
    private String timeZone;
    private String profile;
    private long users;
    private long activedUsers;
    private Integer maxUsers;
    private int paymentDay = 1;
    private BigDecimal paymentValue;
    private Date lastPaymentDate;
    private String phoneNumber;
    private String mobileNumber;
    private String address;
    private String city;
    private String country;
    private String contact;
    private String contactFirstName;
    private String contactLastName;
    private String contactEmail;
    private String uuid;
    private String instanceUuid;
    private Boolean requiredInstanceUuid = Boolean.FALSE;
    private boolean remote;
    private String adminUsername = "admin";

    private String globalMessage;
    private boolean showGlobalMessage;
    private String globalMessageType;
    private BigDecimal fixedPaymentValue;
    private BigDecimal discount;
    private Date discountExpire;
    private List<AccountFeatureDTO> features = new ArrayList<>();
    private BigDecimal balance = BigDecimal.ZERO;
    private String url;
    private long openTicketsCount;
    private long closedTicketsCount;
    private boolean autoInit;
    private Long parentAccountId;
    private int freeTrial;
    private int freeTrialLeft;
    private boolean inFreeTrial;
    private String redirect;

    private String defaultPassword;

    private String activationCoupon;

    private final Map<String, Object> attributes = new HashMap<>();


    public AccountDTO() {
    }

    public AccountDTO(Long id, String subdomain) {
        this.id = id;
        this.subdomain = subdomain;
    }

    public AccountDTO(Long id, String subdomain, AccountStatus status) {
        this.id = id;
        this.subdomain = subdomain;
        this.status = status;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getContactFirstName() {
        return contactFirstName;
    }

    public void setContactFirstName(String contactFirstName) {
        this.contactFirstName = contactFirstName;
    }

    public String getContactLastName() {
        return contactLastName;
    }

    public void setContactLastName(String contactLastName) {
        this.contactLastName = contactLastName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public long getOpenTicketsCount() {
        return openTicketsCount;
    }

    public void setOpenTicketsCount(long openTicketsCount) {
        this.openTicketsCount = openTicketsCount;
    }

    public long getClosedTicketsCount() {
        return closedTicketsCount;
    }

    public void setClosedTicketsCount(long closedTicketsCount) {
        this.closedTicketsCount = closedTicketsCount;
    }

    public boolean isAutoInit() {
        return autoInit;
    }

    public void setAutoInit(boolean autoInit) {
        this.autoInit = autoInit;
    }

    public Long getParentAccountId() {
        return parentAccountId;
    }

    public void setParentAccountId(Long parentAccountId) {
        this.parentAccountId = parentAccountId;
    }

    public int getFreeTrial() {
        return freeTrial;
    }

    public void setFreeTrial(int freeTrial) {
        this.freeTrial = freeTrial;
    }

    public Boolean getRequiredInstanceUuid() {
        return requiredInstanceUuid;
    }

    public void setRequiredInstanceUuid(Boolean requiredInstanceUuid) {
        this.requiredInstanceUuid = requiredInstanceUuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public Date getLastPaymentDate() {
        return lastPaymentDate;
    }

    public void setLastPaymentDate(Date lastPaymentDate) {
        this.lastPaymentDate = lastPaymentDate;
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

    public int getPaymentDay() {
        return paymentDay;
    }

    public void setPaymentDay(int paymentDay) {
        this.paymentDay = paymentDay;
    }

    public BigDecimal getPaymentValue() {
        return paymentValue;
    }

    public void setPaymentValue(BigDecimal paymentValue) {
        this.paymentValue = paymentValue;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public String getLogo() {
        return logo;
    }

    public String getLogoURL() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public boolean isDefaultAccount() {
        return defaultAccount;
    }

    public void setDefaultAccount(boolean defaultAccount) {
        this.defaultAccount = defaultAccount;
    }

    public String getCustomDomain() {
        return customDomain;
    }

    public void setCustomDomain(String customDomain) {
        this.customDomain = customDomain;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AccountTypeDTO getType() {
        return type;
    }

    public void setType(AccountTypeDTO type) {
        if (type != null) {
            this.type = type;
        }
    }


    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public String getInstanceUuid() {
        return instanceUuid;
    }

    public void setInstanceUuid(String instanceUuid) {
        this.instanceUuid = instanceUuid;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getName(), getEmail());
    }


    public String getAdminUsername() {
        if (adminUsername == null) {
            adminUsername = "admin";
        }
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getGlobalMessage() {
        return globalMessage;
    }

    public void setGlobalMessage(String globalMessage) {
        this.globalMessage = globalMessage;
    }

    public boolean isShowGlobalMessage() {
        return showGlobalMessage;
    }

    public void setShowGlobalMessage(boolean showGlobalMessage) {
        this.showGlobalMessage = showGlobalMessage;
    }

    public List<AccountFeatureDTO> getFeatures() {
        return features;
    }

    public void setFeatures(List<AccountFeatureDTO> features) {
        this.features = features;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeName() {
        if (getType() != null) {
            return getType().getName();
        } else {
            return "";
        }
    }


    public int getMaxUsers() {
        if (maxUsers != null && maxUsers > 0) {
            return maxUsers;
        }
        return type.getMaxUsers();
    }

    public boolean isAllowAdditionalUsers() {
        return type.isAllowAdditionalUsers();
    }

    public void setAllowAdditionalUsers(boolean allowAdditionalUsers) {
        type.setAllowAdditionalUsers(allowAdditionalUsers);
    }


    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getGlobalMessageType() {
        return globalMessageType;
    }

    public void setGlobalMessageType(String globalMessageType) {
        this.globalMessageType = globalMessageType;
    }

    public BigDecimal getFixedPaymentValue() {
        return fixedPaymentValue;
    }

    public void setFixedPaymentValue(BigDecimal fixedPaymentValue) {
        this.fixedPaymentValue = fixedPaymentValue;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public Date getDiscountExpire() {
        return discountExpire;
    }

    public void setDiscountExpire(Date discountExpire) {
        this.discountExpire = discountExpire;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers;
    }


    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public String getActivationCoupon() {
        return activationCoupon;
    }

    public void setActivationCoupon(String activationCoupon) {
        this.activationCoupon = activationCoupon;
    }

    public boolean hasFeature(String name) {
        if (features == null) {
            return false;
        }
        return features.stream().filter(f -> f.getProviderId().equals(name))
                .map(AccountFeatureDTO::isEnabled)
                .findFirst()
                .orElse(false);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public int getFreeTrialLeft() {
        return freeTrialLeft;
    }

    public void setFreeTrialLeft(int freeTrialLeft) {
        this.freeTrialLeft = freeTrialLeft;
    }

    public boolean isInFreeTrial() {
        return inFreeTrial;
    }

    public void setInFreeTrial(boolean inFreeTrial) {
        this.inFreeTrial = inFreeTrial;
    }

    public boolean isFreeTrialEnd() {
        return isFreeTrial() && getFreeTrialLeft() <= 0;
    }


    private boolean isFreeTrial() {
        return freeTrial > 0;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public LocalDateTime getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(LocalDateTime statusDate) {
        this.statusDate = statusDate;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
}
