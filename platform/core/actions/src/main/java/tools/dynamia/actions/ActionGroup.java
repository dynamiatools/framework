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
package tools.dynamia.actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a group of {@link Action} objects that are rendered together in the UI.
 * <p>
 * Each group has a name and an alignment property, which can be used to control how actions are displayed.
 * The group maintains a list of actions and provides factory methods for instantiation.
 * <p>
 * Equality is based on the group name.
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
public class ActionGroup implements Serializable {

    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Predefined constant representing no group, aligned to the left.
     */
    public static final ActionGroup NONE = new ActionGroup("NONE", "left");

    /**
     * The name of the group.
     */
    private String name;

    /**
     * The alignment of the group (e.g., "left", "right").
     */
    private String align;

    /**
     * The list of actions contained in this group.
     */
    private final List<Action> actions = new ArrayList<>();

    /**
     * Factory method to create a new {@code ActionGroup} with the specified name and alignment.
     *
     * @param name  the name of the group
     * @param align the alignment of the group (e.g., "left", "right")
     * @return a new instance of {@code ActionGroup}
     */
    public static ActionGroup get(String name, String align) {
        return new ActionGroup(name, align);
    }

    /**
     * Factory method to create a new {@code ActionGroup} with the specified name and default alignment ("left").
     *
     * @param name the name of the group
     * @return a new instance of {@code ActionGroup}
     */
    public static ActionGroup get(String name) {
        return new ActionGroup(name);
    }

    /**
     * Constructs an {@code ActionGroup} with the specified name and alignment.
     *
     * @param name  the name of the group
     * @param align the alignment of the group
     */
    private ActionGroup(String name, String align) {
        this.name = name;
        this.align = align;
    }

    /**
     * Constructs an {@code ActionGroup} with the specified name and default alignment ("left").
     *
     * @param name the name of the group
     */
    public ActionGroup(String name) {
        this.name = name;
        this.align = "left";
    }

    /**
     * Returns the alignment of this group.
     *
     * @return the alignment string
     */
    public String getAlign() {
        return align;
    }

    /**
     * Sets the alignment of this group.
     *
     * @param align the alignment string to set
     */
    public void setAlign(String align) {
        this.align = align;
    }

    /**
     * Returns the name of this group.
     *
     * @return the name of the group
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this group.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the list of actions in this group.
     *
     * @return the list of {@link Action} objects
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * Checks if this group is equal to another object. Equality is based on the group name.
     *
     * @param obj the object to compare
     * @return {@code true} if the names are equal and the object is an {@code ActionGroup}, {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ActionGroup other = (ActionGroup) obj;
        return Objects.equals(this.name, other.name);
    }

    /**
     * Returns the hash code for this group, based on its name.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
