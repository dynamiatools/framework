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
package tools.dynamia.navigation;

import tools.dynamia.commons.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Define a Navigaion element in a {@link Module} like {@link Page},{@link PageGroup} or custom elements
 *
 * @author Mario A. Serrano Leones
 */
public class NavigationElement<T extends NavigationElement> implements Serializable, Comparable<NavigationElement>, Cloneable, PropertyChangeListenerContainer {

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public static final String PATH_SEPARATOR = "/";

    private String id;
    private String name;
    private String longName;
    private String description;
    private String icon;
    private boolean enable = true;
    private boolean visible = true;
    private String renderOnUserRoles;
    private double position = 0;
    private boolean reference;
    private final Map<String, Object> attributes = new HashMap<>();
    private boolean alwaysAllowed = false;
    private String iconSize;
    private String badge;
    private Supplier<String> longNameSupplier;
    private Supplier<String> customLabelSupplier;
    private String customLabel;


    public NavigationElement() {
    }

    public NavigationElement(String id, String name) {
        setId(id);
        this.name = name;
    }

    public NavigationElement(String id, String name, String description) {
        setId(id);
        this.name = name;
        this.description = description;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public void setLongNameSupplier(Supplier<String> longNameSupplier) {
        this.longNameSupplier = longNameSupplier;
    }

    public Supplier<String> getLongNameSupplier() {
        return longNameSupplier;
    }

    public void addAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public Set<String> getAttributesNames() {
        return attributes.keySet();
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public boolean isAlwaysAllowed() {
        return alwaysAllowed;
    }

    public void setAlwaysAllowed(boolean alwaysAllowed) {
        this.alwaysAllowed = alwaysAllowed;
    }

    public boolean isReference() {
        return reference;
    }

    protected void setReference(boolean reference) {
        this.reference = reference;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIconSize() {
        if (iconSize == null || iconSize.isBlank()) {
            iconSize = "SMALL";
        }
        return iconSize;
    }

    /**
     * SMALL, NORMAL, LARGE
     *
     * @param iconSize
     */
    public void setIconSize(String iconSize) {
        this.iconSize = iconSize;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getRenderOnUserRoles() {
        return renderOnUserRoles;
    }

    public void setRenderOnUserRoles(String renderOnUserRoles) {
        this.renderOnUserRoles = renderOnUserRoles;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVirtualPath() {
        return id;
    }

    public String getPrettyVirtualPath() {
        return StringUtils.simplifiedString(getName());
    }

    @Override
    public String toString() {
        if (isReference()) {
            return "REF: " + getId();
        }
        return getName();
    }

    @Override
    public NavigationElement clone() {
        NavigationElement ne = new NavigationElement();
        ne.description = description;
        ne.enable = enable;
        ne.icon = icon;
        ne.id = id;
        ne.name = name;
        ne.renderOnUserRoles = renderOnUserRoles;
        ne.visible = visible;
        ne.position = position;
        ne.reference = reference;
        ne.alwaysAllowed = alwaysAllowed;
        ne.attributes.putAll(attributes);
        ne.longNameSupplier = longNameSupplier;
        ne.badge = badge;
        ne.iconSize = iconSize;
        ne.customLabel = customLabel;
        ne.customLabelSupplier = customLabelSupplier;

        return ne;
    }

    @Override
    public int compareTo(NavigationElement o) {
        NavigationElementComparator comparator = new NavigationElementComparator();
        return comparator.compare(this, o);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NavigationElement other = (NavigationElement) obj;
        return Objects.equals(this.getVirtualPath(), other.getVirtualPath());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.getVirtualPath() != null ? this.getVirtualPath().hashCode() : 0);
        return hash;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        var old = this.badge;
        this.badge = badge;
        firePropertyChange("badge", old, badge);
    }

    public String getLocalizedName() {
        return getLocalizedName(Messages.getDefaultLocale());
    }

    public String getLocalizedName(Locale locale) {
        return getLocalizedText(locale, "name", name);
    }

    public String getLocalizedDescription() {
        return getLocalizedDescription(Messages.getDefaultLocale());
    }

    public String getLocalizedDescription(Locale locale) {
        return getLocalizedText(locale, "description", description);
    }

    public String getLocalizedIcon(Locale locale) {
        return getLocalizedText(locale, "icon", icon);
    }

    protected String getLocalizedText(Locale locale, String sufix, String defaultValue) {
        String key = msgKey(sufix);
        String text = Messages.get(NavigationElement.class, locale, key);
        if (key.equals(text)) {
            return defaultValue;
        }
        return text;
    }

    public void setCustomLabel(String customLabel) {
        var old = this.customLabel;
        this.customLabel = customLabel;
        firePropertyChange("customLabel", old, customLabel);
    }

    public String getCustomLabel() {
        if (customLabelSupplier != null) {
            return customLabelSupplier.get();
        }
        return customLabel;
    }

    public void setCustomLabelSupplier(Supplier<String> customLabelSupplier) {
        this.customLabelSupplier = customLabelSupplier;
    }

    public Supplier<String> getCustomLabelSupplier() {
        return customLabelSupplier;
    }

    protected String msgKey(String sufix) {
        return getVirtualPath() + "." + sufix;
    }

    public T id(String id) {
        setId(id);
        //noinspection unchecked
        return (T) this;
    }


    public T longName(String longName) {
        setLongName(longName);
        //noinspection unchecked
        return (T) this;
    }

    public T name(String name) {
        setName(name);
        //noinspection unchecked
        return (T) this;
    }

    public T description(String description) {
        setDescription(description);
        //noinspection unchecked
        return (T) this;
    }

    public T icon(String icon) {
        setIcon(icon);
        //noinspection unchecked
        return (T) this;
    }

    public T iconSize(String iconSize) {
        setIconSize(iconSize);
        //noinspection unchecked
        return (T) this;
    }

    public T position(double position) {
        setPosition(position);
        //noinspection unchecked
        return (T) this;
    }

    public T badge(String badge) {
        setBadge(badge);
        //noinspection unchecked
        return (T) this;
    }

    public T customLabel(String customLabel) {
        setCustomLabel(customLabel);
        //noinspection unchecked
        return (T) this;
    }

    public T customLabelSupplier(Supplier<String> customLabelSupplier) {
        setCustomLabelSupplier(customLabelSupplier);
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected void clearPropertyChangeListeners() {
        propertyChangeSupport.clearListeners();
    }
}
