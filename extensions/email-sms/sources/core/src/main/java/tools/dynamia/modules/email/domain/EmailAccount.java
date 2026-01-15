
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

package tools.dynamia.modules.email.domain;

import tools.dynamia.domain.contraints.NotEmpty;
import tools.dynamia.modules.saas.jpa.SimpleEntitySaaS;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Represents an email account.
 */
@Entity
@Table(name = "email_accounts")
public class EmailAccount extends SimpleEntitySaaS {

    /**
     *
     */
    private static final long serialVersionUID = 3769420109733883374L;
    @NotEmpty(message = "Enter account's name")
    private String name;
    @NotEmpty(message = "Enter account's username")
    private String username;
    private String password;
    @NotEmpty(message = "Enter server host name or ip address")
    private String serverAddress;
    private String fromAddress;
    private int port = 25;
    private boolean useTTLS;
    private boolean loginRequired;
    private boolean preferred;
    private boolean notifications;
    private String enconding;

    private boolean smsEnabled;
    private String smsUsername;
    private String smsPassword;
    private String smsRegion;
    private String smsDefaultPrefix;
    private String smsSenderID;
    private boolean useSSL;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isUseTTLS() {
        return useTTLS;
    }

    public void setUseTTLS(boolean useTTLS) {
        this.useTTLS = useTTLS;
    }

    public boolean isLoginRequired() {
        return loginRequired;
    }

    public void setLoginRequired(boolean loginRequired) {
        this.loginRequired = loginRequired;
    }

    public boolean isPreferred() {
        return preferred;
    }

    public void setPreferred(boolean preferred) {
        this.preferred = preferred;
    }

    public String getEnconding() {
        return enconding;
    }

    public void setEnconding(String enconding) {
        this.enconding = enconding;
    }

    public String getSmsUsername() {
        return smsUsername;
    }

    public void setSmsUsername(String smsUsername) {
        this.smsUsername = smsUsername;
    }

    public String getSmsPassword() {
        return smsPassword;
    }

    public void setSmsPassword(String smsPassword) {
        this.smsPassword = smsPassword;
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }

    public void setSmsEnabled(boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }

    public String getSmsDefaultPrefix() {
        return smsDefaultPrefix;
    }

    public void setSmsDefaultPrefix(String smsDefaultPrefix) {
        this.smsDefaultPrefix = smsDefaultPrefix;
    }

    public String getSmsRegion() {
        return smsRegion;
    }

    public void setSmsRegion(String smsRegion) {
        this.smsRegion = smsRegion;
    }

    public String getSmsSenderID() {
        return smsSenderID;
    }

    public void setSmsSenderID(String smsSenderID) {
        this.smsSenderID = smsSenderID;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", name, fromAddress);
    }


    public boolean isUseSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }
}
