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
package tools.dynamia.domain.jpa;

import tools.dynamia.commons.StringUtils;
import tools.dynamia.domain.util.AbstractContactInfo;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;


/**
 * Clase contenedor de datos de contacto como direccion, telefono, email,
 * ciudad, pais, etc.
 */
@Embeddable
public class ContactInfo extends AbstractContactInfo implements java.io.Serializable {

    public static final ContactInfo EMPTY;

    /**
     * The address.
     */
    private String address;

    /**
     * The phone number.
     */
    private String phoneNumber;

    /**
     * The city.
     */
    private String city;

    /**
     * The country.
     */
    private String country;

    /**
     * The email.
     */
    private String email;

    /**
     * The mobile number.
     */
    private String mobileNumber;

    /**
     * The region.
     */
    private String region;

    static {
        EMPTY = new ContactInfo();
        EMPTY.setRegion("");
        EMPTY.setPhoneNumber("");
        EMPTY.setEmail("");
        EMPTY.setMobileNumber("");
        EMPTY.setCity("");
        EMPTY.setAddress("");
        EMPTY.setCountry("");
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String getCity() {
        return city;
    }

    @Override
    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String getCountry() {
        return country;
    }

    @Override
    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = StringUtils.trimAllWhitespace(email);
    }

    @Override
    public String getMobileNumber() {
        return mobileNumber;
    }

    @Override
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @Override
    public String getRegion() {
        return region;
    }

    @Override
    public void setRegion(String region) {
        this.region = region;
    }

    @Transient
    public String getPhones() {
        return (getMobileNumber() + " - " + getPhoneNumber()).replace("null", "");
    }


}


