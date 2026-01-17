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
package tools.dynamia.commons;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/***
 * Helper class to store user information and attributes.
 * <p>
 * This class provides a container for user-related data such as username, full name, email, unique identifier, profile image, and other metadata. It also supports custom attributes via a thread-safe map.
 * <p>
 * Typical usage involves setting user properties and storing additional attributes as needed for authentication, authorization, or personalization purposes.
 * <p>
 * Thread safety: The attributes map is thread-safe via {@link ConcurrentHashMap}.
 * <p>
 * Serialization: Implements {@link Serializable} for session or distributed storage.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo implements Serializable {

    private String username;
    private String fullName;
    private String email;
    private String uid;
    private String image;
    private Long id;
    private LocalDate date;
    private Locale locale;
    private ZoneId zoneId;
    private LocalDate creationDate;
    private String profilePath;
    private String location;
    private boolean logged;
    private final Map<String, Serializable> attributes = new ConcurrentHashMap<>();

    /**
     * Gets the date associated with the user (e.g., creation or last login).
     *
     * @return the date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Sets the date associated with the user.
     *
     * @param date the date to set
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Gets the profile path for the user (e.g., URL or file path).
     *
     * @return the profile path
     */
    public String getProfilePath() {
        return profilePath;
    }

    /**
     * Sets the profile path for the user.
     *
     * @param profilePath the profile path to set
     */
    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }

    /**
     * Gets the image URL or path for the user profile.
     * If not set, returns a default image path.
     *
     * @return the image path or URL
     */
    public String getImage() {
        if (image == null) {
            image = "/static/images/no-user-photo.jpg";
        }
        return image;
    }

    /**
     * Sets the image URL or path for the user profile.
     *
     * @param image the image path or URL to set
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Gets the username of the user.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the user.
     *
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the full name of the user.
     *
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the full name of the user.
     *
     * @param fullName the full name to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets the email address of the user.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the user.
     *
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the unique identifier of the user.
     *
     * @return the unique identifier
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the unique identifier of the user.
     *
     * @param uid the unique identifier to set
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Gets the ID of the user.
     *
     * @return the ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the ID of the user.
     *
     * @param id the ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the location of the user.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of the user.
     *
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Checks if the user is logged in.
     *
     * @return true if logged in, false otherwise
     */
    public boolean isLogged() {
        return logged;
    }

    /**
     * Sets the logged-in status of the user.
     *
     * @param logged the logged-in status to set
     */
    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    /**
     * Checks if the user is enabled (i.e., has a valid username).
     *
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return username != null;
    }

    /**
     * Adds a custom attribute for the user.
     *
     * @param name  the attribute name
     * @param value the attribute value
     */
    public void addAttribute(String name, Serializable value) {
        attributes.put(name, value);
    }

    /**
     * Gets a custom attribute value by name.
     *
     * @param name the attribute name
     * @return the attribute value, or null if not set
     */
    public Serializable getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Gets the set of custom attribute keys.
     *
     * @return the set of attribute keys
     */
    public Set<String> getAttributesKeys() {
        return attributes.keySet();
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
