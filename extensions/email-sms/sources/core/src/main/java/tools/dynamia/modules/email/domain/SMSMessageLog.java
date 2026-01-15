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

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tools.dynamia.modules.saas.jpa.BaseEntitySaaS;

@Entity
@Table(name = "email_sms_log")
public class SMSMessageLog extends BaseEntitySaaS {

    private String phoneNumber;
    private String text;
    private String result;

    public SMSMessageLog() {
    }

    public SMSMessageLog(String phoneNumber, String text, String result) {
        this.phoneNumber = phoneNumber;
        this.text = text;
        this.result = result;
    }

    public SMSMessageLog(String phoneNumber, String text, String result, Long accountId) {
        this.phoneNumber = phoneNumber;
        this.text = text;
        this.result = result;
        setAccountId(accountId);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return getPhoneNumber();
    }
}
