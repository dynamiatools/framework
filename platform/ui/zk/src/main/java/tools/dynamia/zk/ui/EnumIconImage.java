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

package tools.dynamia.zk.ui;

import org.zkoss.zhtml.I;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Span;
import tools.dynamia.ui.icons.Icon;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.ui.icons.IconType;
import tools.dynamia.ui.icons.IconsTheme;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

/**
 * A ZK component that displays an icon representation for enum values.
 * This component extends Span and provides a visual way to render enum constants
 * using icons instead of plain text. Each enum ordinal can be mapped to a specific icon name.
 *
 * <p>The component supports both image-based and font-based icons through the IconsTheme system.
 * If no icon mapping is found for a given enum value, it falls back to displaying the enum name as text.</p>
 *
 * <p>Example usage in ZUL:</p>
 * <pre>{@code
 * <enumIconImage value="@bind(vm.status)"
 *                iconsNames="check-circle,clock,times-circle"
 *                size="LARGE"/>
 * }</pre>
 *
 * <p>This component is registered in both ComponentAliasIndex and BindingComponentIndex
 * for easy integration with ZK's data binding system.</p>
 *
 * @see IconsTheme
 * @see IconSize
 * @see LoadableOnly
 */
public class EnumIconImage extends Span implements LoadableOnly {

    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = -5975771607086380537L;

    /**
     * Static initializer block that registers this component in the framework's index systems.
     * Adds the component to ComponentAliasIndex for alias resolution and to BindingComponentIndex
     * for data binding support on the "value" property.
     */
    static {
        ComponentAliasIndex.getInstance().add(EnumIconImage.class);
        BindingComponentIndex.getInstance().put("value", EnumIconImage.class);
    }

    /**
     * The size of the icon to be displayed.
     */
    private IconSize size = IconSize.NORMAL;

    /**
     * The enum value to be displayed as an icon.
     */
    private Enum value;

    /**
     * Array of icon names mapped to enum ordinals.
     * Each position in the array corresponds to an enum ordinal value.
     */
    private String[] iconsNames;

    /**
     * Sets the value to be displayed as an icon.
     * This method accepts an Object parameter but only processes Enum values.
     * Non-enum values are ignored.
     *
     * @param value the enum value to display. If not an Enum instance, the method does nothing
     *
     * Example:
     * <pre>{@code
     * EnumIconImage iconImage = new EnumIconImage();
     * iconImage.setValue(Status.ACTIVE);
     * }</pre>
     */
    public void setValue(Object value) {
        if (value instanceof Enum) {
            this.value = (Enum) value;
            render();
        }
    }

    /**
     * Renders the icon representation of the current enum value.
     * This method clears existing children and creates the appropriate component
     * based on the icon type (Image or font-based icon). If no icon mapping exists,
     * it displays the enum name as a Label.
     *
     * <p>The rendering process:</p>
     * <ul>
     *   <li>Clears all existing child components</li>
     *   <li>Retrieves the icon name mapped to the enum's ordinal</li>
     *   <li>Creates an Image component for image-based icons</li>
     *   <li>Creates an I (italic) component for font-based icons</li>
     *   <li>Falls back to a Label with the enum name if no icon is found</li>
     * </ul>
     */
    private void render() {
        getChildren().clear();
        if (value != null) {
            setTooltiptext(null);
            String iconName = getIconName();
            if (iconName != null) {
                Icon icon = IconsTheme.get().getIcon(iconName);
                setTooltiptext(value.name());
                if (icon.getType() == IconType.IMAGE) {
                    Image image = new Image();
                    image.setParent(this);
                    ZKUtil.configureComponentIcon(icon, image, size);
                } else {
                    I i = new I();
                    i.setParent(this);
                    ZKUtil.configureComponentIcon(icon, i, size);
                }
            } else {
                appendChild(new Label(value.name()));
            }
        }
    }

    /**
     * Retrieves the icon name mapped to the current enum value's ordinal.
     * Returns null if the value is null, the iconsNames array is not set,
     * or if the ordinal is out of bounds.
     *
     * @return the icon name corresponding to the enum's ordinal position, or null if not found
     */
    private String getIconName() {
        try {
            if (value != null) {
                return iconsNames[value.ordinal()];
            }

        } catch (Exception ignored) {

        }
        return null;
    }

    /**
     * Gets the current icon size.
     *
     * @return the size of the icon
     */
    public IconSize getSize() {
        return size;
    }

    /**
     * Sets the icon size.
     *
     * @param size the desired icon size
     */
    public void setSize(IconSize size) {
        this.size = size;
    }

    /**
     * Gets the current enum value being displayed.
     *
     * @return the enum value, or null if not set
     */
    public Enum getValue() {
        return value;
    }

    /**
     * Sets the icon size using a string representation.
     * The string is converted to uppercase and parsed as an IconSize enum value.
     *
     * @param size the icon size as a string (e.g., "small", "normal", "large")
     * @throws IllegalArgumentException if the size string does not match any IconSize value
     */
    public void setSize(String size) {
        setSize(IconSize.valueOf(size.toUpperCase()));
    }

    /**
     * Gets the array of icon names mapped to enum ordinals.
     *
     * @return the array of icon names
     */
    public String[] getIconsNames() {
        return iconsNames;
    }

    /**
     * Sets the array of icon names and triggers a re-render of the component.
     * Each position in the array corresponds to an enum ordinal value.
     *
     * @param iconsNames array of icon names to map to enum ordinals
     *
     * Example:
     * <pre>{@code
     * String[] icons = {"check", "clock", "times"};
     * iconImage.setIconsNamesValues(icons);
     * }</pre>
     */
    public void setIconsNamesValues(String[] iconsNames) {
        this.iconsNames = iconsNames;
        render();
    }

    /**
     * Sets the icon names from a comma-separated string and triggers a re-render.
     * Spaces are automatically removed from the input string.
     *
     * @param iconsNames comma-separated string of icon names (e.g., "check,clock,times")
     *
     * Example:
     * <pre>{@code
     * iconImage.setIconsNames("check-circle, clock, times-circle");
     * }</pre>
     */
    public void setIconsNames(String iconsNames) {
        if (iconsNames != null) {
            setIconsNamesValues(iconsNames.replace(" ", "").split(","));
        }
    }
}
