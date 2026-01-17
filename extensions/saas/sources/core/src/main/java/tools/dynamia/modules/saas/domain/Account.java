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
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.domain.Transferable;
import tools.dynamia.domain.contraints.Email;
import tools.dynamia.domain.contraints.NotEmpty;
import tools.dynamia.domain.jpa.SimpleEntity;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.saas.AccountStatusCache;
import tools.dynamia.modules.saas.api.dto.AccountDTO;
import tools.dynamia.modules.saas.api.enums.AccountStatus;
import tools.dynamia.web.util.HttpUtils;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Mario Serrano Leones
 */
@Entity
@Table(name = "saas_accounts")
@BatchSize(size = 20)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Account extends SimpleEntity implements Transferable<AccountDTO> {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 684169179001325225L;

    @NotNull
    @NotEmpty(message = "ingrese nombre de cuenta")
    @Column(unique = true)
    private String name;
    @NotEmpty(message = "Ingrese numero de identificacion")
    private String identification;
    private String idType;

    @NotNull
    @Column(unique = true)
    private String subdomain;
    private String customDomain;
    @NotNull
    @Email(message = "ingreso direccion de correo valida ")
    @NotEmpty(message = "ingrese direccion de correo electronico")
    private String email;
    @ManyToOne
    @NotNull
    private AccountType type;

    private LocalDate expirationDate;
    private AccountStatus status = AccountStatus.NEW;
    private LocalDateTime statusDate;
    private LocalDateTime oldStatusDate;
    private String statusDescription;
    private LocalDateTime creationDate = LocalDateTime.now();
    private boolean defaultAccount;
    private String skin;
    @ManyToOne
    @JsonIgnore
    private EntityFile logo;
    @Column(length = 600)
    private String logoURL;
    private String locale;
    private String timeZone;
    @ManyToOne
    @JsonIgnore
    private AccountProfile profile;
    private long users;
    private long activedUsers;
    private Integer maxUsers;
    @Min(value = 1, message = "Enter valid day")
    @Max(value = 31, message = "Enter valid day")
    private int paymentDay = 1;
    private BigDecimal paymentValue;

    private LocalDate lastPaymentDate;
    private LocalDate lastChargeDate;
    private String phoneNumber;
    private String mobileNumber;
    private String address;
    private String city;
    private String region;
    private String country;
    private String contact;
    private String contactFirstName;
    private String contactLastName;
    @Email(message = "Email valido")
    private String contactEmail;
    private String uuid = StringUtils.randomString();
    private String instanceUuid;
    private Boolean requiredInstanceUuid = Boolean.FALSE;
    private boolean remote;
    @JsonIgnore
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountStatsData> stats = new ArrayList<>();
    @Size(min = 5, message = "El nombre de usuario debe ser minimo de 5 caracteres")
    private String adminUsername = "admin";
    @Column(length = 2000)
    private String globalMessage;
    private boolean showGlobalMessage;
    private String globalMessageType;
    @JsonIgnore
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountFeature> features = new ArrayList<>();
    private BigDecimal balance = BigDecimal.ZERO;
    private BigDecimal fixedPaymentValue;
    private BigDecimal discount;

    private LocalDate discountExpire;
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<AccountAdditionalService> additionalServices = new ArrayList<>();
    @Column(length = 2000)
    private String customerInfo;

    private LocalDate lastInvoiceDate;


    @Transient
    private boolean useTempPaymentDay;
    private AccountStatus oldStatus;

    @ManyToOne
    private AccountCategory category;

    @ManyToOne
    private AccountReseller reseller;

    @ManyToOne
    private AccountResellerAgent resellerAgent;

    @Lob
    private String resellerComments;

    @Column(length = 50)
    private String resellerInvoice;

    @Temporal(TemporalType.DATE)
    private Date resellerInvoiceDate;

    @ManyToOne
    private AccountRegion accountRegion;
    private boolean templateAccount;


    private long openTicketsCount;
    private long closedTicketsCount;
    private boolean autoInit = true;

    @ManyToOne
    @JsonIgnore
    private Account parentAccount;
    private int freeTrial;


    private String defaultPassword;

    private String activationCoupon;
    private String redirect;
    @ManyToOne
    private AccountChannelSale channel;

    public Account() {
        initLocale();
    }


    public Account(Long accountId) {
        this.setId(accountId);
        initLocale();
    }

    private void initLocale() {
        try {
            Locale current = Locale.getDefault();
            locale = current.toLanguageTag();

            timeZone = ZoneId.systemDefault().getId();

            paymentDay = DateTimeUtils.getCurrentDay();
            if (paymentDay >= 29) {
                paymentDay = 1;
            }
        } catch (Exception e) {
        }
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
        if (contact == null && contactFirstName != null) {
            contact = contactFirstName;
            if (contactLastName != null) {
                contact = contact + " " + contactLastName;
            }
        }
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getContactFirstName() {
        if (contactFirstName == null && contact != null) {
            contactFirstName = contact;
        }
        return contactFirstName;
    }

    public String getContactLastName() {
        return contactLastName;
    }

    public String getIdentification() {
        if (identification == null) {
            identification = String.valueOf(getId());
        }
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public LocalDate getLastPaymentDate() {
        return lastPaymentDate;
    }

    public void setLastPaymentDate(LocalDate lastPaymentDate) {
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
        if (paymentDay == 0) {
            paymentDay = 1;
        }

        return paymentDay;
    }

    public void setPaymentDay(int paymentDay) {
        this.paymentDay = paymentDay;
    }

    public BigDecimal getPaymentValue() {
        if ((paymentValue == null || paymentValue.longValue() == 0) && type != null) {
            paymentValue = type.getPrice();
        }

        return paymentValue;
    }

    public void setPaymentValue(BigDecimal paymentValue) {
        this.paymentValue = paymentValue;
    }

    public AccountProfile getProfile() {
        return profile;
    }

    public void setProfile(AccountProfile profile) {
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

    public EntityFile getLogo() {
        return logo;
    }

    public void setLogo(EntityFile logo) {
        this.logo = logo;
        if (logo != null && logo.getName() != null) {
            logoURL = logo.getStoredEntityFile().getThumbnailUrl();
            if (logoURL != null && logoURL.length() > 600) {
                logoURL = null;
            }
        } else {
            logoURL = null;
        }
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

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
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

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
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

        if (status != this.status) {
            this.oldStatusDate = statusDate;
            this.statusDate = LocalDateTime.now();
            this.oldStatus = this.status;
            AccountStatusCache.statusChanged(this);
        }
        this.status = status;

    }

    public LocalDateTime getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(LocalDateTime statusDate) {
        this.statusDate = statusDate;
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
        return getName();
    }

    @Override
    public AccountDTO toDTO() {
        System.out.println("Loading Account DTO: " + this);
        AccountDTO dto = DomainUtils.autoDataTransferObject(this, AccountDTO.class);

        String logoURL = null;
        if (logo != null) {
            logoURL = logo.getStoredEntityFile().getThumbnailUrl(200, 200);
        }
        dto.setLogo(logoURL);
        dto.setType(getType().toDTO());
        try {
            getFeatures().forEach(f -> dto.getFeatures().add(f.toDTO()));
        } catch (Exception e) {
            var reloadF = DomainUtils.lookupCrudService().find(AccountFeature.class, "account", this);
            reloadF.forEach(f -> dto.getFeatures().add(f.toDTO()));
        }
        dto.setStatus(getStatus());
        dto.setGlobalMessage(getGlobalMessage());
        dto.setShowGlobalMessage(isShowGlobalMessage());
        dto.setGlobalMessageType(getGlobalMessageType());
        dto.setPaymentValue(getPaymentValue());
        dto.setMaxUsers(maxUsers);
        dto.setFreeTrialLeft(getFreeTrialLeft());
        dto.setInFreeTrial(isInFreeTrial());
        try {
            if (HttpUtils.isInWebScope()) {
                dto.setUrl(HttpUtils.getServerPath().replace("admin.", subdomain + "."));
            }
        } catch (Exception e) {

        }

        return dto;
    }

    public List<AccountStatsData> getStats() {
        return stats;
    }

    public void setStats(List<AccountStatsData> stats) {
        this.stats = stats;
    }

    public AccountStatsData findStats(String name) {
        return stats.stream().filter(a -> a.getName().equals(name)).findFirst().orElse(null);
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

    public List<AccountFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<AccountFeature> features) {
        this.features = features;
    }

    public BigDecimal getBalance() {
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }


    public BigDecimal getFixedPaymentValue() {
        return fixedPaymentValue;
    }

    public void setFixedPaymentValue(BigDecimal fixedPaymentValue) {
        this.fixedPaymentValue = fixedPaymentValue;
    }

    public LocalDate getStartDate() {
        return getCreationDate().withDayOfMonth(getPaymentDay()).toLocalDate();

    }

    public LocalDate getLastChargeDate() {
        return lastChargeDate;
    }

    public void setLastChargeDate(LocalDate lastChargeDate) {
        this.lastChargeDate = lastChargeDate;
    }

    public String getGlobalMessageType() {
        return globalMessageType;
    }

    public void setGlobalMessageType(String globalMessageType) {
        this.globalMessageType = globalMessageType;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public LocalDate getDiscountExpire() {
        return discountExpire;
    }

    public void setDiscountExpire(LocalDate discountExpire) {
        this.discountExpire = discountExpire;
    }

    public String getIdType() {
        if (idType == null) {
            idType = "ID";
        }
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public List<AccountAdditionalService> getAdditionalServices() {
        return additionalServices;
    }

    public void setAdditionalServices(List<AccountAdditionalService> additionalServices) {
        this.additionalServices = additionalServices;
    }

    public boolean isUseTempPaymentDay() {
        return useTempPaymentDay;
    }

    public void setUseTempPaymentDay(boolean useTempPaymentDay) {
        this.useTempPaymentDay = useTempPaymentDay;
    }

    public String getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(String customerInfo) {
        this.customerInfo = customerInfo;
    }

    public LocalDate getLastInvoiceDate() {
        return lastInvoiceDate;
    }

    public void setLastInvoiceDate(LocalDate lastInvoiceDate) {
        this.lastInvoiceDate = lastInvoiceDate;
    }

    public AccountStatus getOldStatus() {
        if (oldStatus == null) {
            oldStatus = status;
        }
        return oldStatus;
    }

    public void setOldStatus(AccountStatus oldStatus) {
        this.oldStatus = oldStatus;
    }


    public LocalDateTime getOldStatusDate() {
        if (oldStatusDate == null) {
            oldStatusDate = statusDate;
        }
        return oldStatusDate;
    }

    public void setOldStatusDate(LocalDateTime oldStatusDate) {
        this.oldStatusDate = oldStatusDate;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public void setContactFirstName(String contactFirstName) {
        this.contactFirstName = contactFirstName;
    }

    public void setContactLastName(String contactLastName) {
        this.contactLastName = contactLastName;
    }

    public AccountCategory getCategory() {
        return category;
    }

    public void setCategory(AccountCategory category) {
        this.category = category;
    }

    public AccountReseller getReseller() {
        return reseller;
    }

    public void setReseller(AccountReseller reseller) {
        this.reseller = reseller;
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

    public void setClosedTicketsCount(long closedTicketCount) {
        this.closedTicketsCount = closedTicketCount;
    }

    public boolean isAutoInit() {
        return autoInit;
    }

    public void setAutoInit(boolean autoInit) {
        this.autoInit = autoInit;
    }

    public Integer getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers;
    }

    public Account getParentAccount() {
        return parentAccount;
    }

    public void setParentAccount(Account parentAccount) {
        if (parentAccount != null && parentAccount.equals(this)) {
            return;
        }
        this.parentAccount = parentAccount;
    }

    public int getFreeTrial() {
        return freeTrial;
    }

    public void setFreeTrial(int freeTrial) {
        this.freeTrial = freeTrial;
    }

    public int getFreeTrialLeft() {
        int left = 0;
        if (isFreeTrial()) {
            left = computeTrialLeft(freeTrial, LocalDate.now());
        }
        return left;
    }

    public int computeTrialLeft(int trial, LocalDate date) {
        long diff = DateTimeUtils.daysBetween(creationDate.toLocalDate(), date);
        var left = trial - (int) diff;
        if (left < 0) {
            left = 0;
        }

        return left;
    }

    @JsonIgnore
    public boolean isInFreeTrial() {
        return isFreeTrial() && getFreeTrialLeft() > 0;
    }

    @JsonIgnore
    public boolean isFreeTrialEnd() {
        return isFreeTrial() && getFreeTrialLeft() <= 0;
    }

    @JsonIgnore
    private boolean isFreeTrial() {
        return freeTrial > 0;
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

    public AccountChannelSale getChannel() {
        return channel;
    }

    public void setChannel(AccountChannelSale channel) {
        this.channel = channel;
    }

    public String getLogoURL() {
        if (logoURL == null && logo != null) {
            logoURL = logo.getStoredEntityFile().getThumbnailUrl();
        }
        return logoURL;
    }

    public void setLogoURL(String logoURL) {
        this.logoURL = logoURL;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public AccountRegion getAccountRegion() {
        return accountRegion;
    }

    public void setAccountRegion(AccountRegion accountRegion) {
        this.accountRegion = accountRegion;
    }

    public boolean isTemplateAccount() {
        return templateAccount;
    }

    public void setTemplateAccount(boolean templateAccount) {
        this.templateAccount = templateAccount;
    }

    public AccountResellerAgent getResellerAgent() {
        return resellerAgent;
    }

    public void setResellerAgent(AccountResellerAgent resellerAgent) {
        this.resellerAgent = resellerAgent;
    }

    public String getResellerComments() {
        return resellerComments;
    }

    public void setResellerComments(String resellerComments) {
        this.resellerComments = resellerComments;
    }

    public String getResellerInvoice() {
        return resellerInvoice;
    }

    public void setResellerInvoice(String resellerInvoice) {
        this.resellerInvoice = resellerInvoice;
    }

    public Date getResellerInvoiceDate() {
        return resellerInvoiceDate;
    }

    public void setResellerInvoiceDate(Date resellerInvoiceDate) {
        this.resellerInvoiceDate = resellerInvoiceDate;
    }
}
