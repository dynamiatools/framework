/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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
package tools.dynamia.domain.util;


import tools.dynamia.commons.BeanUtils;

import java.util.Map;

/**
 * Clase contenedor de datos de contacto como direccion, telefono, email,
 * ciudad, pais, etc.
 */

public abstract class AbstractContactInfo implements java.io.Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -2249627623856723930L;

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

    /**
     * Gets the region.
     *
     * @return the region
     */
    public String getRegion() {
        return region;
    }

    /**
     * Sets the region.
     *
     * @param region the new region
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Gets the address.
     *
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address.
     *
     * @param address the new address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the city.
     *
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city.
     *
     * @param city the new city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Gets the email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     *
     * @param email the new email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the mobile number.
     *
     * @return the mobile number
     */
    public String getMobileNumber() {
        return mobileNumber;
    }

    /**
     * Sets the mobile number.
     *
     * @param mobileNumber the new mobile number
     */
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    /**
     * Gets the country.
     *
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the country.
     *
     * @param country the new country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Gets the phone number.
     *
     * @return the phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the phone number.
     *
     * @param phoneNumber the new phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    public String getPhones() {
        return (getMobileNumber() + " - " + getPhoneNumber()).replace("null", "");
    }

    public void setPhones(String phones) {

    }


    public Map<String, Object> toMap(String prefix) {
        return BeanUtils.getValuesMaps(prefix, this);
    }
}


