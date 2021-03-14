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
package tools.dynamia.navigation;

import tools.dynamia.commons.Messages;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.integration.ms.Message;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.ui.icons.IconsTheme;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Mario A. Serrano Leones
 */
public class NavigationElement<T extends NavigationElement> implements Serializable, Comparable<NavigationElement>, Cloneable {

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
    private Map<String, Object> attributes = new HashMap<>();
    private boolean alwaysAllowed = false;
    private IconSize iconSize = IconSize.SMALL;
    private String badge;
    private Supplier<String> longNameSupplier;
    protected String virtualPath;


    public NavigationElement() {
    }

    public NavigationElement(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public NavigationElement(String id, String name, String description) {
        this.id = id;
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
        String realIcon = icon;
        if (icon != null && icon.startsWith("icons:")) {
            realIcon = IconsTheme.get().getIcon(icon).getRealPath(getIconSize());
        }
        return realIcon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public IconSize getIconSize() {
        if (iconSize == null) {
            iconSize = IconSize.SMALL;
        }
        return iconSize;
    }

    public void setIconSize(IconSize iconSize) {

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
        return StringUtils.simplifiedString(name);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public NavigationElement clone() {
        NavigationElement ne = new NavigationElement();
        ne.setDescription(description);
        ne.setEnable(enable);
        ne.setIcon(icon);
        ne.setId(id);
        ne.setName(name);
        ne.setRenderOnUserRoles(renderOnUserRoles);
        ne.setVisible(visible);
        ne.setPosition(position);
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
        return (this.id == null) ? (other.id == null) : this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
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

    protected String msgKey(String sufix) {
        return getVirtualPath() + "." + sufix;
    }

    public T id(String id) {
        setId(id);
        return (T) this;
    }


    public T longName(String longName) {
        setLongName(longName);
        return (T) this;
    }

    public T name(String name) {
        setName(name);
        return (T) this;
    }

    public T description(String description) {
        setDescription(description);
        return (T) this;
    }

    public T icon(String icon) {
        setIcon(icon);
        return (T) this;
    }

    public T iconSize(IconSize iconSize) {
        setIconSize(iconSize);
        return (T) this;
    }

    public T position(double position) {
        setPosition(position);
        return (T) this;
    }

}
