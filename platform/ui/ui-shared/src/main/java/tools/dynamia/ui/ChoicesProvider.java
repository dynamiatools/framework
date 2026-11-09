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
package tools.dynamia.ui;

import java.util.List;
import java.util.function.Consumer;

/**
 * SPI behind {@link UIChoices}. ZK opens a list selector window; a headless run sends a {@code CHOICE} step.
 */
@UIPort(name = "choices", steps = {"CHOICE"})
public interface ChoicesProvider {

    /**
     * Asks the user to choose and calls {@code onChoice} with the chosen options. It is not called when the user cancels
     * or chooses nothing.
     */
    <T> void choose(ChoiceOptions<T> options, Consumer<List<T>> onChoice);

    /**
     * Lets the user search and pick one record of a big catalog. {@code onPick} is not called when the user cancels. The
     * default implementation refuses, so an adapter without a searchable picker fails loudly.
     *
     * @param title   title of the picker, may be null
     * @param lookup  where the records come from
     * @param onPick  receives the record the user picked
     * @param <T>     the record type
     * @throws UnsupportedOperationException when the front end cannot search
     */
    default <T> void pickEntity(String title, EntityLookup<T> lookup, Consumer<T> onPick) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " cannot search " + lookup.name());
    }
}
