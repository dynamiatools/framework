package tools.dynamia.modules.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "dynamia.security")
public class DynamiaSecurityConfigurationProperties {

    private String jwtSecretKey;
    private Duration jwtExpiration = Duration.ofHours(2);
    private Duration jwtRefreshInterval = Duration.ofDays(10);
    private Duration rememberMeDuration = Duration.ofDays(15);
    private String issuer = "dynamia";


    public String getJwtSecretKey() {
        return jwtSecretKey;
    }

    public void setJwtSecretKey(String jwtSecretKey) {
        this.jwtSecretKey = jwtSecretKey;
    }

    public Duration getJwtExpiration() {
        return jwtExpiration;
    }

    public void setJwtExpiration(Duration jwtExpiration) {
        this.jwtExpiration = jwtExpiration;
    }

    public Duration getJwtRefreshInterval() {
        return jwtRefreshInterval;
    }

    public void setJwtRefreshInterval(Duration jwtRefreshInterval) {
        this.jwtRefreshInterval = jwtRefreshInterval;
    }

    public Duration getRememberMeDuration() {
        return rememberMeDuration;
    }

    public void setRememberMeDuration(Duration rememberMeDuration) {
        this.rememberMeDuration = rememberMeDuration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
