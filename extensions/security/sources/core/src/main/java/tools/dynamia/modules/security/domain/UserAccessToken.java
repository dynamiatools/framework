package tools.dynamia.modules.security.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.domain.jpa.BaseEntity;
import tools.dynamia.modules.saas.api.AccountAware;

import java.util.Date;
import java.util.Random;

@Entity
@Table(name = "sec_access_tokens")
public class UserAccessToken extends BaseEntity implements AccountAware {

    @ManyToOne
    @NotNull(message = "Select user")
    private User user;
    @NotEmpty(message = "Enter token name")
    private String tokenName;
    @Column(unique = true)
    private String token;
    private Long accountId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastAccess;
    private long hits;


    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;

    private boolean automatic;

    @Column(length = 500)
    private String tokenSource;
    private boolean otp;

    public UserAccessToken() {
        generate();
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isAutomatic() {
        return automatic;
    }

    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(Date lastAccess) {
        this.lastAccess = lastAccess;
    }


    public void generate() {
        if (otp) {
            Random random = new Random();
            this.token = String.valueOf(100000 + random.nextInt(900000));
            this.expirationDate = DateTimeUtils.addMinutes(new Date(), 10);
        } else {
            this.token = "tk" + StringUtils.randomString() + StringUtils.randomString();
            this.expirationDate = DateTimeUtils.addDays(new Date(), 15);
        }
    }

    @Override
    public String toString() {
        return tokenName;
    }

    public void setTokenSource(String tokenSource) {
        this.tokenSource = tokenSource;
    }

    public String getTokenSource() {
        return tokenSource;
    }

    public long getHits() {
        return hits;
    }

    public void setHits(long hits) {
        this.hits = hits;
    }


    public boolean isOtp() {
        return otp;
    }

    public void setOtp(boolean otp) {
        this.otp = otp;
    }

    @Override
    public Long getAccountId() {
        return accountId;
    }

    @Override
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
