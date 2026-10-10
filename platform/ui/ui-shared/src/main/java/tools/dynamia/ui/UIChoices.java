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
import java.util.function.Function;

/**
 * Facade for asking the user to choose among options, whatever front end it is on (see {@link UIFacades}).
 *
 * <pre>{@code
 * UIChoices.chooseOne("Select new storage", storages, EntityFileStorage::getName, storage -> move(storage));
 * }</pre>
 * Like a question in {@link UIMessages}, what happens after the choice goes in the callback.
 */
public final class UIChoices {

    private UIChoices() {
    }

    /**
     * Asks for one option, shown with {@code String.valueOf}.
     */
    public static <T> void chooseOne(String title, List<T> options, Consumer<T> onChoice) {
        chooseOne(title, options, String::valueOf, onChoice);
    }

    /**
     * Asks for one option.
     *
     * @param label text shown for each option
     */
    public static <T> void chooseOne(String title, List<T> options, Function<T, String> label, Consumer<T> onChoice) {
        UIFacades.resolve(ChoicesProvider.class).choose(new ChoiceOptions<>(title, options, label, false), chosen -> {
            if (!chosen.isEmpty()) {
                onChoice.accept(chosen.get(0));
            }
        });
    }

    /**
     * Asks for any number of options (at least one).
     */
    public static <T> void chooseMany(String title, List<T> options, Function<T, String> label, Consumer<List<T>> onChoice) {
        UIFacades.resolve(ChoicesProvider.class).choose(new ChoiceOptions<>(title, options, label, true), onChoice);
    }
}
