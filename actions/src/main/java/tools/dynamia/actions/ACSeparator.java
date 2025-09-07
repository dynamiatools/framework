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

/**
 * Represents a separator component within an action group or menu.
 * <p>
 * This class is used to visually separate groups of actions in user interfaces.
 * It extends {@link ActionComponent} and overrides the {@link #isSeparator()} method to indicate its separator nature.
 * <p>
 * Instances of this class do not hold any action logic or properties other than being a separator.
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
public class ACSeparator extends ActionComponent {

    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ACSeparator} instance.
     * <p>
     * The separator does not require an id or label, so both are set to {@code null}.
     */
    public ACSeparator() {
        super(null, null);
    }

    /**
     * Indicates that this component is a separator.
     *
     * @return {@code true} always, as this class represents a separator
     */
    @Override
    public boolean isSeparator() {
        return true;
    }
}
