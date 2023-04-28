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
 * Actions in the same group are rendered together
 *
 * @author Mario A. Serrano Leones
 */
public class ActionGroup implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final ActionGroup NONE = new ActionGroup("NONE", "left");
    private String name;
    private String align;
    private final List<Action> actions = new ArrayList<>();

    public static ActionGroup get(String name, String align) {
        return new ActionGroup(name, align);

    }

    public static ActionGroup get(String name) {
        return new ActionGroup(name);
    }

    private ActionGroup(String name, String align) {
        this.name = name;
        this.align = align;
    }

    public ActionGroup(String name) {
        this.name = name;
        this.align = "left";
    }

    public String getAlign() {
        return align;
    }

    public void setAlign(String align) {
        this.align = align;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Action> getActions() {
        return actions;
    }

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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
