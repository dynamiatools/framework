package tools.dynamia.modules.saas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import tools.dynamia.domain.jpa.SimpleEntity;

@Entity
@Table(name = "saas_payments_providers")
public class AccountPaymentProvider extends SimpleEntity {

    @NotNull
    private String name;
    @Column(length = 500)
    private String apiKey;
    @Column(length = 500)
    private String apiSecret;

    @Column(length = 500)
    private String integritySecret;

    @Column(length = 500)
    private String webCheckoutSecret;
    @Column(length = 500)
    private String serviceURL;
    private String merchantId;
    private boolean testMode;
    private boolean active = true;

    private String paymentProcessor;

    private String extra0;
    private String extra1;
    private String extra2;
    private String extra3;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getIntegritySecret() {
        return integritySecret;
    }

    public void setIntegritySecret(String integritySecret) {
        this.integritySecret = integritySecret;
    }

    public String getWebCheckoutSecret() {
        return webCheckoutSecret;
    }

    public void setWebCheckoutSecret(String webcheckoutSecret) {
        this.webCheckoutSecret = webcheckoutSecret;
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

    @Override
    public String toString() {
        return name;
    }

    public String getPaymentProcessor() {
        return paymentProcessor;
    }

    public void setPaymentProcessor(String paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }
}
