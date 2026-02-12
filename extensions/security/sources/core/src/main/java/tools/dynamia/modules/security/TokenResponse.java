package tools.dynamia.modules.security;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {

    private String name;
    private String token;
    private String type;
    private String username;
    private Long userId;
    private Date expires;

    private boolean valid;
    private String error;

    private String accountName;
    private String accountSubdomain;
    private Long accountId;

    private Boolean otp;

    private List<String> roles;

    public TokenResponse() {
    }

    public TokenResponse(String error) {
        this.error = error;
        this.valid = false;
    }

    public TokenResponse(String name, String accessToken, String username, Long userId, Date expires) {
        this.name = name;
        this.token = accessToken;
        this.username = username;
        this.userId = userId;
        this.expires = expires;
        this.valid = true;
        this.type = "bearer";
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountSubdomain() {
        return accountSubdomain;
    }

    public void setAccountSubdomain(String accountSubdomain) {
        this.accountSubdomain = accountSubdomain;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Boolean getOtp() {
        return otp;
    }

    public void setOtp(Boolean otp) {
        this.otp = otp;
    }
}
